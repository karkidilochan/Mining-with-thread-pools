package csx55.threads.threadpool;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ThreadPool {

    private BlockingQueue taskQueue = null;

    private List<> runnableTasks = new ArrayList<>();

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

        /*
         * finally, start all the threads of the thread pool
         */
    }
}
