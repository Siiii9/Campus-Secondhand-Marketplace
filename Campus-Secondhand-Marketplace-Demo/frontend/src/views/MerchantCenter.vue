<template>
  <div class="merchant-center">
    <h2>商家中心</h2>
    
    <div class="tabs">
      <button :class="{ active: activeTab === 'products' }" @click="activeTab = 'products'">我的商品</button>
      <button :class="{ active: activeTab === 'add' }" @click="activeTab = 'add'">发布商品</button>
    </div>

    <div v-if="activeTab === 'products'" class="tab-content">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>商品名</th>
            <th>价格</th>
            <th>库存</th>
            <th>销量</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="product in products" :key="product.id">
            <td>{{ product.id }}</td>
            <td>{{ product.name }}</td>
            <td>¥{{ product.discountPrice }}</td>
            <td>{{ product.stock }}</td>
            <td>{{ product.salesCount }}</td>
            <td>{{ getStatusText(product.status) }}</td>
            <td>
              <button v-if="product.status === 1" @click="updateProduct(product)">编辑</button>
              <button v-if="product.status === 1" @click="offlineProduct(product.id)">下架</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="activeTab === 'add'" class="tab-content">
      <form @submit.prevent="addProduct">
        <div class="form-group">
          <label>商品名称</label>
          <input type="text" v-model="productForm.name" required>
        </div>
        <div class="form-group">
          <label>分类</label>
          <select v-model="productForm.categoryId">
            <option value="1">电子产品</option>
            <option value="5">学习用品</option>
            <option value="8">生活用品</option>
          </select>
        </div>
        <div class="form-group">
          <label>原价</label>
          <input type="number" v-model="productForm.originalPrice" step="0.01" required>
        </div>
        <div class="form-group">
          <label>折扣价</label>
          <input type="number" v-model="productForm.discountPrice" step="0.01" required>
        </div>
        <div class="form-group">
          <label>库存</label>
          <input type="number" v-model="productForm.stock" required>
        </div>
        <div class="form-group">
          <label>尺寸</label>
          <input type="text" v-model="productForm.unit">
        </div>
        <div class="form-group">
          <label>新旧程度</label>
          <select v-model="productForm.conditionLevel">
            <option value="全新">全新</option>
            <option value="九成新">九成新</option>
            <option value="八成新">八成新</option>
            <option value="七成新">七成新</option>
          </select>
        </div>
        <div class="form-group">
          <label>是否议价</label>
          <select v-model="productForm.isNegotiable">
            <option value="0">否</option>
            <option value="1">是</option>
          </select>
        </div>
        <div class="form-group">
          <label>商品描述</label>
          <textarea v-model="productForm.description"></textarea>
        </div>
        <button type="submit" class="submit-btn">发布商品</button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const activeTab = ref('products')
const products = ref<any[]>([])

const productForm = ref({
  name: '',
  categoryId: 1,
  originalPrice: 0,
  discountPrice: 0,
  stock: 1,
  unit: '',
  conditionLevel: '九成新',
  isNegotiable: 0,
  description: ''
})

onMounted(() => {
  loadProducts()
})

const loadProducts = () => {
  axios.get('/api/products/merchant/1').then(res => {
    products.value = res.data.data
  })
}

const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '待审核',
    1: '已发布',
    2: '已下架',
    3: '交易中'
  }
  return statusMap[status] || '未知'
}

const addProduct = () => {
  axios.post('/api/products', productForm.value).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('发布成功，等待审核')
      productForm.value = {
        name: '',
        categoryId: 1,
        originalPrice: 0,
        discountPrice: 0,
        stock: 1,
        unit: '',
        conditionLevel: '九成新',
        isNegotiable: 0,
        description: ''
      }
    }
  })
}

const updateProduct = (product: any) => {
  axios.put(`/api/products/${product.id}`, product).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('更新成功')
    }
  })
}

const offlineProduct = (productId: string) => {
  axios.put(`/api/products/${productId}`, { status: 2 }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('下架成功')
      loadProducts()
    }
  })
}
</script>

<style scoped>
.merchant-center {
  padding: 2rem;
}

.merchant-center h2 {
  margin-bottom: 1.5rem;
}

.tabs {
  margin-bottom: 1.5rem;
}

.tabs button {
  padding: 0.75rem 1.5rem;
  border: none;
  background: #fff;
  cursor: pointer;
  margin-right: 0.5rem;
  border-radius: 4px;
}

.tabs button.active {
  background-color: #e74c3c;
  color: #fff;
}

.tab-content table {
  width: 100%;
  border-collapse: collapse;
  background-color: #fff;
  border-radius: 8px;
}

.tab-content th, .tab-content td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.tab-content th {
  background-color: #f5f5f5;
}

.tab-content button {
  padding: 0.5rem 1rem;
  margin-right: 0.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.tab-content button:nth-child(1) {
  background-color: #3498db;
  color: #fff;
}

.tab-content button:nth-child(2) {
  background-color: #e74c3c;
  color: #fff;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.form-group textarea {
  height: 100px;
}

.submit-btn {
  padding: 0.75rem 2rem;
  background-color: #e74c3c;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
</style>