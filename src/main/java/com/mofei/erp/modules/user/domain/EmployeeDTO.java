package com.mofei.erp.modules.user.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

public record EmployeeDTO(
    String employeeId,
    @NotBlank(message = "員工姓名不能為空")
    String employeeName,
    String password,
    @NotBlank(message = "員工電話不能為空")
    @Pattern(regexp = "^[0-9]{4}-[0-9]{3}-[0-9]{3}$", message = "電話格式必須為 0912-345-678")
    String phone,
    @NotBlank(message = "電子郵件不能為空")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = "電子郵件格式不正確")
    String email,
    String status,
    String role
) implements Serializable {}
