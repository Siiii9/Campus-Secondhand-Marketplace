package com.example.campusmarket.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusmarket.dto.ApiResponse;
import com.example.campusmarket.dto.ProductRequest;
import com.example.campusmarket.entity.Product;
import com.example.campusmarket.entity.User;
import com.example.campusmarket.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ApiResponse<?> addProduct(@RequestBody ProductRequest request, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        if (!"MERCHANT".equals(user.getRole())) {
            return ApiResponse.error("只有商家可以发布商品");
        }

        Product product = new Product();
        product.setMerchantId(user.getId());
        product.setName(request.getName());
        product.setCategoryId(request.getCategoryId());
        product.setDescription(request.getDescription());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setStock(request.getStock());
        product.setUnit(request.getUnit());
        product.setIsNegotiable(request.getIsNegotiable());
        product.setConditionLevel(request.getConditionLevel());

        boolean result = productService.addProduct(product, request.getImages());
        if (result) {
            return ApiResponse.success("发布成功，等待审核");
        }
        return ApiResponse.error("发布失败");
    }

    @GetMapping("/search")
    public ApiResponse<?> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "created") String sortBy,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Product> products = productService.searchProducts(keyword, sortBy, page, size);
        return ApiResponse.success(products);
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ApiResponse.error("商品不存在");
        }
        return ApiResponse.success(product);
    }

    @GetMapping("/merchant/{merchantId}")
    public ApiResponse<?> getProductsByMerchant(@PathVariable Long merchantId) {
        List<Product> products = productService.getProductsByMerchant(merchantId);
        return ApiResponse.success(products);
    }

    @PostMapping("/{id}/audit")
    public ApiResponse<?> auditProduct(@PathVariable Long id, @RequestParam Integer status) {
        boolean result = productService.auditProduct(id, status, 1L);
        if (result) {
            return ApiResponse.success(status == 1 ? "审核通过" : "审核拒绝");
        }
        return ApiResponse.error("审核失败");
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        boolean result = productService.updateProduct(product);
        if (result) {
            return ApiResponse.success("更新成功");
        }
        return ApiResponse.error("更新失败");
    }
}