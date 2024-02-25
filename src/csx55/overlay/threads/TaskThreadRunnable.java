package csx55.overlay.threads;

import java.util.concurrent.BlockingQueue;

import csx55.overlay.task.Miner;
import csx55.overlay.task.Task;

/* Represents the tasks that will be run as a thread */
public class TaskThreadRunnable implements Runnable {

    // a thread object
    private Thread thread;

    // the task queue this thread belongs to
    private BlockingQueue<Task> taskQueue;

    // default constructor will take the blocking queue it takes tasks from
    public TaskThreadRunnable(BlockingQueue<Task> taskQueue) {
        this.taskQueue = taskQueue;
    }

    public void run() {
        /*
         * first define the thread field with current thread
         * the current thread will be called from the thread pool manager
         */
        this.thread = Thread.currentThread();
        while (true) {
            System.out.println("Initiating a workload...");
            /*
             * then, as long as current task thread isn't stopped, take task from task queue
             * and run
             */
            try {
                /* in final version, this will take the mine instance */
                Task task = taskQueue.take();
                System.out.println("Retrieved a task. Starting..");
                /* call mine function of miner here */
                Miner miner = new Miner();
                miner.startMiner(task);

                // runnableTask.run();
            } catch (Exception e) {
                System.out.println("Error mining task:" + e.getMessage());
                e.printStackTrace();
            }
            System.out.println("Completed a task.");
        }

    }
}
