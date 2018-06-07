package ieee1516e.client;

import hla.rti1516e.*;
import hla.rti1516e.encoding.HLAinteger64BE;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.time.HLAfloat64Time;
import ieee1516e.constants.ConfigConstants;
import ieee1516e.queue.Queue;
import ieee1516e.tamplate.BaseFederate;

import java.util.ArrayList;
import java.util.Random;

public class ClientFederate extends BaseFederate<ClientAmbassador> {
    private final double timeStep           = 1.0;

    //Publish
    //Interaction join client to queue
    private InteractionClassHandle joinClientToQueueHandle;
    private ParameterHandle clientNumberHandleJoinClientToQueue;
    private ParameterHandle queueNumberHandleJoinClientToQueue;
    private ParameterHandle amountOfArticlesHandleJoinClientToQueue;

    //Subscribe
    //Object queue
    private ObjectClassHandle queueHandle;
    private AttributeHandle queueNumberQueue;
    private AttributeHandle cashRegisterQueue;
    private AttributeHandle queueLengthQueue;
    //Interaction end simulation
    private InteractionClassHandle endSimulationHandle;

    private ArrayList<Queue> queueList = new ArrayList<>();
    private long clientNumber = 0;

    private Random generator = new Random();
    private double randomTimeToSendClient = generator.nextInt(ConfigConstants.CLIENT_MAX_TIME_TO_END_SHOPPING);

