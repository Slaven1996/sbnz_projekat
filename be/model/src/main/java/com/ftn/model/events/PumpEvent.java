package com.ftn.model.events;

import java.io.Serializable;
import java.util.Date;

import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

import com.ftn.model.enums.PumpEventType;

@Role(Role.Type.EVENT)
@Timestamp("timestamp")
@Expires("2h")
public class PumpEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String locationCode;
    private String pumpId;
    private PumpEventType eventType;
    private Date timestamp;

    public PumpEvent() {
    }

    public PumpEvent(String locationCode, String pumpId, PumpEventType eventType, Date timestamp) {
        this.locationCode = locationCode;
        this.pumpId = pumpId;
        this.eventType = eventType;
        this.timestamp = timestamp;
    }

    public PumpEvent(String locationCode, String pumpId, PumpEventType eventType) {
        this(locationCode, pumpId, eventType, new Date());
    }

    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

    public String getPumpId() { return pumpId; }
    public void setPumpId(String pumpId) { this.pumpId = pumpId; }

    public PumpEventType getEventType() { return eventType; }
    public void setEventType(PumpEventType eventType) { this.eventType = eventType; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "PumpEvent[" + locationCode + "/" + pumpId + " = " + eventType + " @ " + timestamp + "]";
    }
}
