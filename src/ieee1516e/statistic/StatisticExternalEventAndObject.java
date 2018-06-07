package ieee1516e.statistic;

import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.ParameterHandleValueMap;

import java.util.Comparator;

public class StatisticExternalEventAndObject {
    public enum EventType {START_HANDLING_CLIENT, JOIN_CLIENT_TO_QUEUE, QUEUE, CASH_REGISTER}

    private ParameterHandleValueMap interactionAttributes;
    private AttributeHandleValueMap objectAttributes;
    private EventType eventType;
    private Double time;

    public StatisticExternalEventAndObject(ParameterHandleValueMap interactionAttributes, EventType eventType, Double time) {
        this.interactionAttributes = interactionAttributes;
        this.eventType = eventType;
        this.time = time;
    }

    public StatisticExternalEventAndObject(AttributeHandleValueMap objectAttributes, EventType eventType, Double time) {
        this.objectAttributes = objectAttributes;
        this.eventType = eventType;
        this.time = time;
    }

    public EventType getEventType() {
        return eventType;
    }

    public AttributeHandleValueMap getObjectAttributes() {
        return objectAttributes;
    }

    public ParameterHandleValueMap getInteractionAttributes() { return interactionAttributes; }

    public double getTime() {
        return time;
    }

    static class ExternalEventComparator implements Comparator<StatisticExternalEventAndObject> {
        @Override
        public int compare(StatisticExternalEventAndObject o1, StatisticExternalEventAndObject o2) {
            return o1.time.compareTo(o2.time);
        }
    }
}
