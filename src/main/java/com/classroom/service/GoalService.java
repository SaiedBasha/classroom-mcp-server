package com.classroom.service;

import com.classroom.entity.*;
import com.classroom.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing goals and tracking student progress.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GoalService {
    
    private final GoalRepository goalRepository;
    private final GoalProgressRepository goalProgressRepository;
    private final ClassroomRepository classroomRepository;
    private final StudentRepository studentRepository;
    
    @Transactional
    public Goal createGoal(Long classroomId, String name, String description, Integer targetPracticeCount) {
        Classroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new IllegalArgumentException("Classroom not found: " + classroomId));
        
        if (goalRepository.findByClassroomIdAndName(classroomId, name).isPresent()) {
            throw new IllegalArgumentException("Goal '" + name + "' already exists in this classroom");
        }
        
        Goal goal = Goal.builder()
            .name(name)
            .description(description)
            .classroom(classroom)
            .targetPracticeCount(targetPracticeCount)
            .build();
        return goalRepository.save(goal);
    }
    
    @Transactional
    public Goal updateGoal(Long goalId, String name, String description, Integer targetPracticeCount) {
        Goal goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new IllegalArgumentException("Goal not found: " + goalId));
        
        goal.setName(name);
        goal.setDescription(description);
        goal.setTargetPracticeCount(targetPracticeCount);
        return goalRepository.save(goal);
    }
    
    @Transactional
    public void deleteGoal(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new IllegalArgumentException("Goal not found: " + goalId));
        goalRepository.delete(goal);
    }
    
    @Transactional
    public void associateStudentWithGoal(Long goalId, Long studentId) {
        Goal goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new IllegalArgumentException("Goal not found: " + goalId));
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        
        if (!goal.getStudents().contains(student)) {
            goal.getStudents().add(student);
            student.getGoals().add(goal);
            goalRepository.save(goal);
            
            // Create initial goal progress
            GoalProgress progress = GoalProgress.builder()
                .goal(goal)
                .student(student)
                .currentPracticeCount(0)
                .completed(false)
                .build();
            goalProgressRepository.save(progress);
        }
    }
    
    @Transactional
    public void removeStudentFromGoal(Long goalId, Long studentId) {
        Goal goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new IllegalArgumentException("Goal not found: " + goalId));
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        
        goal.getStudents().remove(student);
        student.getGoals().remove(goal);
        
        GoalProgress progress = goalProgressRepository.findByGoalIdAndStudentId(goalId, studentId).orElse(null);
        if (progress != null) {
            goalProgressRepository.delete(progress);
        }
        
        goalRepository.save(goal);
    }
    
    public List<Goal> listGoals(Long classroomId) {
        return goalRepository.findByClassroomId(classroomId);
    }
    
    public Goal getGoal(Long goalId) {
        return goalRepository.findById(goalId)
            .orElseThrow(() -> new IllegalArgumentException("Goal not found: " + goalId));
    }
    
    public GoalProgress getStudentGoalProgress(Long goalId, Long studentId) {
        return goalProgressRepository.findByGoalIdAndStudentId(goalId, studentId)
            .orElseThrow(() -> new IllegalArgumentException("Goal progress not found"));
    }
    
    public List<GoalProgress> getGoalProgressList(Long goalId) {
        return goalProgressRepository.findByGoalId(goalId);
    }
}
