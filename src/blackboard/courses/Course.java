package blackboard.courses;

import blackboard.users.Teacher;
import blackboard.users.Student;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Course {
    private String id;
    private String title;
    private int maxCapacity;
    private Set<Course> prerequisites;
    private Teacher teacher;
    private List<Assignment> assignments;
    private List<Announcement> announcements;
    private List<Enrollment> enrollments;

    // Transient refs used during load; resolved in AppState
    private transient Integer teacherIdRef;
    private transient List<Integer> pendingEnrollStudentIds;

    public Course(String id, String title, int maxCapacity, Teacher teacher) {
        this.id = id;
        this.title = title;
        this.maxCapacity = maxCapacity;
        this.teacher = teacher;
        this.prerequisites = new HashSet<>();
        this.assignments = new ArrayList<>();
        this.announcements = new ArrayList<>();
        this.enrollments = new ArrayList<>();
        this.pendingEnrollStudentIds = new ArrayList<>();
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

    public void setTeacher(Teacher t) {
        this.teacher = t;
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
     * Placeholder: returns true for now to keep enrollment unblocked until
     * Student.hasCompleted(Course) is implemented.
     */
    public boolean meetsPrerequisites(Student s) {
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

    // =====================================================================
    //                          Persistence
    // =====================================================================

    /**
     * CSV layout:
     * id,title,maxCapacity,teacherId,studentId1;studentId2;...
     */
    public void save() throws Exception {
        Path dir = Paths.get("data", "courses");
        Files.createDirectories(dir);
        Path file = dir.resolve(safeFileName(id) + ".csv");

        String teacherIdStr = (teacher == null) ? "" : String.valueOf(teacher.getId());

        // Collect current enrollment student IDs
        List<String> studentIdStrings = new ArrayList<>();
        for (Enrollment e : enrollments) {
            if (e.getStudent() != null) {
                studentIdStrings.add(String.valueOf(e.getStudent().getId()));
            }
        }
        String studentsJoined = String.join(";", studentIdStrings);

        String line = String.join(",",
                escapeCsv(id),
                escapeCsv(title),
                String.valueOf(maxCapacity),
                teacherIdStr,
                studentsJoined
        );

        Files.writeString(
                file,
                line + System.lineSeparator(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    /**
     * Load a course from a CSV file. Leaves teacher unresolved but stores teacherIdRef.
     * Also captures student IDs into pendingEnrollStudentIds so AppState can resolve
     * them later into real Enrollment objects.
     */
    public static Course load(Path file) throws Exception {
        if (!Files.exists(file)) return null;

        String data = Files.readString(file, StandardCharsets.UTF_8).trim();
        if (data.isEmpty()) return null;

        // Split allowing empty trailing fields
        String[] parts = splitCsvLine(data, 5);

        // expected: id,title,maxCapacity,teacherId,students
        String id = unescapeCsv(parts[0]);
        String title = unescapeCsv(parts[1]);
        int maxCapacity = parseIntSafe(parts[2], 0);

        Course c = new Course(id, title, maxCapacity, null);

        if (!parts[3].isEmpty()) {
            c.teacherIdRef = parseIntSafeObj(parts[3], null);
        }

        if (!parts[4].isEmpty()) {
            String[] toks = parts[4].split(";");
            for (String tok : toks) {
                if (!tok.isEmpty()) {
                    Integer sid = parseIntSafeObj(tok, null);
                    if (sid != null) {
                        c.pendingEnrollStudentIds.add(sid);
                    }
                }
            }
        }

        return c;
    }

    /** Expose teacherIdRef for AppState resolution */
    public Integer getTeacherIdRef() { return teacherIdRef; }
    public List<Integer> getPendingEnrollStudentIds() { return Collections.unmodifiableList(pendingEnrollStudentIds); }
    public void clearTransientRefs() {
        this.teacherIdRef = null;
        this.pendingEnrollStudentIds.clear();
    }

    // =====================================================================
    //                          Helpers
    // =====================================================================

    private static String safeFileName(String s) {
        return s == null ? "" : s.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static int parseIntSafe(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return fallback; }
    }
    private static Integer parseIntSafeObj(String s, Integer fallback) {
        try { return Integer.valueOf(s.trim()); } catch (Exception e) { return fallback; }
    }

    // Basic CSV escaping/unescaping (handles commas/quotes/newlines)
    private static String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private static String unescapeCsv(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1).replace("\"\"", "\"");
        }
        return s;
    }

    /**
     * Split a single-line CSV into at most `expected` columns,
     * preserving empty trailing fields.
     */
    private static String[] splitCsvLine(String line, int expected) {
        // Simple splitter for our limited schema; not a full CSV parser.
        List<String> out = new ArrayList<>(expected);
        boolean inQuotes = false;
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        // Escaped quote
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(ch);
                }
            } else {
                if (ch == ',') {
                    out.add(cur.toString());
                    cur.setLength(0);
                } else if (ch == '"') {
                    inQuotes = true;
                } else {
                    cur.append(ch);
                }
            }
        }
        out.add(cur.toString());
        while (out.size() < expected) out.add("");
        return out.toArray(new String[0]);
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
