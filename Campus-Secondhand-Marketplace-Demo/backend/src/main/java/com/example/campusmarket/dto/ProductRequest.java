package com.example.campusmarket.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {

    private String name;

    private Integer categoryId;

    private String description;

    private BigDecimal originalPrice;

    private BigDecimal discountPrice;

    private Integer stock;

    private String unit;

    private String conditionLevel;

    private Integer isNegotiable;

    private List<String> images;
}
