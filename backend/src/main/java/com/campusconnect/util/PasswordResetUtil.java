package com.campusconnect.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class to generate BCrypt password hashes for password reset operations.
 * Run this as a standalone Java application to generate a hash for a password.
 */
public class PasswordResetUtil {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = args.length > 0 ? args[0] : "password123";
        String hash = encoder.encode(password);
        
        System.out.println("========================================");
        System.out.println("Password Reset Hash Generator");
        System.out.println("========================================");
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println("========================================");
        System.out.println();
        System.out.println("SQL Update Command:");
        System.out.println("UPDATE users SET password = '" + hash + "' WHERE email = 'daniel.underwood@mga.edu';");
        System.out.println();
    }
}

