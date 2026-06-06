package com.ftn.service.dto.location;

import java.time.LocalDateTime;

import com.ftn.model.WeatherCondition;

public class WeatherConditionDto {

    private Long id;
    private double precipitation;
    private LocalDateTime lastUpdate;

    public WeatherConditionDto() {
    }

    public WeatherConditionDto(WeatherCondition w) {
        this.id = w.getId();
        this.precipitation = w.getPrecipitation();
        this.lastUpdate = w.getLastUpdate();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public double getPrecipitation() { return precipitation; }
    public void setPrecipitation(double precipitation) { this.precipitation = precipitation; }

    public LocalDateTime getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
}
