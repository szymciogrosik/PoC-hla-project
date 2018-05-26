package hla.queue;

import java.util.Comparator;

public class ExternalEventQueue {
    public enum EventType {JOIN_CLIENT_TO_QUEUE, OPEN_NEW_CASH_REGISTER}

    private  int qty;
    private EventType eventType;
    private Double time;

    public ExternalEventQueue(int qty, EventType eventType, Double time) {
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

    static class ExternalEventComparator implements Comparator<ExternalEventQueue> {

        @Override
        public int compare(ExternalEventQueue o1, ExternalEventQueue o2) {
            return o1.time.compareTo(o2.time);
        }
    }
}
