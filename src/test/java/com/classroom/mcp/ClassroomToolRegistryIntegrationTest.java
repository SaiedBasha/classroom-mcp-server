package com.classroom.mcp;

import com.classroom.entity.*;
import com.classroom.repository.*;
import com.classroom.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ClassroomToolRegistry MCP tools.
 * Tests the complete workflow from classroom creation through reporting.
 */
@SpringBootTest
@Transactional
class ClassroomToolRegistryIntegrationTest {
    
    @Autowired
    private ClassroomToolRegistry toolRegistry;
    
    @Autowired
    private ClassroomRepository classroomRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private BadgeRepository badgeRepository;
    
    @Autowired
    private GoalRepository goalRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private StudentGroupRepository groupRepository;
    
    private Long classroomId;
    
    @BeforeEach
    void setUp() {
        // Create a classroom for testing
        String result = toolRegistry.createClassroom("Test Classroom", "Testing MCP tools");
        assertThat(result).contains("created successfully");
        classroomId = 1L; // First created classroom
    }
    
    // ==================== Classroom Management Tests ====================
    
    @Test
    void testCreateClassroom() {
        String result = toolRegistry.createClassroom("New Classroom", "Description");
        assertTrue(result.contains("created successfully"));
        assertThat(classroomRepository.count()).isGreaterThan(1);
    }
    
    @Test
    void testGetClassroom() {
        String result = toolRegistry.getClassroom(classroomId);
        assertTrue(result.contains("Test Classroom"));
        assertTrue(result.contains("Students: 0"));
    }
    
    @Test
    void testGetClassroomWithInvalidId() {
        String result = toolRegistry.getClassroom(99999L);
        assertTrue(result.contains("Error"));
    }
    
    // ==================== Student Management Tests ====================
    
    @Test
    void testAddStudent() {
        String result = toolRegistry.addStudent(classroomId, "John Smith", "john");
        assertTrue(result.contains("John Smith"));
        assertTrue(result.contains("added successfully"));
        assertThat(studentRepository.findByNickname("john")).isPresent();
    }
    
    @Test
    void testAddMultipleStudents() {
        toolRegistry.addStudent(classroomId, "John Smith", "john");
        toolRegistry.addStudent(classroomId, "Sarah Johnson", "sarah");
        toolRegistry.addStudent(classroomId, "Mike Davis", "mike");
        
        assertThat(studentRepository.count()).isEqualTo(3);
    }
    
    @Test
    void testAddStudentWithDuplicateNickname() {
        toolRegistry.addStudent(classroomId, "John Smith", "john");
        String result = toolRegistry.addStudent(classroomId, "Another John", "john");
        assertTrue(result.contains("Error"));
    }
    
    @Test
    void testListStudents() {
        toolRegistry.addStudent(classroomId, "John Smith", "john");
        toolRegistry.addStudent(classroomId, "Sarah Johnson", "sarah");
        
        String result = toolRegistry.listStudents(classroomId);
        assertTrue(result.contains("john"));
        assertTrue(result.contains("sarah"));
        assertTrue(result.contains("points"));
    }
    
    @Test
    void testListStudentsEmpty() {
        String result = toolRegistry.listStudents(classroomId);
        assertTrue(result.contains("No students"));
    }
    
    @Test
    void testUpdateStudentNickname() {
        toolRegistry.addStudent(classroomId, "John Smith", "john");
        Long studentId = studentRepository.findByNickname("john").get().getId();
        
        String result = toolRegistry.updateStudentNickname(studentId, "johnny");
        assertTrue(result.contains("johnny"));
        assertThat(studentRepository.findByNickname("johnny")).isPresent();
        assertThat(studentRepository.findByNickname("john")).isEmpty();
    }
    
    @Test
    void testRemoveStudent() {
        toolRegistry.addStudent(classroomId, "John Smith", "john");
        Long studentId = studentRepository.findByNickname("john").get().getId();
        
        String result = toolRegistry.removeStudent(studentId);
        assertTrue(result.contains("removed successfully"));
        assertThat(studentRepository.findById(studentId)).isEmpty();
    }
    
    // ==================== Gamification Tests ====================
    
    @Test
    void testAddPoints() {
        toolRegistry.addStudent(classroomId, "John Smith", "john");
        Long studentId = studentRepository.findByNickname("john").get().getId();
        
        String result = toolRegistry.addPoints(studentId, 25, "Excellent participation");
        assertTrue(result.contains("Added 25 points"));
        
        Student student = studentRepository.findById(studentId).get();
        assertEquals(25, student.getCreditPoints());
    }
    
