package blackboard.util;

// Interface ensuring all implementing classes can save themselves to CSV files.
public interface CsvPersistable {
    void save() throws Exception;
}
