package com.eagle.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdateUser {
    @NotBlank
    private String name;

    @Valid
    private Address address;

    @NotBlank
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$")
    private String phoneNumber;

    @NotBlank
    @Email
    private String email;

    // Address inner class based on OpenAPI spec
    public static class Address {
        @NotBlank private String line1;
        private String line2;
        private String line3;
        @NotBlank private String town;
        @NotBlank private String county;
        @NotBlank private String postcode;

        // Getters and Setters
        public String getLine1() { return line1; }
        public void setLine1(String line1) { this.line1 = line1; }

        public String getLine2() { return line2; }
        public void setLine2(String line2) { this.line2 = line2; }

        public String getLine3() { return line3; }
        public void setLine3(String line3) { this.line3 = line3; }

        public String getTown() { return town; }
        public void setTown(String town) { this.town = town; }

        public String getCounty() { return county; }
        public void setCounty(String county) { this.county = county; }

        public String getPostcode() { return postcode; }
        public void setPostcode(String postcode) { this.postcode = postcode; }
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
