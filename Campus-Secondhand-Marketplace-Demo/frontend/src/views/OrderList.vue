<template>
  <div class="order-list">
    <div class="page-header">
      <button class="btn-back" @click="$router.push('/')">← 返回主页</button>
      <h2>我的订单</h2>
    </div>
    
    <div v-if="orders.length === 0" class="empty-orders">
      <p>暂无订单</p>
      <button @click="$router.push('/')">去购物</button>
    </div>
    
    <div v-else>
      <div v-for="order in orders" :key="order.id" class="order-card">
        <div class="order-header">
          <span>订单号: {{ order.orderNo }}</span>
          <span class="order-status">{{ getStatusText(order.status) }}</span>
        </div>
        <div class="order-items">
          <div v-for="(item, idx) in order.items" :key="item.id" class="order-item">
            <img :src="item.image || '/images/OIP-C.jpg'" alt="">
            <div class="item-info">
              <div class="item-name">{{ item.productName }}</div>
              <div class="item-price">¥{{ item.price }}</div>
              <div class="item-quantity">数量: {{ item.quantity }}</div>
            </div>
          </div>
        </div>
        <div class="order-footer">
          <span>实付: ¥{{ order.actualPaid }}</span>
          <div class="order-actions">
            <button v-if="order.status === 2" @click="confirmReceipt(order.id)">确认收货</button>
            <button v-if="order.status === 1 || order.status === 2" @click="applyRefund(order.id)">申请退款</button>
            <button v-if="order.status === 3 && canReturn(order)" @click="applyReturn(order.id)">申请退货</button>
            <button v-if="order.status === 2 && !order.reviewed" @click="openReviewModal(order)">评价商品</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showReviewModal" class="review-modal">
      <div class="modal-content">
        <h3>评价订单 {{ currentOrder?.orderNo }}</h3>
        
        <div v-for="(item, idx) in currentOrder?.items" :key="item.id" class="product-review">
          <h4>{{ item.productName }}</h4>
          <div class="star-rating">
            <span v-for="i in 5" :key="i" @click="setProductRating(idx, i)" :class="{ active: reviewForm.productRatings[idx] >= i }">★</span>
          </div>
          <textarea v-model="reviewForm.productReviews[idx]" placeholder="请输入商品评价（选填）"></textarea>
        </div>

        <div class="merchant-review">
          <h4>商家服务态度评价</h4>
          <div class="star-rating">
            <span v-for="i in 5" :key="i" @click="reviewForm.merchantRating = i" :class="{ active: reviewForm.merchantRating >= i }">★</span>
          </div>
        </div>

        <div class="modal-actions">
          <button @click="showReviewModal = false">取消</button>
          <button @click="submitReview">提交评价</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const orders = ref<any[]>([])
const showReviewModal = ref(false)
const currentOrder = ref<any>(null)

const reviewForm = reactive({
  productRatings: [] as number[],
  productReviews: [] as string[],
  merchantRating: 5
})

onMounted(() => {
  axios.get('/api/orders', { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      orders.value = res.data.data
    } else {
      ElMessage.error(res.data.message || '获取订单失败')
    }
  }).catch(err => {
    ElMessage.error('获取订单失败')
  })
})

const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '待付款',
    1: '待发货',
    2: '已发货',
    3: '已收货',
    4: '退货申请中',
    5: '已完成',
    6: '退款处理中'
  }
  return statusMap[status] || '未知'
}

const canReturn = (order: any) => {
  return order.returnDeadline && new Date(order.returnDeadline) > new Date()
}

const confirmReceipt = (orderId: number) => {
  axios.post(`/api/orders/${orderId}/confirm`, {}, { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('确认收货成功')
      const order = orders.value.find(o => o.id === orderId)
      if (order) {
        order.status = 2
      }
    } else {
      ElMessage.error(res.data.message || '确认收货失败')
    }
  }).catch(err => {
    ElMessage.error('确认收货失败')
  })
}

