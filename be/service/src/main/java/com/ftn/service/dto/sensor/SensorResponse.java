package com.ftn.service.dto.sensor;

import com.ftn.model.Sensor;
import com.ftn.model.enums.SensorType;

public class SensorResponse {

    private Long id;
    private String tagName;
    private String displayCode;
    private SensorType sensorType;
    private Long locationId;
    private String locationCode;
    private Long unitId;
    private String unitCode;
    private Double engLow;
    private Double engHigh;
    private Double rawLow;
    private Double rawHigh;
    private Integer logInterval;

    public SensorResponse() {
    }

    public SensorResponse(Sensor s) {
        this.id = s.getId();
        this.tagName = s.getTagName();
        this.displayCode = s.getDisplayCode();
        this.sensorType = s.getSensorType();
        if (s.getLocation() != null) {
            this.locationId = s.getLocation().getId();
            this.locationCode = s.getLocation().getCode();
        }
        if (s.getUnit() != null) {
            this.unitId = s.getUnit().getId();
            this.unitCode = s.getUnit().getCode();
        }
        this.engLow = s.getEngLow();
        this.engHigh = s.getEngHigh();
        this.rawLow = s.getRawLow();
        this.rawHigh = s.getRawHigh();
        this.logInterval = s.getLogInterval();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }

    public String getDisplayCode() { return displayCode; }
    public void setDisplayCode(String displayCode) { this.displayCode = displayCode; }

    public SensorType getSensorType() { return sensorType; }
    public void setSensorType(SensorType sensorType) { this.sensorType = sensorType; }

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }

    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }

    public String getUnitCode() { return unitCode; }
    public void setUnitCode(String unitCode) { this.unitCode = unitCode; }

    public Double getEngLow() { return engLow; }
    public void setEngLow(Double engLow) { this.engLow = engLow; }

    public Double getEngHigh() { return engHigh; }
    public void setEngHigh(Double engHigh) { this.engHigh = engHigh; }

    public Double getRawLow() { return rawLow; }
    public void setRawLow(Double rawLow) { this.rawLow = rawLow; }

    public Double getRawHigh() { return rawHigh; }
    public void setRawHigh(Double rawHigh) { this.rawHigh = rawHigh; }

    public Integer getLogInterval() { return logInterval; }
    public void setLogInterval(Integer logInterval) { this.logInterval = logInterval; }
}
