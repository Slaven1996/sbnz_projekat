package com.ftn.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.ftn.model.enums.LocationType;

@Entity
@Table(name = "locations")
public class Location implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "display_code")
    private String displayCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private LocationType type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @Column(name = "pos_x")
    private Double posX;

    @Column(name = "pos_y")
    private Double posY;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @OneToOne(mappedBy = "location", fetch = FetchType.LAZY)
    private WeatherCondition weatherCondition;

    public Location() {
    }

    public Location(String code, LocationType type) {
        this.code = code;
        this.displayCode = code;
        this.type = type;
    }

    public Location(String code, LocationType type, Zone zone) {
        this.code = code;
        this.displayCode = code;
        this.type = type;
        this.zone = zone;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDisplayCode() { return displayCode; }
    public void setDisplayCode(String displayCode) { this.displayCode = displayCode; }

    public LocationType getType() { return type; }
    public void setType(LocationType type) { this.type = type; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public Zone getZone() { return zone; }
    public void setZone(Zone zone) { this.zone = zone; }

    public Double getPosX() { return posX; }
    public void setPosX(Double posX) { this.posX = posX; }

    public Double getPosY() { return posY; }
    public void setPosY(Double posY) { this.posY = posY; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public WeatherCondition getWeatherCondition() { return weatherCondition; }
    public void setWeatherCondition(WeatherCondition weatherCondition) { this.weatherCondition = weatherCondition; }

    @Override
    public String toString() {
        return "Location[" + code + ", " + type + "]";
    }
}
