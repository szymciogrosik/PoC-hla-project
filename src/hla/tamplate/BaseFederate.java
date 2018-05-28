package hla.tamplate;

import hla.constants.ConfigConstants;
import hla.rti.*;
import hla.rti.jlc.RtiFactoryFactory;
import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.DoubleTimeInterval;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Random;

public abstract class BaseFederate<T extends BaseAmbassador> {
    protected RTIambassador rtiamb;
    protected T fedamb;

    private String federateName = "DefaultName";

    protected void init(String classPath) throws RTIexception, ClassNotFoundException, IllegalAccessException, InstantiationException {
        rtiamb = RtiFactoryFactory.getRtiFactory().createRtiAmbassador();

        tryCreateFederation();

        fedamb = (T) Class.forName(classPath).newInstance();

        rtiamb.joinFederationExecution( federateName, ConfigConstants.FEDERATION_NAME, fedamb );
        log("Joined Federation as " + federateName);

        rtiamb.registerFederationSynchronizationPoint( ConfigConstants.READY_TO_RUN, null );
        while(!fedamb.isAnnounced)
        {
            rtiamb.tick();
        }

        waitForUser();

        rtiamb.synchronizationPointAchieved( ConfigConstants.READY_TO_RUN );
        log("Achieved sync point: " + ConfigConstants.READY_TO_RUN + ", waiting for federation..." );
        while(!fedamb.isReadyToRun)
        {
            rtiamb.tick();
        }

        enableTimePolicy(fedamb.federateLookahead);
    }

    protected void log( String message )
    {
        System.out.println( federateName + "   : " + message );
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

    // Waiting for sync from RTI
    protected void advanceTime(double timestep) throws RTIexception
    {
        log("requesting time advance for: " + (fedamb.federateTime + timestep));
        // request the advance
        fedamb.isAdvancing = true;
        LogicalTime newTime = convertTime( fedamb.federateTime + timestep );
        rtiamb.timeAdvanceRequest( newTime );
        while( fedamb.isAdvancing )
        {
            rtiamb.tick();
        }
    }

    protected void enableTimePolicy(double federateLookahead) throws RTIexception
    {
        LogicalTime currentTime = convertTime( fedamb.federateTime );
        LogicalTimeInterval lookahead = convertInterval( federateLookahead );

        this.rtiamb.enableTimeRegulation( currentTime, lookahead );

        while(!fedamb.isRegulating)
        {
            rtiamb.tick();
        }

        this.rtiamb.enableTimeConstrained();

        while(!fedamb.isConstrained)
        {
            rtiamb.tick();
        }
    }

    public String getFederateName() {
        return federateName;
    }

    public void setFederateName(String federateName) {
        this.federateName = federateName;
    }
}
