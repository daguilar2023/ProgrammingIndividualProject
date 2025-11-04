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

    public void recordGrade(Course c, String assignmentId, int studentId, int grade) throws Exception {
        if (c == null) { System.out.println("❌ No course."); return; }

        // Enforce: can only grade existing assignments
        if (!c.hasAssignment(assignmentId)) {
            System.out.println("❌ Assignment '" + assignmentId + "' does not exist in " + c.getTitle() + ".");
            return;
        }

        // If you already added 0..100 validation in Main, you can keep this as safety:
        if (grade < 0 || grade > 100) {
            System.out.println("❌ Grade must be between 0 and 100.");
            return;
        }

        // Now record grade using your existing course-grade API
        c.setGrade(assignmentId, studentId, grade); // or whatever your method is named
        c.saveGrades(); // persist (use your existing method name)
    }
}