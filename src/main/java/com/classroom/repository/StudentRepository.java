package com.classroom.repository;

import com.classroom.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByNickname(String nickname);
    List<Student> findByClassroomId(Long classroomId);
    Optional<Student> findByClassroomIdAndName(Long classroomId, String name);
}