    @Test
    void testDeductPoints() {
        toolRegistry.addStudent(classroomId, "John Smith", "john");
        Long studentId = studentRepository.findByNickname("john").get().getId();
        
        toolRegistry.addPoints(studentId, 50, "Initial points");
        String result = toolRegistry.deductPoints(studentId, 20, "Lost points");
        
        assertTrue(result.contains("Deducted 20 points"));
        Student student = studentRepository.findById(studentId).get();
        assertEquals(30, student.getCreditPoints());
    }
    
    @Test
    void testAddPointsToClass() {
        toolRegistry.addStudent(classroomId, "John Smith", "john");
        toolRegistry.addStudent(classroomId, "Sarah Johnson", "sarah");
        
        String result = toolRegistry.addPointsToClass(classroomId, 10, "Class bonus");
        assertTrue(result.contains("Added 10 points"));
        
        List<Student> students = studentRepository.findByClassroomId(classroomId);
        assertTrue(students.stream().allMatch(s -> s.getCreditPoints() == 10));
    }
    
    @Test
    void testAddPointsToMultipleStudents() {
        toolRegistry.addStudent(classroomId, "John Smith", "john");
        toolRegistry.addStudent(classroomId, "Sarah Johnson", "sarah");
        
        Long johnId = studentRepository.findByNickname("john").get().getId();
        Long sarahId = studentRepository.findByNickname("sarah").get().getId();
        
        String result = toolRegistry.addPointsToStudents(Arrays.asList(johnId, sarahId), 15, "Team bonus");
        assertTrue(result.contains("Added 15 points to 2 students"));
        
        assertEquals(15, studentRepository.findById(johnId).get().getCreditPoints());
        assertEquals(15, studentRepository.findById(sarahId).get().getCreditPoints());
    }
    
    @Test
    void testCreateBadge() {
        String result = toolRegistry.createBadge("Star Performer", "Excellent work", "⭐");
        assertTrue(result.contains("created successfully"));
        assertThat(badgeRepository.findByName("Star Performer")).isPresent();
    }
    
    @Test
    void testAwardBadge() {
        toolRegistry.addStudent(classroomId, "John Smith", "john");
        Long studentId = studentRepository.findByNickname("john").get().getId();
        
        toolRegistry.createBadge("Star Performer", "Excellent work", "⭐");
        Long badgeId = badgeRepository.findByName("Star Performer").get().getId();
        
        String result = toolRegistry.awardBadge(studentId, badgeId);
        assertTrue(result.contains("successfully"));
        
        Student student = studentRepository.findById(studentId).get();
        assertThat(student.getBadges()).isNotEmpty();
    }
    
    @Test
    void testRemoveBadge() {
        toolRegistry.addStudent(classroomId, "John Smith", "john");
        Long studentId = studentRepository.findByNickname("john").get().getId();
        
        toolRegistry.createBadge("Star Performer", "Excellent work", "⭐");
        Long badgeId = badgeRepository.findByName("Star Performer").get().getId();
        
        toolRegistry.awardBadge(studentId, badgeId);
        String result = toolRegistry.removeBadge(studentId, badgeId);
        assertTrue(result.contains("successfully"));
        
        Student student = studentRepository.findById(studentId).get();
        assertThat(student.getBadges()).isEmpty();
    }
    
    // ==================== Leaderboard Tests ====================
    
    @Test
    void testLeaderboardByPoints() {
        toolRegistry.addStudent(classroomId, "John Smith", "john");
        toolRegistry.addStudent(classroomId, "Sarah Johnson", "sarah");
        
        Long johnId = studentRepository.findByNickname("john").get().getId();
        Long sarahId = studentRepository.findByNickname("sarah").get().getId();
        
        toolRegistry.addPoints(johnId, 50, "High performer");
        toolRegistry.addPoints(sarahId, 30, "Good work");
        
        String result = toolRegistry.getLeaderboardByPoints(classroomId);
        assertTrue(result.contains("john"));
        assertTrue(result.contains("sarah"));
        // John should be ranked 1st with 50 points
        int johnPos = result.indexOf("john");
        int sarahPos = result.indexOf("sarah");
        assertTrue(johnPos < sarahPos, "John should appear before Sarah in leaderboard");
    }
    
