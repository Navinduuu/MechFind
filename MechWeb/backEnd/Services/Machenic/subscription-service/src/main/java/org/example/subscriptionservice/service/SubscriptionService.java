package org.example.subscriptionservice.service;

import com.google.firebase.database.*;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class SubscriptionService {

    private DatabaseReference getSubscriptionsRef() {
        return FirebaseDatabase.getInstance().getReference("Subscriptions");
    }

    private DatabaseReference getPaymentsRef() {
        return FirebaseDatabase.getInstance().getReference("Payments");
    }

    public boolean saveSubscription(Map<String, Object> requestDto) {
        try {
            System.out.println("🔍 Extracting userId from payload...");
            Object userIdObj = requestDto.get("userId");
            String userId = userIdObj != null ? String.valueOf(userIdObj) : null;

            if (userId == null || userId.trim().isEmpty()) {
                System.out.println("⚠️ Validation Error: 'userId' is missing or null in the request!");
                return false;
            }

            System.out.println("💾 Saving to Firebase under Subscriptions/" + userId);
            CompletableFuture<Boolean> future = new CompletableFuture<>();

            getSubscriptionsRef().child(userId).setValue(requestDto, (error, ref) -> {
                if (error == null) {
                    future.complete(true);
                } else {
                    System.out.println("🔥 Firebase Error: " + error.getMessage());
                    future.complete(false);
                }
            });

            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println("❌ Exception in saveSubscription: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public Map<String, Object> getUserSubscription(String userId) {
        try {
            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            getSubscriptionsRef().child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = (Map<String, Object>) snapshot.getValue();
                        future.complete(data);
                    } else {
                        future.complete(null);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    future.completeExceptionally(error.toException());
                }
            });
            return future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException("Error fetching subscription: " + e.getMessage());
        }
    }

    public boolean cancelSubscription(String subscriptionId) {
        try {
            DatabaseReference subRef = getSubscriptionsRef().child(subscriptionId);
            CompletableFuture<Boolean> future = new CompletableFuture<>();

            subRef.removeValue((error, ref) -> {
                if (error == null) {
                    future.complete(true);
                } else {
                    future.complete(false);
                }
            });
            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            return false;
        }
    }
}