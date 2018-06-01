package ieee1516e.manager;

import hla.rti1516e.*;
import hla.rti1516e.encoding.HLAinteger64BE;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.time.HLAfloat64Time;
import ieee1516e.constants.ConfigConstants;
import ieee1516e.tamplate.BaseFederate;

public class ManagerFederate extends BaseFederate<ManagerAmbassador> {
    private final double timeStep           = 3.0;

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

    private void runFederate() throws RTIexception, IllegalAccessException, InstantiationException, ClassNotFoundException {
        this.setFederateName(ConfigConstants.MANAGER_FED);

        // Create ambassador, tryCreateFederation, and waiting for first sync
        init(ManagerAmbassador.class.getCanonicalName());

        publishAndSubscribe();

        while (fedamb.running) {
            double timeToAdvance = fedamb.federateTime + timeStep;
            advanceTime(timeStep);

            sendInteraction(timeToAdvance + fedamb.federateLookahead);

            if(fedamb.externalObjects.size() > 0) {
                fedamb.externalObjects.sort(new ManagerExternalObject.ExternalObjectComparator());
                for(ManagerExternalObject externalObject : fedamb.externalObjects) {
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
                log("Updating manager time: " + timeToAdvance);
                fedamb.federateTime = timeToAdvance;
//            waitForUser();
            }

            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }
    }

    private void sendInteraction(double timeStep) throws RTIexception {
        // Send Interaction openNewCashRegister
        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + timeStep + fedamb.federateLookahead);
        ParameterHandleValueMap parameters1 = rtiamb.getParameterHandleValueMapFactory().create(2);
        HLAinteger64BE cashRegisterSend = encoderFactory.createHLAinteger64BE( 1 );
        HLAinteger64BE queueNumberClientSend = encoderFactory.createHLAinteger64BE( 5 );
        parameters1.put(cashRegisterNumberHandleOpenNewCashRegister, cashRegisterSend.toByteArray());
        parameters1.put(queueNumberHandleOpenNewCashRegister, queueNumberClientSend.toByteArray());
        rtiamb.sendInteraction(openNewCashRegisterHandle, parameters1, generateTag(), time);
    }

    protected void publishAndSubscribe() throws RTIexception {
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
