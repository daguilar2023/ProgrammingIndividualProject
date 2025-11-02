package blackboard.courses;

import blackboard.users.Student;

import java.time.LocalDateTime;

public class Submission {
    public enum SubmissionStatus {
        PENDING,
        SUBMITTED,
        LATE,
        GRADED
    }

    private Assignment assignment;
    private Student student;
    private LocalDateTime submittedAt;
    private Double score;           // nullable until graded
    private String feedback;        // optional teacher feedback
    private SubmissionStatus status;

    public Submission(Assignment assignment, Student student, LocalDateTime submittedAt) {
        this.assignment = assignment;
        this.student = student;
        this.submittedAt = submittedAt;
        this.status = SubmissionStatus.SUBMITTED; // created on submit flow
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public Student getStudent() {
        return student;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public Double getScore() {
        return score;
    }

    public String getFeedback() {
        return feedback;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setScore(double score) {
        // TODO(feature/grading): validations and weight handling happen outside
        this.score = score;
        this.status = SubmissionStatus.GRADED;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }

    /**
     * Determine if the submission was made after the assignment due date.
     */
    public boolean isLate() {
        // TODO(feature/submission-flow): consider grace periods or time-of-day rules if needed
        if (assignment == null || assignment.getDueDate() == null || submittedAt == null) return false;
        return submittedAt.toLocalDate().isAfter(assignment.getDueDate());
    }

    @Override
    public String toString() {
        return "Submission{" +
                "assignment=" + (assignment != null ? assignment.getId() : "null") +
                ", student=" + (student != null ? student.getUsername() : "null") +
                ", submittedAt=" + submittedAt +
                ", score=" + score +
                ", status=" + status +
                '}';
    }
}
