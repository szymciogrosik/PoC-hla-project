package ieee1516e.manager;

import hla.rti1516e.*;
import hla.rti1516e.encoding.HLAinteger64BE;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.time.HLAfloat64Time;
import ieee1516e.cashRegister.CashRegister;
import ieee1516e.constants.ConfigConstants;
import ieee1516e.queue.Queue;
import ieee1516e.tamplate.BaseFederate;

import java.util.ArrayList;

public class ManagerFederate extends BaseFederate<ManagerAmbassador> {
    private final double timeStep           = 1.0;

    //Publish
    //Interaction open new cash register and queue
    private InteractionClassHandle openNewCashRegisterHandle;
    private ParameterHandle cashRegisterNumberHandleOpenNewCashRegister;
    private ParameterHandle queueNumberHandleOpenNewCashRegister;

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

    private ArrayList<CashRegister> cashRegistersList = new ArrayList<>();
    private ArrayList<Queue> queueList = new ArrayList<>();

    private void runFederate() throws RTIexception, IllegalAccessException, InstantiationException, ClassNotFoundException {
        this.setFederateName(ConfigConstants.MANAGER_FED);

        // Create ambassador, tryCreateFederation, and waiting for first sync
        init(ManagerAmbassador.class.getCanonicalName());

        publishAndSubscribe();

        while (fedamb.running) {
            double timeToAdvance = fedamb.federateTime + timeStep;
            advanceTime(timeStep);

            if(fedamb.externalObjects.size() > 0) {
                fedamb.externalObjects.sort(new ManagerExternalObject.ExternalObjectComparator());
                for(ManagerExternalObject externalObject : fedamb.externalObjects) {
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

                            boolean queueNotExist = true;
                            for (Queue q : queueList) {
                                if(q.getNumberQueue() == queueNumberDecoded) {
                                    q.setLength(queueLengthDecoded);
                                    queueNotExist = false;
                                    break;
                                }
                            }

                            if(queueNotExist)
                                queueList.add(new Queue(queueNumberDecoded, cashRegisterNumberDecoded, queueLengthDecoded));
                            break;
                        case CASH_REGISTER:
                            long cashRegisterNumberDecoded2 = decodeIntValue(externalObject.getAttributes().get(this.cashRegisterNumberHandleCashRegister));
                            boolean isFreeDecoded = decodeBooleanValue(externalObject.getAttributes().get(this.isFreeHandleCashRegister));
                            log("In case object: CASH_REGISTER | Nr kasy: " +
                                    cashRegisterNumberDecoded2 +
                                    ", Czy wolna: " +
                                    isFreeDecoded
                            );

                            boolean cashRegisterNotExist = true;
                            for (CashRegister cR : cashRegistersList) {
                                if(cR.getNumberCashRegister() == cashRegisterNumberDecoded2) {
                                    cR.setFree(isFreeDecoded);
                                    cashRegisterNotExist = false;
                                    break;
                                }
                            }

                            if(cashRegisterNotExist)
                                cashRegistersList.add(new CashRegister(cashRegisterNumberDecoded2, isFreeDecoded));
                            break;
                        default:
                            log("Undetected object.");
                            break;
                    }
                }
                fedamb.externalObjects.clear();
            }

            if(isClientsToMany()) {
                sendInteractionOpenNewCashRegister();
            }

            if(fedamb.grantedTime == timeToAdvance) {
                timeToAdvance += fedamb.federateLookahead;
                log("Updating manager time: " + timeToAdvance);
                fedamb.federateTime = timeToAdvance;
            }

            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }
    }

    private boolean isClientsToMany() {
        boolean isToMany = true;
        for (Queue q : queueList) {
            if(q.getLength() < ConfigConstants.MAX_CLIENTS_NUMBER_IN_QUEUES) {
                isToMany = false;
                break;
            }
        }
        if (queueList.size() == 0)
            //Jeżeli manager jeszcze nie dostał info o utworzonych kolejkach.
            return false;
        else
            return isToMany;
    }

    private void sendInteractionOpenNewCashRegister() throws RTIexception {
        long cashRegisterNumberToSend = 0;
        long queueNumberToSend = 0;

        for (CashRegister cR : cashRegistersList) {
            if(cR.getNumberCashRegister() > cashRegisterNumberToSend)
                cashRegisterNumberToSend = cR.getNumberCashRegister();
        }

        for (Queue q : queueList) {
            if(q.getNumberQueue() > queueNumberToSend)
                queueNumberToSend = q.getNumberQueue();
        }

        cashRegisterNumberToSend++;
        queueNumberToSend++;

        sendInteraction(cashRegisterNumberToSend, queueNumberToSend);

        //Add to Manager queue list and cashRegister list one of queue and cashRegister because send to many interactions.
        queueList.add(new Queue(queueNumberToSend, cashRegisterNumberToSend, 0));
        cashRegistersList.add(new CashRegister(cashRegisterNumberToSend, true));
    }

    private void sendInteraction(long cashRegisterNumber, long queueNumber) throws RTIexception {
        // Send Interaction openNewCashRegister
        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + timeStep + fedamb.federateLookahead);
        ParameterHandleValueMap parameters1 = rtiamb.getParameterHandleValueMapFactory().create(2);
        HLAinteger64BE cashRegisterSend = encoderFactory.createHLAinteger64BE( cashRegisterNumber );
        HLAinteger64BE queueNumberClientSend = encoderFactory.createHLAinteger64BE( queueNumber );
        parameters1.put(cashRegisterNumberHandleOpenNewCashRegister, cashRegisterSend.toByteArray());
        parameters1.put(queueNumberHandleOpenNewCashRegister, queueNumberClientSend.toByteArray());
        rtiamb.sendInteraction(openNewCashRegisterHandle, parameters1, generateTag(), time);
    }

    private void publishAndSubscribe() throws RTIexception {
        //Publish
        //Interaction joinClientToQueueHandle
        this.openNewCashRegisterHandle = rtiamb.getInteractionClassHandle(ConfigConstants.OPEN_NEW_CASH_REGISTER_INTERACTION_NAME);
        cashRegisterNumberHandleOpenNewCashRegister = rtiamb.getParameterHandle(this.openNewCashRegisterHandle, ConfigConstants.CASH_REGISTER_NUMBER_NAME);
        queueNumberHandleOpenNewCashRegister = rtiamb.getParameterHandle(this.openNewCashRegisterHandle, ConfigConstants.QUEUE_NUMBER_NAME);
        rtiamb.publishInteractionClass(openNewCashRegisterHandle);

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
    }

    public static void main(String[] args) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        try {
            new ManagerFederate().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }
}
