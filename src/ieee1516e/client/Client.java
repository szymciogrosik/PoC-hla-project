package ieee1516e.client;

public class Client {
    private long number;
    private long amountOfArticles;

    public Client(long number, long amountOfArticles) {
        this.number = number;
        this.amountOfArticles = amountOfArticles;
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
}
