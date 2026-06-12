package com.ftn.service.dto.simulation;

import com.ftn.model.enums.ActionType;
import com.ftn.model.enums.CapacityLevel;
import com.ftn.model.enums.FlowLevel;
import com.ftn.model.enums.LocationType;
import com.ftn.model.enums.Priority;
import com.ftn.model.enums.RiskLevel;
import com.ftn.model.enums.StatusLevel;

public class LocationStateDTO {

    private String locationCode;
    private LocationType locationType;
    private String zoneCode;

    private StatusLevel waterLevel;
    private Double waterValue;

    private FlowLevel flowLevel;
    private Double flowValue;

    private CapacityLevel capacityLevel;
    private Integer activePumps;
    private Integer totalPumps;

    private RiskLevel riskLevel;
    private String riskReason;

    private ActionType recommendation;
    private Priority recommendationPriority;
    private String recommendationDescription;

    public LocationStateDTO() {
    }

    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

    public LocationType getLocationType() { return locationType; }
    public void setLocationType(LocationType locationType) { this.locationType = locationType; }

    public String getZoneCode() { return zoneCode; }
    public void setZoneCode(String zoneCode) { this.zoneCode = zoneCode; }

    public StatusLevel getWaterLevel() { return waterLevel; }
    public void setWaterLevel(StatusLevel waterLevel) { this.waterLevel = waterLevel; }

    public Double getWaterValue() { return waterValue; }
    public void setWaterValue(Double waterValue) { this.waterValue = waterValue; }

    public FlowLevel getFlowLevel() { return flowLevel; }
    public void setFlowLevel(FlowLevel flowLevel) { this.flowLevel = flowLevel; }

    public Double getFlowValue() { return flowValue; }
    public void setFlowValue(Double flowValue) { this.flowValue = flowValue; }

    public CapacityLevel getCapacityLevel() { return capacityLevel; }
    public void setCapacityLevel(CapacityLevel capacityLevel) { this.capacityLevel = capacityLevel; }

    public Integer getActivePumps() { return activePumps; }
    public void setActivePumps(Integer activePumps) { this.activePumps = activePumps; }

    public Integer getTotalPumps() { return totalPumps; }
    public void setTotalPumps(Integer totalPumps) { this.totalPumps = totalPumps; }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public String getRiskReason() { return riskReason; }
    public void setRiskReason(String riskReason) { this.riskReason = riskReason; }

    public ActionType getRecommendation() { return recommendation; }
    public void setRecommendation(ActionType recommendation) { this.recommendation = recommendation; }

    public Priority getRecommendationPriority() { return recommendationPriority; }
    public void setRecommendationPriority(Priority recommendationPriority) {
        this.recommendationPriority = recommendationPriority;
    }

    public String getRecommendationDescription() { return recommendationDescription; }
    public void setRecommendationDescription(String recommendationDescription) {
        this.recommendationDescription = recommendationDescription;
    }
}
