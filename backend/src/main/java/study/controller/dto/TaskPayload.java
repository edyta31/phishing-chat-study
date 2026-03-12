package study.controller.dto;

/**
 * Task data sent to client (groundTruth omitted so participants cannot see the answer)
 */
public class TaskPayload {
    private long trialId;
    private long taskId;
    private String kind;
    private String title;
    private String payload;

    public TaskPayload() {}

    public TaskPayload(long trialId, long taskId, String kind, String title, String payload) {
        this.trialId = trialId;
        this.taskId = taskId;
        this.kind = kind;
        this.title = title;
        this.payload = payload;
    }

    public long getTrialId() {
        return trialId;
    }

    public void setTrialId(long trialId) {
        this.trialId = trialId;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
