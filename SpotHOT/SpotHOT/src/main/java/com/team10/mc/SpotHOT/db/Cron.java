package com.team10.mc.SpotHOT.db;


public class Cron {

    public static final String NAME = "CRON";

    public enum STATUS {
        DEFAULT(0), SCHED_OFF_ENABLED(1), SCHED_OFF_DISABLED(2), SCHED_ON_ENABLED(3), SCHED_ON_DISABLED(4);

        final int value;

        STATUS(int v) {
            value = v;
        }

        public int getValue() {
            return value;
        }

    }

    private int id;
    private final int hourOff;
    private final int minOff;
    private final int hourOn;
    private final int minOn;
    private final int mask;
    private int status;

    public Cron(int hourOff, int minOff, int hourOn, int minOn, int mask, int status) {
        this.hourOff = hourOff;
        this.hourOn = hourOn;
        this.minOff = minOff;
        this.minOn = minOn;
        this.mask = mask;
        this.status = status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getHourOff() {
        return hourOff;
    }

    public int getHourOn() {
        return hourOn;
    }

    public int getMinOff() {
        return minOff;
    }

    public int getMinOn() {
        return minOn;
    }

    public int getMask() {
        return mask;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void toggle() {
        if (getStatus() == STATUS.SCHED_OFF_ENABLED.getValue()) {
            setStatus(STATUS.SCHED_OFF_DISABLED.getValue());
        } else if (getStatus() == STATUS.SCHED_OFF_DISABLED.getValue()) {
            setStatus(STATUS.SCHED_OFF_ENABLED.getValue());
        }
    }
}
