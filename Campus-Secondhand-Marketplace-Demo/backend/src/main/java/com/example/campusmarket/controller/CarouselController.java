package com.example.campusmarket.controller;

import com.example.campusmarket.dto.ApiResponse;
import com.example.campusmarket.entity.Carousel;
import com.example.campusmarket.service.CarouselService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carousel")
public class CarouselController {

    @Autowired
    private CarouselService carouselService;

    /**
     * 【修改】后台管理：拉取全部轮播图
     * 请求：GET /api/carousel
     */
    @GetMapping
    public ApiResponse<?> getCarousels() {
        List<Carousel> carousels = carouselService.getAllCarousels();
        return ApiResponse.success(carousels);
    }

    /**
     * 【新增】前台门户首页：只获取激活展示中的轮播图列表
     * 请求：GET /api/carousel/active
     */
    @GetMapping("/active")
    public ApiResponse<?> getActiveCarousels() {
        List<Carousel> carousels = carouselService.getActiveCarousels();
        return ApiResponse.success(carousels);
    }

    @PostMapping
    public ApiResponse<?> addCarousel(@RequestBody Carousel carousel) {
        boolean result = carouselService.addCarousel(carousel);
        if (result) {
            return ApiResponse.success("添加成功");
        }
        return ApiResponse.error("添加失败");
    }

    /**
     * 根据数据库字段规范：id 类型为 INT
     */
    @PutMapping("/{id}")
    public ApiResponse<?> updateCarousel(@PathVariable Integer id, @RequestBody Carousel carousel) {
        carousel.setId(id);
        boolean result = carouselService.updateCarousel(carousel);
        if (result) {
            return ApiResponse.success("更新成功");
        }
        return ApiResponse.error("更新失败");
    }

    /**
     * 根据数据库字段规范：id 类型为 INT
     */
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteCarousel(@PathVariable Integer id) {
        boolean result = carouselService.deleteCarousel(id);
        if (result) {
            return ApiResponse.success("删除成功");
        }
        return ApiResponse.error("删除失败");
    }
}