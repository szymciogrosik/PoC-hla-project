package hla.queue;

import hla.rti.ArrayIndexOutOfBounds;
import hla.rti.EventRetractionHandle;
import hla.rti.LogicalTime;
import hla.rti.ReceivedInteraction;
import hla.rti.jlc.EncodingHelpers;
import hla.tamplate.Ambassador;

import java.util.ArrayList;

public class QueueAmbassador extends Ambassador {
    protected double grantedTime         = 0.0;
    public double federateLookahead      = 1.0;

    protected int joinClientToQueueHandle = 0;
    protected int openNewCashRegisterHandle = 2;

    protected ArrayList<QueueExternalEvent> externalEvents = new ArrayList<>();

    public void timeAdvanceGrant( LogicalTime theTime )
    {
        this.grantedTime = convertTime( theTime );
        this.isAdvancing = false;
    }

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
}