const applyReturn = (orderId: number) => {
  const reason = prompt('请输入退货原因')
  if (reason) {
    axios.post(`/api/orders/${orderId}/return`, { reason }, { withCredentials: true }).then(res => {
      if (res.data.code === 200) {
        ElMessage.success('退货申请提交成功')
        const order = orders.value.find(o => o.id === orderId)
        if (order) {
          order.status = 3
        }
      } else {
        ElMessage.error(res.data.message || '退货申请失败')
      }
    }).catch(err => {
      ElMessage.error('退货申请失败')
    })
  }
}

const applyRefund = (orderId: number) => {
  const reason = prompt('请输入退款原因')
  if (reason) {
    axios.post(`/api/orders/${orderId}/refund/apply`, { reason }, { withCredentials: true }).then(res => {
      if (res.data.code === 200) {
        ElMessage.success('退款申请已提交')
        const order = orders.value.find(o => o.id === orderId)
        if (order) {
          order.status = 6
        }
      } else {
        ElMessage.error(res.data.message || '退款申请失败')
      }
    }).catch(err => {
      ElMessage.error('退款申请失败')
    })
  }
}

const openReviewModal = (order: any) => {
  currentOrder.value = order
  reviewForm.productRatings = order.items.map(() => 5)
  reviewForm.productReviews = order.items.map(() => '')
  reviewForm.merchantRating = 5
  showReviewModal.value = true
}

const setProductRating = (idx: number, rating: number) => {
  reviewForm.productRatings[idx] = rating
}

const submitReview = () => {
  const order = currentOrder.value
  const promises: Promise<any>[] = []

  order.items.forEach((item: any, idx: number) => {
    promises.push(axios.post('/api/reviews', {
      orderId: order.id,
      productId: item.productId,
      rating: reviewForm.productRatings[idx],
      content: reviewForm.productReviews[idx],
      reviewType: 'PRODUCT'
    }, { withCredentials: true }))
  })

  promises.push(axios.post('/api/reviews', {
    orderId: order.id,
    toUserId: order.merchantId,
    rating: reviewForm.merchantRating,
    content: '',
    reviewType: 'MERCHANT_SERVICE'
  }, { withCredentials: true }))

  Promise.all(promises).then(() => {
    ElMessage.success('评价成功')
    showReviewModal.value = false
    const orderInList = orders.value.find(o => o.id === order.id)
    if (orderInList) {
      orderInList.reviewed = true
    }
  }).catch(() => {
    ElMessage.error('评价失败')
  })
}
</script>

<style scoped>
.order-list {
  padding: 2rem;
  max-width: 900px;
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

.empty-orders {
  text-align: center;
  padding: 4rem;
  background-color: #fff;
  border-radius: 8px;
}

.empty-orders p {
  color: #999;
  margin-bottom: 1rem;
}

.empty-orders button {
  padding: 0.5rem 2rem;
  background-color: #e74c3c;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
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
  padding: 0.5rem 0;
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

.item-quantity {
  font-size: 0.85rem;
  color: #999;
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

.order-actions button:hover {
  background-color: #e74c3c;
  color: #fff;
}

.review-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: #fff;
  padding: 2rem;
  border-radius: 8px;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-content h3 {
  margin-bottom: 1rem;
}

.product-review {
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #eee;
}

.product-review h4 {
  margin-bottom: 0.5rem;
}

.star-rating {
  font-size: 1.5rem;
  color: #ddd;
  cursor: pointer;
  margin-bottom: 0.5rem;
}

.star-rating span.active {
  color: #f5b041;
}

.product-review textarea {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: vertical;
}

.merchant-review {
  margin-bottom: 1rem;
}

.merchant-review h4 {
  margin-bottom: 0.5rem;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1rem;
}

.modal-actions button {
  padding: 0.5rem 1.5rem;
  border-radius: 4px;
  cursor: pointer;
}

.modal-actions button:first-child {
  background-color: #fff;
  border: 1px solid #ddd;
}

.modal-actions button:last-child {
  background-color: #e74c3c;
  color: #fff;
  border: none;
}
</style>
