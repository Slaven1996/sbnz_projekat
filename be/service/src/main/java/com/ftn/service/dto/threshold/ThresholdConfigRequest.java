package com.ftn.service.dto.threshold;

import javax.validation.constraints.NotNull;

import com.ftn.model.enums.LocationType;
import com.ftn.model.enums.ParameterType;

public class ThresholdConfigRequest {

    @NotNull
    private LocationType locationType;

    @NotNull
    private ParameterType parameterType;

    @NotNull
    private Double normalMax;

    @NotNull
    private Double warningMax;

    private Double criticalMax;

    public LocationType getLocationType() { return locationType; }
    public void setLocationType(LocationType locationType) { this.locationType = locationType; }

    public ParameterType getParameterType() { return parameterType; }
    public void setParameterType(ParameterType parameterType) { this.parameterType = parameterType; }

    public Double getNormalMax() { return normalMax; }
    public void setNormalMax(Double normalMax) { this.normalMax = normalMax; }

    public Double getWarningMax() { return warningMax; }
    public void setWarningMax(Double warningMax) { this.warningMax = warningMax; }

    public Double getCriticalMax() { return criticalMax; }
    public void setCriticalMax(Double criticalMax) { this.criticalMax = criticalMax; }
}
