package com.example.campusmarket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.campusmarket.entity.*;
import com.example.campusmarket.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private WalletMapper walletMapper;

    @Autowired
    private PointsMapper pointsMapper;

    @Autowired
    private PointsRecordMapper pointsRecordMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private MerchantLevelConfigMapper levelConfigMapper;

    @Autowired
    private UserMapper userMapper;

    @Transactional
    public Order createOrder(Long userId, List<Cart> cartItems) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        Order order = new Order();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", "").substring(0, 32));
        order.setUserId(userId);
        order.setStatus(0);
        order.setIsReturned(0);
        
        for (Cart item : cartItems) {
            Product product = productMapper.selectById(item.getProductId());
            if (product == null || product.getStock() < item.getQuantity()) {
                throw new RuntimeException("商品库存不足");
            }
            order.setMerchantId(product.getMerchantId());
            BigDecimal itemTotal = product.getDiscountPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }
        
        order.setTotalAmount(totalAmount);
        
        Points points = pointsMapper.selectById(userId);
        int availablePoints = points != null ? points.getPoints() : 0;
        int pointsToDeduct = Math.min(availablePoints, totalAmount.intValue() * 100);
        BigDecimal deductAmount = BigDecimal.valueOf(pointsToDeduct).divide(BigDecimal.valueOf(100));
        
        order.setPointsDeducted(pointsToDeduct);
        order.setPointsDeductAmount(deductAmount);
        BigDecimal actualPaid = totalAmount.subtract(deductAmount);
        order.setActualPaid(actualPaid);
        
        Wallet wallet = walletMapper.selectById(userId);
        if (wallet == null || wallet.getBalance().compareTo(actualPaid) < 0) {
            throw new RuntimeException("钱包余额不足");
        }
        
        wallet.setBalance(wallet.getBalance().subtract(actualPaid));
        walletMapper.updateById(wallet);
        
        if (pointsToDeduct > 0) {
            points.setPoints(points.getPoints() - pointsToDeduct);
            pointsMapper.updateById(points);
            
            PointsRecord record = new PointsRecord();
            record.setUserId(userId);
            record.setChangeAmount(-pointsToDeduct);
            record.setReason("抵扣现金");
            record.setCreatedAt(LocalDateTime.now());
            pointsRecordMapper.insert(record);
        }
        
        order.setStatus(1);
        order.setPaidAt(LocalDateTime.now());
        orderMapper.insert(order);
        
        for (Cart item : cartItems) {
            Product product = productMapper.selectById(item.getProductId());
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(item.getProductId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(product.getDiscountPrice());
            orderItemMapper.insert(orderItem);
            
            product.setStock(product.getStock() - item.getQuantity());
            product.setStatus(3);
            productMapper.updateById(product);
        }
        
        User merchant = userMapper.selectById(order.getMerchantId());
        int merchantLevel = merchant != null && merchant.getMerchantLevel() != null ? merchant.getMerchantLevel() : 1;
        MerchantLevelConfig config = levelConfigMapper.selectById(merchantLevel);
        BigDecimal feeRate = config != null ? config.getFeeRate() : BigDecimal.valueOf(0.001);
        BigDecimal fee = actualPaid.multiply(feeRate);
        
        Transaction transaction = new Transaction();
        transaction.setOrderId(order.getId());
        transaction.setMerchantId(order.getMerchantId());
        transaction.setBuyerId(userId);
        transaction.setAmount(actualPaid);
        transaction.setFee(fee);
        transaction.setFeeRate(feeRate);
        transaction.setNetAmount(actualPaid.subtract(fee));
        transaction.setStatus(0);
        transaction.setCreatedAt(LocalDateTime.now());
        transactionMapper.insert(transaction);
        
        Wallet merchantWallet = walletMapper.selectById(order.getMerchantId());
        if (merchantWallet == null) {
            merchantWallet = new Wallet();
            merchantWallet.setUserId(order.getMerchantId());
            merchantWallet.setBalance(BigDecimal.ZERO);
            merchantWallet.setFrozenBalance(BigDecimal.ZERO);
            walletMapper.insert(merchantWallet);
        }
        merchantWallet.setFrozenBalance(merchantWallet.getFrozenBalance().add(actualPaid.subtract(fee)));
        walletMapper.updateById(merchantWallet);
        
        return order;
    }

    public Order getOrderById(Long id) {
        return orderMapper.selectById(id);
    }

    public List<Order> getOrdersByUser(Long userId) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return orderMapper.selectList(wrapper);
    }

    @Transactional
    public boolean confirmReceipt(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 1) {
            return false;
        }
        
        order.setStatus(2);
        order.setReceivedAt(LocalDateTime.now());
        order.setReturnDeadline(LocalDateTime.now().plusHours(24));
        orderMapper.updateById(order);
        
        QueryWrapper<Transaction> transactionWrapper = new QueryWrapper<>();
        transactionWrapper.eq("order_id", orderId);
        Transaction transaction = transactionMapper.selectOne(transactionWrapper);
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
        
        Points points = pointsMapper.selectById(order.getUserId());
        if (points == null) {
            points = new Points();
            points.setUserId(order.getUserId());
            points.setPoints(0);
            pointsMapper.insert(points);
        }
        int earnedPoints = order.getActualPaid().intValue();
        points.setPoints(points.getPoints() + earnedPoints);
        pointsMapper.updateById(points);
        
        PointsRecord record = new PointsRecord();
        record.setUserId(order.getUserId());
        record.setChangeAmount(earnedPoints);
        record.setReason("购买商品");
        record.setOrderId(orderId);
        record.setCreatedAt(LocalDateTime.now());
        pointsRecordMapper.insert(record);
        
        return true;
    }

    @Scheduled(fixedRate = 86400000)
    @Transactional
    public void autoConfirmReceipt() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).lt("paid_at", sevenDaysAgo);
        List<Order> orders = orderMapper.selectList(wrapper);
        
        for (Order order : orders) {
            confirmReceipt(order.getId());
        }
    }
}