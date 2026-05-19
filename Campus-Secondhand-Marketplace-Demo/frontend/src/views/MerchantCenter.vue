<template>
  <div class="merchant-center">
    <div class="header">
      <button class="btn-back" @click="$router.push('/')">← 返回主页</button>
      <h2>商家中心</h2>
    </div>
    
    <div class="tabs">
      <button :class="{ active: activeTab === 'products' }" @click="activeTab = 'products'">商品管理</button>
      <button :class="{ active: activeTab === 'add' }" @click="activeTab = 'add'">发布商品</button>
      <button :class="{ active: activeTab === 'shop' }" @click="activeTab = 'shop'">我的店铺</button>
      <button :class="{ active: activeTab === 'orders' }" @click="activeTab = 'orders'; loadOrders()">订单信息</button>
    </div>

    <div v-if="activeTab === 'products'" class="tab-content">
      <div class="product-tabs">
        <button :class="{ active: productTab === 'selling' }" @click="productTab = 'selling'; loadProducts(1)">销售中</button>
        <button :class="{ active: productTab === 'offline' }" @click="productTab = 'offline'; loadProducts(2)">已下架</button>
        <button :class="{ active: productTab === 'sold' }" @click="productTab = 'sold'; loadProducts(3)">已出售</button>
        <button :class="{ active: productTab === 'pending' }" @click="productTab = 'pending'; loadProducts(0)">待审核</button>
      </div>
      
      <div v-if="productList.length === 0" class="empty-state">
        <p>暂无商品</p>
      </div>
      
      <div v-else class="product-grid">
        <div v-for="product in productList" :key="product.id" class="product-card">
          <div class="product-image">
            <img :src="product.images?.[0] || '/placeholder.png'" alt="商品图片">
          </div>
          <div class="product-info">
            <h3>{{ product.name }}</h3>
            <p class="price">¥{{ product.discountPrice }} <span class="original-price">¥{{ product.originalPrice }}</span></p>
            <p class="stock">库存: {{ product.stock }} | 销量: {{ product.salesCount }}</p>
            <div class="product-actions">
              <button class="btn-detail" @click="viewProductDetail(product)">查看详情</button>
              <button v-if="productTab === 'selling'" class="btn-offline" @click="offlineProduct(product.id)">下架</button>
              <button v-if="productTab === 'offline'" class="btn-online" @click="onlineProduct(product.id)">重新上架</button>
              <button v-if="productTab === 'pending'" class="btn-delete" @click="deleteProduct(product.id)">删除</button>
              <button v-if="productTab !== 'sold'" class="btn-edit" @click="editProduct(product)">编辑</button>
              <button class="btn-stock" @click="adjustStock(product)">调整库存</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="activeTab === 'add'" class="tab-content">
      <form @submit.prevent="addProduct" enctype="multipart/form-data">
        <div class="form-group">
          <label>商品名称 <span class="required">*</span></label>
          <input type="text" v-model="productForm.name" required placeholder="请输入商品名称">
        </div>
        
        <div class="form-row">
          <div class="form-group">
            <label>类别 <span class="required">*</span></label>
            <select v-model="productForm.categoryId">
              <option value="1">电子产品</option>
              <option value="2">服装</option>
              <option value="3">食品</option>
              <option value="4">图书</option>
              <option value="5">学习用品</option>
              <option value="6">运动器材</option>
              <option value="7">美妆护肤</option>
              <option value="8">生活用品</option>
            </select>
          </div>
          
          <div class="form-group">
            <label>新旧程度 <span class="required">*</span></label>
            <select v-model="productForm.conditionLevel">
              <option value="全新">全新</option>
              <option value="九成新">九成新</option>
              <option value="八成新">八成新</option>
              <option value="七成新">七成新</option>
              <option value="六成新及以下">六成新及以下</option>
            </select>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>原价 <span class="required">*</span></label>
            <input type="number" v-model="productForm.originalPrice" step="0.01" required placeholder="原价">
          </div>
          <div class="form-group">
            <label>折扣价 <span class="required">*</span></label>
            <input type="number" v-model="productForm.discountPrice" step="0.01" required placeholder="折后价">
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>商品数量 <span class="required">*</span></label>
            <input type="number" v-model="productForm.stock" min="1" required placeholder="正整数">
          </div>
          <div class="form-group">
            <label>尺寸大小</label>
            <input type="text" v-model="productForm.unit" placeholder="如 L,XL 或 10cm*20cm">
          </div>
        </div>

        <div class="form-group">
          <label>是否允许议价</label>
          <div class="radio-group">
            <label><input type="radio" v-model="productForm.isNegotiable" :value="1"> 是</label>
            <label><input type="radio" v-model="productForm.isNegotiable" :value="0"> 否</label>
          </div>
        </div>

        <div class="form-group">
          <label>使用说明</label>
          <textarea v-model="productForm.description" rows="4" placeholder="请输入商品使用说明"></textarea>
        </div>

        <div class="form-group">
          <label>商品照片（支持多张上传，可拖拽排序）</label>
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

        <button type="submit" class="submit-btn">提交发布</button>
      </form>
    </div>

    <div v-if="activeTab === 'shop'" class="tab-content">
      <div class="shop-header">
        <h3>{{ shopInfo.shopName || (currentUser?.realName || currentUser?.username) + '的店' }}</h3>
        <p class="service-rating">服务态度评分: {{ shopInfo.serviceRating || 0 }} ⭐</p>
      </div>
      
      <div class="shop-products">
        <div v-for="product in shopProducts" :key="product.id" class="shop-product-card">
          <img :src="product.images?.[0] || '/placeholder.png'" alt="商品图片">
          <div class="shop-product-info">
            <h4>{{ product.name }}</h4>
            <p>¥{{ product.discountPrice }}</p>
            <p>销量: {{ product.salesCount }}</p>
          </div>
        </div>
      </div>
    </div>

    <div v-if="activeTab === 'orders'" class="tab-content">
      <div class="order-tabs">
        <button :class="{ active: orderTab === 'pending' }" @click="orderTab = 'pending'; loadOrders()">审核购买订单</button>
        <button :class="{ active: orderTab === 'return' }" @click="orderTab = 'return'; loadReturnRequests()">退款申请审核</button>
      </div>
      
      <div v-if="orderTab === 'pending'" class="order-content">
        <div v-if="pendingOrders.length === 0 && toShipOrders.length === 0" class="empty-state">暂无订单</div>
        
        <div v-if="pendingOrders.length > 0">
          <h4>待审核订单</h4>
          <div class="order-list">
            <div v-for="order in pendingOrders" :key="order.id" class="order-item">
              <div class="order-info">
                <span>订单号: {{ order.orderNo }}</span>
                <span>买家: {{ order.userName }}</span>
                <span>金额: ¥{{ order.actualPaid }}</span>
              </div>
              <div class="order-items">
                <div v-for="item in order.items" :key="item.id" class="order-product">
                  <img :src="item.image || '/placeholder.png'" alt="">
                  <span>{{ item.productName }} x{{ item.quantity }}</span>
                </div>
              </div>
              <div class="order-actions">
                <button @click="confirmOrder(order.id)">确认订单</button>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="toShipOrders.length > 0">
          <h4>待发货订单</h4>
          <div class="order-list">
            <div v-for="order in toShipOrders" :key="order.id" class="order-item">
              <div class="order-info">
                <span>订单号: {{ order.orderNo }}</span>
                <span>买家: {{ order.userName }}</span>
                <span>金额: ¥{{ order.actualPaid }}</span>
              </div>
              <div class="order-items">
                <div v-for="item in order.items" :key="item.id" class="order-product">
                  <img :src="item.image || '/placeholder.png'" alt="">
                  <span>{{ item.productName }} x{{ item.quantity }}</span>
                </div>
              </div>
              <div class="order-actions">
                <button @click="openShipModal(order)">确认发货</button>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div v-if="orderTab === 'return'" class="order-content">
        <div v-if="returnRequests.length === 0 && refundOrders.length === 0" class="empty-state">暂无退款相关订单</div>
        
        <div v-if="returnRequests.length > 0">
          <h4>退款申请审核</h4>
          <div class="order-list">
            <div v-for="request in returnRequests" :key="request.id" class="order-item">
              <div class="order-info">
                <span>订单号: {{ request.orderNo }}</span>
                <span>买家: {{ request.userName }}</span>
                <span>退款金额: ¥{{ request.amount }}</span>
              </div>
              <div class="return-reason">
                <span>退款原因: {{ request.reason }}</span>
              </div>
              <div class="order-actions">
                <button @click="approveReturn(request.id)">同意退款</button>
                <button @click="rejectReturn(request.id)">拒绝退款</button>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="refundOrders.length > 0">
          <h4>待退款订单</h4>
          <div class="order-list">
            <div v-for="order in refundOrders" :key="order.id" class="order-item">
              <div class="order-info">
                <span>订单号: {{ order.orderNo }}</span>
                <span>买家: {{ order.userName }}</span>
                <span>退款金额: ¥{{ order.actualPaid }}</span>
              </div>
              <div class="order-actions">
                <button @click="confirmRefund(order.id)">退款</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 商品详情弹窗 -->
    <div v-if="showDetailModal" class="modal-overlay" @click.self="closeDetailModal">
      <div class="modal-content large">
        <h3>商品详情</h3>
        <button class="close-btn" @click="closeDetailModal">×</button>
        
        <div v-if="selectedProduct" class="product-detail">
          <div class="detail-images">
            <img v-for="(img, index) in selectedProduct.images" :key="index" :src="img" alt="商品图片">
          </div>
          <div class="detail-info">
            <h4>{{ selectedProduct.name }}</h4>
            <p class="price">¥{{ selectedProduct.discountPrice }} <span class="original-price">¥{{ selectedProduct.originalPrice }}</span></p>
            <p><strong>类别:</strong> {{ getCategoryName(selectedProduct.categoryId) }}</p>
            <p><strong>新旧程度:</strong> {{ selectedProduct.conditionLevel }}</p>
            <p><strong>尺寸:</strong> {{ selectedProduct.unit || '未设置' }}</p>
            <p><strong>库存:</strong> {{ selectedProduct.stock }}</p>
            <p><strong>销量:</strong> {{ selectedProduct.salesCount }}</p>
            <p><strong>是否议价:</strong> {{ selectedProduct.isNegotiable === 1 ? '是' : '否' }}</p>
            <p><strong>使用说明:</strong> {{ selectedProduct.description || '无' }}</p>
            <p><strong>平均评分:</strong> {{ selectedProduct.avgRating || 0 }} ⭐</p>
          </div>
          
          <div class="review-section">
            <h4>买家评价</h4>
            <div v-if="selectedProduct.reviews?.length === 0" class="no-reviews">暂无评价</div>
            <div v-else class="review-list">
              <div v-for="review in selectedProduct.reviews" :key="review.id" class="review-item">
                <div class="review-header">
                  <span class="reviewer">{{ review.userName }}</span>
                  <span class="review-rating">{{ '⭐'.repeat(review.rating) }}</span>
                  <span class="review-time">{{ formatTime(review.createdAt) }}</span>
                </div>
                <p class="review-content">{{ review.content }}</p>
                <div v-if="review.reply" class="review-reply">
                  <span class="reply-label">商家回复:</span>
                  <span>{{ review.reply }}</span>
                </div>
                <input v-if="!review.reply" type="text" v-model="replyContent" placeholder="回复评价">
                <button v-if="!review.reply" @click="replyReview(review.id)">回复</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑商品弹窗 -->
    <div v-if="showEditModal" class="modal-overlay" @click.self="closeEditModal">
      <div class="modal-content">
        <h3>编辑商品</h3>
        <button class="close-btn" @click="closeEditModal">×</button>
        
        <div v-if="editingProduct" class="edit-form">
          <div class="form-group">
            <label>商品名称</label>
            <input type="text" v-model="editingProduct.name" required>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>原价</label>
              <input type="number" v-model="editingProduct.originalPrice" step="0.01" required>
            </div>
            <div class="form-group">
              <label>折扣价</label>
              <input type="number" v-model="editingProduct.discountPrice" step="0.01" required>
            </div>
          </div>
          <div class="form-group">
            <label>库存</label>
            <input type="number" v-model="editingProduct.stock" required>
          </div>
          <div class="form-group">
            <label>描述</label>
            <textarea v-model="editingProduct.description"></textarea>
          </div>
        </div>
        
        <div class="modal-actions">
          <button class="btn-cancel" @click="closeEditModal">取消</button>
          <button class="btn-confirm" @click="saveEdit">保存（需重新审核）</button>
        </div>
      </div>
    </div>

    <!-- 调整库存弹窗 -->
    <div v-if="showStockModal" class="modal-overlay" @click.self="closeStockModal">
      <div class="modal-content small">
        <h3>调整库存</h3>
        <button class="close-btn" @click="closeStockModal">×</button>
        
        <div v-if="stockProduct" class="stock-form">
          <p>当前库存: {{ stockProduct.stock }}</p>
          <div class="form-group">
            <label>新库存数量</label>
            <input type="number" v-model="newStock" min="0" required>
          </div>
        </div>
        
        <div class="modal-actions">
          <button class="btn-cancel" @click="closeStockModal">取消</button>
          <button class="btn-confirm" @click="saveStock">确认调整</button>
        </div>
      </div>
    </div>

    <!-- 发货弹窗 -->
    <div v-if="showShipModal" class="modal-overlay" @click.self="closeShipModal">
      <div class="modal-content small">
        <h3>填写物流信息</h3>
        <button class="close-btn" @click="closeShipModal">×</button>
        
        <div class="ship-form">
          <div class="form-group">
            <label>物流公司</label>
            <input type="text" v-model="shipForm.logisticsCompany" placeholder="如：顺丰、圆通等" required>
          </div>
          <div class="form-group">
            <label>运单号</label>
            <input type="text" v-model="shipForm.trackingNumber" placeholder="请输入运单号" required>
          </div>
        </div>
        
        <div class="modal-actions">
          <button class="btn-cancel" @click="closeShipModal">取消</button>
          <button class="btn-confirm" @click="submitShip">确认发货</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const activeTab = ref('products')
