package ieee1516e.queue;

import hla.rti1516e.ObjectInstanceHandle;
import ieee1516e.client.Client;

import java.util.LinkedList;

public class Queue {
    private ObjectInstanceHandle objectInstanceHandle;
    private long numberQueue;
    private long numberCashRegister;
    private long length = 0;
    private LinkedList<Client> clientList = new LinkedList<>();

    private boolean toUpdate = true;

    public Queue(long numberQueue, long numberCashRegister, long length) {
        this.numberQueue = numberQueue;
        this.numberCashRegister = numberCashRegister;
        this.length = length;
    }

    public Queue(ObjectInstanceHandle objectInstanceHandle, long numberQueue, long numberCashRegister) {
        this.objectInstanceHandle = objectInstanceHandle;
        this.numberQueue = numberQueue;
        this.numberCashRegister = numberCashRegister;
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

    public ObjectInstanceHandle getObjectInstanceHandle() {
        return objectInstanceHandle;
    }

    public void setObjectInstanceHandle(ObjectInstanceHandle objectInstanceHandle) {
        this.objectInstanceHandle = objectInstanceHandle;
        setToUpdate(true);
    }

    public boolean isToUpdate() {
        return toUpdate;
    }

    private void setToUpdate(boolean toUpdate) {
        this.toUpdate = toUpdate;
    }

    public void addClientToQueue(Client newClient) {
        setToUpdate(true);
        clientList.add(newClient);
        length ++;
    }

    public Client getFirstClient() {
        setToUpdate(true);
        length --;
        return clientList.removeFirst();
    }

    public void setLength(long length) {
        this.length = length;
    }

    public LinkedList<Client> getClientList() {
        return clientList;
    }

    public void setClientList(LinkedList<Client> clientList) {
        this.clientList = clientList;
    }
}
