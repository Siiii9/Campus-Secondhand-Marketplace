<template>
  <div class="admin">
    <div class="header">
      <h2>管理后台</h2>
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </div>
    
    <div class="tabs">
      <button :class="{ active: activeTab === 'user-audit' }" @click="switchTab('user-audit')">用户审核</button>
      <button :class="{ active: activeTab === 'merchant-audit' }" @click="switchTab('merchant-audit')">商家审核</button>
      <button :class="{ active: activeTab === 'user-list' }" @click="switchTab('user-list')">用户管理</button>
      <button :class="{ active: activeTab === 'product-audit' }" @click="switchTab('product-audit')">商品审核</button>
    </div>

    <!-- 用户审核 -->
    <div v-if="activeTab === 'user-audit'" class="tab-content">
      <div class="filter-bar">
        <span>待审核用户列表</span>
      </div>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>姓名</th>
            <th>手机号</th>
            <th>角色</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in pendingUsers" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.realName }}</td>
            <td>{{ user.phone }}</td>
            <td>{{ getRoleText(user.role) }}</td>
            <td>{{ formatTime(user.createdAt) }}</td>
            <td>
              <button class="btn-approve" @click="auditUser(user.id, 1)">通过</button>
              <button class="btn-reject" @click="showRejectModal(user.id)">拒绝</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 商家审核 -->
    <div v-if="activeTab === 'merchant-audit'" class="tab-content">
      <div class="filter-bar">
        <span>待审核商家列表</span>
      </div>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>姓名</th>
            <th>店铺名称</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="merchant in pendingMerchants" :key="merchant.id">
            <td>{{ merchant.id }}</td>
            <td>{{ merchant.username }}</td>
            <td>{{ merchant.realName }}</td>
            <td>{{ merchant.shopName || '未设置' }}</td>
            <td>{{ formatTime(merchant.createdAt) }}</td>
            <td>
              <button class="btn-detail" @click="showMerchantDetail(merchant.id)">查看证件</button>
              <button class="btn-approve" @click="auditUser(merchant.id, 1)">通过</button>
              <button class="btn-reject" @click="showRejectModal(merchant.id)">拒绝</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 用户管理 -->
    <div v-if="activeTab === 'user-list'" class="tab-content">
      <div class="filter-bar">
        <input v-model="searchKeyword" placeholder="搜索用户名/姓名/手机号" class="search-input" />
        <select v-model="filterRole" class="filter-select">
          <option value="">全部角色</option>
          <option value="USER">普通用户</option>
          <option value="MERCHANT">商家</option>
          <option value="ADMIN">管理员</option>
        </select>
        <button class="btn-search" @click="loadUserList">搜索</button>
      </div>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>姓名</th>
            <th>角色</th>
            <th>商家等级</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in userList" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.realName }}</td>
            <td>{{ getRoleText(user.role) }}</td>
            <td>
              <span v-if="user.role === 'MERCHANT'">等级{{ user.merchantLevel }}</span>
              <span v-else>-</span>
            </td>
            <td><span :class="getStatusClass(user.status)">{{ getStatusText(user.status) }}</span></td>
            <td>
              <button class="btn-edit" @click="showUserEdit(user)">编辑</button>
              <button class="btn-recharge" @click="showRechargeModal(user)">充值</button>
              <button v-if="user.role === 'MERCHANT'" class="btn-level" @click="openLevelModal(user)">调整等级</button>
              <button v-if="user.role === 'MERCHANT' && user.shopStatus !== 0" class="btn-close-shop" @click="closeShop(user.id)">关闭店铺</button>
              <button v-if="user.role === 'MERCHANT' && user.shopStatus === 0" class="btn-open-shop" @click="openShop(user.id)">恢复店铺</button>
              <button class="btn-delete" @click="deleteUser(user.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 商品审核 -->
    <div v-if="activeTab === 'product-audit'" class="tab-content">
      <div class="filter-bar">
        <span>待审核商品列表</span>
      </div>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>缩略图</th>
            <th>商品名</th>
            <th>商家</th>
            <th>价格</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="product in pendingProducts" :key="product.id">
            <td>{{ product.id }}</td>
            <td><img :src="product.images?.[0] || '/images/default.jpg'" alt="" class="product-thumb" /></td>
            <td>{{ product.name }}</td>
            <td>{{ product.shopName || product.merchantId }}</td>
            <td>¥{{ product.discountPrice }}</td>
            <td>{{ getProductStatus(product.auditStatus) }}</td>
            <td>
              <button class="btn-detail" @click="showProductDetail(product.id)">查看详情</button>
              <button class="btn-approve" @click="auditProduct(product.id, 1)">通过</button>
              <button class="btn-reject" @click="showProductRejectModal(product.id)">拒绝</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 拒绝审核弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h3>审核拒绝</h3>
        <textarea v-model="rejectReason" placeholder="请输入拒绝原因"></textarea>
        <div class="modal-actions">
          <button class="btn-cancel" @click="closeModal">取消</button>
          <button class="btn-confirm" @click="confirmReject">确认拒绝</button>
        </div>
      </div>
    </div>

    <!-- 商家证件详情弹窗 -->
    <div v-if="showMerchantModal" class="modal-overlay" @click.self="closeMerchantModal">
      <div class="modal-content large">
        <h3>商家证件详情</h3>
        <div v-if="merchantDetail" class="merchant-detail">
          <div class="detail-row">
            <span class="label">用户名：</span>
            <span>{{ merchantDetail.username }}</span>
          </div>
          <div class="detail-row">
            <span class="label">姓名：</span>
            <span>{{ merchantDetail.realName }}</span>
          </div>
          <div class="detail-row">
            <span class="label">手机号：</span>
            <span>{{ merchantDetail.phone }}</span>
          </div>
          <div class="detail-row">
            <span class="label">营业执照：</span>
            <img v-if="merchantDetail.businessLicense" :src="merchantDetail.businessLicense" alt="营业执照" class="doc-image" />
            <span v-else>未上传</span>
          </div>
          <div class="detail-row">
            <span class="label">身份证正面：</span>
            <img v-if="merchantDetail.idCardFront" :src="merchantDetail.idCardFront" alt="身份证正面" class="doc-image" />
            <span v-else>未上传</span>
          </div>
          <div class="detail-row">
            <span class="label">身份证反面：</span>
            <img v-if="merchantDetail.idCardBack" :src="merchantDetail.idCardBack" alt="身份证反面" class="doc-image" />
            <span v-else>未上传</span>
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn-confirm" @click="closeMerchantModal">关闭</button>
        </div>
      </div>
    </div>

    <!-- 用户编辑弹窗 -->
    <div v-if="showEditModal" class="modal-overlay" @click.self="closeEditModal">
      <div class="modal-content">
        <h3>编辑用户信息</h3>
        <div v-if="editingUser" class="form-group">
          <label>用户名</label>
          <input v-model="editingUser.username" disabled />
          <label>姓名</label>
          <input v-model="editingUser.realName" />
          <label>手机号</label>
          <input v-model="editingUser.phone" />
          <label>邮箱</label>
          <input v-model="editingUser.email" />
          <label>状态</label>
          <select v-model="editingUser.status">
            <option :value="0">待审核</option>
            <option :value="1">已激活</option>
            <option :value="2">已拒绝</option>
            <option :value="3">已封禁</option>
          </select>
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="closeEditModal">取消</button>
          <button class="btn-confirm" @click="confirmEdit">确认修改</button>
        </div>
      </div>
    </div>

    <!-- 商家等级调整弹窗 -->
    <div v-if="showLevelModal" class="modal-overlay" @click.self="closeLevelModal">
      <div class="modal-content">
        <h3>调整商家等级</h3>
        <div v-if="levelUser" class="form-group">
          <label>商家：{{ levelUser.username }} ({{ levelUser.realName }})</label>
          <label>当前等级：等级{{ levelUser.merchantLevel }}</label>
          <label>新等级</label>
          <select v-model="newLevel">
            <option :value="1">等级1 (0.1%手续费)</option>
            <option :value="2">等级2 (0.2%手续费)</option>
            <option :value="3">等级3 (0.5%手续费)</option>
            <option :value="4">等级4 (0.75%手续费)</option>
            <option :value="5">等级5 (1%手续费)</option>
          </select>
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="closeLevelModal">取消</button>
          <button class="btn-confirm" @click="confirmLevelChange">确认调整</button>
        </div>
      </div>
    </div>

    <!-- 钱包充值弹窗 -->
    <div v-if="showRechargeModalFlag" class="modal-overlay" @click.self="closeRechargeModal">
      <div class="modal-content">
        <h3>用户钱包充值</h3>
        <div v-if="rechargeUser" class="form-group">
          <label>用户：{{ rechargeUser.username }} ({{ rechargeUser.realName }})</label>
          <label>充值金额（元）</label>
          <input v-model.number="rechargeAmount" type="number" min="1" placeholder="请输入充值金额" />
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="closeRechargeModal">取消</button>
          <button class="btn-confirm" @click="confirmRecharge">确认充值</button>
        </div>
      </div>
    </div>

    <!-- 商品详情弹窗 -->
    <div v-if="showProductModal" class="modal-overlay" @click.self="closeProductModal">
      <div class="modal-content large">
        <h3>商品详情</h3>
        <div v-if="productDetail" class="product-detail-modal">
          <div class="product-images-modal">
            <img v-for="(img, idx) in productDetail.images" :key="idx" :src="img" alt="" class="product-img" />
          </div>
          <div class="product-info-modal">
            <div class="detail-row">
              <span class="label">商品名称：</span>
              <span>{{ productDetail.name }}</span>
            </div>
            <div class="detail-row">
              <span class="label">类别：</span>
              <span>{{ productDetail.categoryName }}</span>
            </div>
            <div class="detail-row">
              <span class="label">原价：</span>
              <span>¥{{ productDetail.originalPrice }}</span>
            </div>
            <div class="detail-row">
              <span class="label">折后价：</span>
              <span class="price">¥{{ productDetail.discountPrice }}</span>
            </div>
            <div class="detail-row">
              <span class="label">尺寸：</span>
              <span>{{ productDetail.unit || '未填写' }}</span>
            </div>
            <div class="detail-row">
              <span class="label">新旧程度：</span>
              <span>{{ productDetail.conditionLevel }}</span>
            </div>
            <div class="detail-row">
              <span class="label">库存：</span>
              <span>{{ productDetail.stock }}</span>
            </div>
            <div class="detail-row">
              <span class="label">是否议价：</span>
              <span>{{ productDetail.isNegotiable === 1 ? '是' : '否' }}</span>
            </div>
            <div class="detail-row">
              <span class="label">使用说明：</span>
              <p>{{ productDetail.description }}</p>
            </div>
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="closeProductModal">关闭</button>
          <button class="btn-approve" @click="auditProduct(productDetail.id, 1)">通过审核</button>
          <button class="btn-reject" @click="showProductRejectModal(productDetail.id)">拒绝审核</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const activeTab = ref('user-audit')
