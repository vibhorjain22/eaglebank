package com.eagle.repository;

import com.eagle.model.TransactionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionModel, String> {
    List<TransactionModel> findAllByAccountNumber(String accountNumber);
}