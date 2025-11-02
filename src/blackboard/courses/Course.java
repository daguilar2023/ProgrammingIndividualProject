package blackboard.courses;

import blackboard.users.Teacher;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections;

public class Course {
    private String id;
    private String title;
    private int maxCapacity;
    private Set<Course> prerequisites;
    private Teacher teacher;
    private List<Assignment> assignments;
    private List<Announcement> announcements;

    public Course(String id, String title, int maxCapacity, Teacher teacher) {
        this.id = id;
        this.title = title;
        this.maxCapacity = maxCapacity;
        this.teacher = teacher;
        this.prerequisites = new HashSet<>();
        this.assignments = new ArrayList<>();
        this.announcements = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public Set<Course> getPrerequisites() {
        return prerequisites;
    }

    public List<Assignment> getAssignments() {
        return Collections.unmodifiableList(assignments);
    }

    public List<Announcement> getAnnouncements() {
        return Collections.unmodifiableList(announcements);
    }

    /**
     * Check if course still has available capacity for enrollment.
     */
    public boolean hasCapacity() {
        // TODO(feature/enrollment-rules): implement capacity tracking and enrollment count
        return true;
    }

    /**
     * Add a prerequisite course.
     */
    public void addPrerequisite(Course c) {
        // TODO(feature/enrollment-rules): validate and add prerequisite
        prerequisites.add(c);
    }

    public void addAssignment(Assignment a) {
        // TODO(feature/submission-flow): validate and attach assignment to this course
        assignments.add(a);
    }

    public void addAnnouncement(Announcement a) {
        // TODO(feature/announcements): validate and attach announcement to this course
        announcements.add(a);
    }

    @Override
    public String toString() {
        return "Course{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", maxCapacity=" + maxCapacity +
                ", teacher=" + (teacher != null ? teacher.getName() : "None") +
                ", prerequisites=" + prerequisites.size() +
                ", assignments=" + assignments.size() +
                ", announcements=" + announcements.size() +
                '}';
    }
}
