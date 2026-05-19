<template>
  <div class="home">
    <header>
      <div class="logo">校园二手交易平台</div>
      <div class="search-bar">
        <input type="text" v-model="keyword" placeholder="搜索商品..." @keyup.enter="search">
        <button @click="search">搜索</button>
      </div>
      <div class="nav-links">
        <span v-if="!isLoggedIn" @click="$router.push('/login')">登录</span>
        <span v-if="!isLoggedIn" @click="$router.push('/register')">注册</span>
        <span v-if="isLoggedIn" @click="$router.push('/cart')">购物车</span>
        <span v-if="isLoggedIn" @click="$router.push('/orders')">订单</span>
        <span v-if="isLoggedIn" @click="$router.push('/profile')">个人中心</span>
        <span v-if="isMerchant" @click="$router.push('/merchant')">商家中心</span>
        <span v-if="isAdmin" @click="$router.push('/admin')">管理后台</span>
      </div>
    </header>

    <div class="carousel-container" @mouseenter="stopAutoPlay" @mouseleave="startAutoPlay">
      <div class="carousel-wrapper" :style="{ transform: `translateX(-${currentIndex * 100}%)` }">
        <div v-for="item in carousels" :key="item.id" class="carousel-item">
          <a :href="item.linkUrl || 'javascript:void(0)'" target="_blank">
            <img :src="item.imageUrl" :alt="item.title || '轮播图'">
          </a>
        </div>
      </div>

      <button v-if="carousels.length > 1" class="carousel-btn prev" @click="prevSlide">‹</button>
      <button v-if="carousels.length > 1" class="carousel-btn next" @click="nextSlide">›</button>

      <div v-if="carousels.length > 1" class="carousel-indicators">
        <span
          v-for="(item, index) in carousels"
          :key="item.id"
          :class="['indicator', { active: index === currentIndex }]"
          @click="goToSlide(index)"
        ></span>
      </div>
    </div>

    <div class="category-section">
      <h3>商品分类</h3>
      <div class="category-list">
        <div v-for="cat in categories" :key="cat.id" class="category-item">
          {{ cat.name }}
        </div>
      </div>
    </div>

    <div class="hot-products">
      <h3>热门商品</h3>
      <div class="product-grid">
        <div v-for="product in products" :key="product.id" class="product-card" @click="$router.push(`/product/${product.id}`)">
          <img :src="product.images?.[0] || '/images/OIP-C.jpg'" alt="">
          <div class="product-name">{{ product.name }}</div>
          <div class="product-price">¥{{ product.discountPrice }}</div>
          <div class="product-sales">销量: {{ product.salesCount }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import axios from 'axios'

const keyword = ref('')
const isLoggedIn = ref(false)
const isAdmin = ref(false)
const isMerchant = ref(false)
const carousels = ref<any[]>([])
const categories = ref<any[]>([])
const products = ref<any[]>([])
const currentIndex = ref(0)
let autoPlayTimer: number | null = null

const getActiveCarousels = () => {
  axios.get('/api/carousel/active').then(res => {
    if (res.data.code === 200) {
      carousels.value = res.data.data || []
      currentIndex.value = 0
      startAutoPlay()
    }
  }).catch(err => {
    console.error('获取轮播图失败:', err)
  })
}

const nextSlide = () => {
  if (carousels.value.length === 0) return
  currentIndex.value = (currentIndex.value + 1) % carousels.value.length
}

const prevSlide = () => {
  if (carousels.value.length === 0) return
  currentIndex.value = (currentIndex.value - 1 + carousels.value.length) % carousels.value.length
}

const goToSlide = (index: number) => {
  currentIndex.value = index
}

const startAutoPlay = () => {
  if (autoPlayTimer) return
  autoPlayTimer = window.setInterval(() => {
    nextSlide()
  }, 5000)
}

const stopAutoPlay = () => {
  if (autoPlayTimer) {
    clearInterval(autoPlayTimer)
    autoPlayTimer = null
  }
}

onMounted(() => {
  isLoggedIn.value = localStorage.getItem('userToken') !== null
  isAdmin.value = localStorage.getItem('isAdmin') === 'true'
  isMerchant.value = localStorage.getItem('isMerchant') === 'true'

  getActiveCarousels()

  axios.get('/api/products/search?page=1&size=8').then(res => {
    products.value = res.data.data.records
  })

  if (isLoggedIn.value) {
    axios.get('/api/users/info', { withCredentials: true }).then(res => {
      if (res.data.code === 200) {
        const user = res.data.data
        if (user.role === 'MERCHANT') {
          isMerchant.value = true
          localStorage.setItem('isMerchant', 'true')
        }
      }
    })
  }
})

onUnmounted(() => {
  stopAutoPlay()
})

const search = () => {
  if (keyword.value) {
    window.location.href = `/products?keyword=${keyword.value}`
  }
}
</script>

<style scoped>
.home {
  min-height: 100vh;
  background-color: #f5f5f5;
}

header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
  color: #e74c3c;
}

.search-bar {
  flex: 1;
  max-width: 500px;
  margin: 0 2rem;
  display: flex;
}

.search-bar input {
  flex: 1;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px 0 0 4px;
}

.search-bar button {
  padding: 0.5rem 1rem;
  background-color: #e74c3c;
  color: #fff;
  border: none;
  border-radius: 0 4px 4px 0;
  cursor: pointer;
}

.nav-links span {
  margin-left: 1.5rem;
  cursor: pointer;
  color: #333;
}

.carousel-container {
  position: relative;
  height: 300px;
  background-color: #333;
  margin: 1rem;
  border-radius: 8px;
  overflow: hidden;
}

.carousel-wrapper {
  display: flex;
  height: 100%;
  transition: transform 0.5s ease;
}

.carousel-item {
  min-width: 100%;
  height: 100%;
}

.carousel-item a {
  display: block;
  width: 100%;
  height: 100%;
}

.carousel-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.carousel-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 60px;
  background-color: rgba(0,0,0,0.3);
  color: #fff;
  border: none;
  font-size: 2rem;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.3s;
}

.carousel-container:hover .carousel-btn {
  opacity: 1;
}

.carousel-btn.prev {
  left: 0;
  border-radius: 0 4px 4px 0;
}

.carousel-btn.next {
  right: 0;
  border-radius: 4px 0 0 4px;
}

.carousel-btn:hover {
  background-color: rgba(0,0,0,0.5);
}

.carousel-indicators {
  position: absolute;
  bottom: 15px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
}

.indicator {
  width: 10px;
  height: 10px;
  background-color: rgba(255,255,255,0.5);
  border-radius: 50%;
  cursor: pointer;
  transition: background-color 0.3s;
}

.indicator.active {
  background-color: #fff;
}

.indicator:hover {
  background-color: rgba(255,255,255,0.8);
}

.category-section {
  padding: 1rem 2rem;
  background-color: #fff;
  margin: 1rem;
  border-radius: 8px;
}

.category-list {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}

.category-item {
  padding: 0.5rem 1rem;
  background-color: #f0f0f0;
  border-radius: 20px;
  cursor: pointer;
}

.hot-products {
  padding: 1rem 2rem;
  margin: 1rem;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
}

.product-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 1rem;
  cursor: pointer;
}

.product-card img {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 4px;
}

.product-name {
  margin-top: 0.5rem;
  font-weight: bold;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  color: #e74c3c;
  font-size: 1.2rem;
  margin-top: 0.5rem;
}

.product-sales {
  font-size: 0.8rem;
  color: #999;
  margin-top: 0.5rem;
}
</style>
