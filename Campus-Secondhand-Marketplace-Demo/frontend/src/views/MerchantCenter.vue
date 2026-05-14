<template>
  <div class="merchant-center">
    <h2>商家中心</h2>
    
    <div class="tabs">
      <button :class="{ active: activeTab === 'products' }" @click="activeTab = 'products'">我的商品</button>
      <button :class="{ active: activeTab === 'add' }" @click="activeTab = 'add'">发布商品</button>
    </div>

    <div v-if="activeTab === 'products'" class="tab-content">
      <div class="filter-bar">
        <select v-model="statusFilter" @change="loadProducts">
          <option value="">全部状态</option>
          <option value="0">待审核</option>
          <option value="1">已发布</option>
          <option value="2">已下架</option>
        </select>
      </div>
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
              <button v-if="product.status === 1" @click="editProduct(product)">编辑</button>
              <button v-if="product.status === 1" @click="offlineProduct(product.id)">下架</button>
              <button v-if="product.status === 2" @click="onlineProduct(product.id)">上架</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="activeTab === 'add'" class="tab-content">
      <form @submit.prevent="addProduct" enctype="multipart/form-data">
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
            <option :value="0">否</option>
            <option :value="1">是</option>
          </select>
        </div>
        <div class="form-group">
          <label>商品描述</label>
          <textarea v-model="productForm.description"></textarea>
        </div>
        <div class="form-group">
          <label>商品图片（可多选）</label>
          <div class="upload-area" @click="triggerFileInput" @dragover.prevent @drop.prevent="handleDrop">
            <input 
              ref="fileInput" 
              type="file" 
              multiple 
              accept="image/*" 
              @change="handleFileSelect"
              style="display: none"
            >
            <div v-if="uploadedImages.length === 0" class="upload-hint">
              <span>点击或拖拽上传图片</span>
            </div>
            <div v-else class="image-preview">
              <div v-for="(img, index) in uploadedImages" :key="index" class="preview-item">
                <img :src="img" alt="">
                <button class="remove-btn" @click.stop="removeImage(index)">×</button>
              </div>
              <div v-if="uploadedImages.length < 5" class="upload-more" @click="triggerFileInput">
                + 添加图片
              </div>
            </div>
          </div>
        </div>
        <button type="submit" class="submit-btn">发布商品</button>
      </form>
    </div>

    <!-- 编辑商品弹窗 -->
    <div v-if="showEditModal" class="modal-overlay" @click.self="closeEditModal">
      <div class="modal-content">
        <h3>编辑商品</h3>
        <div v-if="editingProduct" class="form-group">
          <label>商品名称</label>
          <input type="text" v-model="editingProduct.name" required>
          <label>原价</label>
          <input type="number" v-model="editingProduct.originalPrice" step="0.01" required>
          <label>折扣价</label>
          <input type="number" v-model="editingProduct.discountPrice" step="0.01" required>
          <label>库存</label>
          <input type="number" v-model="editingProduct.stock" required>
          <label>描述</label>
          <textarea v-model="editingProduct.description"></textarea>
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="closeEditModal">取消</button>
          <button class="btn-confirm" @click="saveEdit">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const activeTab = ref('products')
const products = ref<any[]>([])
const statusFilter = ref('')

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

const uploadedImages = ref<string[]>([])
const fileInput = ref<HTMLInputElement | null>(null)

const showEditModal = ref(false)
const editingProduct = ref<any>(null)

onMounted(() => {
  loadProducts()
})

const loadProducts = () => {
  axios.get('/api/products/merchant/1').then(res => {
    if (statusFilter.value) {
      products.value = res.data.data.filter((p: any) => p.status === parseInt(statusFilter.value))
    } else {
      products.value = res.data.data
    }
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

const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  const files = target.files
  if (files) {
    Array.from(files).forEach(file => {
      if (uploadedImages.value.length < 5) {
        previewFile(file)
      }
    })
  }
  target.value = ''
}

const handleDrop = (event: DragEvent) => {
  const files = event.dataTransfer?.files
  if (files) {
    Array.from(files).forEach(file => {
      if (uploadedImages.value.length < 5 && file.type.startsWith('image/')) {
        previewFile(file)
      }
    })
  }
}

const previewFile = (file: File) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    uploadedImages.value.push(e.target?.result as string)
  }
  reader.readAsDataURL(file)
}

const removeImage = (index: number) => {
  uploadedImages.value.splice(index, 1)
}

const addProduct = () => {
  const formData = new FormData()
  formData.append('name', productForm.value.name)
  formData.append('categoryId', String(productForm.value.categoryId))
  formData.append('originalPrice', String(productForm.value.originalPrice))
  formData.append('discountPrice', String(productForm.value.discountPrice))
  formData.append('stock', String(productForm.value.stock))
  formData.append('unit', productForm.value.unit)
  formData.append('conditionLevel', productForm.value.conditionLevel)
  formData.append('isNegotiable', String(productForm.value.isNegotiable))
  formData.append('description', productForm.value.description)

  uploadedImages.value.forEach((img, index) => {
    if (img.startsWith('blob:')) {
      fetch(img)
        .then(res => res.blob())
        .then(blob => {
          const file = new File([blob], `product_${index}.jpg`, { type: 'image/jpeg' })
          formData.append('images', file)
        })
    }
  })

  axios.post('/api/products/submit', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    withCredentials: true
  }).then(res => {
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
      uploadedImages.value = []
    } else {
      ElMessage.error(res.data.message || '发布失败')
    }
  }).catch(err => {
    ElMessage.error('发布失败: ' + err.message)
  })
}

const editProduct = (product: any) => {
  editingProduct.value = { ...product }
  showEditModal.value = true
}

const closeEditModal = () => {
  showEditModal.value = false
  editingProduct.value = null
}

const saveEdit = () => {
  axios.put(`/api/products/${editingProduct.value.id}`, editingProduct.value, { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('更新成功')
      closeEditModal()
      loadProducts()
    }
  })
}

const offlineProduct = (productId: string) => {
  axios.put(`/api/products/${productId}/offline`, {}, { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('下架成功')
      loadProducts()
    }
  })
}

const onlineProduct = (productId: string) => {
  axios.put(`/api/products/${productId}`, { status: 0 }, { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('已提交审核')
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

.filter-bar {
  margin-bottom: 1rem;
}

.filter-bar select {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
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

.upload-area {
  border: 2px dashed #ddd;
  border-radius: 8px;
  padding: 2rem;
  cursor: pointer;
  min-height: 150px;
}

.upload-hint {
  text-align: center;
  color: #999;
}

.image-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}

.preview-item {
  position: relative;
  width: 120px;
  height: 120px;
}

.preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.remove-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 24px;
  height: 24px;
  background-color: #e74c3c;
  color: #fff;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
}

.upload-more {
  width: 120px;
  height: 120px;
  border: 2px dashed #ddd;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
}

.submit-btn {
  padding: 0.75rem 2rem;
  background-color: #e74c3c;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-content {
  background-color: #fff;
  padding: 1.5rem;
  border-radius: 8px;
  width: 400px;
}

.modal-content h3 {
  margin-bottom: 1rem;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1rem;
}

.btn-cancel {
  padding: 0.5rem 1rem;
  background-color: #95a5a6;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-confirm {
  padding: 0.5rem 1rem;
  background-color: #e74c3c;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
</style>