    @Test
    void testLeaderboardByRank() {
        toolRegistry.addStudent(classroomId, "John Smith", "john");
        toolRegistry.addStudent(classroomId, "Sarah Johnson", "sarah");
        
        Long johnId = studentRepository.findByNickname("john").get().getId();
        Long sarahId = studentRepository.findByNickname("sarah").get().getId();
        
        // Add points to move them to different ranks
        toolRegistry.addPoints(johnId, 150, "Promotion to Gold");
        toolRegistry.addPoints(sarahId, 30, "Stays in Silver");
        
        String result = toolRegistry.getLeaderboardByRank(classroomId);
        assertTrue(result.contains("john"));
        assertTrue(result.contains("sarah"));
        assertTrue(result.contains("points"));
    }
    
    // ==================== Group Tests ====================
    
    @Test
    void testCreateGroup() {
        String result = toolRegistry.createGroup(classroomId, "Team A", "Science project");
        assertTrue(result.contains("created successfully"));
        assertThat(groupRepository.findByClassroomIdAndName(classroomId, "Team A")).isPresent();
    }
    
    @Test
    void testAddStudentToGroupById() {
        toolRegistry.addStudent(classroomId, "John Smith", "john");
        Long studentId = studentRepository.findByNickname("john").get().getId();
        
        toolRegistry.createGroup(classroomId, "Team A", "Science project");
        Long groupId = groupRepository.findByClassroomIdAndName(classroomId, "Team A").get().getId();
        
        String result = toolRegistry.addStudentToGroup(groupId, studentId);
        assertTrue(result.contains("successfully"));
        
        StudentGroup group = groupRepository.findById(groupId).get();
        assertThat(group.getStudents()).hasSize(1);
    }
    
    @Test
    void testAddStudentToGroupByNickname() {
        toolRegistry.addStudent(classroomId, "John Smith", "john");
        toolRegistry.createGroup(classroomId, "Team A", "Science project");
        Long groupId = groupRepository.findByClassroomIdAndName(classroomId, "Team A").get().getId();
        
        String result = toolRegistry.addStudentToGroupByNickname(groupId, "john");
        assertTrue(result.contains("successfully"));
        
        StudentGroup group = groupRepository.findById(groupId).get();
        assertThat(group.getStudents()).hasSize(1);
    }
    
    @Test
    void testRemoveStudentFromGroup() {
        toolRegistry.addStudent(classroomId, "John Smith", "john");
        Long studentId = studentRepository.findByNickname("john").get().getId();
        
        toolRegistry.createGroup(classroomId, "Team A", "Science project");
        Long groupId = groupRepository.findByClassroomIdAndName(classroomId, "Team A").get().getId();
        
        toolRegistry.addStudentToGroup(groupId, studentId);
        String result = toolRegistry.removeStudentFromGroup(groupId, studentId);
        assertTrue(result.contains("successfully"));
        
        StudentGroup group = groupRepository.findById(groupId).get();
        assertThat(group.getStudents()).isEmpty();
    }
    
    @Test
    void testListGroups() {
        toolRegistry.createGroup(classroomId, "Team A", "Science");
        toolRegistry.createGroup(classroomId, "Team B", "Math");
        
        String result = toolRegistry.listGroups(classroomId);
        assertTrue(result.contains("Team A"));
        assertTrue(result.contains("Team B"));
    }
    
    // ==================== Task Tests ====================
    
    @Test
    void testCreateTask() {
        String result = toolRegistry.createTask(classroomId, "Complete homework", "Chapter 5-7");
        assertTrue(result.contains("created successfully"));
        assertThat(taskRepository.count()).isGreaterThan(0);
    }
    
    @Test
    void testMarkTaskAsCompleted() {
        toolRegistry.createTask(classroomId, "Complete homework", "Chapter 5-7");
        Long taskId = taskRepository.findByClassroomIdOrderByCreatedAtDesc(classroomId).get(0).getId();
        
        String result = toolRegistry.markTaskAsCompleted(taskId);
        assertTrue(result.contains("marked as completed"));
        
        Task task = taskRepository.findById(taskId).get();
        assertTrue(task.getCompleted());
    }
    
    @Test
    void testMarkTaskAsIncomplete() {
        toolRegistry.createTask(classroomId, "Complete homework", "Chapter 5-7");
        Long taskId = taskRepository.findByClassroomIdOrderByCreatedAtDesc(classroomId).get(0).getId();
        
        toolRegistry.markTaskAsCompleted(taskId);
        String result = toolRegistry.markTaskAsIncomplete(taskId);
        assertTrue(result.contains("marked as incomplete"));
        
        Task task = taskRepository.findById(taskId).get();
        assertFalse(task.getCompleted());
    }
    
