package com.example.campusmarket.controller;

import com.example.campusmarket.dto.ApiResponse;
import com.example.campusmarket.entity.Review;
import com.example.campusmarket.entity.User;
import com.example.campusmarket.mapper.ReviewMapper;
import com.example.campusmarket.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewMapper reviewMapper;

    @PostMapping
    public ApiResponse<?> addReview(@RequestBody Review review, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        review.setFromUserId(user.getId());

        try {
            boolean result = reviewService.addReview(review);
            if (result) {
                return ApiResponse.success("评价成功");
            }
            return ApiResponse.error("评价失败");
        } catch (Exception e) {
            // 捕获订单状态或业务校验抛出的异常，优雅返回
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/product/{productId}")
    public ApiResponse<?> getProductReviews(@PathVariable Long productId) {
        List<Review> reviews = reviewService.getProductReviews(productId);
        return ApiResponse.success(reviews);
    }

    @GetMapping("/merchant/{merchantId}")
    public ApiResponse<?> getMerchantReviews(@PathVariable Long merchantId) {
        List<Review> reviews = reviewService.getMerchantReviews(merchantId);
        return ApiResponse.success(reviews);
    }

    @PostMapping("/{id}/reply")
    public ApiResponse<?> replyReview(@PathVariable Long id, @RequestBody java.util.Map<String, String> body, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        String reply = body.get("content");
        if (reply == null || reply.isEmpty()) {
            return ApiResponse.error("回复内容不能为空");
        }
        boolean result = reviewService.replyReview(id, reply);
        if (result) {
            return ApiResponse.success("回复成功");
        }
        return ApiResponse.error("回复失败");
    }

    /**
     * 商家评价买家
     */
    @PostMapping("/merchant")
    public ApiResponse<?> addMerchantReview(@RequestBody Review review, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        review.setFromUserId(user.getId());
        
        try {
            boolean result = reviewService.addMerchantReview(review);
            if (result) {
                return ApiResponse.success("评价成功");
            }
            return ApiResponse.error("评价失败");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取商品好评率
     */
    @GetMapping("/product/{productId}/rate")
    public ApiResponse<?> getProductPositiveRate(@PathVariable Long productId) {
        double rate = reviewService.getProductPositiveRate(productId);
        return ApiResponse.success(rate);
    }

    /**
     * 获取商家好评率
     */
    @GetMapping("/merchant/{merchantId}/rate")
    public ApiResponse<?> getMerchantPositiveRate(@PathVariable Long merchantId) {
        double rate = reviewService.getMerchantPositiveRate(merchantId);
        return ApiResponse.success(rate);
    }

    /**
     * 获取当前用户的评价列表
     */
    @GetMapping("/my")
    public ApiResponse<?> getUserReviews(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        List<Review> reviews = reviewService.getUserReviews(user.getId());
        return ApiResponse.success(reviews);
    }

    /**
     * 测试接口：添加测试评价数据
     */
    @GetMapping("/test/add")
    public ApiResponse<?> addTestReview() {
        Review review = new Review();
        review.setOrderId(1L);
        review.setProductId(3L);
        review.setFromUserId(6L);
        review.setToUserId(7L);
        review.setRating(5);
        review.setContent("商品非常好，卖家服务态度也很好！");
        review.setReviewType("PRODUCT");
        review.setCreatedAt(java.time.LocalDateTime.now());
        reviewMapper.insert(review);

        Review review2 = new Review();
        review2.setOrderId(2L);
        review2.setProductId(3L);
        review2.setFromUserId(8L);
        review2.setToUserId(7L);
        review2.setRating(4);
        review2.setContent("商品质量不错，物流也很快");
        review2.setReviewType("PRODUCT");
        review2.setCreatedAt(java.time.LocalDateTime.now());
        reviewMapper.insert(review2);

        return ApiResponse.success("测试评价添加成功");
    }
}