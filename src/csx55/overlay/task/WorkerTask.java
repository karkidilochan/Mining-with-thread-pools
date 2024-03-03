package csx55.overlay.task;

public class WorkerTask {
    private Task task;
    private boolean hasMigrated;

    public WorkerTask(Task task, boolean hasMigrated) {
        this.task = task;
        this.hasMigrated = hasMigrated;
    }

    public Task getTask() {
        return task;
    }

    public boolean checkMigration() {
        return hasMigrated;
    }
}
