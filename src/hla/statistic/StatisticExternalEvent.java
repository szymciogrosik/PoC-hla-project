package hla.statistic;

import hla.rti.ReceivedInteraction;

import java.util.Comparator;

public class StatisticExternalEvent {
    public enum EventType {START_HANDLING_CLIENT, JOIN_CLIENT_TO_QUEUE}

    private ReceivedInteraction theAttributes;
    private EventType eventType;
    private Double time;

    public StatisticExternalEvent(ReceivedInteraction theAttributes, EventType eventType, Double time) {
        this.theAttributes = theAttributes;
        this.eventType = eventType;
        this.time = time;
    }

    public EventType getEventType() {
        return eventType;
    }

    public ReceivedInteraction getAttributes() {
        return theAttributes;
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
