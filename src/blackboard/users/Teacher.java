package blackboard.users;

import blackboard.courses.Course;
import blackboard.courses.Assignment;
import blackboard.courses.Announcement;

import java.util.Collections;
import java.util.List;

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
        // TODO(feature/persistence-stub): persist teacher record to storage
    }

    @Override
    public void load() throws Exception {
        // TODO(feature/persistence-stub): load teacher record from storage
    }

    /**
     * View courses taught by this teacher.
     */
    public List<Course> viewCourses() {
        // TODO(feature/enrollment-rules): return courses assigned to this teacher
        return Collections.emptyList();
    }

    /**
     * Record a grade for a student's submission on an assignment.
     */
    public void recordGrade(Assignment a, Student student, double score) {
        // TODO(feature/grading): locate student's submission for assignment and set score/feedback/status
    }

    /**
     * Post an announcement to a course.
     */
    public Announcement postAnnouncement(Course course, String text) {
        // TODO(feature/announcements): create and attach announcement to the course
        return null;
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
