package com.mofei.erp.modules.user.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

public record EmployeeVO(
    String employeeId,
    String employeeName,
    Boolean forceChangePwd,
    String phone,
    String email,
    String status,
    String role,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) implements Serializable {

    public static EmployeeVO fromEntity(Employee e) {
        if (e == null) return null;
        return new EmployeeVO(
            e.getEmployeeId(),
            e.getEmployeeName(),
            e.getForceChangePwd(),
            e.getPhone(),
            e.getEmail(),
            e.getStatus(),
            e.getRole(),
            e.getCreatedAt(),
            e.getUpdatedAt()
        );
    }
}
