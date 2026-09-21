package com.mofei.erp.modules.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mofei.erp.common.BusinessException;
import com.mofei.erp.modules.customer.domain.Customer;
import com.mofei.erp.modules.customer.domain.CustomerDTO;
import com.mofei.erp.modules.customer.domain.CustomerVO;
import com.mofei.erp.modules.customer.mapper.CustomerMapper;
import com.mofei.erp.modules.customer.service.CustomerService;
import com.mofei.erp.modules.order.domain.Orders;
import com.mofei.erp.modules.order.mapper.OrdersMapper;
import com.mofei.erp.modules.user.domain.Employee;
import com.mofei.erp.modules.user.mapper.EmployeeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerMapper customerMapper;
    private final EmployeeMapper employeeMapper;
    private final OrdersMapper ordersMapper;

    public CustomerServiceImpl(CustomerMapper customerMapper, EmployeeMapper employeeMapper, OrdersMapper ordersMapper) {
        this.customerMapper = customerMapper;
        this.employeeMapper = employeeMapper;
        this.ordersMapper = ordersMapper;
    }

    @Override
    public IPage<CustomerVO> queryPage(int current, int size, String keyword, String customerId, String customerName) {
        Page<Customer> page = new Page<>(current, Math.min(size, 50));
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(customerId)) {
            wrapper.like(Customer::getCustomerId, customerId.trim());
        }
        if (StringUtils.hasText(customerName)) {
            wrapper.like(Customer::getCustomerName, customerName.trim());
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Customer::getCustomerId, keyword.trim())
                             .or().like(Customer::getCustomerName, keyword.trim())
                             .or().like(Customer::getPhone, keyword.trim())
                             .or().like(Customer::getCompanyName, keyword.trim())
                             .or().like(Customer::getAddress, keyword.trim()));
        }

        wrapper.orderByAsc(Customer::getCustomerId);
        IPage<Customer> result = customerMapper.selectPage(page, wrapper);

        return result.convert(c -> {
            String empName = "";
            if (StringUtils.hasText(c.getEmployeeId())) {
                Employee emp = employeeMapper.selectById(c.getEmployeeId());
                if (emp != null) empName = emp.getEmployeeName();
            }
            return CustomerVO.fromEntity(c, empName);
        });
    }

    @Override
    public CustomerVO getById(String customerId) {
        Customer c = customerMapper.selectById(customerId);
        if (c == null) {
            throw new BusinessException(404, "找不到客戶編號: " + customerId);
        }
        String empName = "";
        if (StringUtils.hasText(c.getEmployeeId())) {
            Employee emp = employeeMapper.selectById(c.getEmployeeId());
            if (emp != null) empName = emp.getEmployeeName();
        }
        return CustomerVO.fromEntity(c, empName);
    }

    @Override
    @Transactional
    public CustomerVO create(CustomerDTO dto) {
        // 新增客戶編號由系統自動配置，不提供使用者輸入 (依據 yyyyMM 搭配 3 碼流水號依序推進)
        String prefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        Page<Customer> maxPage = new Page<>(1, 1);
        IPage<Customer> result = customerMapper.selectPage(maxPage,
                new LambdaQueryWrapper<Customer>()
                        .select(Customer::getCustomerId)
                        .likeRight(Customer::getCustomerId, prefix)
                        .orderByDesc(Customer::getCustomerId));

        int nextNum = 1;
        if (!result.getRecords().isEmpty()) {
            String maxId = result.getRecords().get(0).getCustomerId();
            try {
                if (maxId != null && maxId.length() >= 9) {
                    String seqPart = maxId.substring(6, 9);
                    nextNum = Integer.parseInt(seqPart) + 1;
                }
            } catch (Exception ex) {
                Long count = customerMapper.selectCount(new LambdaQueryWrapper<Customer>().likeRight(Customer::getCustomerId, prefix));
                nextNum = (count != null ? count.intValue() : 0) + 1;
            }
        }

        String id = String.format("%s%03d", prefix, nextNum);
        while (customerMapper.selectById(id) != null) {
            nextNum++;
            id = String.format("%s%03d", prefix, nextNum);
        }

        // 檢核客戶電話不得重複
        if (StringUtils.hasText(dto.phone())) {
            Long phoneCount = customerMapper.selectCount(
                    new LambdaQueryWrapper<Customer>().eq(Customer::getPhone, dto.phone().trim())
            );
            if (phoneCount != null && phoneCount > 0) {
                throw new BusinessException(400, "客戶電話已存在: " + dto.phone().trim());
            }
        }

        // 檢核統一編號不得重複 (若有填寫)
        if (StringUtils.hasText(dto.taxId())) {
            Long taxIdCount = customerMapper.selectCount(
                    new LambdaQueryWrapper<Customer>().eq(Customer::getTaxId, dto.taxId().trim())
            );
            if (taxIdCount != null && taxIdCount > 0) {
                throw new BusinessException(400, "客戶統一編號已存在: " + dto.taxId().trim());
            }
        }

        // 檢核電子郵件不得重複 (若有填寫)
        if (StringUtils.hasText(dto.email())) {
            Long emailCount = customerMapper.selectCount(
                    new LambdaQueryWrapper<Customer>().eq(Customer::getEmail, dto.email().trim())
            );
            if (emailCount != null && emailCount > 0) {
                throw new BusinessException(400, "客戶電子郵件已存在: " + dto.email().trim());
            }
        }

        // 檢核負責員工是否存在
        if (!StringUtils.hasText(dto.employeeId()) || employeeMapper.selectById(dto.employeeId().trim()) == null) {
            throw new BusinessException(400, "負責員工編號不存在: " + dto.employeeId());
        }

        Customer c = new Customer();
        c.setCustomerId(id);
        c.setCustomerName(dto.customerName() != null ? dto.customerName().trim() : null);
        c.setPhone(dto.phone() != null ? dto.phone().trim() : null);
        c.setEmail(StringUtils.hasText(dto.email()) ? dto.email().trim() : null);
        c.setCompanyName(StringUtils.hasText(dto.companyName()) ? dto.companyName().trim() : null);
        c.setTaxId(StringUtils.hasText(dto.taxId()) ? dto.taxId().trim() : null);
        c.setCountry(StringUtils.hasText(dto.country()) ? dto.country().trim() : "台灣");
        c.setCity(dto.city() != null ? dto.city().trim() : null);
        c.setDistrict(dto.district() != null ? dto.district().trim() : null);
        c.setAddress(dto.address() != null ? dto.address().trim() : null);
        c.setEmployeeId(dto.employeeId().trim());
        c.setCreatedAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());

        customerMapper.insert(c);
        return getById(id);
    }

    @Override
    @Transactional
    public CustomerVO update(String customerId, CustomerDTO dto) {
        Customer c = customerMapper.selectById(customerId);
        if (c == null) {
            throw new BusinessException(404, "找不到欲修改的客戶: " + customerId);
        }

        // 檢核客戶電話不得重複 (排除自身)
        if (StringUtils.hasText(dto.phone())) {
            Long phoneCount = customerMapper.selectCount(
                    new LambdaQueryWrapper<Customer>()
                            .eq(Customer::getPhone, dto.phone().trim())
                            .ne(Customer::getCustomerId, customerId)
            );
            if (phoneCount != null && phoneCount > 0) {
                throw new BusinessException(400, "客戶電話已存在: " + dto.phone().trim());
            }
            c.setPhone(dto.phone().trim());
        }

        // 檢核統一編號不得重複 (若有填寫，排除自身)
        if (StringUtils.hasText(dto.taxId())) {
            Long taxIdCount = customerMapper.selectCount(
                    new LambdaQueryWrapper<Customer>()
                            .eq(Customer::getTaxId, dto.taxId().trim())
                            .ne(Customer::getCustomerId, customerId)
            );
            if (taxIdCount != null && taxIdCount > 0) {
                throw new BusinessException(400, "客戶統一編號已存在: " + dto.taxId().trim());
            }
            c.setTaxId(dto.taxId().trim());
        } else if (dto.taxId() != null) {
            c.setTaxId(null);
        }

        // 檢核電子郵件不得重複 (若有填寫，排除自身)
        if (StringUtils.hasText(dto.email())) {
            Long emailCount = customerMapper.selectCount(
                    new LambdaQueryWrapper<Customer>()
                            .eq(Customer::getEmail, dto.email().trim())
                            .ne(Customer::getCustomerId, customerId)
            );
            if (emailCount != null && emailCount > 0) {
                throw new BusinessException(400, "客戶電子郵件已存在: " + dto.email().trim());
            }
            c.setEmail(dto.email().trim());
        } else if (dto.email() != null) {
            c.setEmail(null);
        }

        if (StringUtils.hasText(dto.customerName())) c.setCustomerName(dto.customerName().trim());
        if (dto.companyName() != null) c.setCompanyName(StringUtils.hasText(dto.companyName()) ? dto.companyName().trim() : null);
        if (StringUtils.hasText(dto.country())) c.setCountry(dto.country().trim());
        if (StringUtils.hasText(dto.city())) c.setCity(dto.city().trim());
        if (StringUtils.hasText(dto.district())) c.setDistrict(dto.district().trim());
        if (StringUtils.hasText(dto.address())) c.setAddress(dto.address().trim());
        if (StringUtils.hasText(dto.employeeId())) {
            if (employeeMapper.selectById(dto.employeeId().trim()) == null) {
                throw new BusinessException(400, "負責員工編號不存在: " + dto.employeeId());
            }
            c.setEmployeeId(dto.employeeId().trim());
        }
        c.setUpdatedAt(LocalDateTime.now());

        customerMapper.updateById(c);
        return getById(customerId);
    }

    @Override
    @Transactional
    public void delete(String customerId) {
        Customer c = customerMapper.selectById(customerId);
        if (c == null) {
            throw new BusinessException(404, "找不到欲刪除的客戶: " + customerId);
        }

        // 檢查是否有歷史訂單關聯
        Long orderCount = ordersMapper.selectCount(new LambdaQueryWrapper<Orders>().eq(Orders::getCustomerId, customerId));
        if (orderCount > 0) {
            throw new BusinessException(400, "該客戶已有關聯之歷史訂單 (" + orderCount + " 筆)，禁止刪除");
        }

        customerMapper.deleteById(customerId);
    }
}
