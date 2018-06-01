package ieee1516e.cashRegister;

import hla.rti1516e.ParameterHandleValueMap;

import java.util.Comparator;

public class CashRegisterExternalEvent {
    public enum EventType {START_HANDLING_CLIENT, OPEN_NEW_CASH_REGISTER}

    private ParameterHandleValueMap attributes;
    private EventType eventType;
    private Double time;

    public CashRegisterExternalEvent(ParameterHandleValueMap theAttributes, EventType eventType, Double time) {
        this.attributes = theAttributes;
        this.eventType = eventType;
        this.time = time;
    }

    public EventType getEventType() {
        return eventType;
    }

    public ParameterHandleValueMap getAttributes() {
        return attributes;
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
