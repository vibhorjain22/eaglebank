package com.eagle.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.eagle.repository.UserRepository;

@Component("userSecurity")
public class UserSecurity {

    private final UserRepository userRepository;

    public UserSecurity(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean hasAccessToUser(String userId) {
        String id = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findById(userId)
                .map(user -> user.getId().equals(id))
                .orElse(true); // If user does not exist, allow controller to handle not found
    }    
}
