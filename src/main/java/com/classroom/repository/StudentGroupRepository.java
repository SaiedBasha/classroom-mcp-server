package com.classroom.repository;

import com.classroom.entity.StudentGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentGroupRepository extends JpaRepository<StudentGroup, Long> {
    Optional<StudentGroup> findByClassroomIdAndName(Long classroomId, String name);
    List<StudentGroup> findByClassroomId(Long classroomId);
}