    private void runFederate() throws RTIexception, IllegalAccessException, InstantiationException, ClassNotFoundException {
        this.setFederateName(ConfigConstants.CLIENT_FED);

        // Create ambassador, tryCreateFederation, and waiting for first sync
        init(ClientAmbassador.class.getCanonicalName());

        publishAndSubscribe();
        log("Published and Subscribed");

        while (fedamb.running) {
            double timeToAdvance = fedamb.federateTime + timeStep;
            advanceTime(timeStep);

            if(fedamb.externalObjects.size() > 0) {
                fedamb.externalObjects.sort(new ClientExternalObject.ExternalObjectComparator());
                for(ClientExternalObject externalObject : fedamb.externalObjects) {
                    switch (externalObject.getObjectType()) {
                        case QUEUE:
                            long queueNumberDecoded = decodeIntValue(externalObject.getAttributes().get(this.queueNumberQueue));
                            long cashRegisterNumberDecoded = decodeIntValue(externalObject.getAttributes().get(this.cashRegisterQueue));
                            long queueLengthDecoded = decodeIntValue(externalObject.getAttributes().get(this.queueLengthQueue));
                            log("In case object: QUEUE | Nr kolejki: " +
                                    queueNumberDecoded +
                                    ", Nr kasy: " +
                                    cashRegisterNumberDecoded +
                                    ", Dlugosc kolejki: " +
                                    queueLengthDecoded
                            );

                            addToQueueList(queueNumberDecoded, cashRegisterNumberDecoded, queueLengthDecoded);
                            break;
                        default:
                            log("Undetected object.");
                            break;
                    }
                }
                fedamb.externalObjects.clear();
            }

            if(fedamb.federateTime >= randomTimeToSendClient) {
                sendInteractionJoinToQueue();
                randomTimeToSendClient = fedamb.federateTime + generator.nextInt(ConfigConstants.CLIENT_MAX_TIME_TO_END_SHOPPING);
            }

            if(fedamb.grantedTime == timeToAdvance) {
                timeToAdvance += fedamb.federateLookahead;
                log("Updating client time: " + timeToAdvance);
                fedamb.federateTime = timeToAdvance;
            }

            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }

        try {
            resign();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendInteractionJoinToQueue() throws RTIexception {
        //Find queue
        Queue queueToJoin = null;
        long lengthQueueToJoin = Integer.MAX_VALUE;
        for (Queue q : queueList) {
            if(q.getLength() < lengthQueueToJoin) {
                lengthQueueToJoin = q.getLength();
                queueToJoin = q;
            }
        }

        if(queueToJoin != null) {
            long amountOfArticles =  generator.nextInt(ConfigConstants.CLIENT_MAX_AMOUNT_OF_ARTICLES);
            sendInteraction(clientNumber, queueToJoin.getNumberQueue(), amountOfArticles);
            clientNumber++;
            log("SEND interaction JOIN_CLIENT_TO_QUEUE_INTERACTION_NAME. Client nr: "+clientNumber + ", queue nr: "+queueToJoin.getNumberQueue()+", amountOfArticles: "+amountOfArticles);
        }
    }

    private void addToQueueList(long numberQueue, long numberCashRegister, long length) {
        // If Queue exist in queueList update length
        boolean notExist = true;
        for (Queue e: queueList) {
            if(e.getNumberQueue() == numberQueue && e.getNumberCashRegister() == numberCashRegister) {
                e.setLength(length);
                notExist = false;
                log("Update queue. QueueNumber=" + e.getNumberQueue() + ", CashRegisterNumber=" + e.getNumberCashRegister() + ", length=" + e.getLength());
                break;
            }
        }

        if(notExist) {
            queueList.add(new Queue(numberQueue, numberCashRegister, length));
            log("Added new queue. QueueNumber=" + numberQueue + ", CashRegisterNumber=" + numberCashRegister + ", length=" + length);
        }
    }

    private void sendInteraction(long clientNumber, long queueNumberClient, long amountOfArticlesClient) throws RTIexception {
        // Send Interaction joinClientToQueueHandle
        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + timeStep + fedamb.federateLookahead);
        ParameterHandleValueMap parameters1 = rtiamb.getParameterHandleValueMapFactory().create(3);
        HLAinteger64BE clientNumberSend = encoderFactory.createHLAinteger64BE( clientNumber );
        HLAinteger64BE queueNumberClientSend = encoderFactory.createHLAinteger64BE( queueNumberClient );
        HLAinteger64BE amountOfArticlesClientSend = encoderFactory.createHLAinteger64BE( amountOfArticlesClient);

        parameters1.put(clientNumberHandleJoinClientToQueue, clientNumberSend.toByteArray());
        parameters1.put(queueNumberHandleJoinClientToQueue, queueNumberClientSend.toByteArray());
        parameters1.put(amountOfArticlesHandleJoinClientToQueue, amountOfArticlesClientSend.toByteArray());

        rtiamb.sendInteraction(joinClientToQueueHandle, parameters1, generateTag(), time);
    }

    private void publishAndSubscribe() throws RTIexception {
        //Publish
        //Interaction joinClientToQueueHandle
        this.joinClientToQueueHandle = rtiamb.getInteractionClassHandle(ConfigConstants.JOIN_CLIENT_TO_QUEUE_INTERACTION_NAME);
        clientNumberHandleJoinClientToQueue = rtiamb.getParameterHandle(this.joinClientToQueueHandle, ConfigConstants.CLIENT_NUMBER_NAME);
        queueNumberHandleJoinClientToQueue = rtiamb.getParameterHandle(this.joinClientToQueueHandle, ConfigConstants.QUEUE_NUMBER_NAME);
        amountOfArticlesHandleJoinClientToQueue = rtiamb.getParameterHandle(this.joinClientToQueueHandle, ConfigConstants.AMOUNT_OF_ARTICLES_NAME);
        rtiamb.publishInteractionClass(joinClientToQueueHandle);

        //Subscribe
        //Queue object
        this.queueHandle = rtiamb.getObjectClassHandle(ConfigConstants.QUEUE_OBJ_NAME);
        this.queueNumberQueue = rtiamb.getAttributeHandle(this.queueHandle, ConfigConstants.QUEUE_NUMBER_NAME);
        this.cashRegisterQueue = rtiamb.getAttributeHandle(this.queueHandle, ConfigConstants.CASH_REGISTER_NUMBER_NAME);
        this.queueLengthQueue = rtiamb.getAttributeHandle(this.queueHandle, ConfigConstants.QUEUE_LENGTH_NAME);
        AttributeHandleSet attributes = rtiamb.getAttributeHandleSetFactory().create();
        attributes.add(this.queueNumberQueue);
        attributes.add(this.cashRegisterQueue);
        attributes.add(this.queueLengthQueue);
        rtiamb.subscribeObjectClassAttributes(queueHandle, attributes);
        //Interaction open new cash register
        this.endSimulationHandle = rtiamb.getInteractionClassHandle(ConfigConstants.END_SIMULATION_INTERACTION_NAME);
        rtiamb.subscribeInteractionClass(endSimulationHandle);
    }

    public static void main(String[] args) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        try {
            new ClientFederate().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }
}
