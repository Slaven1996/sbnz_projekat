package com.ftn.service.dto.user;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.ftn.model.enums.UserRole;

public class UserRequest {

    @NotBlank
    @Size(max = 50)
    private String userCode;

    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String lastName;

    @NotBlank
    @Email
    private String email;

    @Size(min = 6, max = 100)
    private String password;

    @NotNull
    private UserRole role;

    private Boolean active = true;

    private Long departmentId;

    public String getUserCode() { return userCode; }
    public void setUserCode(String userCode) { this.userCode = userCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
}
