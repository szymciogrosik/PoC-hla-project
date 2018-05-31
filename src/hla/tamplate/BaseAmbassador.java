package hla.tamplate;

import hla.constants.ConfigConstants;
import hla.rti.LogicalTime;
import hla.rti.RTIambassador;
import hla.rti.jlc.NullFederateAmbassador;
import org.portico.impl.hla13.types.DoubleTime;

public abstract class BaseAmbassador extends NullFederateAmbassador {
    protected RTIambassador rtiAmbassador  = null;
    public double federateTime          = 0.0;
    public double federateLookahead     = 1.0;
    public double grantedTime           = 0.0;

    public boolean isRegulating         = false;
    public boolean isConstrained        = false;
    public boolean isAdvancing          = false;

    public boolean isAnnounced          = false;
    public boolean isReadyToRun         = false;

    public boolean running 			    = true;

    public void timeAdvanceGrant( LogicalTime theTime )
    {
        this.grantedTime = convertTime( theTime );
        this.isAdvancing = false;
    }

    protected double convertTime(LogicalTime logicalTime)
    {
        // PORTICO SPECIFIC!!
        return ((DoubleTime)logicalTime).getTime();
    }

    protected void log(String message)
    {
        System.out.println( "FederateAmbassador: " + message );
    }

    public void synchronizationPointRegistrationFailed( String label )
    {
        log( "Failed to register sync point: " + label );
    }

    public void synchronizationPointRegistrationSucceeded( String label )
    {
        log( "Successfully registered sync point: " + label );
    }

    public void announceSynchronizationPoint( String label, byte[] tag )
    {
        log( "Synchronization point announced: " + label );
        if( label.equals(ConfigConstants.READY_TO_RUN) )
            this.isAnnounced = true;
    }

    public void federationSynchronized( String label )
    {
        log( "Federation Synchronized: " + label );
        if( label.equals(ConfigConstants.READY_TO_RUN) )
            this.isReadyToRun = true;
    }

    /**
     * The RTI has informed us that time regulation is now enabled.
     */
    public void timeRegulationEnabled( LogicalTime theFederateTime )
    {
        this.federateTime = convertTime( theFederateTime );
        this.isRegulating = true;
    }

    public void timeConstrainedEnabled( LogicalTime theFederateTime )
    {
        this.federateTime = convertTime( theFederateTime );
        this.isConstrained = true;
    }

    public void setRtiAmbassador(RTIambassador rtiAmbassador) {
        this.rtiAmbassador = rtiAmbassador;
    }
}
