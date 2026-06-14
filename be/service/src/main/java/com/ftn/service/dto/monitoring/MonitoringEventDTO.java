package com.ftn.service.dto.monitoring;

public class MonitoringEventDTO {

    private String time;
    private String severity;
    private String locationCode;
    private String message;

    public MonitoringEventDTO() {
    }

    public MonitoringEventDTO(String time, String severity, String locationCode, String message) {
        this.time = time;
        this.severity = severity;
        this.locationCode = locationCode;
        this.message = message;
    }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
