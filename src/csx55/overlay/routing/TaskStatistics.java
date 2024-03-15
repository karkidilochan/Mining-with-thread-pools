package csx55.overlay.routing;

import java.util.concurrent.atomic.AtomicLong;

// import csx55.overlay.wireformats.TrafficSummary;

/**
 * Track statistics of messages sent, received, and
 * relayed.
 */
public class TaskStatistics {

    private AtomicLong generatedCount;
    private AtomicLong pulledCount;
    private AtomicLong pushedCount;
    private AtomicLong completedCount;

    /**
     * Construct a MessagesStatistics object and initialize statistics to zero.
     */
    public TaskStatistics() {
        this.generatedCount = new AtomicLong(0);
        this.pulledCount = new AtomicLong(0);
        this.pushedCount = new AtomicLong(0);
        this.completedCount = new AtomicLong(0);

    }

    /**
     * adds a sent message with its payload size.
     * 
     * @param payload The size of the payload of the sent message.
     */
    public void addGenerated(int tasksCount) {
        generatedCount.getAndAdd(tasksCount);
    }

    /**
     * adds a received message with its payload size.
     * 
     * @param payload The size of the payload of the received message.
     */
    public void addPulled(int tasksCount) {
        pulledCount.getAndAdd(tasksCount);
    }

    public void addCompleted(long tasksCount) {
        completedCount.getAndAdd(tasksCount);
    }

    public void addPushed(int tasksCount) {
        pushedCount.getAndAdd(tasksCount);
    }

    /**
     * Displays the summary of message statistics.
     * 
     * @param statisticsSummary A list of TrafficSummary objects containing
     *                          statistics for each node.
     */

    public long getGenerated() {
        return generatedCount.get();
    }

    public long getPulled() {
        return pulledCount.get();
    }

    public long getPushed() {
        return pushedCount.get();
    }

    public long getCompleted() {
        return completedCount.get();
    }

    /**
     * Reset statistics to zero after each new start by registry.
     */
    public void reset() {
        generatedCount.set(0);
        pulledCount.set(0);
        pushedCount.set(0);
        completedCount.set(0);
    }

}
