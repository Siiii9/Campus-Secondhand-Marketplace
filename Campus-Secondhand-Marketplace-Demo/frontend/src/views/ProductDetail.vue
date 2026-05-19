<template>
  <div class="product-detail">
    <button class="btn-back" @click="$router.push('/')">← 返回主页</button>
    
    <div class="product-main">
      <div class="product-images">
        <img v-if="product.images && product.images.length > 0" :src="product.images[currentImage]" alt="" class="main-image">
        <img v-else src="/images/OIP-C.jpg" alt="" class="main-image">
        <div class="image-thumbs" v-if="product.images && product.images.length > 1">
          <img v-for="(img, index) in product.images" :key="index" :src="img" @click="currentImage = index" :class="{ active: currentImage === index }">
        </div>
      </div>
      
      <div class="product-info">
        <h1>{{ product.name }}</h1>
        <div class="price-section">
          <span class="discount-price">¥{{ product.discountPrice }}</span>
          <span class="original-price">¥{{ product.originalPrice }}</span>
        </div>
        <div class="product-meta">
          <span>库存: {{ product.stock }}</span>
          <span>销量: {{ product.salesCount }}</span>
          <span>评分: {{ product.avgRating || 0 }}</span>
        </div>
        <div class="specs">
          <div>新旧程度: {{ product.conditionLevel }}</div>
          <div>尺寸: {{ product.unit || '未填写' }}</div>
          <div>是否议价: {{ product.isNegotiable === 1 ? '是' : '否' }}</div>
        </div>
        <div class="description">{{ product.description }}</div>
        
        <div class="merchant-info" @click="goToShop">
          <div class="merchant-name">{{ merchantInfo.shopName || '商家店铺' }}</div>
          <div class="merchant-level">商家等级: 等级{{ merchantInfo.merchantLevel }}</div>
          <span class="view-shop">查看店铺 →</span>
        </div>

        <div class="quantity-section">
          <label>数量:</label>
          <button @click="quantity--" :disabled="quantity <= 1">-</button>
          <span>{{ quantity }}</span>
          <button @click="quantity++" :disabled="quantity >= product.stock">+</button>
        </div>
        <div class="action-buttons">
          <button class="add-cart-btn" @click="addToCart">加入购物车</button>
          <button class="buy-now-btn" @click="buyNow">立即购买</button>
        </div>
      </div>
    </div>

    <div class="reviews-section">
      <h3>用户评价 ({{ reviews.length }})</h3>
      <div v-if="reviews.length === 0" class="no-reviews">暂无评价</div>
      <div v-for="review in reviews" :key="review.id" class="review-item">
        <div class="review-header">
          <span class="reviewer">{{ review.fromUserId }}</span>
          <span class="rating">{{ getStarRating(review.rating) }}</span>
        </div>
        <div class="review-content">{{ review.content }}</div>
        <div class="review-time">{{ formatTime(review.createdAt) }}</div>
      </div>

      <div class="review-form" v-if="isLoggedIn">
        <h4>发表评价</h4>
        <select v-model="newReview.rating">
          <option :value="1">★ 1星</option>
          <option :value="2">★★ 2星</option>
          <option :value="3">★★★ 3星</option>
          <option :value="4">★★★★ 4星</option>
          <option :value="5">★★★★★ 5星</option>
        </select>
        <textarea v-model="newReview.content" placeholder="请输入评价内容"></textarea>
        <button class="submit-review-btn" @click="submitReview">提交评价</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const productId = ref(route.params.id as string)

const product = ref({
  id: '',
  name: '',
  originalPrice: 0,
  discountPrice: 0,
  stock: 0,
  salesCount: 0,
  avgRating: 0,
  description: '',
  conditionLevel: '',
  unit: '',
  isNegotiable: 0,
  images: [] as string[],
  merchantId: 0
})

const merchantInfo = ref({
  shopName: '',
  merchantLevel: 1
})

const currentImage = ref(0)
const quantity = ref(1)
const reviews = ref<any[]>([])
const isLoggedIn = ref(false)

const newReview = reactive({
  rating: 5,
  content: ''
})

onMounted(() => {
  isLoggedIn.value = localStorage.getItem('userToken') !== null
  loadProduct()
})

const loadProduct = () => {
  axios.get(`/api/products/${productId.value}/detail`, { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      const data = res.data.data
      product.value = data.product
      product.value.images = data.images || []
      loadMerchantInfo(product.value.merchantId)
    } else {
      ElMessage.error(res.data.message || '获取商品详情失败')
    }
  }).catch(err => {
    console.error('获取商品详情错误:', err)
    ElMessage.error('服务器错误: ' + (err.response?.data?.message || err.message || '请稍后重试'))
  })
  
  axios.get(`/api/reviews/product/${productId.value}`).then(res => {
    if (res.data.code === 200) {
      reviews.value = res.data.data
    }
  }).catch(err => {
    console.error('获取评论错误:', err)
  })
}

const loadMerchantInfo = (merchantId: number) => {
  axios.get(`/api/users/${merchantId}`).then(res => {
    if (res.data.code === 200) {
      const merchant = res.data.data
      merchantInfo.value.shopName = merchant.shopName || '未命名店铺'
      merchantInfo.value.merchantLevel = merchant.merchantLevel || 1
    }
  })
}

