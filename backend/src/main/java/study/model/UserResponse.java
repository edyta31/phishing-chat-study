package study.model;

import javax.persistence.*;
/**
 * Unused leftover. Participant answers are stored on Trial (and chat on ChatTurn).
 * This table is created by JPA but is always empty.
 */
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
