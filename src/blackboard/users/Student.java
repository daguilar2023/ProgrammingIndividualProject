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
                ", studentId='" + studentId + '\'' +
                '}';
    }
}
