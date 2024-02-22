package csx55.threads.threadpool;

public class Test {

    public static void main(String[] args) throws Exception {

        ThreadPool threadPool = new ThreadPool(3, 5);

        for (int i = 0; i < 5; i++) {
            int taskNo = i;
            // this anonymous function signifies the run method of the runnable task we are
            // creating
            threadPool.executeTask(() -> {
                String message = Thread.currentThread().getName() + ": Task" + taskNo;
                System.out.println(message);
            });
        }

        threadPool.waitUntilAllTasksFinished();
    }
}
