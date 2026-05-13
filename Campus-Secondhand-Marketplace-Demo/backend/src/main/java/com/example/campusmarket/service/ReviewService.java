package com.example.campusmarket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.campusmarket.entity.Review;
import com.example.campusmarket.mapper.ReviewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    public boolean addReview(Review review) {
        review.setCreatedAt(LocalDateTime.now());
        return reviewMapper.insert(review) > 0;
    }

    public List<Review> getProductReviews(Long productId) {
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId);
        return reviewMapper.selectList(wrapper);
    }

    public List<Review> getMerchantReviews(Long merchantId) {
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("to_user_id", merchantId).eq("review_type", "MERCHANT_SERVICE");
        return reviewMapper.selectList(wrapper);
    }

    public List<Review> getBuyerReviews(Long buyerId) {
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("to_user_id", buyerId).eq("review_type", "BUYER");
        return reviewMapper.selectList(wrapper);
    }
}