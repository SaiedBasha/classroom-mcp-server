# Classroom MCP Server

A production-ready Model Context Protocol (MCP) server for managing classroom activities, built with Spring Boot, Spring AI, and H2 Database.

## Features

### Core Functionality
- **Classroom Management**: Create and manage multiple classrooms
- **Student Management**: Add/remove students, manage nicknames, track credit points
- **Gamification**: Award points, badges, and dynamic rank adjustments
- **Groups**: Organize students into groups
- **Task Management**: Create and track to-do items
- **Goal Management**: Set skill-based goals with practice tracking
- **Balanced Random Selection**: Ensure fair student participation with daily history tracking
- **Leaderboards**: View rankings by points or rank level
- **Data Portability**: Export/import via Excel
- **Daily Reporting**: Generate activity summaries by date

## Tech Stack

- **Java 21**: Modern Java with records, pattern matching, and virtual threads
- **Spring Boot 3.4+**: Latest Spring framework
- **Spring AI**: MCP (Model Context Protocol) support
- **H2 Database**: File-based, zero-configuration database
- **Apache POI**: Excel file processing
- **JPA/Hibernate**: ORM layer
- **Lombok**: Boilerplate reduction

## Project Structure

```
src/main/java/com/classroom/
├── entity/              # JPA entities (Classroom, Student, Goal, etc.)
├── repository/          # Spring Data JPA repositories
├── service/             # Business logic
│   ├── ClassroomService
│   ├── GamificationService
│   ├── RandomSelectionService
│   ├── GroupService
│   ├── TaskService
│   ├── GoalService
│   ├── ReportingService
│   └── ExcelService
├── mcp/                 # MCP tool registry
│   └── ClassroomToolRegistry  # All @Tool annotated methods
├── config/              # Spring configuration
└── ClassroomMcpServerApplication  # Main entry point
```

## Building and Running

### Prerequisites
- Java 21+
- Maven 3.8+

### Build

```bash
# Make the build script executable
chmod +x build-and-zip.sh

# Run the build and package script
./build-and-zip.sh
```

This will:
1. Clean previous builds
2. Compile and test the project
3. Create a JAR file
4. Package everything into a distributable ZIP

### Run

```bash
java -jar target/classroom-mcp-server-1.0.0.jar
```

The server will start on `http://localhost:8080`

## MCP Tools Available

All tools are exposed via the `ClassroomToolRegistry` component:

### Classroom & Student Management
- `createClassroom(name, description)` - Create a classroom
- `addStudent(classroomId, name, nickname)` - Add a student
- `removeStudent(studentId)` - Remove a student
- `updateStudentNickname(studentId, newNickname)` - Update nickname
- `listStudents(classroomId)` - List all students

### Gamification
- `addPoints(studentId, points, reason)` - Award points to a student
- `deductPoints(studentId, points, reason)` - Deduct points
- `addPointsToClass(classroomId, points, reason)` - Award to entire class
- `addPointsToStudents(studentIds, points, reason)` - Award to specific students
- `createBadge(name, description, icon)` - Create a badge
- `awardBadge(studentId, badgeId)` - Award badge to student
- `removeBadge(studentId, badgeId)` - Remove badge

### Leaderboard
- `getLeaderboardByPoints(classroomId)` - Sorted by points
- `getLeaderboardByRank(classroomId)` - Sorted by rank level

### Groups
- `createGroup(classroomId, groupName, description)` - Create group
- `addStudentToGroup(groupId, studentId)` - Add by ID
- `addStudentToGroupByNickname(groupId, nickname)` - Add by nickname
- `removeStudentFromGroup(groupId, studentId)` - Remove from group
- `listGroups(classroomId)` - List all groups

### Tasks
- `createTask(classroomId, title, description)` - Create task
- `markTaskAsCompleted(taskId)` - Mark done
- `markTaskAsIncomplete(taskId)` - Mark undone
- `listTasks(classroomId)` - List all tasks

### Goals
- `createGoal(classroomId, name, description, targetPracticeCount)` - Create goal
- `associateStudentWithGoal(goalId, studentId)` - Link student to goal
- `removeStudentFromGoal(goalId, studentId)` - Unlink student
- `listGoals(classroomId)` - List all goals
- `getGoalProgress(goalId, studentId)` - Get progress details

### Random Selection
- `selectRandomStudent(classroomId)` - Select 1 student (balanced)
- `selectRandomStudents(classroomId, count)` - Select multiple (balanced)
- `selectRandomStudentForGoal(goalId)` - Select for specific goal
- `selectRandomGroup(classroomId)` - Select random group

### Data Portability
- `exportClassroomToExcel(classroomId, filename)` - Export to Excel
- `importClassroomFromExcel(filename)` - Import from Excel
- `getExcelDirectory()` - Get export directory path

### Reporting
- `generateDailyReportToday(classroomId)` - Report for today
- `generateDailyReport(classroomId, dateStr)` - Report for specific date

## Configuration

Edit `application.yml` to customize:

```yaml
app:
  data:
    excel-dir: ./exports        # Directory for Excel files
    database:
      file-path: ./data/classroom_mcp_db  # Database location
```

## Database

H2 file-based database stores data in `./data/classroom_mcp_db`

### Access H2 Console

Visit `http://localhost:8080/h2-console` with:
- **URL**: `jdbc:h2:file:./data/classroom_mcp_db`
- **User**: `sa`
- **Password**: (empty)

## Entity Relationships

- **Classroom** → many **Students**, **Groups**, **Tasks**, **Goals**
- **Student** → many **Groups**, **Badges**, **Goals**
- **Goal** → many **Students**, tracks **GoalProgress** per student
- **SessionLog** → one **Student**, records daily actions and practices

## Key Services

### RandomSelectionService
Implements balanced picking algorithm:
1. Reads today's `SessionLog` entries
2. Counts how many times each student was selected
3. Prioritizes students not yet selected today
4. Falls back to all students if everyone selected

### ReportingService
Generates date-scoped summaries:
- Points awarded/deducted
- Practices completed
- Rank changes
- Badges awarded
- Per-student breakdown

## MCP Stdio Transport

This server is designed to run as a child process with stdio communication, enabling seamless integration with Claude Desktop or other MCP clients.

## License

MIT License

## Support

For issues and feature requests, please use the GitHub repository.
