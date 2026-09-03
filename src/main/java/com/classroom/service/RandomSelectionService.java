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

/**
 * Service for random selection with balanced picking based on daily session logs.
 * Ensures all students get a fair chance before anyone is repeated on the same calendar day.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RandomSelectionService {
    
    private final StudentRepository studentRepository;
    private final StudentGroupRepository groupRepository;
    private final SessionLogRepository sessionLogRepository;
    private final GoalRepository goalRepository;
    private final GoalProgressRepository goalProgressRepository;
    
    /**
     * Selects a single random student from a classroom, balancing against today's selections.
     */
    @Transactional
    public Student selectRandomStudent(Long classroomId) {
        List<Student> students = studentRepository.findByClassroomId(classroomId);
        if (students.isEmpty()) {
            throw new IllegalArgumentException("No students available in classroom: " + classroomId);
        }
        
        LocalDate today = LocalDate.now();
        List<Long> selectedToday = getStudentsSelectedToday(today);
        
        // Filter out students already selected today
        List<Student> candidatesNotSelected = students.stream()
            .filter(s -> !selectedToday.contains(s.getId()))
            .toList();
        
        if (!candidatesNotSelected.isEmpty()) {
            return candidatesNotSelected.get(new Random().nextInt(candidatesNotSelected.size()));
        }
        
        // If all selected, fall back to random from all
        return students.get(new Random().nextInt(students.size()));
    }
    
    /**
     * Selects multiple random students from a classroom.
     */
    @Transactional
    public List<Student> selectRandomStudents(Long classroomId, Integer count) {
        List<Student> students = studentRepository.findByClassroomId(classroomId);
        if (students.isEmpty()) {
            throw new IllegalArgumentException("No students available in classroom: " + classroomId);
        }
        
        int actualCount = Math.min(count, students.size());
        LocalDate today = LocalDate.now();
        List<Long> selectedToday = getStudentsSelectedToday(today);
        
        // Sort by selection count today (ascending)
        Map<Student, Integer> selectionCount = new HashMap<>();
        for (Student s : students) {
            int count_ = (int) selectedToday.stream().filter(id -> id.equals(s.getId())).count();
            selectionCount.put(s, count_);
        }
        
        return students.stream()
            .sorted(Comparator.comparingInt(s -> selectionCount.getOrDefault(s, 0)))
            .limit(actualCount)
            .toList();
    }
    
    /**
     * Selects a random student associated with a specific goal.
     */
    @Transactional
    public Student selectRandomStudentForGoal(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new IllegalArgumentException("Goal not found: " + goalId));
        
        Set<Student> goalStudents = goal.getStudents();
        if (goalStudents.isEmpty()) {
            throw new IllegalArgumentException("No students associated with goal: " + goalId);
        }
        
        LocalDate today = LocalDate.now();
        List<Long> selectedToday = getStudentsSelectedToday(today);
        
        List<Student> candidatesNotSelected = goalStudents.stream()
            .filter(s -> !selectedToday.contains(s.getId()))
            .toList();
        
        if (!candidatesNotSelected.isEmpty()) {
            Student selected = candidatesNotSelected.get(new Random().nextInt(candidatesNotSelected.size()));
            incrementGoalProgress(selected.getId(), goalId);
            logSelection(selected, "SELECTED_FOR_GOAL", goal.getName());
            return selected;
        }
        
        Student selected = new ArrayList<>(goalStudents).get(new Random().nextInt(goalStudents.size()));
        incrementGoalProgress(selected.getId(), goalId);
        logSelection(selected, "SELECTED_FOR_GOAL", goal.getName());
        return selected;
    }
    
    /**
     * Selects a random group from a classroom.
     */
    @Transactional
    public StudentGroup selectRandomGroup(Long classroomId) {
        List<StudentGroup> groups = groupRepository.findByClassroomId(classroomId);
        if (groups.isEmpty()) {
            throw new IllegalArgumentException("No groups available in classroom: " + classroomId);
        }
        return groups.get(new Random().nextInt(groups.size()));
    }
    
    private List<Long> getStudentsSelectedToday(LocalDate date) {
        List<SessionLog> logs = sessionLogRepository.findByLogDate(date);
        return logs.stream()
            .map(log -> log.getStudent().getId())
            .distinct()
            .toList();
    }
    
    private void incrementGoalProgress(Long studentId, Long goalId) {
        GoalProgress progress = goalProgressRepository.findByGoalIdAndStudentId(goalId, studentId)
            .orElseThrow(() -> new IllegalArgumentException("Goal progress not found"));
        
        progress.setCurrentPracticeCount(progress.getCurrentPracticeCount() + 1);
        
        Goal goal = progress.getGoal();
        if (progress.getCurrentPracticeCount() >= goal.getTargetPracticeCount()) {
            progress.setCompleted(true);
        }
        
        goalProgressRepository.save(progress);
    }
    
    private void logSelection(Student student, String actionType, String description) {
        SessionLog log = SessionLog.builder()
            .student(student)
            .logDate(LocalDate.now())
            .timestamp(LocalDateTime.now())
            .actionType(actionType)
            .description(description)
            .build();
        sessionLogRepository.save(log);
    }
}
