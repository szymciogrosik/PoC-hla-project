package ieee1516e.tamplate;

import hla.rti1516e.*;
import hla.rti1516e.time.HLAfloat64Time;
import ieee1516e.constants.ConfigConstants;

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
        return ((HLAfloat64Time)logicalTime).getValue();
    }

    protected void log(String message)
    {
        System.out.println( "FederateAmbassador: " + message );
    }

    @Override
    public void synchronizationPointRegistrationFailed( String label, SynchronizationPointFailureReason reason  )
    {
        log( "Failed to register sync point: " + label );
    }

    @Override
    public void synchronizationPointRegistrationSucceeded( String label )
    {
        log( "Successfully registered sync point: " + label );
    }

    @Override
    public void announceSynchronizationPoint( String label, byte[] tag )
    {
        log( "Synchronization point announced: " + label );
        if( label.equals(ConfigConstants.READY_TO_RUN) )
            this.isAnnounced = true;
    }
    @Override
    public void federationSynchronized( String label, FederateHandleSet failed  )
    {
        log( "Federation Synchronized: " + label );
        if( label.equals(ConfigConstants.READY_TO_RUN) )
            this.isReadyToRun = true;
    }

    @Override
    public void timeRegulationEnabled( LogicalTime theFederateTime )
    {
        this.federateTime = convertTime( theFederateTime );
        this.isRegulating = true;
    }

    @Override
    public void timeConstrainedEnabled( LogicalTime theFederateTime )
    {
        this.federateTime = convertTime( theFederateTime );
        this.isConstrained = true;
    }

    public void setRtiAmbassador(RTIambassador rtiAmbassador) {
        this.rtiAmbassador = rtiAmbassador;
    }
}
