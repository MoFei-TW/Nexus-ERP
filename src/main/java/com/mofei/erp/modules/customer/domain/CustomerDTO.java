package com.mofei.erp.modules.customer.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

public record CustomerDTO(
    String customerId,
    @NotBlank(message = "客戶姓名不能為空")
    String customerName,
    @NotBlank(message = "客戶電話不能為空")
    @Pattern(regexp = "^[0-9]{4}-[0-9]{3}-[0-9]{3}$", message = "電話格式必須為 0912-345-678")
    String phone,
    @Pattern(regexp = "^$|^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "格式不正確（需包含 @ 與有效網域，例如 service@example.com）")
    String email,
    String companyName,
    @Pattern(regexp = "^$|^[0-9]{4}-[0-9]{4}$", message = "統一編號格式必須為 8888-8888")
    String taxId,
    String country,
    @NotBlank(message = "城市不能為空")
    String city,
    @NotBlank(message = "區域不能為空")
    String district,
    @NotBlank(message = "詳細地址不能為空")
    String address,
    @NotBlank(message = "負責員工編號不能為空")
    String employeeId
) implements Serializable {}
