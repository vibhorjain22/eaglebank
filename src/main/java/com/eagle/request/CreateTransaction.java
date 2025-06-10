package com.eagle.request;

public class CreateTransaction {
    private double amount;
    private String currency; // e.g., "GBP"
    private String type;     // e.g., "deposit" or "withdrawal"
    private String reference;
    private String userId;

    // Getters and Setters
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
