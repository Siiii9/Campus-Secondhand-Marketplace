package com.example.campusmarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campusmarket.entity.Transaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransactionMapper extends BaseMapper<Transaction> {
}