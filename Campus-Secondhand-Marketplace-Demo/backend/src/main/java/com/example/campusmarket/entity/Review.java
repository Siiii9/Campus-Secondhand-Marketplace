package com.example.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String reviewType;

    private Long fromUserId;

    private Long toUserId;

    private Long productId;

    private Integer rating;

    private String content;

    private LocalDateTime createdAt;
}