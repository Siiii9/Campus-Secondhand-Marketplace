package com.example.campusmarket.service;

import com.example.campusmarket.entity.Order;
import com.example.campusmarket.entity.ReturnRequest;
import com.example.campusmarket.entity.Wallet;
import com.example.campusmarket.mapper.OrderMapper;
import com.example.campusmarket.mapper.ReturnRequestMapper;
import com.example.campusmarket.mapper.WalletMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ReturnService {

    @Autowired
    private ReturnRequestMapper returnRequestMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private WalletMapper walletMapper;

    public boolean createReturnRequest(Long orderId, Long userId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return false;
        }
        if (order.getStatus() != 2) {
            return false;
        }
        if (LocalDateTime.now().isAfter(order.getReturnDeadline())) {
            return false;
        }

        ReturnRequest request = new ReturnRequest();
        request.setOrderId(orderId);
        request.setUserId(userId);
        request.setReason(reason);
        request.setStatus(0);
        request.setCreatedAt(LocalDateTime.now());
        return returnRequestMapper.insert(request) > 0;
    }

    @Transactional
    public boolean auditReturn(Long requestId, Integer status, Long auditorId) {
        ReturnRequest request = returnRequestMapper.selectById(requestId);
        if (request == null || request.getStatus() != 0) {
            return false;
        }

        Order order = orderMapper.selectById(request.getOrderId());
        if (order == null || !order.getMerchantId().equals(auditorId)) {
            return false;
        }

        request.setStatus(status);
        request.setAuditorId(auditorId);
        request.setAuditTime(LocalDateTime.now());
        returnRequestMapper.updateById(request);

        if (status == 1) {
            order.setStatus(4);
            order.setIsReturned(1);
            orderMapper.updateById(order);

            Wallet buyerWallet = walletMapper.selectById(order.getUserId());
            if (buyerWallet != null) {
                buyerWallet.setBalance(buyerWallet.getBalance().add(order.getActualPaid()));
                walletMapper.updateById(buyerWallet);
            }

            Wallet merchantWallet = walletMapper.selectById(order.getMerchantId());
            if (merchantWallet != null) {
                merchantWallet.setFrozenBalance(merchantWallet.getFrozenBalance().subtract(order.getActualPaid()));
                walletMapper.updateById(merchantWallet);
            }
        }
        return true;
    }
}