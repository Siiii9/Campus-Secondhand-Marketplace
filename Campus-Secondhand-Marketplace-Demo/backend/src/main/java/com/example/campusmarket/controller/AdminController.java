package com.example.campusmarket.controller;

import com.example.campusmarket.dto.ApiResponse;
import com.example.campusmarket.entity.User;
import com.example.campusmarket.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public ApiResponse<?> getAllUsers() {
        List<User> users = userService.list();
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
    public ApiResponse<?> auditUser(@PathVariable Long id, @RequestParam Integer status, @RequestParam(required = false) String remark) {
        boolean result = userService.auditUser(id, status, remark, 1L);
        if (result) {
            return ApiResponse.success(status == 1 ? "审核通过" : "审核拒绝");
        }
        return ApiResponse.error("审核失败");
    }
}