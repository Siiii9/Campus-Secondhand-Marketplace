package com.example.campusmarket.controller;

import com.example.campusmarket.util.CaptchaUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {

    @GetMapping
    public String getCaptcha(HttpServletRequest request) {
        HttpSession session = request.getSession();
        StringBuilder code = new StringBuilder();
        BufferedImage image = CaptchaUtil.generateCaptcha(code);

        session.setAttribute("captcha", code.toString());

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}