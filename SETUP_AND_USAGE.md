# Classroom MCP Server - Setup & Usage Guide

## Quick Start

### 1. Prerequisites
- Java 21 or higher
- Maven 3.8+
- Git

### 2. Clone and Build

```bash
git clone https://github.com/SaiedBasha/classroom-mcp-server.git
cd classroom-mcp-server

# Make build script executable
chmod +x build-and-zip.sh

# Build and package
./build-and-zip.sh
```

### 3. Run the Server

```bash
java -jar target/classroom-mcp-server-1.0.0.jar
```

Server starts on `http://localhost:8080`

## Configuration

### Application Settings (application.yml)

```yaml
spring:
  application:
    name: classroom-mcp-server
  datasource:
    url: jdbc:h2:file:./data/classroom_mcp_db;MODE=MySQL
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update

app:
  data:
    excel-dir: ./exports
    database:
      file-path: ./data/classroom_mcp_db

server:
  port: 8080
```

### Environment Variables

Copy `.env.example` to `.env` and customize as needed:

```bash
cp .env.example .env
```

## Usage Examples

### 1. Create a Classroom

```
Tool: createClassroom
Parameters:
  - name: "Grade 5A"
  - description: "Primary school class"

Result: Classroom 'Grade 5A' created successfully (ID: 1)
```

### 2. Add Students

```
Tool: addStudent
Parameters:
  - classroomId: 1
  - name: "John Smith"
  - nickname: "john"

Result: Student 'John Smith' ("john") added successfully (ID: 1)
```

Repeat for multiple students with different nicknames.

### 3. List Students

```
Tool: listStudents
Parameters:
  - classroomId: 1

Result:
Students in classroom:
- john (John Smith): 0 points, Rank: Bronze
- sarah (Sarah Johnson): 0 points, Rank: Bronze
- mike (Mike Davis): 0 points, Rank: Bronze
```

### 4. Award Points & Gamification

```
Tool: addPoints
Parameters:
  - studentId: 1
  - points: 25
  - reason: "Excellent participation"

Result: Added 25 points to john. New total: 25
```

### 5. Create a Group

```
Tool: createGroup
Parameters:
  - classroomId: 1
  - groupName: "Team A"
  - description: "Science project group"

Result: Group 'Team A' created successfully (ID: 1)
```

### 6. Add Students to Group

```
Tool: addStudentToGroupByNickname
Parameters:
  - groupId: 1
  - nickname: "john"

Result: Student 'john' added to group successfully
```

### 7. Create a Skill-Based Goal

```
Tool: createGoal
Parameters:
  - classroomId: 1
  - name: "Math Multiplication"
  - description: "Master multiplication tables"
  - targetPracticeCount: 10

Result: Goal 'Math Multiplication' created successfully (ID: 1, Target: 10 practices)
```

### 8. Associate Students with Goal

```
Tool: associateStudentWithGoal
Parameters:
  - goalId: 1
  - studentId: 1

Result: Student associated with goal successfully
```

### 9. Random Selection (Fair Picking)

```
Tool: selectRandomStudent
Parameters:
  - classroomId: 1

Result: Randomly selected: john (Name: John Smith)
```

Calling multiple times today will balance across all students before repeating.

### 10. Select for Goal Practice

```
Tool: selectRandomStudentForGoal
Parameters:
  - goalId: 1

Result: Randomly selected for goal: john
```

This automatically increments the student's practice count for the goal.

### 11. Check Goal Progress

```
Tool: getGoalProgress
Parameters:
  - goalId: 1
  - studentId: 1

Result: Goal Progress for john: 1 / 10 practices completed (Completed: false)
```

### 12. View Leaderboard

```
Tool: getLeaderboardByPoints
Parameters:
  - classroomId: 1

Result:
Leaderboard (by Points):
1. mike - 75 points
2. sarah - 50 points
3. john - 25 points
```

### 13. Create a Badge

```
Tool: createBadge
Parameters:
  - name: "Star Performer"
  - description: "Awarded for excellent participation"
  - icon: "⭐"

Result: Badge 'Star Performer' created successfully (ID: 1)
```

### 14. Award Badge

```
Tool: awardBadge
Parameters:
  - studentId: 1
  - badgeId: 1

Result: Badge awarded successfully
```

### 15. Create Task (To-Do)

```
Tool: createTask
Parameters:
  - classroomId: 1
  - title: "Prepare Science Assignment"
  - description: "Chapter 5-7 homework"

Result: Task 'Prepare Science Assignment' created successfully (ID: 1)
```

### 16. Mark Task Complete

