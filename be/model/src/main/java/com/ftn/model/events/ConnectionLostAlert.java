package com.ftn.model.events;

import java.io.Serializable;
import java.util.Date;

import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

@Role(Role.Type.EVENT)
@Timestamp("timestamp")
@Expires("2h")
public class ConnectionLostAlert implements Serializable {

    private static final long serialVersionUID = 1L;

    private String locationCode;
    private String description;
    private Date timestamp;

    public ConnectionLostAlert() {
    }

    public ConnectionLostAlert(String locationCode, String description, Date timestamp) {
        this.locationCode = locationCode;
        this.description = description;
        this.timestamp = timestamp;
    }

    public ConnectionLostAlert(String locationCode, String description) {
        this(locationCode, description, new Date());
    }

    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "ConnectionLostAlert[" + locationCode + " - " + description + "]";
    }
}