const productTab = ref('selling')
const productList = ref<any[]>([])
const shopProducts = ref<any[]>([])
const shopInfo = reactive({ shopName: '', serviceRating: 0 })
const currentUser = ref<any>(null)

const productForm = reactive({
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

const showDetailModal = ref(false)
const showEditModal = ref(false)
const showStockModal = ref(false)
const showShipModal = ref(false)
const selectedProduct = ref<any>(null)
const editingProduct = ref<any>(null)
const stockProduct = ref<any>(null)
const newStock = ref(0)
const replyContent = ref('')

const orderTab = ref('pending')
const pendingOrders = ref<any[]>([])
const toShipOrders = ref<any[]>([])
const returnRequests = ref<any[]>([])
const refundOrders = ref<any[]>([])
const currentShipOrder = ref<any>(null)

const shipForm = reactive({
  logisticsCompany: '',
  trackingNumber: ''
})

onMounted(() => {
  loadCurrentUser()
  loadProducts(1)
  loadShopInfo()
})

const loadCurrentUser = () => {
  axios.get('/api/users/info', { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      currentUser.value = res.data.data
    }
  })
}

const loadProducts = (status: number) => {
  axios.get('/api/products/merchant', { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      let products = res.data.data
      
      if (status === 1) {
        productList.value = products.filter((p: any) => p.status === 1 && p.stock > 0)
      } else if (status === 2) {
        productList.value = products.filter((p: any) => p.status === 2)
      } else if (status === 3) {
        productList.value = products.filter((p: any) => p.status === 1 && p.stock === 0)
      } else if (status === 0) {
        productList.value = products.filter((p: any) => p.auditStatus === 0)
      }
    }
  })
}

