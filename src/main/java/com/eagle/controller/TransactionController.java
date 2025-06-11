package com.eagle.controller;

import com.eagle.model.AccountModel;
import com.eagle.model.TransactionModel;
import com.eagle.request.CreateTransaction;
import com.eagle.repository.TransactionRepository;
import com.eagle.repository.AccountRepository;
import com.eagle.response.ListTransactionsResponse;
import com.eagle.response.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/accounts/{accountNumber}/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    // List all transactions for an account
    @PreAuthorize("@accountSecurity.hasAccessToAccount(#accountNumber)")
    @GetMapping
    public ResponseEntity<ListTransactionsResponse> listTransactions(@PathVariable String accountNumber) {
        // Check if account exists
        if (!accountRepository.existsById(accountNumber)) {
            return ResponseEntity.notFound().build();
        }

        List<TransactionModel> transactions = transactionRepository.findAllByAccountNumber(accountNumber);

        List<TransactionResponse> responses = transactions.stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(new ListTransactionsResponse(responses));
    }

    // Create a new transaction for an account
    @PreAuthorize("@accountSecurity.hasAccessToAccount(#accountNumber)")
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @PathVariable String accountNumber,
            @Valid @RequestBody CreateTransaction transactionRequest,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        // Fetch account directly from repository
        AccountModel account = accountRepository.findById(accountNumber).orElse(null);
        if (account == null) {
            return ResponseEntity.status(404).build();
        }

        // Check balance for withdrawal
        if ("withdrawal".equalsIgnoreCase(transactionRequest.getType())) {
            if (account.getBalance() < transactionRequest.getAmount()) {
                return ResponseEntity.status(422).body(null); // Or use a custom error response
            }
        }

        // Update balance
        double updateAmount = transactionRequest.getAmount();
        if ("withdrawal".equalsIgnoreCase(transactionRequest.getType())) {
            updateAmount = -updateAmount;
        }
        account.setBalance(account.getBalance() + updateAmount);
        accountRepository.save(account);

        // Create and save transaction
        TransactionModel transactionModel = new TransactionModel();
        transactionModel.setId("tan-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        transactionModel.setAmount(transactionRequest.getAmount());
        transactionModel.setCurrency(transactionRequest.getCurrency());
        transactionModel.setType(transactionRequest.getType());
        transactionModel.setReference(transactionRequest.getReference());
        transactionModel.setCreatedTimestamp(OffsetDateTime.now());
        transactionModel.setAccountNumber(accountNumber);
        transactionModel.setUserId(userId);

        transactionRepository.save(transactionModel);

        // Transform to response
        TransactionResponse response = toResponse(transactionModel);

        return ResponseEntity.status(201).body(response);
    }

    // Get a transaction by ID for an account
    @PreAuthorize("@accountSecurity.hasAccessToAccount(#accountNumber)")
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable String accountNumber,
            @PathVariable String transactionId
    ) {
        if (!accountRepository.existsById(accountNumber)) {
            return ResponseEntity.notFound().build();
        }

        return transactionRepository.findById(transactionId)
                .filter(tx -> accountNumber.equals(tx.getAccountNumber()))
                .map(tx -> {
                    return toResponse(tx);
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Helper method to map TransactionModel to TransactionResponse
    private TransactionResponse toResponse(TransactionModel transactionModel) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transactionModel.getId());
        response.setAmount(transactionModel.getAmount());
        response.setCurrency(transactionModel.getCurrency());
        response.setType(transactionModel.getType());
        response.setReference(transactionModel.getReference());
        response.setUserId(transactionModel.getUserId());
        response.setCreatedTimestamp(transactionModel.getCreatedTimestamp());
        return response;
    }
}
