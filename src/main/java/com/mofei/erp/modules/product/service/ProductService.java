package com.mofei.erp.modules.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mofei.erp.modules.product.domain.ProductDTO;
import com.mofei.erp.modules.product.domain.ProductVO;

public interface ProductService {
    IPage<ProductVO> queryPage(int current, int size, String keyword, String productId, String productName);
    ProductVO getById(String productId);
    ProductVO create(ProductDTO dto);
    ProductVO update(String productId, ProductDTO dto);
    void delete(String productId);
}