const loadShopInfo = () => {
  axios.get('/api/products/merchant/shop', { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      shopProducts.value = res.data.data.records || []
    }
  })
  
  axios.get('/api/users/info', { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      shopInfo.shopName = res.data.data.shopName || ''
      shopInfo.serviceRating = res.data.data.serviceRating || 0
    }
  })
}

const getCategoryName = (id: number) => {
  const categories: Record<number, string> = {
    1: '电子产品',
    2: '服装',
    3: '食品',
    4: '图书',
    5: '学习用品',
    6: '运动器材',
    7: '美妆护肤',
    8: '生活用品'
  }
  return categories[id] || '其他'
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

const addProduct = async () => {
  const formData = new FormData()
  formData.append('name', productForm.name)
  formData.append('categoryId', String(productForm.categoryId))
  formData.append('originalPrice', String(productForm.originalPrice))
  formData.append('discountPrice', String(productForm.discountPrice))
  formData.append('stock', String(productForm.stock))
  formData.append('unit', productForm.unit)
  formData.append('conditionLevel', productForm.conditionLevel)
  formData.append('isNegotiable', String(productForm.isNegotiable))
  formData.append('description', productForm.description)

  for (let i = 0; i < uploadedImages.value.length; i++) {
    const img = uploadedImages.value[i]
    if (img.startsWith('data:')) {
      const matches = img.match(/^data:([^;]+);base64,(.+)$/)
      if (matches) {
        const mimeType = matches[1]
        const base64Data = matches[2]
        const binaryString = atob(base64Data)
        const bytes = new Uint8Array(binaryString.length)
        for (let j = 0; j < binaryString.length; j++) {
          bytes[j] = binaryString.charCodeAt(j)
        }
        const blob = new Blob([bytes], { type: mimeType })
        const ext = mimeType.split('/')[1] || 'jpg'
        const file = new File([blob], `product_${i}.${ext}`, { type: mimeType })
        formData.append('images', file)
      }
    } else if (img.startsWith('blob:')) {
      const response = await fetch(img)
      const blob = await response.blob()
      const mimeType = blob.type || 'image/jpeg'
      const ext = mimeType.split('/')[1] || 'jpg'
      const file = new File([blob], `product_${i}.${ext}`, { type: mimeType })
      formData.append('images', file)
    }
  }

  axios.post('/api/products/submit', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    withCredentials: true
  }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('发布成功，等待审核')
      Object.assign(productForm, {
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
      uploadedImages.value = []
    } else {
      ElMessage.error(res.data.message || '发布失败')
    }
  }).catch(err => {
    ElMessage.error('发布失败: ' + err.message)
  })
}

