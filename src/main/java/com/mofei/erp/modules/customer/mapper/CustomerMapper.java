package com.mofei.erp.modules.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mofei.erp.modules.customer.domain.Customer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {
}
