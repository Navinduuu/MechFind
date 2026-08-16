package org.example.subscriptionservice.dto;

public class SubscriptionRequestDto {
    private String userId;
    private String planName;
    private String planId;
    private Double amount;
    private String billingCycle;
    private String userType;

    public SubscriptionRequestDto() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getBillingCycle() { return billingCycle; }
    public void setBillingCycle(String billingCycle) { this.billingCycle = billingCycle; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
}