const pendingUsers = ref<any[]>([])
const pendingMerchants = ref<any[]>([])
const userList = ref<any[]>([])
const pendingProducts = ref<any[]>([])

const searchKeyword = ref('')
const filterRole = ref('')

const showModal = ref(false)
const showMerchantModal = ref(false)
const showEditModal = ref(false)
const showLevelModal = ref(false)
const showProductModal = ref(false)
const showRechargeModalFlag = ref(false)

const rejectReason = ref('')
const currentAuditId = ref(0)
const currentProductId = ref(0)

const merchantDetail = ref<any>(null)
const rechargeUser = ref<any>(null)
const rechargeAmount = ref(100)
const productDetail = ref<any>(null)
const editingUser = reactive({})
const levelUser = reactive({})
const newLevel = ref(1)

const switchTab = (tab: string) => {
  activeTab.value = tab
  if (tab === 'user-audit') loadPendingUsers()
  if (tab === 'merchant-audit') loadPendingMerchants()
  if (tab === 'user-list') loadUserList()
  if (tab === 'product-audit') loadPendingProducts()
}

const loadPendingUsers = () => {
  axios.get('/api/admin/users/pending', { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      pendingUsers.value = res.data.data.filter((u: any) => u.role === 'USER')
    }
  })
}

const loadPendingMerchants = () => {
  axios.get('/api/admin/users/pending', { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      pendingMerchants.value = res.data.data.filter((u: any) => u.role === 'MERCHANT')
    }
  })
}

