package com.classroom.mcp;

import com.classroom.entity.*;
import com.classroom.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.model.tool.Tool;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * MCP Tool Registry for Classroom Management System.
 * Exposes all tools via @Tool annotation for Claude integration.
 * 
 * All parameter names are explicit to clarify what values the LLM should provide:
 * - IDs (classroomId, studentId, goalId, etc.) are typically Long values from previous operations
 * - Names (name, nickname, groupName, etc.) are String descriptors
 * - Counts (count, points, targetPracticeCount) are Integer values
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ClassroomToolRegistry {
    
    private final ClassroomService classroomService;
    private final StudentRepository studentRepository;
    private final GamificationService gamificationService;
    private final GroupService groupService;
    private final TaskService taskService;
    private final GoalService goalService;
    private final RandomSelectionService randomSelectionService;
    private final ExcelService excelService;
    private final ReportingService reportingService;
    
    // ==================== Classroom Management ====================
    
    @Tool(description = "Create a new classroom. Required: name (String - unique classroom name), description (String - classroom purpose/details)")
    public String createClassroom(String name, String description) {
        try {
            Classroom classroom = classroomService.createClassroom(name, description);
            return "Classroom '" + classroom.getName() + "' created successfully (ID: " + classroom.getId() + ")";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Tool(description = "Get classroom details by ID. Required: classroomId (Long - numeric ID returned from createClassroom)")
    public String getClassroom(Long classroomId) {
        try {
            Classroom classroom = classroomService.getClassroom(classroomId);
            return "Classroom: " + classroom.getName() + " (ID: " + classroom.getId() + ", Students: " + classroom.getStudents().size() + ")";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    // ==================== Student Management ====================
    
    @Tool(description = "Add a student to a classroom. Required: classroomId (Long - ID from createClassroom or listStudents), name (String - full name), nickname (String - unique short name for the student)")
    public String addStudent(Long classroomId, String name, String nickname) {
        try {
            Student student = classroomService.addStudent(classroomId, name, nickname);
            return "Student '" + student.getName() + "' (\"" + student.getNickname() + "\") added successfully (ID: " + student.getId() + ")";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Tool(description = "Remove a student from the system. Required: studentId (Long - numeric ID from addStudent)")
    public String removeStudent(Long studentId) {
        try {
            classroomService.removeStudent(studentId);
            return "Student removed successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Tool(description = "Update a student's nickname. Required: studentId (Long - numeric ID from addStudent), newNickname (String - new unique nickname)")
    public String updateStudentNickname(Long studentId, String newNickname) {
        try {
            Student student = classroomService.updateStudentNickname(studentId, newNickname);
            return "Student nickname updated to '" + student.getNickname() + "'";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Tool(description = "List all students in a classroom with their points and ranks. Required: classroomId (Long - ID from createClassroom)")
    public String listStudents(Long classroomId) {
        try {
            List<Student> students = classroomService.listStudents(classroomId);
            if (students.isEmpty()) return "No students in this classroom";
            
            StringBuilder sb = new StringBuilder("Students in classroom:\n");
            for (Student s : students) {
                sb.append("- ").append(s.getNickname()).append(" (").append(s.getName()).append("): ")
                  .append(s.getCreditPoints()).append(" points, Rank: ")
                  .append(s.getRankLevel() != null ? s.getRankLevel().getName() : "None").append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    // ==================== Gamification ====================
    
    @Tool(description = "Add points to a single student. Required: studentId (Long - numeric ID from addStudent), points (Integer - positive number of points), reason (String - why points are being awarded)")
    public String addPoints(Long studentId, Integer points, String reason) {
        try {
            gamificationService.addPoints(studentId, points, reason);
            Student student = studentRepository.findById(studentId).orElseThrow();
            return "Added " + points + " points to " + student.getNickname() + ". New total: " + student.getCreditPoints();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Tool(description = "Deduct points from a single student. Required: studentId (Long - numeric ID from addStudent), points (Integer - positive number of points to remove), reason (String - why points are being deducted)")
    public String deductPoints(Long studentId, Integer points, String reason) {
        try {
            gamificationService.deductPoints(studentId, points, reason);
            Student student = studentRepository.findById(studentId).orElseThrow();
            return "Deducted " + points + " points from " + student.getNickname() + ". New total: " + student.getCreditPoints();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Tool(description = "Add points to all students in a classroom at once. Required: classroomId (Long - ID from createClassroom), points (Integer - positive points for each student), reason (String - justification)")
    public String addPointsToClass(Long classroomId, Integer points, String reason) {
        try {
            gamificationService.addPointsToClassStudents(classroomId, points, reason);
            return "Added " + points + " points to all students in classroom";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Tool(description = "Add points to multiple specific students. Required: studentIds (List<Long> - array of student IDs like [1,2,3]), points (Integer - points for each), reason (String - justification)")
    public String addPointsToStudents(List<Long> studentIds, Integer points, String reason) {
        try {
            gamificationService.addPointsToStudents(studentIds, points, reason);
            return "Added " + points + " points to " + studentIds.size() + " students";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Tool(description = "Award a badge to a student. Required: studentId (Long - numeric ID from addStudent), badgeId (Long - numeric ID from createBadge)")
    public String awardBadge(Long studentId, Long badgeId) {
        try {
            gamificationService.awardBadge(studentId, badgeId);
            return "Badge awarded successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Tool(description = "Remove a badge from a student. Required: studentId (Long - numeric ID from addStudent), badgeId (Long - numeric ID from createBadge)")
    public String removeBadge(Long studentId, Long badgeId) {
        try {
            gamificationService.removeBadge(studentId, badgeId);
            return "Badge removed successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Tool(description = "Create a new badge. Required: name (String - unique badge name), description (String - what it means), icon (String - emoji or icon path)")
    public String createBadge(String name, String description, String icon) {
        try {
            Badge badge = gamificationService.createBadge(name, description, icon);
            return "Badge '" + badge.getName() + "' created successfully (ID: " + badge.getId() + ")";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    // ==================== Leaderboard ====================
    
    @Tool(description = "Get leaderboard sorted by credit points (highest first). Required: classroomId (Long - ID from createClassroom)")
    public String getLeaderboardByPoints(Long classroomId) {
        try {
            List<Student> leaderboard = gamificationService.getLeaderboardByPoints(classroomId);
            if (leaderboard.isEmpty()) return "No students in leaderboard";
            
            StringBuilder sb = new StringBuilder("Leaderboard (by Points):\n");
            int rank = 1;
            for (Student s : leaderboard) {
                sb.append(rank++).append(". ").append(s.getNickname()).append(" - ")
                  .append(s.getCreditPoints()).append(" points\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Tool(description = "Get leaderboard sorted by rank level (highest rank first). Required: classroomId (Long - ID from createClassroom)")
    public String getLeaderboardByRank(Long classroomId) {
        try {
            List<Student> leaderboard = gamificationService.getLeaderboardByRank(classroomId);
            if (leaderboard.isEmpty()) return "No students in leaderboard";
            
            StringBuilder sb = new StringBuilder("Leaderboard (by Rank):\n");
            int rank = 1;
            for (Student s : leaderboard) {
                String rankName = s.getRankLevel() != null ? s.getRankLevel().getName() : "None";
                sb.append(rank++).append(". ").append(s.getNickname()).append(" - ")
                  .append(rankName).append(" (").append(s.getCreditPoints()).append(" points)\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    // ==================== Groups ====================
    
    @Tool(description = "Create a new student group. Required: classroomId (Long - ID from createClassroom), groupName (String - unique group name), description (String - group purpose)")
    public String createGroup(Long classroomId, String groupName, String description) {
        try {
            StudentGroup group = groupService.createGroup(classroomId, groupName, description);
            return "Group '" + group.getName() + "' created successfully (ID: " + group.getId() + ")";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Tool(description = "Add a student to a group using student ID. Required: groupId (Long - ID from createGroup), studentId (Long - ID from addStudent)")
    public String addStudentToGroup(Long groupId, Long studentId) {
        try {
            groupService.addStudentToGroup(groupId, studentId);
            return "Student added to group successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Tool(description = "Add a student to a group using their nickname. Required: groupId (Long - ID from createGroup), nickname (String - student nickname)")
    public String addStudentToGroupByNickname(Long groupId, String nickname) {
        try {
            groupService.addStudentToGroupByNickname(groupId, nickname);
            return "Student '" + nickname + "' added to group successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Tool(description = "Remove a student from a group. Required: groupId (Long - ID from createGroup), studentId (Long - ID from addStudent)")
    public String removeStudentFromGroup(Long groupId, Long studentId) {
        try {
            groupService.removeStudentFromGroup(groupId, studentId);
            return "Student removed from group successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Tool(description = "List all groups in a classroom with their members. Required: classroomId (Long - ID from createClassroom)")
    public String listGroups(Long classroomId) {
        try {
            List<StudentGroup> groups = groupService.listGroups(classroomId);
            if (groups.isEmpty()) return "No groups in this classroom";
            
            StringBuilder sb = new StringBuilder("Groups in classroom:\n");
            for (StudentGroup g : groups) {
                sb.append("- ").append(g.getName()).append(" (").append(g.getStudents().size())
                  .append(" members): ").append(g.getStudents().stream().map(Student::getNickname)
                  .reduce((a, b) -> a + ", " + b).orElse("No members")).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    // ==================== Tasks ====================
    
    @Tool(description = "Create a new task/to-do item. Required: classroomId (Long - ID from createClassroom), title (String - task name), description (String - task details)")
    public String createTask(Long classroomId, String title, String description) {
        try {
            Task task = taskService.createTask(classroomId, title, description, null);
            return "Task '" + task.getTitle() + "' created successfully (ID: " + task.getId() + ")";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Tool(description = "Mark a task as completed. Required: taskId (Long - numeric ID from createTask)")
    public String markTaskAsCompleted(Long taskId) {
        try {
            Task task = taskService.markTaskAsCompleted(taskId);
            return "Task '" + task.getTitle() + "' marked as completed";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Tool(description = "Mark a task as incomplete/not done. Required: taskId (Long - numeric ID from createTask)")
    public String markTaskAsIncomplete(Long taskId) {
        try {
            Task task = taskService.markTaskAsIncomplete(taskId);
            return "Task '" + task.getTitle() + "' marked as incomplete";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Tool(description = "List all tasks in a classroom with completion status. Required: classroomId (Long - ID from createClassroom)")
    public String listTasks(Long classroomId) {
        try {
            List<Task> tasks = taskService.listAllTasks(classroomId);
            if (tasks.isEmpty()) return "No tasks in this classroom";
            
            StringBuilder sb = new StringBuilder("Tasks:\n");
            for (Task t : tasks) {
                String status = t.getCompleted() ? "✓" : "✗";
                sb.append("[").append(status).append("] ").append(t.getTitle()).append(" (ID: ")
                  .append(t.getId()).append(")\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    // ==================== Goals ====================
    
    @Tool(description = "Create a skill-based goal. Required: classroomId (Long - ID from createClassroom), name (String - goal name), description (String - what skill), targetPracticeCount (Integer - number of practices to complete)")
    public String createGoal(Long classroomId, String name, String description, Integer targetPracticeCount) {
        try {
            Goal goal = goalService.createGoal(classroomId, name, description, targetPracticeCount);
            return "Goal '" + goal.getName() + "' created successfully (ID: " + goal.getId() + ", Target: " + targetPracticeCount + " practices)";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Tool(description = "Associate a student with a goal so they can work on it. Required: goalId (Long - ID from createGoal), studentId (Long - ID from addStudent)")
    public String associateStudentWithGoal(Long goalId, Long studentId) {
        try {
            goalService.associateStudentWithGoal(goalId, studentId);
            return "Student associated with goal successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Tool(description = "Remove a student from a goal. Required: goalId (Long - ID from createGoal), studentId (Long - ID from addStudent)")
    public String removeStudentFromGoal(Long goalId, Long studentId) {
        try {
            goalService.removeStudentFromGoal(goalId, studentId);
            return "Student removed from goal successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Tool(description = "List all goals in a classroom with student counts and targets. Required: classroomId (Long - ID from createClassroom)")
    public String listGoals(Long classroomId) {
        try {
            List<Goal> goals = goalService.listGoals(classroomId);
            if (goals.isEmpty()) return "No goals in this classroom";
            
            StringBuilder sb = new StringBuilder("Goals:\n");
            for (Goal g : goals) {
                sb.append("- ").append(g.getName()).append(": ").append(g.getDescription())
                  .append(" (Target: ").append(g.getTargetPracticeCount()).append(" practices, ")
                  .append(g.getStudents().size()).append(" students)\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Tool(description = "Get progress for a student on a specific goal. Required: goalId (Long - ID from createGoal), studentId (Long - ID from addStudent)")
    public String getGoalProgress(Long goalId, Long studentId) {
        try {
            GoalProgress progress = goalService.getStudentGoalProgress(goalId, studentId);
            Student student = studentRepository.findById(studentId).orElseThrow();
            return "Goal Progress for " + student.getNickname() + ": " + progress.getCurrentPracticeCount()
                   + " / " + progress.getGoal().getTargetPracticeCount() + " practices completed (Completed: " + progress.getCompleted() + ")";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    // ==================== Random Selection ====================
    
    @Tool(description = "Select one random student from a classroom with fair balancing. Ensures no student is picked twice in one day if possible. Required: classroomId (Long - ID from createClassroom)")
    public String selectRandomStudent(Long classroomId) {
        try {
            Student selected = randomSelectionService.selectRandomStudent(classroomId);
            return "Randomly selected: " + selected.getNickname() + " (Name: " + selected.getName() + ")";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Tool(description = "Select multiple random students from a classroom with fair balancing. Required: classroomId (Long - ID from createClassroom), count (Integer - number of students to select)")
    public String selectRandomStudents(Long classroomId, Integer count) {
        try {
            List<Student> selected = randomSelectionService.selectRandomStudents(classroomId, count);
            StringBuilder sb = new StringBuilder("Randomly selected " + selected.size() + " students:\n");
            for (Student s : selected) {
                sb.append("- ").append(s.getNickname()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Tool(description = "Select one random student who is working on a specific goal. Automatically increments their practice count. Required: goalId (Long - ID from createGoal)")
    public String selectRandomStudentForGoal(Long goalId) {
        try {
            Student selected = randomSelectionService.selectRandomStudentForGoal(goalId);
            return "Randomly selected for goal: " + selected.getNickname();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Tool(description = "Select one random group from a classroom. Required: classroomId (Long - ID from createClassroom)")
    public String selectRandomGroup(Long classroomId) {
        try {
            StudentGroup selected = randomSelectionService.selectRandomGroup(classroomId);
            return "Randomly selected group: " + selected.getName() + " (" + selected.getStudents().size() + " members)";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    // ==================== Data Portability (Excel) ====================
    
    @Tool(description = "Export classroom data to an Excel file. Required: classroomId (Long - ID from createClassroom), filename (String - output filename like 'my_classroom.xlsx')")
    public String exportClassroomToExcel(Long classroomId, String filename) {
        try {
            String filepath = excelService.exportClassroomToExcel(classroomId, filename);
            return "Classroom data exported successfully to: " + filepath;
        } catch (IOException e) {
            return "Error exporting Excel: " + e.getMessage();
        }
    }
    
    @Tool(description = "Import classroom data from an Excel file. Required: filename (String - the filename to import from)")
    public String importClassroomFromExcel(String filename) {
        try {
            String result = excelService.importClassroomFromExcel(filename);
            return "Import result: " + result;
        } catch (IOException e) {
            return "Error importing Excel: " + e.getMessage();
        }
    }
    
    @Tool(description = "Get the configured directory path where Excel files are stored or will be saved")
    public String getExcelDirectory() {
        return "Excel files directory: " + excelService.getExcelDirectory();
    }
    
    // ==================== Daily Reporting ====================
    
    @Tool(description = "Generate a detailed report of all classroom activities from today. Required: classroomId (Long - ID from createClassroom)")
    public String generateDailyReportToday(Long classroomId) {
        try {
            Map<String, Object> report = reportingService.generateDailyReportForToday(classroomId);
            return formatReport(report);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @Tool(description = "Generate a detailed report of classroom activities for a specific date. Required: classroomId (Long - ID from createClassroom), dateStr (String - date in format YYYY-MM-DD like '2026-09-03')")
    public String generateDailyReport(Long classroomId, String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            Map<String, Object> report = reportingService.generateDailyReport(classroomId, date);
            return formatReport(report);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    private String formatReport(Map<String, Object> report) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Daily Report for ").append(report.get("date")).append(" ===").append("\n")
          .append("Classroom: ").append(report.get("classroom")).append("\n")
          .append("Points Gained: ").append(report.get("totalPointsGained")).append("\n")
          .append("Points Lost: ").append(report.get("totalPointsLost")).append("\n")
          .append("Practices Completed: ").append(report.get("practicesCompleted")).append("\n");
        
        @SuppressWarnings("unchecked")
        List<Map<String, String>> rankChanges = (List<Map<String, String>>) report.get("rankChanges");
        if (!rankChanges.isEmpty()) {
            sb.append("Rank Changes:\n");
            for (Map<String, String> change : rankChanges) {
                sb.append("  - ").append(change.get("student")).append(": ").append(change.get("change")).append("\n");
            }
        }
        
        return sb.toString();
    }
}
