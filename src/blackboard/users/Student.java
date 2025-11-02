package blackboard.users;

import blackboard.courses.Course;
import blackboard.courses.Assignment;
import blackboard.courses.Enrollment;
import blackboard.courses.Submission;
import blackboard.util.AppState;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Student extends User {

    // Base-models: Student should fix its role to STUDENT
    public Student(int id, String name, String username, String password) {
        super(id, name, username, password, UserRole.STUDENT);
    }


    @Override
    public void save() throws Exception {
        Path dir = Paths.get("data", "students");
        Files.createDirectories(dir);
        Path file = dir.resolve(getId() + ".csv");
        String line = String.join(",",
                String.valueOf(getId()),
                getName() == null ? "" : getName(),
                getUsername() == null ? "" : getUsername(),
                getPassword() == null ? "" : getPassword()
        );
        Files.writeString(file, line + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Override
    public void load() throws Exception {
        Path file = Paths.get("data", "students", getId() + ".csv");
        if (!Files.exists(file)) {
            System.out.println("No persisted record for student id=" + getId());
            return;
        }
        String data = Files.readString(file).trim();
        if (data.isEmpty()) return;
        String[] parts = data.split(",", -1);
        if (parts.length >= 4) {
            boolean ok = String.valueOf(getId()).equals(parts[0])
                    && (getName() == null ? "" : getName()).equals(parts[1])
                    && (getUsername() == null ? "" : getUsername()).equals(parts[2])
                    && (getPassword() == null ? "" : getPassword()).equals(parts[3]);
            if (!ok) {
                System.out.println("Warning: persisted Student differs from in-memory instance (id=" + getId() + ")");
            }
        }
    }

    /**
     * View the list of courses the student is enrolled in.
     */
    public List<Course> viewEnrollments() {
        List<Course> enrolledCourses = new ArrayList<>();
        for (Course c : AppState.getCourses()) {
            for (Enrollment e : c.getEnrollments()) {
                if (e.getStudent() == this) {
                    enrolledCourses.add(c);
                    break;
                }
            }
        }
        return Collections.unmodifiableList(enrolledCourses);
    }

    /**
     * View grades for assignments in a given course.
     */
    public Map<Assignment, Double> viewGrades(Course course) {
        Map<Assignment, Double> grades = new HashMap<>();
        if (course == null) return grades;

        for (Assignment a : course.getAssignments()) {
            Submission s = a.getSubmission(this);
            if (s != null && s.getScore() != null) {
                grades.put(a, s.getScore());
            }
        }
        return Collections.unmodifiableMap(grades);
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", username='" + getUsername() + '\'' +
                '}';
    }
}
