package hla.queue;

import hla.rti.ReflectedAttributes;

import java.util.Comparator;

public class QueueExternalObject {
    public enum ObjectType {CASH_REGISTER}

    private ReflectedAttributes attributes;
    private ObjectType objectType;
    private Double time;

    public QueueExternalObject(ReflectedAttributes theInteraction, ObjectType eventType, Double time) {
        this.attributes = theInteraction;
        this.objectType = eventType;
        this.time = time;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public ReflectedAttributes getAttributes() {
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
