package com.CommunicationService.CommunicationService.service;

import com.CommunicationService.CommunicationService.model.Feedback;
import com.google.firebase.database.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class FeedbackService {

    // Top-level "Feedback" table node
    private DatabaseReference getFeedbackRef() {
        return FirebaseDatabase.getInstance().getReference("Feedback");
    }

    // "RegisteredUsers" table node
    private DatabaseReference getUsersRef() {
        return FirebaseDatabase.getInstance().getReference("RegisteredUsers");
    }

    // Save feedback to separate "Feedback" node with FK (userId) and fetched userName
    public Feedback saveFeedback(Feedback feedback) {
        CompletableFuture<Feedback> future = new CompletableFuture<>();

        // Look up user details using FK (userId) from RegisteredUsers
        getUsersRef().child(feedback.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String userName = "Anonymous User";

                if (snapshot.exists()) {
                    if (snapshot.hasChild("name") && snapshot.child("name").getValue() != null) {
                        userName = snapshot.child("name").getValue(String.class);
                    } else if (snapshot.hasChild("email") && snapshot.child("email").getValue() != null) {
                        userName = snapshot.child("email").getValue(String.class);
                    }
                }

                feedback.setUserName(userName);

                // Generate new push key in "Feedback" table
                DatabaseReference newRef = getFeedbackRef().push();
                String generatedKey = newRef.getKey();
                String createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

                feedback.setId(generatedKey);
                feedback.setCreatedAt(createdAt);

                Map<String, Object> data = new HashMap<>();
                data.put("id", generatedKey);
                data.put("userId", feedback.getUserId());     // FK to RegisteredUsers
                data.put("userName", userName);               // Saved user name
                data.put("userType", feedback.getUserType());
                data.put("message", feedback.getMessage());
                data.put("rating", feedback.getRating());
                data.put("createdAt", createdAt);

                newRef.setValue(data, (error, ref) -> {
                    if (error != null) {
                        future.completeExceptionally(error.toException());
                    } else {
                        future.complete(feedback);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving feedback: " + e.getMessage());
        }
    }

    // Retrieve all entries explicitly extracting fields to ensure userName is included
    public List<Feedback> getAllFeedback() {
        CompletableFuture<List<Feedback>> future = new CompletableFuture<>();

        getFeedbackRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Feedback> feedbackList = new ArrayList<>();

                for (DataSnapshot itemSnap : snapshot.getChildren()) {
                    String id = itemSnap.child("id").getValue(String.class);
                    if (id == null) id = itemSnap.getKey();

                    String userId = itemSnap.child("userId").getValue(String.class);
                    String userName = itemSnap.child("userName").getValue(String.class);
                    String userType = itemSnap.child("userType").getValue(String.class);
                    String message = itemSnap.child("message").getValue(String.class);

                    Long ratingLong = itemSnap.child("rating").getValue(Long.class);
                    Integer rating = ratingLong != null ? ratingLong.intValue() : 5;

                    String createdAt = itemSnap.child("createdAt").getValue(String.class);

                    Feedback fb = new Feedback(id, userId, userName, userType, message, rating, createdAt);
                    feedbackList.add(0, fb); // Newest first
                }
                future.complete(feedbackList);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching feedback: " + e.getMessage());
        }
    }

    // Update feedback record by feedbackId
    public Feedback updateFeedback(String feedbackId, Feedback feedbackDetails) {
        DatabaseReference feedbackRef = getFeedbackRef().child(feedbackId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("message", feedbackDetails.getMessage());
        updates.put("rating", feedbackDetails.getRating());
        updates.put("userType", feedbackDetails.getUserType());

        CompletableFuture<Feedback> future = new CompletableFuture<>();
        feedbackRef.updateChildren(updates, (error, ref) -> {
            if (error != null) {
                future.completeExceptionally(error.toException());
            } else {
                feedbackDetails.setId(feedbackId);
                future.complete(feedbackDetails);
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error updating feedback: " + e.getMessage());
        }
    }

    // Delete feedback record by feedbackId
    public void deleteFeedback(String feedbackId) {
        DatabaseReference recordRef = getFeedbackRef().child(feedbackId);

        CompletableFuture<Void> future = new CompletableFuture<>();
        recordRef.removeValue((error, ref) -> {
            if (error != null) {
                future.completeExceptionally(error.toException());
            } else {
                future.complete(null);
            }
        });

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete feedback: " + e.getMessage());
        }
    }
}