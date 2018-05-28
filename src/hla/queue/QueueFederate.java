package hla.queue;

import hla.constants.ConfigConstants;
import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import hla.tamplate.BaseFederate;

public class QueueFederate extends BaseFederate<QueueAmbassador> {
    private int queueHlaHandle;
    private final double timeStep           = 10.0;

    private int queueNr                     = 44;
    private int cashRegisterNr              = 55;
    private int queueLengthNr               = 66;


    private void runFederate() throws RTIexception, IllegalAccessException, InstantiationException, ClassNotFoundException {
        this.setFederateName(ConfigConstants.QUEUE_FED);

        // Create ambassador, tryCreateFederation, and waiting for first sync
        init(QueueAmbassador.class.getCanonicalName());

        publishAndSubscribe();
        registerStorageObject();

        while (fedamb.running) {
            double timeToAdvance = fedamb.federateTime + timeStep;
            advanceTime(timeStep);

            sendInteraction(timeToAdvance + fedamb.federateLookahead);

            if(fedamb.externalEvents.size() > 0) {
                fedamb.externalEvents.sort(new QueueExternalEvent.ExternalEventComparator());
                for(QueueExternalEvent externalEvent : fedamb.externalEvents) {
                    fedamb.federateTime = externalEvent.getTime();
                    switch (externalEvent.getEventType()) {
                        case JOIN_CLIENT_TO_QUEUE:
                            clientJoinedToQueue(externalEvent.getQty());
                            break;

                        case OPEN_NEW_CASH_REGISTER:
                            log("Drugie wejscie");
//                            this.getFromStock(externalEvent.getQty());
                            break;
                        default:
                            log("Undetected interaction.");
                            break;
                    }
                }
                fedamb.externalEvents.clear();
            }

            if(fedamb.grantedTime == timeToAdvance) {
                timeToAdvance += fedamb.federateLookahead;
                log("Updating queue at time: " + timeToAdvance);
                updateHLAObject(timeToAdvance);
                fedamb.federateTime = timeToAdvance;
//                waitForUser(ConfigConstants.QUEUE_FED);
            }

            rtiamb.tick();
        }
    }

    private void clientJoinedToQueue(int qty) {
        queueNr++;
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
        byte[] queueNumber = EncodingHelpers.encodeInt(queueNr);
        byte[] cashRegisterNumber = EncodingHelpers.encodeInt(cashRegisterNr);
        byte[] queueLength = EncodingHelpers.encodeInt(queueLengthNr);

        attributes.add(queueNumberHandle, queueNumber);
        attributes.add(cashRegisterNumberHandle, cashRegisterNumber);
        attributes.add(queueLengthHandle, queueLength);
        LogicalTime logicalTime = convertTime( time );
        rtiamb.updateAttributeValues( queueHlaHandle, attributes, "actualize queue".getBytes(), logicalTime );
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
        // Register publish Interaction startHandlingClient
        int startHandlingClientHandle = rtiamb.getInteractionClassHandle( ConfigConstants.START_HANDLING_CLIENT_NAME );
        rtiamb.publishInteractionClass(startHandlingClientHandle);

        // Register publish Object Queue
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

        // Register subscribe to Interaction joinClientToQueue
        int joinClientToQueueHandle = rtiamb.getInteractionClassHandle( ConfigConstants.JOIN_CLIENT_TO_QUEUE_INTERACTION_NAME );
        fedamb.joinClientToQueueHandle = joinClientToQueueHandle;
        rtiamb.subscribeInteractionClass( joinClientToQueueHandle );

        // Register subscribe to Interaction openNewCashRegisterHandle
        int openNewCashRegisterHandle = rtiamb.getInteractionClassHandle( ConfigConstants.OPEN_NEW_CASH_REGISTER_NAME );
        fedamb.openNewCashRegisterHandle = openNewCashRegisterHandle;
        rtiamb.subscribeInteractionClass( openNewCashRegisterHandle );

        // Todo: Register Subscribe Object CashRegister
    }



    public static void main(String[] args) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        try {
            new QueueFederate().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }
}
