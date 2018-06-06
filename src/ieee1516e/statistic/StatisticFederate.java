package ieee1516e.statistic;

import hla.rti1516e.*;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.time.HLAfloat64Time;
import ieee1516e.constants.ConfigConstants;
import ieee1516e.statistic.statisticObjects.StatisticCashRegister;
import ieee1516e.statistic.statisticObjects.StatisticClient;
import ieee1516e.statistic.statisticObjects.StatisticQueue;
import ieee1516e.tamplate.BaseFederate;
import ptolemy.plot.Plot;
import ptolemy.plot.PlotApplication;

import java.util.ArrayList;

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

    private ArrayList<StatisticQueue> queueListAvgTimeWaitingInQueueTemp = new ArrayList<>();
    private ArrayList<AvgTimeWaitingInQueue> queueListAvgTimeWaitingInQueueFinally = new ArrayList<>();
    private ArrayList<StatisticCashRegister> cashRegisterListMaxHandlingClient = new ArrayList<>();

    private void runFederate() throws RTIexception, IllegalAccessException, InstantiationException, ClassNotFoundException {
        this.setFederateName(ConfigConstants.STATISTIC_FED);

        // Create ambassador, tryCreateFederation, and waiting for first sync
        init(StatisticAmbassador.class.getCanonicalName());

        publishAndSubscribe();

        while (fedamb.running) {
            double timeToAdvance = fedamb.federateTime + timeStep;
            advanceTime(timeStep);

            if(fedamb.externalObjects.size() > 0) {
                fedamb.externalObjects.sort(new StatisticExternalObject.ExternalObjectComparator());
                for(StatisticExternalObject externalObject : fedamb.externalObjects) {
                    switch (externalObject.getObjectType()) {
                        case QUEUE:
                            long queueNumberDecoded = decodeIntValue(externalObject.getAttributes().get(this.queueNumberQueue));
                            long cashRegisterDecoded = decodeIntValue(externalObject.getAttributes().get(this.cashRegisterQueue));
                            long queueLengthDecoded = decodeIntValue(externalObject.getAttributes().get(this.queueLengthQueue));

                            log("In case object: QUEUE | Nr kolejki: " +
                                    queueNumberDecoded +
                                    ", Nr kasy: " +
                                    cashRegisterDecoded +
                                    ", Dlugosc kolejki: " +
                                    queueLengthDecoded
                            );

                            boolean notExist = true;

                            //------------------------------------------------------------------------------------------
                            //Statistic avg time waiting client in queue.
                            // &
                            //Avg length queue for cashRegister
                            //If queue in this number doesn't exist add new
                            for (StatisticQueue q : queueListAvgTimeWaitingInQueueTemp) {
                                if(q.getQueueNumber() == queueNumberDecoded) {
                                    q.setActualLength(queueLengthDecoded);
                                    notExist = false;
                                    break;
                                }
                            }
                            if(notExist) {
                                StatisticQueue sQ = new StatisticQueue(queueNumberDecoded);
                                sQ.setActualLength(queueLengthDecoded);
                                queueListAvgTimeWaitingInQueueTemp.add(sQ);
                            }
                            //------------------------------------------------------------------------------------------

                            break;
                        case CASH_REGISTER:
                            long cashRegisterNumberDecoded = decodeIntValue(externalObject.getAttributes().get(this.cashRegisterNumberHandleCashRegister));
                            boolean isFreeDecoded = decodeBooleanValue(externalObject.getAttributes().get(this.isFreeHandleCashRegister));

                            log("In case object: CASH_REGISTER | Nr kasy: " +
                                    cashRegisterNumberDecoded+
                                    ", Czy wolna: " +
                                    isFreeDecoded
                            );

                            //------------------------------------------------------------------------------------------
                            //Handling clients from cashRegister
                            boolean notExist2 = true;
                            if(externalObject.getTime()>=ConfigConstants.STATISTIC_MAX_CLIENTS_HANDLING_NUMBER_START_TIME_VALUE
                                    && externalObject.getTime()<=ConfigConstants.STATISTIC_MAX_CLIENTS_HANDLING_NUMBER_END_TIME_VALUE) {
                                for (StatisticCashRegister cR : cashRegisterListMaxHandlingClient) {
                                    if (cR.getCashRegisterNumber() == cashRegisterNumberDecoded) {
                                        if (isFreeDecoded)
                                            cR.incrementHandlingClientsCounter();
                                        notExist2 = false;
                                        break;
                                    }
                                }
                                if (notExist2)
                                    cashRegisterListMaxHandlingClient.add(new StatisticCashRegister(cashRegisterNumberDecoded));
                                //------------------------------------------------------------------------------------------
                            }
                            break;
                        default:
                            log("Undetected object.");
                            break;
                    }
                }
                fedamb.externalObjects.clear();
            }

            if(fedamb.externalEvents.size() > 0) {
                fedamb.externalEvents.sort(new StatisticExternalEvent.ExternalEventComparator());
                for(StatisticExternalEvent externalEvent : fedamb.externalEvents) {
                    switch (externalEvent.getEventType()) {
                        case JOIN_CLIENT_TO_QUEUE:
                            long clientNumberDecoded = decodeIntValue(externalEvent.getAttributes().get(this.clientNumberHandleJoinClientToQueue));
                            long queueNumberDecoded = decodeIntValue(externalEvent.getAttributes().get(this.queueNumberHandleJoinClientToQueue));
                            long amountOfArticles = decodeIntValue(externalEvent.getAttributes().get(this.amountOfArticlesHandleJoinClientToQueue));

                            log("In case interaction: JOIN_CLIENT_TO_QUEUE | Nr klienta: " +
                                    clientNumberDecoded +
                                    ", Nr kolejki: " +
                                    queueNumberDecoded +
                                    ", Liczba artykulow: " +
                                    amountOfArticles
                            );

                            //------------------------------------------------------------------------------------------
                            //Statistic avg time waiting client in queue.
                            //Check if client is in queue, if not add new client.
                            for (StatisticQueue q : queueListAvgTimeWaitingInQueueTemp) {
                                if(q.getQueueNumber() == queueNumberDecoded) {
                                    boolean clientNotExist = true;
                                    for (StatisticClient sC : q.getStatisticClientsList()) {
                                        if(sC.getClientNumber() == clientNumberDecoded) {
                                            sC.setJoinToQueue(externalEvent.getTime());
                                            clientNotExist = false;
                                            break;
                                        }
                                    }
                                    if(clientNotExist)
                                        q.getStatisticClientsList().add(new StatisticClient(clientNumberDecoded, externalEvent.getTime(), externalEvent.getTime()));
                                    break;
                                }
                            }
                            //------------------------------------------------------------------------------------------

                            break;

                        case START_HANDLING_CLIENT:
                            long queueNumberDecoded2 = decodeIntValue(externalEvent.getAttributes().get(this.queueNumberHandleStartHandlingClient));
                            long cashRegisterNumber2 = decodeIntValue(externalEvent.getAttributes().get(this.cashRegisterNumberHandleStartHandlingClient));
                            long clientNumberDecoded2 = decodeIntValue(externalEvent.getAttributes().get(this.clientNumberHandleStartHandlingClient));
                            long amountOfArticles2 = decodeIntValue(externalEvent.getAttributes().get(this.amountOfArticlesHandleStartHandlingClient));

                            log("In case interaction: START_HANDLING_CLIENT | Nr kolejki: " +
                                    queueNumberDecoded2 +
                                    ", Nr kasy: " +
                                    cashRegisterNumber2 +
                                    ", Nr klienta: " +
                                    clientNumberDecoded2+
                                    ", Liczba zakupow: " +
                                    amountOfArticles2
                            );

                            //Check if client is in queue and update time exit queue.
                            for (StatisticQueue q : queueListAvgTimeWaitingInQueueTemp) {
                                if(q.getQueueNumber() == queueNumberDecoded2) {
                                    for (StatisticClient sC : q.getStatisticClientsList()) {
                                        if(sC.getClientNumber() == clientNumberDecoded2) {
                                            sC.setExitQueue(externalEvent.getTime());
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }

                            break;

                        default:
                            log("Undetected interaction.");
                            break;
                    }
                }
                fedamb.externalEvents.clear();
            }

            //Avg length queue for cashRegister
            //Length queue in time
            for (StatisticQueue sQ : queueListAvgTimeWaitingInQueueTemp) {
                sQ.setCounter(sQ.getCounter()+1);
                sQ.setLengthSum(sQ.getLengthSum() + sQ.getActualLength());
                sQ.addToLengthInTime(fedamb.federateTime, sQ.getActualLength());
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

        //------------------------------------------------------------------------------------------
        //Statistic avg time waiting client in queue.
        for (StatisticQueue q : queueListAvgTimeWaitingInQueueTemp) {
            double avgWaitingInQueue;
            double waitingInQueueTime = 0;
            double clientsNumber = 0;
            System.out.println("Dla kolejki: "+q.getQueueNumber());
            for (StatisticClient c : q.getStatisticClientsList()) {
                if(c.getExitQueue() == c.getJoinToQueue())
                    c.setExitQueue(fedamb.federateTime);
                waitingInQueueTime = waitingInQueueTime + (c.getExitQueue() - c.getJoinToQueue());
                clientsNumber++;
                System.out.println("Nr klienta: " + c.getClientNumber() + ", Join: " + c.getJoinToQueue() + ", Exit: " + c.getExitQueue());
            }
            avgWaitingInQueue = waitingInQueueTime/clientsNumber;
            System.out.println("Srednia długość oczekiwania dla kolejki: " + avgWaitingInQueue);
            queueListAvgTimeWaitingInQueueFinally.add(new AvgTimeWaitingInQueue(q.getQueueNumber(), avgWaitingInQueue));
        }
        //------------------------------------------------------------------------------------------

        //------------------------------------------------------------------------------------------
        //Avg length queue for cashRegister
        for (StatisticQueue sQ : queueListAvgTimeWaitingInQueueTemp) {
            System.out.println("Dla kasy: "+sQ.getQueueNumber());
            System.out.println(sQ.getLengthSum() + " , " + sQ.getCounter());
            System.out.println(sQ.getLengthSum()/sQ.getCounter());
        }
        //------------------------------------------------------------------------------------------

        //------------------------------------------------------------------------------------------
        //Handling clients from cashRegister
        for (StatisticCashRegister cR : cashRegisterListMaxHandlingClient) {
            System.out.println("Dla kasy: " + cR.getCashRegisterNumber() + ", obsłużono: "+cR.getHandlingClientsCounter()+", w przedziale czasu od: "+ConfigConstants.STATISTIC_MAX_CLIENTS_HANDLING_NUMBER_START_TIME_VALUE+" do "+ConfigConstants.STATISTIC_MAX_CLIENTS_HANDLING_NUMBER_END_TIME_VALUE);
        }
        //------------------------------------------------------------------------------------------

        //------------------------------------------------------------------------------------------
        //Queue length in time
        //Plot
        printPlotsQueueLengthFromTime();
        printOnePlotQueuesLengthFromTime();
        //------------------------------------------------------------------------------------------

        //------------------------------------------------------------------------------------------
        //Avg time waiting in queue
        //Plot
        printPlotAvgTimeWaitingInQueue();
        //------------------------------------------------------------------------------------------
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

    private void printPlotsQueueLengthFromTime() {
        for (StatisticQueue q : queueListAvgTimeWaitingInQueueTemp) {
            Plot myPlot = new Plot();
            myPlot.setTitle("Wykres długości kolejki od czasu dla kolejki " + q.getQueueNumber());
            myPlot.setXLabel("Czas");
            myPlot.setYLabel("Dlugość");
            myPlot.setMarksStyle("bigdots", 0);
            myPlot.setMarksStyle("dots", 1);

            for (StatisticQueue.LengthInTime lT : q.getLengthInTimeList()) {
                myPlot.addPoint(0, lT.getTime(), lT.getLength(), true);
            }

            PlotApplication app = new PlotApplication(myPlot);
            app.setSize(600, 600);
            app.setLocation(100, 100);
            app.setTitle("Wykres długości kolejki od czasu dla kolejki " + q.getQueueNumber());
        }
    }

    private void printOnePlotQueuesLengthFromTime() {
            Plot myPlot = new Plot();
            myPlot.setTitle("Wykres długości kolejki od czasu dla wszystkich kolejek");
            myPlot.setXLabel("Czas");
            myPlot.setYLabel("Dlugość");
            myPlot.setMarksStyle("bigdots", 0);
            myPlot.setMarksStyle("dots", 1);
            int dataSetColor = 0;
            for (StatisticQueue q : queueListAvgTimeWaitingInQueueTemp) {
                for (StatisticQueue.LengthInTime lT : q.getLengthInTimeList()) {
                    myPlot.addPoint(dataSetColor, lT.getTime(), lT.getLength(), true);
                }
                dataSetColor++;
            }
            PlotApplication app = new PlotApplication(myPlot);
            app.setSize(600, 600);
            app.setLocation(100, 100);
            app.setTitle("Wykres długości kolejki od czasu dla wszystkich kolejek");
    }

    private void printPlotAvgTimeWaitingInQueue() {
        Plot myPlot = new Plot();
        myPlot.setTitle("Sredni czas oczekiwania w kolejce");
        myPlot.setXLabel("Nr kolejki");
        myPlot.setYLabel("Sredni czas");
        myPlot.setMarksStyle("bigdots", 0);
        myPlot.setMarksStyle("dots", 1);
        int dataSetColor = 0;
        for (AvgTimeWaitingInQueue aT : queueListAvgTimeWaitingInQueueFinally) {
            myPlot.addPoint(dataSetColor, aT.queueNumber, aT.avgTime, true);
            dataSetColor++;
        }
        PlotApplication app = new PlotApplication(myPlot);
        app.setSize(600, 600);
        app.setLocation(100, 100);
        app.setTitle("Wykres sredniego czasu oczekiwania w kolejce");
    }

    private class AvgTimeWaitingInQueue {
        private long queueNumber = 0;
        private double avgTime = 0;

        public AvgTimeWaitingInQueue(long queueNumber, double avgTime) {
            this.queueNumber = queueNumber;
            this.avgTime = avgTime;
        }

        public long getQueueNumber() {
            return queueNumber;
        }

        public double getAvgTime() {
            return avgTime;
        }
    }
}
