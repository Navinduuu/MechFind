package org.example.paymentservice.dto;

public class PaymentRequestDto {
    private String userId;
    private String email;
    private String cardNumber;
    private String expiry;
    private String cvv;
    private Double amount;
    private String planId;

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public String getExpiry() { return expiry; }
    public void setExpiry(String expiry) { this.expiry = expiry; }
    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
}


