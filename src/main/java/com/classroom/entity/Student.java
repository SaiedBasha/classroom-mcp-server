package com.classroom.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a student in a classroom.
 */
@Entity
@Table(name = "students", uniqueConstraints = @UniqueConstraint(columnNames = {"classroom_id", "name"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String nickname;
    
    @Column(name = "credit_points", nullable = false)
    private Integer creditPoints = 0;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rank_level_id")
    private RankLevel rankLevel;
    
    @ManyToMany(mappedBy = "students")
    private Set<StudentGroup> groups = new HashSet<>();
    
    @ManyToMany(mappedBy = "students")
    private Set<Badge> badges = new HashSet<>();
    
    @ManyToMany(mappedBy = "students")
    private Set<Goal> goals = new HashSet<>();
    
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SessionLog> sessionLogs = new HashSet<>();
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
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
}
