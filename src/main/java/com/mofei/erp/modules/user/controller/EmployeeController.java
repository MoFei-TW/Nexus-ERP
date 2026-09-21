package com.mofei.erp.modules.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mofei.erp.common.R;
import com.mofei.erp.modules.user.domain.EmployeeDTO;
import com.mofei.erp.modules.user.domain.EmployeeVO;
import com.mofei.erp.modules.user.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/page")
    public R<IPage<EmployeeVO>> queryPage(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String employeeName) {
        IPage<EmployeeVO> page = employeeService.queryPage(current, size, keyword, employeeId, employeeName);
        return R.ok(page);
    }

    @GetMapping("/{id}")
    public R<EmployeeVO> getById(@PathVariable String id) {
        return R.ok(employeeService.getById(id));
    }

    @PostMapping
    public R<EmployeeVO> create(@Valid @RequestBody EmployeeDTO dto) {
        return R.ok("新增員工成功", employeeService.create(dto));
    }

    @PutMapping("/{id}")
    public R<EmployeeVO> update(@PathVariable String id, @Valid @RequestBody EmployeeDTO dto) {
        return R.ok("修改員工成功", employeeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable String id) {
        employeeService.softDelete(id);
        return R.ok("員工已標記為離職狀態", null);
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable String id, @RequestParam String status) {
        employeeService.updateStatus(id, status);
        return R.ok("員工狀態更新成功", null);
    }
}
