package study.model;

import javax.persistence.*;
import java.time.Instant;

@Entity
public class ChatTurn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long trialId;
    private String role;
    @Lob
    private String text;
    private Instant ts;

    public ChatTurn() {}

    public ChatTurn(Long trialId, String role, String text) {
        this.trialId = trialId;
        this.role = role;
        this.text = text;
        this.ts = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTrialId() {
        return trialId;
    }

    public void setTrialId(Long trialId) {
        this.trialId = trialId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Instant getTs() {
        return ts;
    }

    public void setTs(Instant ts) {
        this.ts = ts;
    }
}
