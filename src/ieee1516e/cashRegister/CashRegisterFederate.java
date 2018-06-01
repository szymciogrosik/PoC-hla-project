package ieee1516e.cashRegister;

import hla.rti1516e.*;
import hla.rti1516e.encoding.HLAboolean;
import hla.rti1516e.encoding.HLAinteger64BE;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.time.HLAfloat64Time;
import ieee1516e.constants.ConfigConstants;
import ieee1516e.tamplate.BaseFederate;

public class CashRegisterFederate extends BaseFederate<CashRegisterAmbassador> {
    private final double timeStep           = 5.0;

    //Publish
    //Object cash register
    private ObjectClassHandle cashRegisterHandle;
    private AttributeHandle cashRegisterNumberHandleCashRegister;
    private AttributeHandle isFreeHandleCashRegister;

    //Subscribe
    //Interaction start handling client
    private InteractionClassHandle startHandlingClientHandle;
    private ParameterHandle queueNumberHandleStartHandlingClient;
    private ParameterHandle cashRegisterNumberHandleStartHandlingClient;
    private ParameterHandle clientNumberHandleStartHandlingClient;
    private ParameterHandle amountOfArticlesHandleStartHandlingClient;
    //Interaction open new cash register
    private InteractionClassHandle openNewCashRegisterHandle;
    private ParameterHandle cashRegisterNumberHandleOpenNewCashRegister;
    private ParameterHandle queueNumberHandleOpenNewCashRegister;

    private int cashRegisterNr              = 111;
    private boolean isFree                  = true;
    private ObjectInstanceHandle cashRegister;

    private void runFederate() throws RTIexception, IllegalAccessException, InstantiationException, ClassNotFoundException {
        this.setFederateName(ConfigConstants.CASH_REGISTER_FED);

        // Create ambassador, tryCreateFederation, and waiting for first sync
        init(CashRegisterAmbassador.class.getCanonicalName());

        publishAndSubscribe();
        log("Published and Subscribed");
        cashRegister = registerStorageObject();
        log("Registered Object");

        while (fedamb.running) {
            double timeToAdvance = fedamb.federateTime + timeStep;
            advanceTime(timeStep);

            if(fedamb.externalEvents.size() > 0) {
                fedamb.externalEvents.sort(new CashRegisterExternalEvent.ExternalEventComparator());
                for(CashRegisterExternalEvent externalEvent : fedamb.externalEvents) {
                    switch (externalEvent.getEventType()) {
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
                        case OPEN_NEW_CASH_REGISTER:
                            log("In case interaction: OPEN_NEW_CASH_REGISTER | Nr kasy: " +
                                    decodeIntValue(externalEvent.getAttributes().get(this.cashRegisterNumberHandleOpenNewCashRegister)) +
                                    ", Nr kolejki: " +
                                    decodeIntValue(externalEvent.getAttributes().get(this.queueNumberHandleOpenNewCashRegister))
                            );
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
                log("Updating cash register at time: " + timeToAdvance);
                updateHLAObject(timeToAdvance);
                fedamb.federateTime = timeToAdvance;
            }

            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }
    }

    private ObjectInstanceHandle registerStorageObject() throws RTIexception {
        return rtiamb.registerObjectInstance(cashRegisterHandle);
    }

    private void publishAndSubscribe() throws RTIexception {
        //Publish
        //Object cash register
        this.cashRegisterHandle = rtiamb.getObjectClassHandle(ConfigConstants.CASH_REGISTER_OBJ_NAME);
        this.cashRegisterNumberHandleCashRegister = rtiamb.getAttributeHandle(cashRegisterHandle, ConfigConstants.CASH_REGISTER_NUMBER_NAME);
        this.isFreeHandleCashRegister = rtiamb.getAttributeHandle(this.cashRegisterHandle, ConfigConstants.CASH_REGISTER_IS_FREE_NAME);
        AttributeHandleSet attributesQueue = rtiamb.getAttributeHandleSetFactory().create();
        attributesQueue.add(this.cashRegisterNumberHandleCashRegister);
        attributesQueue.add(this.isFreeHandleCashRegister);
        rtiamb.publishObjectClassAttributes(cashRegisterHandle, attributesQueue);

        //Subscribe
        //Interaction start handling client
        this.startHandlingClientHandle = rtiamb.getInteractionClassHandle(ConfigConstants.START_HANDLING_CLIENT_INTERACTION_NAME);
        rtiamb.subscribeInteractionClass(startHandlingClientHandle);
        this.queueNumberHandleStartHandlingClient = rtiamb.getParameterHandle(this.startHandlingClientHandle, ConfigConstants.QUEUE_NUMBER_NAME);
        this.cashRegisterNumberHandleStartHandlingClient = rtiamb.getParameterHandle(this.startHandlingClientHandle, ConfigConstants.CASH_REGISTER_NUMBER_NAME);
        this.clientNumberHandleStartHandlingClient = rtiamb.getParameterHandle(this.startHandlingClientHandle, ConfigConstants.CLIENT_NUMBER_NAME);
        this.amountOfArticlesHandleStartHandlingClient = rtiamb.getParameterHandle(this.startHandlingClientHandle, ConfigConstants.AMOUNT_OF_ARTICLES_NAME);
        //Interaction open new cash register
        this.openNewCashRegisterHandle = rtiamb.getInteractionClassHandle(ConfigConstants.OPEN_NEW_CASH_REGISTER_INTERACTION_NAME);
        rtiamb.subscribeInteractionClass(openNewCashRegisterHandle);
        this.cashRegisterNumberHandleOpenNewCashRegister = rtiamb.getParameterHandle(this.openNewCashRegisterHandle, ConfigConstants.CASH_REGISTER_NUMBER_NAME);
        this.queueNumberHandleOpenNewCashRegister = rtiamb.getParameterHandle(this.openNewCashRegisterHandle, ConfigConstants.QUEUE_NUMBER_NAME);
    }

    private void updateHLAObject(double time) throws RTIexception {
        AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(2);
        HLAinteger64BE cashRegisterNumber = encoderFactory.createHLAinteger64BE(cashRegisterNr);
        attributes.put(this.cashRegisterNumberHandleCashRegister, cashRegisterNumber.toByteArray());
        HLAboolean isFreeToSend = encoderFactory.createHLAboolean(isFree);
        attributes.put(this.isFreeHandleCashRegister, isFreeToSend.toByteArray());
        HLAfloat64Time logicalTime = timeFactory.makeTime(time);
        rtiamb.updateAttributeValues(cashRegister, attributes, generateTag(), logicalTime);
    }

    public static void main(String[] args) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        try {
            new CashRegisterFederate().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }
}
