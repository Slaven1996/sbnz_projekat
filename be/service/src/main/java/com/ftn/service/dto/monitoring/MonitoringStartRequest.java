package com.ftn.service.dto.monitoring;

public class MonitoringStartRequest {

    private boolean cepEnabled = true;

    public boolean isCepEnabled() { return cepEnabled; }
    public void setCepEnabled(boolean cepEnabled) { this.cepEnabled = cepEnabled; }
}
