package com.eagle.controller;

import com.eagle.model.AccountModel;
import com.eagle.request.CreateAccount;
import com.eagle.request.UpdateAccount;

import jakarta.validation.Valid;

import com.eagle.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/v1/accounts")
public class AccountController {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // Utility method to generate a unique account number in the pattern 01\d{6}
    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            int randomSixDigits = (int)(Math.random() * 1_000_000);
            accountNumber = String.format("01%06d", randomSixDigits);
        } while (accountRepository.existsById(accountNumber));
        return accountNumber;
    }

    // Create a new bank account
    @PostMapping
    public ResponseEntity<AccountModel> createAccount(@Valid @RequestBody CreateAccount accountRequest) {
        AccountModel accountModel = new AccountModel();
        accountModel.setAccountNumber(generateUniqueAccountNumber());
        accountModel.setSortCode("10-10-10");
        accountModel.setName(accountRequest.getName());
        accountModel.setAccountType(accountRequest.getAccountType());
        accountModel.setCurrency(accountRequest.getCurrency());
        accountModel.setCreatedTimestamp(OffsetDateTime.now());
        accountModel.setUpdatedTimestamp(OffsetDateTime.now());

        // Extract userId from JWT token
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        accountModel.setUserId(userId);

        accountRepository.save(accountModel);
        return ResponseEntity.status(201).body(accountModel);
    }

    // List all bank accounts
    @GetMapping
    public ResponseEntity<List<AccountModel>> listAccounts() {
        return ResponseEntity.ok(accountRepository.findAll());
    }

    // Fetch account by account number
    @PreAuthorize("@accountSecurity.hasAccessToAccount(#accountNumber)")
    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountModel> fetchAccountByAccountNumber(@PathVariable String accountNumber) {
        return accountRepository.findById(accountNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update account by account number
    @PreAuthorize("@accountSecurity.hasAccessToAccount(#accountNumber)")
    @PatchMapping("/{accountNumber}")
    public ResponseEntity<AccountModel> updateAccountByAccountNumber(
            @PathVariable String accountNumber,
            @Valid @RequestBody UpdateAccount accountRequest) {
        return accountRepository.findById(accountNumber)
                .map(accountModel -> {
                    accountModel.setName(accountRequest.getName());
                    accountModel.setAccountType(accountRequest.getAccountType());
                    accountModel.setUpdatedTimestamp(OffsetDateTime.now());
                    accountRepository.save(accountModel);
                    return ResponseEntity.ok(accountModel);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete account by account number
    @PreAuthorize("@accountSecurity.hasAccessToAccount(#accountNumber)")
    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<Void> deleteAccountByAccountNumber(@PathVariable String accountNumber) {
        if (accountRepository.existsById(accountNumber)) {
            accountRepository.deleteById(accountNumber);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