    @Test
    void testListTasks() {
        toolRegistry.createTask(classroomId, "Task 1", "Description 1");
        toolRegistry.createTask(classroomId, "Task 2", "Description 2");
        
        String result = toolRegistry.listTasks(classroomId);
        assertTrue(result.contains("Task 1"));
        assertTrue(result.contains("Task 2"));
        assertTrue(result.contains("✗")); // Not completed
    }
    
    // ==================== Goal Tests ====================
    
    @Test
    void testCreateGoal() {
        String result = toolRegistry.createGoal(classroomId, "Math Multiplication", "Master multiplication tables", 10);
        assertTrue(result.contains("created successfully"));
        assertTrue(result.contains("Target: 10 practices"));
        assertThat(goalRepository.findByClassroomIdAndName(classroomId, "Math Multiplication")).isPresent();
    }
    
    @Test
    void testAssociateStudentWithGoal() {
        toolRegistry.addStudent(classroomId, "John Smith", "john");
        Long studentId = studentRepository.findByNickname("john").get().getId();
        
        toolRegistry.createGoal(classroomId, "Math Multiplication", "Master multiplication tables", 10);
        Long goalId = goalRepository.findByClassroomIdAndName(classroomId, "Math Multiplication").get().getId();
        
        String result = toolRegistry.associateStudentWithGoal(goalId, studentId);
        assertTrue(result.contains("successfully"));
        
        Goal goal = goalRepository.findById(goalId).get();
        assertThat(goal.getStudents()).hasSize(1);
    }
    
    @Test
    void testRemoveStudentFromGoal() {
        toolRegistry.addStudent(classroomId, "John Smith", "john");
        Long studentId = studentRepository.findByNickname("john").get().getId();
        
        toolRegistry.createGoal(classroomId, "Math Multiplication", "Master multiplication tables", 10);
        Long goalId = goalRepository.findByClassroomIdAndName(classroomId, "Math Multiplication").get().getId();
        
        toolRegistry.associateStudentWithGoal(goalId, studentId);
        String result = toolRegistry.removeStudentFromGoal(goalId, studentId);
        assertTrue(result.contains("successfully"));
        
        Goal goal = goalRepository.findById(goalId).get();
        assertThat(goal.getStudents()).isEmpty();
    }
    
    @Test
    void testListGoals() {
        toolRegistry.createGoal(classroomId, "Goal 1", "Description 1", 5);
        toolRegistry.createGoal(classroomId, "Goal 2", "Description 2", 10);
        
        String result = toolRegistry.listGoals(classroomId);
        assertTrue(result.contains("Goal 1"));
        assertTrue(result.contains("Goal 2"));
    }
    
    @Test
    void testGetGoalProgress() {
        toolRegistry.addStudent(classroomId, "John Smith", "john");
        Long studentId = studentRepository.findByNickname("john").get().getId();
        
        toolRegistry.createGoal(classroomId, "Math Multiplication", "Master multiplication tables", 10);
        Long goalId = goalRepository.findByClassroomIdAndName(classroomId, "Math Multiplication").get().getId();
        
        toolRegistry.associateStudentWithGoal(goalId, studentId);
        
        String result = toolRegistry.getGoalProgress(goalId, studentId);
        assertTrue(result.contains("john"));
        assertTrue(result.contains("0 / 10 practices"));
    }
    
    // ==================== Random Selection Tests ====================
    
    @Test
    void testSelectRandomStudent() {
        toolRegistry.addStudent(classroomId, "John Smith", "john");
        toolRegistry.addStudent(classroomId, "Sarah Johnson", "sarah");
        
        String result = toolRegistry.selectRandomStudent(classroomId);
        assertTrue(result.contains("Randomly selected"));
        assertTrue(result.contains("john") || result.contains("sarah"));
    }
    
    @Test
    void testSelectRandomStudents() {
        toolRegistry.addStudent(classroomId, "John Smith", "john");
        toolRegistry.addStudent(classroomId, "Sarah Johnson", "sarah");
        toolRegistry.addStudent(classroomId, "Mike Davis", "mike");
        
        String result = toolRegistry.selectRandomStudents(classroomId, 2);
        assertTrue(result.contains("Randomly selected 2 students"));
    }
    
    @Test
    void testSelectRandomStudentForGoal() {
        toolRegistry.addStudent(classroomId, "John Smith", "john");
        Long studentId = studentRepository.findByNickname("john").get().getId();
        
        toolRegistry.createGoal(classroomId, "Math Multiplication", "Master multiplication tables", 10);
        Long goalId = goalRepository.findByClassroomIdAndName(classroomId, "Math Multiplication").get().getId();
        
        toolRegistry.associateStudentWithGoal(goalId, studentId);
        
        String result = toolRegistry.selectRandomStudentForGoal(goalId);
        assertTrue(result.contains("Randomly selected for goal"));
        assertTrue(result.contains("john"));
    }
    
