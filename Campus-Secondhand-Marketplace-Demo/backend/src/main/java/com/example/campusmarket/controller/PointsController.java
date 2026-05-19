package com.example.campusmarket.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.campusmarket.dto.ApiResponse;
import com.example.campusmarket.entity.Points;
import com.example.campusmarket.entity.User;
import com.example.campusmarket.mapper.PointsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/points")
public class PointsController {

    @Autowired
    private PointsMapper pointsMapper;

    @GetMapping
    public ApiResponse<?> getPoints(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        QueryWrapper<Points> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", user.getId());
        Points points = pointsMapper.selectOne(queryWrapper);
        if (points == null) {
            points = new Points();
            points.setUserId(user.getId());
            points.setPoints(0);
            pointsMapper.insert(points);
        }
        return ApiResponse.success(points);
    }
}