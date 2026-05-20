package com.ftn.model;

import java.io.Serializable;
import java.util.Date;

import com.ftn.model.enums.FlowLevel;

public class FlowRateStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    private Location location;
    private FlowLevel level;
    private double value;
    private Date timestamp;

    public FlowRateStatus() {
    }

    public FlowRateStatus(Location location, FlowLevel level, double value, Date timestamp) {
        this.location = location;
        this.level = level;
        this.value = value;
        this.timestamp = timestamp;
    }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    public FlowLevel getLevel() { return level; }
    public void setLevel(FlowLevel level) { this.level = level; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "FlowRateStatus[" + (location != null ? location.getCode() : "?")
                + " = " + level + " (" + value + ")]";
    }
}
