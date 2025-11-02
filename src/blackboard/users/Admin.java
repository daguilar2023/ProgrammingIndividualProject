package blackboard.users;

import blackboard.courses.Course;

public class Admin extends User {

    // Base-models: fix role to ADMIN
    public Admin(int id, String name, String username, String password) {
        super(id, name, username, password, UserRole.ADMIN);
    }

    @Override
    public void save() throws Exception {
        // TODO(feature/persistence-stub): persist admin record to storage
    }

    @Override
    public void load() throws Exception {
        // TODO(feature/persistence-stub): load admin record from storage
    }

    public void createTeacher(Teacher t) {
        // TODO(feature/base-models): store created teacher account
    }

    public void createStudent(Student s) {
        // TODO(feature/base-models): store created student account
    }

    public void createCourse() {
        // TODO(feature/base-models): create a new course record
    }

    public void enrollStudent(Course c) {
        // TODO(feature/enrollment-rules): enroll a student into the course (choose student in UI)
    }

    public void assignTeacher(Course course, Teacher t) {
        // TODO(feature/enrollment-rules): assign teacher to course
    }

    @Override
    public String toString() {
        return "Admin{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", username='" + getUsername() + '\'' +
                '}';
    }
}
