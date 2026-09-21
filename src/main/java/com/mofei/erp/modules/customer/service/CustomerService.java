package com.mofei.erp.modules.customer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mofei.erp.modules.customer.domain.CustomerDTO;
import com.mofei.erp.modules.customer.domain.CustomerVO;

public interface CustomerService {
    IPage<CustomerVO> queryPage(int current, int size, String keyword, String customerId, String customerName);
    CustomerVO getById(String customerId);
    CustomerVO create(CustomerDTO dto);
    CustomerVO update(String customerId, CustomerDTO dto);
    void delete(String customerId);
}
