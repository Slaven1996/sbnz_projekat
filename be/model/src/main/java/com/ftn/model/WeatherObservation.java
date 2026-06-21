package com.ftn.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "weather_observations",
       indexes = {
           @Index(name = "idx_weather_obs_location_time", columnList = "location_code, observed_at")
       })
public class WeatherObservation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location_code", nullable = false)
    private String locationCode;

    @Column(name = "observed_at", nullable = false)
    private LocalDateTime observedAt;

    @Column(name = "precipitation", nullable = false)
    private double precipitation;

    public WeatherObservation() {
    }

    public WeatherObservation(String locationCode, LocalDateTime observedAt, double precipitation) {
        this.locationCode = locationCode;
        this.observedAt = observedAt;
        this.precipitation = precipitation;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

    public LocalDateTime getObservedAt() { return observedAt; }
    public void setObservedAt(LocalDateTime observedAt) { this.observedAt = observedAt; }

    public double getPrecipitation() { return precipitation; }
    public void setPrecipitation(double precipitation) { this.precipitation = precipitation; }

    @Override
    public String toString() {
        return "WeatherObservation[id=" + id + ", locationCode=" + locationCode
                + ", observedAt=" + observedAt + ", precipitation=" + precipitation + "]";
    }
}
