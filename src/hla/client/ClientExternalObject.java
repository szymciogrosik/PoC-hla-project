package hla.client;

import hla.rti.ReflectedAttributes;

import java.util.Comparator;

public class ClientExternalObject {
    public enum ObjectType {QUEUE}

    private ReflectedAttributes attributes;
    private ObjectType objectType;
    private Double time;

    public ClientExternalObject(ReflectedAttributes theInteraction, ObjectType eventType, Double time) {
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

    static class ExternalObjectComparator implements Comparator<ClientExternalObject> {
        @Override
        public int compare(ClientExternalObject o1, ClientExternalObject o2) {
            return o1.time.compareTo(o2.time);
        }
    }
}
