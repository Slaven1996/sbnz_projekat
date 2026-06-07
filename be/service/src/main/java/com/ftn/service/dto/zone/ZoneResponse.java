package com.ftn.service.dto.zone;

import com.ftn.model.Zone;

public class ZoneResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private int locationCount;

    public ZoneResponse() {
    }

    public ZoneResponse(Zone z) {
        this.id = z.getId();
        this.code = z.getCode();
        this.name = z.getName();
        this.description = z.getDescription();
        this.locationCount = z.getLocations() != null ? z.getLocations().size() : 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getLocationCount() { return locationCount; }
    public void setLocationCount(int locationCount) { this.locationCount = locationCount; }
}
