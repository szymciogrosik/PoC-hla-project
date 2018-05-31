package hla.cashRegister;

import hla.rti.ArrayIndexOutOfBounds;
import hla.rti.EventRetractionHandle;
import hla.rti.LogicalTime;
import hla.rti.ReceivedInteraction;
import hla.rti.jlc.EncodingHelpers;
import hla.tamplate.BaseAmbassador;

import java.util.ArrayList;

public class CashRegisterAmbassador extends BaseAmbassador {
    protected int startHandlingClientHandle = 1;
    protected int openNewCashRegisterHandle = 2;

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
        StringBuilder builder = new StringBuilder( "Interaction Received:" );
        if(interactionClass == startHandlingClientHandle) {
            try {
                int qty = EncodingHelpers.decodeInt(theInteraction.getValue(0));
                double time =  convertTime(theTime);
                externalEvents.add(new CashRegisterExternalEvent(qty, CashRegisterExternalEvent.EventType.START_HANDLING_CLIENT , time));
                builder.append("START_HANDLING_CLIENT , time=" + time);
                builder.append(" qty=").append(qty);
                builder.append( "\n" );

            } catch (ArrayIndexOutOfBounds ignored) { }

        } else if (interactionClass == openNewCashRegisterHandle) {
            try {
                int qty = EncodingHelpers.decodeInt(theInteraction.getValue(0));
                double time =  convertTime(theTime);
                externalEvents.add(new CashRegisterExternalEvent(qty, CashRegisterExternalEvent.EventType.OPEN_NEW_CASH_REGISTER , time));
                builder.append( "OPEN_NEW_CASH_REGISTER , time=" + time );
                builder.append(" qty=").append(qty);
                builder.append( "\n" );

            } catch (ArrayIndexOutOfBounds ignored) {

            }
        }

        log( builder.toString() );
    }
}
