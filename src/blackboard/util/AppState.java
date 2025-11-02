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
import java.nio.file.Files;
import java.nio.file.DirectoryStream;
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
    private static final Path DATA_DIR     = Paths.get("data");
    private static final Path ADMINS_DIR   = DATA_DIR.resolve("admins");
    private static final Path TEACHERS_DIR = DATA_DIR.resolve("teachers");
    private static final Path STUDENTS_DIR = DATA_DIR.resolve("students");
    private static final Path COURSES_DIR  = DATA_DIR.resolve("courses");

    // -------------------- Lifecycle --------------------
    /** Ensure base data dirs exist; may be called at app start. */
    public static void initialize() throws IOException {
        FileStore.ensureDir(DATA_DIR);
        FileStore.ensureDir(ADMINS_DIR);
        FileStore.ensureDir(TEACHERS_DIR);
        FileStore.ensureDir(STUDENTS_DIR);
        FileStore.ensureDir(COURSES_DIR);
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

    // -------------------- Orchestration --------------------
    /** Load everything (users first, then courses), then resolve references. */
    public static void loadAll() throws Exception {
        initialize();
        clearAll();
        loadUsers();
        loadCoursesFromFiles();
        resolveCourseRefs();
    }

    /** Save all users and courses using their own save() implementations. */
    public static void saveAll() throws Exception {
        initialize();
        for (Admin a : admins)   try { a.save(); }    catch (Exception e) { log("Save admin failed: " + e.getMessage()); }
        for (Teacher t : teachers) try { t.save(); }  catch (Exception e) { log("Save teacher failed: " + e.getMessage()); }
        for (Student s : students) try { s.save(); }  catch (Exception e) { log("Save student failed: " + e.getMessage()); }
        for (Course c : courses)  try { c.save(); }   catch (Exception e) { log("Save course failed: " + e.getMessage()); }
    }

    // -------------------- Persistence helpers --------------------

    /** Save courses by delegating to Course.save(), one file per course. */
    public static void saveCourses() throws IOException {
        initialize();
        for (Course c : courses) {
            try {
                c.save();
            } catch (Exception e) {
                log("Save course failed: " + c.getId() + " -> " + e.getMessage());
            }
        }
    }

    /** Load courses from individual CSV files written by Course.save(). */
    public static void loadCourses() throws IOException {
        initialize();
        courses.clear();
        loadCoursesFromFiles();
        resolveCourseRefs();
    }

    // Internal: iterate data/courses/*.csv and call Course.load(Path)
    private static void loadCoursesFromFiles() throws IOException {
        if (!Files.isDirectory(COURSES_DIR)) return;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(COURSES_DIR, "*.csv")) {
            for (Path p : ds) {
                try {
                    Course c = Course.load(p);
                    if (c != null) courses.add(c);
                } catch (Exception e) {
                    log("Skip bad course file " + p.getFileName() + ": " + e.getMessage());
                }
            }
        }
    }

    // After raw loads, hook up teacher and re-create enrollments.
    private static void resolveCourseRefs() {
        Map<Integer, Teacher> teacherById = new HashMap<>();
        for (Teacher t : teachers) teacherById.put(t.getId(), t);

        Map<Integer, Student> studentById = new HashMap<>();
        for (Student s : students) studentById.put(s.getId(), s);

        for (Course c : courses) {
            // teacher
            Integer tid = c.getTeacherIdRef();
            if (tid != null) {
                Teacher t = teacherById.get(tid);
                if (t != null) c.setTeacher(t);
            }
            // enrollments
            for (Integer sid : c.getPendingEnrollStudentIds()) {
                Student s = studentById.get(sid);
                if (s != null) c.enroll(s);
            }
            c.clearTransientRefs();
        }
    }

    // -------------------- User loaders (simple CSV: id,name,username,password) --------------------
    private static void loadUsers() throws IOException {
        loadAdmins();
        loadTeachers();
        loadStudents();
    }

    private static void loadAdmins() throws IOException {
        admins.clear();
        if (!Files.isDirectory(ADMINS_DIR)) return;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(ADMINS_DIR, "*.csv")) {
            for (Path p : ds) {
                try {
                    String[] cols = readOneLineCsv(p, 4);
                    int id = parseInt(cols[0], -1);
                    String name = cols[1];
                    String username = cols[2];
                    String password = cols[3];
                    if (id >= 0) admins.add(new Admin(id, name, username, password));
                } catch (Exception e) {
                    log("Skip bad admin file " + p.getFileName() + ": " + e.getMessage());
                }
            }
        }
    }

    private static void loadTeachers() throws IOException {
        teachers.clear();
        if (!Files.isDirectory(TEACHERS_DIR)) return;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(TEACHERS_DIR, "*.csv")) {
            for (Path p : ds) {
                try {
                    String[] cols = readOneLineCsv(p, 4);
                    int id = parseInt(cols[0], -1);
                    String name = cols[1];
                    String username = cols[2];
                    String password = cols[3];
                    if (id >= 0) teachers.add(new Teacher(id, name, username, password));
                } catch (Exception e) {
                    log("Skip bad teacher file " + p.getFileName() + ": " + e.getMessage());
                }
            }
        }
    }

    private static void loadStudents() throws IOException {
        students.clear();
        if (!Files.isDirectory(STUDENTS_DIR)) return;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(STUDENTS_DIR, "*.csv")) {
            for (Path p : ds) {
                try {
                    String[] cols = readOneLineCsv(p, 4);
                    int id = parseInt(cols[0], -1);
                    String name = cols[1];
                    String username = cols[2];
                    String password = cols[3];
                    if (id >= 0) students.add(new Student(id, name, username, password));
                } catch (Exception e) {
                    log("Skip bad student file " + p.getFileName() + ": " + e.getMessage());
                }
            }
        }
    }

    // -------------------- Small helpers --------------------
    private static String[] readOneLineCsv(Path p, int expected) throws IOException {
        String data = Files.readString(p).trim();
        if (data.isEmpty()) {
            String[] out = new String[expected];
            for (int i = 0; i < expected; i++) out[i] = "";
            return out;
        }
        // simple CSV splitter (supports quotes)
        List<String> out = new ArrayList<>(expected);
        boolean inQ = false;
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            char ch = data.charAt(i);
            if (inQ) {
                if (ch == '"') {
                    if (i + 1 < data.length() && data.charAt(i + 1) == '"') { cur.append('"'); i++; }
                    else inQ = false;
                } else cur.append(ch);
            } else {
                if (ch == ',') { out.add(cur.toString()); cur.setLength(0); }
                else if (ch == '"') inQ = true;
                else cur.append(ch);
            }
        }
        out.add(cur.toString());
        while (out.size() < expected) out.add("");
        return out.toArray(new String[0]);
    }

    private static int parseInt(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return fallback; }
    }

    private static void log(String msg) {
        System.out.println("[AppState] " + msg);
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
