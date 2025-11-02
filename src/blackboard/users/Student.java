package blackboard.users;

import blackboard.courses.Course;
import blackboard.courses.Assignment;

import java.util.List;
import java.util.Map;
import java.util.Collections;

public class Student extends User {
    private final String studentId;

    // Base-models: Student should fix its role to STUDENT
    public Student(int id, String name, String username, String password, String studentId) {
        super(id, name, username, password, UserRole.STUDENT);
        this.studentId = studentId;
    }

    public String getStudentId() {
        return studentId;
    }

    @Override
    public void save() throws Exception {
        // TODO(feature/persistence-stub): persist student record to storage
    }

    @Override
    public void load() throws Exception {
        // TODO(feature/persistence-stub): load student record from storage
    }

    /**
     * View the list of courses the student is enrolled in.
     */
    public List<Course> viewEnrollments() {
        // TODO(feature/enrollment-rules): fetch enrollments for this student and map to courses
        return Collections.emptyList();
    }

    /**
     * View grades for assignments in a given course.
     */
    public Map<Assignment, Double> viewGrades(Course course) {
        // TODO(feature/grading): compute or aggregate assignment grades for this student in the given course
        return Collections.emptyMap();
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", studentId='" + studentId + '\'' +
                '}';
    }
}
