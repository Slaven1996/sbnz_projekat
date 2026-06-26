package com.ftn.service.dto.monitoring;

public class MonitoringLocationDTO {

    private String locationCode;
    private String displayCode;
    private String locationType;
    private String zoneCode;
    private Double posX;
    private Double posY;

    private String waterLevel;
    private Double waterValue;

    private String flowLevel;
    private Double flowValue;

    private String capacityLevel;
    private Integer activePumps;
    private Integer totalPumps;

    private String riskLevel;
    private String riskReason;

    private String recommendation;
    private String recommendationPriority;
    private String recommendationDescription;

    private String severity;

    private boolean monitored = true;

    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

    public String getDisplayCode() { return displayCode; }
    public void setDisplayCode(String displayCode) { this.displayCode = displayCode; }

    public String getLocationType() { return locationType; }
    public void setLocationType(String locationType) { this.locationType = locationType; }

    public String getZoneCode() { return zoneCode; }
    public void setZoneCode(String zoneCode) { this.zoneCode = zoneCode; }

    public Double getPosX() { return posX; }
    public void setPosX(Double posX) { this.posX = posX; }

    public Double getPosY() { return posY; }
    public void setPosY(Double posY) { this.posY = posY; }

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

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public boolean isMonitored() { return monitored; }
    public void setMonitored(boolean monitored) { this.monitored = monitored; }
}
