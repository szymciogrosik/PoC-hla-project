package ieee1516e.queue;

import hla.rti1516e.AttributeHandleValueMap;

import java.util.Comparator;

public class QueueExternalObject {
    public enum ObjectType {CASH_REGISTER}

    private AttributeHandleValueMap attributes;
    private ObjectType objectType;
    private Double time;

    public QueueExternalObject(AttributeHandleValueMap theInteraction, ObjectType eventType, Double time) {
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

    static class ExternalObjectComparator implements Comparator<QueueExternalObject> {
        @Override
        public int compare(QueueExternalObject o1, QueueExternalObject o2) {
            return o1.time.compareTo(o2.time);
        }
    }
}
