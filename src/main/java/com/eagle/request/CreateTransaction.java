package com.eagle.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class CreateTransaction {
    @NotNull
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "10000.00")
    private Double amount;

    @NotBlank
    @Pattern(regexp = "GBP")
    private String currency; // Only "GBP" allowed

    @NotBlank
    @Pattern(regexp = "deposit|withdrawal")
    private String type; // Only "deposit" or "withdrawal" allowed

    private String reference;

    // Getters and Setters
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
}
