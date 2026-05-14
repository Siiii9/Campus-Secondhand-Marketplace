package com.example.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("product_image")
public class ProductImage {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long productId;

    private String imageUrl;

    private Integer sortOrder;
}
