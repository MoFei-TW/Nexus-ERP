package com.mofei.erp.modules.customer.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

public record CustomerVO(
    String customerId,
    String customerName,
    String phone,
    String email,
    String companyName,
    String taxId,
    String country,
    String city,
    String district,
    String address,
    String employeeId,
    String employeeName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) implements Serializable {

    public static CustomerVO fromEntity(Customer c, String employeeName) {
        if (c == null) return null;
        return new CustomerVO(
            c.getCustomerId(),
            c.getCustomerName(),
            c.getPhone(),
            c.getEmail(),
            c.getCompanyName(),
            c.getTaxId(),
            c.getCountry(),
            c.getCity(),
            c.getDistrict(),
            c.getAddress(),
            c.getEmployeeId(),
            employeeName,
            c.getCreatedAt(),
            c.getUpdatedAt()
        );
    }
}
