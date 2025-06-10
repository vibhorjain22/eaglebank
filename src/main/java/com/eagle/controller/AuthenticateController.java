package com.eagle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.eagle.security.JwtService;
import com.eagle.repository.UserRepository;
import com.eagle.model.UserModel;

@RestController
@RequestMapping("/v1/authenticate")
public class AuthenticateController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    // DTO for authentication request
    public static class AuthRequest {
        private String username;

        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    // DTO for authentication response
    public static class AuthResponse {
        private String token;

        public AuthResponse(String token) {
            this.token = token;
        }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }

    @PostMapping
    public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest request) {
        // Check if user exists by email
        UserModel user = userRepository.findByEmail(request.getUsername())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        String token = jwtService.generateToken(user.getId());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}