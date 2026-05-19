package com.example.campusmarket.controller;

import com.example.campusmarket.dto.ApiResponse;
import com.example.campusmarket.entity.User;
import com.example.campusmarket.service.ReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/returns")
public class ReturnController {

    @Autowired
    private ReturnService returnService;

    @PostMapping
    public ApiResponse<?> createReturnRequest(@RequestParam Long orderId, @RequestParam String reason, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        boolean result = returnService.createReturnRequest(orderId, user.getId(), reason);
        if (result) {
            return ApiResponse.success("退货申请提交成功");
        }
        return ApiResponse.error("退货申请失败");
    }

    @PostMapping("/{id}/audit")
    public ApiResponse<?> auditReturn(@PathVariable Long id, @RequestParam Integer status) {
        boolean result = returnService.auditReturn(id, status);
        if (result) {
            return ApiResponse.success(status == 1 ? "退货审核通过" : "退货审核拒绝");
        }
        return ApiResponse.error("审核失败");
    }
}