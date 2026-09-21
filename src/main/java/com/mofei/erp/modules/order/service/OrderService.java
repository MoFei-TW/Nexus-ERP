package com.mofei.erp.modules.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mofei.erp.modules.order.domain.OrderDTO;
import com.mofei.erp.modules.order.domain.OrderVO;

public interface OrderService {
    IPage<OrderVO> queryPage(int current, int size, String keyword, String orderId, String status);
    OrderVO getById(String orderId);
    OrderVO create(OrderDTO dto);
    OrderVO update(String orderId, OrderDTO dto);
    void updateStatus(String orderId, String status);
    void delete(String orderId);
}
