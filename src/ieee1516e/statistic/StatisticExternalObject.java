package ieee1516e.statistic;

import hla.rti1516e.AttributeHandleValueMap;

import java.util.Comparator;

public class StatisticExternalObject {
    public enum ObjectType {QUEUE, CASH_REGISTER}

    private AttributeHandleValueMap attributes;
    private ObjectType objectType;
    private Double time;

    public StatisticExternalObject(AttributeHandleValueMap theInteraction, ObjectType eventType, Double time) {
        this.attributes = theInteraction;
        this.objectType = eventType;
        this.time = time;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public AttributeHandleValueMap getAttributes() {
        return attributes;
    }

    public double getTime() {
        return time;
    }

    static class ExternalObjectComparator implements Comparator<StatisticExternalObject> {
        @Override
        public int compare(StatisticExternalObject o1, StatisticExternalObject o2) {
            return o1.time.compareTo(o2.time);
        }
    }
}
