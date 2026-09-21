package com.mofei.erp.modules.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mofei.erp.modules.user.domain.EmployeeDTO;
import com.mofei.erp.modules.user.domain.EmployeeVO;

public interface EmployeeService {
    IPage<EmployeeVO> queryPage(int current, int size, String keyword, String employeeId, String employeeName);
    EmployeeVO getById(String employeeId);
    EmployeeVO create(EmployeeDTO dto);
    EmployeeVO update(String employeeId, EmployeeDTO dto);
    void softDelete(String employeeId);
    void updateStatus(String employeeId, String status);
}
