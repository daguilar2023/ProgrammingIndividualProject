package blackboard.users;

import blackboard.courses.Course;
import blackboard.courses.Assignment;
import blackboard.courses.Announcement;
import blackboard.courses.Submission;
import blackboard.courses.Enrollment;
import blackboard.util.AppState;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Teacher extends User {
    private final String employeeId;

    // Base-models: fix role to TEACHER
    public Teacher(int id, String name, String username, String password, String employeeId) {
        super(id, name, username, password, UserRole.TEACHER);
        this.employeeId = employeeId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    @Override
    public void save() throws Exception {
        Path dir = Paths.get("data", "teachers");
        Files.createDirectories(dir);
        Path file = dir.resolve(getId() + ".csv");
        String line = String.join(",",
                String.valueOf(getId()),
                getName() == null ? "" : getName(),
                getUsername() == null ? "" : getUsername(),
                getPassword() == null ? "" : getPassword(),
                employeeId == null ? "" : employeeId
        );
        Files.writeString(file, line + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Override
    public void load() throws Exception {
        Path file = Paths.get("data", "teachers", getId() + ".csv");
        if (!Files.exists(file)) {
            System.out.println("No persisted record for teacher id=" + getId());
            return;
        }
        String data = Files.readString(file).trim();
        if (data.isEmpty()) return;
        String[] parts = data.split(",", -1);
        if (parts.length >= 5) {
            // Compare persisted data with current instance; warn if mismatched
            boolean ok = String.valueOf(getId()).equals(parts[0])
                    && (getName() == null ? "" : getName()).equals(parts[1])
                    && (getUsername() == null ? "" : getUsername()).equals(parts[2])
                    && (getPassword() == null ? "" : getPassword()).equals(parts[3])
                    && (employeeId == null ? "" : employeeId).equals(parts[4]);
            if (!ok) {
                System.out.println("Warning: persisted Teacher differs from in-memory instance (id=" + getId() + ")");
            }
        }
    }

    /**
     * View courses taught by this teacher by filtering the global AppState courses list.
     */
    public List<Course> viewCourses() {
        List<Course> mine = new ArrayList<>();
        for (Course c : AppState.getCourses()) {
            if (c != null && c.getTeacher() == this) {
                mine.add(c);
            }
        }
        return Collections.unmodifiableList(mine);
    }

    /**
     * Record a grade for a student's submission on an assignment.
     */
    public void recordGrade(Assignment a, Student student, double score) {
        if (a == null || student == null) return;

        Submission s = a.getSubmission(student);
        if (s == null) return; // no submission to grade

        s.setScore(score);
        s.setStatus(Submission.SubmissionStatus.GRADED);

        // Recompute the student's final grade for this course, if enrolled
        Course c = a.getCourse();
        if (c != null) {
            for (Enrollment e : c.getEnrollments()) {
                if (e.getStudent() != null && e.getStudent().getId() == student.getId()) {
                    e.computeFinal();
                    break;
                }
            }
        }
    }

    /**
     * Post an announcement to a course.
     */
    public Announcement postAnnouncement(Course course, String text) {
        if (course == null || text == null) return null;
        int nextId = course.getAnnouncements().size() + 1; // simple local id
        Announcement ann = new Announcement(nextId, course, text, LocalDateTime.now());
        course.addAnnouncement(ann);
        return ann;
    }

    @Override
    public String toString() {
        return "Teacher{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", employeeId='" + employeeId + '\'' +
                '}';
    }
}