const viewProductDetail = (product: any) => {
  axios.get(`/api/products/${product.id}/detail`, { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      selectedProduct.value = res.data.data.product
      selectedProduct.value.images = res.data.data.images || []
      selectedProduct.value.reviews = res.data.data.reviews || []
      showDetailModal.value = true
    } else {
      ElMessage.error(res.data.message || '获取商品详情失败')
    }
  }).catch(err => {
    console.error('获取商品详情错误:', err)
    ElMessage.error('服务器错误: ' + (err.response?.data?.message || err.message || '请稍后重试'))
  })
}

const editProduct = (product: any) => {
  editingProduct.value = { ...product }
  showEditModal.value = true
}

const closeDetailModal = () => {
  showDetailModal.value = false
  selectedProduct.value = null
}

const closeEditModal = () => {
  showEditModal.value = false
  editingProduct.value = null
}

const saveEdit = () => {
  editingProduct.value.auditStatus = 0
  editingProduct.value.status = 0
  axios.put(`/api/products/${editingProduct.value.id}`, editingProduct.value, { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('更新成功，需重新审核')
      closeEditModal()
      loadProducts(1)
    }
  })
}

const offlineProduct = (productId: number) => {
  axios.put(`/api/products/${productId}/offline`, {}, { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('下架成功')
      loadProducts(1)
    }
  })
}

