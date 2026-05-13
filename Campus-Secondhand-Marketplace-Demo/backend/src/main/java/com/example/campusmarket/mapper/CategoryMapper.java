package com.example.campusmarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campusmarket.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}