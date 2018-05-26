package hla.tamplate;

import hla.constants.ConfigConstants;
import hla.rti.*;
import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.DoubleTimeInterval;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Random;

public abstract class Federate {
    protected RTIambassador rtiamb;
    protected final double timeStep           = 10.0;

    protected void log( String message )
    {
        System.out.println( ConfigConstants.QUEUE_FED + "   : " + message );
    }

    protected void waitForUser()
    {
        log( " >>>>>>>>>> Press Enter to Continue <<<<<<<<<<" );
        BufferedReader reader = new BufferedReader( new InputStreamReader(System.in) );
        try
        {
            reader.readLine();
        }
        catch( Exception e )
        {
            log( "Error while waiting for user input: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    protected void tryCreateFederation() throws ConcurrentAccessAttempted, RTIinternalError, CouldNotOpenFED, ErrorReadingFED {
        try
        {
            File fom = new File(ConfigConstants.FEDERATION_FILE_PATH);
            rtiamb.createFederationExecution( ConfigConstants.FEDERATION_NAME,
                    fom.toURI().toURL() );
            log( "Created Federation" );
        }
        catch( FederationExecutionAlreadyExists exists )
        {
            log( "Didn't create federation, it already existed" );
        }
        catch( MalformedURLException urle )
        {
            log( "Exception processing fom: " + urle.getMessage() );
            urle.printStackTrace();
            return;
        }
    }

    protected double randomTime() {
        Random r = new Random();
        return 1 +(4 * r.nextDouble());
    }

    protected LogicalTime convertTime(double time )
    {
        // PORTICO SPECIFIC!!
        return new DoubleTime( time );
    }

    /**
     * Same as for {@link #convertTime(double)}
     */
    protected LogicalTimeInterval convertInterval(double time )
    {
        // PORTICO SPECIFIC!!
        return new DoubleTimeInterval( time );
    }

    protected void advanceTime(double timestep, Ambassador fedamb) throws RTIexception
    {
        log("requesting time advance for: " + timestep);
        // request the advance
        fedamb.isAdvancing = true;
        LogicalTime newTime = convertTime( fedamb.federateTime + timestep );
        rtiamb.timeAdvanceRequest( newTime );
        while( fedamb.isAdvancing )
        {
            rtiamb.tick();
        }
    }

    protected void enableTimePolicy(Ambassador fedamb) throws RTIexception
    {
        LogicalTime currentTime = convertTime( fedamb.federateTime );
        LogicalTimeInterval lookahead = convertInterval( fedamb.federateLookahead );

        this.rtiamb.enableTimeRegulation( currentTime, lookahead );

        while( fedamb.isRegulating == false )
        {
            rtiamb.tick();
        }

        this.rtiamb.enableTimeConstrained();

        while( fedamb.isConstrained == false )
        {
            rtiamb.tick();
        }
    }
}
