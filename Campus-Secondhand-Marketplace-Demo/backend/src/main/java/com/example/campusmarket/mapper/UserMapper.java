package com.example.campusmarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campusmarket.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}