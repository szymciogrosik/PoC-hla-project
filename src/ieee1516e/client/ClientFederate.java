package ieee1516e.client;

import hla.rti1516e.*;
import hla.rti1516e.encoding.HLAinteger64BE;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.time.HLAfloat64Time;
import ieee1516e.constants.ConfigConstants;
import ieee1516e.queue.Queue;
import ieee1516e.tamplate.BaseFederate;

import java.util.ArrayList;

public class ClientFederate extends BaseFederate<ClientAmbassador> {
    private final double timeStep           = 3.0;

    //Publish
    //Interaction join client to queue
    protected InteractionClassHandle joinClientToQueue;
    protected ParameterHandle clientNumber;
    protected ParameterHandle queueNumberClient;
    protected ParameterHandle ammountOfArticlesClient;

    //Subscribe
    //Object queue
    protected ObjectClassHandle queueHandle;
    protected AttributeHandle queueNumberQueue;
    protected AttributeHandle cashRegisterQueue;
    protected AttributeHandle queueLenghtQueue;

    private ArrayList<Queue> queueList = new ArrayList<>();

    private void runFederate() throws RTIexception, IllegalAccessException, InstantiationException, ClassNotFoundException {
        this.setFederateName(ConfigConstants.CLIENT_FED);

        // Create ambassador, tryCreateFederation, and waiting for first sync
        init(ClientAmbassador.class.getCanonicalName());

        publishAndSubscribe();
        log("Published and Subscribed");

        while (fedamb.running) {
            double timeToAdvance = fedamb.federateTime + timeStep;
            advanceTime(timeStep);

            sendInteraction();

            if(fedamb.externalObjects.size() > 0) {
                fedamb.externalObjects.sort(new ClientExternalObject.ExternalObjectComparator());
                for(ClientExternalObject externalObject : fedamb.externalObjects) {
                    switch (externalObject.getObjectType()) {
                        case QUEUE:
                            log("In case queue");
                            System.out.println("Nr kolejki: " +
                                    decodeIntValue(externalObject.getAttributes().get(this.queueNumberQueue)) +
                                    ", Nr kasy: " +
                                    decodeIntValue(externalObject.getAttributes().get(this.cashRegisterQueue)) +
                                    ", Dlugosc kolejki: " +
                                    decodeIntValue(externalObject.getAttributes().get(this.queueLenghtQueue))
                            );
                            break;
                        default:
                            log("Undetected object.");
                            break;
                    }
                }
                fedamb.externalObjects.clear();
            }

            if(fedamb.grantedTime == timeToAdvance) {
                timeToAdvance += fedamb.federateLookahead;
                log("Updating client time: " + timeToAdvance);
                fedamb.federateTime = timeToAdvance;
//            waitForUser();
            }

            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }
    }

    private void addToQueueList(int numberQueue, int numberCashRegister, int length) {
        // If Queue exist in queueList update length
        boolean notExist = true;
        for (Queue e: queueList) {
            if(e.getNumberQueue() == numberQueue && e.getNumberCashRegister() == numberCashRegister) {
                e.setLength(length);
                notExist = false;
                log("Update queue number=" + e.getNumberQueue() + ", length=" + e.getLength());
                break;
            }
        }

        if(notExist) {
            queueList.add(new Queue(numberQueue, numberCashRegister, length));
            log("Added new queue. Number=" + numberQueue + ", length=" + length);
        }
    }

    private void sendInteraction() throws RTIexception {
        // Send Interaction joinClientToQueue
        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + timeStep + fedamb.federateLookahead);
        ParameterHandleValueMap parameters1 = rtiamb.getParameterHandleValueMapFactory().create(3);
        HLAinteger64BE clientNumberSend = encoderFactory.createHLAinteger64BE( 1 );
        HLAinteger64BE queueNumberClientSend = encoderFactory.createHLAinteger64BE( 5 );
        HLAinteger64BE ammountOfArticlesClientSend = encoderFactory.createHLAinteger64BE( 15);

        parameters1.put(clientNumber, clientNumberSend.toByteArray());
        parameters1.put(queueNumberClient, queueNumberClientSend.toByteArray());
        parameters1.put(ammountOfArticlesClient, ammountOfArticlesClientSend.toByteArray());

        rtiamb.sendInteraction(joinClientToQueue, parameters1, generateTag(), time);
    }

    protected void publishAndSubscribe() throws RTIexception {
        //Publish
        //Interaction joinClientToQueue
        this.joinClientToQueue = rtiamb.getInteractionClassHandle(ConfigConstants.JOIN_CLIENT_TO_QUEUE_INTERACTION_NAME);
        clientNumber = rtiamb.getParameterHandle(this.joinClientToQueue, ConfigConstants.CLIENT_NUMBER_NAME);
        queueNumberClient = rtiamb.getParameterHandle(this.joinClientToQueue, ConfigConstants.QUEUE_NUMBER_NAME);
        ammountOfArticlesClient = rtiamb.getParameterHandle(this.joinClientToQueue, ConfigConstants.AMOUNT_OF_ARTICLES_NAME);
        rtiamb.publishInteractionClass(joinClientToQueue);

        //Subscribe
        //Queue object
        this.queueHandle = rtiamb.getObjectClassHandle(ConfigConstants.QUEUE_OBJ_NAME);
        this.queueNumberQueue = rtiamb.getAttributeHandle(this.queueHandle, ConfigConstants.QUEUE_NUMBER_NAME);
        this.cashRegisterQueue = rtiamb.getAttributeHandle(this.queueHandle, ConfigConstants.CASH_REGISTER_NUMBER_NAME);
        this.queueLenghtQueue = rtiamb.getAttributeHandle(this.queueHandle, ConfigConstants.QUEUE_LENGTH_NAME);
        AttributeHandleSet attributes = rtiamb.getAttributeHandleSetFactory().create();
        attributes.add(this.queueNumberQueue);
        attributes.add(this.cashRegisterQueue);
        attributes.add(this.queueLenghtQueue);
        rtiamb.subscribeObjectClassAttributes(queueHandle, attributes);
    }

    public static void main(String[] args) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        try {
            new ClientFederate().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }
}
