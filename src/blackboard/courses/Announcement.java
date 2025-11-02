package blackboard.courses;

import java.time.LocalDateTime;

public class Announcement {
    private int id;
    private Course course;
    private String text;
    private LocalDateTime postedAt;

    public Announcement(int id, Course course, String text, LocalDateTime postedAt) {
        this.id = id;
        this.course = course;
        this.text = text;
        this.postedAt = postedAt;
    }

    public int getId() {
        return id;
    }

    public Course getCourse() {
        return course;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getPostedAt() {
        return postedAt;
    }

    /**
     * Allows a teacher to edit the announcement text.
     * @param text the updated announcement content
     */
    public void edit(String text) {
        // TODO(feature/announcements): allow teacher to modify announcement text with permission checks
        this.text = text;
    }

    @Override
    public String toString() {
        return "Announcement{" +
                "id=" + id +
                ", course=" + (course != null ? course.getTitle() : "Unknown") +
                ", text='" + text + '\'' +
                ", postedAt=" + postedAt +
                '}';
    }
}
