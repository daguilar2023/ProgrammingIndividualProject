package blackboard.app;


import blackboard.util.AppState;
import blackboard.users.*;
import blackboard.courses.*;

import java.nio.file.*;
import java.util.*;

public class Main {
    private static final Scanner in = new Scanner(System.in);
    // Ask user to create an admin account or re-init sample data if none exist
    private static void ensureAdminInteractive() throws Exception {
        if (!AppState.admins.isEmpty()) return;

        System.out.println("\nNo admin accounts found. Choose an option:");
        System.out.println("1) Create admin manually");
        System.out.println("2) Re-initialize with sample data (recommended for testing, if you dont want to manually create students, teachers and assignments)");
        System.out.print("> ");
        String choice = in.nextLine().trim();

        if ("2".equals(choice)) {
            initSampleData();   // includes admin=admin/admin
            System.out.println("✅ Sample data created (with default admin).");
            return;
        }

        // default to manual if not '2'
        System.out.print("Enter admin: id name username password > ");
        String[] x = in.nextLine().trim().split("\\s+");
        if (x.length < 4) {
            System.out.println("Invalid input. Expected: id name username password");
            return;
        }
        int id = Integer.parseInt(x[0]);
        Admin a = new Admin(id, x[1], x[2], x[3]);
        AppState.admins.add(a);
        a.save();
        System.out.println("✅ Admin created. You can now log in.");
    }

    public static void main(String[] args) throws Exception {
        Files.createDirectories(Paths.get("data"));
        AppState.loadAll();
        ensureAdminInteractive();
        while (true) {
            System.out.println("\n1) Login  2) Exit  3) Reset Data");
            String ch = in.nextLine().trim();
            if ("2".equals(ch)) { AppState.saveAll(); break; }
            if ("3".equals(ch)) {
                try {
                    AppState.resetAllData();
                    System.out.println("🧹 All data cleared. Exiting...");
                } catch (Exception e) {
                    System.out.println("Reset failed: " + e.getMessage());
                }
                break; // exit app
            }
            login();

        }
    }

