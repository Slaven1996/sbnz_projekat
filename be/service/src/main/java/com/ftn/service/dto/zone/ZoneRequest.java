package com.ftn.service.dto.zone;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ZoneRequest {

    @NotBlank
    @Size(max = 50)
    private String code;

    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
