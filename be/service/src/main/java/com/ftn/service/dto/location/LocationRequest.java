package com.ftn.service.dto.location;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.ftn.model.enums.LocationType;

public class LocationRequest {

    @NotBlank
    @Size(max = 50)
    private String code;

    @Size(max = 50)
    private String displayCode;

    @NotNull
    private LocationType type;

    private Long departmentId;

    private Long zoneId;

    private Double posX;

    private Double posY;

    private Boolean active = true;

    @Valid
    private WeatherConditionDto weatherCondition;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDisplayCode() { return displayCode; }
    public void setDisplayCode(String displayCode) { this.displayCode = displayCode; }

    public LocationType getType() { return type; }
    public void setType(LocationType type) { this.type = type; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }

    public Double getPosX() { return posX; }
    public void setPosX(Double posX) { this.posX = posX; }

    public Double getPosY() { return posY; }
    public void setPosY(Double posY) { this.posY = posY; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public WeatherConditionDto getWeatherCondition() { return weatherCondition; }
    public void setWeatherCondition(WeatherConditionDto weatherCondition) { this.weatherCondition = weatherCondition; }
}
