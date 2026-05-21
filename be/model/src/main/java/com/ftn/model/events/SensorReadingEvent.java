package com.ftn.model.events;

import java.io.Serializable;
import java.util.Date;

import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

import com.ftn.model.enums.SensorType;

@Role(Role.Type.EVENT)
@Timestamp("timestamp")
@Expires("2h")
public class SensorReadingEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String locationCode;
    private SensorType sensorType;
    private String sensorName;
    private double value;
    private Date timestamp;

    public SensorReadingEvent() {
    }

    public SensorReadingEvent(String locationCode, SensorType sensorType, String sensorName,
                              double value, Date timestamp) {
        this.locationCode = locationCode;
        this.sensorType = sensorType;
        this.sensorName = sensorName;
        this.value = value;
        this.timestamp = timestamp;
    }

    public SensorReadingEvent(String locationCode, SensorType sensorType, double value) {
        this(locationCode, sensorType, null, value, new Date());
    }

    public SensorReadingEvent(String locationCode, SensorType sensorType, String sensorName, double value) {
        this(locationCode, sensorType, sensorName, value, new Date());
    }

    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

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
        return "SensorReadingEvent[" + locationCode
                + ", " + sensorType + (sensorName != null ? "/" + sensorName : "")
                + " = " + value + " @ " + timestamp + "]";
    }
}
