package ieee1516e.queue;

import hla.rti1516e.*;
import hla.rti1516e.encoding.HLAinteger64BE;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.time.HLAfloat64Time;
import ieee1516e.cashRegister.CashRegister;
import ieee1516e.client.Client;
import ieee1516e.constants.ConfigConstants;
import ieee1516e.tamplate.BaseFederate;

import java.util.ArrayList;

public class QueueFederate extends BaseFederate<QueueAmbassador> {
    private final double timeStep           = 1.0;

    //Publish
    //Interaction start handling client
    private InteractionClassHandle startHandlingClientHandle;
    private ParameterHandle queueNumberHandleStartHandlingClient;
    private ParameterHandle cashRegisterNumberHandleStartHandlingClient;
    private ParameterHandle clientNumberHandleStartHandlingClient;
    private ParameterHandle amountOfArticlesHandleStartHandlingClient;
    //Object queue
    private ObjectClassHandle queueHandle;
    private AttributeHandle queueNumberHandleQueue;
    private AttributeHandle cashRegisterNumberHandleQueue;
    private AttributeHandle queueLengthHandleQueue;

    //Subscribe
    //Interaction join client to queue
    private InteractionClassHandle joinClientToQueueHandle;
    private ParameterHandle clientNumberHandleJoinClientToQueue;
    private ParameterHandle queueNumberHandleJoinClientToQueue;
    private ParameterHandle amountOfArticlesHandleJoinClientToQueue;
    //Interaction open new cash register
    private InteractionClassHandle openNewCashRegisterHandle;
    private ParameterHandle cashRegisterNumberHandleOpenNewCashRegister;
    private ParameterHandle queueNumberHandleOpenNewCashRegister;
    //Object cash register
    private ObjectClassHandle cashRegisterHandle;
    private AttributeHandle cashRegisterNumberHandleCashRegister;
    private AttributeHandle isFreeHandleCashRegister;

    private long queueStartNr                     = 0;
    private long cashRegisterStartNr              = 0;
    private long queueLengthStartNr               = 0;

    private ArrayList<Queue> queueList = new ArrayList<>();
    private ArrayList<CashRegister> cashRegisterList = new ArrayList<>();

