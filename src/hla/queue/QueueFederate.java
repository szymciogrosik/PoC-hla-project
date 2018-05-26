package hla.queue;

import hla.constants.ConfigConstants;
import hla.example.producerConsumer.consumer.ConsumerFederate;
import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.DoubleTimeInterval;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Random;

public class QueueFederate {
    private RTIambassador rtiamb;
    private QueueAmbassador fedamb;
    private final double timeStep           = 100.0;
    private int queueHlaHandle;

    public void runFederate() throws RTIexception {
        rtiamb = RtiFactoryFactory.getRtiFactory().createRtiAmbassador();

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

        fedamb = new QueueAmbassador();
        rtiamb.joinFederationExecution( ConfigConstants.QUEUE_FED, ConfigConstants.FEDERATION_NAME, fedamb );
        log( "Joined Federation as " + ConfigConstants.QUEUE_FED);

        rtiamb.registerFederationSynchronizationPoint( ConfigConstants.READY_TO_RUN, null );

        while( fedamb.isAnnounced == false )
        {
            rtiamb.tick();
        }

        waitForUser();

        rtiamb.synchronizationPointAchieved( ConfigConstants.READY_TO_RUN );
        log( "Achieved sync point: " +ConfigConstants.READY_TO_RUN+ ", waiting for federation..." );
        while( fedamb.isReadyToRun == false )
        {
            rtiamb.tick();
        }

        enableTimePolicy();

        publishAndSubscribe();

        registerStorageObject();

        while (fedamb.running) {
            double timeToAdvance = fedamb.federateTime + timeStep;
            advanceTime(timeToAdvance);
            sendInteraction(fedamb.federateTime + fedamb.federateLookahead);

            if(fedamb.externalEvents.size() > 0) {
                fedamb.externalEvents.sort(new ExternalEventQueue.ExternalEventComparator());
                for(ExternalEventQueue externalEvent : fedamb.externalEvents) {
                    fedamb.federateTime = externalEvent.getTime();
                    switch (externalEvent.getEventType()) {
                        case JOIN_CLIENT_TO_QUEUE:
//                            log (externalEvent.getQty() + "");
                            break;

                        case OPEN_NEW_CASH_REGISTER:
                            log("Drugie wejscie");
//                            this.getFromStock(externalEvent.getQty());
                            break;
                        default:
                            log ("Default");
                            break;
                    }
                }
                fedamb.externalEvents.clear();
            }

            if(fedamb.grantedTime == timeToAdvance) {
                timeToAdvance += fedamb.federateLookahead;
                log("Updating stock at time: " + timeToAdvance);
                updateHLAObject(timeToAdvance);
                fedamb.federateTime = timeToAdvance;
            }

            rtiamb.tick();
        }

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

    private void registerStorageObject() throws RTIexception {
        int classHandle = rtiamb.getObjectClassHandle("ObjectRoot." + ConfigConstants.QUEUE_OBJ_NAME);
        this.queueHlaHandle = rtiamb.registerObjectInstance(classHandle);
    }

    private void updateHLAObject(double time) throws RTIexception {
        SuppliedAttributes attributes =
                RtiFactoryFactory.getRtiFactory().createSuppliedAttributes();

        int classHandle = rtiamb.getObjectClass(queueHlaHandle);
        int queueNumberHandle = rtiamb.getAttributeHandle( ConfigConstants.QUEUE_NUMBER_NAME, classHandle );
        int cashRegisterNumberHandle = rtiamb.getAttributeHandle( ConfigConstants.CASH_REGISTER_NUMBER_NAME, classHandle );
        int queueLengthHandle = rtiamb.getAttributeHandle( ConfigConstants.QUEUE_LENGTH_NAME, classHandle );
        byte[] queueNumber = EncodingHelpers.encodeInt(2);
        byte[] cashRegisterNumber = EncodingHelpers.encodeInt(3);
        byte[] queueLength = EncodingHelpers.encodeInt(1);


        attributes.add(queueNumberHandle, queueNumber);
        attributes.add(cashRegisterNumberHandle, cashRegisterNumber);
        attributes.add(queueLengthHandle, queueLength);
        LogicalTime logicalTime = convertTime( time );
        rtiamb.updateAttributeValues( queueHlaHandle, attributes, "actualize queue".getBytes(), logicalTime );
    }

    private void enableTimePolicy() throws RTIexception
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

    private void sendInteraction(double timeStep) throws RTIexception {
        SuppliedParameters parameters =
                RtiFactoryFactory.getRtiFactory().createSuppliedParameters();
//        Random random = new Random();
//        int quantityInt = random.nextInt(10) + 1;
        byte[] clientNumber = EncodingHelpers.encodeInt(1);
        byte[] queueNumber = EncodingHelpers.encodeInt(2);
        byte[] amountOfArticles = EncodingHelpers.encodeInt(12);
        byte[] cashRegisterNumber = EncodingHelpers.encodeInt(3);

        int interactionHandle = rtiamb.getInteractionClassHandle(ConfigConstants.START_HANDLING_CLIENT_NAME);
        int clientNumberHandle = rtiamb.getParameterHandle( ConfigConstants.CLIENT_NUMBER_NAME, interactionHandle );
        int queueNumberHandle = rtiamb.getParameterHandle( ConfigConstants.QUEUE_NUMBER_NAME, interactionHandle );
        int amountOfArticlesHandle = rtiamb.getParameterHandle( ConfigConstants.AMOUNT_OF_ARTICLES_NAME, interactionHandle );
        int cashRegisterNumberHandle = rtiamb.getParameterHandle( ConfigConstants.CASH_REGISTER_NUMBER_NAME, interactionHandle );

        parameters.add(clientNumberHandle, clientNumber);
        parameters.add(queueNumberHandle, queueNumber);
        parameters.add(amountOfArticlesHandle, amountOfArticles);
        parameters.add(cashRegisterNumberHandle, cashRegisterNumber);

        LogicalTime time = convertTime( timeStep );
        log("Sending "+ ConfigConstants.START_HANDLING_CLIENT_NAME +": 1, 2, 12, 3");
        rtiamb.sendInteraction( interactionHandle, parameters, "tag".getBytes(), time );
    }

    private void publishAndSubscribe() throws RTIexception {
        int startHandlingClientHandle = rtiamb.getInteractionClassHandle( ConfigConstants.START_HANDLING_CLIENT_NAME );
        rtiamb.publishInteractionClass(startHandlingClientHandle);

        byte[] queueNumber = EncodingHelpers.encodeInt(2);
        byte[] cashRegisterNumber = EncodingHelpers.encodeInt(3);
        byte[] queueLengthNumber = EncodingHelpers.encodeInt(10);

        int queueHandle = rtiamb.getObjectClassHandle( "ObjectRoot." + ConfigConstants.QUEUE_OBJ_NAME );
        int queueNumberHandle    = rtiamb.getAttributeHandle( ConfigConstants.QUEUE_NUMBER_NAME, queueHandle );
        int cashRegisterNumberHandle    = rtiamb.getAttributeHandle( ConfigConstants.CASH_REGISTER_NUMBER_NAME, queueHandle );
        int queueLengthNumberHandle    = rtiamb.getAttributeHandle( ConfigConstants.QUEUE_LENGTH_NAME, queueHandle );


        AttributeHandleSet attributes =
                RtiFactoryFactory.getRtiFactory().createAttributeHandleSet();
        attributes.add( queueNumberHandle );
        attributes.add( cashRegisterNumberHandle );
        attributes.add( queueLengthNumberHandle );

        rtiamb.publishObjectClass(queueHandle, attributes);

        int joinClientToQueueHandle = rtiamb.getInteractionClassHandle( ConfigConstants.JOIN_CLIENT_TO_QUEUE_INTERACTION_NAME );
        fedamb.joinClientToQueueHandle = joinClientToQueueHandle;
        rtiamb.subscribeInteractionClass( joinClientToQueueHandle );

        int openNewCashRegisterHandle = rtiamb.getInteractionClassHandle( ConfigConstants.OPEN_NEW_CASH_REGISTER_NAME );
        fedamb.openNewCashRegisterHandle = openNewCashRegisterHandle;
        rtiamb.subscribeInteractionClass( openNewCashRegisterHandle );
    }

    private void advanceTime( double timestep ) throws RTIexception
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

    private double randomTime() {
        Random r = new Random();
        return 1 +(4 * r.nextDouble());
    }

    private LogicalTime convertTime(double time )
    {
        // PORTICO SPECIFIC!!
        return new DoubleTime( time );
    }

    /**
     * Same as for {@link #convertTime(double)}
     */
    private LogicalTimeInterval convertInterval(double time )
    {
        // PORTICO SPECIFIC!!
        return new DoubleTimeInterval( time );
    }

    private void log( String message )
    {
        System.out.println( ConfigConstants.QUEUE_FED + "   : " + message );
    }

    public static void main(String[] args) {
        try {
            new QueueFederate().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }
}
