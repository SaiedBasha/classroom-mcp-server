package com.classroom.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Extension to SessionLog to track classroom context.
 * Added to SessionLog entity for complete logging.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionLogDto {
    private Long sessionLogId;
    private String studentNickname;
    private String actionType;
    private Integer pointsChanged;
    private String description;
    private String goalName;
}
