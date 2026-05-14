package com.example.campusmarket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusmarket.entity.User;
import com.example.campusmarket.entity.UserAuditLog;
import com.example.campusmarket.mapper.UserAuditLogMapper;
import com.example.campusmarket.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserAuditLogMapper userAuditLogMapper;

    public User findByUsername(String username) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        return userMapper.selectOne(wrapper);
    }

    public User findById(Long id) {
        return userMapper.selectById(id);
    }

    @Transactional
    public boolean register(User user, String businessLicense, String idCardFront, String idCardBack) {
        user.setStatus(0);
        user.setCreatedAt(LocalDateTime.now());
        if ("MERCHANT".equals(user.getRole())) {
            user.setMerchantLevel(1);
            user.setShopStatus(0);
        }
        int result = userMapper.insert(user);
        
        if (result > 0 && "MERCHANT".equals(user.getRole())) {
            UserAuditLog auditLog = new UserAuditLog();
            auditLog.setUserId(user.getId());
            auditLog.setBusinessLicense(businessLicense);
            auditLog.setIdCardFront(idCardFront);
            auditLog.setIdCardBack(idCardBack);
            auditLog.setAuditStatus(0);
            userAuditLogMapper.insert(auditLog);
        }
        return result > 0;
    }

    @Transactional
    public boolean auditUser(Long userId, Integer auditStatus, String remark, Long auditorId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        
        user.setStatus(auditStatus);
        userMapper.updateById(user);
        
        if ("MERCHANT".equals(user.getRole()) && auditStatus == 1) {
            user.setShopStatus(1);
            userMapper.updateById(user);
        }
        
        QueryWrapper<UserAuditLog> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        UserAuditLog auditLog = userAuditLogMapper.selectOne(wrapper);
        if (auditLog != null) {
            auditLog.setAuditStatus(auditStatus);
            auditLog.setAuditRemark(remark);
            auditLog.setAuditorId(auditorId);
            auditLog.setAuditTime(LocalDateTime.now());
            userAuditLogMapper.updateById(auditLog);
        }
        return true;
    }

    public boolean updateUser(User user) {
        return userMapper.updateById(user) > 0;
    }

    public boolean deleteUser(Long id) {
        return userMapper.deleteById(id) > 0;
    }

    public List<User> list(String keyword, String role) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("username", keyword)
                    .or()
                    .like("real_name", keyword)
                    .or()
                    .like("phone", keyword));
        }
        if (role != null && !role.isEmpty()) {
            wrapper.eq("role", role);
        }
        wrapper.orderByDesc("created_at");
        return userMapper.selectList(wrapper);
    }

    public List<User> getPendingUsers() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);
        wrapper.orderByAsc("created_at");
        return userMapper.selectList(wrapper);
    }

    public UserAuditLog getAuditLogByUserId(Long userId) {
        QueryWrapper<UserAuditLog> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return userAuditLogMapper.selectOne(wrapper);
    }

    @Transactional
    public boolean updateMerchantLevel(Long userId, Integer level) {
        User user = userMapper.selectById(userId);
        if (user == null || !"MERCHANT".equals(user.getRole())) {
            return false;
        }
        user.setMerchantLevel(level);
        return userMapper.updateById(user) > 0;
    }

    @Transactional
    public boolean closeShop(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || !"MERCHANT".equals(user.getRole())) {
            return false;
        }
        user.setShopStatus(0);
        return userMapper.updateById(user) > 0;
    }
}