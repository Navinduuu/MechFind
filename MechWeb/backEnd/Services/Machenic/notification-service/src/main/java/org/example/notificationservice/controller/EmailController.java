package org.example.notificationservice.controller;

import org.example.notificationservice.dto.EmailRequest;
import org.example.notificationservice.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mechfind/email")
@CrossOrigin(origins = "*")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody EmailRequest request) {
        try {
            String email = request.getRecipientEmail();
            String purpose = request.getPurpose() != null ? request.getPurpose() : "Verification";
            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Recipient email is required"));
            }
            emailService.generateAndSendOtp(email, purpose);
            return ResponseEntity.ok(Map.of("message", "OTP sent successfully to " + email));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to send OTP email: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String otp = payload.get("otp");

        if (email == null || otp == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email and OTP are required"));
        }

        boolean isValid = emailService.verifyOtp(email, otp);
        if (isValid) {
            return ResponseEntity.ok(Map.of("success", true, "message", "OTP verified successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid or expired OTP code"));
        }
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendGeneralEmail(@RequestBody EmailRequest request) {
        try {
            emailService.sendEmail(request.getRecipientEmail(), request.getSubject(), request.getBody());
            return ResponseEntity.ok(Map.of("message", "Email sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to send email: " + e.getMessage()));
        }
    }
}