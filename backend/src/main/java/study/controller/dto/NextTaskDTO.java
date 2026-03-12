package study.controller.dto;

import study.model.Task;

public class NextTaskDTO {
    private boolean done;
    private TaskPayload task;
    private int currentIndex;
    private int totalTasks;

    public NextTaskDTO() {}

    public NextTaskDTO(boolean done, TaskPayload task, int currentIndex, int totalTasks) {
        this.done = done;
        this.task = task;
        this.currentIndex = currentIndex;
        this.totalTasks = totalTasks;
    }

    public static NextTaskDTO of(long trialId, int index, int total, Task t) {
        TaskPayload payload = new TaskPayload(trialId, t.getId(), t.getKind(), t.getTitle(), t.getPayload());
        return new NextTaskDTO(false, payload, index, total);
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public TaskPayload getTask() {
        return task;
    }

    public void setTask(TaskPayload task) {
        this.task = task;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(int totalTasks) {
        this.totalTasks = totalTasks;
    }
}
