package study.controller.dto;

/**
 * Decision for current task plus after-task questions (trust, used chatbot, etc.).
 */
public class DecideReq {
    private long trialId;
    private String token;
    private String decision;       // "phish" | "legit"
    private Integer confidence;    // e.g. 1-5 Likert
    private Boolean usedChatbot;   // did they use the chatbot for this task
    private Integer trustInBot;    // e.g. 1-5 how much they trusted the bot's advice

    public DecideReq() {}

    public long getTrialId() {
        return trialId;
    }

    public void setTrialId(long trialId) {
        this.trialId = trialId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public Integer getConfidence() {
        return confidence;
    }

    public void setConfidence(Integer confidence) {
        this.confidence = confidence;
    }

    public Boolean getUsedChatbot() {
        return usedChatbot;
    }

    public void setUsedChatbot(Boolean usedChatbot) {
        this.usedChatbot = usedChatbot;
    }

    public Integer getTrustInBot() {
        return trustInBot;
    }

    public void setTrustInBot(Integer trustInBot) {
        this.trustInBot = trustInBot;
    }
}
