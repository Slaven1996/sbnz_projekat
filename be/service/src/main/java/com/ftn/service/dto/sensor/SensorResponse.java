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
}
