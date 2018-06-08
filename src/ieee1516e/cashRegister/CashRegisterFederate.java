package ieee1516e.cashRegister;

import hla.rti1516e.*;
import hla.rti1516e.encoding.HLAboolean;
import hla.rti1516e.encoding.HLAinteger64BE;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.time.HLAfloat64Time;
import ieee1516e.client.Client;
import ieee1516e.constants.ConfigConstants;
import ieee1516e.tamplate.BaseFederate;

import java.util.ArrayList;

public class CashRegisterFederate extends BaseFederate<CashRegisterAmbassador> {
    private final double timeStep           = 1.0;

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
    //Interaction end simulation
    private InteractionClassHandle endSimulationHandle;

    private boolean isFreeStartFlag = true;

    private ArrayList<CashRegister> cashRegisterList = new ArrayList<>();
    private ArrayList<Client> handlingClientList = new ArrayList<>();

    private void runFederate() throws RTIexception, IllegalAccessException, InstantiationException, ClassNotFoundException {
        this.setFederateName(ConfigConstants.CASH_REGISTER_FED);

        // Create ambassador, tryCreateFederation, and waiting for first sync
        init(CashRegisterAmbassador.class.getCanonicalName());

        publishAndSubscribe();
        log("Published and Subscribed");
        for (int i=0; i<ConfigConstants.START_ALL_CASH_REGISTER_NUMBER; i++) {
            registerNewCashRegister(i);
        }

        while (fedamb.running) {
            //Check, any cashRegister should be open yet?
            for (Client c : handlingClientList) {
                if(c.getTimeToEndHandling() <= fedamb.federateTime) {
                    for (CashRegister cR : cashRegisterList) {
                        if(cR.getNumberCashRegister() == c.getCashRegisterNumber()) {
                            cR.setFree(true);
                            cR.setToUpdate(true);
                            break;
                        }
                    }
                    log("Klient: " + c.getNumber() +" opuścił kasę nr: " + c.getCashRegisterNumber() + ", o czasie: " +fedamb.federateTime);
                    handlingClientList.remove(c);
                    break;
                }
            }

            double timeToAdvance = fedamb.federateTime + timeStep;
            advanceTime(timeToAdvance);

            if(fedamb.externalEvents.size() > 0) {
                fedamb.externalEvents.sort(new CashRegisterExternalEvent.ExternalEventComparator());
                for(CashRegisterExternalEvent externalEvent : fedamb.externalEvents) {
                    switch (externalEvent.getEventType()) {
                        case START_HANDLING_CLIENT:
                            long queueNumberDecoded = decodeIntValue(externalEvent.getAttributes().get(this.queueNumberHandleStartHandlingClient));
                            long cashRegisterNumberDecoded = decodeIntValue(externalEvent.getAttributes().get(this.cashRegisterNumberHandleStartHandlingClient));
                            long clientNumberDecoded = decodeIntValue(externalEvent.getAttributes().get(this.clientNumberHandleStartHandlingClient));
                            long amountOfArticlesDecoded = decodeIntValue(externalEvent.getAttributes().get(this.amountOfArticlesHandleStartHandlingClient));
                            log("In case interaction: START_HANDLING_CLIENT | Nr kolejki: " +
                                    queueNumberDecoded +
                                    ", Nr kasy: " +
                                    cashRegisterNumberDecoded +
                                    ", Nr klienta: " +
                                    clientNumberDecoded+
                                    ", Liczba zakupow: " +
                                    amountOfArticlesDecoded
                            );

                            double timeHandlingClientInCashRegister = fedamb.federateTime + amountOfArticlesDecoded * ConfigConstants.CASH_REGISTER_TIME_TO_SCAN_ONE_ARTICLE;
                            handlingClientList.add(new Client(
                                        clientNumberDecoded,
                                        amountOfArticlesDecoded,
                                        timeHandlingClientInCashRegister,
                                        cashRegisterNumberDecoded
                                    ));
                            break;
                        case OPEN_NEW_CASH_REGISTER:
                            long cashRegisterNumberDecoded2 = decodeIntValue(externalEvent.getAttributes().get(this.cashRegisterNumberHandleOpenNewCashRegister));
                            long queueNumberDecoded2 = decodeIntValue(externalEvent.getAttributes().get(this.queueNumberHandleOpenNewCashRegister));
                            log("In case interaction: OPEN_NEW_CASH_REGISTER | Nr kasy: " +
                                    cashRegisterNumberDecoded2 +
                                    ", Nr kolejki: " +
                                    queueNumberDecoded2
                            );
                            registerNewCashRegister(cashRegisterNumberDecoded2);
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
                log("Updating cash register time: " + timeToAdvance);
                updateHLAObjects(timeToAdvance);
                fedamb.federateTime = timeToAdvance;
            }
        }

        try {
            resign();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerNewCashRegister(long cahsRegisterNumber) throws RTIexception {
        cashRegisterList.add(new CashRegister(registerStorageObject(), cahsRegisterNumber, isFreeStartFlag));
        log("Register cashRegister object: CashRegister=" + cahsRegisterNumber +", isFree=" + isFreeStartFlag);
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
        //Interaction open new cash register
        this.endSimulationHandle = rtiamb.getInteractionClassHandle(ConfigConstants.END_SIMULATION_INTERACTION_NAME);
        rtiamb.subscribeInteractionClass(endSimulationHandle);
    }

    private void updateHLAObjects(double time) throws RTIexception {
        for (CashRegister cR : cashRegisterList) {
            if(cR.isToUpdate()) {
                AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(2);
                HLAinteger64BE cashRegisterNumber = encoderFactory.createHLAinteger64BE(cR.getNumberCashRegister());
                attributes.put(this.cashRegisterNumberHandleCashRegister, cashRegisterNumber.toByteArray());
                HLAboolean isFreeToSend = encoderFactory.createHLAboolean(cR.isFree());
                attributes.put(this.isFreeHandleCashRegister, isFreeToSend.toByteArray());
                HLAfloat64Time logicalTime = timeFactory.makeTime(time);
                rtiamb.updateAttributeValues(cR.getObjectInstanceHandle(), attributes, generateTag(), logicalTime);
                cR.setToUpdate(false);
                log("Update cashRegister object: CashRegisterNumber=" + cR.getNumberCashRegister() +", isFree=" + cR.isFree() + ", at time= " + time);
            }
        }
    }

    public static void main(String[] args) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        try {
            new CashRegisterFederate().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }
}
