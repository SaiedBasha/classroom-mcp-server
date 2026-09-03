package com.classroom.repository;

import com.classroom.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByClassroomIdOrderByCreatedAtDesc(Long classroomId);
    List<Task> findByClassroomIdAndCompletedOrderByCreatedAtDesc(Long classroomId, Boolean completed);
}
