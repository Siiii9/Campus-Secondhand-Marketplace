package com.example.campusmarket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusmarket.entity.Product;
import com.example.campusmarket.entity.ProductImage;
import com.example.campusmarket.entity.Review;
import com.example.campusmarket.mapper.ProductImageMapper;
import com.example.campusmarket.mapper.ProductMapper;
import com.example.campusmarket.mapper.ReviewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService extends ServiceImpl<ProductMapper, Product> {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductImageMapper productImageMapper;

    @Autowired
    private ReviewMapper reviewMapper;

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

    public Page<Map<String, Object>> searchProducts(String keyword, String sortBy, int page, int size, Integer auditStatus) {
        Page<Product> pageInfo = new Page<>(page, size);
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("name", keyword);
        }
        
        if (auditStatus != null) {
            wrapper.eq("audit_status", auditStatus);
        } else {
            wrapper.eq("status", 1);
        }
        
        if ("price".equals(sortBy)) {
            wrapper.orderByAsc("discount_price");
        } else if ("sales".equals(sortBy)) {
            wrapper.orderByDesc("sales_count");
        } else if ("rating".equals(sortBy)) {
            wrapper.orderByDesc("avg_rating");
        } else {
            wrapper.orderByDesc("created_at");
        }
        
        Page<Product> result = productMapper.selectPage(pageInfo, wrapper);
        
        Page<Map<String, Object>> resultPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<Map<String, Object>> records = result.getRecords().stream().map(product -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", product.getId());
            map.put("name", product.getName());
            map.put("originalPrice", product.getOriginalPrice());
            map.put("discountPrice", product.getDiscountPrice());
            map.put("stock", product.getStock());
            map.put("salesCount", product.getSalesCount());
            map.put("status", product.getStatus());
            map.put("auditStatus", product.getAuditStatus());
            map.put("merchantId", product.getMerchantId());
            
            QueryWrapper<ProductImage> imageWrapper = new QueryWrapper<>();
            imageWrapper.eq("product_id", product.getId());
            imageWrapper.orderByAsc("sort_order");
            List<ProductImage> images = productImageMapper.selectList(imageWrapper);
            List<String> imageUrls = images.stream().map(ProductImage::getImageUrl).toList();
            map.put("images", imageUrls);
            
            return map;
        }).toList();
        
        resultPage.setRecords(records);
        return resultPage;
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

    public List<Map<String, Object>> getProductsByMerchant(Long merchantId) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("merchant_id", merchantId);
        List<Product> products = productMapper.selectList(wrapper);
        
        return products.stream().map(product -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", product.getId());
            map.put("name", product.getName());
            map.put("originalPrice", product.getOriginalPrice());
            map.put("discountPrice", product.getDiscountPrice());
            map.put("stock", product.getStock());
            map.put("salesCount", product.getSalesCount());
            map.put("status", product.getStatus());
            map.put("auditStatus", product.getAuditStatus());
            map.put("conditionLevel", product.getConditionLevel());
            
            QueryWrapper<ProductImage> imageWrapper = new QueryWrapper<>();
            imageWrapper.eq("product_id", product.getId());
            imageWrapper.orderByAsc("sort_order");
            List<ProductImage> images = productImageMapper.selectList(imageWrapper);
            List<String> imageUrls = images.stream().map(ProductImage::getImageUrl).toList();
            map.put("images", imageUrls);
            
            return map;
        }).toList();
    }

    public Product getProductById(Long id) {
        return productMapper.selectById(id);
    }

    public boolean deleteProduct(Long id) {
        QueryWrapper<ProductImage> imageWrapper = new QueryWrapper<>();
        imageWrapper.eq("product_id", id);
        productImageMapper.delete(imageWrapper);
        return productMapper.deleteById(id) > 0;
    }

    public Page<Product> getShopProducts(Long merchantId, Integer status, int page, int size) {
        Page<Product> pageInfo = new Page<>(page, size);
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("merchant_id", merchantId);
        
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        wrapper.orderByDesc("created_at");
        return productMapper.selectPage(pageInfo, wrapper);
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

    public Map<String, Object> getProductDetail(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            return null;
        }

        Map<String, Object> detail = new HashMap<>();
        detail.put("product", product);

        QueryWrapper<ProductImage> imageWrapper = new QueryWrapper<>();
        imageWrapper.eq("product_id", productId);
        imageWrapper.orderByAsc("sort_order");
        List<ProductImage> images = productImageMapper.selectList(imageWrapper);
        List<String> imageUrls = images.stream().map(ProductImage::getImageUrl).toList();
        detail.put("images", imageUrls);

        QueryWrapper<Review> reviewWrapper = new QueryWrapper<>();
        reviewWrapper.eq("product_id", productId);
        reviewWrapper.orderByDesc("created_at");
        List<Review> reviews = reviewMapper.selectList(reviewWrapper);
        detail.put("reviews", reviews);

        return detail;
    }
}