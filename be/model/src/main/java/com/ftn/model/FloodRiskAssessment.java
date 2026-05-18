package com.ftn.model;

import java.io.Serializable;

public class FloodRiskAssessment implements Serializable {

    private static final long serialVersionUID = 1L;

    private Location location;
    private RiskLevel riskLevel;
    private String reason;

    public FloodRiskAssessment() {
    }

    public FloodRiskAssessment(Location location, RiskLevel riskLevel, String reason) {
        this.location = location;
        this.riskLevel = riskLevel;
        this.reason = reason;
    }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    @Override
    public String toString() {
        return "FloodRiskAssessment[" + (location != null ? location.getCode() : "?")
                + " = " + riskLevel + " - " + reason + "]";
    }
}
