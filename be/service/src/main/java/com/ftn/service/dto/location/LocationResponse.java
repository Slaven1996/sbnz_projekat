package com.ftn.service.dto.location;

import com.ftn.model.Location;
import com.ftn.model.enums.LocationType;

public class LocationResponse {

    private Long id;
    private String code;
    private String displayCode;
    private LocationType type;
    private boolean active;
    private Double posX;
    private Double posY;
    private Long departmentId;
    private String departmentCode;
    private Long zoneId;
    private String zoneCode;
    private WeatherConditionDto weatherCondition;

    public LocationResponse() {
    }

    public LocationResponse(Location l) {
        this.id = l.getId();
        this.code = l.getCode();
        this.displayCode = l.getDisplayCode();
        this.type = l.getType();
        this.active = l.isActive();
        this.posX = l.getPosX();
        this.posY = l.getPosY();
        if (l.getDepartment() != null) {
            this.departmentId = l.getDepartment().getId();
            this.departmentCode = l.getDepartment().getCode();
        }
        if (l.getZone() != null) {
            this.zoneId = l.getZone().getId();
            this.zoneCode = l.getZone().getCode();
        }
        if (l.getWeatherCondition() != null) {
            this.weatherCondition = new WeatherConditionDto(l.getWeatherCondition());
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDisplayCode() { return displayCode; }
    public void setDisplayCode(String displayCode) { this.displayCode = displayCode; }

    public LocationType getType() { return type; }
    public void setType(LocationType type) { this.type = type; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Double getPosX() { return posX; }
    public void setPosX(Double posX) { this.posX = posX; }

    public Double getPosY() { return posY; }
    public void setPosY(Double posY) { this.posY = posY; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }

    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }

    public String getZoneCode() { return zoneCode; }
    public void setZoneCode(String zoneCode) { this.zoneCode = zoneCode; }

    public WeatherConditionDto getWeatherCondition() { return weatherCondition; }
    public void setWeatherCondition(WeatherConditionDto weatherCondition) { this.weatherCondition = weatherCondition; }
}
