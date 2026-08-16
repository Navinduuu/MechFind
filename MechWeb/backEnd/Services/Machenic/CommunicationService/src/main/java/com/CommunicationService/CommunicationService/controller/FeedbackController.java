package com.CommunicationService.CommunicationService.controller;

import com.CommunicationService.CommunicationService.model.Feedback;
import com.CommunicationService.CommunicationService.service.FeedbackService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mechfind")
@CrossOrigin(origins = "*")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Autowired
    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping("/feedback")
    public ResponseEntity<List<Feedback>> getAllFeedback() {
        return ResponseEntity.ok(feedbackService.getAllFeedback());
    }

    @PostMapping("/feedback")
    public ResponseEntity<Feedback> postFeedback(@Valid @RequestBody FeedbackInput input) {
        Feedback feedback = new Feedback();
        feedback.setUserId(input.getUserId());
        feedback.setUserType(input.getUserType());
        feedback.setMessage(input.getMessage());
        feedback.setRating(input.getRating());

        Feedback saved = feedbackService.saveFeedback(feedback);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/feedback/{feedbackId}")
    public ResponseEntity<Feedback> updateFeedback(
            @PathVariable String feedbackId,
            @Valid @RequestBody FeedbackInput input) {
        Feedback feedback = new Feedback();
        feedback.setUserId(input.getUserId());
        feedback.setUserType(input.getUserType());
        feedback.setMessage(input.getMessage());
        feedback.setRating(input.getRating());

        try {
            Feedback updated = feedbackService.updateFeedback(feedbackId, feedback);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/feedback/{feedbackId}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable String feedbackId) {
        try {
            feedbackService.deleteFeedback(feedbackId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/track-download")
    public ResponseEntity<String> trackDownload(@RequestParam String platform) {
        System.out.println("Download initiated for: " + platform);
        return ResponseEntity.ok("Logged");
    }

    public static class FeedbackInput {

        @NotBlank(message = "userId is required")
        private String userId;

        @NotBlank(message = "userType is required")
        private String userType;

        @NotBlank(message = "message is required")
        private String message;

        @NotNull(message = "rating is required")
        @Min(value = 1, message = "rating must be between 1 and 5")
        @Max(value = 5, message = "rating must be between 1 and 5")
        private Integer rating;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserType() {
            return userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Integer getRating() {
            return rating;
        }

        public void setRating(Integer rating) {
            this.rating = rating;
        }
    }
}