package com.eagle.controller;

import com.eagle.request.CreateUser;
import com.eagle.request.CreateUser.Address;
import com.eagle.request.CreateAccount;
import com.eagle.request.CreateTransaction;
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
public class TransactionControllerE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    private String createAccountAndGetNumber(CreateAccount account, String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/v1/accounts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(account)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accountNumber").asText();
    }

    @Test
    void createTransaction_success() throws Exception {
        // Create user and authenticate
        String email = "tranuser" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("Transaction User", "+441234567890", email, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token = authenticateAndGetToken(email);

        // Create account
        CreateAccount account = new CreateAccount();
        account.setName("Main Account");
        account.setAccountType("personal");
        account.setCurrency("GBP");
        String accountNumber = createAccountAndGetNumber(account, token);

        // Create transaction (deposit)
        CreateTransaction transaction = new CreateTransaction();
        transaction.setAmount(100.0);
        transaction.setCurrency("GBP");
        transaction.setType("deposit");
        transaction.setReference("Initial deposit");

        MvcResult result = mockMvc.perform(post("/v1/accounts/" + accountNumber + "/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transaction)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.type").value("deposit"))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(response).contains("Initial deposit");
    }

    @Test
    void createTransaction_withdrawal_insufficientFunds_returnsUnprocessableEntityError() throws Exception {
        // Create user and authenticate
        String email = "tranuser" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("Transaction User", "+441234567890", email, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token = authenticateAndGetToken(email);

        // Create account
        CreateAccount account = new CreateAccount();
        account.setName("Main Account");
        account.setAccountType("personal");
        account.setCurrency("GBP");
        String accountNumber = createAccountAndGetNumber(account, token);

        // Try withdrawal with insufficient funds
        CreateTransaction transaction = new CreateTransaction();
        transaction.setAmount(100.0);
        transaction.setCurrency("GBP");
        transaction.setType("withdrawal");
        transaction.setReference("Attempted overdraft");

        mockMvc.perform(post("/v1/accounts/" + accountNumber + "/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transaction)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void createTransaction_withdrawal_withSufficientFunds_success() throws Exception {
        // Create user and authenticate
        String email = "tranuser" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("Transaction User", "+441234567890", email, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token = authenticateAndGetToken(email);

        // Create account
        CreateAccount account = new CreateAccount();
        account.setName("Main Account");
        account.setAccountType("personal");
        account.setCurrency("GBP");
        String accountNumber = createAccountAndGetNumber(account, token);

        // Deposit first to ensure sufficient balance
        CreateTransaction deposit = new CreateTransaction();
        deposit.setAmount(200.0);
        deposit.setCurrency("GBP");
        deposit.setType("deposit");
        deposit.setReference("Deposit for withdrawal");

        mockMvc.perform(post("/v1/accounts/" + accountNumber + "/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deposit)))
                .andExpect(status().isCreated());

        // Withdraw less than deposited amount
        CreateTransaction withdrawal = new CreateTransaction();
        withdrawal.setAmount(150.0);
        withdrawal.setCurrency("GBP");
        withdrawal.setType("withdrawal");
        withdrawal.setReference("Withdrawal with sufficient funds");

        mockMvc.perform(post("/v1/accounts/" + accountNumber + "/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withdrawal)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(150.0))
                .andExpect(jsonPath("$.type").value("withdrawal"));

        // Optionally, check the balance (if you have an endpoint for it)
        // mockMvc.perform(get("/v1/accounts/" + accountNumber)
        //         .header("Authorization", "Bearer " + token))
        //         .andExpect(status().isOk())
        //         .andExpect(jsonPath("$.balance").value(50.0));

        // List transactions and assert both deposit and withdrawal are present
        MvcResult result = mockMvc.perform(get("/v1/accounts/" + accountNumber + "/transactions")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(response).contains("Deposit for withdrawal");
        assertThat(response).contains("Withdrawal with sufficient funds");
    }

    @Test
    void listTransactions_success() throws Exception {
        // Create user and authenticate
        String email = "tranuser" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("Transaction User", "+441234567890", email, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token = authenticateAndGetToken(email);

        // Create account
        CreateAccount account = new CreateAccount();
        account.setName("Main Account");
        account.setAccountType("personal");
        account.setCurrency("GBP");
        String accountNumber = createAccountAndGetNumber(account, token);

        // Create transaction (deposit)
        CreateTransaction transaction = new CreateTransaction();
        transaction.setAmount(50.0);
        transaction.setCurrency("GBP");
        transaction.setType("deposit");
        transaction.setReference("Deposit for listing");

        mockMvc.perform(post("/v1/accounts/" + accountNumber + "/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transaction)))
                .andExpect(status().isCreated());

        // List transactions and assert the new response structure
        MvcResult result = mockMvc.perform(get("/v1/accounts/" + accountNumber + "/transactions")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.transactions[0].amount").value(50.0))
                .andExpect(jsonPath("$.transactions[0].reference").value("Deposit for listing"))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(response).contains("Deposit for listing");
    }

    @Test
    void getTransactionById_success() throws Exception {
        // Create user and authenticate
        String email = "tranuser" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("Transaction User", "+441234567890", email, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token = authenticateAndGetToken(email);

        // Create account
        CreateAccount account = new CreateAccount();
        account.setName("Main Account");
        account.setAccountType("personal");
        account.setCurrency("GBP");
        String accountNumber = createAccountAndGetNumber(account, token);

        // Create transaction (deposit)
        CreateTransaction transaction = new CreateTransaction();
        transaction.setAmount(25.0);
        transaction.setCurrency("GBP");
        transaction.setType("deposit");
        transaction.setReference("Deposit for get by id");

        MvcResult createResult = mockMvc.perform(post("/v1/accounts/" + accountNumber + "/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transaction)))
                .andExpect(status().isCreated())
                .andReturn();

        String transactionId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        // Get transaction by id
        mockMvc.perform(get("/v1/accounts/" + accountNumber + "/transactions/" + transactionId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId))
                .andExpect(jsonPath("$.reference").value("Deposit for get by id"));
    }

    @Test
    void createTransaction_onOtherUsersAccount_shouldReturnForbidden() throws Exception {
        // User 1
        String email1 = "user1" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("User One", "+441234567890", email1, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token1 = authenticateAndGetToken(email1);

        // User 2
        String email2 = "user2" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("User Two", "+441234567891", email2, "456 New St", "Manchester", "Greater Manchester", "M1 2AB"));
        String token2 = authenticateAndGetToken(email2);

        // User 2 creates an account
        CreateAccount account = new CreateAccount();
        account.setName("Other Account");
        account.setAccountType("personal");
        account.setCurrency("GBP");
        String accountNumber = createAccountAndGetNumber(account, token2);

        // User 1 tries to create a transaction on User 2's account
        CreateTransaction transaction = new CreateTransaction();
        transaction.setAmount(10.0);
        transaction.setCurrency("GBP");
        transaction.setType("deposit");
        transaction.setReference("Should be forbidden");

        mockMvc.perform(post("/v1/accounts/" + accountNumber + "/transactions")
                .header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transaction)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createTransaction_onNonExistentAccount_shouldReturnNotFound() throws Exception {
        String email = "tranuser" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("Transaction User", "+441234567890", email, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token = authenticateAndGetToken(email);

        CreateTransaction transaction = new CreateTransaction();
        transaction.setAmount(10.0);
        transaction.setCurrency("GBP");
        transaction.setType("deposit");
        transaction.setReference("Non-existent account");

        String madeUpAccountNumber = "acc-99999999";
        mockMvc.perform(post("/v1/accounts/" + madeUpAccountNumber + "/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transaction)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTransaction_withInvalidDetails_shouldReturnBadRequest() throws Exception {
        String email = "tranuser" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("Transaction User", "+441234567890", email, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token = authenticateAndGetToken(email);

        // Missing required fields (e.g., amount is null)
        CreateTransaction transaction = new CreateTransaction();
        transaction.setCurrency("GBP");
        transaction.setType("deposit");
        transaction.setReference("Missing amount");

        // Should return 400 Bad Request
        mockMvc.perform(post("/v1/accounts/acc-12345678/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transaction)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listTransactions_onOtherUsersAccount_shouldReturnForbidden() throws Exception {
        // User 1
        String email1 = "user1" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("User One", "+441234567890", email1, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token1 = authenticateAndGetToken(email1);

        // User 2
        String email2 = "user2" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("User Two", "+441234567891", email2, "456 New St", "Manchester", "Greater Manchester", "M1 2AB"));
        String token2 = authenticateAndGetToken(email2);

        // User 2 creates an account
        CreateAccount account = new CreateAccount();
        account.setName("Other Account");
        account.setAccountType("personal");
        account.setCurrency("GBP");
        String accountNumber = createAccountAndGetNumber(account, token2);

        // User 1 tries to list transactions for User 2's account
        mockMvc.perform(get("/v1/accounts/" + accountNumber + "/transactions")
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isForbidden());
    }

    @Test
    void listTransactions_onNonExistentAccount_shouldReturnNotFound() throws Exception {
        String email = "tranuser" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("Transaction User", "+441234567890", email, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token = authenticateAndGetToken(email);

        String madeUpAccountNumber = "acc-99999999";
        mockMvc.perform(get("/v1/accounts/" + madeUpAccountNumber + "/transactions")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTransactionById_onOtherUsersAccount_shouldReturnForbidden() throws Exception {
        // User 1
        String email1 = "user1" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("User One", "+441234567890", email1, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token1 = authenticateAndGetToken(email1);

        // User 2
        String email2 = "user2" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("User Two", "+441234567891", email2, "456 New St", "Manchester", "Greater Manchester", "M1 2AB"));
        String token2 = authenticateAndGetToken(email2);

        // User 2 creates an account and transaction
        CreateAccount account = new CreateAccount();
        account.setName("Other Account");
        account.setAccountType("personal");
        account.setCurrency("GBP");
        String accountNumber = createAccountAndGetNumber(account, token2);

        CreateTransaction transaction = new CreateTransaction();
        transaction.setAmount(10.0);
        transaction.setCurrency("GBP");
        transaction.setType("deposit");
        transaction.setReference("Other's transaction");

        MvcResult result = mockMvc.perform(post("/v1/accounts/" + accountNumber + "/transactions")
                .header("Authorization", "Bearer " + token2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transaction)))
                .andExpect(status().isCreated())
                .andReturn();

        String transactionId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        // User 1 tries to get User 2's transaction
        mockMvc.perform(get("/v1/accounts/" + accountNumber + "/transactions/" + transactionId)
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isForbidden());
    }

    @Test
    void getTransactionById_onNonExistentAccount_shouldReturnNotFound() throws Exception {
        String email = "tranuser" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("Transaction User", "+441234567890", email, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token = authenticateAndGetToken(email);

        // Create a real account and transaction
        CreateAccount account = new CreateAccount();
        account.setName("Main Account");
        account.setAccountType("personal");
        account.setCurrency("GBP");
        String accountNumber = createAccountAndGetNumber(account, token);

        CreateTransaction transaction = new CreateTransaction();
        transaction.setAmount(10.0);
        transaction.setCurrency("GBP");
        transaction.setType("deposit");
        transaction.setReference("Real transaction");

        MvcResult result = mockMvc.perform(post("/v1/accounts/" + accountNumber + "/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transaction)))
                .andExpect(status().isCreated())
                .andReturn();

        String realTransactionId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        // Use a non-existent account number with a real transaction id
        String madeUpAccountNumber = "acc-99999999";
        mockMvc.perform(get("/v1/accounts/" + madeUpAccountNumber + "/transactions/" + realTransactionId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTransactionById_nonExistentTransactionId_shouldReturnNotFound() throws Exception {
        String email = "tranuser" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("Transaction User", "+441234567890", email, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token = authenticateAndGetToken(email);

        // Create account
        CreateAccount account = new CreateAccount();
        account.setName("Main Account");
        account.setAccountType("personal");
        account.setCurrency("GBP");
        String accountNumber = createAccountAndGetNumber(account, token);

        String madeUpTransactionId = "tan-99999999";
        mockMvc.perform(get("/v1/accounts/" + accountNumber + "/transactions/" + madeUpTransactionId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTransactionById_transactionIdFromAnotherAccount_shouldReturnNotFound() throws Exception {
        String email = "tranuser" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("Transaction User", "+441234567890", email, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token = authenticateAndGetToken(email);

        // Create two accounts
        CreateAccount account1 = new CreateAccount();
        account1.setName("Account 1");
        account1.setAccountType("personal");
        account1.setCurrency("GBP");
        String accountNumber1 = createAccountAndGetNumber(account1, token);

        CreateAccount account2 = new CreateAccount();
        account2.setName("Account 2");
        account2.setAccountType("personal");
        account2.setCurrency("GBP");
        String accountNumber2 = createAccountAndGetNumber(account2, token);

        // Create transaction on account 2
        CreateTransaction transaction = new CreateTransaction();
        transaction.setAmount(10.0);
        transaction.setCurrency("GBP");
        transaction.setType("deposit");
        transaction.setReference("Account 2 transaction");

        MvcResult result = mockMvc.perform(post("/v1/accounts/" + accountNumber2 + "/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transaction)))
                .andExpect(status().isCreated())
                .andReturn();

        String transactionId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        // Try to get transaction from account 1 using transaction id from account 2
        mockMvc.perform(get("/v1/accounts/" + accountNumber1 + "/transactions/" + transactionId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}