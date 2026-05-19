package com.example.campusmarket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.campusmarket.entity.*;
import com.example.campusmarket.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReturnService {

    @Autowired
    private ReturnRequestMapper returnRequestMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private WalletMapper walletMapper;

    @Autowired
    private PointsMapper pointsMapper;

    @Autowired
    private PointsRecordMapper pointsRecordMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    /**
     * 买家申请退货
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean createReturnRequest(Long orderId, Long userId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return false;
        }
        if (order.getStatus() != 3) { // 必须是 3=已收货 状态才能申请退货
            return false;
        }
        if (LocalDateTime.now().isAfter(order.getReturnDeadline())) { // 检查24小时时限
            return false;
        }

        // 1. 必须将订单状态修改为 4=退货申请中
        order.setStatus(4);
        orderMapper.updateById(order);

        // 2. 插入退货申请单
        ReturnRequest request = new ReturnRequest();
        request.setOrderId(orderId);
        request.setUserId(userId);
        request.setReason(reason);
        request.setStatus(0); // 0=待商家审核
        request.setCreatedAt(LocalDateTime.now());
        return returnRequestMapper.insert(request) > 0;
    }

    /**
     * 商家/管理员审核退货
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean auditReturn(Long requestId, Integer status) {
        ReturnRequest request = returnRequestMapper.selectById(requestId);
        if (request == null || request.getStatus() != 0) {
            return false;
        }

        // 更新退货单审核状态 (1=通过，2=拒绝)
        request.setStatus(status);
        request.setAuditTime(LocalDateTime.now());
        returnRequestMapper.updateById(request);

        Order order = orderMapper.selectById(request.getOrderId());
        if (order == null) {
            return false;
        }

        if (status == 1) { // 审核通过
            order.setStatus(4); // 4=退货完成
            order.setIsReturned(1);
            orderMapper.updateById(order);

            // 1. 退款给买家（实际支付金额）
            Wallet buyerWallet = walletMapper.selectById(order.getUserId());
            if (buyerWallet != null) {
                buyerWallet.setBalance(buyerWallet.getBalance().add(order.getActualPaid()));
                walletMapper.updateById(buyerWallet);
            }

            // 2. 扣减商家的账目（平齐手续费和净得金额）
            Wallet merchantWallet = walletMapper.selectById(order.getMerchantId());
            if (merchantWallet != null) {
                // 精确获取对应的交易流水记录
                QueryWrapper<Transaction> qw = new QueryWrapper<>();
                qw.eq("order_id", order.getId());
                Transaction transaction = transactionMapper.selectOne(qw);

                if (transaction != null) {
                    if (transaction.getStatus() == 1) {
                        // 如果已经结算过了，扣除可提现的可用余额
                        merchantWallet.setBalance(merchantWallet.getBalance().subtract(transaction.getNetAmount()));
                    } else {
                        // 如果还在24小时未结算期间，扣除对应的冻结余额
                        merchantWallet.setFrozenBalance(merchantWallet.getFrozenBalance().subtract(transaction.getNetAmount()));
                    }
                    walletMapper.updateById(merchantWallet);

                    // 同步将流水状态更新为已取消/退款，防止二次结算
                    transaction.setStatus(2); // 假设2代表由于退货导致的交易作废
                    transactionMapper.updateById(transaction);
                }
            }

            // 3. 扣回买家因本次购物而非法赠送的积分，防止刷分
            Points points = pointsMapper.selectById(order.getUserId());
            int pointsToDeduct = order.getActualPaid().intValue(); // 1元=1积分
            if (points != null && points.getPoints() >= pointsToDeduct) {
                points.setPoints(points.getPoints() - pointsToDeduct);
                pointsMapper.updateById(points);

                // 记入积分变动流水
                PointsRecord record = new PointsRecord();
                record.setUserId(order.getUserId());
                record.setChangeAmount(-pointsToDeduct);
                record.setReason("退货扣回赠送积分");
                record.setOrderId(order.getId());
                record.setCreatedAt(LocalDateTime.now());
                pointsRecordMapper.insert(record);
            }

        } else if (status == 2) { // 审核拒绝
            // 订单状态重新复原为 2=已收货
            order.setStatus(2);
            orderMapper.updateById(order);
        }

        return true;
    }

    /**
     * 补充：获取全部退货申请列表（方便管理端或卖家端审查）
     */
    public List<ReturnRequest> getReturnList() {
        return returnRequestMapper.selectList(null);
    }
}