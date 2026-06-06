package com.ftn.service.dto.department;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class DepartmentRequest {

    @NotBlank
    @Size(max = 50)
    private String code;

    @Size(max = 255)
    private String description;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
