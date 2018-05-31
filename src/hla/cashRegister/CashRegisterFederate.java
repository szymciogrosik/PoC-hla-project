package hla.cashRegister;

import hla.constants.ConfigConstants;
import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import hla.tamplate.BaseFederate;

public class CashRegisterFederate extends BaseFederate<CashRegisterAmbassador> {
    private int cashRegisterHlaHandle;
    private final double timeStep           = 5.0;

    private int cashRegisterNr              = 111;
    private boolean isFree                  = true;

    private void runFederate() throws RTIexception, IllegalAccessException, InstantiationException, ClassNotFoundException {
        this.setFederateName(ConfigConstants.CASH_REGISTER_FED);

        // Create ambassador, tryCreateFederation, and waiting for first sync
        init(CashRegisterAmbassador.class.getCanonicalName());

        publishAndSubscribe();
        registerStorageObject();

        while (fedamb.running) {
            double timeToAdvance = fedamb.federateTime + timeStep;
            advanceTime(timeStep);

            if(fedamb.externalEvents.size() > 0) {
                fedamb.externalEvents.sort(new CashRegisterExternalEvent.ExternalEventComparator());
                for(CashRegisterExternalEvent externalEvent : fedamb.externalEvents) {
                    switch (externalEvent.getEventType()) {
                        case START_HANDLING_CLIENT:
                            log("START_HANDLING_CLIENT");
                            break;
                        case OPEN_NEW_CASH_REGISTER:
                            log("OPEN_NEW_CASH_REGISTER");
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

            rtiamb.tick();
        }
    }

    private void registerStorageObject() throws RTIexception {
        int classHandle = rtiamb.getObjectClassHandle(ConfigConstants.CASH_REGISTER_OBJ_NAME);
        this.cashRegisterHlaHandle = rtiamb.registerObjectInstance(classHandle);
    }

    private void updateHLAObject(double time) throws RTIexception {
        SuppliedAttributes attributes =
                RtiFactoryFactory.getRtiFactory().createSuppliedAttributes();

        int classHandle = rtiamb.getObjectClass(cashRegisterHlaHandle);
        int cashRegisterNumberHandle = rtiamb.getAttributeHandle( ConfigConstants.CASH_REGISTER_NUMBER_NAME, classHandle );
        int isFreeHandle = rtiamb.getAttributeHandle( ConfigConstants.CASH_REGISTER_IS_FREE_NAME, classHandle );
        byte[] cashRegisterNumber = EncodingHelpers.encodeInt(cashRegisterNr);
        byte[] isFreeFlag = EncodingHelpers.encodeBoolean(isFree);

        attributes.add(cashRegisterNumberHandle, cashRegisterNumber);
        attributes.add(isFreeHandle, isFreeFlag);

        LogicalTime logicalTime = convertTime( time );
        rtiamb.updateAttributeValues( cashRegisterHlaHandle, attributes, "actualize cash register".getBytes(), logicalTime );
    }

    private void publishAndSubscribe() throws RTIexception {
        // Register publish Object Cash Register
        int cashRegisterHandle = rtiamb.getObjectClassHandle( ConfigConstants.CASH_REGISTER_OBJ_NAME );
        int cashRegisterNumberHandle    = rtiamb.getAttributeHandle( ConfigConstants.CASH_REGISTER_NUMBER_NAME, cashRegisterHandle );
        int isFreeHandle    = rtiamb.getAttributeHandle( ConfigConstants.CASH_REGISTER_IS_FREE_NAME, cashRegisterHandle );

        AttributeHandleSet attributes =
                RtiFactoryFactory.getRtiFactory().createAttributeHandleSet();
        attributes.add( cashRegisterNumberHandle );
        attributes.add( isFreeHandle );

        rtiamb.publishObjectClass(cashRegisterHandle, attributes);

        // Register subscribe to Interaction joinClientToQueue
        int startHandlingClientHandle = rtiamb.getInteractionClassHandle( ConfigConstants.START_HANDLING_CLIENT_INTERACTION_NAME);
        rtiamb.subscribeInteractionClass( startHandlingClientHandle );

        // Register subscribe to Interaction openNewCashRegisterHandle
        int openNewCashRegisterHandle = rtiamb.getInteractionClassHandle( ConfigConstants.OPEN_NEW_CASH_REGISTER_INTERACTION_NAME);
        rtiamb.subscribeInteractionClass( openNewCashRegisterHandle );
    }



    public static void main(String[] args) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        try {
            new CashRegisterFederate().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }
}
