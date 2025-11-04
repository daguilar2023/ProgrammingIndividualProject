package blackboard.util;

import blackboard.users.*;
import blackboard.courses.Course;

import java.util.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.stream.Stream;

public final class AppState {
    public static final List<Admin> admins   = new ArrayList<>();
    public static final List<Teacher> teachers = new ArrayList<>();
    public static final List<Student> students = new ArrayList<>();
    public static final List<Course>  courses  = new ArrayList<>();

    private AppState(){}

    public static void loadAll() throws Exception {
        loadUsers("admins", admins, (p)->{
            String[] a = readOne(p); return new Admin(i(a[0]), a[1], a[2], a[3]);
        });
        loadUsers("teachers", teachers, (p)->{
            String[] a = readOne(p); return new Teacher(i(a[0]), a[1], a[2], a[3]);
        });
        loadUsers("students", students, (p)->{
            String[] a = readOne(p); return new Student(i(a[0]), a[1], a[2], a[3]);
        });

        courses.clear();
        Path cdir = Paths.get("data","courses");
        if (Files.isDirectory(cdir)){
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(cdir, "*.csv")){
                for (Path p : ds){
                    Course c = Course.load(p);
                    if (c != null) {
                        c.loadSubmissionsIfExists();
                        courses.add(c);
                    }
                }
            }
        }
        // (Teacher resolution: keep simple—admin assigns and saves later)
    }

    public static void saveAll() throws Exception {
        for (Admin a: admins) a.save();
        for (Teacher t: teachers) t.save();
        for (Student s: students) s.save();
        for (Course c: courses) c.save();
    }

    // --- helpers ---
    public static boolean teacherIdExists(int id) {
        return teachers.stream().anyMatch(t -> t.getId() == id);
    }
    public static boolean studentIdExists(int id) {
        return students.stream().anyMatch(s -> s.getId() == id);
    }
    public static boolean courseIdExists(String id) {
        if (id == null) return false;
        String needle = id.trim();
        return courses.stream().anyMatch(c -> c.getId().equals(needle));
    }

    private interface Maker<T> { T make(Path p) throws Exception; }
    private static <T> void loadUsers(String folder, List<T> out, Maker<T> mk) throws Exception {
        out.clear(); Path dir = Paths.get("data", folder);
        if (!Files.isDirectory(dir)) return;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.csv")){
            for (Path p : ds) out.add(mk.make(p));
        }
    }
    private static String[] readOne(Path p) throws Exception {
        String s = Files.readString(p, StandardCharsets.UTF_8).trim();
        String[] a = s.split(",", -1);
        while (a.length < 4) {
            s += ",";
            a = s.split(",", -1);
        }
        return a;
    }
    private static int i(String s){ try { return Integer.parseInt(s.trim()); } catch(Exception e){ return -1; } }

    public static void resetAllData() throws Exception {
        Path data = Paths.get("data");
        if (Files.exists(data)) {
            try (Stream<Path> walk = Files.walk(data)) {
                walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception e) {
                        System.out.println("Delete failed: " + p + " -> " + e.getMessage());
                    }
                });
            }
        }
        admins.clear();
        teachers.clear();
        students.clear();
        courses.clear();
    }
}