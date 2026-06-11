package com.ftn.service.dto.sensor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.ftn.model.enums.SensorType;

public class SensorRequest {

    @NotNull
    private Long locationId;

    @NotBlank
    @Size(max = 100)
    private String tagName;

    @Size(max = 100)
    private String displayCode;

    @NotNull
    private SensorType sensorType;

    private Long unitId;

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }

    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }

    public String getDisplayCode() { return displayCode; }
    public void setDisplayCode(String displayCode) { this.displayCode = displayCode; }

    public SensorType getSensorType() { return sensorType; }
    public void setSensorType(SensorType sensorType) { this.sensorType = sensorType; }

    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }
}
