package org.example.identificationservice.service;

import com.google.firebase.database.*;
import org.example.identificationservice.controller.AuthController.RegisterInput;
import org.example.identificationservice.model.User;
import org.example.identificationservice.model.UserType;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class UserService {

    private String determineSubNodeName(UserType userType) {
        if (userType == null || userType == UserType.Mechanic) {
            return "Mechanic";
        }
        return "TowTruck";
    }

    public void validateEmailUniqueness(String email) {
        try {
            User existingUser = getUserByEmail(email);
            if (existingUser != null) {
                throw new RuntimeException("Email is already registered.");
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException("Error checking database for existing user. Please try again.");
        }
    }

    public User registerUser(RegisterInput input) {
        validateEmailUniqueness(input.getEmail());

        String subNodeName = determineSubNodeName(input.getUserType());
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("RegisteredUsers").child(subNodeName);
        String id = usersRef.push().getKey();

        if (id == null) {
            throw new RuntimeException("Failed to generate user ID.");
        }

        // Handle default specialty if user is a mechanic and specialty is not provided
        String resolvedSpeciality = input.getSpeciality();
        if (input.getUserType() == UserType.Mechanic && (resolvedSpeciality == null || resolvedSpeciality.trim().isEmpty())) {
            resolvedSpeciality = "ABS & Brake Mechanic";
        } else if (input.getUserType() == UserType.TowTruck) {
            resolvedSpeciality = null; // Tow trucks typically don't have mechanic specialties
        }

        User newUser = new User(
                input.getName(),
                resolvedSpeciality,
                input.getEmail(),
                input.getPassword(),
                input.getUserType(),
                input.getPhone(),
                input.getStreet(),
                input.getGender(),
                input.getLatitude(),
                input.getLongitude()
        );

        usersRef.child(id).setValueAsync(newUser);
        newUser.setId(id);

        return newUser;
    }

    public User loginUser(String email, String password) {
        try {
            User user = getUserByEmail(email);
            if (user == null || !user.getPassword().equals(password)) {
                throw new RuntimeException("Invalid email or password.");
            }
            return user;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException("Login failed due to database timeout: " + e.getMessage());
        }
    }

    public void changePassword(String email, String oldPassword, String newPassword) {
        try {
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("RegisteredUsers");
            String[] subNodes = {"Mechanic", "TowTruck"};

            CompletableFuture<Void> passwordChangeFuture = new CompletableFuture<>();
            final int[] pending = {subNodes.length};
            final boolean[] updated = {false};

            for (String subNode : subNodes) {
                DatabaseReference nodeRef = rootRef.child(subNode);
                nodeRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        synchronized (passwordChangeFuture) {
                            if (snapshot.exists() && !updated[0]) {
                                for (DataSnapshot child : snapshot.getChildren()) {
                                    User user = child.getValue(User.class);
                                    if (user != null && user.getPassword().equals(oldPassword)) {
                                        updated[0] = true;
                                        child.getRef().child("password").setValueAsync(newPassword);
                                        passwordChangeFuture.complete(null);
                                        return;
                                    }
                                }
                            }
                            pending[0]--;
                            if (pending[0] == 0 && !updated[0]) {
                                passwordChangeFuture.completeExceptionally(new RuntimeException("User not found or incorrect old password."));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        synchronized (passwordChangeFuture) {
                            pending[0]--;
                            if (pending[0] == 0 && !updated[0]) {
                                passwordChangeFuture.completeExceptionally(error.toException());
                            }
                        }
                    }
                });
            }

            passwordChangeFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Password change failed: " + e.getMessage());
        }
    }

    private CompletableFuture<User> findUserInNode(String subNodeName, String email) {
        CompletableFuture<User> cf = new CompletableFuture<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("RegisteredUsers").child(subNodeName);
        ref.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        User user = child.getValue(User.class);
                        if (user != null) {
                            user.setId(child.getKey());
                            cf.complete(user);
                            return;
                        }
                    }
                }
                cf.complete(null);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                cf.completeExceptionally(error.toException());
            }
        });
        return cf;
    }

    private User getUserByEmail(String email) throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<User> mechanicFuture = findUserInNode("Mechanic", email);
        CompletableFuture<User> towTruckFuture = findUserInNode("TowTruck", email);

        return CompletableFuture.allOf(mechanicFuture, towTruckFuture)
                .thenApply(v -> {
                    User mech = mechanicFuture.join();
                    if (mech != null) return mech;
                    return towTruckFuture.join();
                })
                .get(10, TimeUnit.SECONDS);
    }
}