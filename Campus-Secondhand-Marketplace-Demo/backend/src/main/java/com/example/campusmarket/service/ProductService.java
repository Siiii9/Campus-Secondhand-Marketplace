package com.example.campusmarket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusmarket.entity.Product;
import com.example.campusmarket.entity.ProductImage;
import com.example.campusmarket.mapper.ProductImageMapper;
import com.example.campusmarket.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService extends ServiceImpl<ProductMapper, Product> {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductImageMapper productImageMapper;

    @Transactional
    public boolean addProduct(Product product, List<String> images) {
        product.setStatus(0);
        product.setAuditStatus(0);
        product.setSalesCount(0);
        product.setViewCount(0);
        product.setCreatedAt(LocalDateTime.now());
        int result = productMapper.insert(product);
        
        if (result > 0 && images != null && !images.isEmpty()) {
            int sortOrder = 0;
            for (String imageUrl : images) {
                ProductImage productImage = new ProductImage();
                productImage.setProductId(product.getId());
                productImage.setImageUrl(imageUrl);
                productImage.setSortOrder(sortOrder++);
                productImageMapper.insert(productImage);
            }
        }
        return result > 0;
    }

    public Page<Product> searchProducts(String keyword, String sortBy, int page, int size) {
        Page<Product> pageInfo = new Page<>(page, size);
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("name", keyword);
        }
        wrapper.eq("status", 1);
        
        if ("price".equals(sortBy)) {
            wrapper.orderByAsc("discount_price");
        } else if ("sales".equals(sortBy)) {
            wrapper.orderByDesc("sales_count");
        } else if ("rating".equals(sortBy)) {
            wrapper.orderByDesc("avg_rating");
        } else {
            wrapper.orderByDesc("created_at");
        }
        
        return productMapper.selectPage(pageInfo, wrapper);
    }

    public boolean updateProduct(Product product) {
        return productMapper.updateById(product) > 0;
    }

    public boolean auditProduct(Long productId, Integer auditStatus, Long auditorId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            return false;
        }
        product.setAuditStatus(auditStatus);
        product.setStatus(auditStatus == 1 ? 1 : 2);
        product.setAuditTime(LocalDateTime.now());
        return productMapper.updateById(product) > 0;
    }

    public List<Product> getProductsByMerchant(Long merchantId) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("merchant_id", merchantId);
        return productMapper.selectList(wrapper);
    }

    public Product getProductById(Long id) {
        return productMapper.selectById(id);
    }

    public boolean updateStock(Long productId, Integer quantity) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStock() < quantity) {
            return false;
        }
        product.setStock(product.getStock() - quantity);
        return productMapper.updateById(product) > 0;
    }

    public boolean incrementSales(Long productId, Integer quantity) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            return false;
        }
        product.setSalesCount(product.getSalesCount() + quantity);
        return productMapper.updateById(product) > 0;
    }
}