package blackboard.users;

import blackboard.courses.Course;
import blackboard.util.AppState;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;

public class Admin extends User {

    // Base-models: fix role to ADMIN
    public Admin(int id, String name, String username, String password) {
        super(id, name, username, password, UserRole.ADMIN);
    }

    @Override
    public void save() throws Exception {
        Path dir = Paths.get("data", "admins");
        Files.createDirectories(dir);
        Path file = dir.resolve(getId() + ".csv");
        String line = String.join(",",
                String.valueOf(getId()),
                getName() == null ? "" : getName(),
                getUsername() == null ? "" : getUsername(),
                getPassword() == null ? "" : getPassword()
        );
        Files.writeString(file, line + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Override
    public void load() throws Exception {
        Path file = Paths.get("data", "admins", getId() + ".csv");
        if (!Files.exists(file)) {
            System.out.println("No persisted record for admin id=" + getId());
            return;
        }
        String data = Files.readString(file, StandardCharsets.UTF_8).trim();
        if (data.isEmpty()) return;
        String[] parts = data.split(",", -1);
        if (parts.length >= 4) {
            boolean ok = String.valueOf(getId()).equals(parts[0])
                    && (getName() == null ? "" : getName()).equals(parts[1])
                    && (getUsername() == null ? "" : getUsername()).equals(parts[2])
                    && (getPassword() == null ? "" : getPassword()).equals(parts[3]);
            if (!ok) {
                System.out.println("Warning: persisted Admin differs from in-memory instance (id=" + getId() + ")");
            }
        }
    }

    public void createTeacher(Teacher t) {
        if (t == null) return;
        AppState.teachers.add(t);
    }


    public void createStudent(Student s) {
        if (s == null) return;
        AppState.students.add(s);
    }

    public void createCourse(Course c) {
        if (c == null) return;
        AppState.courses.add(c);
    }

    public void enrollStudent(Course c, Student s) {
        // Delegate validation to Course.enroll(s)
        if (c.enroll(s) == null) {
            System.out.println("Enrollment failed for " + s.getName() + " in " + c.getTitle());
        } else {
            System.out.println("Enrollment successful: " + s.getName() + " -> " + c.getTitle());
        }
    }

    public void assignTeacher(Course course, Teacher t) {
        if (course == null || t == null) {
            System.out.println("Invalid course or teacher.");
            return;
        }

        try {
            java.lang.reflect.Field teacherField = course.getClass().getDeclaredField("teacher");
            teacherField.setAccessible(true);
            teacherField.set(course, t);
            System.out.println("Teacher " + t.getName() + " assigned to course " + course.getTitle());
        } catch (NoSuchFieldException e) {
            System.out.println("Error: Course class does not have a 'teacher' field.");
        } catch (IllegalAccessException e) {
            System.out.println("Error assigning teacher to course: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "Admin{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", username='" + getUsername() + '\'' +
                '}';
    }
}
