package com.example.campusmarket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.campusmarket.entity.*;
import com.example.campusmarket.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private CartMapper cartMapper;

    @Autowired
    private ReturnRequestMapper returnRequestMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    @Transactional
    public Order createOrderFromCart(Long userId, boolean usePoints) {
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("selected", 1);
        List<Cart> cartItems = cartMapper.selectList(wrapper);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("请至少选择一件商品");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        Long merchantId = null;

        for (Cart item : cartItems) {
            Product product = productMapper.selectById(item.getProductId());
            if (product == null) {
                throw new RuntimeException("商品不存在");
            }
            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException("商品库存不足");
            }
            merchantId = product.getMerchantId();
            BigDecimal itemTotal = product.getDiscountPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        Order order = new Order();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", "").substring(0, 32));
        order.setUserId(userId);
        order.setMerchantId(merchantId);
        order.setTotalAmount(totalAmount);
        order.setStatus(0);
        order.setIsReturned(0);

        int pointsDeducted = 0;
        BigDecimal pointsDeductAmount = BigDecimal.ZERO;

        if (usePoints) {
            Points points = pointsMapper.selectById(userId);
            int availablePoints = points != null ? points.getPoints() : 0;
            int maxDeductPoints = totalAmount.intValue() * 100;
            pointsDeducted = Math.min(availablePoints, maxDeductPoints);
            pointsDeductAmount = BigDecimal.valueOf(pointsDeducted).divide(BigDecimal.valueOf(100));
        }

        order.setPointsDeducted(pointsDeducted);
        order.setPointsDeductAmount(pointsDeductAmount);
        BigDecimal actualPaid = totalAmount.subtract(pointsDeductAmount);
        order.setActualPaid(actualPaid);

        QueryWrapper<Wallet> walletWrapper = new QueryWrapper<>();
        walletWrapper.eq("user_id", userId);
        Wallet buyerWallet = walletMapper.selectOne(walletWrapper);
        if (buyerWallet == null) {
            buyerWallet = new Wallet();
            buyerWallet.setUserId(userId);
            buyerWallet.setBalance(BigDecimal.ZERO);
            buyerWallet.setFrozenBalance(BigDecimal.ZERO);
            walletMapper.insert(buyerWallet);
        }
        if (buyerWallet.getBalance().compareTo(actualPaid) < 0) {
            throw new RuntimeException("余额不足，请联系管理员充值");
        }

        buyerWallet.setBalance(buyerWallet.getBalance().subtract(actualPaid));
        buyerWallet.setFrozenBalance(buyerWallet.getFrozenBalance().add(actualPaid));
        walletMapper.updateById(buyerWallet);

        if (pointsDeducted > 0) {
            Points points = pointsMapper.selectById(userId);
            if (points != null) {
                points.setPoints(points.getPoints() - pointsDeducted);
                pointsMapper.updateById(points);

                PointsRecord record = new PointsRecord();
                record.setUserId(userId);
                record.setChangeAmount(-pointsDeducted);
                record.setReason("抵扣现金");
                record.setCreatedAt(LocalDateTime.now());
                pointsRecordMapper.insert(record);
            }
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
            productMapper.updateById(product);
        }

        cartMapper.delete(wrapper);

        return order;
    }

    public Order getOrderById(Long id) {
        return orderMapper.selectById(id);
    }

    public List<Order> getOrdersByUser(Long userId) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).orderByDesc("paid_at");
        List<Order> orders = orderMapper.selectList(queryWrapper);
        for (Order order : orders) {
            QueryWrapper<OrderItem> itemWrapper = new QueryWrapper<>();
            itemWrapper.eq("order_id", order.getId());
            List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
            for (OrderItem item : items) {
                Product product = productMapper.selectById(item.getProductId());
                if (product != null) {
                    item.setProductName(product.getName());
                }
            }
            order.setItems(items);
        }
        return orders;
    }

    public List<Order> getOrdersByMerchant(Long merchantId) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("merchant_id", merchantId).orderByDesc("paid_at");
        List<Order> orders = orderMapper.selectList(queryWrapper);
        for (Order order : orders) {
            QueryWrapper<OrderItem> itemWrapper = new QueryWrapper<>();
            itemWrapper.eq("order_id", order.getId());
            List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
            for (OrderItem item : items) {
                Product product = productMapper.selectById(item.getProductId());
                if (product != null) {
                    item.setProductName(product.getName());
                }
            }
            order.setItems(items);
        }
        return orders;
    }

    @Transactional
    public boolean confirmReceipt(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 2) {
            return false;
        }

        order.setStatus(3);
        order.setReceivedAt(LocalDateTime.now());
        order.setReturnDeadline(LocalDateTime.now().plusHours(24));
        orderMapper.updateById(order);

        QueryWrapper<Wallet> buyerWalletWrapper = new QueryWrapper<>();
        buyerWalletWrapper.eq("user_id", order.getUserId());
        Wallet buyerWallet = walletMapper.selectOne(buyerWalletWrapper);
        if (buyerWallet != null) {
            buyerWallet.setFrozenBalance(buyerWallet.getFrozenBalance().subtract(order.getActualPaid()));
            walletMapper.updateById(buyerWallet);
        }

        MerchantLevelConfig config = levelConfigMapper.selectById(1);
        BigDecimal feeRate = config != null ? config.getFeeRate() : BigDecimal.valueOf(0.001);
        BigDecimal fee = order.getActualPaid().multiply(feeRate);
        BigDecimal netAmount = order.getActualPaid().subtract(fee);

        Transaction transaction = new Transaction();
        transaction.setOrderId(order.getId());
        transaction.setMerchantId(order.getMerchantId());
        transaction.setBuyerId(order.getUserId());
        transaction.setAmount(order.getActualPaid());
        transaction.setFee(fee);
        transaction.setFeeRate(feeRate);
        transaction.setNetAmount(netAmount);
        transaction.setStatus(1);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setSettledAt(LocalDateTime.now());
        transactionMapper.insert(transaction);

        QueryWrapper<Wallet> merchantWalletWrapper = new QueryWrapper<>();
        merchantWalletWrapper.eq("user_id", order.getMerchantId());
        Wallet merchantWallet = walletMapper.selectOne(merchantWalletWrapper);
        if (merchantWallet == null) {
            merchantWallet = new Wallet();
            merchantWallet.setUserId(order.getMerchantId());
            merchantWallet.setBalance(BigDecimal.ZERO);
            merchantWallet.setFrozenBalance(BigDecimal.ZERO);
            walletMapper.insert(merchantWallet);
        }
        merchantWallet.setBalance(merchantWallet.getBalance().add(netAmount));
        walletMapper.updateById(merchantWallet);

        QueryWrapper<Points> pointsWrapper = new QueryWrapper<>();
        pointsWrapper.eq("user_id", order.getUserId());
        Points points = pointsMapper.selectOne(pointsWrapper);
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

    @Transactional
    public boolean applyReturn(Long orderId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 2) {
            return false;
        }

        if (LocalDateTime.now().isAfter(order.getReturnDeadline())) {
            return false;
        }

        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setOrderId(orderId);
        returnRequest.setUserId(order.getUserId());
        returnRequest.setReason(reason);
        returnRequest.setStatus(0);
        returnRequest.setCreatedAt(LocalDateTime.now());
        returnRequestMapper.insert(returnRequest);

        order.setStatus(3);
        orderMapper.updateById(order);

        return true;
    }

    @Transactional
    public boolean shipOrder(Long orderId, String logisticsCompany, String trackingNumber) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 1) {
            return false;
        }

        order.setStatus(2);
        orderMapper.updateById(order);

        return true;
    }

    @Transactional
    public boolean refundOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 4) {
            return false;
        }

        Wallet buyerWallet = walletMapper.selectById(order.getUserId());
        if (buyerWallet != null) {
            buyerWallet.setBalance(buyerWallet.getBalance().add(order.getActualPaid()));
            walletMapper.updateById(buyerWallet);
        }

        order.setStatus(5);
        orderMapper.updateById(order);

        return true;
    }

    @Transactional
    public boolean approveReturn(Long returnId, String rejectReason) {
        ReturnRequest returnRequest = returnRequestMapper.selectById(returnId);
        if (returnRequest == null || returnRequest.getStatus() != 0) {
            return false;
        }

        Order order = orderMapper.selectById(returnRequest.getOrderId());
        if (order == null) {
            return false;
        }

        if (rejectReason != null && !rejectReason.isEmpty()) {
            returnRequest.setStatus(2);
            order.setStatus(2);
        } else {
            returnRequest.setStatus(1);
            order.setStatus(4);

            Wallet buyerWallet = walletMapper.selectById(order.getUserId());
            if (buyerWallet != null) {
                buyerWallet.setBalance(buyerWallet.getBalance().add(order.getActualPaid()));
                walletMapper.updateById(buyerWallet);
            }

            Wallet merchantWallet = walletMapper.selectById(order.getMerchantId());
            if (merchantWallet != null) {
                BigDecimal fee = order.getActualPaid().multiply(BigDecimal.valueOf(0.001));
                merchantWallet.setBalance(merchantWallet.getBalance().subtract(order.getActualPaid().subtract(fee)));
                walletMapper.updateById(merchantWallet);
            }
        }

        returnRequest.setAuditTime(LocalDateTime.now());
        returnRequestMapper.updateById(returnRequest);
        orderMapper.updateById(order);

        return true;
    }

    public List<ReturnRequest> getReturnRequestsByMerchant(Long merchantId) {
        QueryWrapper<ReturnRequest> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);
        List<ReturnRequest> requests = returnRequestMapper.selectList(wrapper);

        List<ReturnRequest> merchantRequests = new ArrayList<>();
        for (ReturnRequest request : requests) {
            Order order = orderMapper.selectById(request.getOrderId());
            if (order != null && order.getMerchantId().equals(merchantId)) {
                merchantRequests.add(request);
            }
        }
        return merchantRequests;
    }

    @Transactional
    public Review submitReview(Long orderId, Integer rating, String content, String reviewType) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 5) {
            return null;
        }

        Review review = new Review();
        review.setOrderId(orderId);
        review.setReviewType(reviewType);
        review.setFromUserId(order.getUserId());
        review.setToUserId(order.getMerchantId());
        review.setProductId(order.getMerchantId());

        OrderItem orderItem = orderItemMapper.selectOne(new QueryWrapper<OrderItem>().eq("order_id", orderId));
        if (orderItem != null) {
            review.setProductId(orderItem.getProductId());
        }

        review.setRating(rating);
        review.setContent(content);
        review.setCreatedAt(LocalDateTime.now());
        reviewMapper.insert(review);

        if ("PRODUCT".equals(reviewType) && review.getProductId() != null) {
            QueryWrapper<Review> wrapper = new QueryWrapper<>();
            wrapper.eq("product_id", review.getProductId());
            List<Review> productReviews = reviewMapper.selectList(wrapper);

            if (!productReviews.isEmpty()) {
                double avgRating = productReviews.stream()
                        .mapToInt(Review::getRating)
                        .average()
                        .orElse(0);
                Product product = productMapper.selectById(review.getProductId());
                if (product != null) {
                    product.setAvgRating(BigDecimal.valueOf(avgRating));
                    productMapper.updateById(product);
                }
            }
        }

        return review;
    }

    @Transactional
    public boolean replyReview(Long reviewId, String reply) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            return false;
        }
        review.setReply(reply);
        reviewMapper.updateById(review);
        return true;
    }

    public List<Review> getReviewsByMerchant(Long merchantId) {
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("to_user_id", merchantId).orderByDesc("created_at");
        return reviewMapper.selectList(wrapper);
    }
}