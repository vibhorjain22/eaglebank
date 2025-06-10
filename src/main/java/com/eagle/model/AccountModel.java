package com.eagle.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.OffsetDateTime;

@Entity
public class AccountModel {
    @Id
    private String accountNumber; // 6-digit unique number followed by 01

    private String userId; // Store the userId from JWT

    private String sortCode;
    private String name;
    private String accountType;
    private double balance;
    private String currency;
    private OffsetDateTime createdTimestamp;
    private OffsetDateTime updatedTimestamp;

    // Getters and Setters

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSortCode() { return sortCode; }
    public void setSortCode(String sortCode) { this.sortCode = sortCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public OffsetDateTime getCreatedTimestamp() { return createdTimestamp; }
    public void setCreatedTimestamp(OffsetDateTime createdTimestamp) { this.createdTimestamp = createdTimestamp; }

    public OffsetDateTime getUpdatedTimestamp() { return updatedTimestamp; }
    public void setUpdatedTimestamp(OffsetDateTime updatedTimestamp) { this.updatedTimestamp = updatedTimestamp; }
}