    private void runFederate() throws RTIexception, IllegalAccessException, InstantiationException, ClassNotFoundException {
        this.setFederateName(ConfigConstants.QUEUE_FED);

        // Create ambassador, tryCreateFederation, and waiting for first sync
        init(QueueAmbassador.class.getCanonicalName());

        publishAndSubscribe();
        log("Published and Subscribed");

        while (fedamb.running) {
            double timeToAdvance = fedamb.federateTime + timeStep;
            advanceTime(timeStep);

            if(fedamb.externalObjects.size() > 0) {
                fedamb.externalObjects.sort(new QueueExternalObject.ExternalObjectComparator());
                for(QueueExternalObject externalObject : fedamb.externalObjects) {
                    switch (externalObject.getObjectType()) {
                        case CASH_REGISTER:
                            long cashRegisterNumberDecoded = decodeIntValue(externalObject.getAttributes().get(this.cashRegisterNumberHandleCashRegister));
                            boolean isFreeDecoded = decodeBooleanValue(externalObject.getAttributes().get(this.isFreeHandleCashRegister));
                            log("In case object: CASH_REGISTER | Nr kasy: " +
                                    cashRegisterNumberDecoded +
                                    ", Czy wolna: " +
                                    isFreeDecoded
                            );

                            // Jeżeli nie istnieje w liście kolejek taka kolejka z nr kasy to zarejestruj nową kolejkę i dodaj kasę to listy kas, jeśli istnieje zrób update na kasie
                            boolean notExist = true;
                            for (CashRegister cR : cashRegisterList) {
                                if(cR.getNumberCashRegister() == cashRegisterNumberDecoded) {
                                    cR.setFree(isFreeDecoded);
                                    cR.setToUpdate(true);
                                    notExist = false;
                                    break;
                                }
                            }

                            if(notExist) {
                                cashRegisterList.add(new CashRegister(cashRegisterNumberDecoded, isFreeDecoded));
                                registerNewQueue(cashRegisterNumberDecoded);
                            }

                            break;
                        default:
                            log("In case object: Undetected object.");
                            break;
                    }
                }
                fedamb.externalObjects.clear();
            }

            if(fedamb.externalEvents.size() > 0) {
                fedamb.externalEvents.sort(new QueueExternalEvent.ExternalEventComparator());
                for(QueueExternalEvent externalEvent : fedamb.externalEvents) {
                    switch (externalEvent.getEventType()) {
                        case JOIN_CLIENT_TO_QUEUE:
                            long clientNumberDecoded = decodeIntValue(externalEvent.getAttributes().get(this.clientNumberHandleJoinClientToQueue));
                            long queueNumberDecoded = decodeIntValue(externalEvent.getAttributes().get(this.queueNumberHandleJoinClientToQueue));
                            long articlesAmountDecoded = decodeIntValue(externalEvent.getAttributes().get(this.amountOfArticlesHandleJoinClientToQueue));
                            log("In case interaction: JOIN_CLIENT_TO_QUEUE | Nr klienta: " +
                                    clientNumberDecoded +
                                    ", Nr kolejki: " +
                                    queueNumberDecoded +
                                    ", Liczba artykulow: " +
                                    articlesAmountDecoded
                            );

                            for (Queue q : queueList) {
                                if(q.getNumberQueue() == queueNumberDecoded) {
                                    q.addClientToQueue(new Client(clientNumberDecoded, articlesAmountDecoded));
                                    break;
                                }
                            }
                            break;
                        case OPEN_NEW_CASH_REGISTER:
                            //Todo: właściwie niepotrzebna bo kiedy otworzymy nową kasę kolejka i tak doda nową kolejkę
                            log("In case interaction: OPEN_NEW_CASH_REGISTER | Nr kasy: " +
                                    decodeIntValue(externalEvent.getAttributes().get(this.cashRegisterNumberHandleOpenNewCashRegister)) +
                                    ", Nr kolejki: " +
                                    decodeIntValue(externalEvent.getAttributes().get(this.queueNumberHandleOpenNewCashRegister))
                            );
                            break;
                        default:
                            log("In case interaction: Undetected interaction.");
                            break;
                    }
                }
                fedamb.externalEvents.clear();
            }

            //Send interaction startHandlingClients
            sendInteractionsStartHandlingClients();

            if(fedamb.grantedTime == timeToAdvance) {
                timeToAdvance += fedamb.federateLookahead;
                log("Updating queue at time: " + timeToAdvance);
                updateHLAObjects(timeToAdvance);
                fedamb.federateTime = timeToAdvance;
            }

            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }
    }

    private void sendInteractionsStartHandlingClients() throws RTIexception {
        //Send interaction startHandlingClient
        for (CashRegister cR : cashRegisterList) {
            if(cR.isFree()) {
                //Find queue pass to cashRegister
                Queue queue = null;
                for (Queue q : queueList) {
                    if(q.getNumberCashRegister() == cR.getNumberCashRegister()) {
                        queue = q;
                        break;
                    }
                }

                if (queueList.get(queueList.indexOf(queue)).getClientList().size() > 0) {
                    Client clientToSend = queueList.get(queueList.indexOf(queue)).getFirstClient();
                    sendInteraction(queue.getNumberQueue(), cR.getNumberCashRegister(), clientToSend.getNumber(), clientToSend.getAmountOfArticles());
                    cR.setFree(false);
                }
            }
        }
    }


    private void registerNewQueue(long cashRegisterNumber) throws RTIexception {
        queueList.add(new Queue(registerStorageObject(), cashRegisterNumber, cashRegisterNumber));
        log("Register queue object: QueueNumber=" + cashRegisterNumber +", CashRegisterNumber=" + cashRegisterNumber +", Length=" +queueLengthStartNr);
    }

