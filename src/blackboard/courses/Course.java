package blackboard.courses;

import blackboard.users.Student;
import blackboard.users.Teacher;
import blackboard.util.CsvPersistable;

import java.util.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class Course implements CsvPersistable {
    private final String id;
    private final String title;
    private final int maxCapacity;
    private Teacher teacher; // nullable
    private final List<Integer> studentIds = new ArrayList<>();
    private final List<Assignment> assignments = new ArrayList<>();
    // store grades as (assignmentId -> (studentId -> grade))
    private final Map<String, Map<Integer,Integer>> grades = new HashMap<>();

    public Course(String id, String title, int maxCapacity) {
        this.id=id; this.title=title; this.maxCapacity=maxCapacity;
    }

    public String getId(){ return id; }
    public String getTitle(){ return title; }
    public int getMaxCapacity(){ return maxCapacity; }
    public Teacher getTeacher(){ return teacher; }
    public void setTeacher(Teacher t){ this.teacher=t; }

    public boolean enroll(Student s){
        if (s==null) return false;
        if (studentIds.contains(s.getId())) { System.out.println("Already enrolled"); return false; }
        if (maxCapacity>0 && studentIds.size()>=maxCapacity){ System.out.println("Course full"); return false; }
        studentIds.add(s.getId()); return true;
    }

    public List<Integer> getStudentIds(){ return Collections.unmodifiableList(studentIds); }
    public List<Assignment> getAssignments(){ return Collections.unmodifiableList(assignments); }

    public void addAssignment(Assignment a){
        if (a==null) return;
        for (Assignment x: assignments) if (x.getId().equals(a.getId())) return;
        assignments.add(a);
    }

    private final Map<String, java.util.Set<Integer>> submissions = new HashMap<>();

    public void setGrade(String assignmentId, int studentId, int grade){
        grades.computeIfAbsent(assignmentId,k->new HashMap<>()).put(studentId, grade);
    }
    public Integer getGrade(String assignmentId, int studentId){
        Map<Integer,Integer> m = grades.get(assignmentId);
        return (m==null)? null : m.get(studentId);
    }

    public double getFinalGrade(int studentId) {
        int total = 0, count = 0;
        for (Map<Integer, Integer> m : grades.values()) {
            if (m.containsKey(studentId)) {
                total += m.get(studentId);
                count++;
            }
        }
        return count == 0 ? -1 : (double) total / count;
    }

    public void markSubmitted(String assignmentId, int studentId) {
        submissions.computeIfAbsent(assignmentId, k -> new java.util.HashSet<>()).add(studentId);
    }

    public boolean hasSubmitted(String assignmentId, int studentId) {
        return submissions.getOrDefault(assignmentId, java.util.Collections.emptySet()).contains(studentId);
    }

    public void saveSubmissions() throws Exception {
        Path dir = Paths.get("data","submissions"); Files.createDirectories(dir);
        Path file = dir.resolve(safe(id)+".csv");
        StringBuilder sb = new StringBuilder();
        for (var e : submissions.entrySet()) {
            for (int sid : e.getValue()) {
                sb.append(e.getKey()).append(",").append(sid).append('\n');
            }
        }
        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }

    public void loadSubmissionsIfExists() throws Exception {
        Path file = Paths.get("data","submissions", safe(id)+".csv");
        if (!Files.exists(file)) return;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (line.isBlank()) continue;
            String[] p = line.split(",", -1);
            if (p.length >= 2) {
                String aid = p[0];
                int sid = parseInt(p[1], -1);
                if (sid >= 0) markSubmitted(aid, sid);
            }
        }
    }

    // ----------------- Persistence -----------------

    public void save() throws Exception {
        Path dir = Paths.get("data","courses"); Files.createDirectories(dir);
        Path file = dir.resolve(safe(id)+".csv");
        String teacherId = (teacher==null) ? "" : String.valueOf(teacher.getId());
        String students = String.join(";", studentIds.stream().map(String::valueOf).toList());
        String line = String.join(",", id, title, String.valueOf(maxCapacity), teacherId, students);
        Files.writeString(file, line+System.lineSeparator(), StandardCharsets.UTF_8);
    }

    public static Course load(Path file) throws Exception {
        String data = Files.readString(file, StandardCharsets.UTF_8).trim();
        if (data.isEmpty()) return null;
        String[] p = data.split(",", -1);
        // id,title,maxCapacity,teacherId,students
        String id = p[0]; String title = p[1];
        int cap = parseInt(p[2], 0);
        Course c = new Course(id,title,cap);
        // teacher resolved later in AppState
        if (p.length>=5 && !p[4].isEmpty()){
            for (String tok : p[4].split(";")){
                if (!tok.isEmpty()) c.studentIds.add(parseInt(tok,-1));
            }
        }
        return c;
    }

    public void saveAssignments() throws Exception {
        Path dir = Paths.get("data","assignments"); Files.createDirectories(dir);
        Path file = dir.resolve(safe(id)+".csv");
        StringBuilder sb = new StringBuilder();
        for (Assignment a: assignments){
            sb.append(String.join(",", a.getId(), a.getTitle())).append('\n');
        }
        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }

    public void saveGrades() throws Exception {
        Path dir = Paths.get("data","grades"); Files.createDirectories(dir);
        Path file = dir.resolve(safe(id)+".csv");
        StringBuilder sb = new StringBuilder();
        for (var e : grades.entrySet()){
            String aid = e.getKey();
            for (var g : e.getValue().entrySet()){
                sb.append(String.join(",", aid, String.valueOf(g.getKey()), String.valueOf(g.getValue()))).append('\n');
            }
        }
        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }

    public boolean hasAssignment(String assignmentId) {
        if (assignmentId == null) return false;
        String needle = assignmentId.trim();
        for (Assignment a : getAssignments()) {
            if (a.getId().equals(needle)) return true;
        }
        return false;
    }

    // small helpers (local)
    private static String safe(String s){ return s.replaceAll("[^a-zA-Z0-9._-]","_"); }
    private static int parseInt(String s,int fb){ try{return Integer.parseInt(s.trim());}catch(Exception e){return fb;}}
}