    private static void initSampleData() throws Exception {
        // Start clean, then make sure data/ exists
        AppState.resetAllData();
        Files.createDirectories(Paths.get("data"));

        // --- Admin (per your spec) ---
        Admin admin = new Admin(1, "admin", "admin", "admin");
        AppState.admins.add(admin);
        admin.save();

        // --- Teachers ---
        Teacher t1 = new Teacher(1, "Teacher 1", "teacher1", "teacher1");
        Teacher t2 = new Teacher(2, "Teacher 2", "teacher2", "teacher2");
        AppState.teachers.add(t1); AppState.teachers.add(t2);
        t1.save(); t2.save();

        // --- Students ---
        Student s1 = new Student(1, "Student 1", "student1", "student1");
        Student s2 = new Student(2, "Student 2", "student2", "student2");
        Student s3 = new Student(3, "Student 3", "student3", "student3");
        AppState.students.add(s1); AppState.students.add(s2); AppState.students.add(s3);
        s1.save(); s2.save(); s3.save();

        // --- Courses (maxCapacity = 2) ---
        Course c1 = new Course("1", "Course 1", 2);
        Course c2 = new Course("2", "Course 2", 2);

        // Assign teachers
        c1.setTeacher(t1);
        c2.setTeacher(t2);

        // Enrollments
        c1.enroll(s1);  // S1 -> C1
        c1.enroll(s3);  // S3 -> C1
        c2.enroll(s2);  // S2 -> C2
        c2.enroll(s3);  // S3 -> C2

        // Assignments
        c1.addAssignment(new Assignment("A1", "Assignment 1"));
        c2.addAssignment(new Assignment("A2", "Assignment 2"));

        // --- Submissions (per your spec) ---
        c1.markSubmitted("A1", 1); // S1 -> A1 (Course 1)
        c1.markSubmitted("A1", 3); // S3 -> A1 (Course 1)

        c2.markSubmitted("A2", 2); // S2 -> A2 (Course 2)
        c2.markSubmitted("A2", 3); // S3 -> A2 (Course 2)

        // persist submission files
        c1.saveSubmissions();
        c2.saveSubmissions();

        // Persist courses (+ assignments)
        AppState.courses.add(c1); AppState.courses.add(c2);
        c1.save(); c2.save();
        c1.saveAssignments(); c2.saveAssignments();

        // --------- Pretty summary printout (with credentials) ---------
        System.out.println("\n===============================");
        System.out.println("  Sample Data Initialized ✅");
        System.out.println("===============================\n");

        // Admin
        System.out.println("Admin:");
        for (Admin adm : AppState.admins) {
            System.out.println(" - id=" + adm.getId()
                    + ", name=" + adm.getName()
                    + ", username=" + adm.getUsername()
                    + ", password=" + adm.getPassword());
        }
        System.out.println();

        // Teachers + which courses they teach (include username/password)
        System.out.println("Teachers:");
        for (Teacher t : AppState.teachers) {
            System.out.print(" - " + t.getId() + ": " + t.getName()
                    + " (username=" + t.getUsername()
                    + ", password=" + t.getPassword() + ")");
            // find their courses
            List<String> myCourses = new ArrayList<>();
            for (Course c : AppState.courses) {
                if (c.getTeacher() != null && c.getTeacher().getId() == t.getId()) {
                    myCourses.add(c.getId() + " (" + c.getTitle() + ")");
                }
            }
            System.out.println(myCourses.isEmpty() ? "" : "  | teaches: " + String.join(", ", myCourses));
        }
        System.out.println();

        // Students + which courses they take (include username/password)
        System.out.println("Students:");
        for (Student s : AppState.students) {
            List<String> courses = new ArrayList<>();
            for (Course c : AppState.courses) {
                if (c.getStudentIds().contains(s.getId())) {
                    courses.add(c.getId() + " (" + c.getTitle() + ")");
                }
            }
            System.out.println(" - " + s.getId() + ": " + s.getName()
                    + " (username=" + s.getUsername()
                    + ", password=" + s.getPassword() + ")  | courses: "
                    + (courses.isEmpty() ? "(none)" : String.join(", ", courses)));
        }
        System.out.println();

        // Courses + assignments + enrolled students
        System.out.println("Courses:");
        for (Course c : AppState.courses) {
            System.out.println(" - " + c.getId() + ": " + c.getTitle()
                    + "  | maxCap=" + c.getMaxCapacity()
                    + "  | teacher=" + (c.getTeacher() == null ? "None" : c.getTeacher().getName()));
            // assignments
            if (c.getAssignments().isEmpty()) {
                System.out.println("    Assignments: (none)");
            } else {
                System.out.print("    Assignments: ");
                List<String> as = new ArrayList<>();
                for (Assignment a : c.getAssignments()) {
                    as.add(a.getId() + " \"" + a.getTitle() + "\"");
                }
                System.out.println(String.join(", ", as));
            }
            // enrolled students
            if (c.getStudentIds().isEmpty()) {
                System.out.println("    Students: (none)");
            } else {
                System.out.print("    Students: ");
                List<String> ss = new ArrayList<>();
                for (int sid : c.getStudentIds()) {
                    for (Student s : AppState.students) {
                        if (s.getId() == sid) {
                            ss.add(s.getId() + " " + s.getName());
                            break;
                        }
                    }
                }
                System.out.println(String.join(", ", ss));
            }
        }
        System.out.println("\n===============================\n");
    }

    static void login(){
        System.out.print("username: "); String u = in.nextLine().trim();
        System.out.print("password: "); String p = in.nextLine().trim();

        for (Admin a: AppState.admins)   if (a.getUsername().equals(u) && a.getPassword().equals(p)) { adminMenu(a); return; }
        for (Teacher t: AppState.teachers)if (t.getUsername().equals(u) && t.getPassword().equals(p)) { teacherMenu(t); return; }
        for (Student s: AppState.students)if (s.getUsername().equals(u) && s.getPassword().equals(p)) { studentMenu(s); return; }
        System.out.println("Invalid credentials.");
    }

