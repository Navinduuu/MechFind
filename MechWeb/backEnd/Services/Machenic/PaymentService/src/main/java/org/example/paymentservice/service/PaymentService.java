package org.example.paymentservice.service;

import com.google.api.core.ApiFuture;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.example.paymentservice.dto.PaymentRequestDto;
import org.example.paymentservice.dto.PaymentResponseDto;
import org.example.paymentservice.model.Payment;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
public class PaymentService {

    private final Map<String, String> otpStorage = new HashMap<>();

    private DatabaseReference getPaymentsRef() {
        return FirebaseDatabase.getInstance().getReference("payments");
    }

    public String generateAndSendOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(email, otp);
        System.out.println("Generated Payment OTP for " + email + ": " + otp);
        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        if (otpStorage.containsKey(email) && otpStorage.get(email).equals(otp)) {
            otpStorage.remove(email);
            return true;
        }
        return false;
    }

    public PaymentResponseDto processPayment(PaymentRequestDto request) {
        try {
            String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

            Payment payment = new Payment();
            payment.setPaymentId(transactionId);
            payment.setUserId(request.getUserId());
            payment.setEmail(request.getEmail());

            String cardNumber = request.getCardNumber();
            String maskedCard = "****-****-****-" + (cardNumber != null && cardNumber.length() >= 4 ? cardNumber.substring(cardNumber.length() - 4) : "1234");
            payment.setMaskedCardNumber(maskedCard);

            payment.setAmount(request.getAmount());
            payment.setPlanId(request.getPlanId());
            payment.setStatus("COMPLETED");
            payment.setTimestamp(Instant.now().toString());

            DatabaseReference userPaymentRef = getPaymentsRef().child(request.getUserId()).child(transactionId);
            ApiFuture<Void> future = userPaymentRef.setValueAsync(payment);
            future.get(); // Wait for Firebase write confirmation

            return new PaymentResponseDto(true, "Payment successful and details saved.", transactionId);
        } catch (Exception e) {
            e.printStackTrace();
            return new PaymentResponseDto(false, "Payment failed: " + e.getMessage(), null);
        }
    }
}