    @Test
    void testSelectRandomGroup() {
        toolRegistry.createGroup(classroomId, "Team A", "Science");
        toolRegistry.createGroup(classroomId, "Team B", "Math");
        
        String result = toolRegistry.selectRandomGroup(classroomId);
        assertTrue(result.contains("Randomly selected group"));
        assertTrue(result.contains("Team") || result.contains("members"));
    }
    
    // ==================== Reporting Tests ====================
    
    @Test
    void testGenerateDailyReportToday() {
        toolRegistry.addStudent(classroomId, "John Smith", "john");
        Long studentId = studentRepository.findByNickname("john").get().getId();
        
        toolRegistry.addPoints(studentId, 25, "Test points");
        
        String result = toolRegistry.generateDailyReportToday(classroomId);
        assertTrue(result.contains("Daily Report"));
        assertTrue(result.contains("Test Classroom"));
        assertTrue(result.contains("Points Gained: 25"));
    }
    
    @Test
    void testGenerateDailyReportForSpecificDate() {
        toolRegistry.addStudent(classroomId, "John Smith", "john");
        Long studentId = studentRepository.findByNickname("john").get().getId();
        
        toolRegistry.addPoints(studentId, 25, "Test points");
        
        String result = toolRegistry.generateDailyReport(classroomId, LocalDate.now().toString());
        assertTrue(result.contains("Daily Report"));
        assertTrue(result.contains("Test Classroom"));
    }
    
    @Test
    void testGetExcelDirectory() {
        String result = toolRegistry.getExcelDirectory();
        assertTrue(result.contains("Excel files directory"));
    }
    
    // ==================== End-to-End Workflow Tests ====================
    
    @Test
    void testCompleteClassroomWorkflow() {
        // 1. Create students
        toolRegistry.addStudent(classroomId, "Alice", "alice");
        toolRegistry.addStudent(classroomId, "Bob", "bob");
        toolRegistry.addStudent(classroomId, "Charlie", "charlie");
        
        Long aliceId = studentRepository.findByNickname("alice").get().getId();
        Long bobId = studentRepository.findByNickname("bob").get().getId();
        Long charlieId = studentRepository.findByNickname("charlie").get().getId();
        
        // 2. Create groups
        toolRegistry.createGroup(classroomId, "Team 1", "Science Team");
        Long groupId = groupRepository.findByClassroomIdAndName(classroomId, "Team 1").get().getId();
        
        toolRegistry.addStudentToGroupByNickname(groupId, "alice");
        toolRegistry.addStudentToGroupByNickname(groupId, "bob");
        
        // 3. Create goals
        toolRegistry.createGoal(classroomId, "Reading", "Improve reading skills", 5);
        Long goalId = goalRepository.findByClassroomIdAndName(classroomId, "Reading").get().getId();
        
        toolRegistry.associateStudentWithGoal(goalId, aliceId);
        toolRegistry.associateStudentWithGoal(goalId, bobId);
        
        // 4. Award points and badges
        toolRegistry.createBadge("Top Performer", "Best student", "🏆");
        Long badgeId = badgeRepository.findByName("Top Performer").get().getId();
        
        toolRegistry.addPoints(aliceId, 50, "Excellent work");
        toolRegistry.addPoints(bobId, 30, "Good participation");
        toolRegistry.addPoints(charlieId, 20, "Needs improvement");
        
        toolRegistry.awardBadge(aliceId, badgeId);
        
        // 5. Create tasks
        toolRegistry.createTask(classroomId, "Reading Assignment", "Read chapter 1-5");
        Long taskId = taskRepository.findByClassroomIdOrderByCreatedAtDesc(classroomId).get(0).getId();
        toolRegistry.markTaskAsCompleted(taskId);
        
        // 6. Verify data
        String leaderboard = toolRegistry.getLeaderboardByPoints(classroomId);
        assertTrue(leaderboard.contains("alice"));
        assertTrue(leaderboard.contains("50 points"));
        
        String report = toolRegistry.generateDailyReportToday(classroomId);
        assertTrue(report.contains("Points Gained: 100")); // 50+30+20
        
        String students = toolRegistry.listStudents(classroomId);
        assertTrue(students.contains("alice"));
        assertTrue(students.contains("bob"));
        assertTrue(students.contains("charlie"));
    }
    
    // Assertion helper for readability
    private void assertThat(Object actual) {
        assertNotNull(actual);
    }
}
