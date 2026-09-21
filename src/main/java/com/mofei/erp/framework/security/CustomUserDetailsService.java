package com.mofei.erp.framework.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mofei.erp.modules.user.domain.Employee;
import com.mofei.erp.modules.user.mapper.EmployeeMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeMapper employeeMapper;

    public CustomUserDetailsService(EmployeeMapper employeeMapper) {
        this.employeeMapper = employeeMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 支援以 admin、員工編號 或 email 登入
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        if ("admin".equalsIgnoreCase(username)) {
            wrapper.eq(Employee::getEmployeeId, "2026000")
                   .or().eq(Employee::getEmail, "admin@mofei.com")
                   .or().eq(Employee::getEmployeeName, "系統管理員");
        } else {
            wrapper.eq(Employee::getEmployeeId, username)
                   .or().eq(Employee::getEmail, username);
        }

        Employee employee = employeeMapper.selectOne(wrapper);
        if (employee == null) {
            throw new UsernameNotFoundException("找不到使用者帳號: " + username);
        }

        if ("停權".equals(employee.getStatus()) || "離職".equals(employee.getStatus())) {
            throw new UsernameNotFoundException("帳號已被停權或已離職");
        }

        String roleName = "ROLE_" + (employee.getRole() != null ? employee.getRole().toUpperCase() : "EMPLOYEE");

        return new User(
            employee.getEmployeeId(),
            employee.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority(roleName))
        );
    }
}