const loadUserList = () => {
  axios.get('/api/admin/users', { 
    params: { keyword: searchKeyword.value, role: filterRole.value },
    withCredentials: true 
  }).then(res => {
    if (res.data.code === 200) {
      userList.value = res.data.data
    }
  })
}

const loadPendingProducts = () => {
  axios.get('/api/products/search', { 
    params: { page: 1, size: 100, auditStatus: 0 },
    withCredentials: true 
  }).then(res => {
    if (res.data.code === 200) {
      pendingProducts.value = res.data.data.records
    }
  })
}

const getRoleText = (role: string) => {
  const roleMap: Record<string, string> = {
    'USER': '普通用户',
    'MERCHANT': '商家',
    'ADMIN': '管理员'
  }
  return roleMap[role] || role
}

const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '待审核',
    1: '已激活',
    2: '已拒绝',
    3: '已封禁'
  }
  return statusMap[status] || '未知'
}

const getStatusClass = (status: number) => {
  const classMap: Record<number, string> = {
    0: 'status-pending',
    1: 'status-active',
    2: 'status-rejected',
    3: 'status-banned'
  }
  return classMap[status] || ''
}

const getProductStatus = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝'
  }
  return statusMap[status] || '未知'
}

const formatTime = (time: string) => {
  if (!time) return ''
  return time.replace('T', ' ')
}

