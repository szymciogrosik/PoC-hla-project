package hla.manager;

import hla.constants.ConfigConstants;
import hla.rti.AttributeHandleSet;
import hla.rti.LogicalTime;
import hla.rti.RTIexception;
import hla.rti.SuppliedParameters;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import hla.tamplate.BaseFederate;

public class ManagerFederate extends BaseFederate<ManagerAmbassador> {
    private final double timeStep           = 3.0;

    private void runFederate() throws RTIexception, IllegalAccessException, InstantiationException, ClassNotFoundException {
        this.setFederateName(ConfigConstants.MANAGER_FED);

        // Create ambassador, tryCreateFederation, and waiting for first sync
        init(ManagerAmbassador.class.getCanonicalName());

        publishAndSubscribe();

        while (fedamb.running) {
            double timeToAdvance = fedamb.federateTime + timeStep;
            advanceTime(timeStep);

            sendInteraction(timeToAdvance + fedamb.federateLookahead);

            if(fedamb.grantedTime == timeToAdvance) {
                timeToAdvance += fedamb.federateLookahead;
                log("Updating manager time: " + timeToAdvance);
                fedamb.federateTime = timeToAdvance;
//            waitForUser();
            }

            rtiamb.tick();
        }
    }

    private void sendInteraction(double timeStep) throws RTIexception {
        // Send Interaction openNewCashRegister
        SuppliedParameters parameters =
                RtiFactoryFactory.getRtiFactory().createSuppliedParameters();
//        Random random = new Random();
//        int quantityInt = random.nextInt(10) + 1;
        byte[] cashRegisterNumber = EncodingHelpers.encodeInt(98);
        byte[] queueNumber = EncodingHelpers.encodeInt(99);

        int interactionHandle = rtiamb.getInteractionClassHandle(ConfigConstants.OPEN_NEW_CASH_REGISTER_NAME);
        int cashRegisterNumberHandle = rtiamb.getParameterHandle( ConfigConstants.CASH_REGISTER_NUMBER_NAME, interactionHandle );
        int queueNumberHandle = rtiamb.getParameterHandle( ConfigConstants.QUEUE_NUMBER_NAME, interactionHandle );

        parameters.add(cashRegisterNumberHandle, cashRegisterNumber);
        parameters.add(queueNumberHandle, queueNumber);

        LogicalTime time = convertTime( timeStep );
        log("Sending "+ ConfigConstants.OPEN_NEW_CASH_REGISTER_NAME +": 98, 99");
        rtiamb.sendInteraction( interactionHandle, parameters, "tag".getBytes(), time );
    }

    protected void publishAndSubscribe() throws RTIexception {
        // Publish interaction openNewCashRegister
        int openNewCashRegisterHandle = rtiamb.getInteractionClassHandle( ConfigConstants.OPEN_NEW_CASH_REGISTER_NAME );
        rtiamb.publishInteractionClass(openNewCashRegisterHandle);

        // Register listening on queue objects
        int queueHandle = rtiamb.getObjectClassHandle("ObjectRoot." + ConfigConstants.QUEUE_OBJ_NAME);
        int queueNumberHandle = rtiamb.getAttributeHandle(ConfigConstants.QUEUE_NUMBER_NAME, queueHandle);
        int cashRegisterNumberHandle = rtiamb.getAttributeHandle(ConfigConstants.CASH_REGISTER_NUMBER_NAME, queueHandle);
        int queueLengthHandle = rtiamb.getAttributeHandle(ConfigConstants.QUEUE_LENGTH_NAME, queueHandle);

        AttributeHandleSet attributes = RtiFactoryFactory.getRtiFactory().createAttributeHandleSet();
        attributes.add(queueNumberHandle);
        attributes.add(cashRegisterNumberHandle);
        attributes.add(queueLengthHandle);

        rtiamb.subscribeObjectClassAttributes(queueHandle, attributes);

        // Register listening on cash register objects
        int cashRegisterHandle = rtiamb.getObjectClassHandle( "ObjectRoot." + ConfigConstants.CASH_REGISTER_OBJ_NAME );
        int cashRegisterNumber2Handle    = rtiamb.getAttributeHandle( ConfigConstants.CASH_REGISTER_NUMBER_NAME, cashRegisterHandle );
        int isFreeHandle    = rtiamb.getAttributeHandle( ConfigConstants.CASH_REGISTER_IS_FREE_NAME, cashRegisterHandle );

        AttributeHandleSet attributes2 = RtiFactoryFactory.getRtiFactory().createAttributeHandleSet();
        attributes2.add(cashRegisterNumber2Handle);
        attributes2.add(isFreeHandle);

        rtiamb.subscribeObjectClassAttributes(cashRegisterHandle, attributes2);
    }

    public static void main(String[] args) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        try {
            new ManagerFederate().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }
}
