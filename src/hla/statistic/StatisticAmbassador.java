package hla.statistic;

import hla.constants.ConfigConstants;
import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.tamplate.BaseAmbassador;

import java.util.ArrayList;

public class StatisticAmbassador extends BaseAmbassador {
    protected ArrayList<StatisticExternalEvent> externalEvents = new ArrayList<>();
    protected ArrayList<StatisticExternalObject> externalObjects = new ArrayList<>();

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
                    externalEvents.add(new StatisticExternalEvent(theInteraction, StatisticExternalEvent.EventType.JOIN_CLIENT_TO_QUEUE , time));
                    builder.append(StatisticExternalEvent.EventType.JOIN_CLIENT_TO_QUEUE + ", time=").append(time);
                    builder.append(" " + ConfigConstants.CLIENT_NUMBER_NAME + "=").append(EncodingHelpers.decodeInt(theInteraction.getValue(0)));
                    builder.append(" " + ConfigConstants.CASH_REGISTER_NUMBER_NAME + "=").append(EncodingHelpers.decodeInt(theInteraction.getValue(1)));
                    builder.append(" " + ConfigConstants.AMOUNT_OF_ARTICLES_NAME + "=").append(EncodingHelpers.decodeInt(theInteraction.getValue(2)));
                    builder.append( "\n" );
                } catch (ArrayIndexOutOfBounds arrayIndexOutOfBounds) {
                    arrayIndexOutOfBounds.printStackTrace();
                }
                break;

            case ConfigConstants.START_HANDLING_CLIENT_INTERACTION_NAME:
                try {
                    externalEvents.add(new StatisticExternalEvent(theInteraction, StatisticExternalEvent.EventType.START_HANDLING_CLIENT , time));
                    builder.append(StatisticExternalEvent.EventType.START_HANDLING_CLIENT + ", time=").append(time);
                    builder.append(" " + ConfigConstants.QUEUE_NUMBER_NAME + "=").append(EncodingHelpers.decodeInt(theInteraction.getValue(0)));
                    builder.append(" " + ConfigConstants.CASH_REGISTER_NUMBER_NAME + "=").append(EncodingHelpers.decodeInt(theInteraction.getValue(1)));
                    builder.append(" " + ConfigConstants.CLIENT_NUMBER_NAME + "=").append(EncodingHelpers.decodeInt(theInteraction.getValue(2)));
                    builder.append(" " + ConfigConstants.AMOUNT_OF_ARTICLES_NAME + "=").append(EncodingHelpers.decodeInt(theInteraction.getValue(3)));
                    builder.append("\n");
                } catch (ArrayIndexOutOfBounds arrayIndexOutOfBounds) {
                    arrayIndexOutOfBounds.printStackTrace();
                }
                builder.append( "\n" );
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
            case ConfigConstants.QUEUE_OBJ_NAME:
                try {
                    externalObjects.add(new StatisticExternalObject(theAttributes, StatisticExternalObject.ObjectType.QUEUE , time));
                    builder.append(StatisticExternalObject.ObjectType.QUEUE + ", time=").append(time);
                    builder.append(" " + ConfigConstants.QUEUE_NUMBER_NAME + "=").append(EncodingHelpers.decodeInt(theAttributes.getValue(0)));
                    builder.append(" " + ConfigConstants.CASH_REGISTER_NUMBER_NAME + "=").append(EncodingHelpers.decodeInt(theAttributes.getValue(1)));
                    builder.append(" " + ConfigConstants.QUEUE_LENGTH_NAME + "=").append(EncodingHelpers.decodeInt(theAttributes.getValue(2)));
                    builder.append( "\n" );
                } catch (ArrayIndexOutOfBounds arrayIndexOutOfBounds) {
                    arrayIndexOutOfBounds.printStackTrace();
                }
                break;

            case ConfigConstants.CASH_REGISTER_OBJ_NAME:
                try {
                    externalObjects.add(new StatisticExternalObject(theAttributes, StatisticExternalObject.ObjectType.CASH_REGISTER , time));
                    builder.append(StatisticExternalObject.ObjectType.CASH_REGISTER + ", time=").append(time);
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
    public void discoverObjectInstance(int theObject, int theObjectClass, String objectName) {
        String objName = "";
        try {
            objName = rtiAmbassador.getObjectClassName(rtiAmbassador.getObjectClass(theObject));
        } catch (RTIinternalError | FederateNotExecutionMember | ObjectClassNotDefined | ObjectNotKnown rtIinternalError) {
            rtIinternalError.printStackTrace();
        }
        System.out.println("Pojawil sie nowy obiekt typu SimObject: " + objName + ".");
    }
}
