package csx55.overlay.routing;

import java.util.List;
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
    // public void display(List<TrafficSummary> statisticsSummary) {
    // if (statisticsSummary.size() == 0) {
    // System.out.println("No message statistics available.");
    // return;
    // }
    // int totalSent = 0;
    // int totalReceived = 0;
    // long totalSentSummation = 0;
    // long totalReceivedSummation = 0;

    // System.out.println(
    // String.format("\n%1$20s %2$1s %3$1s %4$5s %5$5s %6$5s",
    // "",
    // "No. of generated tasks",
    // "No. of pulled tasks",
    // "No. of pushed tasks",
    // "No. of completed tasks"));

    // for (TrafficSummary summary : statisticsSummary) {
    // System.out.println(summary.toString());
    // totalSent += summary.getSentMessagesCount();
    // totalReceived += summary.getReceivedMessagesCount();
    // totalSentSummation += summary.getSentMessagesSummation();
    // totalReceivedSummation += summary.getReceivedSummationCount();
    // }
    // System.out.println(String.format("%1$20s %2$40s %3$20s %4$15s %5$15s\n",
    // "Sum:", Integer.toString(totalSent),
    // Integer.toString(totalReceived), Long.toString(totalSentSummation),
    // Long.toString(totalReceivedSummation)));
    // }

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
