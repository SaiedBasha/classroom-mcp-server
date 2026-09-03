package com.classroom.repository;

import com.classroom.entity.SessionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SessionLogRepository extends JpaRepository<SessionLog, Long> {
    List<SessionLog> findByStudentIdAndLogDate(Long studentId, LocalDate logDate);
    List<SessionLog> findByLogDate(LocalDate logDate);
    List<SessionLog> findByClassroomIdAndLogDate(Long classroomId, LocalDate logDate);
}
