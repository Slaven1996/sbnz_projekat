package com.ftn.service.dto.monitoring;

public class MonitoringStatusDTO {

    private boolean active;
    private boolean cepEnabled;
    private int tick;
    private String pseudoTime;
    private int locationCount;
    private int tickIntervalSeconds;
    private int pseudoStepMinutes;

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isCepEnabled() { return cepEnabled; }
    public void setCepEnabled(boolean cepEnabled) { this.cepEnabled = cepEnabled; }

    public int getTick() { return tick; }
    public void setTick(int tick) { this.tick = tick; }

    public String getPseudoTime() { return pseudoTime; }
    public void setPseudoTime(String pseudoTime) { this.pseudoTime = pseudoTime; }

    public int getLocationCount() { return locationCount; }
    public void setLocationCount(int locationCount) { this.locationCount = locationCount; }

    public int getTickIntervalSeconds() { return tickIntervalSeconds; }
    public void setTickIntervalSeconds(int tickIntervalSeconds) {
        this.tickIntervalSeconds = tickIntervalSeconds;
    }

    public int getPseudoStepMinutes() { return pseudoStepMinutes; }
    public void setPseudoStepMinutes(int pseudoStepMinutes) {
        this.pseudoStepMinutes = pseudoStepMinutes;
    }
}
