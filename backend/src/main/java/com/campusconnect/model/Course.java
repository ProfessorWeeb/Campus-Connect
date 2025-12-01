package com.campusconnect.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String code; // e.g., "CSCI 1301"

    @Column(nullable = false, length = 200)
    private String name; // e.g., "Computer Science I"

    @Column(length = 50)
    private String department; // e.g., "CSCI"

    @Column(nullable = false)
    private Boolean active = true;
}

