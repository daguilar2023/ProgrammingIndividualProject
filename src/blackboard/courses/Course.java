package blackboard.courses;

import blackboard.users.Teacher;
import blackboard.users.Student;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.time.LocalDateTime;

public class Course {
    private String id;
    private String title;
    private int maxCapacity;
    private Set<Course> prerequisites;
    private Teacher teacher;
    private List<Assignment> assignments;
    private List<Announcement> announcements;
    private List<Enrollment> enrollments;

    public Course(String id, String title, int maxCapacity, Teacher teacher) {
        this.id = id;
        this.title = title;
        this.maxCapacity = maxCapacity;
        this.teacher = teacher;
        this.prerequisites = new HashSet<>();
        this.assignments = new ArrayList<>();
        this.announcements = new ArrayList<>();
        this.enrollments = new ArrayList<>();
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

    public List<Enrollment> getEnrollments() {
        return Collections.unmodifiableList(enrollments);
    }

    /**
     * Check if course still has available capacity for enrollment.
     */
    public boolean hasCapacity() {
        return enrollments.size() < maxCapacity;
    }

    /**
     * Add a prerequisite course.
     */
    public void addPrerequisite(Course c) {
        if (c == null || c == this) {
            System.out.println("Cannot add null or self as a prerequisite.");
            return;
        }
        if (prerequisites.contains(c)) {
            System.out.println("Course " + c.getTitle() + " is already a prerequisite for " + title);
            return;
        }
        prerequisites.add(c);
    }

    public void addAssignment(Assignment a) {
        if (a == null) {
            System.out.println("Cannot add a null assignment to " + title);
            return;
        }
        if (assignments.contains(a)) {
            System.out.println("Assignment " + a.getTitle() + " already exists in " + title);
            return;
        }
        assignments.add(a);
        System.out.println("Assignment " + a.getTitle() + " added to course " + title);
    }

    public void addAnnouncement(Announcement a) {
        if (a == null) {
            System.out.println("Cannot add a null announcement to " + title);
            return;
        }
        if (announcements.contains(a)) {
            System.out.println("Announcement already exists in " + title);
            return;
        }
        announcements.add(a);
        System.out.println("New announcement added to course " + title);
    }

    public boolean isEnrolled(Student s) {
        // Check if the student already has an enrollment in this course
        for (Enrollment e : enrollments) {
            if (e.getStudent() != null && e.getStudent().getId() == s.getId()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the student meets all prerequisites for this course.
     * NOTE: This is a placeholder. When grading/completion is implemented,
     * replace with real checks like student.hasCompleted(prereq).
     */
    public boolean meetsPrerequisites(Student s) {
        if (prerequisites.isEmpty()) return true; // no prereqs to check

        for (Course prereq : prerequisites) {
            try {
                // Later, Student will have hasCompleted(Course)
                java.lang.reflect.Method method = s.getClass().getMethod("hasCompleted", Course.class);
                boolean completed = (boolean) method.invoke(s, prereq);
                if (!completed) {
                    System.out.println("Student " + s.getName() + " has not completed prerequisite: " + prereq.getTitle());
                    return false;
                }
            } catch (NoSuchMethodException e) {
                // Student.hasCompleted not implemented yet; assume unmet
                System.out.println("Warning: prerequisite check deferred for " + s.getName());
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public boolean canEnroll(Student s) {
        if (s == null) {
            System.out.println("Invalid student.");
            return false;
        }
        if (!hasCapacity()) {
            System.out.println("Course is full: " + title);
            return false;
        }
        if (isEnrolled(s)) {
            System.out.println("Student " + s.getName() + " is already enrolled in " + title);
            return false;
        }
        if (!meetsPrerequisites(s)) {
            System.out.println("Student " + s.getName() + " has not met prerequisites for " + title);
            return false;
        }
        return true;
    }

    public Enrollment enroll(Student s) {
        if (!canEnroll(s)) return null;
        Enrollment e = new Enrollment(s, this, LocalDateTime.now());
        enrollments.add(e);
        System.out.println("Enrolled " + s.getName() + " in " + title);
        return e;
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
                ", enrollments=" + enrollments.size() +
                '}';
    }
}
