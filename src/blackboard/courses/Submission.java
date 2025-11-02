package blackboard.courses;

import blackboard.users.Student;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A Submission is owned by an Assignment (composition).
 */
public class Submission {
    public enum SubmissionStatus { PENDING, LATE, GRADED }

    private final Assignment assignment;
    private final Student student;
    private final LocalDateTime submittedAt;

    private Double score;           // optional, set when graded
    private String feedback;        // optional teacher feedback
    private SubmissionStatus status;

    public Submission(Assignment assignment, Student student, LocalDateTime submittedAt) {
        this.assignment = assignment;
        this.student = student;
        this.submittedAt = submittedAt;
        this.status = SubmissionStatus.PENDING;
    }

    public Assignment getAssignment() { return assignment; }
    public Student getStudent() { return student; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }

    public Double getScore() { return score; }

    public boolean isGraded() {
        return status == SubmissionStatus.GRADED;
    }

    public void setScore(Double score) { this.score = score; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public SubmissionStatus getStatus() { return status; }
    public void setStatus(SubmissionStatus status) { this.status = status; }

    /**
     * A submission is late if its submit date is after the assignment's due date.
     */
    public boolean isLate() {
        LocalDate due = assignment.getDueDate();
        return submittedAt.toLocalDate().isAfter(due);
    }
}
