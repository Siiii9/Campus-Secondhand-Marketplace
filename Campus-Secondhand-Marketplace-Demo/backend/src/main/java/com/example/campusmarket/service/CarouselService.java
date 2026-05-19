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

    /**
     * 前端展示：只获取启用的轮播图 (status = 1)
     */
    public List<Carousel> getActiveCarousels() {
        QueryWrapper<Carousel> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).orderByAsc("sort_order"); // 保持原有排序逻辑
        return carouselMapper.selectList(wrapper);
    }

    /**
     * 【新增】后台维护：拉取数据库全量轮播图（包括未启用的），方便管理端上下架
     */
    public List<Carousel> getAllCarousels() {
        QueryWrapper<Carousel> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("created_at"); // 按照创建时间倒序排列
        return carouselMapper.selectList(wrapper);
    }

    public boolean addCarousel(Carousel carousel) {
        carousel.setCreatedAt(LocalDateTime.now());
        if (carousel.getStatus() == null) {
            carousel.setStatus(1); // 兜底：默认状态为启用
        }
        return carouselMapper.insert(carousel) > 0;
    }

    public boolean updateCarousel(Carousel carousel) {
        return carouselMapper.updateById(carousel) > 0;
    }

    /**
     * 根据数据库字段规范：id 类型为 INT
     */
    public boolean deleteCarousel(Integer id) {
        return carouselMapper.deleteById(id) > 0;
    }
}