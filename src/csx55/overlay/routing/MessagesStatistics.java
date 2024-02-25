package csx55.overlay.routing;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import csx55.overlay.wireformats.TrafficSummary;

/**
 * Track statistics of messages sent, received, and
 * relayed.
 */
public class MessagesStatistics {

    private AtomicInteger sentMessagesCount;
    private AtomicInteger receivedMessagesCount;
    private AtomicLong sentMessagesSummation;
    private AtomicLong receivedMessagesSummation;
    private AtomicInteger relayedMessagesCount;

    /**
     * Construct a MessagesStatistics object and initialize statistics to zero.
     */
    public MessagesStatistics() {
        this.sentMessagesCount = new AtomicInteger(0);
        this.receivedMessagesCount = new AtomicInteger(0);
        this.sentMessagesSummation = new AtomicLong(0);
        this.receivedMessagesSummation = new AtomicLong(0);
        this.relayedMessagesCount = new AtomicInteger(0);

    }

    /**
     * Records a sent message with its payload size.
     * 
     * @param payload The size of the payload of the sent message.
     */
    public void recordSent(int payload) {
        sentMessagesSummation.getAndAdd(payload);
        sentMessagesCount.getAndIncrement();
    }

    /**
     * Records a received message with its payload size.
     * 
     * @param payload The size of the payload of the received message.
     */
    public void recordReceived(int payload) {
        receivedMessagesSummation.getAndAdd(payload);
        receivedMessagesCount.getAndIncrement();
    }

    /**
     * Records a relayed message.
     */
    public void recordForwarded() {
        relayedMessagesCount.getAndIncrement();
    }

    /**
     * Displays the summary of message statistics.
     * 
     * @param statisticsSummary A list of TrafficSummary objects containing
     *                          statistics for each node.
     */
    public void display(List<TrafficSummary> statisticsSummary) {
        if (statisticsSummary.size() == 0) {
            System.out.println("No message statistics available.");
            return;
        }
        int totalSent = 0;
        int totalReceived = 0;
        long totalSentSummation = 0;
        long totalReceivedSummation = 0;

        System.out.println(
                String.format("\n%1$20s %2$1s %3$1s %4$5s %5$5s %6$5s",
                        "",
                        "No. of messages sent",
                        "No. of messages received",
                        "Sum of sent messages",
                        "Sum of received messages",
                        "No. of messages relayed"));

        for (TrafficSummary summary : statisticsSummary) {
            System.out.println(summary.toString());
            totalSent += summary.getSentMessagesCount();
            totalReceived += summary.getReceivedMessagesCount();
            totalSentSummation += summary.getSentMessagesSummation();
            totalReceivedSummation += summary.getReceivedSummationCount();
        }
        System.out.println(String.format("%1$20s %2$40s %3$20s %4$15s %5$15s\n",
                "Sum:", Integer.toString(totalSent),
                Integer.toString(totalReceived), Long.toString(totalSentSummation),
                Long.toString(totalReceivedSummation)));
    }

    public int getSentMessagesCount() {
        return sentMessagesCount.get();
    }

    public int getReceivedMessagesCount() {
        return receivedMessagesCount.get();
    }

    public long getSentMessagesSummation() {
        return sentMessagesSummation.get();
    }

    public long getReceivedSummationCount() {
        return receivedMessagesSummation.get();
    }

    public int getRelayedMessagesCount() {
        return relayedMessagesCount.get();
    }

    /**
     * Reset statistics to zero after each new start by registry.
     */
    public void reset() {
        sentMessagesCount.set(0);
        receivedMessagesCount.set(0);
        relayedMessagesCount.set(0);
        sentMessagesSummation.set(0);
        receivedMessagesSummation.set(0);
    }

}
