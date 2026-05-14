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

    @GetMapping
    public ApiResponse<?> getCarousels() {
        List<Carousel> carousels = carouselService.getActiveCarousels();
        return ApiResponse.success(carousels);
    }

    @GetMapping("/type/{type}")
    public ApiResponse<?> getCarouselsByType(@PathVariable String type) {
        List<Carousel> carousels = carouselService.getCarouselsByType(type);
        return ApiResponse.success(carousels);
    }

    @GetMapping("/merchant/{merchantId}")
    public ApiResponse<?> getMerchantCarousels(@PathVariable Long merchantId) {
        List<Carousel> carousels = carouselService.getMerchantCarousels(merchantId);
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