const goToShop = () => {
  router.push(`/shop/${product.value.merchantId}`)
}

const addToCart = () => {
  axios.post(`/api/cart?productId=${productId.value}&quantity=${quantity.value}`, {}, { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('添加成功')
    }
  })
}

const buyNow = () => {
  axios.post(`/api/cart?productId=${productId.value}&quantity=${quantity.value}`, {}, { withCredentials: true }).then(() => {
    router.push('/cart')
  })
}

const getStarRating = (rating: number) => {
  return '★'.repeat(rating) + '☆'.repeat(5 - rating)
}

const formatTime = (time: string) => {
  if (!time) return ''
  return time.replace('T', ' ')
}

const submitReview = () => {
  if (!newReview.content.trim()) {
    ElMessage.error('请输入评价内容')
    return
  }
  
  axios.post('/api/reviews', {
    productId: productId.value,
    rating: newReview.rating,
    content: newReview.content
  }, { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('评价成功')
      newReview.content = ''
      newReview.rating = 5
      loadProduct()
    }
  })
}
</script>

<style scoped>
.product-detail {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.btn-back {
  padding: 0.5rem 1rem;
  background-color: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  margin-bottom: 0.5rem;
}

.btn-back:hover {
  background-color: #f5f5f5;
}

.product-main {
  display: flex;
  gap: 3rem;
  background-color: #fff;
  padding: 2rem;
  border-radius: 8px;
  margin-bottom: 1rem;
}

.product-images {
  width: 450px;
  flex-shrink: 0;
}

.main-image {
  width: 100%;
  height: 400px;
  object-fit: cover;
  border-radius: 8px;
}

.image-thumbs {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
}

.image-thumbs img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  opacity: 0.6;
  border: 2px solid transparent;
}

.image-thumbs img.active {
  opacity: 1;
  border-color: #e74c3c;
}

.product-info {
  flex: 1;
}

.product-info h1 {
  font-size: 1.8rem;
  margin-bottom: 1rem;
}

.price-section {
  margin-bottom: 1rem;
}

.discount-price {
  font-size: 2rem;
  color: #e74c3c;
  font-weight: bold;
  margin-right: 1rem;
}

.original-price {
  font-size: 1.2rem;
  color: #999;
  text-decoration: line-through;
}

.product-meta {
  display: flex;
  gap: 2rem;
  padding: 1rem 0;
  border-top: 1px solid #eee;
  border-bottom: 1px solid #eee;
  margin-bottom: 1rem;
  color: #666;
}

.specs {
  margin-bottom: 1rem;
}

.specs div {
  margin-bottom: 0.5rem;
  color: #666;
}

.description {
  padding: 1rem;
  background-color: #f9f9f9;
  border-radius: 4px;
  margin-bottom: 1rem;
  line-height: 1.6;
}

.merchant-info {
  padding: 1rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  margin-bottom: 1.5rem;
}

.merchant-info:hover {
  border-color: #e74c3c;
}

.merchant-name {
  font-weight: bold;
  margin-bottom: 0.3rem;
}

.merchant-level {
  font-size: 0.85rem;
  color: #666;
  margin-bottom: 0.3rem;
}

.view-shop {
  color: #e74c3c;
  font-size: 0.85rem;
}

.quantity-section {
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
}

.quantity-section label {
  margin-right: 1rem;
}

.quantity-section button {
  width: 30px;
  height: 30px;
  border: 1px solid #ddd;
  background: #fff;
  cursor: pointer;
}

.quantity-section span {
  padding: 0 1rem;
  font-size: 1.2rem;
}

.action-buttons {
  display: flex;
  gap: 1rem;
}

.add-cart-btn {
  flex: 1;
  padding: 0.8rem;
  background-color: #ff9500;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1.1rem;
}

.add-cart-btn:hover {
  background-color: #e68600;
}

.buy-now-btn {
  flex: 1;
  padding: 0.8rem;
  background-color: #e74c3c;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1.1rem;
}

.buy-now-btn:hover {
  background-color: #c0392b;
}

.reviews-section {
  background-color: #fff;
  padding: 2rem;
  border-radius: 8px;
}

.reviews-section h3 {
  margin-bottom: 1rem;
}

.no-reviews {
  text-align: center;
  padding: 2rem;
  color: #999;
}

.review-item {
  padding: 1rem 0;
  border-bottom: 1px solid #eee;
}

.review-item:last-of-type {
  border-bottom: none;
}

.review-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.reviewer {
  font-weight: bold;
}

.rating {
  color: #f5b041;
}

.review-content {
  margin-bottom: 0.5rem;
  line-height: 1.5;
}

.review-time {
  font-size: 0.85rem;
  color: #999;
}

.review-form {
  margin-top: 2rem;
  padding-top: 1rem;
  border-top: 1px solid #eee;
}

.review-form h4 {
  margin-bottom: 1rem;
}

.review-form select {
  width: 100%;
  padding: 0.5rem;
  margin-bottom: 1rem;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.review-form textarea {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: vertical;
  min-height: 100px;
  margin-bottom: 1rem;
}

.submit-review-btn {
  padding: 0.6rem 2rem;
  background-color: #e74c3c;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.submit-review-btn:hover {
  background-color: #c0392b;
}
</style>
