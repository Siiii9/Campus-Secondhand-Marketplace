<template>
  <div class="product-list">
    <div class="search-bar">
      <input type="text" v-model="keyword" placeholder="搜索商品...">
      <button @click="search">搜索</button>
      <select v-model="sortBy">
        <option value="created">最新发布</option>
        <option value="price">价格排序</option>
        <option value="sales">销量排序</option>
        <option value="rating">好评排序</option>
      </select>
    </div>
    
    <div class="product-grid">
      <div v-for="product in products" :key="product.id" class="product-card" @click="$router.push(`/product/${product.id}`)">
        <img :src="product.images?.[0] || '/images/default.jpg'" alt="">
        <div class="product-name">{{ product.name }}</div>
        <div class="product-price">¥{{ product.discountPrice }}</div>
        <div class="product-info">
          <span>销量: {{ product.salesCount }}</span>
          <span>评分: {{ product.avgRating }}</span>
        </div>
      </div>
    </div>

    <div class="pagination">
      <button @click="prevPage" :disabled="currentPage <= 1">上一页</button>
      <span>{{ currentPage }} / {{ totalPages }}</span>
      <button @click="nextPage" :disabled="currentPage >= totalPages">下一页</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'

const keyword = ref('')
const sortBy = ref('created')
const products = ref<any[]>([])
const currentPage = ref(1)
const totalPages = ref(1)

onMounted(() => {
  loadProducts()
})

const loadProducts = () => {
  axios.get(`/api/products/search?keyword=${keyword.value}&sortBy=${sortBy.value}&page=${currentPage.value}&size=12`)
    .then(res => {
      products.value = res.data.data.records
      totalPages.value = res.data.data.pages
    })
}

const search = () => {
  currentPage.value = 1
  loadProducts()
}

const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
    loadProducts()
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    loadProducts()
  }
}
</script>

<style scoped>
.product-list {
  padding: 2rem;
}

.search-bar {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}

.search-bar input {
  flex: 1;
  max-width: 400px;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.search-bar button {
  padding: 0.75rem 1.5rem;
  background-color: #e74c3c;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.search-bar select {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1.5rem;
}

.product-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 1rem;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.product-card img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 4px;
}

.product-name {
  margin-top: 0.75rem;
  font-weight: bold;
  font-size: 1rem;
}

.product-price {
  color: #e74c3c;
  font-size: 1.3rem;
  margin-top: 0.5rem;
}

.product-info {
  display: flex;
  justify-content: space-between;
  margin-top: 0.5rem;
  font-size: 0.85rem;
  color: #666;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
}

.pagination button {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>