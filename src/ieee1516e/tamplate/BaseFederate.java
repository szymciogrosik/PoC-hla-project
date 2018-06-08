package ieee1516e.tamplate;


import hla.rti1516e.*;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAboolean;
import hla.rti1516e.encoding.HLAinteger64BE;
import hla.rti1516e.exceptions.*;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;
import ieee1516e.constants.ConfigConstants;
import org.portico.impl.hla1516e.types.time.DoubleTime;
import org.portico.impl.hla1516e.types.time.DoubleTimeInterval;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public abstract class BaseFederate<T extends BaseAmbassador> {
    protected RTIambassador rtiamb;
    protected T fedamb;
    protected HLAfloat64TimeFactory timeFactory;
    protected EncoderFactory encoderFactory;

    private String federateName = "DefaultName";

    protected void init(String classPath) throws RTIexception, ClassNotFoundException, IllegalAccessException, InstantiationException {
        rtiamb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
        encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();

        fedamb = (T) Class.forName(classPath).newInstance();
        fedamb.setRtiAmbassador(rtiamb);
        rtiamb.connect(fedamb, CallbackModel.HLA_EVOKED);

        //Try create federation
        tryCreateFederation();

        //Join to federation
        rtiamb.joinFederationExecution(federateName, federateName, ConfigConstants.FEDERATION_NAME);
        log("Joined Federation as " + federateName);

        timeFactory = (HLAfloat64TimeFactory) rtiamb.getTimeFactory();

        rtiamb.registerFederationSynchronizationPoint( ConfigConstants.READY_TO_RUN, null );
        while(!fedamb.isAnnounced)
        {
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }

        waitForUser();

        rtiamb.synchronizationPointAchieved( ConfigConstants.READY_TO_RUN );
        log("Achieved sync point: " + ConfigConstants.READY_TO_RUN + ", waiting for federation..." );
        while(!fedamb.isReadyToRun)
        {
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }

        enableTimePolicy(fedamb.federateLookahead);
        log("Time Policy Enabled");
    }

    protected void log( String message )
    {
        System.out.println( federateName + "   : " + message );
    }

    private void waitForUser()
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

    private void tryCreateFederation() {
        try {
            URL[] modules = new URL[]{
                    (new File(ConfigConstants.FEDERATION_FILE_PATH)).toURI().toURL()
            };
            rtiamb.createFederationExecution(ConfigConstants.FEDERATION_NAME, modules);
            log("Created Federation ");
        } catch (FederationExecutionAlreadyExists exists) {
            log("Didn't create federation, it already existed");
        } catch (MalformedURLException urle) {
            log("Exception loading one of the FOM modules from disk: " + urle.getMessage());
            urle.printStackTrace();
        } catch (NotConnected | CouldNotOpenFDD | RTIinternalError | ErrorReadingFDD | InconsistentFDD notConnected) {
            notConnected.printStackTrace();
        }
    }

    // Waiting for sync from RTI
    protected void advanceTime(double timeToAdvance) throws RTIexception
    {
        log("Requesting time advance for: " + ( timeToAdvance ));
        // request the advance
        fedamb.isAdvancing = true;
        HLAfloat64Time newTime = timeFactory.makeTime( timeToAdvance );
        rtiamb.timeAdvanceRequest( newTime );
        while( fedamb.isAdvancing )
        {
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }
    }

    protected void enableTimePolicy(double federateLookahead) throws RTIexception
    {
        HLAfloat64Interval lookahead = timeFactory.makeInterval( federateLookahead );

        this.rtiamb.enableTimeRegulation( lookahead );

        while(!fedamb.isRegulating)
        {
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }

        this.rtiamb.enableTimeConstrained();

        while(!fedamb.isConstrained)
        {
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }
    }

    protected void setFederateName(String federateName) {
        this.federateName = federateName;
    }

    protected void resign() throws Exception {
        rtiamb.resignFederationExecution(ResignAction.DELETE_OBJECTS);
        log("Resigned from Federation");

        try {
            rtiamb.destroyFederationExecution(ConfigConstants.FEDERATION_NAME);
            log("Destroyed Federation");
        } catch (FederationExecutionDoesNotExist dne) {
            log("No need to destroy federation, it doesn't exist");
        } catch (FederatesCurrentlyJoined fcj) {
            log("Didn't destroy federation, federates still joined");
        }
    }

    protected byte[] generateTag() {
        return ("(timestamp) " + System.currentTimeMillis()).getBytes();
    }

    protected long decodeIntValue(byte[] bytes )
    {
        HLAinteger64BE value = encoderFactory.createHLAinteger64BE();
        try
        {
            value.decode( bytes );
            return value.getValue();
        }
        catch( DecoderException de )
        {
            de.printStackTrace();
            return 0;
        }
    }

    protected boolean decodeBooleanValue(byte[] bytes )
    {
        HLAboolean value = encoderFactory.createHLAboolean();
        try
        {
            value.decode( bytes );
            return value.getValue();
        }
        catch( DecoderException de )
        {
            de.printStackTrace();
            return false;
        }
    }
}
