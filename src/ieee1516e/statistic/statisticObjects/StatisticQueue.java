package ieee1516e.statistic.statisticObjects;

import java.util.ArrayList;

public class StatisticQueue {
    private long queueNumber;
    private ArrayList<StatisticClient> statisticClientsList = new ArrayList<>();

    private double counter = 0;
    private double lengthSum = 0;

    private double actualLength = 0;

    private ArrayList<LengthInTime> lengthInTimeList = new ArrayList<>();

    public StatisticQueue(long queueNumber) {
        this.queueNumber = queueNumber;
    }

    public long getQueueNumber() {
        return queueNumber;
    }

    public ArrayList<StatisticClient> getStatisticClientsList() {
        return statisticClientsList;
    }

    public double getCounter() {
        return counter;
    }

    public void setCounter(double counter) {
        this.counter = counter;
    }

    public double getLengthSum() {
        return lengthSum;
    }

    public void setLengthSum(double lengthSum) {
        this.lengthSum = lengthSum;
    }

    public double getActualLength() {
        return actualLength;
    }

    public ArrayList<LengthInTime> getLengthInTimeList() {
        return lengthInTimeList;
    }

    public void addToLengthInTime(double time, double length) {
        lengthInTimeList.add(new LengthInTime(length, time));
    }

    public void setActualLength(double actualLength) {
        this.actualLength = actualLength;
    }

    public class LengthInTime {
        private double length = 0;
        private double time = 0;

        public LengthInTime(double length, double time) {
            this.length = length;
            this.time = time;
        }

        public double getLength() {
            return length;
        }

        public double getTime() {
            return time;
        }
    }
}
