package com.example.campusmarket.dto;

import lombok.Data;

@Data
public class RegisterRequest {

    private String username;

    private String password;

    private String realName;

    private String phone;

    private String email;

    private String city;

    private String gender;

    private String bankAccount;

    private String role;

    private String shopName;

    private String captcha;
}