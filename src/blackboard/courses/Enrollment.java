package blackboard.courses;

import blackboard.users.Student;
import java.time.LocalDateTime;
import java.util.List;

public class Enrollment {
    private Student student;
    private Course course;
    private LocalDateTime enrolledAt;
    private double finalGrade;

    public Enrollment(Student student, Course course, LocalDateTime enrolledAt) {
        this.student = student;
        this.course = course;
        this.enrolledAt = enrolledAt;
        this.finalGrade = 0.0; // default grade
    }

    public Student getStudent() {
        return student;
    }

    public Course getCourse() {
        return course;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public double getFinalGrade() {
        return finalGrade;
    }

    public void setFinalGrade(double finalGrade) {
        this.finalGrade = finalGrade;
    }

    /**
     * Compute the student's final grade for this course.
     * @return the computed final grade.
     */
    public double computeFinal() {
        List<Assignment> assignments = course.getAssignments();
        double totalScore = 0.0;
        int gradedCount = 0;

        for (Assignment assignment : assignments) {
            Submission submission = assignment.getSubmission(student);
            if (submission != null && submission.isGraded()) {
                totalScore += submission.getScore();
                gradedCount++;
            }
        }

        if (gradedCount > 0) {
            finalGrade = totalScore / gradedCount;
        } else {
            finalGrade = 0.0;
        }

        return finalGrade;
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "student=" + (student != null ? student.getName() : "Unknown") +
                ", course=" + (course != null ? course.getTitle() : "Unknown") +
                ", enrolledAt=" + enrolledAt +
                ", finalGrade=" + finalGrade +
                '}';
    }
}
