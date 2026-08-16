package org.example.paymentservice.model;

public class Payment {
    private String paymentId;
    private String userId;
    private String email;
    private String maskedCardNumber;
    private Double amount;
    private String planId;
    private String status;
    private String timestamp;

    // Generate Getters and Setters for all fields...
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMaskedCardNumber() { return maskedCardNumber; }
    public void setMaskedCardNumber(String maskedCardNumber) { this.maskedCardNumber = maskedCardNumber; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}