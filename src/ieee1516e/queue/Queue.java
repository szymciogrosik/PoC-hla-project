package ieee1516e.queue;

public class Queue {
    private int numberQueue;
    private int numberCashRegister;
    private int length;

    public Queue(int numberQueue, int numberCashRegister, int length) {
        this.numberQueue = numberQueue;
        this.numberCashRegister = numberCashRegister;
        this.length = length;
    }

    public int getNumberQueue() {
        return numberQueue;
    }

    public void setNumberQueue(int numberQueue) {
        this.numberQueue = numberQueue;
    }

    public int getNumberCashRegister() {
        return numberCashRegister;
    }

    public void setNumberCashRegister(int numberCashRegister) {
        this.numberCashRegister = numberCashRegister;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
