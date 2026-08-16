package org.example.notificationservice.dto;

public class EmailRequest {
    private String recipientEmail;
    private String email;
    private String subject;
    private String body;
    private String otpCode;
    private String purpose;

    // Getters and Setters
    public String getRecipientEmail() {
        return recipientEmail != null ? recipientEmail : email;
    }
    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
}
