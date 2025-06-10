package com.eagle.security;

import com.eagle.repository.AccountRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("accountSecurity")
public class AccountSecurity {

    private final AccountRepository accountRepository;

    public AccountSecurity(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public boolean hasAccessToAccount(String accountNumber) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return accountRepository.findById(accountNumber)
                .map(account -> account.getUserId().equals(userId))
                .orElse(true); // allow controller to handle not found
    }
}