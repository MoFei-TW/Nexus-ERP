package com.mofei.erp.modules.product.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductVO(
    String productId,
    String productName,
    String attributes,
    BigDecimal price,
    Integer stock,
    String employeeId,
    String employeeName,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) implements Serializable {

    public static ProductVO fromEntity(Product p, String employeeName) {
        if (p == null) return null;
        return new ProductVO(
            p.getProductId(),
            p.getProductName(),
            p.getAttributes(),
            p.getPrice(),
            p.getStock(),
            p.getEmployeeId(),
            employeeName,
            p.getStatus(),
            p.getCreatedAt(),
            p.getUpdatedAt()
        );
    }
}
