package com.eagle.controller;

import com.eagle.model.UserModel;
import com.eagle.repository.AccountRepository;
import com.eagle.repository.UserRepository;
import com.eagle.request.CreateUser;
import com.eagle.request.UpdateUser;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    private final UserRepository repo;
    private final AccountRepository accountRepository;

    public UserController(UserRepository repo, AccountRepository accountRepository) {
        this.repo = repo;
        this.accountRepository = accountRepository;
    }
    // Create a new user
    @PostMapping
    public ResponseEntity<UserModel> createUser(@Valid @RequestBody CreateUser userRequest) {

        UserModel userModel = new UserModel();
        userModel.setName(userRequest.getName());
        userModel.setPhoneNumber(userRequest.getPhoneNumber());
        userModel.setEmail(userRequest.getEmail());
        userModel.setCreatedTimestamp(OffsetDateTime.now());
        userModel.setUpdatedTimestamp(OffsetDateTime.now());

        // Map address if present
        if (userRequest.getAddress() != null) {
            UserModel.Address address = new UserModel.Address();
            address.setLine1(userRequest.getAddress().getLine1());
            address.setLine2(userRequest.getAddress().getLine2());
            address.setLine3(userRequest.getAddress().getLine3());
            address.setTown(userRequest.getAddress().getTown());
            address.setCounty(userRequest.getAddress().getCounty());
            address.setPostcode(userRequest.getAddress().getPostcode());
            userModel.setAddress(address);
        }
        repo.save(userModel);
        return ResponseEntity.status(201).body(userModel);
    }

    // Fetch user by ID
    @PreAuthorize("@userSecurity.hasAccessToUser(#userId)")
    @GetMapping("/{userId}")
    public ResponseEntity<UserModel> fetchUserById(@PathVariable String userId) {
        return repo.findById(userId)
                .map(user -> {
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> {
                    return ResponseEntity.notFound().build();
                });
    }

    // Update user by ID
    @PreAuthorize("@userSecurity.hasAccessToUser(#userId)")
    @PatchMapping("/{userId}")
    public ResponseEntity<UserModel> updateUserById(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUser userUpdateRequest) {

        // Find the existing user
        return repo.findById(userId)
                .map(existingUser -> {
                    existingUser.setName(userUpdateRequest.getName());
                    existingUser.setPhoneNumber(userUpdateRequest.getPhoneNumber());
                    existingUser.setEmail(userUpdateRequest.getEmail());
                    existingUser.setUpdatedTimestamp(OffsetDateTime.now());

                    // Map address if present
                    if (userUpdateRequest.getAddress() != null) {
                        UserModel.Address address = new UserModel.Address();
                        address.setLine1(userUpdateRequest.getAddress().getLine1());
                        address.setLine2(userUpdateRequest.getAddress().getLine2());
                        address.setLine3(userUpdateRequest.getAddress().getLine3());
                        address.setTown(userUpdateRequest.getAddress().getTown());
                        address.setCounty(userUpdateRequest.getAddress().getCounty());
                        address.setPostcode(userUpdateRequest.getAddress().getPostcode());
                        existingUser.setAddress(address);
                    }

                    repo.save(existingUser);
                    return ResponseEntity.ok(existingUser);
                })
                .orElseGet(() -> {
                    return ResponseEntity.notFound().build();
                });
    }

    // Delete user by ID
    @PreAuthorize("@userSecurity.hasAccessToUser(#userId)")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUserById(@PathVariable String userId) {

        // Check if user has any accounts
        boolean hasAccounts = accountRepository.findAllByUserId(userId).size() > 0;
        if (hasAccounts) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User has associated accounts and cannot be deleted.");
        }

        return repo.findById(userId)
            .map(existingUser -> {
                repo.delete(existingUser);
                return ResponseEntity.noContent().build();
            })
            .orElseGet(() -> {
                return ResponseEntity.notFound().build();
            });
    }
}
