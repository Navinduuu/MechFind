package org.example.paymentservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.database.url}")
    private String databaseUrl;

    @PostConstruct
    public void initFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("serviceAccountKey.json");

                if (serviceAccount == null) {
                    throw new RuntimeException("Could not find serviceAccountKey.json in src/main/resources/");
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setDatabaseUrl(databaseUrl)
                        .build();

                FirebaseApp.initializeApp(options);
                System.out.println("Firebase initialized successfully for PaymentService.");
            }
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: Failed to initialize Firebase App in PaymentService!");
            e.printStackTrace();
            throw new RuntimeException("Firebase initialization failed", e);
        }
    }
}