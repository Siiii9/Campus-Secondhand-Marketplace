package com.example.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("merchant_level_config")
public class MerchantLevelConfig {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Integer level;

    private BigDecimal feeRate;

    private String description;
}
