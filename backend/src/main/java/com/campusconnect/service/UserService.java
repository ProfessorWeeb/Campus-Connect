package com.campusconnect.service;

import com.campusconnect.model.User;
import com.campusconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User getCurrentUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional
    public User updateUser(Long userId, String firstName, String lastName, String major, 
                          String bio, Set<String> interests, Set<String> skills, 
                          Set<String> courses, User.ProfileVisibility visibility,
                          java.time.LocalDate birthday, String schoolYear, String phoneNumber,
                          String location, String linkedin, String github) {
        User user = getCurrentUser(userId);
        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);
        if (major != null) user.setMajor(major);
        if (bio != null) user.setBio(bio);
        if (interests != null) user.setInterests(interests);
        if (skills != null) user.setSkills(skills);
        if (courses != null) user.setCourses(courses);
        if (visibility != null) user.setVisibility(visibility);
        if (birthday != null) user.setBirthday(birthday);
        if (schoolYear != null) user.setSchoolYear(schoolYear);
        if (phoneNumber != null) user.setPhoneNumber(phoneNumber);
        if (location != null) user.setLocation(location);
        if (linkedin != null) user.setLinkedin(linkedin);
        if (github != null) user.setGithub(github);
        return userRepository.save(user);
    }

    public List<User> searchUsers(String query) {
        return userRepository.findAll().stream()
                .filter(user -> !user.getIsBot()) // Exclude bots
                .filter(user -> 
                    (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(query.toLowerCase())) ||
                    (user.getLastName() != null && user.getLastName().toLowerCase().contains(query.toLowerCase())) ||
                    (user.getMajor() != null && user.getMajor().toLowerCase().contains(query.toLowerCase())) ||
                    (user.getUsername().toLowerCase().contains(query.toLowerCase())) ||
                    user.getInterests().stream().anyMatch(i -> i.toLowerCase().contains(query.toLowerCase())) ||
                    user.getSkills().stream().anyMatch(s -> s.toLowerCase().contains(query.toLowerCase())) ||
                    user.getCourses().stream().anyMatch(c -> c.toLowerCase().contains(query.toLowerCase()))
                )
                .toList();
    }

    public List<User> findUsersByCourse(String course) {
        return userRepository.findAll().stream()
                .filter(user -> !user.getIsBot()) // Exclude bots
                .filter(user -> user.getCourses().contains(course))
                .toList();
    }

    public List<User> findUsersBySkill(String skill) {
        return userRepository.findAll().stream()
                .filter(user -> !user.getIsBot()) // Exclude bots
                .filter(user -> user.getSkills().contains(skill))
                .toList();
    }
}

