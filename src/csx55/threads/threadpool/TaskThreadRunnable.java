package csx55.threads.threadpool;

import java.util.concurrent.BlockingQueue;

/* Represents the tasks that will be run as a thread */
public class TaskThreadRunnable implements Runnable {

    // a thread object
    private Thread thread = null;

    // the task queue this thread belongs to
    private BlockingQueue taskQueue = null;

    // default constructor will take the blocking queue it takes tasks from
    public TaskThreadRunnable(BlockingQueue<Runnable> taskQueue) {
        this.taskQueue = taskQueue;
    }

    public void run() {
        /*
         * first define the thread field with current thread
         * the current thread will be called from the thread pool manager
         */
        this.thread = Thread.currentThread();

        /*
         * then, as long as current task thread isn't stopped, take task from task queue
         * and run
         */
        try {
            Runnable runnableTask = (Runnable) taskQueue.take();
            runnableTask.run();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}
