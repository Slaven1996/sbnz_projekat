package com.ftn.service.dto.simulation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TimelineEventDTO {

    private LocalDateTime stepTime;
    private int firedRules;

    private List<String> appliedReadings = new ArrayList<>();

    private List<String> changes = new ArrayList<>();

    private List<LocationStateDTO> locationStates = new ArrayList<>();

    private String systemAlertLevel;
    private String systemAlertDescription;

    public TimelineEventDTO() {
    }

    public LocalDateTime getStepTime() { return stepTime; }
    public void setStepTime(LocalDateTime stepTime) { this.stepTime = stepTime; }

    public int getFiredRules() { return firedRules; }
    public void setFiredRules(int firedRules) { this.firedRules = firedRules; }

    public List<String> getAppliedReadings() { return appliedReadings; }
    public void setAppliedReadings(List<String> appliedReadings) { this.appliedReadings = appliedReadings; }

    public List<String> getChanges() { return changes; }
    public void setChanges(List<String> changes) { this.changes = changes; }

    public List<LocationStateDTO> getLocationStates() { return locationStates; }
    public void setLocationStates(List<LocationStateDTO> locationStates) { this.locationStates = locationStates; }

    public String getSystemAlertLevel() { return systemAlertLevel; }
    public void setSystemAlertLevel(String systemAlertLevel) { this.systemAlertLevel = systemAlertLevel; }

    public String getSystemAlertDescription() { return systemAlertDescription; }
    public void setSystemAlertDescription(String systemAlertDescription) {
        this.systemAlertDescription = systemAlertDescription;
    }
}
