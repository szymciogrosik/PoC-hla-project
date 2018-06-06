package ieee1516e.statistic.statisticObjects;

public class StatisticCashRegister {
    private long cashRegisterNumber;
    private double handlingClientsCounter;

    public StatisticCashRegister(long cashRegisterNumber) {
        this.cashRegisterNumber = cashRegisterNumber;
    }

    public long getCashRegisterNumber() {
        return cashRegisterNumber;
    }

    public double getHandlingClientsCounter() {
        return handlingClientsCounter;
    }

    public void incrementHandlingClientsCounter() {
        this.handlingClientsCounter++;
    }
}
