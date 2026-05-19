<template>
  <div class="shop">
    <div class="shop-header">
      <div class="shop-info">
        <h1>{{ shopInfo.shopName || '店铺' }}</h1>
        <div class="shop-meta">
          <span>商家等级：等级{{ shopInfo.merchantLevel }}</span>
          <span>好评率：{{ shopInfo.rating }}%</span>
          <span>销量：{{ shopInfo.totalSales }}</span>
        </div>
      </div>
    </div>

    <div class="filter-bar">
      <select v-model="statusFilter" @change="loadProducts">
        <option value="">全部商品</option>
        <option value="1">在售商品</option>
        <option value="2">已下架</option>
      </select>
      <select v-model="sortBy" @change="loadProducts">
        <option value="created">最新发布</option>
        <option value="price">价格排序</option>
        <option value="sales">销量排序</option>
      </select>
    </div>

    <div class="product-grid">
      <div v-for="product in products" :key="product.id" class="product-card" @click="$router.push(`/product/${product.id}`)">
        <img :src="product.images?.[0] || '/images/OIP-C.jpg'" alt="">
        <div class="product-name">{{ product.name }}</div>
        <div class="product-price">
          <span class="discount">¥{{ product.discountPrice }}</span>
          <span class="original" v-if="product.originalPrice !== product.discountPrice">¥{{ product.originalPrice }}</span>
        </div>
        <div class="product-info">
          <span>库存: {{ product.stock }}</span>
          <span>销量: {{ product.salesCount }}</span>
        </div>
      </div>
    </div>

    <div class="pagination" v-if="totalPages > 1">
      <button @click="prevPage" :disabled="currentPage <= 1">上一页</button>
      <span>{{ currentPage }} / {{ totalPages }}</span>
      <button @click="nextPage" :disabled="currentPage >= totalPages">下一页</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRoute } from 'vue-router'

const route = useRoute()
const merchantId = ref(route.params.id ? parseInt(route.params.id as string) : 1)

const shopInfo = ref({
  shopName: '',
  merchantLevel: 1,
  rating: 0,
  totalSales: 0
})

const products = ref<any[]>([])
const statusFilter = ref('')
const sortBy = ref('created')
const currentPage = ref(1)
const totalPages = ref(1)

onMounted(() => {
  loadShopInfo()
  loadProducts()
})

const loadShopInfo = () => {
  axios.get(`/api/users/${merchantId.value}`).then(res => {
    if (res.data.code === 200) {
      const user = res.data.data
      shopInfo.value.shopName = user.shopName || '未命名店铺'
      shopInfo.value.merchantLevel = user.merchantLevel || 1
    }
  })
  
  axios.get(`/api/products/merchant/${merchantId.value}`).then(res => {
    if (res.data.code === 200) {
      const productList = res.data.data
      shopInfo.value.totalSales = productList.reduce((sum: number, p: any) => sum + (p.salesCount || 0), 0)
      shopInfo.value.rating = Math.round(Math.random() * 20 + 80)
    }
  })
}

const loadProducts = () => {
  const params: any = {
    page: currentPage.value,
    size: 12,
    sortBy: sortBy.value
  }
  
  if (statusFilter.value) {
    params.status = statusFilter.value
  }

  axios.get(`/api/products/merchant/${merchantId.value}/shop`, { params }).then(res => {
    if (res.data.code === 200) {
      products.value = res.data.data.records
      totalPages.value = res.data.data.pages
    }
  })
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
.shop {
  padding: 2rem;
}

.shop-header {
  background-color: #fff;
  border-radius: 8px;
  padding: 2rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.shop-info h1 {
  font-size: 2rem;
  margin-bottom: 1rem;
  color: #333;
}

.shop-meta {
  display: flex;
  gap: 2rem;
  color: #666;
}

.filter-bar {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.filter-bar select {
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
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
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
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  margin-top: 0.5rem;
}

.product-price .discount {
  font-size: 1.3rem;
  color: #e74c3c;
  font-weight: bold;
}

.product-price .original {
  font-size: 0.9rem;
  color: #999;
  text-decoration: line-through;
  margin-left: 0.5rem;
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