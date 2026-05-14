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

    private Long userId;

    private Long merchantId;

    private Long buyerId;

    private BigDecimal amount;

    private BigDecimal feeRate;

    private BigDecimal netAmount;

    private BigDecimal fee;

    private Integer type;

    private Integer status;

    private LocalDateTime settledAt;

    private LocalDateTime createdAt;
}
