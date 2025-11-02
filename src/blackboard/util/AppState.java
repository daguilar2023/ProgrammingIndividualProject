package blackboard.util;

import blackboard.courses.Announcement;
import blackboard.courses.Assignment;
import blackboard.courses.Course;
import blackboard.courses.Enrollment;
import blackboard.courses.Submission;
import blackboard.users.Admin;
import blackboard.users.Student;
import blackboard.users.Teacher;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Global in-memory state for the Mini Blackboard app.
 *
 * Keep this class tiny: only hold lists and provide simple load/save hooks.
 * Real logic stays in the model classes (Course, Enrollment, etc.).
 */
public final class AppState {
    private AppState() {}

    // -------------------- In-memory lists --------------------
    public static final List<Course> courses = new ArrayList<>();
    public static final List<Enrollment> enrollments = new ArrayList<>();
    public static final List<Assignment> assignments = new ArrayList<>();
    public static final List<Submission> submissions = new ArrayList<>();
    public static final List<Announcement> announcements = new ArrayList<>();

    public static final List<Teacher> teachers = new ArrayList<>();
    public static final List<Student> students = new ArrayList<>();
    public static final List<Admin> admins = new ArrayList<>();

    // -------------------- Data locations --------------------
    private static final Path DATA_DIR = Paths.get("data");
    private static final Path COURSES_FILE = Paths.get("data", "courses", "courses.csv");

    // -------------------- Lifecycle --------------------
    /** Ensure base data dirs exist; may be called at app start. */
    public static void initialize() throws IOException {
        FileStore.ensureDir(DATA_DIR);
        FileStore.ensureDir(COURSES_FILE.getParent());
    }

    /** Clear all in-memory collections (useful before load). */
    public static void clearAll() {
        courses.clear();
        enrollments.clear();
        assignments.clear();
        submissions.clear();
        announcements.clear();
        teachers.clear();
        students.clear();
        admins.clear();
    }

    // -------------------- Minimal persistence (Courses only for now) --------------------
    // Simple, non-invasive persistence to keep the project lightweight.

    /** Save just the courses list to CSV. Other entities can be added later similarly. */
    public static void saveCourses() throws IOException {
        List<String> lines = new ArrayList<>();
        for (Course c : courses) {
            String teacherId = (c.getTeacher() == null) ? "" : String.valueOf(c.getTeacher().getId());
            lines.add(FileStore.toCsv(c.getId(), c.getTitle(), String.valueOf(c.getMaxCapacity()), teacherId));
        }
        FileStore.writeAll(COURSES_FILE, lines);
    }

    /** Load courses from CSV. Teachers are resolved best-effort by matching id from the teachers list. */
    public static void loadCourses() throws IOException {
        courses.clear();
        Map<Integer, Teacher> teacherById = new HashMap<>();
        for (Teacher t : teachers) teacherById.put(t.getId(), t);

        for (String line : FileStore.readAll(COURSES_FILE)) {
            String[] cols = FileStore.parseCsvLine(line);
            if (cols.length < 4) continue;
            String id = cols[0];
            String title = cols[1];
            int maxCap;
            try { maxCap = Integer.parseInt(cols[2]); }
            catch (NumberFormatException nfe) { continue; }
            Teacher teacher = null;
            if (!cols[3].isEmpty()) {
                try { teacher = teacherById.get(Integer.parseInt(cols[3])); } catch (NumberFormatException ignored) {}
            }
            courses.add(new Course(id, title, maxCap, teacher));
        }
    }

    // -------------------- Read-only views --------------------
    public static List<Course> getCourses() { return Collections.unmodifiableList(courses); }
    public static List<Teacher> getTeachers() { return Collections.unmodifiableList(teachers); }
    public static List<Student> getStudents() { return Collections.unmodifiableList(students); }
    public static List<Enrollment> getEnrollments() { return Collections.unmodifiableList(enrollments); }
    public static List<Assignment> getAssignments() { return Collections.unmodifiableList(assignments); }
    public static List<Submission> getSubmissions() { return Collections.unmodifiableList(submissions); }
    public static List<Announcement> getAnnouncements() { return Collections.unmodifiableList(announcements); }
}