const deleteProduct = (productId: number) => {
  if (!confirm('确定要删除该商品吗？')) return
  axios.delete(`/api/products/${productId}`, { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      loadProducts(0)
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  })
}

const onlineProduct = (productId: number) => {
  axios.put(`/api/products/${productId}`, { status: 0, auditStatus: 0 }, { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('已提交审核')
      loadProducts(2)
    }
  })
}

const adjustStock = (product: any) => {
  stockProduct.value = product
  newStock.value = product.stock
  showStockModal.value = true
}

const closeStockModal = () => {
  showStockModal.value = false
  stockProduct.value = null
  newStock.value = 0
}

const saveStock = () => {
  axios.put(`/api/products/${stockProduct.value.id}/stock`, { stock: newStock.value }, { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('库存调整成功')
      closeStockModal()
      loadProducts(1)
    }
  })
}

const replyReview = (reviewId: number) => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  axios.post(`/api/reviews/${reviewId}/reply`, { content: replyContent.value }, { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('回复成功')
      replyContent.value = ''
      viewProductDetail(selectedProduct.value)
    }
  })
}

const formatTime = (time: string) => {
  return new Date(time).toLocaleString()
}

const loadOrders = () => {
  axios.get('/api/orders/merchant', { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      const orders = res.data.data
      pendingOrders.value = orders.filter((o: any) => o.status === 0)
      toShipOrders.value = orders.filter((o: any) => o.status === 1)
      refundOrders.value = orders.filter((o: any) => o.status === 4 || o.status === 5)
    }
  }).catch(err => {
    ElMessage.error('获取订单失败')
  })
}

