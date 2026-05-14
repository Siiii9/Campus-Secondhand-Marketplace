package com.example.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
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
}