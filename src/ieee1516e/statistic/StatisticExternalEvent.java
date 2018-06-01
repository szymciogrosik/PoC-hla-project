package ieee1516e.statistic;

import hla.rti1516e.ParameterHandleValueMap;

import java.util.Comparator;

public class StatisticExternalEvent {
    public enum EventType {START_HANDLING_CLIENT, JOIN_CLIENT_TO_QUEUE}

    private ParameterHandleValueMap theAttributes;
    private EventType eventType;
    private Double time;

    public StatisticExternalEvent(ParameterHandleValueMap theAttributes, EventType eventType, Double time) {
        this.theAttributes = theAttributes;
        this.eventType = eventType;
        this.time = time;
    }

    public EventType getEventType() {
        return eventType;
    }

    public ParameterHandleValueMap getAttributes() {
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
