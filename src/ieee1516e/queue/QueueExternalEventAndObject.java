package ieee1516e.queue;

import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.ParameterHandleValueMap;

import java.util.Comparator;

public class QueueExternalEventAndObject {
    public enum EventType {JOIN_CLIENT_TO_QUEUE, OPEN_NEW_CASH_REGISTER, CASH_REGISTER}

    private ParameterHandleValueMap interactionAttributes;
    private AttributeHandleValueMap objectAttributes;
    private EventType eventType;
    private Double time;

    public QueueExternalEventAndObject(ParameterHandleValueMap interactionAttributes, EventType eventType, Double time) {
        this.interactionAttributes = interactionAttributes;
        this.eventType = eventType;
        this.time = time;
    }

    public QueueExternalEventAndObject(AttributeHandleValueMap objectAttributes, EventType eventType, Double time) {
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

    static class ExternalEventComparator implements Comparator<QueueExternalEventAndObject> {
        @Override
        public int compare(QueueExternalEventAndObject o1, QueueExternalEventAndObject o2) {
            return o1.time.compareTo(o2.time);
        }
    }
}
