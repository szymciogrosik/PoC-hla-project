package hla.queue;

import hla.constants.ConfigConstants;
import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.tamplate.BaseAmbassador;

import java.util.ArrayList;

public class QueueAmbassador extends BaseAmbassador {
    protected ArrayList<QueueExternalEvent> externalEvents = new ArrayList<>();
    protected ArrayList<QueueExternalObject> externalObjects = new ArrayList<>();

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
        String interactionName = "";

        try {
            interactionName = rtiAmbassador.getInteractionClassName(interactionClass);
        } catch (RTIinternalError | FederateNotExecutionMember | InteractionClassNotDefined rtIinternalError) {
            rtIinternalError.printStackTrace();
        }

        StringBuilder builder = new StringBuilder( "Interaction Received: " );
        double time =  convertTime(theTime);

        switch (interactionName) {
            case ConfigConstants.JOIN_CLIENT_TO_QUEUE_INTERACTION_NAME:
                try {
                    externalEvents.add(new QueueExternalEvent(theInteraction, QueueExternalEvent.EventType.JOIN_CLIENT_TO_QUEUE , time));
                    builder.append(QueueExternalEvent.EventType.JOIN_CLIENT_TO_QUEUE + ", time=").append(time);
                    builder.append(" " + ConfigConstants.CLIENT_NUMBER_NAME + "=").append(EncodingHelpers.decodeInt(theInteraction.getValue(0)));
                    builder.append(" " + ConfigConstants.CASH_REGISTER_NUMBER_NAME + "=").append(EncodingHelpers.decodeInt(theInteraction.getValue(1)));
                    builder.append(" " + ConfigConstants.AMOUNT_OF_ARTICLES_NAME + "=").append(EncodingHelpers.decodeInt(theInteraction.getValue(2)));
                    builder.append( "\n" );
                } catch (ArrayIndexOutOfBounds arrayIndexOutOfBounds) {
                    arrayIndexOutOfBounds.printStackTrace();
                }
                break;

            case ConfigConstants.OPEN_NEW_CASH_REGISTER_INTERACTION_NAME:
                try {
                    externalEvents.add(new QueueExternalEvent(theInteraction, QueueExternalEvent.EventType.OPEN_NEW_CASH_REGISTER , time));
                    builder.append(QueueExternalEvent.EventType.OPEN_NEW_CASH_REGISTER + ", time=").append(time);
                    builder.append(" " + ConfigConstants.CASH_REGISTER_NUMBER_NAME + "=").append(EncodingHelpers.decodeInt(theInteraction.getValue(0)));
                    builder.append(" " + ConfigConstants.QUEUE_NUMBER_NAME + "=").append(EncodingHelpers.decodeInt(theInteraction.getValue(1)));
                    builder.append( "\n" );
                } catch (ArrayIndexOutOfBounds arrayIndexOutOfBounds) {
                    arrayIndexOutOfBounds.printStackTrace();
                }
                break;

            default:
                builder.append("Undetected interaction.");
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
        String objName = "";
        try {
            objName = rtiAmbassador.getObjectClassName(rtiAmbassador.getObjectClass(theObject));
        } catch (RTIinternalError | FederateNotExecutionMember | ObjectClassNotDefined | ObjectNotKnown rtIinternalError) {
            rtIinternalError.printStackTrace();
        }
        System.out.println("Pojawil sie nowy obiekt typu SimObject: " + objName + ".");
    }
}
