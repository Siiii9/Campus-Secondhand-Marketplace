package com.example.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String realName;

    private String phone;

    private String email;

    private String city;

    private String gender;

    private String bankAccount;

    private String role;

    private Integer status;

    private Integer auditStatus;

    private Integer merchantLevel;

    private String shopName;

    private Integer shopStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
