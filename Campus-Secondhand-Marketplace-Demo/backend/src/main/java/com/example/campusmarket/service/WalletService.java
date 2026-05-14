package com.example.campusmarket.service;

import com.example.campusmarket.entity.Wallet;
import com.example.campusmarket.mapper.WalletMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class WalletService {

    @Autowired
    private WalletMapper walletMapper;

    public Wallet getWalletByUserId(Long userId) {
        Wallet wallet = walletMapper.selectById(userId);
        if (wallet == null) {
            wallet = new Wallet();
            wallet.setUserId(userId);
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setFrozenBalance(BigDecimal.ZERO);
            wallet.setUpdatedAt(LocalDateTime.now());
            walletMapper.insert(wallet);
        }
        return wallet;
    }

    public boolean recharge(Long userId, BigDecimal amount) {
        Wallet wallet = getWalletByUserId(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        return walletMapper.updateById(wallet) > 0;
    }

    public boolean adminRecharge(Long userId, BigDecimal amount) {
        Wallet wallet = getWalletByUserId(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        return walletMapper.updateById(wallet) > 0;
    }

    public boolean deduct(Long userId, BigDecimal amount) {
        Wallet wallet = getWalletByUserId(userId);
        if (wallet.getBalance().compareTo(amount) < 0) {
            return false;
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        return walletMapper.updateById(wallet) > 0;
    }
}