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
 * Service for managing student groups.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {
    
    private final StudentGroupRepository groupRepository;
    private final ClassroomRepository classroomRepository;
    private final StudentRepository studentRepository;
    
    @Transactional
    public StudentGroup createGroup(Long classroomId, String groupName, String description) {
        Classroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new IllegalArgumentException("Classroom not found: " + classroomId));
        
        if (groupRepository.findByClassroomIdAndName(classroomId, groupName).isPresent()) {
            throw new IllegalArgumentException("Group '" + groupName + "' already exists in this classroom");
        }
        
        StudentGroup group = StudentGroup.builder()
            .name(groupName)
            .description(description)
            .classroom(classroom)
            .build();
        return groupRepository.save(group);
    }
    
    @Transactional
    public void addStudentToGroup(Long groupId, Long studentId) {
        StudentGroup group = groupRepository.findById(groupId)
            .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        
        group.getStudents().add(student);
        student.getGroups().add(group);
        groupRepository.save(group);
    }
    
    @Transactional
    public void addStudentToGroupByNickname(Long groupId, String nickname) {
        StudentGroup group = groupRepository.findById(groupId)
            .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));
        Student student = studentRepository.findByNickname(nickname)
            .orElseThrow(() -> new IllegalArgumentException("Student not found: " + nickname));
        
        group.getStudents().add(student);
        student.getGroups().add(group);
        groupRepository.save(group);
    }
    
    @Transactional
    public void removeStudentFromGroup(Long groupId, Long studentId) {
        StudentGroup group = groupRepository.findById(groupId)
            .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        
        group.getStudents().remove(student);
        student.getGroups().remove(group);
        groupRepository.save(group);
    }
    
    public List<StudentGroup> listGroups(Long classroomId) {
        return groupRepository.findByClassroomId(classroomId);
    }
    
    public StudentGroup getGroup(Long groupId) {
        return groupRepository.findById(groupId)
            .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));
    }
}
