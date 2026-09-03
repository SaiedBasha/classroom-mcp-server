package com.classroom.service;

import com.classroom.entity.*;
import com.classroom.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating daily reports on classroom activities.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportingService {
    
    private final SessionLogRepository sessionLogRepository;
    private final StudentRepository studentRepository;
    private final ClassroomRepository classroomRepository;
    
    @Transactional(readOnly = true)
    public Map<String, Object> generateDailyReport(Long classroomId, LocalDate date) {
        Classroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new IllegalArgumentException("Classroom not found: " + classroomId));
        
        List<SessionLog> logsForDay = sessionLogRepository.findByLogDate(date);
        
        // Filter for this classroom's students
        Set<Long> classroomStudentIds = classroom.getStudents().stream()
            .map(Student::getId)
            .collect(Collectors.toSet());
        
        List<SessionLog> relevantLogs = logsForDay.stream()
            .filter(log -> classroomStudentIds.contains(log.getStudent().getId()))
            .collect(Collectors.toList());
        
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("date", date);
        report.put("classroom", classroom.getName());
        
        // Points gained/lost summary
        int totalPointsGained = 0;
        int totalPointsLost = 0;
        for (SessionLog log : relevantLogs) {
            if ("POINTS_AWARDED".equals(log.getActionType()) && log.getPointsChanged() != null) {
                totalPointsGained += log.getPointsChanged();
            } else if ("POINTS_DEDUCTED".equals(log.getActionType()) && log.getPointsChanged() != null) {
                totalPointsLost += Math.abs(log.getPointsChanged());
            }
        }
        
        report.put("totalPointsGained", totalPointsGained);
        report.put("totalPointsLost", totalPointsLost);
        
        // Practices completed count
        long practicesCompleted = relevantLogs.stream()
            .filter(log -> "SELECTED_FOR_GOAL".equals(log.getActionType()))
            .count();
        report.put("practicesCompleted", practicesCompleted);
        
        // Rank changes
        List<Map<String, String>> rankChanges = relevantLogs.stream()
            .filter(log -> "RANK_CHANGED".equals(log.getActionType()))
            .map(log -> {
                Map<String, String> change = new LinkedHashMap<>();
                change.put("student", log.getStudent().getNickname());
                change.put("change", log.getDescription());
                return change;
            })
            .collect(Collectors.toList());
        report.put("rankChanges", rankChanges);
        
        // Badges awarded
        List<Map<String, String>> badgesAwarded = relevantLogs.stream()
            .filter(log -> "BADGE_AWARDED".equals(log.getActionType()))
            .map(log -> {
                Map<String, String> badge = new LinkedHashMap<>();
                badge.put("student", log.getStudent().getNickname());
                badge.put("badge", log.getDescription());
                return badge;
            })
            .collect(Collectors.toList());
        report.put("badgesAwarded", badgesAwarded);
        
        // Student performance summary
        Map<String, Map<String, Object>> studentSummary = new LinkedHashMap<>();
        for (Student student : classroom.getStudents()) {
            List<SessionLog> studentLogs = relevantLogs.stream()
                .filter(log -> log.getStudent().getId().equals(student.getId()))
                .collect(Collectors.toList());
            
            if (studentLogs.isEmpty()) continue;
            
            Map<String, Object> summary = new LinkedHashMap<>();
            int studentPointsGained = 0;
            int studentPointsLost = 0;
            int studentPractices = 0;
            
            for (SessionLog log : studentLogs) {
                if ("POINTS_AWARDED".equals(log.getActionType()) && log.getPointsChanged() != null) {
                    studentPointsGained += log.getPointsChanged();
                } else if ("POINTS_DEDUCTED".equals(log.getActionType()) && log.getPointsChanged() != null) {
                    studentPointsLost += Math.abs(log.getPointsChanged());
                } else if ("SELECTED_FOR_GOAL".equals(log.getActionType())) {
                    studentPractices++;
                }
            }
            
            summary.put("pointsGained", studentPointsGained);
            summary.put("pointsLost", studentPointsLost);
            summary.put("practicesCompleted", studentPractices);
            summary.put("currentPoints", student.getCreditPoints());
            summary.put("currentRank", student.getRankLevel() != null ? student.getRankLevel().getName() : "None");
            
            studentSummary.put(student.getNickname(), summary);
        }
        report.put("studentSummary", studentSummary);
        
        return report;
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> generateDailyReportForToday(Long classroomId) {
        return generateDailyReport(classroomId, LocalDate.now());
    }
}
