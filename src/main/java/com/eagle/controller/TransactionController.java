package com.eagle.controller;

import com.eagle.model.TransactionModel;
import com.eagle.request.CreateTransaction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v1/accounts/{accountNumber}/transactions")
public class TransactionController {

    // List all transactions for an account
    @GetMapping
    public ResponseEntity<List<TransactionModel>> listTransactions(@PathVariable String accountNumber) {
        // Stub: Replace with service call
        List<TransactionModel> transactions = new ArrayList<>();
        return ResponseEntity.ok(transactions);
    }

    // Create a new transaction for an account
    @PostMapping
    public ResponseEntity<TransactionModel> createTransaction(
            @PathVariable String accountNumber,
            @RequestBody CreateTransaction transactionRequest
    ) {
        // Map CreateTransaction to TransactionModel
        TransactionModel transactionModel = new TransactionModel();
        transactionModel.setId("tan-123abc");
        transactionModel.setAmount(transactionRequest.getAmount());
        transactionModel.setCurrency(transactionRequest.getCurrency());
        transactionModel.setType(transactionRequest.getType());
        transactionModel.setReference(transactionRequest.getReference());
        transactionModel.setUserId(transactionRequest.getUserId());
        transactionModel.setCreatedTimestamp(OffsetDateTime.now());

        return ResponseEntity.status(201).body(transactionModel);
    }

    // Get a transaction by ID for an account
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionModel> getTransactionById(
            @PathVariable String accountNumber,
            @PathVariable String transactionId
    ) {
        // Stub: Replace with service call
        TransactionModel transaction = new TransactionModel();
        transaction.setId(transactionId);
        transaction.setCreatedTimestamp(OffsetDateTime.now());
        return ResponseEntity.ok(transaction);
    }
}
