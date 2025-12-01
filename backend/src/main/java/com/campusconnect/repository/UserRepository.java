package com.campusconnect.repository;

import com.campusconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);
    
    // Find users excluding bots
    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.isBot = false")
    java.util.List<User> findAllRealUsers();
    
    // Count real users (non-bots)
    long countByIsBotFalse();
    
    // Count bot users
    long countByIsBotTrue();
}

