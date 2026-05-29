package com.ftn.model.events;

import java.io.Serializable;
import java.util.Date;

import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Role;

@Role(Role.Type.EVENT)
@Expires("2h")
public class PumpConnectionLostAlert implements Serializable {

    private static final long serialVersionUID = 1L;

    private String locationCode;
    private String pumpId;
    private String description;
    private Date timestamp;

    public PumpConnectionLostAlert() {
    }

    public PumpConnectionLostAlert(String locationCode, String pumpId, String description, Date timestamp) {
        this.locationCode = locationCode;
        this.pumpId = pumpId;
        this.description = description;
        this.timestamp = timestamp;
    }

    public PumpConnectionLostAlert(String locationCode, String pumpId, String description) {
        this(locationCode, pumpId, description, new Date());
    }

    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

    public String getPumpId() { return pumpId; }
    public void setPumpId(String pumpId) { this.pumpId = pumpId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "PumpConnectionLostAlert[" + locationCode + "/" + pumpId + " - " + description + "]";
    }
}
