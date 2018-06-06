package ieee1516e.client;

public class Client {
    private long number;
    private long amountOfArticles;

    //Only for CashRegister
    private double timeToEndHandling;
    private long cashRegisterNumber;

    public Client(long number, long amountOfArticles) {
        this.number = number;
        this.amountOfArticles = amountOfArticles;
    }

    public Client(long number, long amountOfArticles, double timeToEndHandling, long cashRegisterNumber) {
        this.number = number;
        this.amountOfArticles = amountOfArticles;
        this.timeToEndHandling = timeToEndHandling;
        this.cashRegisterNumber = cashRegisterNumber;
    }

    public long getAmountOfArticles() {
        return amountOfArticles;
    }

    public void setAmountOfArticles(long amountOfArticles) {
        this.amountOfArticles = amountOfArticles;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public double getTimeToEndHandling() {
        return timeToEndHandling;
    }

    public void setTimeToEndHandling(double timeToEndHandling) {
        this.timeToEndHandling = timeToEndHandling;
    }

    public long getCashRegisterNumber() {
        return cashRegisterNumber;
    }

    public void setCashRegisterNumber(long cashRegisterNumber) {
        this.cashRegisterNumber = cashRegisterNumber;
    }
}
