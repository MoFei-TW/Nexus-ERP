package com.mofei.erp.modules.order.domain;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

public record OrderDetailDTO(
    Integer orderDetailId,
    String orderId,
    @NotBlank(message = "產品編號不能為空")
    String productId,
    String productNameSnapshot,
    @NotNull(message = "成交單價不能為空")
    @DecimalMin(value = "0.00", message = "單價不能為負數")
    BigDecimal unitPrice,
    @NotNull(message = "購買數量不能為空")
    @Min(value = 1, message = "購買數量必須大於0")
    Integer quantity,
    BigDecimal discountAmount,
    BigDecimal subtotal
) implements Serializable {}
