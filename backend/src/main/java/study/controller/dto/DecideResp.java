package study.controller.dto;

public class DecideResp {
    private boolean done;

    public DecideResp() {}

    public DecideResp(boolean done) {
        this.done = done;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
