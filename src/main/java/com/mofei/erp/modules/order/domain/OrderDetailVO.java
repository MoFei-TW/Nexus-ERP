package com.mofei.erp.modules.order.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderDetailVO(
    Integer orderDetailId,
    String orderId,
    String productId,
    String productNameSnapshot,
    BigDecimal unitPrice,
    Integer quantity,
    BigDecimal discountAmount,
    BigDecimal subtotal,
    LocalDateTime createdAt
) implements Serializable {

    public static OrderDetailVO fromEntity(OrderDetail d) {
        if (d == null) return null;
        return new OrderDetailVO(
            d.getOrderDetailId(),
            d.getOrderId(),
            d.getProductId(),
            d.getProductNameSnapshot(),
            d.getUnitPrice(),
            d.getQuantity(),
            d.getDiscountAmount(),
            d.getSubtotal(),
            d.getCreatedAt()
        );
    }
}
