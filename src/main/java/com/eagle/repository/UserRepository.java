package com.eagle.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.eagle.model.UserModel;

@Service
public interface UserRepository extends JpaRepository<UserModel, String> {
    // Additional query methods can be defined here if needed
    Optional<UserModel> findByEmail(String email);
}
