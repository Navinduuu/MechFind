package com.CommunicationService.CommunicationService.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = getClass().getClassLoader()
                        .getResourceAsStream("serviceAccountKey.json");

                if (serviceAccount == null) {
                    throw new RuntimeException("serviceAccountKey.json not found in resources folder");
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setDatabaseUrl("https://machfind-6ce35-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .build();

                FirebaseApp.initializeApp(options);
                System.out.println("Firebase initialized successfully in FeedbackService!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}