package org.example.subscriptionservice.model;

public class Subscription {
    private String subscriptionId;
    private String userId;
    private String planName;
    private String planId;
    private Double amount;
    private String billingCycle;
    private String userType;
    private String status;
    private String startDate;

    public Subscription() {}

    public String getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }


    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getBillingCycle() { return billingCycle; }
    public void setBillingCycle(String billingCycle) { this.billingCycle = billingCycle; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
}