package ieee1516e.statistic.statisticObjects;

public class StatisticClient {
    private long clientNumber;
    private double joinToQueue;
    private double exitQueue;

    public StatisticClient(long clientNumber, double joinToQueue, double exitQueue) {
        this.clientNumber = clientNumber;
        this.joinToQueue = joinToQueue;
        this.exitQueue = exitQueue;
    }

    public long getClientNumber() {
        return clientNumber;
    }

    public double getJoinToQueue() {
        return joinToQueue;
    }

    public void setJoinToQueue(double joinToQueue) {
        this.joinToQueue = joinToQueue;
    }

    public double getExitQueue() {
        return exitQueue;
    }

    public void setExitQueue(double exitQueue) {
        this.exitQueue = exitQueue;
    }
}
