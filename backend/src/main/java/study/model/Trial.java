package study.model;

import javax.persistence.*;
import java.time.Instant;

@Entity
public class Trial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long participantId;
    private Long taskId;
    private Integer indexInSequence;
    private Boolean usedChat;
    private String finalDecision;
    private Boolean finalCorrect;
    private Integer confidence;
    private Integer trustInBot;
    private Boolean botShown;
    private Boolean botAnswerCorrect;
    @Lob
    private String botAnswerText;
    private Instant startedAt;
    private Instant decidedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParticipantId() {
        return participantId;
    }

    public void setParticipantId(Long participantId) {
        this.participantId = participantId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Integer getIndexInSequence() {
        return indexInSequence;
    }

    public void setIndexInSequence(Integer indexInSequence) {
        this.indexInSequence = indexInSequence;
    }

    public Boolean getUsedChat() {
        return usedChat;
    }

    public void setUsedChat(Boolean usedChat) {
        this.usedChat = usedChat;
    }

    public String getFinalDecision() {
        return finalDecision;
    }

    public void setFinalDecision(String finalDecision) {
        this.finalDecision = finalDecision;
    }

    public Boolean getFinalCorrect() {
        return finalCorrect;
    }

    public void setFinalCorrect(Boolean finalCorrect) {
        this.finalCorrect = finalCorrect;
    }

    public Integer getConfidence() {
        return confidence;
    }

    public void setConfidence(Integer confidence) {
        this.confidence = confidence;
    }

    public Integer getTrustInBot() {
        return trustInBot;
    }

    public void setTrustInBot(Integer trustInBot) {
        this.trustInBot = trustInBot;
    }

    public Boolean getBotShown() {
        return botShown;
    }

    public void setBotShown(Boolean botShown) {
        this.botShown = botShown;
    }

    public Boolean getBotAnswerCorrect() {
        return botAnswerCorrect;
    }

    public void setBotAnswerCorrect(Boolean botAnswerCorrect) {
        this.botAnswerCorrect = botAnswerCorrect;
    }

    public String getBotAnswerText() {
        return botAnswerText;
    }

    public void setBotAnswerText(String botAnswerText) {
        this.botAnswerText = botAnswerText;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(Instant decidedAt) {
        this.decidedAt = decidedAt;
    }
}
