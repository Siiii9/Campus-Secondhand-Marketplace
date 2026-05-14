package com.example.campusmarket.controller;

import com.example.campusmarket.dto.ApiResponse;
import com.example.campusmarket.dto.LoginRequest;
import com.example.campusmarket.dto.RegisterRequest;
import com.example.campusmarket.dto.UserUpdateRequest;
import com.example.campusmarket.entity.User;
import com.example.campusmarket.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private org.springframework.core.env.Environment env;

    private String getUploadDir() {
        String uploadDir = env.getProperty("file.upload-dir", "uploads/");
        File dir = new File(uploadDir + "images/");
        if (!dir.isAbsolute()) {
            dir = new File(System.getProperty("user.dir"), uploadDir + "images/");
        }
        return dir.getAbsolutePath() + File.separator;
    }

    @PostMapping("/register")
    public ApiResponse<?> register(@RequestBody RegisterRequest request, HttpSession session) {
        String sessionCaptcha = (String) session.getAttribute("captcha");
        if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(request.getCaptcha())) {
            return ApiResponse.error("验证码错误");
        }

        session.removeAttribute("captcha");

        if (userService.findByUsername(request.getUsername()) != null) {
            return ApiResponse.error("用户名已存在");
        }

        String role = request.getRole();
        if (!"USER".equals(role) && !"MERCHANT".equals(role)) {
            return ApiResponse.error("无效的角色类型");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setCity(request.getCity());
        user.setGender(request.getGender());
        user.setBankAccount(request.getBankAccount());
        user.setRole(role);
        user.setShopName(request.getShopName());

        boolean result = userService.register(user, request.getBusinessLicense(), 
                request.getIdCardFront(), request.getIdCardBack());
        if (result) {
            return ApiResponse.success("注册成功，请等待审核");
        }
        return ApiResponse.error("注册失败");
    }

    @PostMapping("/register/merchant")
    public ApiResponse<?> registerMerchant(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String realName,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam String city,
            @RequestParam String gender,
            @RequestParam String bankAccount,
            @RequestParam String shopName,
            @RequestParam String captcha,
            @RequestParam(required = false) MultipartFile businessLicense,
            @RequestParam(required = false) MultipartFile idCardFront,
            @RequestParam(required = false) MultipartFile idCardBack,
            HttpSession session) {

        String sessionCaptcha = (String) session.getAttribute("captcha");
        if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(captcha)) {
            return ApiResponse.error("验证码错误");
        }

        session.removeAttribute("captcha");

        if (userService.findByUsername(username) != null) {
            return ApiResponse.error("用户名已存在");
        }

        String businessLicensePath = null;
        String idCardFrontPath = null;
        String idCardBackPath = null;

        try {
            if (businessLicense != null && !businessLicense.isEmpty()) {
                businessLicensePath = saveFile(businessLicense);
            }
            if (idCardFront != null && !idCardFront.isEmpty()) {
                idCardFrontPath = saveFile(idCardFront);
            }
            if (idCardBack != null && !idCardBack.isEmpty()) {
                idCardBackPath = saveFile(idCardBack);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRealName(realName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setCity(city);
        user.setGender(gender);
        user.setBankAccount(bankAccount);
        user.setRole("MERCHANT");
        user.setShopName(shopName);

        boolean result = userService.register(user, businessLicensePath, idCardFrontPath, idCardBackPath);
        if (result) {
            return ApiResponse.success("商家注册成功，请等待审核");
        }
        return ApiResponse.error("注册失败");
    }

    private String saveFile(MultipartFile file) throws IOException {
        String uploadDirPath = getUploadDir();
        File uploadDir = new File(uploadDirPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String newFilename = UUID.randomUUID().toString() + extension;

        File destFile = new File(uploadDirPath + newFilename);
        file.transferTo(destFile);

        return "/api/images/" + newFilename;
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

    @GetMapping("/{id}")
    public ApiResponse<?> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        return ApiResponse.success(user);
    }

    @PostMapping("/logout")
    public ApiResponse<?> logout(HttpSession session) {
        session.removeAttribute("user");
        return ApiResponse.success("退出成功");
    }

    @PutMapping("/info")
    public ApiResponse<?> updateUserInfo(@RequestBody UserUpdateRequest request, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }

        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getCity() != null) {
            user.setCity(request.getCity());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getBankAccount() != null) {
            user.setBankAccount(request.getBankAccount());
        }
        if (request.getShopName() != null && "MERCHANT".equals(user.getRole())) {
            user.setShopName(request.getShopName());
        }

        boolean result = userService.updateUser(user);
        if (result) {
            session.setAttribute("user", user);
            return ApiResponse.success("修改成功");
        }
        return ApiResponse.error("修改失败");
    }
}