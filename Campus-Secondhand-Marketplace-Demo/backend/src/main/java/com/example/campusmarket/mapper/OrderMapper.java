package com.example.campusmarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campusmarket.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
