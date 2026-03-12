package study.model;

import javax.persistence.*;

@Entity
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String token;
    private String condition;
    private String taskOrderCsv;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getTaskOrderCsv() {
        return taskOrderCsv;
    }

    public void setTaskOrderCsv(String taskOrderCsv) {
        this.taskOrderCsv = taskOrderCsv;
    }
}
