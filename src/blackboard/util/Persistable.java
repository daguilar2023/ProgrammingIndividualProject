package blackboard.util;

public interface Persistable {
    public void save() throws Exception;
    public void load() throws Exception;
}
