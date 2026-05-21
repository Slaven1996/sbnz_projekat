package com.ftn.model.events;

import java.io.Serializable;
import java.util.Date;

import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

@Role(Role.Type.EVENT)
@Timestamp("timestamp")
@Expires("2h")
public class RapidWaterLevelRise implements Serializable {

    private static final long serialVersionUID = 1L;

    private String locationCode;
    private double riseCm;
    private int periodMinutes;
    private Date timestamp;

    public RapidWaterLevelRise() {
    }

    public RapidWaterLevelRise(String locationCode, double riseCm, int periodMinutes, Date timestamp) {
        this.locationCode = locationCode;
        this.riseCm = riseCm;
        this.periodMinutes = periodMinutes;
        this.timestamp = timestamp;
    }

    public RapidWaterLevelRise(String locationCode, double riseCm, int periodMinutes) {
        this(locationCode, riseCm, periodMinutes, new Date());
    }

    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

    public double getRiseCm() { return riseCm; }
    public void setRiseCm(double riseCm) { this.riseCm = riseCm; }

    public int getPeriodMinutes() { return periodMinutes; }
    public void setPeriodMinutes(int periodMinutes) { this.periodMinutes = periodMinutes; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "RapidWaterLevelRise[" + locationCode + " +" + riseCm + "cm in "
                + periodMinutes + "min @ " + timestamp + "]";
    }
}
