package com.classroom.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Tracks daily actions: points gained/lost, practices completed, selections made.
 * Each log entry is timestamped and associated with a calendar day.
 */
@Entity
@Table(name = "session_logs", indexes = {
    @Index(name = "idx_student_date", columnList = "student_id,log_date"),
    @Index(name = "idx_log_date", columnList = "log_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @Column(name = "log_date", nullable = false)
    private LocalDate logDate; // Calendar day for grouping
    
    @Column(nullable = false)
    private LocalDateTime timestamp; // Exact time of action
    
    @Column(nullable = false)
    private String actionType; // e.g., "POINTS_AWARDED", "POINTS_DEDUCTED", "SELECTED_FOR_TASK", "PRACTICE_COMPLETED"
    
    @Column
    private Integer pointsChanged; // Positive or negative value
    
    @Column
    private String description; // Additional context
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private Goal relatedGoal; // If action is practice-related
}
