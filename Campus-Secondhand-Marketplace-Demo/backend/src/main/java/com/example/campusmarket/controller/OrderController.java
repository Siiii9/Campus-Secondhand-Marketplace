package com.example.campusmarket.controller;

import com.example.campusmarket.dto.ApiResponse;
import com.example.campusmarket.entity.*;
import com.example.campusmarket.service.CartService;
import com.example.campusmarket.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @PostMapping("/cart")
    public ApiResponse<?> createOrderFromCart(HttpSession session, @RequestBody(required = false) Map<String, Object> body) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }

        boolean usePoints = body != null && Boolean.TRUE.equals(body.get("usePoints"));

        try {
            Order order = orderService.createOrderFromCart(user.getId(), usePoints);
            return ApiResponse.success(order);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        if (order == null) {
            return ApiResponse.error("订单不存在");
        }
        return ApiResponse.success(order);
    }

    @GetMapping
    public ApiResponse<?> getOrders(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        List<Order> orders = orderService.getOrdersByUser(user.getId());
        return ApiResponse.success(orders);
    }

    @GetMapping("/merchant")
    public ApiResponse<?> getMerchantOrders(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        List<Order> orders = orderService.getOrdersByMerchant(user.getId());
        return ApiResponse.success(orders);
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<?> confirmReceipt(@PathVariable Long id) {
        boolean result = orderService.confirmReceipt(id);
        if (result) {
            return ApiResponse.success("确认收货成功");
        }
        return ApiResponse.error("确认收货失败");
    }

    @PostMapping("/{id}/return")
    public ApiResponse<?> applyReturn(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        if (reason == null || reason.isEmpty()) {
            return ApiResponse.error("请填写退货原因");
        }
        boolean result = orderService.applyReturn(id, reason);
        if (result) {
            return ApiResponse.success("退货申请已提交");
        }
        return ApiResponse.error("退货申请失败，可能已超过退货时限");
    }

    @PostMapping("/{id}/refund/apply")
    public ApiResponse<?> applyRefund(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        if (reason == null || reason.isEmpty()) {
            return ApiResponse.error("请填写退款原因");
        }
        boolean result = orderService.applyRefund(id, reason);
        if (result) {
            return ApiResponse.success("退款申请已提交");
        }
        return ApiResponse.error("退款申请失败，订单状态不符");
    }

    @PostMapping("/{id}/ship")
    public ApiResponse<?> shipOrder(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String logisticsCompany = body.get("logisticsCompany");
        String trackingNumber = body.get("trackingNumber");
        if (logisticsCompany == null || logisticsCompany.isEmpty()) {
            return ApiResponse.error("请填写物流公司");
        }
        if (trackingNumber == null || trackingNumber.isEmpty()) {
            return ApiResponse.error("请填写运单号");
        }
        boolean result = orderService.shipOrder(id, logisticsCompany, trackingNumber);
        if (result) {
            return ApiResponse.success("发货成功");
        }
        return ApiResponse.error("发货失败");
    }

    @PostMapping("/{id}/refund")
    public ApiResponse<?> refundOrder(@PathVariable Long id) {
        boolean result = orderService.refundOrder(id);
        if (result) {
            return ApiResponse.success("退款成功");
        }
        return ApiResponse.error("退款失败");
    }

    @GetMapping("/returns")
    public ApiResponse<?> getReturnRequests(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        List<ReturnRequest> requests = orderService.getReturnRequestsByMerchant(user.getId());
        return ApiResponse.success(requests);
    }

    @PostMapping("/returns/{id}/approve")
    public ApiResponse<?> approveReturn(@PathVariable Long id) {
        boolean result = orderService.approveReturn(id, null);
        if (result) {
            return ApiResponse.success("退货已同意");
        }
        return ApiResponse.error("操作失败");
    }

    @PostMapping("/returns/{id}/reject")
    public ApiResponse<?> rejectReturn(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        if (reason == null || reason.isEmpty()) {
            return ApiResponse.error("请填写拒绝原因");
        }
        boolean result = orderService.approveReturn(id, reason);
        if (result) {
            return ApiResponse.success("退货已拒绝");
        }
        return ApiResponse.error("操作失败");
    }

    @PostMapping("/{id}/review")
    public ApiResponse<?> submitReview(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer rating = (Integer) body.get("rating");
        String content = (String) body.get("content");
        String reviewType = (String) body.get("reviewType");

        if (rating == null || rating < 1 || rating > 5) {
            return ApiResponse.error("请选择1-5星评分");
        }
        if (content == null || content.isEmpty()) {
            return ApiResponse.error("请填写评价内容");
        }
        if (reviewType == null || (!"PRODUCT".equals(reviewType) && !"MERCHANT_SERVICE".equals(reviewType))) {
            return ApiResponse.error("评价类型不正确");
        }

        Review review = orderService.submitReview(id, rating, content, reviewType);
        if (review != null) {
            return ApiResponse.success(review);
        }
        return ApiResponse.error("评价失败");
    }

    @PostMapping("/reviews/{id}/reply")
    public ApiResponse<?> replyReview(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reply = body.get("reply");
        if (reply == null || reply.isEmpty()) {
            return ApiResponse.error("请填写回复内容");
        }
        boolean result = orderService.replyReview(id, reply);
        if (result) {
            return ApiResponse.success("回复成功");
        }
        return ApiResponse.error("回复失败");
    }

    @GetMapping("/reviews")
    public ApiResponse<?> getMerchantReviews(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        List<Review> reviews = orderService.getReviewsByMerchant(user.getId());
        return ApiResponse.success(reviews);
    }
}