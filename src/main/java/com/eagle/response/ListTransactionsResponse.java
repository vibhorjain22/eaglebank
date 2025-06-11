package com.eagle.response;

import java.util.List;

public class ListTransactionsResponse {
    private List<TransactionResponse> transactions;

    public ListTransactionsResponse(List<TransactionResponse> responses) {
        this.transactions = responses;
    }

    public List<TransactionResponse> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionResponse> transactions) {
        this.transactions = transactions;
    }
}