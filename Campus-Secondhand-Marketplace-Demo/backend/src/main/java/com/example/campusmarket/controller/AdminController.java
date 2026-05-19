package com.example.campusmarket.controller;

import com.example.campusmarket.dto.ApiResponse;
import com.example.campusmarket.entity.User;
import com.example.campusmarket.entity.UserAuditLog;
import com.example.campusmarket.entity.Wallet;
import com.example.campusmarket.service.UserService;
import com.example.campusmarket.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private WalletService walletService;

    @GetMapping("/users")
    public ApiResponse<?> getAllUsers(@RequestParam(required = false) String keyword, 
                                      @RequestParam(required = false) String role) {
        List<User> users = userService.list(keyword, role);
        return ApiResponse.success(users);
    }

    @GetMapping("/users/{id}")
    public ApiResponse<?> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        return ApiResponse.success(user);
    }

    @GetMapping("/users/pending")
    public ApiResponse<?> getPendingUsers() {
        List<User> users = userService.getPendingUsers();
        return ApiResponse.success(users);
    }

    @GetMapping("/users/{id}/audit-detail")
    public ApiResponse<?> getAuditDetail(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        UserAuditLog auditLog = userService.getAuditLogByUserId(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("phone", user.getPhone());
        
        if (auditLog != null) {
            result.put("businessLicense", auditLog.getBusinessLicense());
            result.put("idCardFront", auditLog.getIdCardFront());
            result.put("idCardBack", auditLog.getIdCardBack());
        }
        
        return ApiResponse.success(result);
    }

    @PutMapping("/users/{id}")
    public ApiResponse<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        boolean result = userService.updateUser(user);
        if (result) {
            return ApiResponse.success("更新成功");
        }
        return ApiResponse.error("更新失败");
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<?> deleteUser(@PathVariable Long id) {
        boolean result = userService.deleteUser(id);
        if (result) {
            return ApiResponse.success("删除成功");
        }
        return ApiResponse.error("删除失败");
    }

    @PostMapping("/users/{id}/audit")
    public ApiResponse<?> auditUser(@PathVariable Long id, @RequestParam Integer status, 
                                    @RequestParam(required = false) String remark) {
        boolean result = userService.auditUser(id, status, remark, 1L);
        if (result) {
            return ApiResponse.success(status == 1 ? "审核通过" : "审核拒绝");
        }
        return ApiResponse.error("审核失败");
    }

    @PostMapping("/users/{id}/level")
    public ApiResponse<?> updateMerchantLevel(@PathVariable Long id, @RequestParam Integer level) {
        if (level < 1 || level > 5) {
            return ApiResponse.error("等级必须在1-5之间");
        }
        boolean result = userService.updateMerchantLevel(id, level);
        if (result) {
            return ApiResponse.success("等级调整成功");
        }
        return ApiResponse.error("等级调整失败");
    }

    @PostMapping("/users/{id}/close-shop")
    public ApiResponse<?> closeShop(@PathVariable Long id) {
        boolean result = userService.closeShop(id);
        if (result) {
            return ApiResponse.success("店铺已关闭");
        }
        return ApiResponse.error("关闭店铺失败");
    }

    @PostMapping("/wallet/recharge")
    public ApiResponse<?> rechargeUserWallet(@RequestParam Long userId, @RequestParam BigDecimal amount) {
        boolean result = walletService.recharge(userId, amount);
        if (result) {
            Wallet wallet = walletService.getWalletByUserId(userId);
            return ApiResponse.success("充值成功", wallet);
        }
        return ApiResponse.error("充值失败");
    }
}
    
    @PostMapping("/users/{id}/open-shop")
    public ApiResponse<?> openShop(@PathVariable Long id) {
        boolean result = userService.openShop(id);
        if (result) {
            return ApiResponse.success("店铺已恢复");
        }
        return ApiResponse.error("恢复店铺失败");
    }
}
