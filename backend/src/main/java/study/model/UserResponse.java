package study.model;

import javax.persistence.*;

@Entity
public class UserResponse {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long taskId;
    private String answer;

    public Long getId() { return id; }
    public Long getTaskId() { return taskId; }
    public String getAnswer() { return answer; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public void setAnswer(String answer) { this.answer = answer; }
}
