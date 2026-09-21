package com.mofei.erp.modules.product.domain;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

public record ProductDTO(
    String productId,
    @NotBlank(message = "產品名稱不能為空")
    String productName,
    String attributes,
    @NotNull(message = "產品價格不能為空")
    @DecimalMin(value = "0", message = "產品價格必須大於或等於0")
    @Digits(integer = 10, fraction = 0, message = "產品價格必須為整數")
    BigDecimal price,
    @NotNull(message = "產品庫存不能為空")
    @Min(value = 0, message = "庫存數量不能為負數")
    Integer stock,
    @NotBlank(message = "負責員工編號不能為空")
    String employeeId,
    String status
) implements Serializable {}
