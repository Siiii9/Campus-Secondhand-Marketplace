<template>
  <div class="cart">
    <div class="page-header">
      <button class="btn-back" @click="$router.push('/')">← 返回主页</button>
      <h2>我的购物车</h2>
    </div>
    
    <div v-if="cartItems.length === 0" class="empty-cart">
      <p>购物车是空的</p>
      <button @click="$router.push('/')">去购物</button>
    </div>
    
    <div v-else>
      <div class="cart-list">
        <div v-for="item in cartItems" :key="item.id" class="cart-item">
          <input type="checkbox" :checked="item.selected === 1" @change="updateCart(item.id, null, $event.target.checked ? 1 : 0)">
          <img :src="item.image || '/images/OIP-C.jpg'" alt="商品图片">
          <div class="item-info">
            <div class="item-name">{{ item.productName || '商品名称' }}</div>
            <div class="item-price">¥{{ item.price || item.discountPrice || 0 }}</div>
            <div class="quantity-control">
              <button @click="updateQuantity(item, -1)" :disabled="item.quantity <= 1">-</button>
              <span>{{ item.quantity }}</span>
              <button @click="updateQuantity(item, 1)">+</button>
            </div>
          </div>
          <div class="item-total">¥{{ getTotal(item) }}</div>
          <button class="remove-btn" @click="removeItem(item.id)">删除</button>
        </div>
      </div>
      
      <div class="cart-summary">
        <div>已选 {{ selectedCount }} 件商品</div>
        <div>合计: <span class="total-price">¥{{ totalPrice }}</span></div>
        <button class="checkout-btn" @click="checkout">一键下单</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const cartItems = ref<any[]>([])

onMounted(() => {
  axios.get('/api/cart', { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      cartItems.value = res.data.data
      console.log('购物车数据:', cartItems.value)
    } else {
      ElMessage.error(res.data.message)
    }
  }).catch(err => {
    console.error('获取购物车失败:', err)
    ElMessage.error('获取购物车失败')
  })
})

const getTotal = (item: any) => {
  const price = item.price || item.discountPrice || 0
  return (item.quantity * price).toFixed(2)
}

const updateCart = (id: string, quantity: number | null, selected: number | null) => {
  const item = cartItems.value.find(item => item.id === id)
  if (item) {
    if (quantity !== null) {
      item.quantity = quantity
    }
    if (selected !== null) {
      item.selected = selected
    }
  }
  
  axios.put(`/api/cart/${id}`, {}, {
    params: { quantity, selected },
    withCredentials: true
  }).then(res => {
    if (res.data.code !== 200) {
      ElMessage.error('更新失败')
    }
  })
}

const updateQuantity = (item: any, delta: number) => {
  const newQuantity = item.quantity + delta
  if (newQuantity >= 1) {
    item.quantity = newQuantity
    updateCart(item.id, newQuantity, null)
  }
}

const removeItem = (id: string) => {
  axios.delete(`/api/cart/${id}`, { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      cartItems.value = cartItems.value.filter(item => item.id !== id)
      ElMessage.success('删除成功')
    }
  })
}

const selectedCount = computed(() => {
  return cartItems.value.filter(item => item.selected === 1).length
})

const totalPrice = computed(() => {
  return cartItems.value
    .filter(item => item.selected === 1)
    .reduce((sum, item) => {
      const price = item.price || item.discountPrice || 0
      return sum + item.quantity * price
    }, 0).toFixed(2)
})

const checkout = () => {
  if (selectedCount.value === 0) {
    ElMessage.warning('请先选择商品')
    return
  }
  axios.post('/api/orders/cart', {}, { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('下单成功')
      cartItems.value = cartItems.value.filter(item => item.selected !== 1)
    } else {
      ElMessage.error(res.data.message || '下单失败')
    }
  })
}
</script>

<style scoped>
.cart {
  padding: 2rem;
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 1.5rem;
}

.btn-back {
  padding: 0.5rem 1rem;
  background-color: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 1rem;
}

.btn-back:hover {
  background-color: #f5f5f5;
}

h2 {
  margin: 0;
}

.empty-cart {
  text-align: center;
  padding: 3rem;
  background-color: #fff;
  border-radius: 8px;
}

.empty-cart p {
  color: #999;
  margin-bottom: 1rem;
}

.empty-cart button {
  padding: 0.5rem 2rem;
  background-color: #e74c3c;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.cart-list {
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid #eee;
}

.cart-item:last-child {
  border-bottom: none;
}

.cart-item input[type="checkbox"] {
  margin-right: 1rem;
}

.cart-item img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 1rem;
}

.item-info {
  flex: 1;
}

.item-name {
  font-weight: bold;
  margin-bottom: 0.5rem;
}

.item-price {
  color: #e74c3c;
}

.quantity-control {
  display: flex;
  align-items: center;
  margin-top: 0.5rem;
}

.quantity-control button {
  width: 24px;
  height: 24px;
  border: 1px solid #ddd;
  background: #fff;
  cursor: pointer;
}

.quantity-control span {
  padding: 0 0.5rem;
}

.item-total {
  font-size: 1.2rem;
  font-weight: bold;
  color: #e74c3c;
  margin: 0 1rem;
}

.remove-btn {
  padding: 0.3rem 0.8rem;
  background-color: #ff4d4f;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.cart-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1rem;
  padding: 1rem;
  background-color: #fff;
  border-radius: 8px;
}

.total-price {
  font-size: 1.5rem;
  color: #e74c3c;
  font-weight: bold;
}

.checkout-btn {
  padding: 0.8rem 2rem;
  background-color: #e74c3c;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 1.1rem;
  cursor: pointer;
}

.checkout-btn:hover {
  background-color: #c0392b;
}
</style>