    static void adminMenu(Admin a){
        while (true){
            System.out.println("\n[ADMIN] 1) New Teacher 2) New Student 3) New Course 4) Assign Teacher 5) Enroll Student 6) Back 7) View All Users 8) Update User 9) Delete User");
            String ch = in.nextLine().trim();
            try {
                if ("1".equals(ch)) {
                    try {
                        System.out.print("Teacher id (number): ");
                        String idStr = in.nextLine().trim();
                        int id = Integer.parseInt(idStr);

                        System.out.print("Teacher name: ");
                        String name = in.nextLine().trim();

                        System.out.print("Teacher username: ");
                        String user = in.nextLine().trim();

                        System.out.print("Teacher password: ");
                        String pass = in.nextLine().trim();

                        a.createTeacher(new Teacher(id, name, user, pass));
                        System.out.println("✅ Teacher created.");
                    } catch (NumberFormatException nfe) {
                        System.out.println("❌ Invalid id. Please enter a number.");
                    } catch (Exception e) {
                        System.out.println("❌ Could not create teacher: " + e.getMessage());
                    }
                } else if ("2".equals(ch)) {
                    try {
                        System.out.print("Student id (number): ");
                        int id = Integer.parseInt(in.nextLine().trim());

                        System.out.print("Student name: ");
                        String name = in.nextLine().trim();

                        System.out.print("Student username: ");
                        String user = in.nextLine().trim();

                        System.out.print("Student password: ");
                        String pass = in.nextLine().trim();

                        a.createStudent(new Student(id, name, user, pass));
                        System.out.println("✅ Student created.");
                    } catch (NumberFormatException nfe) {
                        System.out.println("❌ Invalid id. Please enter a number.");
                    } catch (Exception e) {
                        System.out.println("❌ Could not create student: " + e.getMessage());
                    }
                } else if ("3".equals(ch)) {
                    try {
                        System.out.print("Course id: ");
                        String cid = in.nextLine().trim();
                        if (cid.isEmpty()) { System.out.println("❌ Course id cannot be empty."); break; }

                        System.out.print("Course title: ");
                        String title = in.nextLine().trim();
                        if (title.isEmpty()) { System.out.println("❌ Course title cannot be empty."); break; }

                        System.out.print("Max capacity (number): ");
                        String capStr = in.nextLine().trim();
                        int cap = Integer.parseInt(capStr);

                        Course cnew = new Course(cid, title, cap);
                        a.createCourse(cnew); // enforces unique id and saves
                    } catch (NumberFormatException nfe) {
                        System.out.println("❌ Invalid capacity. Please enter a number.");
                    } catch (Exception e) {
                        System.out.println("❌ Could not create course: " + e.getMessage());
                    }
                } else if ("4".equals(ch)) {
                    Course c = pickCourse(); Teacher t = pickTeacher(); a.assignTeacher(c,t);
                } else if ("5".equals(ch)) {
                    Course c = pickCourse(); Student s = pickStudent(); a.enrollStudent(c,s);
                } else if ("6".equals(ch)) return;
                else if ("7".equals(ch)) {
                    printAllUsers();
                } else if ("8".equals(ch)) {
                    System.out.print("Enter user type (teacher/student): ");
                    String type = in.nextLine().trim().toLowerCase();
                    System.out.print("Enter user id: ");
                    int id = Integer.parseInt(in.nextLine().trim());

                    if (type.equals("teacher")) {
                        for (Teacher t : AppState.teachers)
                            if (t.getId() == id) {
                                System.out.print("New username: "); String u = in.nextLine();
                                System.out.print("New password: "); String p = in.nextLine();
                                Teacher updated = new Teacher(id, t.getName(), u, p);
                                AppState.teachers.remove(t);
                                a.createTeacher(updated);
                                System.out.println("✅ Teacher updated.");
                                break;
                            }
                    } else if (type.equals("student")) {
                        for (Student s : AppState.students)
                            if (s.getId() == id) {
                                System.out.print("New username: "); String u = in.nextLine();
                                System.out.print("New password: "); String p = in.nextLine();
                                Student updated = new Student(id, s.getName(), u, p);
                                AppState.students.remove(s);
                                a.createStudent(updated);
                                System.out.println("✅ Student updated.");
                                break;
                            }
                    }
                } else if ("9".equals(ch)) {
                    System.out.print("Enter user type (teacher/student): ");
                    String type = in.nextLine().trim().toLowerCase();
                    System.out.print("Enter user id: ");
                    int id = Integer.parseInt(in.nextLine().trim());

                    if (type.equals("teacher")) {
                        AppState.teachers.removeIf(t -> t.getId() == id);
                        Files.deleteIfExists(Paths.get("data", "teachers", id + ".csv"));
                        System.out.println("🗑️ Teacher deleted.");
                    } else if (type.equals("student")) {
                        AppState.students.removeIf(s -> s.getId() == id);
                        Files.deleteIfExists(Paths.get("data", "students", id + ".csv"));
                        System.out.println("🗑️ Student deleted.");
                    }
                }
            } catch(Exception e){ System.out.println("Error: "+e.getMessage()); }
        }
    }

