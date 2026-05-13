package com.example.campusmarket.controller;

import com.example.campusmarket.dto.ApiResponse;
import com.example.campusmarket.dto.LoginRequest;
import com.example.campusmarket.dto.RegisterRequest;
import com.example.campusmarket.entity.User;
import com.example.campusmarket.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ApiResponse<?> register(@RequestBody RegisterRequest request) {
        if (userService.findByUsername(request.getUsername()) != null) {
            return ApiResponse.error("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());  // 不加密
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setCity(request.getCity());
        user.setGender(request.getGender());
        user.setBankAccount(request.getBankAccount());
        user.setRole(request.getRole());
        user.setShopName(request.getShopName());

        boolean result = userService.register(user, null, null, null);
        if (result) {
            return ApiResponse.success("注册成功，请等待审核");
        }
        return ApiResponse.error("注册失败");
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody LoginRequest request, HttpSession session) {
        User user = userService.findByUsername(request.getUsername());
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }

        if (!user.getPassword().equals(request.getPassword())) {  // 不加密直接比较
            return ApiResponse.error("密码错误");
        }

        if (user.getStatus() != 1) {
            return ApiResponse.error("账号未审核或已禁用");
        }

        session.setAttribute("user", user);
        return ApiResponse.success(user);
    }

    @GetMapping("/info")
    public ApiResponse<?> getUserInfo(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        return ApiResponse.success(user);
    }

    @PostMapping("/logout")
    public ApiResponse<?> logout(HttpSession session) {
        session.removeAttribute("user");
        return ApiResponse.success("退出成功");
    }
}