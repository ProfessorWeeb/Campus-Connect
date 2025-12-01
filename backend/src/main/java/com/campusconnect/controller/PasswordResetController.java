package com.campusconnect.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campusconnect.repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")
public class PasswordResetController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Generate a BCrypt hash for a password (for password reset)
     * This is a temporary utility endpoint - remove in production!
     */
    @PostMapping("/generate-hash")
    public ResponseEntity<Map<String, String>> generateHash(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        Map<String, String> response = new HashMap<>();
        response.put("password", password);
        response.put("hash", hash);
        response.put("sql", "UPDATE users SET password = '" + hash + "' WHERE email = 'daniel.underwood@mga.edu';");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Reset password for a user (temporary utility - remove in production!)
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestParam String email,
            @RequestParam String newPassword) {
        
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "User not found");
            return ResponseEntity.badRequest().body(error);
        }
        
        var user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset successfully");
        response.put("email", email);
        return ResponseEntity.ok(response);
    }
}

