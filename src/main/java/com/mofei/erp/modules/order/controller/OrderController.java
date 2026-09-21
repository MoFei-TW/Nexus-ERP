package com.mofei.erp.modules.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mofei.erp.common.R;
import com.mofei.erp.modules.order.domain.OrderDTO;
import com.mofei.erp.modules.order.domain.OrderVO;
import com.mofei.erp.modules.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/page")
    public R<IPage<OrderVO>> queryPage(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String status) {
        IPage<OrderVO> page = orderService.queryPage(current, size, keyword, orderId, status);
        return R.ok(page);
    }

    @GetMapping("/{id}")
    public R<OrderVO> getById(@PathVariable String id) {
        return R.ok(orderService.getById(id));
    }

    @PostMapping
    public R<OrderVO> create(@Valid @RequestBody OrderDTO dto) {
        return R.ok("新增訂單成功", orderService.create(dto));
    }

    @PutMapping("/{id}")
    public R<OrderVO> update(@PathVariable String id, @Valid @RequestBody OrderDTO dto) {
        return R.ok("修改訂單成功", orderService.update(id, dto));
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable String id, @RequestParam String status) {
        orderService.updateStatus(id, status);
        return R.ok("訂單狀態更新成功", null);
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable String id) {
        orderService.delete(id);
        return R.ok("訂單已取消", null);
    }
}
