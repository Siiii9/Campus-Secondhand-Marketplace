package com.example.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("transaction")
public class Transaction {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long merchantId;

    private Long buyerId;

    private BigDecimal amount;

    private BigDecimal fee;

    private BigDecimal feeRate;

    private BigDecimal netAmount;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime settledAt;
}