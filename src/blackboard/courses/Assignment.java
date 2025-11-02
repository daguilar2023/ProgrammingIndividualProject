
package blackboard.courses;

import blackboard.users.Student;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Assignment {
    private String id;          // UML: id: String
    private String title;       // UML: title: String
    private LocalDate dueDate;  // UML: dueDate: LocalDate
    private Course course;      // UML: course: Course
    private double weight;      // UML: weight: double

    // Composition: an assignment owns its submissions
    private final List<Submission> submissions = new ArrayList<>();

    public Assignment(String id, String title, LocalDate dueDate, Course course, double weight) {
        this.id = id;
        this.title = title;
        this.dueDate = dueDate;
        this.course = course;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Course getCourse() {
        return course;
    }

    public double getWeight() {
        return weight;
    }

    /**
     * Returns an unmodifiable view of the submissions for this assignment.
     */
    public List<Submission> submissions() {
        return Collections.unmodifiableList(submissions);
    }

    /**
     * Create a submission for a student. The concrete Submission structure will
     * be filled in during the submission-flow feature.
     */
    public Submission submit(Student student, String content) {
        // TODO(feature/submission-flow): create a new Submission(student, this, content, now)
        // and add it to `submissions`, then return it.
        return null;
    }

    @Override
    public String toString() {
        return "Assignment{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", dueDate=" + dueDate +
                ", course=" + (course != null ? course.getTitle() : "Unknown") +
                ", weight=" + weight +
                ", submissions=" + submissions.size() +
                '}';
    }
}