    private ObjectInstanceHandle registerStorageObject() throws RTIexception {
        return rtiamb.registerObjectInstance(queueHandle);
    }

    private void publishAndSubscribe() throws RTIexception {
        //Publish
        //Object queue
        this.queueHandle = rtiamb.getObjectClassHandle(ConfigConstants.QUEUE_OBJ_NAME);
        this.queueNumberHandleQueue = rtiamb.getAttributeHandle(this.queueHandle, ConfigConstants.QUEUE_NUMBER_NAME);
        this.cashRegisterNumberHandleQueue = rtiamb.getAttributeHandle(this.queueHandle, ConfigConstants.CASH_REGISTER_NUMBER_NAME);
        this.queueLengthHandleQueue = rtiamb.getAttributeHandle(this.queueHandle, ConfigConstants.QUEUE_LENGTH_NAME);
        AttributeHandleSet attributesQueue = rtiamb.getAttributeHandleSetFactory().create();
        attributesQueue.add(this.queueNumberHandleQueue);
        attributesQueue.add(this.cashRegisterNumberHandleQueue);
        attributesQueue.add(this.queueLengthHandleQueue);
        rtiamb.publishObjectClassAttributes(queueHandle, attributesQueue);
        //Interaction start handling client
        this.startHandlingClientHandle = rtiamb.getInteractionClassHandle(ConfigConstants.START_HANDLING_CLIENT_INTERACTION_NAME);
        this.queueNumberHandleStartHandlingClient = rtiamb.getParameterHandle(this.startHandlingClientHandle, ConfigConstants.QUEUE_NUMBER_NAME);
        this.cashRegisterNumberHandleStartHandlingClient = rtiamb.getParameterHandle(this.startHandlingClientHandle, ConfigConstants.CASH_REGISTER_NUMBER_NAME);
        this.clientNumberHandleStartHandlingClient = rtiamb.getParameterHandle(this.startHandlingClientHandle, ConfigConstants.CLIENT_NUMBER_NAME);
        this.amountOfArticlesHandleStartHandlingClient = rtiamb.getParameterHandle(this.startHandlingClientHandle, ConfigConstants.AMOUNT_OF_ARTICLES_NAME);
        rtiamb.publishInteractionClass(startHandlingClientHandle);

        //Subscribe
        //Object cash register
        this.cashRegisterHandle = rtiamb.getObjectClassHandle(ConfigConstants.CASH_REGISTER_OBJ_NAME);
        this.cashRegisterNumberHandleCashRegister = rtiamb.getAttributeHandle(this.cashRegisterHandle, ConfigConstants.CASH_REGISTER_NUMBER_NAME);
        this.isFreeHandleCashRegister = rtiamb.getAttributeHandle(this.cashRegisterHandle, ConfigConstants.CASH_REGISTER_IS_FREE_NAME);
        AttributeHandleSet attributesCashRegister = rtiamb.getAttributeHandleSetFactory().create();
        attributesCashRegister.add(this.cashRegisterNumberHandleCashRegister);
        attributesCashRegister.add(this.isFreeHandleCashRegister);
        rtiamb.subscribeObjectClassAttributes(cashRegisterHandle, attributesCashRegister);
        //Interaction join client to queue
        this.joinClientToQueueHandle = rtiamb.getInteractionClassHandle(ConfigConstants.JOIN_CLIENT_TO_QUEUE_INTERACTION_NAME);
        rtiamb.subscribeInteractionClass(joinClientToQueueHandle);
        this.clientNumberHandleJoinClientToQueue = rtiamb.getParameterHandle(this.joinClientToQueueHandle, ConfigConstants.CLIENT_NUMBER_NAME);
        this.queueNumberHandleJoinClientToQueue = rtiamb.getParameterHandle(this.joinClientToQueueHandle, ConfigConstants.QUEUE_NUMBER_NAME);
        this.amountOfArticlesHandleJoinClientToQueue = rtiamb.getParameterHandle(this.joinClientToQueueHandle, ConfigConstants.AMOUNT_OF_ARTICLES_NAME);
        //Interaction open new cash register
        this.openNewCashRegisterHandle = rtiamb.getInteractionClassHandle(ConfigConstants.OPEN_NEW_CASH_REGISTER_INTERACTION_NAME);
        rtiamb.subscribeInteractionClass(openNewCashRegisterHandle);
        this.cashRegisterNumberHandleOpenNewCashRegister = rtiamb.getParameterHandle(this.openNewCashRegisterHandle, ConfigConstants.CASH_REGISTER_NUMBER_NAME);
        this.queueNumberHandleOpenNewCashRegister = rtiamb.getParameterHandle(this.openNewCashRegisterHandle, ConfigConstants.QUEUE_NUMBER_NAME);
    }

