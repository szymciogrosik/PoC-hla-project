package ieee1516e.cashRegister;

public class CashRegister {
    private int numberCashRegister;
    private boolean isFree;

    public CashRegister(int numberCashRegister, boolean isFree) {
        this.numberCashRegister = numberCashRegister;
        this.isFree = isFree;
    }

    public int getNumberCashRegister() {
        return numberCashRegister;
    }

    public void setNumberCashRegister(int numberCashRegister) {
        this.numberCashRegister = numberCashRegister;
    }

    public boolean isFree() {
        return isFree;
    }

    public void setFree(boolean free) {
        isFree = free;
    }
}
