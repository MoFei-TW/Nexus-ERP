package com.mofei.erp.modules.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mofei.erp.common.R;
import com.mofei.erp.modules.product.domain.ProductDTO;
import com.mofei.erp.modules.product.domain.ProductVO;
import com.mofei.erp.modules.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/page")
    public R<IPage<ProductVO>> queryPage(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) String productName) {
        IPage<ProductVO> page = productService.queryPage(current, size, keyword, productId, productName);
        return R.ok(page);
    }

    @GetMapping("/{id}")
    public R<ProductVO> getById(@PathVariable String id) {
        return R.ok(productService.getById(id));
    }

    @PostMapping
    public R<ProductVO> create(@Valid @RequestBody ProductDTO dto) {
        return R.ok("新增產品成功", productService.create(dto));
    }

    @PutMapping("/{id}")
    public R<ProductVO> update(@PathVariable String id, @Valid @RequestBody ProductDTO dto) {
        return R.ok("修改產品成功", productService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable String id) {
        productService.delete(id);
        return R.ok("產品已下架", null);
    }
}
