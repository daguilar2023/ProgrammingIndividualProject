package blackboard.users;

public class Teacher extends User {
    protected Teacher(int id, String name, String username, String password, UserRole role) {
        super(id, name, username, password, role);
    }

    @Override
    public void save() throws Exception {

    }

    @Override
    public void load() throws Exception {

    }
}