    static void teacherMenu(Teacher t){
        while (true){
            System.out.println("\n[TEACHER] 1) My Courses 2) New Assignment 3) Grade 4) View Assignments 5) View Submissions 6) Back");
            String ch = in.nextLine().trim();
            if ("1".equals(ch)) listCourses(t);
            else if ("2".equals(ch)) {
                Course c = pickCourse();
                if (c == null) { System.out.println("❌ No course selected."); break; }
                System.out.print("Assignment id: ");
                String aid = in.nextLine().trim();
                if (aid.isEmpty()) { System.out.println("❌ Assignment id cannot be empty."); break; }
                System.out.print("Assignment title: ");
                String atitle = in.nextLine().trim();
                if (atitle.isEmpty()) atitle = "Untitled";
                t.createAssignment(c, aid, atitle);
                System.out.println("✅ Assignment created.");
            } else if ("3".equals(ch)) {
                Course c = pickCourse();
                if (c == null) { System.out.println("❌ No course selected."); break; }
                System.out.print("Assignment id: ");
                String aid = in.nextLine().trim();
                if (aid.isEmpty()) { System.out.println("❌ Assignment id cannot be empty."); break; }
                try {
                    System.out.print("Student id (number): ");
                    int sid = Integer.parseInt(in.nextLine().trim());
                    System.out.print("Grade (0-100): ");
                    int grade = Integer.parseInt(in.nextLine().trim());
                    if (grade < 0 || grade > 100) {
                        System.out.println("❌ Grade must be between 0 and 100.");
                    } else {
                        t.recordGrade(c, aid, sid, grade);
                        System.out.println("✅ Grade recorded.");
                    }
                } catch (NumberFormatException nfe) {
                    System.out.println("❌ Invalid number. Please enter numeric student id and grade.");
                } catch (Exception e) {
                    System.out.println("❌ Could not record grade: " + e.getMessage());
                }

            }
            else if ("4".equals(ch)) {
                Course c = pickCourse();
                if (c == null) { System.out.println("❌ No course selected."); break; }
                System.out.println("Assignments for " + c.getTitle() + ":");
                for (Assignment a : c.getAssignments()) {
                    System.out.println("- " + a.getId() + " " + a.getTitle());
                }
            }
            else if ("5".equals(ch)) {
                Course c = pickCourse();
                if (c == null) { System.out.println("❌ No course selected."); break; }
                System.out.print("Assignment id: ");
                String aid = in.nextLine().trim();
                if (aid.isEmpty()) { System.out.println("❌ Assignment id cannot be empty."); break; }

                System.out.println("Submissions for " + c.getTitle() + " / " + aid + ":");
                for (Student s2 : AppState.students) {
                    if (c.getStudentIds().contains(s2.getId())) {
                        boolean submitted = c.hasSubmitted(aid, s2.getId());
                        System.out.println("- " + s2.getId() + " " + s2.getName() + "  [" + (submitted ? "submitted" : "not submitted") + "]");
                    }
                }
            }
            else if ("6".equals(ch)) return;

        }
    }

