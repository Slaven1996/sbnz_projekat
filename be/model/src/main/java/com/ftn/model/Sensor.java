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
import javax.persistence.Table;

import com.ftn.model.enums.SensorType;

@Entity
@Table(name = "sensors")
public class Sensor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "tag_name", nullable = false)
    private String tagName;

    @Column(name = "display_code")
    private String displayCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "sensor_type", nullable = false)
    private SensorType sensorType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "unit_id")
    private TagUnit unit;

    @Column(name = "eng_low")
    private Double engLow;

    @Column(name = "eng_high")
    private Double engHigh;

    @Column(name = "raw_low")
    private Double rawLow;

    @Column(name = "raw_high")
    private Double rawHigh;

    public Sensor() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }

    public String getDisplayCode() { return displayCode; }
    public void setDisplayCode(String displayCode) { this.displayCode = displayCode; }

    public SensorType getSensorType() { return sensorType; }
    public void setSensorType(SensorType sensorType) { this.sensorType = sensorType; }

    public TagUnit getUnit() { return unit; }
    public void setUnit(TagUnit unit) { this.unit = unit; }

    public Double getEngLow() { return engLow; }
    public void setEngLow(Double engLow) { this.engLow = engLow; }

    public Double getEngHigh() { return engHigh; }
    public void setEngHigh(Double engHigh) { this.engHigh = engHigh; }

    public Double getRawLow() { return rawLow; }
    public void setRawLow(Double rawLow) { this.rawLow = rawLow; }

    public Double getRawHigh() { return rawHigh; }
    public void setRawHigh(Double rawHigh) { this.rawHigh = rawHigh; }

    @Override
    public String toString() {
        return "Sensor[id=" + id + ", tagName=" + tagName + ", displayCode=" + displayCode
                + ", sensorType=" + sensorType + ", location=" + (location != null ? location.getCode() : "?")
                + ", unit=" + (unit != null ? unit.getCode() : "?") + "]";
    }
}
