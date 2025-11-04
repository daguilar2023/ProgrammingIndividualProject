package blackboard.users;

import blackboard.util.AppState;
import blackboard.courses.Course;
import blackboard.util.CsvPersistable;

import java.nio.file.*;

public class Admin extends User implements CsvPersistable {
    public Admin(int id, String name, String username, String password) {
        super(id, name, username, password, UserRole.ADMIN);
    }

    @Override public void save() throws Exception {
        Path dir = Paths.get("data","admins"); Files.createDirectories(dir);
        Path file = dir.resolve(getId()+".csv");
        String line = String.join(",",
                String.valueOf(getId()), getName(), getUsername(), getPassword());
        Files.writeString(file, line+System.lineSeparator());
    }
    @Override public void load() throws Exception { /* optional sanity; not needed */ }

    // Simple CRUD (persist-on-create)
    public void createTeacher(Teacher t) {
        if (t == null) return;
        if (AppState.teacherIdExists(t.getId())) {
            System.out.println("❌ Teacher id already exists: " + t.getId());
            return;
        }
        AppState.teachers.add(t);
        try { t.save(); } catch (Exception e) { System.out.println("Save teacher failed: " + e.getMessage()); }
    }
    public void createStudent(Student s) {
        if (s == null) return;
        if (AppState.studentIdExists(s.getId())) {
            System.out.println("❌ Student id already exists: " + s.getId());
            return;
        }
        AppState.students.add(s);
        try { s.save(); } catch (Exception e) { System.out.println("Save student failed: " + e.getMessage()); }
    }
    public void createCourse(blackboard.courses.Course c) {
        if (c == null) return;
        if (AppState.courseIdExists(c.getId())) {
            System.out.println("❌ Course id already exists: " + c.getId());
            return;
        }
        AppState.courses.add(c);
        try { c.save(); } catch (Exception e) { System.out.println("Save course failed: " + e.getMessage()); }
    }

    public void assignTeacher(Course c, Teacher t) {
        if (c==null || t==null) { System.out.println("Invalid course/teacher"); return; }
        c.setTeacher(t);
        try { c.save(); } catch (Exception ignore) {}
    }

    public void enrollStudent(Course c, Student s) {
        if (c==null || s==null) { System.out.println("Invalid course/student"); return; }
        c.enroll(s);
        try { c.save(); } catch (Exception ignore) {}
    }
}