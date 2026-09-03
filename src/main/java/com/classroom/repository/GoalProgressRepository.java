package com.classroom.repository;

import com.classroom.entity.GoalProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalProgressRepository extends JpaRepository<GoalProgress, Long> {
    Optional<GoalProgress> findByGoalIdAndStudentId(Long goalId, Long studentId);
    List<GoalProgress> findByStudentIdAndCompleted(Long studentId, Boolean completed);
    List<GoalProgress> findByGoalId(Long goalId);
}
