package com.ftn.service.dto.threshold;

import com.ftn.model.ThresholdConfig;
import com.ftn.model.enums.LocationType;
import com.ftn.model.enums.ParameterType;

public class ThresholdConfigResponse {

    private Long id;
    private LocationType locationType;
    private ParameterType parameterType;
    private double normalMax;
    private double warningMax;
    private Double criticalMax;

    public ThresholdConfigResponse() {
    }

    public ThresholdConfigResponse(ThresholdConfig t) {
        this.id = t.getId();
        this.locationType = t.getLocationType();
        this.parameterType = t.getParameterType();
        this.normalMax = t.getNormalMax();
        this.warningMax = t.getWarningMax();
        this.criticalMax = t.getCriticalMax();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocationType getLocationType() { return locationType; }
    public void setLocationType(LocationType locationType) { this.locationType = locationType; }

    public ParameterType getParameterType() { return parameterType; }
    public void setParameterType(ParameterType parameterType) { this.parameterType = parameterType; }

    public double getNormalMax() { return normalMax; }
    public void setNormalMax(double normalMax) { this.normalMax = normalMax; }

    public double getWarningMax() { return warningMax; }
    public void setWarningMax(double warningMax) { this.warningMax = warningMax; }

    public Double getCriticalMax() { return criticalMax; }
    public void setCriticalMax(Double criticalMax) { this.criticalMax = criticalMax; }
}
