package org.example.notificationservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private final Map<String, OtpRecord> otpStorage = new ConcurrentHashMap<>();
    private static final int OTP_VALIDITY_MINUTES = 5;
    private static final SecureRandom random = new SecureRandom();

    private static class OtpRecord {
        String otp;
        LocalDateTime expiryTime;

        OtpRecord(String otp, LocalDateTime expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }
    }

    public void generateAndSendOtp(String email, String purpose) {
        int otpInt = 100000 + random.nextInt(900000);
        String otp = String.valueOf(otpInt);

        LocalDateTime expiry = LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES);
        otpStorage.put(email, new OtpRecord(otp, expiry));

        String verificationType = purpose != null ? purpose : "Verification";
        String subject = "MechFind - " + verificationType + " OTP Code";
        String body = "Your verification OTP code for " + verificationType.toLowerCase() + " is: " + otp +
                "\nThis code is valid for " + OTP_VALIDITY_MINUTES + " minutes." +
                "\nDo not share this code with anyone.";

        sendEmail(email, subject, body);
    }

    public boolean verifyOtp(String email, String enteredOtp) {
        OtpRecord record = otpStorage.get(email);
        if (record == null) {
            return false;
        }
        if (LocalDateTime.now().isAfter(record.expiryTime)) {
            otpStorage.remove(email);
            return false;
        }
        boolean isValid = record.otp.equals(enteredOtp);
        if (isValid) {
            otpStorage.remove(email); // Single-use enforcement
        }
        return isValid;
    }

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}