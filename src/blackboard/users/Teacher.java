package blackboard.users;

import blackboard.courses.Assignment;
import blackboard.courses.Course;
import blackboard.util.CsvPersistable;

import java.nio.file.*;

public class Teacher extends User implements CsvPersistable {
    public Teacher(int id, String name, String username, String password) {
        super(id, name, username, password, UserRole.TEACHER);
    }

    @Override public void save() throws Exception {
        Path dir = Paths.get("data","teachers"); Files.createDirectories(dir);
        Path file = dir.resolve(getId()+".csv");
        String line = String.join(",",
                String.valueOf(getId()), getName(), getUsername(), getPassword());
        Files.writeString(file, line+System.lineSeparator());
    }
    @Override public void load() throws Exception { }

    // Features per spec: create assignment + grade
    public void createAssignment(Course c, String assignmentId, String title) {
        if (c==null || assignmentId==null || title==null) return;
        c.addAssignment(new Assignment(assignmentId, title));
        try { c.saveAssignments(); } catch (Exception ignore) {}
    }

    public void recordGrade(Course c, String assignmentId, int studentId, int grade) {
        if (c==null || assignmentId==null) return;
        c.setGrade(assignmentId, studentId, grade);
        try { c.saveGrades(); } catch (Exception ignore) {}
    }
}