<template>
  <div class="cart">
    <h2>我的购物车</h2>
    
    <div v-if="cartItems.length === 0" class="empty-cart">
      <p>购物车是空的</p>
      <button @click="$router.push('/')">去购物</button>
    </div>
    
    <div v-else>
      <div class="cart-list">
        <div v-for="item in cartItems" :key="item.id" class="cart-item">
          <input type="checkbox" :checked="item.selected === 1" @change="updateCart(item.id, null, $event.target.checked ? 1 : 0)">
          <img :src="getProductImage(item.productId)" alt="">
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

const getProductImage = (productId: string) => {
  return '/images/default.jpg'
}

const getTotal = (item: any) => {
  const price = item.price || item.discountPrice || 0
  return (item.quantity * price).toFixed(2)
}

const updateCart = (id: string, quantity: number | null, selected: number | null) => {
  // 先更新本地数据，让UI立即响应
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
  return cartItems.value.filter(item => item.selected === 1).reduce((sum, item) => sum + item.quantity, 0)
})

const totalPrice = computed(() => {
  const selectedItems = cartItems.value.filter(item => item.selected === 1)
  console.log('已勾选商品:', selectedItems)
  const total = selectedItems.reduce((sum, item) => {
    const price = Number(item.price) || Number(item.discountPrice) || 0
    const qty = Number(item.quantity) || 0
    return sum + qty * price
  }, 0)
  console.log('合计金额:', total)
  return total.toFixed(2)
})

const selectedItems = computed(() => {
  return cartItems.value.filter(item => item.selected === 1)
})

const checkout = () => {
  // 检查是否勾选了商品
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请至少选择一件商品')
    return
  }
  
  axios.post('/api/orders/cart', {}, { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('下单成功')
      setTimeout(() => {
        window.location.href = '/orders'
      }, 1500)
    } else {
      ElMessage.error(res.data.message)
    }
  }).catch(err => {
    ElMessage.error(err.response?.data?.message || '下单失败')
  })
}
</script>

<style scoped>
.cart {
  padding: 2rem;
}

.cart h2 {
  margin-bottom: 1.5rem;
}

.empty-cart {
  text-align: center;
  padding: 4rem;
}

.empty-cart button {
  padding: 0.75rem 1.5rem;
  background-color: #e74c3c;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 1rem;
}

.cart-list {
  margin-bottom: 1.5rem;
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 1rem;
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 1rem;
}

.cart-item img {
  width: 100px;
  height: 100px;
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
  margin-bottom: 0.5rem;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.quantity-control button {
  width: 25px;
  height: 25px;
  border: 1px solid #ddd;
  background: #fff;
  cursor: pointer;
}

.item-total {
  font-weight: bold;
  margin-right: 1rem;
}

.remove-btn {
  color: #999;
  border: none;
  background: none;
  cursor: pointer;
}

.cart-summary {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 1rem;
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
  padding: 0.75rem 2rem;
  background-color: #e74c3c;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
</style>