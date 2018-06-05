package ieee1516e.statistic;

import hla.rti1516e.*;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.time.HLAfloat64Time;
import ieee1516e.constants.ConfigConstants;
import ieee1516e.tamplate.BaseFederate;

public class StatisticFederate extends BaseFederate<StatisticAmbassador> {
    private final double timeStep           = 1.0;

    //Publish
    //Interaction end simulation
    private InteractionClassHandle endSimulationHandle;

    //Subscribe
    //Object queue
    private ObjectClassHandle queueHandle;
    private AttributeHandle queueNumberQueue;
    private AttributeHandle cashRegisterQueue;
    private AttributeHandle queueLengthQueue;
    //Object cash register
    private ObjectClassHandle cashRegisterHandle;
    private AttributeHandle cashRegisterNumberHandleCashRegister;
    private AttributeHandle isFreeHandleCashRegister;
    //Interaction start handling client
    private InteractionClassHandle startHandlingClientHandle;
    private ParameterHandle queueNumberHandleStartHandlingClient;
    private ParameterHandle cashRegisterNumberHandleStartHandlingClient;
    private ParameterHandle clientNumberHandleStartHandlingClient;
    private ParameterHandle amountOfArticlesHandleStartHandlingClient;
    //Interaction join client to queue
    private InteractionClassHandle joinClientToQueueHandle;
    private ParameterHandle clientNumberHandleJoinClientToQueue;
    private ParameterHandle queueNumberHandleJoinClientToQueue;
    private ParameterHandle amountOfArticlesHandleJoinClientToQueue;

