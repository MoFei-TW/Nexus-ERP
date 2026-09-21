package com.mofei.erp.modules.user.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("employee")
public class Employee {

    @TableId
    private String employeeId;
    private String employeeName;
    private String password;
    private Boolean forceChangePwd;
    private String phone;
    private String email;
    private String status;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Employee() {}

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Boolean getForceChangePwd() { return forceChangePwd; }
    public void setForceChangePwd(Boolean forceChangePwd) { this.forceChangePwd = forceChangePwd; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
