package com.campusconnect.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    @Size(min = 6)
    @Column(nullable = false)
    private String password;

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 100)
    private String major;

    @Column(length = 1000)
    private String bio;

    @Column(name = "birthday")
    private java.time.LocalDate birthday;

    @Size(max = 50)
    @Column(name = "school_year")
    private String schoolYear; // e.g., "Freshman", "Sophomore", "Junior", "Senior", "Graduate"

    @Size(max = 20)
    @Column(name = "phone_number")
    private String phoneNumber;

    @Size(max = 200)
    @Column(name = "location")
    private String location; // e.g., "Macon, GA" or "Warner Robins Campus"

    @Size(max = 100)
    @Column(name = "linkedin")
    private String linkedin;

    @Size(max = 100)
    @Column(name = "github")
    private String github;

    @ElementCollection
    @CollectionTable(name = "user_interests", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "interest")
    private Set<String> interests = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "user_skills", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill")
    private Set<String> skills = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "user_courses", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "course")
    private Set<String> courses = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private ProfileVisibility visibility = ProfileVisibility.PUBLIC;

    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.STUDENT;

    @Column(nullable = false)
    private Boolean isBot = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ProfileVisibility {
        PUBLIC, PRIVATE
    }

    public enum UserRole {
        STUDENT, TA, FACULTY, ADMIN
    }
}

