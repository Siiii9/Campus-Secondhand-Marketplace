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

    private String name;

    private Integer categoryId;

    private String description;

    private BigDecimal originalPrice;

    private BigDecimal discountPrice;

    private Integer stock;

    private String unit;

    private String conditionLevel;

    private Integer isNegotiable;

    private Long merchantId;

    private Integer status;

    private Integer auditStatus;

    private Integer salesCount;

    private Integer viewCount;

    private Double avgRating;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime auditTime;
}
