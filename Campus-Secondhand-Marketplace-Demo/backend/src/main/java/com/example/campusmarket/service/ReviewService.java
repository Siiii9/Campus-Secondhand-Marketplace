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
        wrapper.eq("to_user_id", merchantId).eq("review_type", "MERCHANT_SERVICE");
        List<Review> reviews = reviewMapper.selectList(wrapper);
        
        int totalReviews = reviews.size();
        int positiveReviews = 0;
        int totalRating = 0;
        
        for (Review review : reviews) {
            if (review.getRating() != null) {
                totalRating += review.getRating();
                if (review.getRating() >= 4) {
                    positiveReviews++;
                }
            }
        }
        
        BigDecimal avgRating = totalReviews > 0 ? 
            BigDecimal.valueOf(totalRating).divide(BigDecimal.valueOf(totalReviews), 1, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;
        
        BigDecimal positiveRate = totalReviews > 0 ? 
            BigDecimal.valueOf(positiveReviews).divide(BigDecimal.valueOf(totalReviews), 4, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;
        
        result.put("totalReviews", totalReviews);
        result.put("avgRating", avgRating);
        result.put("positiveRate", positiveRate);
        
        return result;
    }

    public Map<String, Object> getProductRating(Long productId) {
        Map<String, Object> result = new HashMap<>();
        
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId).eq("review_type", "PRODUCT");
        List<Review> reviews = reviewMapper.selectList(wrapper);
        
        int totalReviews = reviews.size();
        int positiveReviews = 0;
        int totalRating = 0;
        
        for (Review review : reviews) {
            if (review.getRating() != null) {
                totalRating += review.getRating();
                if (review.getRating() >= 4) {
                    positiveReviews++;
                }
            }
        }
        
        BigDecimal avgRating = totalReviews > 0 ? 
            BigDecimal.valueOf(totalRating).divide(BigDecimal.valueOf(totalReviews), 1, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;
        
        BigDecimal positiveRate = totalReviews > 0 ? 
            BigDecimal.valueOf(positiveReviews).divide(BigDecimal.valueOf(totalReviews), 4, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;
        
        result.put("totalReviews", totalReviews);
        result.put("avgRating", avgRating);
        result.put("positiveRate", positiveRate);
        
        return result;
    }

    public Map<String, Object> getBuyerRating(Long buyerId) {
        Map<String, Object> result = new HashMap<>();
        
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("to_user_id", buyerId).eq("review_type", "BUYER");
        List<Review> reviews = reviewMapper.selectList(wrapper);
        
        int totalReviews = reviews.size();
        int positiveReviews = 0;
        int totalRating = 0;
        
        for (Review review : reviews) {
            if (review.getRating() != null) {
                totalRating += review.getRating();
                if (review.getRating() >= 4) {
                    positiveReviews++;
                }
            }
        }
        
        BigDecimal avgRating = totalReviews > 0 ? 
            BigDecimal.valueOf(totalRating).divide(BigDecimal.valueOf(totalReviews), 1, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;
        
        BigDecimal positiveRate = totalReviews > 0 ? 
            BigDecimal.valueOf(positiveReviews).divide(BigDecimal.valueOf(totalReviews), 4, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;
        
        result.put("totalReviews", totalReviews);
        result.put("avgRating", avgRating);
        result.put("positiveRate", positiveRate);
        
        return result;
    }
}