package study.controller.dto;

public class ChatResp {
    private String answer;

    public ChatResp() {}

    public ChatResp(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
