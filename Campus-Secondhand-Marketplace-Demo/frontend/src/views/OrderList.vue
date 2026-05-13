<template>
  <div class="order-list">
    <h2>我的订单</h2>
    
    <div v-if="orders.length === 0" class="empty-orders">
      <p>暂无订单</p>
    </div>
    
    <div v-else>
      <div v-for="order in orders" :key="order.id" class="order-card">
        <div class="order-header">
          <span>订单号: {{ order.orderNo }}</span>
          <span class="order-status">{{ getStatusText(order.status) }}</span>
        </div>
        <div class="order-items">
          <div class="order-item">
            <img :src="'/images/default.jpg'" alt="">
            <div class="item-info">
              <div class="item-name">商品名称</div>
              <div class="item-price">¥{{ order.totalAmount }}</div>
            </div>
          </div>
        </div>
        <div class="order-footer">
          <span>实付: ¥{{ order.actualPaid }}</span>
          <div class="order-actions">
            <button v-if="order.status === 1" @click="confirmReceipt(order.id)">确认收货</button>
            <button v-if="order.status === 2 && canReturn(order)" @click="applyReturn(order.id)">申请退货</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const orders = ref<any[]>([])

onMounted(() => {
  axios.get('/api/orders').then(res => {
    orders.value = res.data.data
  })
})

const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '待付款',
    1: '待发货',
    2: '已收货',
    3: '退货申请中',
    4: '退货完成',
    5: '已完成'
  }
  return statusMap[status] || '未知'
}

const canReturn = (order: any) => {
  return order.returnDeadline && new Date(order.returnDeadline) > new Date()
}

const confirmReceipt = (orderId: string) => {
  axios.post(`/api/orders/${orderId}/confirm`).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('确认收货成功')
      const order = orders.value.find(o => o.id === orderId)
      if (order) {
        order.status = 2
      }
    }
  })
}

const applyReturn = (orderId: string) => {
  const reason = prompt('请输入退货原因')
  if (reason) {
    axios.post('/api/returns', { orderId, reason }).then(res => {
      if (res.data.code === 200) {
        ElMessage.success('退货申请提交成功')
        const order = orders.value.find(o => o.id === orderId)
        if (order) {
          order.status = 3
        }
      }
    })
  }
}
</script>

<style scoped>
.order-list {
  padding: 2rem;
}

.order-list h2 {
  margin-bottom: 1.5rem;
}

.empty-orders {
  text-align: center;
  padding: 4rem;
}

.order-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 1rem;
}

.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.order-status {
  color: #e74c3c;
}

.order-items {
  margin-bottom: 1rem;
}

.order-item {
  display: flex;
  gap: 1rem;
}

.order-item img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
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

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 1rem;
  border-top: 1px solid #eee;
}

.order-actions button {
  padding: 0.5rem 1rem;
  border: 1px solid #e74c3c;
  color: #e74c3c;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
  margin-left: 0.5rem;
}
</style>