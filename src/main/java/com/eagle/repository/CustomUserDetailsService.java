package com.eagle.repository;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.eagle.model.UserModel;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository repo;

    public CustomUserDetailsService(UserRepository repo) {
        this.repo = repo;
    }
    
    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        UserModel user = repo.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(
                user.getId(), "{noop}dummy-password", Collections.emptyList());
    }
}
