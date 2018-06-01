package ieee1516e.statistic;

import hla.rti1516e.*;
import hla.rti1516e.exceptions.*;
import ieee1516e.constants.ConfigConstants;
import ieee1516e.tamplate.BaseAmbassador;

import java.util.ArrayList;

public class StatisticAmbassador extends BaseAmbassador {
    protected ArrayList<StatisticExternalEvent> externalEvents = new ArrayList<>();
    protected ArrayList<StatisticExternalObject> externalObjects = new ArrayList<>();

    @Override
    public void receiveInteraction( InteractionClassHandle interactionClass,
                                    ParameterHandleValueMap theParameters,
                                    byte[] tag,
                                    OrderType sentOrdering,
                                    TransportationTypeHandle theTransport,
                                    hla.rti1516e.FederateAmbassador.SupplementalReceiveInfo receiveInfo )
    {
        this.receiveInteraction( interactionClass, theParameters, tag, sentOrdering, theTransport, null, sentOrdering, receiveInfo );
    }

    @Override
    public void receiveInteraction( InteractionClassHandle interactionClass,
                                    ParameterHandleValueMap theParameters,
                                    byte[] tag,
                                    OrderType sentOrdering,
                                    TransportationTypeHandle theTransport,
                                    hla.rti1516e.LogicalTime timeReceived,
                                    OrderType receivedOrdering,
                                    hla.rti1516e.FederateAmbassador.SupplementalReceiveInfo receiveInfo )
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
                externalEvents.add(new StatisticExternalEvent(theParameters, StatisticExternalEvent.EventType.JOIN_CLIENT_TO_QUEUE , time));
                builder.append(StatisticExternalEvent.EventType.JOIN_CLIENT_TO_QUEUE + ", time=").append(time);
                builder.append("\n");
                break;

            case ConfigConstants.START_HANDLING_CLIENT_INTERACTION_NAME:
                externalEvents.add(new StatisticExternalEvent(theParameters, StatisticExternalEvent.EventType.START_HANDLING_CLIENT , time));
                builder.append(StatisticExternalEvent.EventType.START_HANDLING_CLIENT + ", time=").append(time);
                builder.append("\n");
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
            case ConfigConstants.QUEUE_OBJ_NAME:
                    externalObjects.add(new StatisticExternalObject(theAttributes, StatisticExternalObject.ObjectType.QUEUE , time));
                    builder.append(StatisticExternalObject.ObjectType.QUEUE + ", time=").append(time);
                    builder.append( "\n" );
                break;

            case ConfigConstants.CASH_REGISTER_OBJ_NAME:
                externalObjects.add(new StatisticExternalObject(theAttributes, StatisticExternalObject.ObjectType.CASH_REGISTER , time));
                builder.append(StatisticExternalObject.ObjectType.CASH_REGISTER + ", time=").append(time);
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
