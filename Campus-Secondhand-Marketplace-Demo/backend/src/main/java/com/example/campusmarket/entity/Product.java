package com.example.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long merchantId;

    private Integer categoryId;

    private String name;

    private String description;

    private BigDecimal originalPrice;

    private BigDecimal discountPrice;

    private Integer stock;

    private String unit;

    private Integer isNegotiable;

    private String conditionLevel;

    private Integer status;

    private Integer salesCount;

    private Integer viewCount;

    private BigDecimal avgRating;

    private Integer auditStatus;

    private LocalDateTime auditTime;

    private LocalDateTime createdAt;
}