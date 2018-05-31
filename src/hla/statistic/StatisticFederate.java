package hla.statistic;

import hla.constants.ConfigConstants;
import hla.rti.*;
import hla.rti.jlc.RtiFactoryFactory;
import hla.tamplate.BaseFederate;

public class StatisticFederate extends BaseFederate<StatisticAmbassador> {
    private final double timeStep           = 1.0;

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
                            log("JOIN_CLIENT_TO_QUEUE");
                            break;
                        case START_HANDLING_CLIENT:
                            log("START_HANDLING_CLIENT");
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
                            log("QUEUE");
                            break;
                        case CASH_REGISTER:
                            log("CASH_REGISTER");
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

            rtiamb.tick();
        }
    }

    private void publishAndSubscribe() throws RTIexception {
        // Register subscribe to Interaction joinClientToQueue
        int joinClientToQueueHandle = rtiamb.getInteractionClassHandle( ConfigConstants.JOIN_CLIENT_TO_QUEUE_INTERACTION_NAME );
        rtiamb.subscribeInteractionClass( joinClientToQueueHandle );

        // Register subscribe to Interaction startHandlingClient
        int startHandlingClientHandle = rtiamb.getInteractionClassHandle( ConfigConstants.START_HANDLING_CLIENT_INTERACTION_NAME);
        rtiamb.subscribeInteractionClass( startHandlingClientHandle );

        // Register listening on queue objects
        int queueHandle = rtiamb.getObjectClassHandle(ConfigConstants.QUEUE_OBJ_NAME);
        int queueNumberHandle = rtiamb.getAttributeHandle(ConfigConstants.QUEUE_NUMBER_NAME, queueHandle);
        int cashRegisterNumberHandle = rtiamb.getAttributeHandle(ConfigConstants.CASH_REGISTER_NUMBER_NAME, queueHandle);
        int queueLengthHandle = rtiamb.getAttributeHandle(ConfigConstants.QUEUE_LENGTH_NAME, queueHandle);

        AttributeHandleSet attributes = RtiFactoryFactory.getRtiFactory().createAttributeHandleSet();
        attributes.add(queueNumberHandle);
        attributes.add(cashRegisterNumberHandle);
        attributes.add(queueLengthHandle);

        rtiamb.subscribeObjectClassAttributes(queueHandle, attributes);

        // Register listening on cash register objects
        int cashRegisterHandle = rtiamb.getObjectClassHandle( ConfigConstants.CASH_REGISTER_OBJ_NAME );
        int cashRegisterNumber2Handle    = rtiamb.getAttributeHandle( ConfigConstants.CASH_REGISTER_NUMBER_NAME, cashRegisterHandle );
        int isFreeHandle    = rtiamb.getAttributeHandle( ConfigConstants.CASH_REGISTER_IS_FREE_NAME, cashRegisterHandle );

        AttributeHandleSet attributes2 = RtiFactoryFactory.getRtiFactory().createAttributeHandleSet();
        attributes2.add(cashRegisterNumber2Handle);
        attributes2.add(isFreeHandle);

        rtiamb.subscribeObjectClassAttributes(cashRegisterHandle, attributes2);
    }



    public static void main(String[] args) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        try {
            new StatisticFederate().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }
}