const auditUser = (userId: number, status: number) => {
  axios.post(`/api/admin/users/${userId}/audit`, {}, { 
    params: { status },
    withCredentials: true 
  }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success(status === 1 ? '审核通过' : '审核拒绝')
      loadPendingUsers()
      loadPendingMerchants()
      loadUserList()
    }
  })
}

const showRejectModal = (userId: number) => {
  currentAuditId.value = userId
  rejectReason.value = ''
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  rejectReason.value = ''
}

const confirmReject = () => {
  axios.post(`/api/admin/users/${currentAuditId.value}/audit`, {}, { 
    params: { status: 2, remark: rejectReason.value },
    withCredentials: true 
  }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('审核拒绝成功')
      closeModal()
      loadPendingUsers()
      loadPendingMerchants()
      loadUserList()
    }
  })
}

const showMerchantDetail = (userId: number) => {
  axios.get(`/api/admin/users/${userId}/audit-detail`, { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      merchantDetail.value = res.data.data
      showMerchantModal.value = true
    }
  })
}

const closeMerchantModal = () => {
  showMerchantModal.value = false
  merchantDetail.value = null
}

const showUserEdit = (user: any) => {
  Object.assign(editingUser, user)
  showEditModal.value = true
}

const closeEditModal = () => {
  showEditModal.value = false
  Object.keys(editingUser).forEach(key => delete editingUser[key])
}

const confirmEdit = () => {
  axios.put(`/api/admin/users/${editingUser.id}`, editingUser, { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('修改成功')
      closeEditModal()
      loadUserList()
    }
  })
}

const openLevelModal = (user: any) => {
  Object.assign(levelUser, user)
  newLevel.value = user.merchantLevel || 1
  showLevelModal.value = true
}

const closeLevelModal = () => {
  showLevelModal.value = false
  Object.keys(levelUser).forEach(key => delete levelUser[key])
}

const confirmLevelChange = () => {
  axios.post(`/api/admin/users/${levelUser.id}/level`, {}, { 
    params: { level: newLevel.value },
    withCredentials: true 
  }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('等级调整成功')
      closeLevelModal()
      loadUserList()
    }
  })
}

const showRechargeModal = (user: any) => {
  rechargeUser.value = { ...user }
  showRechargeModalFlag.value = true
}

const closeRechargeModal = () => {
  showRechargeModalFlag.value = false
  rechargeUser.value = null
  rechargeAmount.value = 100
}

const confirmRecharge = () => {
  if (!rechargeAmount.value || rechargeAmount.value <= 0) {
    ElMessage.error('请输入有效的充值金额')
    return
  }
  
  axios.post(`/api/admin/users/${rechargeUser.value.id}/recharge`, { 
    amount: rechargeAmount.value 
  }, { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('充值成功')
      closeRechargeModal()
    } else {
      ElMessage.error(res.data.message)
    }
  }).catch(err => {
    ElMessage.error(err.response?.data?.message || '充值失败')
  })
}

const closeShop = (userId: number) => {
  if (confirm('确定要关闭该商家店铺吗？')) {
    axios.post(`/api/admin/users/${userId}/close-shop`, {}, { withCredentials: true }).then(res => {
      if (res.data.code === 200) {
        ElMessage.success('店铺已关闭')
        loadUserList()
      }
    })
  }
}

const openShop = (userId: number) => {
  if (confirm('确定要恢复该商家店铺吗？')) {
    axios.post(`/api/admin/users/${userId}/open-shop`, {}, { withCredentials: true }).then(res => {
      if (res.data.code === 200) {
        ElMessage.success('店铺已恢复')
        loadUserList()
      }
    })
  }
}

