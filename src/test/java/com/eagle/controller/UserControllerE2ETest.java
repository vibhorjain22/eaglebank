package com.eagle.controller;

import com.eagle.request.CreateUser;
import com.eagle.request.CreateUser.Address;
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
public class UserControllerE2ETest {

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

    // --- Test Scenarios ---

    @Test
    void createUser_success() throws Exception {
        CreateUser user = buildUser("Test User", "+441234567890", "testuser@example.com", "123 Main St", "London", "Greater London", "E1 6AN");
        mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("testuser@example.com"));
    }

    @Test
    void createUser_missingRequiredField_returnsBadRequest() throws Exception {
        CreateUser user = buildUser(null, "+441234567890", "testuser2@example.com", "123 Main St", "London", "Greater London", "E1 6AN");
        mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_authenticate_and_getUser_success() throws Exception {
        String email = "testuser" + System.currentTimeMillis() + "@example.com";
        CreateUser user = buildUser("Test User", "+441234567890", email, "123 Main St", "London", "Greater London", "E1 6AN");
        String userId = createUserAndGetId(user);
        String token = authenticateAndGetToken(email);

        MvcResult getResult = mockMvc.perform(get("/v1/users/" + userId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value(email))
                .andReturn();

        String getUserResponse = getResult.getResponse().getContentAsString();
        assertThat(objectMapper.readTree(getUserResponse).get("id").asText()).isEqualTo(userId);
    }

    @Test
    void getOtherUserDetails_shouldReturnForbidden() throws Exception {
        String email1 = "userone" + System.currentTimeMillis() + "@example.com";
        String email2 = "usertwo" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("User One", "+441234567890", email1, "123 Main St", "London", "Greater London", "E1 6AN"));
        String userId2 = createUserAndGetId(buildUser("User Two", "+441234567891", email2, "456 New St", "Manchester", "Greater Manchester", "M1 2AB"));
        String token = authenticateAndGetToken(email1);

        mockMvc.perform(get("/v1/users/" + userId2)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getNonExistentUser_shouldReturnNotFound() throws Exception {
        String email = "testuser" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("Test User", "+441234567890", email, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token = authenticateAndGetToken(email);

        String madeUpUserId = "nonexistent-" + System.currentTimeMillis();
        mockMvc.perform(get("/v1/users/" + madeUpUserId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void authenticate_and_updateUser_success() throws Exception {
        String email = "updateuser" + System.currentTimeMillis() + "@example.com";
        String userId = createUserAndGetId(buildUser("Original Name", "+441234567890", email, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token = authenticateAndGetToken(email);

        CreateUser updateUser = buildUser("Updated Name", "+441234567891", email, "456 Updated St", "Manchester", "Greater Manchester", "M1 2AB");

        mockMvc.perform(
                patch("/v1/users/" + userId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.phoneNumber").value("+441234567891"))
                .andExpect(jsonPath("$.address.line1").value("456 Updated St"));

        MvcResult getResult = mockMvc.perform(get("/v1/users/" + userId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.phoneNumber").value("+441234567891"))
                .andExpect(jsonPath("$.address.line1").value("456 Updated St"))
                .andReturn();

        String getUserResponse = getResult.getResponse().getContentAsString();
        assertThat(objectMapper.readTree(getUserResponse).get("id").asText()).isEqualTo(userId);
    }

    @Test
    void updateOtherUserDetails_shouldReturnForbidden() throws Exception {
        String email1 = "userone" + System.currentTimeMillis() + "@example.com";
        String email2 = "usertwo" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("User One", "+441234567890", email1, "123 Main St", "London", "Greater London", "E1 6AN"));
        String userId2 = createUserAndGetId(buildUser("User Two", "+441234567891", email2, "456 New St", "Manchester", "Greater Manchester", "M1 2AB"));
        String token = authenticateAndGetToken(email1);

        CreateUser updateUser = buildUser("Hacker Name", "+441234567899", email2, "789 Hacker St", "Leeds", "West Yorkshire", "LS1 1AA");

        mockMvc.perform(patch("/v1/users/" + userId2)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateNonExistentUser_shouldReturnNotFound() throws Exception {
        String email = "nonexistentupdate" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("Test User", "+441234567890", email, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token = authenticateAndGetToken(email);

        String madeUpUserId = "nonexistent-" + System.currentTimeMillis();
        CreateUser updateUser = buildUser("Should Not Exist", "+441234567899", "shouldnotexist@example.com", "No Street", "No Town", "No County", "NO1 1NO");

        mockMvc.perform(patch("/v1/users/" + madeUpUserId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOtherUser_shouldReturnForbidden() throws Exception {
        String email1 = "userone" + System.currentTimeMillis() + "@example.com";
        String email2 = "usertwo" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("User One", "+441234567890", email1, "123 Main St", "London", "Greater London", "E1 6AN"));
        String userId2 = createUserAndGetId(buildUser("User Two", "+441234567891", email2, "456 New St", "Manchester", "Greater Manchester", "M1 2AB"));
        String token = authenticateAndGetToken(email1);

        mockMvc.perform(delete("/v1/users/" + userId2)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteNonExistentUser_shouldReturnNotFound() throws Exception {
        String email = "deleteuser" + System.currentTimeMillis() + "@example.com";
        createUserAndGetId(buildUser("Test User", "+441234567890", email, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token = authenticateAndGetToken(email);

        String madeUpUserId = "nonexistent-" + System.currentTimeMillis();
        mockMvc.perform(delete("/v1/users/" + madeUpUserId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void authenticate_and_deleteUser_success() throws Exception {
        String email = "deleteme" + System.currentTimeMillis() + "@example.com";
        String userId = createUserAndGetId(buildUser("Delete Me", "+441234567890", email, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token = authenticateAndGetToken(email);

        mockMvc.perform(delete("/v1/users/" + userId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_withAccount_shouldReturnConflict() throws Exception {
        // Create and authenticate user
        String email = "userwithacc" + System.currentTimeMillis() + "@example.com";
        String userId = createUserAndGetId(buildUser("User With Account", "+441234567890", email, "123 Main St", "London", "Greater London", "E1 6AN"));
        String token = authenticateAndGetToken(email);

        // Create an account for this user
        com.eagle.request.CreateAccount account = new com.eagle.request.CreateAccount();
        account.setName("User Account");
        account.setAccountType("personal");
        account.setCurrency("GBP");

        mockMvc.perform(post("/v1/accounts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(account)))
                .andExpect(status().isCreated());

        // Try to delete the user (should return 409 Conflict)
        mockMvc.perform(delete("/v1/users/" + userId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }
}