package com.ftn.service.dto.trenddata;

import java.time.LocalDateTime;

import com.ftn.model.TrendData;

public class TrendDataResponse {

    private Long id;
    private String locationCode;
    private String tagName;
    private LocalDateTime logTime;
    private double tagValue;
    private boolean valid;

    public TrendDataResponse() {
    }

    public TrendDataResponse(TrendData t) {
        this.id = t.getId();
        this.locationCode = t.getLocationCode();
        this.tagName = t.getTagName();
        this.logTime = t.getLogTime();
        this.tagValue = t.getTagValue();
        this.valid = t.isValid();
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
}
