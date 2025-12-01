package com.campusconnect.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String courseName;

    @Size(max = 100)
    private String courseCode;

    @Size(max = 200)
    private String topic;

    @Column(nullable = false)
    private Integer maxSize = 10;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToMany
    @JoinTable(
        name = "group_members",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GroupJoinRequest> joinRequests = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private GroupStatus status = GroupStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupVisibility visibility = GroupVisibility.PUBLIC;

    @Column(nullable = false)
    private Boolean requiresInvite = false;

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

    public enum GroupStatus {
        ACTIVE, INACTIVE, ARCHIVED
    }

    public enum GroupVisibility {
        PUBLIC,      // Visible to everyone in search/browse
        PRIVATE      // Hidden from search, only visible to members and those with direct link
    }
    
    /**
     * Group Privacy Combinations:
     * 1. PUBLIC + requiresInvite=false  → Public (anyone can join directly)
     * 2. PUBLIC + requiresInvite=true   → Public and Invite Only (visible but requires approval)
     * 3. PRIVATE + requiresInvite=false  → Private (hidden, but allows direct join if you have link)
     * 4. PRIVATE + requiresInvite=true   → Private and Invite Only (hidden, requires invitation)
     */
}

