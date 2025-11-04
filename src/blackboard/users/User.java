package blackboard.users;

public abstract class User {
    private final int id;
    private final String name;
    private final String username;
    private final String password;
    private final UserRole role;

    protected User(int id, String name, String username, String password, UserRole role) {
        this.id = id; this.name = name; this.username = username; this.password = password; this.role = role;
    }
    public int getId() { return id; }
    public String getName() { return name == null ? "" : name; }
    public String getUsername() { return username == null ? "" : username; }
    public String getPassword() { return password == null ? "" : password; }
    public UserRole getRole() { return role; }

    public abstract void save() throws Exception;
    public abstract void load() throws Exception;
}