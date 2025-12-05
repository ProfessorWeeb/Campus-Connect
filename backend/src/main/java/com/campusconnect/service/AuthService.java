package com.campusconnect.service;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusconnect.dto.AuthResponse;
import com.campusconnect.dto.LoginRequest;
import com.campusconnect.dto.RegisterRequest;
import com.campusconnect.model.User;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.security.JwtTokenProvider;
import com.campusconnect.security.UserPrincipal;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private MessageService messageService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setMajor(request.getMajor());
        if (request.getInterests() != null) {
            user.setInterests(request.getInterests());
        }
        if (request.getSkills() != null) {
            user.setSkills(request.getSkills());
        }
        if (request.getCourses() != null) {
            user.setCourses(request.getCourses());
        }

        user = userRepository.save(user);

        // Send welcome messages from 2-7 random bot users
        sendWelcomeMessagesFromBots(user.getId());

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = tokenProvider.generateToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        return new AuthResponse(token, "Bearer", user.getId(), user.getUsername(), 
                               user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(userPrincipal.getId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        return new AuthResponse(token, "Bearer", user.getId(), user.getUsername(), 
                               user.getEmail(), user.getRole().name());
    }

    /**
     * Send welcome messages from 2-7 random bot users to a new real user
     */
    private void sendWelcomeMessagesFromBots(Long newUserId) {
        try {
            // Get all bot users
            List<User> allBots = userRepository.findAll().stream()
                    .filter(User::getIsBot)
                    .collect(java.util.stream.Collectors.toList());

            if (allBots.isEmpty()) {
                return; // No bots available to send messages
            }

            // Randomly select 2-7 bots
            Random random = new Random();
            int numMessages = 2 + random.nextInt(6); // 2-7 messages
            numMessages = Math.min(numMessages, allBots.size()); // Don't exceed available bots

            // Shuffle and select random bots
            Collections.shuffle(allBots);
            List<User> selectedBots = allBots.subList(0, numMessages);

            // Welcome message templates
            List<String> welcomeMessages = List.of(
                    "Welcome to Campus Connect! Looking forward to connecting with you.",
                    "Hi! Welcome to the platform. Feel free to reach out if you need anything!",
                    "Hey there! Welcome to Campus Connect. Hope you find some great study groups!",
                    "Welcome! I'm excited to see you on Campus Connect. Let's connect!",
                    "Hi! Welcome to the community. Don't hesitate to message me if you have questions!",
                    "Welcome to Campus Connect! I'm here if you want to chat or need help getting started.",
                    "Hey! Great to see you joined. Welcome to Campus Connect!",
                    "Welcome! I hope you find some awesome study groups here. Feel free to reach out!",
                    "Hi there! Welcome to Campus Connect. Looking forward to connecting!",
                    "Welcome! If you need help navigating the platform, just let me know!"
            );

            // Get the new user to retrieve username
            User newUser = userRepository.findById(newUserId)
                    .orElse(null);
            
            if (newUser == null) {
                return; // User not found, can't send messages
            }
            
            // Send messages from each selected bot
            for (User bot : selectedBots) {
                try {
                    String message = welcomeMessages.get(random.nextInt(welcomeMessages.size()));
                    messageService.sendDirectMessage(bot.getId(), newUser.getUsername(), message);
                } catch (Exception e) {
                    // Log error but continue with other bots
                    System.err.println("Failed to send welcome message from bot " + bot.getId() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            // Don't fail registration if welcome messages fail
            System.err.println("Error sending welcome messages: " + e.getMessage());
        }
    }
}

