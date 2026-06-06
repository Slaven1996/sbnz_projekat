package com.ftn.service.dto.department;

import com.ftn.model.Department;

public class DepartmentResponse {

    private Long id;
    private String code;
    private String description;

    public DepartmentResponse() {
    }

    public DepartmentResponse(Department d) {
        this.id = d.getId();
        this.code = d.getCode();
        this.description = d.getDescription();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
