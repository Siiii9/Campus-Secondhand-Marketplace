<template>
  <div class="product-detail">
    <div class="product-images">
      <img v-for="(img, index) in product.images" :key="index" :src="img" :class="{ active: currentImage === index }">
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
        <span>评分: {{ product.avgRating }}</span>
      </div>
      <div class="description">{{ product.description }}</div>
      <div class="specs">
        <div>新旧程度: {{ product.conditionLevel }}</div>
        <div>尺寸: {{ product.unit }}</div>
        <div>是否议价: {{ product.isNegotiable === 1 ? '是' : '否' }}</div>
      </div>
      <div class="quantity-section">
        <label>数量:</label>
        <button @click="quantity--" :disabled="quantity <= 1">-</button>
        <span>{{ quantity }}</span>
        <button @click="quantity++" :disabled="quantity >= product.stock">+</button>
      </div>
      <button class="add-cart-btn" @click="addToCart">加入购物车</button>
      <button class="buy-now-btn" @click="buyNow">立即购买</button>
    </div>

    <div class="reviews-section">
      <h3>用户评价</h3>
      <div v-for="review in reviews" :key="review.id" class="review-item">
        <div class="review-header">
          <span>{{ review.fromUserId }}</span>
          <span class="rating">★★★★★</span>
        </div>
        <div class="review-content">{{ review.content }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
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
  images: []
})

const currentImage = ref(0)
const quantity = ref(1)
const reviews = ref<any[]>([])

onMounted(() => {
  axios.get(`/api/products/${productId.value}`).then(res => {
    product.value = res.data.data
  })
  
  axios.get(`/api/reviews/product/${productId.value}`).then(res => {
    reviews.value = res.data.data
  })
})

const addToCart = () => {
  axios.post(`/api/cart?productId=${productId.value}&quantity=${quantity.value}`).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('添加成功')
    } else {
      ElMessage.error(res.data.message || '添加失败')
    }
  }).catch(err => {
    ElMessage.error('添加失败，请先登录')
    console.error(err)
  })
}

const buyNow = () => {
  axios.post(`/api/cart?productId=${productId.value}&quantity=${quantity.value}`).then(res => {
    if (res.data.code === 200) {
      window.location.href = '/cart'
    } else {
      ElMessage.error(res.data.message || '添加失败')
    }
  }).catch(err => {
    ElMessage.error('添加失败，请先登录')
    console.error(err)
  })
}
</script>

<style scoped>
.product-detail {
  display: flex;
  gap: 2rem;
  padding: 2rem;
}

.product-images {
  width: 500px;
}

.product-images img {
  width: 100%;
  height: 400px;
  object-fit: cover;
  border-radius: 8px;
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
}

.original-price {
  font-size: 1rem;
  color: #999;
  text-decoration: line-through;
  margin-left: 1rem;
}

.product-meta {
  display: flex;
  gap: 2rem;
  margin-bottom: 1rem;
  color: #666;
}

.description {
  margin-bottom: 1rem;
  line-height: 1.6;
}

.specs {
  margin-bottom: 1.5rem;
}

.specs div {
  margin-bottom: 0.5rem;
}

.quantity-section {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.quantity-section button {
  width: 30px;
  height: 30px;
  border: 1px solid #ddd;
  background: #fff;
  cursor: pointer;
}

.add-cart-btn, .buy-now-btn {
  padding: 0.75rem 2rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 1rem;
}

.add-cart-btn {
  background-color: #f39c12;
  color: #fff;
}

.buy-now-btn {
  background-color: #e74c3c;
  color: #fff;
}

.reviews-section {
  margin-top: 2rem;
  padding: 2rem;
  border-top: 1px solid #ddd;
}

.review-item {
  margin-bottom: 1rem;
  padding: 1rem;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.rating {
  color: #f39c12;
}
</style>