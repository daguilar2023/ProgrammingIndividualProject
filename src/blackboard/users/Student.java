package blackboard.users;

import blackboard.util.CsvPersistable;

import java.nio.file.*;

public class Student extends User implements CsvPersistable {
    public Student(int id, String name, String username, String password) {
        super(id, name, username, password, UserRole.STUDENT);
    }

    @Override public void save() throws Exception {
        Path dir = Paths.get("data","students"); Files.createDirectories(dir);
        Path file = dir.resolve(getId()+".csv");
        String line = String.join(",",
                String.valueOf(getId()), getName(), getUsername(), getPassword());
        Files.writeString(file, line+System.lineSeparator());
    }
    @Override public void load() throws Exception { }
}