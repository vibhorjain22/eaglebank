package com.eagle.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CreateAccount {
    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "personal") // Only "personal" is allowed as per OpenAPI enum
    private String accountType;

    private String currency;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
