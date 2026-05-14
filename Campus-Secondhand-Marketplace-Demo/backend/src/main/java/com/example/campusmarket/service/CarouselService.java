package com.example.campusmarket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.campusmarket.entity.Carousel;
import com.example.campusmarket.mapper.CarouselMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CarouselService {

    @Autowired
    private CarouselMapper carouselMapper;

    public List<Carousel> getActiveCarousels() {
        QueryWrapper<Carousel> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).orderByAsc("sort_order");
        return carouselMapper.selectList(wrapper);
    }

    public List<Carousel> getCarouselsByType(String type) {
        QueryWrapper<Carousel> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).eq("type", type).orderByAsc("sort_order");
        return carouselMapper.selectList(wrapper);
    }

    public List<Carousel> getMerchantCarousels(Long merchantId) {
        QueryWrapper<Carousel> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).eq("merchant_id", merchantId).orderByAsc("sort_order");
        return carouselMapper.selectList(wrapper);
    }

    public boolean addCarousel(Carousel carousel) {
        carousel.setCreatedAt(LocalDateTime.now());
        return carouselMapper.insert(carousel) > 0;
    }

    public boolean updateCarousel(Carousel carousel) {
        return carouselMapper.updateById(carousel) > 0;
    }

    public boolean deleteCarousel(Integer id) {
        return carouselMapper.deleteById(id) > 0;
    }
}