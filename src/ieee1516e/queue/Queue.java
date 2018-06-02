package ieee1516e.queue;

import hla.rti1516e.ObjectInstanceHandle;

public class Queue {
    private ObjectInstanceHandle objectInstanceHandle;
    private long numberQueue;
    private long numberCashRegister;
    private long length;

    public Queue(long numberQueue, long numberCashRegister, long length) {
        this.numberQueue = numberQueue;
        this.numberCashRegister = numberCashRegister;
        this.length = length;
    }

    public Queue(ObjectInstanceHandle objectInstanceHandle, long numberQueue, long numberCashRegister, long length) {
        this.objectInstanceHandle = objectInstanceHandle;
        this.numberQueue = numberQueue;
        this.numberCashRegister = numberCashRegister;
        this.length = length;
    }

    public long getNumberQueue() {
        return numberQueue;
    }

    public void setNumberQueue(long numberQueue) {
        this.numberQueue = numberQueue;
    }

    public long getNumberCashRegister() {
        return numberCashRegister;
    }

    public void setNumberCashRegister(long numberCashRegister) {
        this.numberCashRegister = numberCashRegister;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public ObjectInstanceHandle getObjectInstanceHandle() {
        return objectInstanceHandle;
    }

    public void setObjectInstanceHandle(ObjectInstanceHandle objectInstanceHandle) {
        this.objectInstanceHandle = objectInstanceHandle;
    }
}
