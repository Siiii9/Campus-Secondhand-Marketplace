package com.example.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_audit_log")
public class UserAuditLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String businessLicense;

    private String idCardFront;

    private String idCardBack;

    private Integer auditStatus;

    private String remark;

    private String auditRemark;

    private Long auditorId;

    private LocalDateTime auditTime;

    private LocalDateTime createdAt;
}
