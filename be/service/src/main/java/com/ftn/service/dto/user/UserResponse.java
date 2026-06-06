package com.ftn.service.dto.user;

import com.ftn.model.User;
import com.ftn.model.enums.UserRole;

public class UserResponse {

    private Long id;
    private String userCode;
    private String name;
    private String lastName;
    private String email;
    private UserRole role;
    private boolean active;
    private Long departmentId;
    private String departmentCode;

    public UserResponse() {
    }

    public UserResponse(User u) {
        this.id = u.getId();
        this.userCode = u.getUserCode();
        this.name = u.getName();
        this.lastName = u.getLastName();
        this.email = u.getEmail();
        this.role = u.getRole();
        this.active = u.isActive();
        if (u.getDepartment() != null) {
            this.departmentId = u.getDepartment().getId();
            this.departmentCode = u.getDepartment().getCode();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserCode() { return userCode; }
    public void setUserCode(String userCode) { this.userCode = userCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }
}
