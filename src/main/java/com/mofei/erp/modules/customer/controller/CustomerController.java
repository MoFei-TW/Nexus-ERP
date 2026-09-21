package com.mofei.erp.modules.customer.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mofei.erp.common.R;
import com.mofei.erp.modules.customer.domain.CustomerDTO;
import com.mofei.erp.modules.customer.domain.CustomerVO;
import com.mofei.erp.modules.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/page")
    public R<IPage<CustomerVO>> queryPage(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String customerName) {
        IPage<CustomerVO> page = customerService.queryPage(current, size, keyword, customerId, customerName);
        return R.ok(page);
    }

    @GetMapping("/{id}")
    public R<CustomerVO> getById(@PathVariable String id) {
        return R.ok(customerService.getById(id));
    }

    @PostMapping
    public R<CustomerVO> create(@Valid @RequestBody CustomerDTO dto) {
        return R.ok("新增客戶成功", customerService.create(dto));
    }

    @PutMapping("/{id}")
    public R<CustomerVO> update(@PathVariable String id, @Valid @RequestBody CustomerDTO dto) {
        return R.ok("修改客戶成功", customerService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable String id) {
        customerService.delete(id);
        return R.ok("刪除客戶成功", null);
    }
}