const deleteUser = (userId: number) => {
  if (confirm('确定要删除该用户吗？')) {
    axios.delete(`/api/admin/users/${userId}`, { withCredentials: true }).then(res => {
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        loadUserList()
      }
    })
  }
}

const showProductRejectModal = (productId: number) => {
  currentProductId.value = productId
  rejectReason.value = ''
  showModal.value = true
}

const showProductDetail = (productId: number) => {
  axios.get(`/api/products/${productId}/detail`, { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      productDetail.value = res.data.data.product
      productDetail.value.images = res.data.data.images || []
      showProductModal.value = true
    } else {
      ElMessage.error(res.data.message || '获取商品详情失败')
    }
  }).catch(err => {
    console.error('获取商品详情错误:', err)
    ElMessage.error('服务器错误: ' + (err.response?.data?.message || err.message || '请稍后重试'))
  })
}

const closeProductModal = () => {
  showProductModal.value = false
  productDetail.value = null
}

const auditProduct = (productId: number, status: number) => {
  axios.post(`/api/products/${productId}/audit`, {}, { 
    params: { status },
    withCredentials: true 
  }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success(status === 1 ? '审核通过' : '审核拒绝')
      loadPendingProducts()
      closeProductModal()
    }
  })
}

const handleLogout = () => {
  axios.post('/api/users/logout', {}, { withCredentials: true }).then(res => {
    localStorage.clear()
    ElMessage.success('退出成功')
    router.push('/login')
  }).catch(() => {
    localStorage.clear()
    ElMessage.success('退出成功')
    router.push('/login')
  })
}

switchTab('user-audit')
</script>

<style scoped>
.admin {
  padding: 2rem;
}

.admin .header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.admin h2 {
  margin-bottom: 0;
}

.logout-btn {
  padding: 0.75rem 1.5rem;
  background-color: #e74c3c;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.logout-btn:hover {
  background-color: #c0392b;
}

.tabs {
  margin-bottom: 1.5rem;
  display: flex;
  gap: 0.5rem;
}

.tabs button {
  padding: 0.75rem 1.5rem;
  border: none;
  background: #fff;
  cursor: pointer;
  border-radius: 4px;
}

.tabs button.active {
  background-color: #e74c3c;
  color: #fff;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.search-input {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 200px;
}

.filter-select {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.btn-search {
  padding: 0.5rem 1rem;
  background-color: #3498db;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
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

.btn-approve {
  background-color: #27ae60;
  color: #fff;
}

.btn-reject {
  background-color: #e74c3c;
  color: #fff;
}

.btn-detail {
  background-color: #3498db;
  color: #fff;
}

.btn-edit {
  background-color: #3498db;
  color: #fff;
}

.btn-recharge {
  background-color: #27ae60;
  color: #fff;
}

.btn-level {
  background-color: #f39c12;
  color: #fff;
}

.btn-close-shop {
  background-color: #9b59b6;
  color: #fff;
}

.btn-open-shop {
  background-color: #27ae60;
  color: #fff;
}

.btn-delete {
  background-color: #95a5a6;
  color: #fff;
}

.status-pending {
  color: #f39c12;
}

.status-active {
  color: #27ae60;
}

.status-rejected {
  color: #e74c3c;
}

.status-banned {
  color: #95a5a6;
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

.modal-content.large {
  width: 600px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-content h3 {
  margin-bottom: 1rem;
}

.modal-content textarea {
  width: 100%;
  height: 80px;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin-bottom: 1rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
}

.form-group input, .form-group select {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.form-group input:disabled {
  background-color: #f5f5f5;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
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

.merchant-detail {
  margin-bottom: 1rem;
}

.detail-row {
  margin-bottom: 0.5rem;
}

.detail-row .label {
  font-weight: bold;
}

.doc-image {
  max-width: 100%;
  max-height: 200px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.product-thumb {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}

.product-detail-modal {
  display: flex;
  gap: 2rem;
  margin-bottom: 1rem;
}

.product-images-modal {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  max-width: 200px;
}

.product-img {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 4px;
}

.product-info-modal {
  flex: 1;
}

.product-info-modal .price {
  color: #e74c3c;
  font-weight: bold;
  font-size: 1.2rem;
}

.product-info-modal .detail-row p {
  margin: 0;
  padding-left: 80px;
}
</style>