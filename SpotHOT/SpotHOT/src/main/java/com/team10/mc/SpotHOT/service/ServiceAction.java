package com.team10.mc.SpotHOT.service;


public enum ServiceAction {

    TETHER_ON,
    TETHER_OFF,
    INTERNET_ON,
    INTERNET_OFF,
    SCHEDULED_TETHER_ON,
    SCHEDULED_TETHER_OFF,
    SCHEDULED_INTERNET_ON,
    SCHEDULED_INTERNET_OFF,
    TETHER_IDLE_OFF,
    INTERNET_IDLE_OFF,
    DATA_USAGE_EXCEED_LIMIT(true, true, false),
    BLUETOOTH_INTERNET_TETHER_ON,
    BLUETOOTH_INTERNET_TETHER_OFF,
    CELL_INTERNET_TETHER_ON,
    CELL_INTERNET_TETHER_OFF,
    TEMP_TETHER_OFF,
    TEMP_TETHER_ON;

    private final boolean on;
    private final boolean tethering;
    private final boolean internet;

    ServiceAction() {
        this.on = name().endsWith("_ON");
        this.internet = name().contains("INTERNET");
        this.tethering = name().contains("TETHER");
    }

    ServiceAction(boolean tethering, boolean internet, boolean on) {
        this.tethering = tethering;
        this.internet = internet;
        this.on = on;
    }

    public boolean isOn() {
        return on;
    }

    public boolean isTethering() {
        return tethering;
    }

    public boolean isInternet() {
        return internet;
    }

    @Override
    public String toString() {
        return name() + ", isTethering=" + isTethering() + ", isInternet=" + isInternet() + ", state=" + isOn();
    }
}
