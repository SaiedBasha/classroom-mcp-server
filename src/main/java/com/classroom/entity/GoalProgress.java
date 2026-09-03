package com.classroom.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Tracks the progress of a student toward a goal's target practice count.
 */
@Entity
@Table(name = "goal_progress", uniqueConstraints = @UniqueConstraint(columnNames = {"goal_id", "student_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalProgress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @Column(name = "current_practice_count", nullable = false)
    private Integer currentPracticeCount = 0;
    
    @Column(name = "completed", nullable = false)
    private Boolean completed = false;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
