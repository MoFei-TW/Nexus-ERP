package com.mofei.erp.modules.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mofei.erp.common.BusinessException;
import com.mofei.erp.modules.product.domain.Product;
import com.mofei.erp.modules.product.domain.ProductDTO;
import com.mofei.erp.modules.product.domain.ProductVO;
import com.mofei.erp.modules.product.mapper.ProductMapper;
import com.mofei.erp.modules.product.service.ProductService;
import com.mofei.erp.modules.user.domain.Employee;
import com.mofei.erp.modules.user.mapper.EmployeeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final EmployeeMapper employeeMapper;

    public ProductServiceImpl(ProductMapper productMapper, EmployeeMapper employeeMapper) {
        this.productMapper = productMapper;
        this.employeeMapper = employeeMapper;
    }

    @Override
    public IPage<ProductVO> queryPage(int current, int size, String keyword, String productId, String productName) {
        Page<Product> page = new Page<>(current, Math.min(size, 50));
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(productId)) {
            wrapper.like(Product::getProductId, productId.trim());
        }
        if (StringUtils.hasText(productName)) {
            wrapper.like(Product::getProductName, productName.trim());
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Product::getProductId, keyword.trim())
                             .or().like(Product::getProductName, keyword.trim())
                             .or().like(Product::getAttributes, keyword.trim()));
        }

        wrapper.orderByAsc(Product::getProductId);
        IPage<Product> result = productMapper.selectPage(page, wrapper);

        return result.convert(p -> {
            String empName = "";
            if (StringUtils.hasText(p.getEmployeeId())) {
                Employee emp = employeeMapper.selectById(p.getEmployeeId());
                if (emp != null) empName = emp.getEmployeeName();
            }
            return ProductVO.fromEntity(p, empName);
        });
    }

    @Override
    public ProductVO getById(String productId) {
        Product p = productMapper.selectById(productId);
        if (p == null) {
            throw new BusinessException(404, "找不到產品編號: " + productId);
        }
        String empName = "";
        if (StringUtils.hasText(p.getEmployeeId())) {
            Employee emp = employeeMapper.selectById(p.getEmployeeId());
            if (emp != null) empName = emp.getEmployeeName();
        }
        return ProductVO.fromEntity(p, empName);
    }

    @Override
    @Transactional
    public ProductVO create(ProductDTO dto) {
        // 新增產品編號由系統自動配置，不提供使用者輸入 (依序由 202601A 往後遞增)
        Page<Product> maxPage = new Page<>(1, 1);
        IPage<Product> result = productMapper.selectPage(maxPage,
                new LambdaQueryWrapper<Product>()
                        .select(Product::getProductId)
                        .likeRight(Product::getProductId, "2026")
                        .orderByDesc(Product::getProductId));

        int nextNum = 1;
        if (!result.getRecords().isEmpty()) {
            String maxId = result.getRecords().get(0).getProductId();
            try {
                if (maxId != null && maxId.length() >= 6) {
                    String numPart = maxId.substring(4, 6);
                    nextNum = Integer.parseInt(numPart) + 1;
                }
            } catch (Exception ex) {
                Long count = productMapper.selectCount(new LambdaQueryWrapper<Product>().likeRight(Product::getProductId, "2026"));
                nextNum = (count != null ? count.intValue() : 0) + 1;
            }
        }

        char suffix = (char) ('A' + (nextNum - 1) % 26);
        String id = String.format("2026%02d%c", nextNum, suffix);

        while (productMapper.selectById(id) != null) {
            nextNum++;
            suffix = (char) ('A' + (nextNum - 1) % 26);
            id = String.format("2026%02d%c", nextNum, suffix);
        }

        // 驗證產品名稱是否重複
        if (StringUtils.hasText(dto.productName())) {
            Long count = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                    .eq(Product::getProductName, dto.productName().trim()));
            if (count != null && count > 0) {
                throw new BusinessException(400, "產品名稱已存在: " + dto.productName().trim());
            }
        }

        // 驗證負責員工是否存在
        if (!StringUtils.hasText(dto.employeeId()) || employeeMapper.selectById(dto.employeeId().trim()) == null) {
            throw new BusinessException(400, "負責員工編號不存在: " + dto.employeeId());
        }

        Product p = new Product();
        p.setProductId(id);
        p.setProductName(dto.productName().trim());
        p.setAttributes(dto.attributes());
        p.setPrice(dto.price());
        p.setStock(dto.stock() != null ? dto.stock() : 26);
        p.setEmployeeId(dto.employeeId().trim());
        p.setStatus(StringUtils.hasText(dto.status()) ? dto.status() : "正常販售");
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());

        productMapper.insert(p);
        return getById(id);
    }

    @Override
    @Transactional
    public ProductVO update(String productId, ProductDTO dto) {
        Product p = productMapper.selectById(productId);
        if (p == null) {
            throw new BusinessException(404, "找不到欲修改的產品: " + productId);
        }

        if (StringUtils.hasText(dto.productName())) {
            Long count = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                    .eq(Product::getProductName, dto.productName().trim())
                    .ne(Product::getProductId, productId));
            if (count != null && count > 0) {
                throw new BusinessException(400, "產品名稱已存在: " + dto.productName().trim());
            }
            p.setProductName(dto.productName().trim());
        }
        if (dto.attributes() != null) p.setAttributes(dto.attributes());
        if (dto.price() != null) p.setPrice(dto.price());
        if (dto.stock() != null) p.setStock(dto.stock());
        if (StringUtils.hasText(dto.employeeId())) {
            if (employeeMapper.selectById(dto.employeeId().trim()) == null) {
                throw new BusinessException(400, "負責員工編號不存在: " + dto.employeeId());
            }
            p.setEmployeeId(dto.employeeId().trim());
        }
        if (StringUtils.hasText(dto.status())) p.setStatus(dto.status());
        p.setUpdatedAt(LocalDateTime.now());

        productMapper.updateById(p);
        return getById(productId);
    }

    @Override
    @Transactional
    public void delete(String productId) {
        Product p = productMapper.selectById(productId);
        if (p == null) {
            throw new BusinessException(404, "找不到欲刪除的產品: " + productId);
        }
        // 優先將狀態標記為下架停售，保護歷史訂單
        p.setStatus("下架停售");
        p.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(p);
    }
}
