package com.example.campusmarket.controller;

import com.example.campusmarket.dto.ApiResponse;
import com.example.campusmarket.entity.ReturnRequest;
import com.example.campusmarket.entity.User;
import com.example.campusmarket.service.ReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/returns")
public class ReturnController {

    @Autowired
    private ReturnService returnService;

    /**
     * 买家提交退货申请
     */
    @PostMapping
    public ApiResponse<?> createReturnRequest(@RequestBody java.util.Map<String, Object> body, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        Long orderId = ((Number) body.get("orderId")).longValue();
        String reason = (String) body.get("reason");
        boolean result = returnService.createReturnRequest(orderId, user.getId(), reason);
        if (result) {
            return ApiResponse.success("退货申请提交成功，请等待商家审核");
        }
        return ApiResponse.error("退货申请失败，可能超时(限24小时内)或订单状态不符");
    }

    /**
     * 商家/管理员审核退货申请
     * @param status 1=通过，2=拒绝
     */
    @PostMapping("/{id}/audit")
    public ApiResponse<?> auditReturn(@PathVariable Long id, @RequestParam Integer status) {
        boolean result = returnService.auditReturn(id, status);
        if (result) {
            return ApiResponse.success(status == 1 ? "退货审核通过，款项已原路退回" : "退货审核已拒绝");
        }
        return ApiResponse.error("审核失败");
    }

    /**
     * 获取所有退货申请单（用于后台系统及商家面板展示列表）
     */
    @GetMapping
    public ApiResponse<?> getReturnList() {
        List<ReturnRequest> list = returnService.getReturnList();
        return ApiResponse.success(list);
    }
}