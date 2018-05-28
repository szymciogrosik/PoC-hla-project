package hla.queue;

import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.tamplate.BaseAmbassador;

import java.util.ArrayList;

public class QueueAmbassador extends BaseAmbassador {
    protected int joinClientToQueueHandle = 0;
    protected int openNewCashRegisterHandle = 2;

    protected ArrayList<QueueExternalEvent> externalEvents = new ArrayList<>();

    public void receiveInteraction( int interactionClass,
                                    ReceivedInteraction theInteraction,
                                    byte[] tag )
    {
        // just pass it on to the other method for printing purposes
        // passing null as the time will let the other method know it
        // it from us, not from the RTI
        receiveInteraction(interactionClass, theInteraction, tag, null, null);
    }

    public void receiveInteraction( int interactionClass,
                                    ReceivedInteraction theInteraction,
                                    byte[] tag,
                                    LogicalTime theTime,
                                    EventRetractionHandle eventRetractionHandle )
    {
        StringBuilder builder = new StringBuilder( "Interaction Received:" );
        if(interactionClass == joinClientToQueueHandle) {
            try {
                int qty = EncodingHelpers.decodeInt(theInteraction.getValue(0));
                double time =  convertTime(theTime);
                externalEvents.add(new QueueExternalEvent(qty, QueueExternalEvent.EventType.JOIN_CLIENT_TO_QUEUE , time));
                builder.append("JOIN_CLIENT_TO_QUEUE , time=" + time);
                builder.append(" qty=").append(qty);
                builder.append( "\n" );

            } catch (ArrayIndexOutOfBounds ignored) {

            }

        } else if (interactionClass == openNewCashRegisterHandle) {
            try {
                int qty = EncodingHelpers.decodeInt(theInteraction.getValue(0));
                double time =  convertTime(theTime);
                externalEvents.add(new QueueExternalEvent(qty, QueueExternalEvent.EventType.OPEN_NEW_CASH_REGISTER , time));
                builder.append( "OPEN_NEW_CASH_REGISTER , time=" + time );
                builder.append(" qty=").append(qty);
                builder.append( "\n" );

            } catch (ArrayIndexOutOfBounds ignored) {

            }
        }

        log( builder.toString() );
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
                if(i != 1)
                    builder.append(EncodingHelpers.decodeInt(theAttributes
                        .getValue(i)));
                else
                    builder.append(EncodingHelpers.decodeBoolean(theAttributes
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
    public void discoverObjectInstance(int theObject, int theObjectClass, String objectName) throws ObjectClassNotKnown, FederateInternalError {
        System.out.println("Pojawil sie nowy obiekt typu SimObject");
    }
}
