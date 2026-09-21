package com.mofei.erp.modules.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mofei.erp.common.BusinessException;
import com.mofei.erp.modules.customer.domain.Customer;
import com.mofei.erp.modules.customer.mapper.CustomerMapper;
import com.mofei.erp.modules.order.domain.*;
import com.mofei.erp.modules.order.mapper.OrderDetailMapper;
import com.mofei.erp.modules.order.mapper.OrdersMapper;
import com.mofei.erp.modules.order.service.OrderService;
import com.mofei.erp.modules.product.domain.Product;
import com.mofei.erp.modules.product.mapper.ProductMapper;
import com.mofei.erp.modules.user.domain.Employee;
import com.mofei.erp.modules.user.mapper.EmployeeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrdersMapper ordersMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final CustomerMapper customerMapper;
    private final EmployeeMapper employeeMapper;
    private final ProductMapper productMapper;

    public OrderServiceImpl(OrdersMapper ordersMapper,
                            OrderDetailMapper orderDetailMapper,
                            CustomerMapper customerMapper,
                            EmployeeMapper employeeMapper,
                            ProductMapper productMapper) {
        this.ordersMapper = ordersMapper;
        this.orderDetailMapper = orderDetailMapper;
        this.customerMapper = customerMapper;
        this.employeeMapper = employeeMapper;
        this.productMapper = productMapper;
    }

    @Override
    public IPage<OrderVO> queryPage(int current, int size, String keyword, String orderId, String status) {
        Page<Orders> page = new Page<>(current, Math.min(size, 50));
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(orderId)) {
            wrapper.like(Orders::getOrderId, orderId.trim());
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Orders::getStatus, status.trim());
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Orders::getOrderId, keyword.trim())
                             .or().like(Orders::getRecipientName, keyword.trim())
                             .or().like(Orders::getRecipientPhone, keyword.trim())
                             .or().like(Orders::getCustomerId, keyword.trim()));
        }

        wrapper.orderByAsc(Orders::getOrderId);
        IPage<Orders> result = ordersMapper.selectPage(page, wrapper);

        return result.convert(this::assembleOrderVO);
    }

    @Override
    public OrderVO getById(String orderId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(404, "找不到訂單編號: " + orderId);
        }
        return assembleOrderVO(order);
    }

    @Override
    @Transactional
    public OrderVO create(OrderDTO dto) {
        String orderId = dto.orderId();
        if (!StringUtils.hasText(orderId)) {
            String prefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            long count = ordersMapper.selectCount(null);
            orderId = String.format("%s-%04d", prefix, count + 1);
        }

        if (ordersMapper.selectById(orderId) != null) {
            throw new BusinessException(400, "訂單編號已存在: " + orderId);
        }

        // 驗證客戶與負責員工
        Customer customer = customerMapper.selectById(dto.customerId());
        if (customer == null) {
            throw new BusinessException(400, "客戶編號不存在: " + dto.customerId());
        }
        if (employeeMapper.selectById(dto.employeeId()) == null) {
            throw new BusinessException(400, "業務員工編號不存在: " + dto.employeeId());
        }

        // 計算明細與訂單總金額
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderDetail> detailEntities = new ArrayList<>();

        for (OrderDetailDTO d : dto.details()) {
            Product product = productMapper.selectById(d.productId());
            if (product == null) {
                throw new BusinessException(400, "產品編號不存在: " + d.productId());
            }

            BigDecimal unitPrice = d.unitPrice() != null ? d.unitPrice() : product.getPrice();
            BigDecimal discount = d.discountAmount() != null ? d.discountAmount() : BigDecimal.ZERO;
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(d.quantity())).subtract(discount);
            if (subtotal.compareTo(BigDecimal.ZERO) < 0) subtotal = BigDecimal.ZERO;

            totalAmount = totalAmount.add(subtotal);

            OrderDetail detail = new OrderDetail();
            detail.setOrderId(orderId);
            detail.setProductId(product.getProductId());
            detail.setProductNameSnapshot(product.getProductName()); // 歷史快照
            detail.setUnitPrice(unitPrice);                         // 歷史快照
            detail.setQuantity(d.quantity());
            detail.setDiscountAmount(discount);
            detail.setSubtotal(subtotal);
            detail.setCreatedAt(LocalDateTime.now());
            detailEntities.add(detail);
        }

        Orders order = new Orders();
        order.setOrderId(orderId);
        order.setCustomerId(dto.customerId());
        order.setEmployeeId(dto.employeeId());
        order.setTotalAmount(totalAmount);
        order.setStatus(StringUtils.hasText(dto.status()) ? dto.status() : "處理中");
        order.setRecipientName(dto.recipientName());
        order.setRecipientPhone(dto.recipientPhone());
        order.setRecipientAddress(dto.recipientAddress());
        order.setCreatedAt(LocalDateTime.now());

        ordersMapper.insert(order);

        for (OrderDetail detail : detailEntities) {
            orderDetailMapper.insert(detail);
        }

        return getById(orderId);
    }

    @Override
    @Transactional
    public OrderVO update(String orderId, OrderDTO dto) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(404, "找不到欲修改的訂單: " + orderId);
        }

        if (StringUtils.hasText(dto.status())) order.setStatus(dto.status());
        if (StringUtils.hasText(dto.customerId())) {
            Customer customer = customerMapper.selectById(dto.customerId());
            if (customer == null) {
                throw new BusinessException(400, "客戶編號不存在: " + dto.customerId());
            }
            order.setCustomerId(dto.customerId());
        }
        if (StringUtils.hasText(dto.employeeId())) {
            if (employeeMapper.selectById(dto.employeeId()) == null) {
                throw new BusinessException(400, "業務員工編號不存在: " + dto.employeeId());
            }
            order.setEmployeeId(dto.employeeId());
        }
        if (StringUtils.hasText(dto.recipientName())) order.setRecipientName(dto.recipientName());
        if (StringUtils.hasText(dto.recipientPhone())) order.setRecipientPhone(dto.recipientPhone());
        if (StringUtils.hasText(dto.recipientAddress())) order.setRecipientAddress(dto.recipientAddress());

        if (dto.details() != null && !dto.details().isEmpty()) {
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<OrderDetail> detailEntities = new ArrayList<>();

            for (OrderDetailDTO d : dto.details()) {
                Product product = productMapper.selectById(d.productId());
                if (product == null) {
                    throw new BusinessException(400, "產品編號不存在: " + d.productId());
                }

                BigDecimal unitPrice = d.unitPrice() != null ? d.unitPrice() : product.getPrice();
                BigDecimal discount = d.discountAmount() != null ? d.discountAmount() : BigDecimal.ZERO;
                BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(d.quantity())).subtract(discount);
                if (subtotal.compareTo(BigDecimal.ZERO) < 0) subtotal = BigDecimal.ZERO;

                totalAmount = totalAmount.add(subtotal);

                OrderDetail detail = new OrderDetail();
                detail.setOrderId(orderId);
                detail.setProductId(product.getProductId());
                detail.setProductNameSnapshot(product.getProductName());
                detail.setUnitPrice(unitPrice);
                detail.setQuantity(d.quantity());
                detail.setDiscountAmount(discount);
                detail.setSubtotal(subtotal);
                detail.setCreatedAt(LocalDateTime.now());
                detailEntities.add(detail);
            }

            orderDetailMapper.delete(new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, orderId));
            for (OrderDetail detail : detailEntities) {
                orderDetailMapper.insert(detail);
            }
            order.setTotalAmount(totalAmount);
        }

        ordersMapper.updateById(order);
        return getById(orderId);
    }

    @Override
    @Transactional
    public void updateStatus(String orderId, String status) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(404, "找不到訂單: " + orderId);
        }
        order.setStatus(status);
        ordersMapper.updateById(order);
    }

    @Override
    @Transactional
    public void delete(String orderId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(404, "找不到欲刪除的訂單: " + orderId);
        }
        // 將狀態改為已取消
        order.setStatus("已取消");
        ordersMapper.updateById(order);
    }

    private OrderVO assembleOrderVO(Orders o) {
        String custName = "";
        Customer c = customerMapper.selectById(o.getCustomerId());
        if (c != null) custName = c.getCustomerName();

        String empName = "";
        Employee e = employeeMapper.selectById(o.getEmployeeId());
        if (e != null) empName = e.getEmployeeName();

        List<OrderDetail> details = orderDetailMapper.selectList(
            new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, o.getOrderId())
        );
        List<OrderDetailVO> detailVOs = details.stream().map(OrderDetailVO::fromEntity).toList();

        return OrderVO.fromEntity(o, custName, empName, detailVOs);
    }
}
