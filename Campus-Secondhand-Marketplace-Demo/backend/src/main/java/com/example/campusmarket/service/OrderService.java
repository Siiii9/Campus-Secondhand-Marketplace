package com.example.campusmarket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.campusmarket.entity.*;
import com.example.campusmarket.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
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

        // 积分处理
        Points points = pointsMapper.selectById(userId);
        int availablePoints = points != null ? points.getPoints() : 0;
        int pointsToDeduct = Math.min(availablePoints, totalAmount.intValue() * 100);
        BigDecimal deductAmount = BigDecimal.valueOf(pointsToDeduct).divide(BigDecimal.valueOf(100));

        order.setPointsDeducted(pointsToDeduct);
        order.setPointsDeductAmount(deductAmount);
        BigDecimal actualPaid = totalAmount.subtract(deductAmount);
        order.setActualPaid(actualPaid);

        // 钱包扣款
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

        // 扣减库存、创建明细
        for (Cart item : cartItems) {
            Product product = productMapper.selectById(item.getProductId());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(item.getProductId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(product.getDiscountPrice());
            orderItemMapper.insert(orderItem);

            product.setStock(product.getStock() - item.getQuantity());
            product.setStatus(3); // 设为交易中/锁定状态
            productMapper.updateById(product);
        }

        // 计算手续费并进入商家冻结余额（中间账户）
        MerchantLevelConfig config = levelConfigMapper.selectById(1);
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
        transaction.setStatus(0); // 0=待结算
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

    /**
     * 【已修正 Bug 1】增加查询条件，只查当前登录用户的订单
     */
    public List<Order> getOrdersByUser(Long userId) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).orderByDesc("paid_at");
        return orderMapper.selectList(queryWrapper);
    }

    @Transactional
    public boolean confirmReceipt(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 1) {
            return false;
        }

        order.setStatus(2); // 2=已收货
        order.setReceivedAt(LocalDateTime.now());
        order.setReturnDeadline(LocalDateTime.now().plusHours(24)); // 24小时退货时限
        orderMapper.updateById(order);

        /**
         * 【已修正 Bug 2】增加精确条件，只结算跟当前订单绑定的流水记录
         */
        QueryWrapper<Transaction> transactionWrapper = new QueryWrapper<>();
        transactionWrapper.eq("order_id", orderId).eq("status", 0);
        Transaction transaction = transactionMapper.selectOne(transactionWrapper);

        if (transaction != null) {
            transaction.setStatus(1); // 1=已结算
            transaction.setSettledAt(LocalDateTime.now());
            transactionMapper.updateById(transaction);

            Wallet merchantWallet = walletMapper.selectById(order.getMerchantId());
            if (merchantWallet != null) {
                // 钱从商家的冻结余额转入到可用余额中
                merchantWallet.setFrozenBalance(merchantWallet.getFrozenBalance().subtract(transaction.getNetAmount()));
                merchantWallet.setBalance(merchantWallet.getBalance().add(transaction.getNetAmount()));
                walletMapper.updateById(merchantWallet);
            }
        }

        // 送积分：消费1元送1积分
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
}