```
Tool: markTaskAsCompleted
Parameters:
  - taskId: 1

Result: Task 'Prepare Science Assignment' marked as completed
```

### 17. Generate Daily Report

```
Tool: generateDailyReportToday
Parameters:
  - classroomId: 1

Result:
=== Daily Report for 2026-09-03 ===
Classroom: Grade 5A
Points Gained: 75
Points Lost: 0
Practices Completed: 3
Rank Changes:
  - john: New Rank: Silver
```

### 18. Export to Excel

```
Tool: exportClassroomToExcel
Parameters:
  - classroomId: 1
  - filename: "grade_5a_export.xlsx"

Result: Classroom data exported successfully to: /path/to/exports/grade_5a_export.xlsx
```

### 19. Get Excel Directory

```
Tool: getExcelDirectory

Result: Excel files directory: /path/to/exports
```

## Database Access

### H2 Console

Access the database directly:

1. Navigate to `http://localhost:8080/h2-console`
2. Use these credentials:
   - **URL**: `jdbc:h2:file:./data/classroom_mcp_db;MODE=MySQL`
   - **User**: `sa`
   - **Password**: (leave empty)
3. Click "Connect"

### Sample SQL Queries

```sql
-- List all students with points
SELECT n.nickname, s.credit_points, rl.name as rank
FROM students s
JOIN rank_levels rl ON s.rank_level_id = rl.id
ORDER BY s.credit_points DESC;

-- View today's activities
SELECT s.nickname, sl.action_type, sl.points_changed, sl.description
FROM session_logs sl
JOIN students s ON sl.student_id = s.id
WHERE sl.log_date = CURRENT_DATE
ORDER BY sl.timestamp DESC;

-- Goal completion status
SELECT g.name, s.nickname, gp.current_practice_count, g.target_practice_count, gp.completed
FROM goal_progress gp
JOIN goals g ON gp.goal_id = g.id
JOIN students s ON gp.student_id = s.id
ORDER BY g.name, gp.completed;
```

## Integration with Claude Desktop

To integrate with Claude Desktop MCP client:

1. Create/edit `~/Library/Application Support/Claude/claude_desktop_config.json` (Mac) or equivalent Windows path

2. Add this server configuration:

```json
{
  "mcpServers": {
    "classroom": {
      "command": "java",
      "args": ["-jar", "/path/to/classroom-mcp-server-1.0.0.jar"]
    }
  }
}
```

3. Restart Claude Desktop - the Classroom tools will now be available

## Troubleshooting

### Database Lock Issues

If you get "Database is locked" errors:

```bash
# Delete the lock files
rm ./data/classroom_mcp_db.lock.db
rm ./data/classroom_mcp_db.trace.db

# Restart the server
java -jar target/classroom-mcp-server-1.0.0.jar
```

### Port Already in Use

Change the port in `application.yml`:

```yaml
server:
  port: 8081  # Use different port
```

### Build Failures

```bash
# Clean and rebuild
mvn clean install -DskipTests=true

# Check Java version
java -version  # Must be 21+

# Check Maven version
mvn -version   # Must be 3.8+
```

## Development

### Project Structure

```
src/main/java/com/classroom/
├── entity/           # JPA entities
├── repository/       # Spring Data repositories
├── service/          # Business logic
├── mcp/              # MCP tool registry
├── config/           # Spring configuration
└── ClassroomMcpServerApplication.java

src/main/resources/
└── application.yml   # Configuration

pom.xml              # Maven build configuration
```

### Building Without Tests

```bash
mvn clean package -DskipTests=true
```

### Running Tests

```bash
mvn test
```

### IDE Setup

**IntelliJ IDEA**:
1. Open `pom.xml` as project
2. Mark `src/main/java` as Sources Root
3. Mark `src/main/resources` as Resources Root
4. Enable annotation processing for Lombok

**VS Code**:
1. Install Extension Pack for Java
2. Open project folder
3. Maven extension will auto-import

## Performance Tips

1. **Batch Operations**: Use `addPointsToStudents()` for multiple students instead of looping
2. **Lazy Loading**: Relations use `FetchType.LAZY` - use joins only when needed
3. **Indexing**: `SessionLog` has indexes on `student_id` and `log_date` for fast queries
4. **Database Maintenance**: Periodically clean up old session logs if not needed

## API Reference

See `ClassroomToolRegistry.java` for complete tool documentation.

All tools return `String` responses suitable for display or further processing.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit changes
4. Push to branch
5. Create Pull Request

## License

MIT License - See LICENSE file

## Support

For issues, feature requests, or questions:
- Open an issue on GitHub
- Check existing documentation
- Review example usage above
