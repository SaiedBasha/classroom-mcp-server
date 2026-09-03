package com.classroom.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents rank levels tied to credit point thresholds.
 * Example: 0-50 = Bronze, 51-100 = Silver, 101+ = Gold
 */
@Entity
@Table(name = "rank_levels")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankLevel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name; // Bronze, Silver, Gold, etc.
    
    @Column(nullable = false)
    private Integer minPoints; // Minimum points required for this rank
    
    @Column(nullable = false)
    private Integer maxPoints; // Maximum points for this rank
    
    @Column
    private String description;
    
    @Column
    private String badgeColor; // Visual indicator
}
