package com.ftn.model;

import java.io.Serializable;
import java.util.Date;

public class PumpOperationalStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    private Location location;
    private String pumpId;
    private PumpState state;
    private Date timestamp;

    public PumpOperationalStatus() {
    }

    public PumpOperationalStatus(Location location, String pumpId, PumpState state, Date timestamp) {
        this.location = location;
        this.pumpId = pumpId;
        this.state = state;
        this.timestamp = timestamp;
    }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    public String getPumpId() { return pumpId; }
    public void setPumpId(String pumpId) { this.pumpId = pumpId; }

    public PumpState getState() { return state; }
    public void setState(PumpState state) { this.state = state; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "PumpOperationalStatus[" + (location != null ? location.getCode() : "?")
                + "/" + pumpId + " = " + state + "]";
    }
}
