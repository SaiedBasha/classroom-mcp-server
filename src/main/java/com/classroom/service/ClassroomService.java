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
 * Service for managing classroom operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClassroomService {
    
    private final ClassroomRepository classroomRepository;
    private final StudentRepository studentRepository;
    private final RankLevelRepository rankLevelRepository;
    private final StudentGroupRepository groupRepository;
    
    @Transactional
    public Classroom createClassroom(String name, String description) {
        if (classroomRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Classroom with name '" + name + "' already exists");
        }
        Classroom classroom = Classroom.builder()
            .name(name)
            .description(description)
            .build();
        return classroomRepository.save(classroom);
    }
    
    public Classroom getClassroom(Long classroomId) {
        return classroomRepository.findById(classroomId)
            .orElseThrow(() -> new IllegalArgumentException("Classroom not found: " + classroomId));
    }
    
    public Classroom getClassroomByName(String name) {
        return classroomRepository.findByName(name)
            .orElseThrow(() -> new IllegalArgumentException("Classroom not found: " + name));
    }
    
    @Transactional
    public Student addStudent(Long classroomId, String name, String nickname) {
        Classroom classroom = getClassroom(classroomId);
        
        if (studentRepository.findByNickname(nickname).isPresent()) {
            throw new IllegalArgumentException("Nickname '" + nickname + "' already in use");
        }
        
        Student student = Student.builder()
            .name(name)
            .nickname(nickname)
            .classroom(classroom)
            .creditPoints(0)
            .rankLevel(getDefaultRankLevel())
            .build();
        return studentRepository.save(student);
    }
    
    @Transactional
    public void removeStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        studentRepository.delete(student);
    }
    
    @Transactional
    public Student updateStudentNickname(Long studentId, String newNickname) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        
        if (studentRepository.findByNickname(newNickname).isPresent()) {
            throw new IllegalArgumentException("Nickname '" + newNickname + "' already in use");
        }
        
        student.setNickname(newNickname);
        return studentRepository.save(student);
    }
    
    public List<Student> listStudents(Long classroomId) {
        return studentRepository.findByClassroomId(classroomId);
    }
    
    public Student getStudentByNickname(String nickname) {
        return studentRepository.findByNickname(nickname)
            .orElseThrow(() -> new IllegalArgumentException("Student not found: " + nickname));
    }
    
    private RankLevel getDefaultRankLevel() {
        return rankLevelRepository.findByName("Bronze")
            .orElseGet(() -> {
                RankLevel bronze = RankLevel.builder()
                    .name("Bronze")
                    .minPoints(0)
                    .maxPoints(50)
                    .description("Starting rank")
                    .badgeColor("#CD7F32")
                    .build();
                return rankLevelRepository.save(bronze);
            });
    }
    
    @Transactional
    public void initializeDefaultRankLevels() {
        if (rankLevelRepository.count() > 0) return;
        
        List<RankLevel> ranks = Arrays.asList(
            RankLevel.builder()
                .name("Bronze")
                .minPoints(0)
                .maxPoints(50)
                .description("Starting rank")
                .badgeColor("#CD7F32")
                .build(),
            RankLevel.builder()
                .name("Silver")
                .minPoints(51)
                .maxPoints(100)
                .description("Intermediate rank")
                .badgeColor("#C0C0C0")
                .build(),
            RankLevel.builder()
                .name("Gold")
                .minPoints(101)
                .maxPoints(200)
                .description("Advanced rank")
                .badgeColor("#FFD700")
                .build(),
            RankLevel.builder()
                .name("Platinum")
                .minPoints(201)
                .maxPoints(500)
                .description("Expert rank")
                .badgeColor("#E5E4E2")
                .build()
        );
        rankLevelRepository.saveAll(ranks);
    }
}
