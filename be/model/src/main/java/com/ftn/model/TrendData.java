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
@Table(name = "trend_data",
       indexes = {
           @Index(name = "idx_trend_location_time", columnList = "location_code, log_time"),
           @Index(name = "idx_trend_tag_time", columnList = "tag_name, log_time")
       })
public class TrendData implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location_code", nullable = false)
    private String locationCode;

    @Column(name = "tag_name", nullable = false)
    private String tagName;

    @Column(name = "log_time", nullable = false)
    private LocalDateTime logTime;

    @Column(name = "tag_value", nullable = false)
    private double tagValue;

    @Column(name = "valid", nullable = false)
    private boolean valid = true;

    public TrendData() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }

    public LocalDateTime getLogTime() { return logTime; }
    public void setLogTime(LocalDateTime logTime) { this.logTime = logTime; }

    public double getTagValue() { return tagValue; }
    public void setTagValue(double tagValue) { this.tagValue = tagValue; }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    @Override
    public String toString() {
        return "TrendData[id=" + id + ", locationCode=" + locationCode
                + ", tagName=" + tagName + ", logTime=" + logTime
                + ", tagValue=" + tagValue + ", valid=" + valid + "]";
    }
}
