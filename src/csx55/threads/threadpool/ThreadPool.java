package csx55.threads.threadpool;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ThreadPool {

    private BlockingQueue<Runnable> taskQueue = null;

    // this will be the thread pool
    private ArrayList<TaskThreadRunnable> runnableTasks = new ArrayList<>();

    // default constructor
    public ThreadPool(int numberOfThreads, int maxTasks) {

        /*
         * initialized using ArrayBlockingQueue as it creates a bounded blocking queue
         * wrapped by an array
         */
        taskQueue = new ArrayBlockingQueue<>(maxTasks);

        /*
         * now create an array of running threads of thread pool size
         */
        for (int i = 0; i < numberOfThreads; i++) {
            TaskThreadRunnable tasks = new TaskThreadRunnable(taskQueue);
            // adding threads to the thread pool
            runnableTasks.add(tasks);
        }

        /*
         * finally, start all the threads of the thread pool
         */
        for (TaskThreadRunnable taskThread : runnableTasks) {
            new Thread(taskThread).start();
        }

    }

    /*
     * Execute function will take a task and offer it to the task queue
     * This will be picked up by a task thread object
     * This will be synchronized because we want the task queue to be offered by
     * only one thread at a time
     */

    public void executeTask(Runnable task) {
        this.taskQueue.offer(task);
    }

    public synchronized void waitUntilAllTasksFinished() {
        while (this.taskQueue.size() > 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
