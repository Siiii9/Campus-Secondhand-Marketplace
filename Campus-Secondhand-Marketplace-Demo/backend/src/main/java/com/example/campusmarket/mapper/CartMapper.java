package com.example.campusmarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campusmarket.entity.Cart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartMapper extends BaseMapper<Cart> {
}