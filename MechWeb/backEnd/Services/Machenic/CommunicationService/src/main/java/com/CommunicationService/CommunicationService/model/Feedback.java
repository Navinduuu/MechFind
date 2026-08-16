package com.CommunicationService.CommunicationService.model;

public class Feedback {

    private String id;        // Firebase Push Key for feedback
    private String userId;    // Foreign Key (FK) -> RegisteredUsers ID
    private String userName;  // User name retrieved from RegisteredUsers
    private String userType;  // "TowTruck", "Mechanic", "Customer", etc.
    private String message;   // Feedback message
    private Integer rating;   // Rating (1 to 5)
    private String createdAt; // ISO Timestamp

    public Feedback() {
    }

    public Feedback(String id, String userId, String userName, String userType, String message, Integer rating, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userType = userType;
        this.message = message;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}