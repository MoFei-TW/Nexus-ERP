package com.mofei.erp.modules.portal;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mofei.erp.common.R;
import com.mofei.erp.modules.customer.mapper.CustomerMapper;
import com.mofei.erp.modules.order.domain.Orders;
import com.mofei.erp.modules.order.mapper.OrdersMapper;
import com.mofei.erp.modules.product.domain.Product;
import com.mofei.erp.modules.product.mapper.ProductMapper;
import com.mofei.erp.modules.user.domain.Employee;
import com.mofei.erp.modules.user.mapper.EmployeeMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final EmployeeMapper employeeMapper;
    private final ProductMapper productMapper;
    private final CustomerMapper customerMapper;
    private final OrdersMapper ordersMapper;

    public record DashboardStatsVO(
        long totalEmployees,
        long activeEmployees,
        long totalProducts,
        long lowStockProducts,
        long totalCustomers,
        long totalOrders,
        long pendingOrders,
        long completedOrders,
        BigDecimal totalRevenue
    ) {}

    public DashboardController(EmployeeMapper employeeMapper,
                               ProductMapper productMapper,
                               CustomerMapper customerMapper,
                               OrdersMapper ordersMapper) {
        this.employeeMapper = employeeMapper;
        this.productMapper = productMapper;
        this.customerMapper = customerMapper;
        this.ordersMapper = ordersMapper;
    }

    @GetMapping("/stats")
    public R<DashboardStatsVO> getStats() {
        // 員工統計
        long totalEmployees = employeeMapper.selectCount(null);
        long activeEmployees = employeeMapper.selectCount(
            new LambdaQueryWrapper<Employee>().eq(Employee::getStatus, "在職")
        );

        // 產品統計
        long totalProducts = productMapper.selectCount(null);
        long lowStockProducts = productMapper.selectCount(
            new LambdaQueryWrapper<Product>().le(Product::getStock, 20)
        );

        // 客戶統計
        long totalCustomers = customerMapper.selectCount(null);

        // 訂單統計
        long totalOrders = ordersMapper.selectCount(null);
        long pendingOrders = ordersMapper.selectCount(
            new LambdaQueryWrapper<Orders>().eq(Orders::getStatus, "處理中")
        );
        long completedOrders = ordersMapper.selectCount(
            new LambdaQueryWrapper<Orders>().eq(Orders::getStatus, "已完成")
        );

        // 總營業額
        List<Orders> allOrders = ordersMapper.selectList(
            new LambdaQueryWrapper<Orders>().ne(Orders::getStatus, "已取消")
        );
        BigDecimal totalRevenue = allOrders.stream()
            .map(Orders::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        DashboardStatsVO stats = new DashboardStatsVO(
            totalEmployees,
            activeEmployees,
            totalProducts,
            lowStockProducts,
            totalCustomers,
            totalOrders,
            pendingOrders,
            completedOrders,
            totalRevenue
        );

        return R.ok(stats);
    }
}
