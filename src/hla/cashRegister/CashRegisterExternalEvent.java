package hla.cashRegister;

import java.util.Comparator;

public class CashRegisterExternalEvent {
    public enum EventType {START_HANDLING_CLIENT, OPEN_NEW_CASH_REGISTER}

    private  int qty;
    private EventType eventType;
    private Double time;

    public CashRegisterExternalEvent(int qty, EventType eventType, Double time) {
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

    static class ExternalEventComparator implements Comparator<CashRegisterExternalEvent> {
        @Override
        public int compare(CashRegisterExternalEvent o1, CashRegisterExternalEvent o2) {
            return o1.time.compareTo(o2.time);
        }
    }
}
