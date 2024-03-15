package csx55.overlay.threads;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import csx55.overlay.node.ComputeNode;
import csx55.overlay.task.Miner;
import csx55.overlay.task.Task;

/* Represents the tasks that will be run as a thread */
public class TaskThreadRunnable implements Runnable {

    // a thread object
    private Thread thread;

    // the task queue this thread belongs to
    private BlockingQueue<Task> taskQueue;

    private ComputeNode clientNode;

//    private ThreadPool threadPool;

    // default constructor will take the blocking queue it takes tasks from
    public TaskThreadRunnable(BlockingQueue<Task> taskQueue, ComputeNode clientNode) {
        this.taskQueue = taskQueue;
        this.clientNode = clientNode;
//        this.threadPool = threadPool;
    }

    public void run() {
        /*
         * first define the thread field with current thread
         * the current thread will be called from the thread pool manager
         */
        this.thread = Thread.currentThread();
        while (true) {
            /*
             * then, as long as current task thread isn't stopped, take task from task queue
             * and run
             */
            try {


                /* in final version, this will take the mine instance */
                Task task = taskQueue.take();
                /* call mine function of miner here */
                Miner miner = new Miner();
                miner.startMiner(task);

                /* increase count for completed tasks */
//                clientNode.incrementComplete();

                if (taskQueue.isEmpty()) {
                    checkOnLatch();
                }

            } catch (Exception e) {
                System.out.println("Error mining task:" + e.getMessage());
                e.printStackTrace();
            }
        }

    }

    private synchronized void checkOnLatch() {
        if (this.clientNode.getRoundsLatch().getCount() > 0) {
            System.out.println("Checked by: {}" + this.thread);
            this.clientNode.getRoundsLatch().countDown();
        }
    }
}
