package org.example.subscriptionservice.controller;

import org.example.subscriptionservice.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mechfind/subscriptions")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody Map<String, Object> requestDto) {
        System.out.println("📥 Received subscription request payload: " + requestDto);

        boolean success = subscriptionService.saveSubscription(requestDto);

        if (success) {
            System.out.println("✅ Subscription saved successfully!");
            return ResponseEntity.ok(Map.of("message", "Subscribed successfully"));
        }

        System.out.println("❌ Failed to save subscription!");
        return ResponseEntity.badRequest().body(Map.of("message", "Failed to process subscription"));
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserSubscription(@PathVariable String userId) {
        Map<String, Object> subscriptionDetails = subscriptionService.getUserSubscription(userId);

        if (subscriptionDetails != null && !subscriptionDetails.isEmpty()) {
            return ResponseEntity.ok(subscriptionDetails);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("hasSubscription", false);
        response.put("subscription", null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/plans")
    public ResponseEntity<?> getPlans() {
        return ResponseEntity.ok(List.of(
                Map.of("id", "driver-basic", "name", "Driver Basic", "price", 0),
                Map.of("id", "driver-pro", "name", "Driver Pro", "price", 9.99),
                Map.of("id", "mechanic-pro", "name", "Mechanic Pro", "price", 19.99)
        ));
    }

    @PutMapping("/cancel/{subscriptionId}")
    public ResponseEntity<?> cancelSubscription(@PathVariable String subscriptionId) {
        boolean cancelled = subscriptionService.cancelSubscription(subscriptionId);
        if (cancelled) {
            return ResponseEntity.ok(Map.of("message", "Subscription cancelled successfully"));
        }
        return ResponseEntity.badRequest().body(Map.of("message", "Failed to cancel subscription"));
    }
}