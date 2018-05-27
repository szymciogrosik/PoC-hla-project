package hla.client;

import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.tamplate.Ambassador;

public class ClientAmbassador extends Ambassador {
    protected double grantedTime         = 0.0;
    public double federateLookahead      = 1.0;

    public void timeAdvanceGrant( LogicalTime theTime )
    {
        this.grantedTime = convertTime( theTime );
//        this.federateTime = convertTime( theTime );
        this.isAdvancing = false;
    }

    public void reflectAttributeValues(int theObject,
                                       ReflectedAttributes theAttributes, byte[] tag) {
        reflectAttributeValues(theObject, theAttributes, tag, null, null);
    }

    public void reflectAttributeValues(int theObject,
                                       ReflectedAttributes theAttributes, byte[] tag, LogicalTime theTime,
                                       EventRetractionHandle retractionHandle) {
        StringBuilder builder = new StringBuilder("Reflection for object:");

        builder.append(" handle=" + theObject);
//		builder.append(", tag=" + EncodingHelpers.decodeString(tag));

        // print the attribute information
        builder.append(", attributeCount=" + theAttributes.size());
        builder.append("\n");
        for (int i = 0; i < theAttributes.size(); i++) {
            try {
                // print the attibute handle
                builder.append("\tattributeHandle=");
                builder.append(theAttributes.getAttributeHandle(i));
                // print the attribute value
                builder.append(", attributeValue=");
                builder.append(EncodingHelpers.decodeInt(theAttributes
                        .getValue(i)));
                builder.append(", time=");
                builder.append(theTime);
                builder.append("\n");
            } catch (ArrayIndexOutOfBounds aioob) {
                // won't happen
            }
        }

        log(builder.toString());
    }

    @Override
    public void discoverObjectInstance(int theObject, int theObjectClass, String objectName) throws CouldNotDiscover, ObjectClassNotKnown, FederateInternalError {
        System.out.println("Pojawil sie nowy obiekt typu SimObject");
    }
}