    private void updateHLAObjects(double time) throws RTIexception {
        //Object queue
        for (Queue q : queueList) {
            if(q.isToUpdate()) {
                AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(3);
                HLAinteger64BE queueNumber = encoderFactory.createHLAinteger64BE(q.getNumberQueue());
                attributes.put(this.queueNumberHandleQueue, queueNumber.toByteArray());
                HLAinteger64BE cashRegisterNumber = encoderFactory.createHLAinteger64BE(q.getNumberCashRegister());
                attributes.put(this.cashRegisterNumberHandleQueue, cashRegisterNumber.toByteArray());
                HLAinteger64BE queueLength = encoderFactory.createHLAinteger64BE(q.getLength());
                attributes.put(this.queueLengthHandleQueue, queueLength.toByteArray());
                HLAfloat64Time logicalTime = timeFactory.makeTime(time);
                rtiamb.updateAttributeValues(q.getObjectInstanceHandle(), attributes, generateTag(), logicalTime);
                q.setToUpdate(false);
                log("Update queue object: queueNumber=" + q.getNumberQueue() +", cashRegisterNumber=" + q.getNumberCashRegister() + ", length=" + q.getLength() +  ", at time= " + time);
            }
        }
    }

    private void sendInteraction(long queueNumber, long cashRegisterNumber, long clientNumber, long amountOfArticlesNumber) throws RTIexception {
        //Interaction start handling client
        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + timeStep + fedamb.federateLookahead);
        ParameterHandleValueMap parametersStartHandlingClient = rtiamb.getParameterHandleValueMapFactory().create(4);
        HLAinteger64BE queueNumberStartHandlingClient = encoderFactory.createHLAinteger64BE( queueNumber );
        HLAinteger64BE cashRegisterNumberStartHandlingClient = encoderFactory.createHLAinteger64BE( cashRegisterNumber );
        HLAinteger64BE clientNumberStartHandlingClient = encoderFactory.createHLAinteger64BE( clientNumber );
        HLAinteger64BE amountOfArticlesStartHandlingClient = encoderFactory.createHLAinteger64BE( amountOfArticlesNumber);
        parametersStartHandlingClient.put(this.queueNumberHandleStartHandlingClient, queueNumberStartHandlingClient.toByteArray());
        parametersStartHandlingClient.put(this.cashRegisterNumberHandleStartHandlingClient, cashRegisterNumberStartHandlingClient.toByteArray());
        parametersStartHandlingClient.put(this.clientNumberHandleStartHandlingClient, clientNumberStartHandlingClient.toByteArray());
        parametersStartHandlingClient.put(this.amountOfArticlesHandleStartHandlingClient, amountOfArticlesStartHandlingClient.toByteArray());

        rtiamb.sendInteraction(this.startHandlingClientHandle, parametersStartHandlingClient, generateTag(), time);
    }

    public static void main(String[] args) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        try {
            new QueueFederate().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }
}
