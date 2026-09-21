package com.mofei.erp.modules.order.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderVO(
    String orderId,
    String customerId,
    String customerName,
    String employeeId,
    String employeeName,
    BigDecimal totalAmount,
    String status,
    String recipientName,
    String recipientPhone,
    String recipientAddress,
    LocalDateTime createdAt,
    List<OrderDetailVO> details
) implements Serializable {

    public static OrderVO fromEntity(Orders o, String customerName, String employeeName, List<OrderDetailVO> details) {
        if (o == null) return null;
        return new OrderVO(
            o.getOrderId(),
            o.getCustomerId(),
            customerName,
            o.getEmployeeId(),
            employeeName,
            o.getTotalAmount(),
            o.getStatus(),
            o.getRecipientName(),
            o.getRecipientPhone(),
            o.getRecipientAddress(),
            o.getCreatedAt(),
            details
        );
    }
}
