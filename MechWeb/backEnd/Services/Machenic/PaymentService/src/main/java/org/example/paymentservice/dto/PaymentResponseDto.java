package org.example.paymentservice.dto;

public class PaymentResponseDto {
    private boolean success;
    private String message;
    private String transactionId;

    public PaymentResponseDto(boolean success, String message, String transactionId) {
        this.success = success;
        this.message = message;
        this.transactionId = transactionId;
    }
    // Getters and Setters...
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getTransactionId() { return transactionId; }
}