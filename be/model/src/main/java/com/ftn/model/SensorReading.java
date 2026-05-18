package com.ftn.model;

import java.io.Serializable;
import java.util.Date;

public class SensorReading implements Serializable {

    private static final long serialVersionUID = 1L;

    private Location location;
    private SensorType sensorType;
    private String sensorName;
    private double value;
    private Date timestamp;

    public SensorReading() {
    }

    public SensorReading(Location location, SensorType sensorType, String sensorName,
                         double value, Date timestamp) {
        this.location = location;
        this.sensorType = sensorType;
        this.sensorName = sensorName;
        this.value = value;
        this.timestamp = timestamp;
    }

    public SensorReading(Location location, SensorType sensorType, double value) {
        this(location, sensorType, null, value, new Date());
    }

    public SensorReading(Location location, SensorType sensorType, String sensorName, double value) {
        this(location, sensorType, sensorName, value, new Date());
    }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    public SensorType getSensorType() { return sensorType; }
    public void setSensorType(SensorType sensorType) { this.sensorType = sensorType; }

    public String getSensorName() { return sensorName; }
    public void setSensorName(String sensorName) { this.sensorName = sensorName; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "SensorReading[" + (location != null ? location.getCode() : "?")
                + ", " + sensorType + (sensorName != null ? "/" + sensorName : "")
                + " = " + value + " @ " + timestamp + "]";
    }
}
