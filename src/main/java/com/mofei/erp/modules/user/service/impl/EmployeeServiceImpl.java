package com.mofei.erp.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mofei.erp.common.BusinessException;
import com.mofei.erp.modules.user.domain.Employee;
import com.mofei.erp.modules.user.domain.EmployeeDTO;
import com.mofei.erp.modules.user.domain.EmployeeVO;
import com.mofei.erp.modules.user.mapper.EmployeeMapper;
import com.mofei.erp.modules.user.service.EmployeeService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    public EmployeeServiceImpl(EmployeeMapper employeeMapper, PasswordEncoder passwordEncoder) {
        this.employeeMapper = employeeMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public IPage<EmployeeVO> queryPage(int current, int size, String keyword, String employeeId, String employeeName) {
        Page<Employee> page = new Page<>(current, Math.min(size, 50));
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(employeeId)) {
            wrapper.like(Employee::getEmployeeId, employeeId.trim());
        }
        if (StringUtils.hasText(employeeName)) {
            wrapper.like(Employee::getEmployeeName, employeeName.trim());
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Employee::getEmployeeId, keyword.trim())
                             .or().like(Employee::getEmployeeName, keyword.trim())
                             .or().like(Employee::getPhone, keyword.trim())
                             .or().like(Employee::getEmail, keyword.trim()));
        }

        wrapper.orderByAsc(Employee::getEmployeeId);

        IPage<Employee> result = employeeMapper.selectPage(page, wrapper);
        return result.convert(EmployeeVO::fromEntity);
    }

    @Override
    public EmployeeVO getById(String employeeId) {
        Employee e = employeeMapper.selectById(employeeId);
        if (e == null) {
            throw new BusinessException(404, "找不到員工編號: " + employeeId);
        }
        return EmployeeVO.fromEntity(e);
    }

    @Override
    @Transactional
    public EmployeeVO create(EmployeeDTO dto) {
        // 新增員工編號由系統自動配置，不提供使用者輸入 (預設西元年前綴 2026)
        Page<Employee> maxPage = new Page<>(1, 1);
        IPage<Employee> result = employeeMapper.selectPage(maxPage,
                new LambdaQueryWrapper<Employee>()
                        .select(Employee::getEmployeeId)
                        .likeRight(Employee::getEmployeeId, "2026")
                        .orderByDesc(Employee::getEmployeeId));
        String id;
        if (result.getRecords().isEmpty()) {
            id = "2026001";
        } else {
            String maxId = result.getRecords().get(0).getEmployeeId();
            try {
                long num = Long.parseLong(maxId);
                id = String.valueOf(num + 1);
            } catch (NumberFormatException ex) {
                long count = employeeMapper.selectCount(new LambdaQueryWrapper<Employee>().likeRight(Employee::getEmployeeId, "2026"));
                id = String.format("2026%03d", count + 1);
            }
        }
        while (employeeMapper.selectById(id) != null) {
            long num = Long.parseLong(id);
            id = String.valueOf(num + 1);
        }

        // 檢核員工編號不得重複
        if (employeeMapper.selectById(id) != null) {
            throw new BusinessException(400, "員工編號已存在: " + id);
        }

        // 檢核員工電話不得重複
        if (StringUtils.hasText(dto.phone())) {
            Long phoneCount = employeeMapper.selectCount(
                    new LambdaQueryWrapper<Employee>().eq(Employee::getPhone, dto.phone().trim())
            );
            if (phoneCount != null && phoneCount > 0) {
                throw new BusinessException(400, "員工電話已存在: " + dto.phone().trim());
            }
        }

        // 檢核電子郵件不得重複
        if (StringUtils.hasText(dto.email())) {
            Long emailCount = employeeMapper.selectCount(
                    new LambdaQueryWrapper<Employee>().eq(Employee::getEmail, dto.email().trim())
            );
            if (emailCount != null && emailCount > 0) {
                throw new BusinessException(400, "電子郵件已存在: " + dto.email().trim());
            }
        }

        Employee e = new Employee();
        e.setEmployeeId(id);
        e.setEmployeeName(dto.employeeName());
        String rawPassword = StringUtils.hasText(dto.password()) ? dto.password() : "0000000";
        e.setPassword(passwordEncoder.encode(rawPassword));
        e.setForceChangePwd(true);
        e.setPhone(dto.phone());
        e.setEmail(dto.email());
        e.setStatus(StringUtils.hasText(dto.status()) ? dto.status() : "在職");
        e.setRole(StringUtils.hasText(dto.role()) ? dto.role() : "EMPLOYEE");
        e.setCreatedAt(LocalDateTime.now());
        e.setUpdatedAt(LocalDateTime.now());

        employeeMapper.insert(e);
        return EmployeeVO.fromEntity(e);
    }

    @Override
    @Transactional
    public EmployeeVO update(String employeeId, EmployeeDTO dto) {
        Employee e = employeeMapper.selectById(employeeId);
        if (e == null) {
            throw new BusinessException(404, "找不到欲修改的員工: " + employeeId);
        }

        // 檢核員工編號不得重複 (排除自身)
        if (StringUtils.hasText(dto.employeeId()) && !dto.employeeId().trim().equals(employeeId)) {
            if (employeeMapper.selectById(dto.employeeId().trim()) != null) {
                throw new BusinessException(400, "員工編號已存在: " + dto.employeeId().trim());
            }
        }

        // 檢核員工電話不得重複 (排除自身)
        if (StringUtils.hasText(dto.phone())) {
            Long phoneCount = employeeMapper.selectCount(
                    new LambdaQueryWrapper<Employee>()
                            .eq(Employee::getPhone, dto.phone().trim())
                            .ne(Employee::getEmployeeId, employeeId)
            );
            if (phoneCount != null && phoneCount > 0) {
                throw new BusinessException(400, "員工電話已存在: " + dto.phone().trim());
            }
        }

        // 檢核電子郵件不得重複 (排除自身)
        if (StringUtils.hasText(dto.email())) {
            Long emailCount = employeeMapper.selectCount(
                    new LambdaQueryWrapper<Employee>()
                            .eq(Employee::getEmail, dto.email().trim())
                            .ne(Employee::getEmployeeId, employeeId)
            );
            if (emailCount != null && emailCount > 0) {
                throw new BusinessException(400, "電子郵件已存在: " + dto.email().trim());
            }
        }

        if (StringUtils.hasText(dto.employeeName())) e.setEmployeeName(dto.employeeName());
        if (StringUtils.hasText(dto.phone())) e.setPhone(dto.phone());
        if (StringUtils.hasText(dto.email())) e.setEmail(dto.email());
        if (StringUtils.hasText(dto.status())) e.setStatus(dto.status());
        if (StringUtils.hasText(dto.role())) e.setRole(dto.role());
        if (StringUtils.hasText(dto.password())) {
            e.setPassword(passwordEncoder.encode(dto.password()));
        }
        e.setUpdatedAt(LocalDateTime.now());

        employeeMapper.updateById(e);
        return EmployeeVO.fromEntity(e);
    }

    @Override
    @Transactional
    public void softDelete(String employeeId) {
        Employee e = employeeMapper.selectById(employeeId);
        if (e == null) {
            throw new BusinessException(404, "找不到欲刪除的員工: " + employeeId);
        }
        // 採用在職狀態控制（軟刪除），避免歷史訂單外鍵斷鏈
        e.setStatus("離職");
        e.setUpdatedAt(LocalDateTime.now());
        employeeMapper.updateById(e);
    }

    @Override
    @Transactional
    public void updateStatus(String employeeId, String status) {
        Employee e = employeeMapper.selectById(employeeId);
        if (e == null) {
            throw new BusinessException(404, "找不到員工: " + employeeId);
        }
        e.setStatus(status);
        e.setUpdatedAt(LocalDateTime.now());
        employeeMapper.updateById(e);
    }
}
