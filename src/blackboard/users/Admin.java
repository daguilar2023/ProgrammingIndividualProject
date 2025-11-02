package blackboard.users;

public class Admin extends User {
    protected Admin(int id, String name, String username, String password, UserRole role) {
        super(id, name, username, password, role);
    }

    @Override
    public void save() throws Exception {

    }

    @Override
    public void load() throws Exception {

    }
}
