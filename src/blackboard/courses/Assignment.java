package blackboard.courses;

import blackboard.users.Student;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Assignment belongs to a Course and owns its Submissions (composition).
 */
public class Assignment {
    private int id;
    private String title;
    private LocalDate dueDate;
    private Course course;        // back-reference to owning course
    private double weight;        // percentage weight (0..1 or 0..100 depending on your choice)

    private final List<Submission> submissions = new ArrayList<>();

    public Assignment(int id, String title, LocalDate dueDate, Course course, double weight) {
        this.id = id;
        this.title = title;
        this.dueDate = dueDate;
        this.course = course;
        this.weight = weight;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public LocalDate getDueDate() { return dueDate; }
    public Course getCourse() { return course; }
    public double getWeight() { return weight; }

    /**
     * Read-only view of all submissions for this assignment.
     */
    public List<Submission> submissions() {
        return Collections.unmodifiableList(submissions);
    }

    /**
     * Create and register a submission for this assignment by a student.
     * Note: current Submission constructor takes (assignment, student, submittedAt).
     * The `content` parameter is accepted for future use; it's not stored yet
     * because `Submission` does not currently keep content.
     */
    public Submission submit(Student student, String content) {
        Submission submission = new Submission(this, student, LocalDateTime.now());
        if (submission.isLate()) {
            submission.setStatus(Submission.SubmissionStatus.LATE);
        }
        submissions.add(submission);
        return submission;
    }

    public Submission getSubmission(Student student) {
        for (Submission sub : submissions) {
            if (sub.getStudent().equals(student)) {
                return sub;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Assignment{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", dueDate=" + dueDate +
                ", course=" + (course != null ? course.getTitle() : "null") +
                ", weight=" + weight +
                ", submissions=" + submissions.size() +
                '}';
    }
}
