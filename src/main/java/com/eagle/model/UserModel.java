package com.eagle.model;

import java.time.OffsetDateTime;

import jakarta.persistence.*;

@Entity // This annotation indicates that this class is a JPA entity
@Table(name = "users") // This annotation specifies the table name in the database
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id; // ^usr-[A-Za-z0-9]+$
    private String name;

    @Embedded
    private Address address;

    private String phoneNumber; // E.164 format, e.g. +1234567890

    @Column(unique = true, nullable = false) // Ensures email is unique and not null
    private String email;
    private OffsetDateTime createdTimestamp;
    private OffsetDateTime updatedTimestamp;

    @Embeddable
    public static class Address {
        private String line1;
        private String line2;
        private String line3;
        private String town;
        private String county;
        private String postcode;

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
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public OffsetDateTime getCreatedTimestamp() { return createdTimestamp; }
    public void setCreatedTimestamp(OffsetDateTime createdTimestamp) { this.createdTimestamp = createdTimestamp; }

    public OffsetDateTime getUpdatedTimestamp() { return updatedTimestamp; }
    public void setUpdatedTimestamp(OffsetDateTime updatedTimestamp) { this.updatedTimestamp = updatedTimestamp; }
}
