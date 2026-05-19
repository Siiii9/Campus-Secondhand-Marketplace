package com.example.campusmarket.controller;

import com.example.campusmarket.dto.ApiResponse;
import com.example.campusmarket.entity.Carousel;
import com.example.campusmarket.entity.User;
import com.example.campusmarket.service.CarouselService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/carousel")
public class CarouselController {

    @Autowired
    private CarouselService carouselService;

    @GetMapping
    public ApiResponse<?> getCarousels() {
        List<Carousel> carousels = carouselService.getAllCarousels();
        return ApiResponse.success(carousels);
    }

    @GetMapping("/active")
    public ApiResponse<?> getActiveCarousels() {
        List<Carousel> carousels = carouselService.getActiveCarousels();
        return ApiResponse.success(carousels);
    }

    @PostMapping("/apply")
    public ApiResponse<?> applyCarousel(
            @RequestParam("productId") Integer productId,
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        if (file.isEmpty()) {
            return ApiResponse.error("请上传轮播图");
        }
        if (productId == null) {
            return ApiResponse.error("请选择商品");
        }

        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads/carousel/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
            String filename = UUID.randomUUID().toString() + ext;
            File destFile = new File(uploadDir + filename);
            file.transferTo(destFile);

            Carousel carousel = new Carousel();
            carousel.setLinkUrl("/product/" + productId);
            carousel.setImageUrl("/uploads/carousel/" + filename);
            carousel.setStatus(0);
            carousel.setSortOrder(0);

            boolean result = carouselService.addCarousel(carousel);
            if (result) {
                return ApiResponse.success("申请提交成功，请等待审核");
            }
            return ApiResponse.error("申请提交失败");
        } catch (IOException e) {
            return ApiResponse.error("上传失败");
        }
    }

    @PostMapping
    public ApiResponse<?> addCarousel(@RequestBody Carousel carousel) {
        boolean result = carouselService.addCarousel(carousel);
        if (result) {
            return ApiResponse.success("添加成功");
        }
        return ApiResponse.error("添加失败");
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateCarousel(@PathVariable Integer id, @RequestBody Carousel carousel) {
        carousel.setId(id);
        boolean result = carouselService.updateCarousel(carousel);
        if (result) {
            return ApiResponse.success("更新成功");
        }
        return ApiResponse.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteCarousel(@PathVariable Integer id) {
        boolean result = carouselService.deleteCarousel(id);
        if (result) {
            return ApiResponse.success("删除成功");
        }
        return ApiResponse.error("删除失败");
    }
}