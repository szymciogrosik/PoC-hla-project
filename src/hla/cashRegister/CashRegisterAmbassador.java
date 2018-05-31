package hla.cashRegister;

import hla.constants.ConfigConstants;
import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.tamplate.BaseAmbassador;

import java.util.ArrayList;

public class CashRegisterAmbassador extends BaseAmbassador {
    protected ArrayList<CashRegisterExternalEvent> externalEvents = new ArrayList<>();

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
            case ConfigConstants.START_HANDLING_CLIENT_INTERACTION_NAME:
                try {
                    externalEvents.add(new CashRegisterExternalEvent(theInteraction, CashRegisterExternalEvent.EventType.START_HANDLING_CLIENT , time));
                    builder.append(CashRegisterExternalEvent.EventType.START_HANDLING_CLIENT + ", time=").append(time);
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

            case ConfigConstants.OPEN_NEW_CASH_REGISTER_INTERACTION_NAME:
                try {
                    externalEvents.add(new CashRegisterExternalEvent(theInteraction, CashRegisterExternalEvent.EventType.OPEN_NEW_CASH_REGISTER , time));
                    builder.append(CashRegisterExternalEvent.EventType.OPEN_NEW_CASH_REGISTER + ", time=").append(time);
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
}
