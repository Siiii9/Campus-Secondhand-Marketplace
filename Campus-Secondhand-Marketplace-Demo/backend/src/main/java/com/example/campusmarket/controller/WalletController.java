package com.example.campusmarket.controller;

import com.example.campusmarket.dto.ApiResponse;
import com.example.campusmarket.entity.User;
import com.example.campusmarket.entity.Wallet;
import com.example.campusmarket.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping
    public ApiResponse<?> getWallet(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        Wallet wallet = walletService.getWalletByUserId(user.getId());
        return ApiResponse.success(wallet);
    }

    @PostMapping("/recharge")
    public ApiResponse<?> recharge(@RequestParam BigDecimal amount, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        boolean result = walletService.recharge(user.getId(), amount);
        if (result) {
            return ApiResponse.success("充值成功");
        }
        return ApiResponse.error("充值失败");
    }

    @PostMapping("/admin/recharge")
    public ApiResponse<?> adminRecharge(@RequestParam Long userId, @RequestParam BigDecimal amount) {
        boolean result = walletService.adminRecharge(userId, amount);
        if (result) {
            return ApiResponse.success("充值成功");
        }
        return ApiResponse.error("充值失败");
    }
}