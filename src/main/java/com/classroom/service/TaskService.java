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
 * Service for managing tasks (To-Do items).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    
    private final TaskRepository taskRepository;
    private final ClassroomRepository classroomRepository;
    
    @Transactional
    public Task createTask(Long classroomId, String title, String description, LocalDateTime dueDate) {
        Classroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new IllegalArgumentException("Classroom not found: " + classroomId));
        
        Task task = Task.builder()
            .title(title)
            .description(description)
            .classroom(classroom)
            .dueDate(dueDate)
            .completed(false)
            .build();
        return taskRepository.save(task);
    }
    
    @Transactional
    public Task markTaskAsCompleted(Long taskId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
        task.setCompleted(true);
        return taskRepository.save(task);
    }
    
    @Transactional
    public Task markTaskAsIncomplete(Long taskId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
        task.setCompleted(false);
        return taskRepository.save(task);
    }
    
    public List<Task> listAllTasks(Long classroomId) {
        return taskRepository.findByClassroomIdOrderByCreatedAtDesc(classroomId);
    }
    
    public List<Task> listCompletedTasks(Long classroomId) {
        return taskRepository.findByClassroomIdAndCompletedOrderByCreatedAtDesc(classroomId, true);
    }
    
    public List<Task> listIncompleteTasks(Long classroomId) {
        return taskRepository.findByClassroomIdAndCompletedOrderByCreatedAtDesc(classroomId, false);
    }
    
    public Task getTask(Long taskId) {
        return taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
    }
}
