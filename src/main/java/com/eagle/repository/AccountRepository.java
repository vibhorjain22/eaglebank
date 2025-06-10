package com.eagle.repository;

import com.eagle.model.AccountModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<AccountModel, String> {
    // Add custom queries if needed
    List<AccountModel> findAllByUserId(String userId);
}