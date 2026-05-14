package com.example.campusmarket.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.campusmarket.entity.Order;
import com.example.campusmarket.entity.Transaction;
import com.example.campusmarket.entity.User;
import com.example.campusmarket.entity.Wallet;
import com.example.campusmarket.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderScheduler {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private WalletMapper walletMapper;

    @Autowired
    private PointsMapper pointsMapper;

    @Autowired
    private PointsRecordMapper pointsRecordMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void autoConfirmReceipt() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1)
               .lt("paid_at", sevenDaysAgo);
        
        List<Order> orders = orderMapper.selectList(wrapper);
        
        for (Order order : orders) {
            order.setStatus(2);
            order.setReceivedAt(LocalDateTime.now());
            order.setReturnDeadline(LocalDateTime.now().plusHours(24));
            orderMapper.updateById(order);
            
            QueryWrapper<Transaction> txWrapper = new QueryWrapper<>();
            txWrapper.eq("order_id", order.getId());
            Transaction transaction = transactionMapper.selectOne(txWrapper);
            
            if (transaction != null && transaction.getStatus() == 0) {
                transaction.setStatus(1);
                transaction.setSettledAt(LocalDateTime.now());
                transactionMapper.updateById(transaction);
                
                Wallet merchantWallet = walletMapper.selectById(order.getMerchantId());
                if (merchantWallet != null) {
                    merchantWallet.setFrozenBalance(merchantWallet.getFrozenBalance().subtract(transaction.getNetAmount()));
                    merchantWallet.setBalance(merchantWallet.getBalance().add(transaction.getNetAmount()));
                    walletMapper.updateById(merchantWallet);
                }
            }
            
            com.example.campusmarket.entity.Points points = pointsMapper.selectById(order.getUserId());
            if (points == null) {
                points = new com.example.campusmarket.entity.Points();
                points.setUserId(order.getUserId());
                points.setPoints(0);
                pointsMapper.insert(points);
            }
            int earnedPoints = order.getActualPaid().intValue();
            points.setPoints(points.getPoints() + earnedPoints);
            pointsMapper.updateById(points);
            
            com.example.campusmarket.entity.PointsRecord record = new com.example.campusmarket.entity.PointsRecord();
            record.setUserId(order.getUserId());
            record.setChangeAmount(earnedPoints);
            record.setReason("购买商品");
            record.setOrderId(order.getId());
            record.setCreatedAt(LocalDateTime.now());
            pointsRecordMapper.insert(record);
        }
    }
}