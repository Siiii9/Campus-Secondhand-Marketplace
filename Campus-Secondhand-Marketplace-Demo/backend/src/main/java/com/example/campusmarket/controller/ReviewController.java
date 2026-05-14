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
        boolean result = reviewService.addReview(review);
        if (result) {
            return ApiResponse.success("评价成功");
        }
        return ApiResponse.error("评价失败");
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

    @GetMapping("/merchant/{merchantId}/rating")
    public ApiResponse<?> getMerchantRating(@PathVariable Long merchantId) {
        return ApiResponse.success(reviewService.getMerchantRating(merchantId));
    }

    @GetMapping("/product/{productId}/rating")
    public ApiResponse<?> getProductRating(@PathVariable Long productId) {
        return ApiResponse.success(reviewService.getProductRating(productId));
    }

    @GetMapping("/buyer/{buyerId}/rating")
    public ApiResponse<?> getBuyerRating(@PathVariable Long buyerId) {
        return ApiResponse.success(reviewService.getBuyerRating(buyerId));
    }
}