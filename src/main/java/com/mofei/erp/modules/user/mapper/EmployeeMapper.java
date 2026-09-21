package com.mofei.erp.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mofei.erp.modules.user.domain.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
