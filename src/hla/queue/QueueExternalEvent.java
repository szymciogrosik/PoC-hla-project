package hla.queue;

import hla.rti.ReceivedInteraction;

import java.util.Comparator;

public class QueueExternalEvent {
    public enum EventType {JOIN_CLIENT_TO_QUEUE, OPEN_NEW_CASH_REGISTER}

    private ReceivedInteraction theAttributes;
    private EventType eventType;
    private Double time;

    public QueueExternalEvent(ReceivedInteraction theAttributes, EventType eventType, Double time) {
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

    static class ExternalEventComparator implements Comparator<QueueExternalEvent> {
        @Override
        public int compare(QueueExternalEvent o1, QueueExternalEvent o2) {
            return o1.time.compareTo(o2.time);
        }
    }
}
