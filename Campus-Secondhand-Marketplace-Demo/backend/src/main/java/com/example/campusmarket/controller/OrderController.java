package com.example.campusmarket.controller;

import com.example.campusmarket.dto.ApiResponse;
import com.example.campusmarket.entity.Cart;
import com.example.campusmarket.entity.Order;
import com.example.campusmarket.entity.User;
import com.example.campusmarket.service.CartService;
import com.example.campusmarket.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @PostMapping
    public ApiResponse<?> createOrder(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }

        List<Cart> cartItems = cartService.getCartByUser(user.getId());
        if (cartItems.isEmpty()) {
            return ApiResponse.error("购物车为空");
        }

        try {
            List<Order> orders = orderService.createOrder(user.getId(), cartItems);
            cartService.clearCart(user.getId());
            return ApiResponse.success(orders);
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

    @PostMapping("/{id}/confirm")
    public ApiResponse<?> confirmReceipt(@PathVariable Long id) {
        boolean result = orderService.confirmReceipt(id);
        if (result) {
            return ApiResponse.success("确认收货成功");
        }
        return ApiResponse.error("确认收货失败");
    }
}