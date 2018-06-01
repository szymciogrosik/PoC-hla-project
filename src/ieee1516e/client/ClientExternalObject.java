package ieee1516e.client;

import hla.rti1516e.AttributeHandleValueMap;

import java.util.Comparator;

public class ClientExternalObject {
    public enum ObjectType {QUEUE}

    private AttributeHandleValueMap attributes;
    private ObjectType objectType;
    private Double time;

    public ClientExternalObject(AttributeHandleValueMap theInteraction, ObjectType eventType, Double time) {
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

    static class ExternalObjectComparator implements Comparator<ClientExternalObject> {
        @Override
        public int compare(ClientExternalObject o1, ClientExternalObject o2) {
            return o1.time.compareTo(o2.time);
        }
    }
}