const loadReturnRequests = () => {
  axios.get('/api/orders/returns', { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      returnRequests.value = res.data.data.filter((r: any) => r.status === 0)
    }
  }).catch(err => {
    ElMessage.error('获取退款申请失败')
  })
}

const confirmOrder = (orderId: number) => {
  axios.post(`/api/orders/${orderId}/confirm`, {}, { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('订单确认成功')
      loadOrders()
    } else {
      ElMessage.error(res.data.message || '确认失败')
    }
  }).catch(err => {
    ElMessage.error('确认失败')
  })
}

const openShipModal = (order: any) => {
  currentShipOrder.value = order
  shipForm.logisticsCompany = ''
  shipForm.trackingNumber = ''
  showShipModal.value = true
}

const closeShipModal = () => {
  showShipModal.value = false
  currentShipOrder.value = null
}

const submitShip = () => {
  if (!shipForm.logisticsCompany || !shipForm.trackingNumber) {
    ElMessage.warning('请填写完整物流信息')
    return
  }
  
  axios.post(`/api/orders/${currentShipOrder.value.id}/ship`, {
    logisticsCompany: shipForm.logisticsCompany,
    trackingNumber: shipForm.trackingNumber
  }, { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('发货成功')
      closeShipModal()
      loadOrders()
    } else {
      ElMessage.error(res.data.message || '发货失败')
    }
  }).catch(err => {
    ElMessage.error('发货失败')
  })
}

const approveReturn = (requestId: number) => {
  axios.post(`/api/orders/returns/${requestId}/approve`, {}, { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('同意退款成功')
      loadReturnRequests()
      loadOrders()
    } else {
      ElMessage.error(res.data.message || '操作失败')
    }
  }).catch(err => {
    ElMessage.error('操作失败')
  })
}

const rejectReturn = (requestId: number) => {
  const reason = prompt('请输入拒绝退款的原因')
  if (reason) {
    axios.post(`/api/orders/returns/${requestId}/reject`, { reason }, { withCredentials: true }).then(res => {
      if (res.data.code === 200) {
        ElMessage.success('拒绝退款成功')
        loadReturnRequests()
      } else {
        ElMessage.error(res.data.message || '操作失败')
      }
    }).catch(err => {
      ElMessage.error('操作失败')
    })
  }
}

const confirmRefund = (orderId: number) => {
  if (confirm('确认要执行退款吗？')) {
    axios.post(`/api/orders/${orderId}/refund`, {}, { withCredentials: true }).then(res => {
      if (res.data.code === 200) {
        ElMessage.success('退款成功')
        loadOrders()
      } else {
        ElMessage.error(res.data.message || '退款失败')
      }
    }).catch(err => {
      ElMessage.error('退款失败')
    })
  }
}
</script>

<style scoped>
.merchant-center {
  padding: 2rem;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.btn-back {
  padding: 0.5rem 1rem;
  background-color: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  color: #333;
  text-decoration: none;
}

.btn-back:hover {
  background-color: #f5f5f5;
}

.merchant-center h2 {
  margin-bottom: 0;
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
  font-size: 1rem;
}

.tabs button.active {
  background-color: #e74c3c;
  color: #fff;
}

.product-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.product-tabs button {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  background: #fff;
  cursor: pointer;
  border-radius: 4px;
}

.product-tabs button.active {
  background-color: #3498db;
  color: #fff;
  border-color: #3498db;
}

.tab-content {
  background-color: #fff;
  padding: 1.5rem;
  border-radius: 8px;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: #999;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1rem;
}

.product-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
}

