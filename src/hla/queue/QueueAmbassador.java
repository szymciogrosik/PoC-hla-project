package hla.queue;

import hla.constants.ConfigConstants;
import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.tamplate.BaseAmbassador;

import java.util.ArrayList;

public class QueueAmbassador extends BaseAmbassador {
    protected ArrayList<QueueExternalObject> externalObjects = new ArrayList<>();

    protected int joinClientToQueueHandle = 0;
    protected int openNewCashRegisterHandle = 2;

    protected ArrayList<QueueExternalEvent> externalEvents = new ArrayList<>();

    public void receiveInteraction( int interactionClass,
                                    ReceivedInteraction theInteraction,
                                    byte[] tag )
    {
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

            } catch (ArrayIndexOutOfBounds ignored) { }

        } else if (interactionClass == openNewCashRegisterHandle) {
            try {
                int qty = EncodingHelpers.decodeInt(theInteraction.getValue(0));
                double time =  convertTime(theTime);
                externalEvents.add(new QueueExternalEvent(qty, QueueExternalEvent.EventType.OPEN_NEW_CASH_REGISTER , time));
                builder.append( "OPEN_NEW_CASH_REGISTER , time=" + time );
                builder.append(" qty=").append(qty);
                builder.append( "\n" );

            } catch (ArrayIndexOutOfBounds ignored) { }
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
        String objectName = "";
        double time =  convertTime(theTime);

        try {
            objectName = rtiAmbassador.getObjectClassName(rtiAmbassador.getObjectClass(theObject));
        } catch (RTIinternalError | FederateNotExecutionMember | ObjectClassNotDefined | ObjectNotKnown rtIinternalError) {
            rtIinternalError.printStackTrace();
        }

        StringBuilder builder = new StringBuilder("Reflection for object: ");

        switch (objectName) {
            case ConfigConstants.CASH_REGISTER_OBJ_NAME:
                try {
                    externalObjects.add(new QueueExternalObject(theAttributes, QueueExternalObject.ObjectType.CASH_REGISTER , time));
                    builder.append(QueueExternalObject.ObjectType.CASH_REGISTER + ", time=").append(time);
                    builder.append(" " + ConfigConstants.CASH_REGISTER_NUMBER_NAME + "=").append(EncodingHelpers.decodeInt(theAttributes.getValue(0)));
                    builder.append(" " + ConfigConstants.CASH_REGISTER_IS_FREE_NAME + "=").append(EncodingHelpers.decodeBoolean(theAttributes.getValue(1)));
                    builder.append("\n");
                } catch (ArrayIndexOutOfBounds arrayIndexOutOfBounds) {
                    arrayIndexOutOfBounds.printStackTrace();
                }
                builder.append( "\n" );
                break;

            default:
                builder.append("Undetected interaction.");
        }

        log(builder.toString());
    }

    @Override
    public void discoverObjectInstance(int theObject, int theObjectClass, String objectName) throws ObjectClassNotKnown, FederateInternalError {
        System.out.println("Pojawil sie nowy obiekt typu SimObject " + theObject + " " + theObjectClass + " " + objectName);
    }
}
