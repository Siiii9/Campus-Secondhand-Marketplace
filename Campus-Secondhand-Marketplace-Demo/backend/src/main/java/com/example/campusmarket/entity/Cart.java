package com.example.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("cart")
public class Cart {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 强行对齐前端的 user_id
    @JsonProperty("user_id")
    private Long userId;

    // 强行对齐前端的 product_id
    @JsonProperty("product_id")
    private Long productId;

    private Integer quantity;

    private Integer selected;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    // ==========================================
    // ✨ 以下为学生 B 专门为前端展示追加的“非数据库映射字段”
    // ==========================================

    // 1. 商品名称（迎合前端 item.product_name）
    @TableField(exist = false)
    @JsonProperty("product_name")
    private String productName;

    // 2. 商品实际价格（迎合前端 item.price）
    @TableField(exist = false)
    private BigDecimal price;

    // 3. 商品主图链接（迎合前端 item.image）
    @TableField(exist = false)
    private String image;
}