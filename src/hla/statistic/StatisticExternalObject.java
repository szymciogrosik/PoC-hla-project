package hla.statistic;

        import hla.rti.ReflectedAttributes;

        import java.util.Comparator;

public class StatisticExternalObject {
    public enum ObjectType {QUEUE, CASH_REGISTER}

    private ReflectedAttributes attributes;
    private ObjectType objectType;
    private Double time;

    public StatisticExternalObject(ReflectedAttributes theInteraction, ObjectType eventType, Double time) {
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

    static class ExternalObjectComparator implements Comparator<StatisticExternalObject> {
        @Override
        public int compare(StatisticExternalObject o1, StatisticExternalObject o2) {
            return o1.time.compareTo(o2.time);
        }
    }
}