.product-image {
  height: 200px;
  background-color: #f5f5f5;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  padding: 1rem;
}

.product-info h3 {
  margin-bottom: 0.5rem;
  font-size: 1.1rem;
}

.price {
  color: #e74c3c;
  font-weight: bold;
}

.original-price {
  color: #999;
  text-decoration: line-through;
  margin-left: 0.5rem;
}

.stock {
  color: #666;
  font-size: 0.9rem;
}

.product-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
  flex-wrap: wrap;
}

.product-actions button {
  padding: 0.4rem 0.8rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.85rem;
}

.btn-detail {
  background-color: #3498db;
  color: #fff;
}

.btn-offline {
  background-color: #e74c3c;
  color: #fff;
}

.btn-delete {
  background-color: #e74c3c;
  color: #fff;
}

.btn-online {
  background-color: #27ae60;
  color: #fff;
}

.btn-edit {
  background-color: #95a5a6;
  color: #fff;
}

.btn-stock {
  background-color: #f39c12;
  color: #fff;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
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
  box-sizing: border-box;
}

.form-group textarea {
  height: 100px;
}

.required {
  color: #e74c3c;
}

.radio-group {
  display: flex;
  gap: 1.5rem;
}

.radio-group label {
  display: flex;
  align-items: center;
  gap: 0.3rem;
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

.shop-header {
  margin-bottom: 2rem;
}

.shop-header h3 {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
}

.service-rating {
  color: #f39c12;
  font-size: 1.1rem;
}

.shop-products {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 1rem;
}

.shop-product-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
}

.shop-product-card img {
  width: 100%;
  height: 150px;
  object-fit: cover;
}

.shop-product-info {
  padding: 1rem;
}

.shop-product-info h4 {
  margin-bottom: 0.3rem;
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
  z-index: 1000;
}

.modal-content {
  background-color: #fff;
  padding: 1.5rem;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  position: relative;
}

.modal-content.large {
  max-width: 800px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-content.small {
  max-width: 350px;
}

.close-btn {
  position: absolute;
  top: 1rem;
  right: 1rem;
  width: 32px;
  height: 32px;
  background-color: #eee;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 1.2rem;
}

.product-detail {
  margin-top: 1rem;
}

.detail-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.detail-images img {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 4px;
}

.detail-info {
  margin-bottom: 1.5rem;
}

.detail-info h4 {
  font-size: 1.3rem;
  margin-bottom: 0.5rem;
}

.review-section {
  border-top: 1px solid #eee;
  padding-top: 1rem;
}

.review-section h4 {
  margin-bottom: 1rem;
}

.no-reviews {
  text-align: center;
  color: #999;
  padding: 2rem;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.review-item {
  padding: 1rem;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.reviewer {
  font-weight: bold;
}

.review-rating {
  color: #f39c12;
}

.review-time {
  margin-left: auto;
  color: #999;
  font-size: 0.85rem;
}

.review-content {
  margin-bottom: 0.5rem;
}

.review-reply {
  background-color: #fff;
  padding: 0.5rem;
  border-radius: 4px;
  margin-top: 0.5rem;
}

.reply-label {
  font-weight: bold;
  color: #3498db;
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

.stock-form {
  margin-top: 1rem;
}

.order-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.order-tabs button {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  background: #fff;
  cursor: pointer;
  border-radius: 4px;
}

.order-tabs button.active {
  background-color: #3498db;
  color: #fff;
  border-color: #3498db;
}

.order-content {
  margin-top: 1rem;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.order-item {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 1rem;
}

.order-info {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 1rem;
  font-size: 0.9rem;
}

.order-items {
  margin-bottom: 1rem;
}

.order-product {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.order-product img {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}

.return-reason {
  margin-bottom: 1rem;
  padding: 0.5rem;
  background-color: #fff3f3;
  border-radius: 4px;
}

.order-actions button {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 0.5rem;
  font-size: 0.9rem;
}

.order-actions button:first-child {
  background-color: #e74c3c;
  color: #fff;
}

.order-actions button:nth-child(2) {
  background-color: #95a5a6;
  color: #fff;
}

.ship-form {
  margin-top: 1rem;
}
</style>