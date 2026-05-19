package com.example.campusmarket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.campusmarket.entity.Order;
import com.example.campusmarket.entity.Product;
import com.example.campusmarket.entity.Review;
import com.example.campusmarket.mapper.OrderMapper;
import com.example.campusmarket.mapper.ProductMapper;
import com.example.campusmarket.mapper.ReviewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    /**
     * 发表评价（增加了订单状态更改和商品平均分计算）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean addReview(Review review) {
        // 校验订单是否存在，且必须是 2=已收货 状态才能评价
        Order order = orderMapper.selectById(review.getOrderId());
        if (order == null || order.getStatus() != 2) {
            throw new RuntimeException("订单未完成或不存在，无法进行评价");
        }

        review.setCreatedAt(LocalDateTime.now());
        boolean insertSuccess = reviewMapper.insert(review) > 0;

        if (insertSuccess) {
            // 1. 自动计算并同步更新该商品的最新平均评分 (avg_rating)
            if (review.getProductId() != null) {
                QueryWrapper<Review> wrapper = new QueryWrapper<>();
                wrapper.eq("product_id", review.getProductId());
                List<Review> reviews = reviewMapper.selectList(wrapper);

                if (reviews != null && !reviews.isEmpty()) {
                    double sum = reviews.stream().mapToInt(Review::getRating).sum();
                    double avg = sum / reviews.size();

                    Product product = productMapper.selectById(review.getProductId());
                    if (product != null) {
                        product.setAvgRating(BigDecimal.valueOf(avg));
                        productMapper.updateById(product);
                    }
                }
            }

            // 2. 扭转订单状态：将订单更新为 5=已评价/完全结束
            order.setStatus(5);
            orderMapper.updateById(order);
        }

        return insertSuccess;
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

    public boolean replyReview(Long reviewId, String reply) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            return false;
        }
        review.setReply(reply);
        return reviewMapper.updateById(review) > 0;
    }

    /**
     * 商家评价买家
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean addMerchantReview(Review review) {
        Order order = orderMapper.selectById(review.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        review.setReviewType("BUYER");
        review.setCreatedAt(LocalDateTime.now());
        return reviewMapper.insert(review) > 0;
    }

    /**
     * 计算商品好评率
     */
    public double getProductPositiveRate(Long productId) {
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId);
        List<Review> reviews = reviewMapper.selectList(wrapper);
        
        if (reviews == null || reviews.isEmpty()) {
            return 0;
        }
        
        long positiveCount = reviews.stream()
            .filter(r -> r.getRating() != null && r.getRating() >= 4)
            .count();
        
        return (double) positiveCount / reviews.size() * 100;
    }

    /**
     * 计算商家好评率（服务态度）
     */
    public double getMerchantPositiveRate(Long merchantId) {
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("to_user_id", merchantId).eq("review_type", "MERCHANT_SERVICE");
        List<Review> reviews = reviewMapper.selectList(wrapper);
        
        if (reviews == null || reviews.isEmpty()) {
            return 0;
        }
        
        long positiveCount = reviews.stream()
            .filter(r -> r.getRating() != null && r.getRating() >= 4)
            .count();
        
        return (double) positiveCount / reviews.size() * 100;
    }

    /**
     * 获取用户评价列表（用于用户个人中心查看评价）
     */
    public List<Review> getUserReviews(Long userId) {
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("from_user_id", userId);
        return reviewMapper.selectList(wrapper);
    }
}