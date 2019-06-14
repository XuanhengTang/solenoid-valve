package com.cernet.smartcharge_snap;

public class ChargeEntity {
    public double current;
    public double voltage;
    public double capacity;

    public ChargeEntity() {
    }

    public ChargeEntity(double current, double voltage, double capacity) {
        this.current = current;
        this.voltage = voltage;
        this.capacity = capacity;
    }
}
