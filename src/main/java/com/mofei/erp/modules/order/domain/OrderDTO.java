package com.mofei.erp.modules.order.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public record OrderDTO(
    String orderId,
    @NotBlank(message = "客戶編號不能為空")
    String customerId,
    @NotBlank(message = "業務員工編號不能為空")
    String employeeId,
    BigDecimal totalAmount,
    String status,
    @NotBlank(message = "收件人姓名不能為空")
    String recipientName,
    @NotBlank(message = "收件人電話不能為空")
    String recipientPhone,
    @NotBlank(message = "收件地址全稱不能為空")
    String recipientAddress,
    @NotEmpty(message = "訂單明細不能為空")
    @Valid
    List<OrderDetailDTO> details
) implements Serializable {}
