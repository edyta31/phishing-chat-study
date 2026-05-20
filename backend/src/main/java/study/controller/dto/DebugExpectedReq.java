package study.controller.dto;

public class DebugExpectedReq {
    private String token;
    private int taskIndex;

    public DebugExpectedReq() {}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getTaskIndex() {
        return taskIndex;
    }

    public void setTaskIndex(int taskIndex) {
        this.taskIndex = taskIndex;
    }
}
