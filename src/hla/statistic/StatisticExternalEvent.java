package hla.statistic;

import java.util.Comparator;

public class StatisticExternalEvent {
    public enum EventType {START_HANDLING_CLIENT, JOIN_CLIENT_TO_QUEUE}

    private  int qty;
    private EventType eventType;
    private Double time;

    public StatisticExternalEvent(int qty, EventType eventType, Double time) {
        this.qty = qty;
        this.eventType = eventType;
        this.time = time;
    }

    public EventType getEventType() {
        return eventType;
    }

    public int getQty() {
        return qty;
    }

    public double getTime() {
        return time;
    }

    static class ExternalEventComparator implements Comparator<StatisticExternalEvent> {
        @Override
        public int compare(StatisticExternalEvent o1, StatisticExternalEvent o2) {
            return o1.time.compareTo(o2.time);
        }
    }
}
