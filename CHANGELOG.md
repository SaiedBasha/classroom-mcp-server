# Changelog

## Version 1.0.0 (Initial Release)

### Core Features
- ✅ Classroom management (create, retrieve, list)
- ✅ Student management (add, remove, update nicknames)
- ✅ Gamification system
  - Award/deduct points with reasons
  - Dynamic rank level adjustments (Bronze → Silver → Gold → Platinum)
  - Badge system with creation and assignment
  - Leaderboards (by points and by rank)
- ✅ Student groups
  - Create groups within classrooms
  - Add/remove students from groups
  - Manage group memberships
- ✅ Task management
  - Create tasks/to-do items
  - Mark complete/incomplete
  - List with filters (all, completed, incomplete)
- ✅ Skill-based goals
  - Define learning goals with practice targets
  - Associate students with goals
  - Track individual progress
  - Auto-completion detection
- ✅ Balanced random selection algorithm
  - Fair picking ensuring equal daily opportunity
  - Daily session log tracking
  - Goal-specific student selection
  - Group selection
- ✅ Excel data portability
  - Export classroom data to XLSX
  - Import from XLSX
  - Multi-sheet workbooks
- ✅ Daily reporting
  - Activity summaries by date
  - Points distribution analysis
  - Practice completion tracking
  - Rank changes and badge awards
  - Per-student breakdowns
- ✅ MCP Tool Registry
  - 50+ tools for complete classroom management
  - Spring AI @Tool annotation support
  - Stdio transport ready
  - Claude Desktop compatible

### Technical
- Java 21 with modern features
- Spring Boot 3.4.0
- Spring AI 1.0.0 (MCP support)
- H2 Database (file-based, zero-config)
- Apache POI 5.2.4 (Excel)
- JPA/Hibernate ORM
- Lombok for boilerplate reduction
- Transactional consistency
- Custom indexes on frequently queried columns

### Infrastructure
- Maven build with assembly plugin
- Bash build script (build-and-zip.sh)
- Distributable ZIP packaging
- H2 Console access for debugging
- Configuration via YAML

### Documentation
- README.md with architecture overview
- SETUP_AND_USAGE.md with step-by-step guide
- Inline code documentation
- Entity relationship diagrams (in README)
- SQL query examples
- MCP integration guide

### Database Schema
- `classrooms` - Classroom definitions
- `students` - Student records with credit points
- `rank_levels` - Rank definitions (Bronze, Silver, Gold, Platinum)
- `student_groups` - Group definitions
- `group_students` - Student-to-group mappings
- `badges` - Badge definitions
- `student_badges` - Student-to-badge mappings
- `tasks` - To-do items
- `goals` - Learning goals
- `goal_students` - Student-to-goal mappings
- `goal_progress` - Per-student goal tracking
- `session_logs` - Daily activity audit trail

### Known Limitations
- Single H2 database instance (no multi-tenancy)
- In-memory backups not included
- Excel import has basic validation
- No authentication/authorization in this version

### Future Roadmap
- [ ] REST API layer (HTTP endpoints)
- [ ] WebSocket support for real-time updates
- [ ] Multi-database support (PostgreSQL, MySQL)
- [ ] User authentication and roles
- [ ] Custom rank levels per classroom
- [ ] Advanced analytics and charts
- [ ] Bulk student import via CSV
- [ ] Student messaging/notifications
- [ ] Parent/guardian portal integration

---

## Development Notes

### Service Layer Architecture
Each service handles a specific domain:
- `ClassroomService`: Core CRUD operations
- `GamificationService`: Points, badges, ranks
- `RandomSelectionService`: Fair student picking
- `GroupService`: Group management
- `TaskService`: Task/to-do management
- `GoalService`: Learning goals and progress
- `ExcelService`: Data import/export
- `ReportingService`: Daily activity summaries

### Session Log Strategy
Every action is logged to `SessionLog` with:
- Student reference
- Calendar date for daily grouping
- Timestamp for exact time tracking
- Action type (POINTS_AWARDED, SELECTED_FOR_TASK, etc.)
- Optional numeric change
- Description for context

This enables:
- Daily report generation
- Balanced random selection
- Audit trail of all changes
- Analytics and analytics queries

### Random Selection Algorithm
```
For each selection:
1. Get all students in classroom/goal
2. Query SessionLog for today's entries
3. Count selections per student
4. Prefer students with lowest count
5. If all selected, select from all
6. Log the selection
7. Return selected student
```

Result: Fair, transparent picking with no student left out on same day.
