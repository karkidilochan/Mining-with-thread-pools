package csx55.overlay.threads;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import csx55.overlay.node.MessagingNode;
import csx55.overlay.task.Task;

/* A new threadpool is created for each computation node
 * its size is defined during the creation of an overlay
 */
public class ThreadPool {
    /* TODO: replace runnable with miners */

    private BlockingQueue<Task> taskQueue = null;

    // this will be the thread pool
    private ArrayList<TaskThreadRunnable> runnableTasks = new ArrayList<>();

    private Thread[] threads;

    // default constructor
    public ThreadPool(int numberOfThreads, MessagingNode messagingNode) {
        this.threads = new Thread[numberOfThreads];

        int maxTasks = 5;

        /*
         * initialized using ArrayBlockingQueue as it creates a bounded blocking queue
         * wrapped by an array
         */
        taskQueue = new ArrayBlockingQueue<>(maxTasks);

        /*
         * now create an array of running threads of thread pool size
         */
        for (int i = 0; i < numberOfThreads; i++) {
            TaskThreadRunnable task = new TaskThreadRunnable(taskQueue, messagingNode);
            // adding threads to the thread pool
            threads[i] = new Thread(task);
        }

    }

    /*
     * Execute function will take a task and offer it to the task queue
     * This will be picked up by a task thread object
     * This will be synchronized because we want the task queue to be offered by
     * only one thread at a time
     */

    public void executeTask(Runnable task) {
        // this.taskQueue.offer(task);
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

    public ArrayList<TaskThreadRunnable> getTasksRunnable() {
        return runnableTasks;
    }

    public void start() {
        for (int i = 0; i < threads.length; ++i) {
            threads[i].start();
        }
    }

    public void addTask(Task task) {
        this.taskQueue.offer(task);
    }
}
