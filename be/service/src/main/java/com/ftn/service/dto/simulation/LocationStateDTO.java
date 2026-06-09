package com.ftn.service.dto.simulation;

public class LocationStateDTO {

    private String locationCode;
    private String locationType;
    private String zoneCode;

    private String waterLevel;   // StatusLevel: NORMAL / ELEVATED / HIGH / CRITICAL
    private Double waterValue;

    private String flowLevel;    // FlowLevel: LOW / NORMAL / HIGH
    private Double flowValue;

    private String capacityLevel; // CapacityLevel: FULL / REDUCED / MINIMAL / OFFLINE
    private Integer activePumps;
    private Integer totalPumps;

    private String riskLevel;    // RiskLevel: LOW / MODERATE / HIGH / EXTREME
    private String riskReason;

    private String recommendation;        // ActionType: MONITOR / PREPARE / ACTIVATE / EVACUATE
    private String recommendationPriority; // Priority
    private String recommendationDescription;

    public LocationStateDTO() {
    }

    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

    public String getLocationType() { return locationType; }
    public void setLocationType(String locationType) { this.locationType = locationType; }

    public String getZoneCode() { return zoneCode; }
    public void setZoneCode(String zoneCode) { this.zoneCode = zoneCode; }

    public String getWaterLevel() { return waterLevel; }
    public void setWaterLevel(String waterLevel) { this.waterLevel = waterLevel; }

    public Double getWaterValue() { return waterValue; }
    public void setWaterValue(Double waterValue) { this.waterValue = waterValue; }

    public String getFlowLevel() { return flowLevel; }
    public void setFlowLevel(String flowLevel) { this.flowLevel = flowLevel; }

    public Double getFlowValue() { return flowValue; }
    public void setFlowValue(Double flowValue) { this.flowValue = flowValue; }

    public String getCapacityLevel() { return capacityLevel; }
    public void setCapacityLevel(String capacityLevel) { this.capacityLevel = capacityLevel; }

    public Integer getActivePumps() { return activePumps; }
    public void setActivePumps(Integer activePumps) { this.activePumps = activePumps; }

    public Integer getTotalPumps() { return totalPumps; }
    public void setTotalPumps(Integer totalPumps) { this.totalPumps = totalPumps; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getRiskReason() { return riskReason; }
    public void setRiskReason(String riskReason) { this.riskReason = riskReason; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public String getRecommendationPriority() { return recommendationPriority; }
    public void setRecommendationPriority(String recommendationPriority) {
        this.recommendationPriority = recommendationPriority;
    }

    public String getRecommendationDescription() { return recommendationDescription; }
    public void setRecommendationDescription(String recommendationDescription) {
        this.recommendationDescription = recommendationDescription;
    }
}
