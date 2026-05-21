package com.ftn.model.events;

import java.io.Serializable;
import java.util.Date;

import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

@Role(Role.Type.EVENT)
@Timestamp("timestamp")
@Expires("10m")
public class HeartbeatEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String locationCode;
    private Date timestamp;

    public HeartbeatEvent() {
    }

    public HeartbeatEvent(String locationCode, Date timestamp) {
        this.locationCode = locationCode;
        this.timestamp = timestamp;
    }

    public HeartbeatEvent(String locationCode) {
        this(locationCode, new Date());
    }

    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "HeartbeatEvent[" + locationCode + " @ " + timestamp + "]";
    }
}
