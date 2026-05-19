package com.example.campusmarket.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusmarket.dto.ApiResponse;
import com.example.campusmarket.dto.ProductRequest;
import com.example.campusmarket.entity.Product;
import com.example.campusmarket.entity.User;
import com.example.campusmarket.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private org.springframework.core.env.Environment env;

    private String getUploadDir() {
        String uploadDir = env.getProperty("file.upload-dir", "uploads/");
        File dir = new File(uploadDir + "images/");
        if (!dir.isAbsolute()) {
            dir = new File(System.getProperty("user.dir"), uploadDir + "images/");
        }
        return dir.getAbsolutePath() + File.separator;
    }

    private String saveFile(MultipartFile file) throws IOException {
        String uploadDirPath = getUploadDir();
        File uploadDir = new File(uploadDirPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String newFilename = UUID.randomUUID().toString() + extension;

        File destFile = new File(uploadDirPath + newFilename);
        file.transferTo(destFile);

        return "/api/images/" + newFilename;
    }

    @PostMapping
    public ApiResponse<?> addProduct(@RequestBody ProductRequest request, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        if (!"MERCHANT".equals(user.getRole())) {
            return ApiResponse.error("只有商家可以发布商品");
        }
        if (user.getShopStatus() != null && user.getShopStatus() == 0) {
            return ApiResponse.error("您的店铺已被管理员关闭，无法发布新商品");
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
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer auditStatus) {
        Page<Map<String, Object>> products = productService.searchProducts(keyword, sortBy, page, size, auditStatus);
        return ApiResponse.success(products);
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getProductById(@PathVariable Long id) {
        Map<String, Object> detail = productService.getProductDetail(id);
        if (detail == null) {
            return ApiResponse.error("商品不存在");
        }
        return ApiResponse.success(detail);
    }

    @GetMapping("/merchant/{merchantId}")
    public ApiResponse<?> getProductsByMerchant(@PathVariable Long merchantId) {
        List<Map<String, Object>> products = productService.getProductsByMerchant(merchantId);
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

    @PostMapping("/upload")
    public ApiResponse<?> uploadImages(@RequestParam("files") List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return ApiResponse.error("请选择要上传的文件");
        }

        List<String> imageUrls = new java.util.ArrayList<>();
        try {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String imageUrl = saveFile(file);
                    imageUrls.add(imageUrl);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }

        return ApiResponse.success(imageUrls);
    }

    @PostMapping("/submit")
    public ApiResponse<?> submitProduct(
            @RequestParam String name,
            @RequestParam Integer categoryId,
            @RequestParam Double originalPrice,
            @RequestParam Double discountPrice,
            @RequestParam Integer stock,
            @RequestParam(required = false) String unit,
            @RequestParam String conditionLevel,
            @RequestParam Integer isNegotiable,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) List<MultipartFile> images,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        if (!"MERCHANT".equals(user.getRole())) {
            return ApiResponse.error("只有商家可以发布商品");
        }

        Product product = new Product();
        product.setMerchantId(user.getId());
        product.setName(name);
        product.setCategoryId(categoryId);
        product.setDescription(description);
        product.setOriginalPrice(java.math.BigDecimal.valueOf(originalPrice));
        product.setDiscountPrice(java.math.BigDecimal.valueOf(discountPrice));
        product.setStock(stock);
        product.setUnit(unit);
        product.setIsNegotiable(isNegotiable);
        product.setConditionLevel(conditionLevel);

        List<String> imageUrls = new java.util.ArrayList<>();
        if (images != null && !images.isEmpty()) {
            try {
                for (MultipartFile file : images) {
                    if (!file.isEmpty()) {
                        String imageUrl = saveFile(file);
                        imageUrls.add(imageUrl);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                return ApiResponse.error("图片上传失败: " + e.getMessage());
            }
        }

        boolean result = productService.addProduct(product, imageUrls);
        if (result) {
            return ApiResponse.success("发布成功，等待审核");
        }
        return ApiResponse.error("发布失败");
    }

    @PutMapping("/{id}/offline")
    public ApiResponse<?> offlineProduct(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }

        Product product = productService.getProductById(id);
        if (product == null) {
            return ApiResponse.error("商品不存在");
        }

        if (!product.getMerchantId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
            return ApiResponse.error("无权限操作");
        }

        product.setStatus(2);
        boolean result = productService.updateProduct(product);
        if (result) {
            return ApiResponse.success("下架成功");
        }
        return ApiResponse.error("下架失败");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteProduct(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }

        Product product = productService.getProductById(id);
        if (product == null) {
            return ApiResponse.error("商品不存在");
        }

        if (!product.getMerchantId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
            return ApiResponse.error("无权限操作");
        }

        boolean result = productService.deleteProduct(id);
        if (result) {
            return ApiResponse.success("删除成功");
        }
        return ApiResponse.error("删除失败");
    }

    @PutMapping("/{id}/stock")
    public ApiResponse<?> updateStock(@PathVariable Long id, @RequestBody Map<String, Integer> request, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }

        Product product = productService.getProductById(id);
        if (product == null) {
            return ApiResponse.error("商品不存在");
        }

        if (!product.getMerchantId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
            return ApiResponse.error("无权限操作");
        }

        Integer stock = request.get("stock");
        if (stock == null || stock < 0) {
            return ApiResponse.error("库存数量无效");
        }

        product.setStock(stock);
        boolean result = productService.updateProduct(product);
        if (result) {
            return ApiResponse.success("库存调整成功");
        }
        return ApiResponse.error("库存调整失败");
    }

    @GetMapping("/{id}/detail")
    public ApiResponse<?> getProductDetail(@PathVariable Long id) {
        Map<String, Object> detail = productService.getProductDetail(id);
        if (detail == null) {
            return ApiResponse.error("商品不存在");
        }
        return ApiResponse.success(detail);
    }

    @GetMapping("/merchant/{merchantId}/shop")
    public ApiResponse<?> getShopProducts(
            @PathVariable Long merchantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        
        Page<Map<String, Object>> products = productService.getShopProducts(merchantId, status, page, size);
        return ApiResponse.success(products);
    }

    @GetMapping("/merchant")
    public ApiResponse<?> getMerchantProducts(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        
        List<Map<String, Object>> products = productService.getProductsByMerchant(user.getId());
        return ApiResponse.success(products);
    }

    @GetMapping("/merchant/shop")
    public ApiResponse<?> getCurrentShopProducts(
            HttpSession session,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        
        Page<Map<String, Object>> products = productService.getShopProducts(user.getId(), 1, page, size);
        return ApiResponse.success(products);
    }
}