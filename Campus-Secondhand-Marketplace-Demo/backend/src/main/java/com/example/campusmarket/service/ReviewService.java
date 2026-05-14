package com.example.campusmarket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.campusmarket.entity.Review;
import com.example.campusmarket.mapper.ReviewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Map<String, Object> getMerchantRating(Long merchantId) {
        Map<String, Object> result = new HashMap<>();
        
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("to_user_id", merchantId);
        
        List<Review> reviews = reviewMapper.selectList(wrapper);
        int totalCount = reviews.size();
        
        if (totalCount == 0) {
            result.put("totalCount", 0);
            result.put("avgRating", 0.0);
            result.put("positiveRate", 0.0);
            return result;
        }
        
        int sumRating = 0;
        int positiveCount = 0;
        
        for (Review review : reviews) {
            Integer rating = review.getRating();
            if (rating != null) {
                sumRating += rating;
                if (rating >= 4) {
                    positiveCount++;
                }
            }
        }
        
        double avgRating = totalCount > 0 ? BigDecimal.valueOf(sumRating)
                .divide(BigDecimal.valueOf(totalCount), 1, RoundingMode.HALF_UP)
                .doubleValue() : 0.0;
        
        double positiveRate = totalCount > 0 ? BigDecimal.valueOf(positiveCount)
                .divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP)
                .doubleValue() : 0.0;
        
        result.put("totalCount", totalCount);
        result.put("avgRating", avgRating);
        result.put("positiveRate", positiveRate);
        
        return result;
    }

    public Map<String, Object> getProductRating(Long productId) {
        Map<String, Object> result = new HashMap<>();
        
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId);
        
        List<Review> reviews = reviewMapper.selectList(wrapper);
        int totalCount = reviews.size();
        
        if (totalCount == 0) {
            result.put("totalCount", 0);
            result.put("avgRating", 0.0);
            result.put("positiveRate", 0.0);
            return result;
        }
        
        int sumRating = 0;
        int positiveCount = 0;
        
        for (Review review : reviews) {
            Integer rating = review.getRating();
            if (rating != null) {
                sumRating += rating;
                if (rating >= 4) {
                    positiveCount++;
                }
            }
        }
        
        double avgRating = totalCount > 0 ? BigDecimal.valueOf(sumRating)
                .divide(BigDecimal.valueOf(totalCount), 1, RoundingMode.HALF_UP)
                .doubleValue() : 0.0;
        
        double positiveRate = totalCount > 0 ? BigDecimal.valueOf(positiveCount)
                .divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP)
                .doubleValue() : 0.0;
        
        result.put("totalCount", totalCount);
        result.put("avgRating", avgRating);
        result.put("positiveRate", positiveRate);
        
        return result;
    }
}