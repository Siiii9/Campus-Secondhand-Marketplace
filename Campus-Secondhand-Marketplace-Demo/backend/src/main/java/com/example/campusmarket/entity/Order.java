package com.example.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("\"order\"")
public class Order {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private Long merchantId;

    private BigDecimal totalAmount;

    private Integer pointsDeducted;

    private BigDecimal pointsDeductAmount;

    private BigDecimal actualPaid;

    private Integer status;

    private LocalDateTime paidAt;

    private LocalDateTime receivedAt;

    private LocalDateTime returnDeadline;

    private Integer isReturned;

    @TableField(exist = false)
    private String logisticsCompany;

    @TableField(exist = false)
    private String trackingNumber;

    @TableField(exist = false)
    private LocalDateTime shippedAt;

    @TableField(exist = false)
    private LocalDateTime refundedAt;

    @TableField(exist = false)
    private List<OrderItem> items;
}