    static void studentMenu(Student s){
        while (true){
            System.out.println("\n[STUDENT] 1) My Courses 2) View Assignments & Grades 3) Submit Assignment 4) Back");
            String ch = in.nextLine().trim();
            if ("1".equals(ch)) listCourses();
            else if ("2".equals(ch)) {
                Course c = pickCourse();
                System.out.println("Assignments:");
                for (Assignment a : c.getAssignments()){
                    Integer g = c.getGrade(a.getId(), s.getId());
                    System.out.println("- "+a.getId()+" "+a.getTitle()+"  grade: "+(g==null?"N/A":g));
                }

                double finalGrade = c.getFinalGrade(s.getId());
                if (finalGrade >= 0)
                    System.out.println("Final grade: " + String.format("%.2f", finalGrade));
                else
                    System.out.println("No grades recorded yet.");
            }
            else if ("3".equals(ch)) {
                Course c = pickCourse();
                if (c == null) { System.out.println("❌ No course selected."); break; }
                System.out.print("Assignment id to submit: ");
                String aid = in.nextLine().trim();
                if (aid.isEmpty()) { System.out.println("❌ Assignment id cannot be empty."); break; }
                c.markSubmitted(aid, s.getId());
                try { c.saveSubmissions(); } catch (Exception ignore) {}
                System.out.println("✅ Submitted " + aid + " for " + c.getTitle());
            }
            else if ("4".equals(ch)) return;
        }
    }

    // ---- helpers for UI ----
    static void listCourses() {
        for (Course c : AppState.courses)
            System.out.println(c.getId() + ": " + c.getTitle());
    }

    //overload for teachers only
    static void listCourses(Teacher t) {
        for (Course c : AppState.courses)
            if (c.getTeacher() != null && c.getTeacher().getId() == t.getId())
                System.out.println(c.getId() + ": " + c.getTitle());
    }
    static Course pickCourse(){ listCourses(); System.out.print("courseId > "); String id=in.nextLine().trim();
        for (Course c: AppState.courses) if (c.getId().equals(id)) return c; System.out.println("Not found."); return null; }
    static Teacher pickTeacher(){ for (Teacher t: AppState.teachers) System.out.println(t.getId()+": "+t.getName());
        System.out.print("teacherId > "); int id=i(in.nextLine()); for (Teacher t: AppState.teachers) if (t.getId()==id) return t; return null; }
    static Student pickStudent(){ for (Student s: AppState.students) System.out.println(s.getId()+": "+s.getName());
        System.out.print("studentId > "); int id=i(in.nextLine()); for (Student s: AppState.students) if (s.getId()==id) return s; return null; }
    static int i(String s){ try { return Integer.parseInt(s.trim()); } catch(Exception e){ return -1; } }

    static void printAllUsers() {
        System.out.println("\nAdmins:");
        for (Admin a : AppState.admins) {
            System.out.println(a.getId() + "  " + a.getName() + " (" + a.getUsername() + ")");
        }
        System.out.println("\nTeachers:");
        for (Teacher t : AppState.teachers) {
            System.out.println(t.getId() + "  " + t.getName() + " (" + t.getUsername() + ")");
        }
        System.out.println("\nStudents:");
        for (Student s : AppState.students) {
            System.out.println(s.getId() + "  " + s.getName() + " (" + s.getUsername() + ")");
        }
    }

}