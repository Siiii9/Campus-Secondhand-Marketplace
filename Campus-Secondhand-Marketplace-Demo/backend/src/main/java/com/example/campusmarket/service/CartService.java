package com.example.campusmarket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.campusmarket.entity.Cart;
import com.example.campusmarket.mapper.CartMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartMapper cartMapper;

    public List<Cart> getCartByUser(Long userId) {
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return cartMapper.selectList(wrapper);
    }

    public boolean addToCart(Long userId, Long productId, Integer quantity) {
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("product_id", productId);
        Cart existing = cartMapper.selectOne(wrapper);
        
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            return cartMapper.updateById(existing) > 0;
        }
        
        Cart newCart = new Cart();
        newCart.setUserId(userId);
        newCart.setProductId(productId);
        newCart.setQuantity(quantity);
        newCart.setSelected(1);
        newCart.setCreatedAt(LocalDateTime.now());
        return cartMapper.insert(newCart) > 0;
    }

    public boolean updateCart(Long id, Integer quantity, Integer selected) {
        Cart cart = cartMapper.selectById(id);
        if (cart == null) {
            return false;
        }
        if (quantity != null) {
            cart.setQuantity(quantity);
        }
        if (selected != null) {
            cart.setSelected(selected);
        }
        return cartMapper.updateById(cart) > 0;
    }

    public boolean removeFromCart(Long id) {
        return cartMapper.deleteById(id) > 0;
    }

    public boolean clearCart(Long userId) {
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return cartMapper.delete(wrapper) > 0;
    }
}