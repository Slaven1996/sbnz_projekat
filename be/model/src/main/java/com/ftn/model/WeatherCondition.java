package com.ftn.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "weather_conditions")
public class WeatherCondition implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "location_id", unique = true, nullable = false)
    private Location location;

    @Column(name = "precipitation")
    private double precipitation;

    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    public WeatherCondition() {
    }

    public WeatherCondition(Location location, double precipitation) {
        this.location = location;
        this.precipitation = precipitation;
        this.lastUpdate = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    public double getPrecipitation() { return precipitation; }
    public void setPrecipitation(double precipitation) { this.precipitation = precipitation; }

    public LocalDateTime getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }

    @Override
    public String toString() {
        return "WeatherCondition[id=" + id + ", location=" + location
                + ", precipitation=" + precipitation + ", lastUpdate=" + lastUpdate + "]";
    }
}
