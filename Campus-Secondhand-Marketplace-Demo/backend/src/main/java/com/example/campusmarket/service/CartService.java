package com.example.campusmarket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.campusmarket.entity.Cart;
import com.example.campusmarket.entity.Product;
import com.example.campusmarket.mapper.CartMapper;
import com.example.campusmarket.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    public List<Cart> getCartByUser(Long userId) {
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<Cart> cartList = cartMapper.selectList(wrapper);

        System.out.println("====== 触发购物车列表查询，当前传入的 userId 为: " + userId + "，查出记录数: " + (cartList != null ? cartList.size() : 0) + " ======");

        if (cartList != null) {
            for (Cart cart : cartList) {
                if (cart.getProductId() != null) {
                    Product product = productMapper.selectById(cart.getProductId());
                    if (product != null) {
                        cart.setProductName(product.getName());
                        cart.setPrice(product.getDiscountPrice());
                    }
                }
            }
        }
        return cartList;
    }

    public boolean addToCart(Long userId, Long productId, Integer quantity) {
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("product_id", productId);
        Cart existingCart = cartMapper.selectOne(wrapper);
        
        if (existingCart != null) {
            existingCart.setQuantity(existingCart.getQuantity() + quantity);
            return cartMapper.updateById(existingCart) > 0;
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(quantity);
            cart.setSelected(1);
            cart.setCreatedAt(LocalDateTime.now());
            return cartMapper.insert(cart) > 0;
        }
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