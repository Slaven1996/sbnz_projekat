package com.ftn.service.dto.tagunit;

import com.ftn.model.TagUnit;

public class TagUnitResponse {

    private Long id;
    private String code;
    private String unit;
    private String description;

    public TagUnitResponse() {
    }

    public TagUnitResponse(TagUnit t) {
        this.id = t.getId();
        this.code = t.getCode();
        this.unit = t.getUnit();
        this.description = t.getDescription();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
