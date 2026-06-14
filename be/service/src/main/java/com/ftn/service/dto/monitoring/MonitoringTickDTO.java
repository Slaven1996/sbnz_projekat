package com.ftn.service.dto.monitoring;

import java.util.ArrayList;
import java.util.List;

public class MonitoringTickDTO {

    private int tick;
    private String pseudoTime;
    private boolean cepEnabled;
    private int firedRules;

    private String systemAlertLevel;
    private String systemAlertDescription;

    private List<MonitoringLocationDTO> locations = new ArrayList<>();
    private List<MonitoringEventDTO> events = new ArrayList<>();

    public int getTick() { return tick; }
    public void setTick(int tick) { this.tick = tick; }

    public String getPseudoTime() { return pseudoTime; }
    public void setPseudoTime(String pseudoTime) { this.pseudoTime = pseudoTime; }

    public boolean isCepEnabled() { return cepEnabled; }
    public void setCepEnabled(boolean cepEnabled) { this.cepEnabled = cepEnabled; }

    public int getFiredRules() { return firedRules; }
    public void setFiredRules(int firedRules) { this.firedRules = firedRules; }

    public String getSystemAlertLevel() { return systemAlertLevel; }
    public void setSystemAlertLevel(String systemAlertLevel) { this.systemAlertLevel = systemAlertLevel; }

    public String getSystemAlertDescription() { return systemAlertDescription; }
    public void setSystemAlertDescription(String systemAlertDescription) {
        this.systemAlertDescription = systemAlertDescription;
    }

    public List<MonitoringLocationDTO> getLocations() { return locations; }
    public void setLocations(List<MonitoringLocationDTO> locations) { this.locations = locations; }

    public List<MonitoringEventDTO> getEvents() { return events; }
    public void setEvents(List<MonitoringEventDTO> events) { this.events = events; }
}
