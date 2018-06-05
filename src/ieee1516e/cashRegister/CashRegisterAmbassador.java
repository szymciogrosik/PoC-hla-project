package ieee1516e.cashRegister;

import hla.rti1516e.*;
import hla.rti1516e.exceptions.*;
import ieee1516e.constants.ConfigConstants;
import ieee1516e.tamplate.BaseAmbassador;

import java.util.ArrayList;

public class CashRegisterAmbassador extends BaseAmbassador {
    protected ArrayList<CashRegisterExternalEvent> externalEvents = new ArrayList<>();

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
        } catch (RTIinternalError | FederateNotExecutionMember | NotConnected | InvalidInteractionClassHandle rtIinternalError) {
            rtIinternalError.printStackTrace();
        }

        StringBuilder builder = new StringBuilder( "Interaction Received: " );
        double time =  convertTime(timeReceived);

        switch (interactionName) {
            case ConfigConstants.START_HANDLING_CLIENT_INTERACTION_NAME:
                externalEvents.add(new CashRegisterExternalEvent(theParameters, CashRegisterExternalEvent.EventType.START_HANDLING_CLIENT , time));
                builder.append(CashRegisterExternalEvent.EventType.START_HANDLING_CLIENT + ", time=").append(time);
                builder.append("\n");
                break;

            case ConfigConstants.OPEN_NEW_CASH_REGISTER_INTERACTION_NAME:
                externalEvents.add(new CashRegisterExternalEvent(theParameters, CashRegisterExternalEvent.EventType.OPEN_NEW_CASH_REGISTER , time));
                builder.append(CashRegisterExternalEvent.EventType.OPEN_NEW_CASH_REGISTER + ", time=").append(time);
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
}
