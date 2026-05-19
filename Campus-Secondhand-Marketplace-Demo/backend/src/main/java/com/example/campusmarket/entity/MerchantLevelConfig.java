package com.example.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("merchant_level_config")
public class MerchantLevelConfig {

    @TableId(value = "level", type = IdType.AUTO)
    private Integer level;

    private BigDecimal feeRate;

    private BigDecimal minTransactionAmount;

    private BigDecimal minSatisfaction;
}