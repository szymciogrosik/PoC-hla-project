package ieee1516e.queue;

import hla.rti1516e.*;
import hla.rti1516e.exceptions.*;
import ieee1516e.constants.ConfigConstants;
import ieee1516e.tamplate.BaseAmbassador;

import java.util.ArrayList;

public class QueueAmbassador extends BaseAmbassador {
    protected ArrayList<QueueExternalEventAndObject> externalEventsAndObjects = new ArrayList<>();

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

        StringBuilder builder = new StringBuilder( "Interaction Received: " );
        double time =  convertTime(timeReceived);

        switch (interactionName) {
            case ConfigConstants.JOIN_CLIENT_TO_QUEUE_INTERACTION_NAME:
                externalEventsAndObjects.add(new QueueExternalEventAndObject(theParameters, QueueExternalEventAndObject.EventType.JOIN_CLIENT_TO_QUEUE , time));
                builder.append(QueueExternalEventAndObject.EventType.JOIN_CLIENT_TO_QUEUE + ", time=").append(time);
                builder.append( "\n" );
                break;

            case ConfigConstants.OPEN_NEW_CASH_REGISTER_INTERACTION_NAME:
                externalEventsAndObjects.add(new QueueExternalEventAndObject(theParameters, QueueExternalEventAndObject.EventType.OPEN_NEW_CASH_REGISTER , time));
                builder.append(QueueExternalEventAndObject.EventType.OPEN_NEW_CASH_REGISTER + ", time=").append(time);
                builder.append( "\n" );
                break;

            case ConfigConstants.END_SIMULATION_INTERACTION_NAME:
                builder.append("END_SIMULATION" + ", time=").append(time);
                builder.append( "\n" );
                this.running = false;
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
            objectName = rtiAmbassador.getObjectClassName(rtiAmbassador.getKnownObjectClassHandle(theObject));
        } catch (RTIinternalError | FederateNotExecutionMember | InvalidObjectClassHandle | NotConnected | ObjectInstanceNotKnown rtIinternalError) {
            rtIinternalError.printStackTrace();
            return;
        }

        StringBuilder builder = new StringBuilder("Reflection for object: ");

        switch (objectName) {
            case ConfigConstants.CASH_REGISTER_OBJ_NAME:
                externalEventsAndObjects.add(new QueueExternalEventAndObject(theAttributes, QueueExternalEventAndObject.EventType.CASH_REGISTER , time));
                builder.append(QueueExternalEventAndObject.EventType.CASH_REGISTER + ", time=").append(time);
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
            objName = rtiAmbassador.getObjectClassName(rtiAmbassador.getKnownObjectClassHandle(theObject));
        } catch (RTIinternalError | FederateNotExecutionMember | InvalidObjectClassHandle | NotConnected | ObjectInstanceNotKnown rtIinternalError) {
            rtIinternalError.printStackTrace();
            return;
        }
        System.out.println("Pojawil sie nowy obiekt typu SimObject: " + objName + ".");
    }
}
