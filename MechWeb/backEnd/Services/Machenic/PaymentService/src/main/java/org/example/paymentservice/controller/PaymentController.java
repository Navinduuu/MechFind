package org.example.paymentservice.controller;

import org.example.paymentservice.dto.PaymentRequestDto;
import org.example.paymentservice.dto.PaymentResponseDto;
import org.example.paymentservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mechfind/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // Handles POST http://localhost:8085/api/mechfind/payments/process
    @PostMapping("/process")
    public ResponseEntity<PaymentResponseDto> processPayment(@RequestBody PaymentRequestDto request) {
        PaymentResponseDto response = paymentService.processPayment(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    // Handles POST http://localhost:8085/api/mechfind/payments/generate-otp
    @PostMapping("/generate-otp")
    public ResponseEntity<?> requestOtp(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String otp = paymentService.generateAndSendOtp(email);
        return ResponseEntity.ok(Map.of("message", "OTP generated successfully", "otp", otp));
    }

    // Handles OTP verification and payment processing
    @PostMapping("/verify-and-pay")
    public ResponseEntity<PaymentResponseDto> verifyAndPay(
            @RequestParam String otp,
            @RequestBody PaymentRequestDto request) {

        boolean isOtpValid = paymentService.verifyOtp(request.getEmail(), otp);
        if (!isOtpValid) {
            return ResponseEntity.badRequest().body(new PaymentResponseDto(false, "Invalid or expired OTP", null));
        }

        PaymentResponseDto response = paymentService.processPayment(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }
}