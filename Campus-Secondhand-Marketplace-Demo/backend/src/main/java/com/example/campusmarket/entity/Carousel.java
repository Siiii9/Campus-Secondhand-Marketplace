package com.example.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("carousel")
public class Carousel {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String imageUrl;

    private String linkUrl;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createdAt;
}