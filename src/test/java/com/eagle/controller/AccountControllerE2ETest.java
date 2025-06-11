package com.eagle.controller;

import com.eagle.request.CreateUser;
import com.eagle.request.CreateUser.Address;
import com.eagle.request.UpdateAccount;
import com.eagle.request.CreateAccount;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // --- Helper Methods ---

    private CreateUser buildUser(String name, String phone, String email, String line1, String town, String county, String postcode) {
        CreateUser user = new CreateUser();
        user.setName(name);
        user.setPhoneNumber(phone);
        user.setEmail(email);
        Address address = new Address();
        address.setLine1(line1);
        address.setTown(town);
        address.setCounty(county);
        address.setPostcode(postcode);
        user.setAddress(address);
        return user;
    }

    private String createUserAndGetId(CreateUser user) throws Exception {
        MvcResult result = mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private String authenticateAndGetToken(String email) throws Exception {
        String authRequest = "{\"username\": \"" + email + "\"}";
        MvcResult result = mockMvc.perform(post("/v1/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authRequest))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private CreateAccount buildAccount(String name, String accountType, String currency) {
        CreateAccount account = new CreateAccount();
        account.setName(name);
        account.setAccountType(accountType);
        account.setCurrency(currency);
        return account;
    }

    private String createAccountAndGetNumber(CreateAccount account, String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/v1/accounts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(account)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accountNumber").asText();
    }

    // --- Test Scenarios ---

    @Test
    void createAccount_authenticate_and_getAccount_success() throws Exception {
        // Create and authenticate user
        String email = "accountuser" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("Account User", "+441234567890", email, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token = authenticateAndGetToken(email);

        // Create account
        CreateAccount account = buildAccount("Main Account", "personal", "GBP");
        String accountNumber = createAccountAndGetNumber(account, token);

        // Get account
        MvcResult getResult = mockMvc.perform(get("/v1/accounts/" + accountNumber)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Main Account"))
                .andExpect(jsonPath("$.accountType").value("personal"))
                .andExpect(jsonPath("$.currency").value("GBP"))
                .andReturn();

        String getAccountResponse = getResult.getResponse().getContentAsString();
        assertThat(objectMapper.readTree(getAccountResponse).get("accountNumber").asText()).isEqualTo(accountNumber);
    }

    @Test
    void getOtherUsersAccount_shouldReturnForbidden() throws Exception {
        // Create and authenticate user 1
        String email1 = "accuserone" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("User One", "+441234567890", email1, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token1 = authenticateAndGetToken(email1);

        // Create and authenticate user 2
        String email2 = "accusertwo" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("User Two", "+441234567891", email2, "456 New St", "Manchester", "Greater Manchester", "M1 2AB"));
        String token2 = authenticateAndGetToken(email2);

        // User 2 creates an account
        CreateAccount account = buildAccount("Other Account", "personal", "GBP");
        String accountNumber = createAccountAndGetNumber(account, token2);

        // User 1 tries to get user 2's account
        mockMvc.perform(get("/v1/accounts/" + accountNumber)
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isForbidden());
    }

    @Test
    void getNonExistentAccount_shouldReturnNotFound() throws Exception {
        String email = "accuser" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("Test User", "+441234567890", email, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token = authenticateAndGetToken(email);

        String madeUpAccountNumber = "01999999";
        mockMvc.perform(get("/v1/accounts/" + madeUpAccountNumber)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAccount_success() throws Exception {
        String email = "delacc" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("Delete Account", "+441234567890", email, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token = authenticateAndGetToken(email);

        CreateAccount account = buildAccount("Delete Account", "personal", "GBP");
        String accountNumber = createAccountAndGetNumber(account, token);

        // Delete account
        mockMvc.perform(delete("/v1/accounts/" + accountNumber)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // Verify account is deleted
        mockMvc.perform(get("/v1/accounts/" + accountNumber)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOtherUsersAccount_shouldReturnForbidden() throws Exception {
        // Create and authenticate user 1
        String email1 = "delaccuserone" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("User One", "+441234567890", email1, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token1 = authenticateAndGetToken(email1);

        // Create and authenticate user 2
        String email2 = "delaccusertwo" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("User Two", "+441234567891", email2, "456 New St", "Manchester", "Greater Manchester", "M1 2AB"));
        String token2 = authenticateAndGetToken(email2);

        // User 2 creates an account
        CreateAccount account = buildAccount("Other Account", "personal", "GBP");
        String accountNumber = createAccountAndGetNumber(account, token2);

        // User 1 tries to delete user 2's account
        mockMvc.perform(delete("/v1/accounts/" + accountNumber)
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteNonExistentAccount_shouldReturnNotFound() throws Exception {
        String email = "delacc" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("Test User", "+441234567890", email, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token = authenticateAndGetToken(email);

        String madeUpAccountNumber = "01988888";
        mockMvc.perform(delete("/v1/accounts/" + madeUpAccountNumber)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void createAccount_withInvalidDetails_returnsBadRequest() throws Exception {
        // Create and authenticate user
        String email = "invalidacc" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("Invalid Account User", "+441234567890", email, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token = authenticateAndGetToken(email);

        // Missing required name and invalid accountType/currency
        CreateAccount invalidAccount = new CreateAccount();
        invalidAccount.setName(""); // Blank name (should be @NotBlank)
        invalidAccount.setAccountType("business"); // Invalid, only "personal" allowed
        invalidAccount.setCurrency("USD"); // Invalid, only "GBP" allowed

        mockMvc.perform(post("/v1/accounts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidAccount)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateAccount_success() throws Exception {
        // Create and authenticate user
        String email = "updateacc" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("Update Account User", "+441234567890", email, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token = authenticateAndGetToken(email);

        // Create account
        CreateAccount account = buildAccount("Update Account", "personal", "GBP");
        String accountNumber = createAccountAndGetNumber(account, token);

        // Prepare update payload (only name and accountType are updatable)
        UpdateAccount updateAccount = new UpdateAccount();
        updateAccount.setName("Updated Account Name");
        updateAccount.setAccountType("personal");

        // Update account
        mockMvc.perform(patch("/v1/accounts/" + accountNumber)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAccount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Account Name"))
                .andExpect(jsonPath("$.accountType").value("personal"));

        // Verify update
        mockMvc.perform(get("/v1/accounts/" + accountNumber)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Account Name"));
    }

    @Test
    void updateOtherUsersAccount_shouldReturnForbidden() throws Exception {
        // Create and authenticate user 1
        String email1 = "updateaccuserone" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("User One", "+441234567890", email1, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token1 = authenticateAndGetToken(email1);

        // Create and authenticate user 2
        String email2 = "updateaccusertwo" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("User Two", "+441234567891", email2, "456 New St", "Manchester", "Greater Manchester", "M1 2AB"));
        String token2 = authenticateAndGetToken(email2);

        // User 2 creates an account
        CreateAccount account = buildAccount("Other Account", "personal", "GBP");
        String accountNumber = createAccountAndGetNumber(account, token2);

        // User 1 tries to update user 2's account
        com.eagle.request.UpdateAccount updateAccount = new com.eagle.request.UpdateAccount();
        updateAccount.setName("Hacker Update");
        updateAccount.setAccountType("personal");

        mockMvc.perform(patch("/v1/accounts/" + accountNumber)
                .header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAccount)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateNonExistentAccount_shouldReturnNotFound() throws Exception {
        String email = "updateacc" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("Test User", "+441234567890", email, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token = authenticateAndGetToken(email);

        String madeUpAccountNumber = "01977777";
        com.eagle.request.UpdateAccount updateAccount = new com.eagle.request.UpdateAccount();
        updateAccount.setName("Should Not Exist");
        updateAccount.setAccountType("personal");

        mockMvc.perform(patch("/v1/accounts/" + madeUpAccountNumber)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAccount)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAccount_withInvalidDetails_returnsBadRequest() throws Exception {
        // Create and authenticate user
        String email = "invalidupdateacc" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("Invalid Update Account User", "+441234567890", email, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token = authenticateAndGetToken(email);

        // Create account
        CreateAccount account = buildAccount("Invalid Update Account", "personal", "GBP");
        String accountNumber = createAccountAndGetNumber(account, token);

        // Prepare invalid update payload (blank name, invalid accountType)
        com.eagle.request.UpdateAccount updateAccount = new com.eagle.request.UpdateAccount();
        updateAccount.setName(""); // Invalid: blank
        updateAccount.setAccountType("business"); // Invalid: only "personal" allowed

        mockMvc.perform(patch("/v1/accounts/" + accountNumber)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAccount)))
                .andExpect(status().isBadRequest());
    }
}