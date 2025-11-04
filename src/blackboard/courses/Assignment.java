package blackboard.courses;

public class Assignment {
    private final String id;
    private final String title;
    public Assignment(String id, String title) { this.id=id; this.title=title; }
    public String getId(){ return id; }
    public String getTitle(){ return title; }
}