    private void runFederate() throws RTIexception, IllegalAccessException, InstantiationException, ClassNotFoundException {
        this.setFederateName(ConfigConstants.STATISTIC_FED);

        // Create ambassador, tryCreateFederation, and waiting for first sync
        init(StatisticAmbassador.class.getCanonicalName());

        publishAndSubscribe();

        while (fedamb.running) {
            double timeToAdvance = fedamb.federateTime + timeStep;
            advanceTime(timeStep);

            if(fedamb.externalEvents.size() > 0) {
                fedamb.externalEvents.sort(new StatisticExternalEvent.ExternalEventComparator());
                for(StatisticExternalEvent externalEvent : fedamb.externalEvents) {
                    switch (externalEvent.getEventType()) {
                        case JOIN_CLIENT_TO_QUEUE:
                            log("In case interaction: JOIN_CLIENT_TO_QUEUE | Nr klienta: " +
                                    decodeIntValue(externalEvent.getAttributes().get(this.clientNumberHandleJoinClientToQueue)) +
                                    ", Nr kolejki: " +
                                    decodeIntValue(externalEvent.getAttributes().get(this.queueNumberHandleJoinClientToQueue)) +
                                    ", Liczba artykulow: " +
                                    decodeIntValue(externalEvent.getAttributes().get(this.amountOfArticlesHandleJoinClientToQueue))
                            );
                            break;
                        case START_HANDLING_CLIENT:
                            log("In case interaction: START_HANDLING_CLIENT | Nr kolejki: " +
                                    decodeIntValue(externalEvent.getAttributes().get(this.queueNumberHandleStartHandlingClient)) +
                                    ", Nr kasy: " +
                                    decodeIntValue(externalEvent.getAttributes().get(this.cashRegisterNumberHandleStartHandlingClient)) +
                                    ", Nr klienta: " +
                                    decodeIntValue(externalEvent.getAttributes().get(this.clientNumberHandleStartHandlingClient))+
                                    ", Liczba zakupow: " +
                                    decodeIntValue(externalEvent.getAttributes().get(this.amountOfArticlesHandleStartHandlingClient))
                            );
                            break;
                        default:
                            log("Undetected interaction.");
                            break;
                    }
                }
                fedamb.externalEvents.clear();
            }

            if(fedamb.externalObjects.size() > 0) {
                fedamb.externalObjects.sort(new StatisticExternalObject.ExternalObjectComparator());
                for(StatisticExternalObject externalObject : fedamb.externalObjects) {
                    switch (externalObject.getObjectType()) {
                        case QUEUE:
                            log("In case object: QUEUE | Nr kolejki: " +
                                    decodeIntValue(externalObject.getAttributes().get(this.queueNumberQueue)) +
                                    ", Nr kasy: " +
                                    decodeIntValue(externalObject.getAttributes().get(this.cashRegisterQueue)) +
                                    ", Dlugosc kolejki: " +
                                    decodeIntValue(externalObject.getAttributes().get(this.queueLengthQueue))
                            );
                            break;
                        case CASH_REGISTER:
                            log("In case object: CASH_REGISTER | Nr kasy: " +
                                    decodeIntValue(externalObject.getAttributes().get(this.cashRegisterNumberHandleCashRegister)) +
                                    ", Czy wolna: " +
                                    decodeBooleanValue(externalObject.getAttributes().get(this.isFreeHandleCashRegister))
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
                log("Updating statistic time: " + timeToAdvance);
                fedamb.federateTime = timeToAdvance;
            }

            if(ConfigConstants.SIMULATION_TIME < fedamb.federateTime && ConfigConstants.SIMULATION_TIME != 0) {
                sendInteractionEndSimulation();
            }

            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }

        try {
            resign();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendInteractionEndSimulation() throws RTIexception {
        // Send Interaction endSimulation
        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + timeStep + fedamb.federateLookahead);
        ParameterHandleValueMap parameters1 = rtiamb.getParameterHandleValueMapFactory().create(0);
        rtiamb.sendInteraction(endSimulationHandle, parameters1, generateTag(), time);
        log("SEND Interaction END_SIMULATION");
        fedamb.running = false;
    }

    private void publishAndSubscribe() throws RTIexception {
        //Publish
        //Interaction endSimulation
        this.endSimulationHandle = rtiamb.getInteractionClassHandle(ConfigConstants.END_SIMULATION_INTERACTION_NAME);
        rtiamb.publishInteractionClass(endSimulationHandle);

        //Subscribe
        //Object cash register
        this.cashRegisterHandle = rtiamb.getObjectClassHandle(ConfigConstants.CASH_REGISTER_OBJ_NAME);
        this.cashRegisterNumberHandleCashRegister = rtiamb.getAttributeHandle(this.cashRegisterHandle, ConfigConstants.CASH_REGISTER_NUMBER_NAME);
        this.isFreeHandleCashRegister = rtiamb.getAttributeHandle(this.cashRegisterHandle, ConfigConstants.CASH_REGISTER_IS_FREE_NAME);
        AttributeHandleSet attributesCashRegister = rtiamb.getAttributeHandleSetFactory().create();
        attributesCashRegister.add(this.cashRegisterNumberHandleCashRegister);
        attributesCashRegister.add(this.isFreeHandleCashRegister);
        rtiamb.subscribeObjectClassAttributes(cashRegisterHandle, attributesCashRegister);
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
        //Interaction start handling client
        this.startHandlingClientHandle = rtiamb.getInteractionClassHandle(ConfigConstants.START_HANDLING_CLIENT_INTERACTION_NAME);
        rtiamb.subscribeInteractionClass(startHandlingClientHandle);
        this.queueNumberHandleStartHandlingClient = rtiamb.getParameterHandle(this.startHandlingClientHandle, ConfigConstants.QUEUE_NUMBER_NAME);
        this.cashRegisterNumberHandleStartHandlingClient = rtiamb.getParameterHandle(this.startHandlingClientHandle, ConfigConstants.CASH_REGISTER_NUMBER_NAME);
        this.clientNumberHandleStartHandlingClient = rtiamb.getParameterHandle(this.startHandlingClientHandle, ConfigConstants.CLIENT_NUMBER_NAME);
        this.amountOfArticlesHandleStartHandlingClient = rtiamb.getParameterHandle(this.startHandlingClientHandle, ConfigConstants.AMOUNT_OF_ARTICLES_NAME);
        //Interaction join client to queue
        this.joinClientToQueueHandle = rtiamb.getInteractionClassHandle(ConfigConstants.JOIN_CLIENT_TO_QUEUE_INTERACTION_NAME);
        rtiamb.subscribeInteractionClass(joinClientToQueueHandle);
        this.clientNumberHandleJoinClientToQueue = rtiamb.getParameterHandle(this.joinClientToQueueHandle, ConfigConstants.CLIENT_NUMBER_NAME);
        this.queueNumberHandleJoinClientToQueue = rtiamb.getParameterHandle(this.joinClientToQueueHandle, ConfigConstants.QUEUE_NUMBER_NAME);
        this.amountOfArticlesHandleJoinClientToQueue = rtiamb.getParameterHandle(this.joinClientToQueueHandle, ConfigConstants.AMOUNT_OF_ARTICLES_NAME);

    }



    public static void main(String[] args) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        try {
            new StatisticFederate().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }
}
