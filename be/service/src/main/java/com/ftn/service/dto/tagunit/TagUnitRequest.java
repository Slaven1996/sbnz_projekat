package com.ftn.service.dto.tagunit;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class TagUnitRequest {

    @NotBlank
    @Size(max = 50)
    private String code;

    @NotBlank
    @Size(max = 50)
    private String unit;

    @Size(max = 255)
    private String description;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
