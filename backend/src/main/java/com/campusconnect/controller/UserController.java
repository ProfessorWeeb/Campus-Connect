package com.campusconnect.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campusconnect.model.User;
import com.campusconnect.security.UserPrincipal;
import com.campusconnect.service.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userService.getCurrentUser(userPrincipal.getId());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me/id")
    public ResponseEntity<Map<String, Object>> getCurrentUserId(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userPrincipal.getId());
        response.put("username", userPrincipal.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) Set<String> interests,
            @RequestParam(required = false) Set<String> skills,
            @RequestParam(required = false) Set<String> courses,
            @RequestParam(required = false) User.ProfileVisibility visibility,
            @RequestParam(required = false) String birthday,
            @RequestParam(required = false) String schoolYear,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String linkedin,
            @RequestParam(required = false) String github) {
        java.time.LocalDate birthdayDate = null;
        if (birthday != null && !birthday.isEmpty()) {
            try {
                birthdayDate = java.time.LocalDate.parse(birthday);
            } catch (Exception e) {
                // Invalid date format, ignore
            }
        }
        User user = userService.updateUser(userPrincipal.getId(), firstName, lastName, 
                                          major, bio, interests, skills, courses, visibility,
                                          birthdayDate, schoolYear, phoneNumber, location, linkedin, github);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String query) {
        List<User> users = userService.searchUsers(query);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/by-course")
    public ResponseEntity<List<User>> getUsersByCourse(@RequestParam String course) {
        List<User> users = userService.findUsersByCourse(course);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/by-skill")
    public ResponseEntity<List<User>> getUsersBySkill(@RequestParam String skill) {
        List<User> users = userService.findUsersBySkill(skill);
        return ResponseEntity.ok(users);
    }
}

