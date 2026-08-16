package org.example.subscriptionservice.dto;

import org.example.subscriptionservice.model.Subscription;

public class SubscriptionResponseDto {
    private boolean success;
    private String message;
    private Subscription subscription;

    public SubscriptionResponseDto() {}

    public SubscriptionResponseDto(boolean success, String message, Subscription subscription) {
        this.success = success;
        this.message = message;
        this.subscription = subscription;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }
}