package ieee1516e.cashRegister;

public class CashRegister {
    private long numberCashRegister;
    private boolean isFree;

    public CashRegister(long numberCashRegister, boolean isFree) {
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
}
