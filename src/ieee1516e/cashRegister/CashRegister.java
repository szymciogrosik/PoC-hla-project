package ieee1516e.cashRegister;

import hla.rti1516e.ObjectInstanceHandle;

public class CashRegister {
    private ObjectInstanceHandle objectInstanceHandle;
    private long numberCashRegister;
    private boolean isFree;

    private boolean toUpdate = true;

    public CashRegister(long numberCashRegister, boolean isFree) {
        this.numberCashRegister = numberCashRegister;
        this.isFree = isFree;
    }

    public CashRegister(ObjectInstanceHandle objectInstanceHandle, long numberCashRegister, boolean isFree) {
        this.objectInstanceHandle = objectInstanceHandle;
        this.numberCashRegister = numberCashRegister;
        this.isFree = isFree;
    }

    public long getNumberCashRegister() {
        return numberCashRegister;
    }

    public void setNumberCashRegister(long numberCashRegister) {
        this.numberCashRegister = numberCashRegister;
    }

    public boolean isFree() {
        return isFree;
    }

    public void setFree(boolean free) {
        isFree = free;
    }

    public ObjectInstanceHandle getObjectInstanceHandle() {
        return objectInstanceHandle;
    }

    public void setObjectInstanceHandle(ObjectInstanceHandle objectInstanceHandle) {
        this.objectInstanceHandle = objectInstanceHandle;
    }

    public boolean isToUpdate() {
        return toUpdate;
    }

    public void setToUpdate(boolean toUpdate) {
        this.toUpdate = toUpdate;
    }
}
