package ieee1516e.queue;

import hla.rti1516e.*;
import hla.rti1516e.exceptions.*;
import ieee1516e.constants.ConfigConstants;
import ieee1516e.tamplate.BaseAmbassador;

import java.util.ArrayList;

public class QueueAmbassador extends BaseAmbassador {
    protected ArrayList<QueueExternalEvent> externalEvents = new ArrayList<>();
    protected ArrayList<QueueExternalObject> externalObjects = new ArrayList<>();

    @Override
    public void receiveInteraction( InteractionClassHandle interactionClass,
                                    ParameterHandleValueMap theParameters,
                                    byte[] tag,
                                    OrderType sentOrdering,
                                    TransportationTypeHandle theTransport,
                                    SupplementalReceiveInfo receiveInfo )
    {
        this.receiveInteraction( interactionClass, theParameters, tag, sentOrdering, theTransport, null, sentOrdering, receiveInfo );
    }

    @Override
    public void receiveInteraction( InteractionClassHandle interactionClass,
                        ParameterHandleValueMap theParameters,
                        byte[] tag,
                        OrderType sentOrdering,
                        TransportationTypeHandle theTransport,
                        LogicalTime timeReceived,
                        OrderType receivedOrdering,
                        SupplementalReceiveInfo receiveInfo )
    {
        String interactionName = "";

        try {
            interactionName = rtiAmbassador.getInteractionClassName(interactionClass);
        } catch (InvalidInteractionClassHandle | FederateNotExecutionMember | NotConnected | RTIinternalError invalidInteractionClassHandle) {
            invalidInteractionClassHandle.printStackTrace();
        }

        log("Nazwa interakcji" + interactionName );

        StringBuilder builder = new StringBuilder( "Interaction Received: " );
        double time =  convertTime(timeReceived);

        switch (interactionName) {
            case ConfigConstants.JOIN_CLIENT_TO_QUEUE_INTERACTION_NAME:
                externalEvents.add(new QueueExternalEvent(theParameters, QueueExternalEvent.EventType.JOIN_CLIENT_TO_QUEUE , time));
                builder.append(QueueExternalEvent.EventType.JOIN_CLIENT_TO_QUEUE + ", time=").append(time);
//                    builder.append(" " + ConfigConstants.CLIENT_NUMBER_NAME + "=").append(EncodingHelpers.decodeInt(theInteraction.getValue(0)));
//                    builder.append(" " + ConfigConstants.CASH_REGISTER_NUMBER_NAME + "=").append(EncodingHelpers.decodeInt(theInteraction.getValue(1)));
//                    builder.append(" " + ConfigConstants.AMOUNT_OF_ARTICLES_NAME + "=").append(EncodingHelpers.decodeInt(theInteraction.getValue(2)));
                builder.append( "\n" );
                break;

            case ConfigConstants.OPEN_NEW_CASH_REGISTER_INTERACTION_NAME:
                externalEvents.add(new QueueExternalEvent(theParameters, QueueExternalEvent.EventType.OPEN_NEW_CASH_REGISTER , time));
                builder.append(QueueExternalEvent.EventType.OPEN_NEW_CASH_REGISTER + ", time=").append(time);
//                builder.append(" " + ConfigConstants.CASH_REGISTER_NUMBER_NAME + "=").append(EncodingHelpers.decodeInt(theInteraction.getValue(0)));
//                builder.append(" " + ConfigConstants.QUEUE_NUMBER_NAME + "=").append(EncodingHelpers.decodeInt(theInteraction.getValue(1)));
                builder.append( "\n" );
                break;

            default:
                builder.append("Undetected interaction.");
        }

        log( builder.toString() );
    }

    @Override
    public void reflectAttributeValues( ObjectInstanceHandle theObject,
                                        AttributeHandleValueMap theAttributes,
                                        byte[] tag,
                                        OrderType sentOrder,
                                        TransportationTypeHandle transport,
                                        SupplementalReflectInfo reflectInfo ) {
        reflectAttributeValues( theObject, theAttributes, tag, sentOrder, transport, null, sentOrder, reflectInfo );
    }

    @Override
    public void reflectAttributeValues( ObjectInstanceHandle theObject,
                                        AttributeHandleValueMap theAttributes,
                                        byte[] tag,
                                        OrderType sentOrdering,
                                        TransportationTypeHandle theTransport,
                                        LogicalTime timeReceived,
                                        OrderType receivedOrdering,
                                        SupplementalReflectInfo reflectInfo ) {
        String objectName = "";
        double time =  convertTime(timeReceived);

        try {
            objectName = rtiAmbassador.getObjectClassName((ObjectClassHandle) theObject);
        } catch (RTIinternalError | FederateNotExecutionMember | InvalidObjectClassHandle | NotConnected rtIinternalError) {
            rtIinternalError.printStackTrace();
            return;
        }

        StringBuilder builder = new StringBuilder("Reflection for object: ");

        switch (objectName) {
            case ConfigConstants.CASH_REGISTER_OBJ_NAME:
                externalObjects.add(new QueueExternalObject(theAttributes, QueueExternalObject.ObjectType.CASH_REGISTER , time));
                builder.append(QueueExternalObject.ObjectType.CASH_REGISTER + ", time=").append(time);
//                    builder.append(" " + ConfigConstants.CASH_REGISTER_NUMBER_NAME + "=").append(EncodingHelpers.decodeInt(theAttributes.getValue(0)));
//                    builder.append(" " + ConfigConstants.CASH_REGISTER_IS_FREE_NAME + "=").append(EncodingHelpers.decodeBoolean(theAttributes.getValue(1)));
                builder.append("\n");
                break;

            default:
                builder.append("Undetected interaction.");
        }

        log(builder.toString());
    }

    @Override
    public void discoverObjectInstance( ObjectInstanceHandle theObject,
                                        ObjectClassHandle theObjectClass,
                                        String objectName ) {
        String objName = "";
        try {
            objName = rtiAmbassador.getObjectClassName((ObjectClassHandle) theObject);
        } catch (RTIinternalError | FederateNotExecutionMember | InvalidObjectClassHandle | NotConnected rtIinternalError) {
            rtIinternalError.printStackTrace();
        }
        System.out.println("Pojawil sie nowy obiekt typu SimObject: " + objName + ".");
    }
}
