package study.controller.dto;

public class ChatReq {
    private long trialId;
    private String token;
    private String userText;

    public ChatReq() {}

    public ChatReq(long trialId, String token, String userText) {
        this.trialId = trialId;
        this.token = token;
        this.userText = userText;
    }

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

    public String getUserText() {
        return userText;
    }

    public void setUserText(String userText) {
        this.userText = userText;
    }
}
