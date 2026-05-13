package com.example.campusmarket.controller;

import com.example.campusmarket.dto.ApiResponse;
import com.example.campusmarket.entity.Cart;
import com.example.campusmarket.entity.User;
import com.example.campusmarket.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public ApiResponse<?> getCart(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        List<Cart> cartItems = cartService.getCartByUser(user.getId());
        return ApiResponse.success(cartItems);
    }

    @PostMapping
    public ApiResponse<?> addToCart(@RequestParam Long productId, @RequestParam Integer quantity, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        boolean result = cartService.addToCart(user.getId(), productId, quantity);
        if (result) {
            return ApiResponse.success("添加成功");
        }
        return ApiResponse.error("添加失败");
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateCart(@PathVariable Long id, @RequestParam(required = false) Integer quantity, @RequestParam(required = false) Integer selected) {
        boolean result = cartService.updateCart(id, quantity, selected);
        if (result) {
            return ApiResponse.success("更新成功");
        }
        return ApiResponse.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> removeFromCart(@PathVariable Long id) {
        boolean result = cartService.removeFromCart(id);
        if (result) {
            return ApiResponse.success("删除成功");
        }
        return ApiResponse.error("删除失败");
    }

    @DeleteMapping
    public ApiResponse<?> clearCart(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        boolean result = cartService.clearCart(user.getId());
        if (result) {
            return ApiResponse.success("清空成功");
        }
        return ApiResponse.error("清空失败");
    }
}