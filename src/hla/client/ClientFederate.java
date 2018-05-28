package hla.client;

import hla.constants.ConfigConstants;
import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import hla.tamplate.BaseFederate;

public class ClientFederate extends BaseFederate<ClientAmbassador> {
    private final double timeStep           = 3.0;

    private void runFederate() throws RTIexception, IllegalAccessException, InstantiationException, ClassNotFoundException {
        this.setFederateName(ConfigConstants.CLIENT_FED);

        // Create ambassador, tryCreateFederation, and waiting for first sync
        init(ClientAmbassador.class.getCanonicalName());

        publishAndSubscribe();

        while (fedamb.running) {
            double timeToAdvance = fedamb.federateTime + timeStep;
            advanceTime(timeStep);

            sendInteraction(timeToAdvance + fedamb.federateLookahead);

            if(fedamb.grantedTime == timeToAdvance) {
                timeToAdvance += fedamb.federateLookahead;
                log("Updating client at time: " + timeToAdvance);
                fedamb.federateTime = timeToAdvance;
//            waitForUser();
            }

            rtiamb.tick();
        }
    }

    private void sendInteraction(double timeStep) throws RTIexception {
        // Send Interaction joinClientToQueue
        SuppliedParameters parameters =
                RtiFactoryFactory.getRtiFactory().createSuppliedParameters();
//        Random random = new Random();
//        int quantityInt = random.nextInt(10) + 1;
        byte[] clientNumber = EncodingHelpers.encodeInt(1);
        byte[] queueNumber = EncodingHelpers.encodeInt(2);
        byte[] amountOfArticles = EncodingHelpers.encodeInt(12);

        int interactionHandle = rtiamb.getInteractionClassHandle(ConfigConstants.JOIN_CLIENT_TO_QUEUE_INTERACTION_NAME);
        int clientNumberHandle = rtiamb.getParameterHandle( ConfigConstants.CLIENT_NUMBER_NAME, interactionHandle );
        int queueNumberHandle = rtiamb.getParameterHandle( ConfigConstants.QUEUE_NUMBER_NAME, interactionHandle );
        int amountOfArticlesHandle = rtiamb.getParameterHandle( ConfigConstants.AMOUNT_OF_ARTICLES_NAME, interactionHandle );

        parameters.add(clientNumberHandle, clientNumber);
        parameters.add(queueNumberHandle, queueNumber);
        parameters.add(amountOfArticlesHandle, amountOfArticles);

        LogicalTime time = convertTime( timeStep );
        log("Sending "+ ConfigConstants.JOIN_CLIENT_TO_QUEUE_INTERACTION_NAME +": 1, 2, 12");
        rtiamb.sendInteraction( interactionHandle, parameters, "tag".getBytes(), time );
    }

    protected void publishAndSubscribe() throws RTIexception {
        // Publish interaction joinClientToQueue
        int joinClientToQueueHandle = rtiamb.getInteractionClassHandle( ConfigConstants.JOIN_CLIENT_TO_QUEUE_INTERACTION_NAME );
        rtiamb.publishInteractionClass(joinClientToQueueHandle);

        // Register listening on queue objects
        int simObjectClassHandle = rtiamb.getObjectClassHandle("ObjectRoot." + ConfigConstants.QUEUE_OBJ_NAME);
        int queueNumberHandle = rtiamb.getAttributeHandle(ConfigConstants.QUEUE_NUMBER_NAME, simObjectClassHandle);
        int cashRegisterNumberHandle = rtiamb.getAttributeHandle(ConfigConstants.CASH_REGISTER_NUMBER_NAME, simObjectClassHandle);
        int queueLengthHandle = rtiamb.getAttributeHandle(ConfigConstants.QUEUE_LENGTH_NAME, simObjectClassHandle);

        AttributeHandleSet attributes = RtiFactoryFactory.getRtiFactory().createAttributeHandleSet();
        attributes.add(queueNumberHandle);
        attributes.add(cashRegisterNumberHandle);
        attributes.add(queueLengthHandle);

        rtiamb.subscribeObjectClassAttributes(simObjectClassHandle, attributes);
    }

    public static void main(String[] args) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        try {
            new ClientFederate().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }
}
