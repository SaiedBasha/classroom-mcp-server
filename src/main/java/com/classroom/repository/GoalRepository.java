package com.classroom.repository;

import com.classroom.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByClassroomId(Long classroomId);
    Optional<Goal> findByClassroomIdAndName(Long classroomId, String name);
}
