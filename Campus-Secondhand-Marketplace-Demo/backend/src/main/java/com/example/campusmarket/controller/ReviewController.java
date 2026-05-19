package com.example.campusmarket.controller;

import com.example.campusmarket.dto.ApiResponse;
import com.example.campusmarket.entity.Review;
import com.example.campusmarket.entity.User;
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
    public ApiResponse<?> replyReview(@PathVariable Long id, @RequestBody Review review, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        boolean result = reviewService.replyReview(id, review.getReply());
        if (result) {
            return ApiResponse.success("回复成功");
        }
        return ApiResponse.error("回复失败");
    }
}