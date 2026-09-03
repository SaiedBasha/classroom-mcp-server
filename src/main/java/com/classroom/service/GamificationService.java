package com.classroom.service;

import com.classroom.entity.*;
import com.classroom.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing gamification: points, badges, and rank adjustments.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GamificationService {
    
    private final StudentRepository studentRepository;
    private final BadgeRepository badgeRepository;
    private final RankLevelRepository rankLevelRepository;
    private final SessionLogRepository sessionLogRepository;
    
    @Transactional
    public void addPoints(Long studentId, Integer points, String reason) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        
        student.setCreditPoints(student.getCreditPoints() + points);
        updateRankLevel(student);
        studentRepository.save(student);
        
        logSessionAction(student, "POINTS_AWARDED", points, reason);
    }
    
    @Transactional
    public void deductPoints(Long studentId, Integer points, String reason) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        
        student.setCreditPoints(Math.max(0, student.getCreditPoints() - points));
        updateRankLevel(student);
        studentRepository.save(student);
        
        logSessionAction(student, "POINTS_DEDUCTED", -points, reason);
    }
    
    @Transactional
    public void addPointsToClassStudents(Long classroomId, Integer points, String reason) {
        List<Student> students = studentRepository.findByClassroomId(classroomId);
        for (Student student : students) {
            addPoints(student.getId(), points, reason);
        }
    }
    
    @Transactional
    public void addPointsToStudents(List<Long> studentIds, Integer points, String reason) {
        for (Long studentId : studentIds) {
            addPoints(studentId, points, reason);
        }
    }
    
    @Transactional
    public void addPointsToGroup(Long groupId, Integer points, String reason) {
        // Implementation in GroupService
    }
    
    @Transactional
    public void awardBadge(Long studentId, Long badgeId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        Badge badge = badgeRepository.findById(badgeId)
            .orElseThrow(() -> new IllegalArgumentException("Badge not found: " + badgeId));
        
        if (!student.getBadges().contains(badge)) {
            student.getBadges().add(badge);
            badge.getStudents().add(student);
            studentRepository.save(student);
            logSessionAction(student, "BADGE_AWARDED", 0, "Badge: " + badge.getName());
        }
    }
    
    @Transactional
    public void removeBadge(Long studentId, Long badgeId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        Badge badge = badgeRepository.findById(badgeId)
            .orElseThrow(() -> new IllegalArgumentException("Badge not found: " + badgeId));
        
        if (student.getBadges().contains(badge)) {
            student.getBadges().remove(badge);
            badge.getStudents().remove(student);
            studentRepository.save(student);
            logSessionAction(student, "BADGE_REMOVED", 0, "Badge: " + badge.getName());
        }
    }
    
    @Transactional
    public Badge createBadge(String name, String description, String icon) {
        if (badgeRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Badge with name '" + name + "' already exists");
        }
        Badge badge = Badge.builder()
            .name(name)
            .description(description)
            .icon(icon)
            .build();
        return badgeRepository.save(badge);
    }
    
    @Transactional
    public void removeBadgeDefinition(Long badgeId) {
        Badge badge = badgeRepository.findById(badgeId)
            .orElseThrow(() -> new IllegalArgumentException("Badge not found: " + badgeId));
        badgeRepository.delete(badge);
    }
    
    private void updateRankLevel(Student student) {
        Integer points = student.getCreditPoints();
        RankLevel rank = rankLevelRepository
            .findByMinPointsLessThanEqualAndMaxPointsGreaterThanEqual(points, points)
            .orElse(rankLevelRepository.findByName("Bronze").orElse(null));
        
        if (rank != null && !rank.equals(student.getRankLevel())) {
            student.setRankLevel(rank);
            logSessionAction(student, "RANK_CHANGED", 0, "New Rank: " + rank.getName());
        }
    }
    
    private void logSessionAction(Student student, String actionType, Integer pointsChanged, String description) {
        SessionLog log = SessionLog.builder()
            .student(student)
            .logDate(LocalDate.now())
            .timestamp(LocalDateTime.now())
            .actionType(actionType)
            .pointsChanged(pointsChanged)
            .description(description)
            .build();
        sessionLogRepository.save(log);
    }
    
    public List<Student> getLeaderboardByPoints(Long classroomId) {
        List<Student> students = studentRepository.findByClassroomId(classroomId);
        return students.stream()
            .sorted((s1, s2) -> s2.getCreditPoints().compareTo(s1.getCreditPoints()))
            .collect(Collectors.toList());
    }
    
    public List<Student> getLeaderboardByRank(Long classroomId) {
        List<Student> students = studentRepository.findByClassroomId(classroomId);
        return students.stream()
            .sorted((s1, s2) -> {
                int rankCompare = s2.getRankLevel().getMinPoints().compareTo(s1.getRankLevel().getMinPoints());
                if (rankCompare != 0) return rankCompare;
                return s2.getCreditPoints().compareTo(s1.getCreditPoints());
            })
            .collect(Collectors.toList());
    }
}
