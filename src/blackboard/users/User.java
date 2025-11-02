package blackboard.users;

import blackboard.util.Persistable;

public abstract class User implements Persistable {

    public enum UserRole {
        ADMIN,
        TEACHER,
        STUDENT
    }

    private int id;
    private String name;
    private String username;
    private String password;
    private UserRole role;

    protected User(int id, String name, String username, String password, UserRole role) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Stub for later logic
    public boolean login(String username, String password) {
        return false;
    }

    public void viewProfile() {
        // future implementation
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public UserRole getRole() { return role; }


    //this override is to make it so that when u print a user it gives more useful info rather than just the memory
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", role=" + role +
                '}';
    }
}
