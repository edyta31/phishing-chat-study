package study.controller.dto;

import study.model.Participant;

public class ParticipantDTO {
    private String token;
    private int totalTasks;

    public ParticipantDTO() {}

    public ParticipantDTO(String token, int totalTasks) {
        this.token = token;
        this.totalTasks = totalTasks;
    }

    public static ParticipantDTO from(Participant p, long totalTasks) {
        return new ParticipantDTO(p.getToken(), (int) totalTasks);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(int totalTasks) {
        this.totalTasks = totalTasks;
    }
}
