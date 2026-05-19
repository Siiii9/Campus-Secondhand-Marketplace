<template>
  <div class="profile">
    <div class="profile-header">
      <div class="header-left">
        <button class="back-btn" @click="$router.push('/')">← 返回首页</button>
        <h2>个人中心</h2>
      </div>
      <div class="header-right">
        <button v-if="!isEditing" class="edit-btn" @click="startEdit">编辑资料</button>
        <button v-else class="save-btn" @click="saveEdit">保存</button>
      </div>
    </div>
    
    <div class="user-info">
      <div class="info-item">
        <label>用户名</label>
        <span>{{ userInfo.username }}</span>
      </div>
      <div class="info-item">
        <label>姓名</label>
        <input v-if="isEditing" v-model="editForm.realName" type="text" />
        <span v-else>{{ userInfo.realName }}</span>
      </div>
      <div class="info-item">
        <label>手机号</label>
        <input v-if="isEditing" v-model="editForm.phone" type="text" />
        <span v-else>{{ userInfo.phone }}</span>
      </div>
      <div class="info-item">
        <label>邮箱</label>
        <input v-if="isEditing" v-model="editForm.email" type="email" />
        <span v-else>{{ userInfo.email }}</span>
      </div>
      <div class="info-item">
        <label>城市</label>
        <input v-if="isEditing" v-model="editForm.city" type="text" />
        <span v-else>{{ userInfo.city }}</span>
      </div>
      <div class="info-item">
        <label>性别</label>
        <select v-if="isEditing" v-model="editForm.gender">
          <option value="男">男</option>
          <option value="女">女</option>
        </select>
        <span v-else>{{ userInfo.gender || '未设置' }}</span>
      </div>
      <div class="info-item">
        <label>银行账号</label>
        <input v-if="isEditing" v-model="editForm.bankAccount" type="text" />
        <span v-else>{{ formatBankAccount(userInfo.bankAccount) }}</span>
      </div>
      <div class="info-item">
        <label>角色</label>
        <span>{{ getRoleText(userInfo.role) }}</span>
      </div>
      <div class="info-item" v-if="userInfo.role === 'MERCHANT'">
        <label>商家等级</label>
        <span>等级{{ userInfo.merchantLevel }} ({{ getMerchantRate(userInfo.merchantLevel) }}%手续费)</span>
      </div>
      <div class="info-item" v-if="userInfo.role === 'MERCHANT'">
        <label>店铺名称</label>
        <input v-if="isEditing" v-model="editForm.shopName" type="text" />
        <span v-else>{{ userInfo.shopName }}</span>
      </div>
      <div class="info-item">
        <label>状态</label>
        <span :class="getStatusClass(userInfo.status)">{{ getStatusText(userInfo.status) }}</span>
      </div>
    </div>

    <div class="wallet-section">
      <h3>钱包余额</h3>
      <div class="balance">¥{{ wallet.balance.toFixed(2) }}</div>
      <button @click="recharge">充值</button>
    </div>

    <div class="points-section">
      <h3>积分</h3>
      <div class="points">{{ points.points }} 积分</div>
      <div class="points-desc">1元消费 = 1积分，100积分 = 1元抵扣</div>
    </div>

    <button class="logout-btn" @click="logout">退出登录</button>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const userInfo = ref({
  id: 0,
  username: '',
  realName: '',
  phone: '',
  email: '',
  city: '',
  gender: '',
  bankAccount: '',
  role: '',
  status: 0,
  merchantLevel: 0,
  shopName: ''
})

const editForm = reactive({
  realName: '',
  phone: '',
  email: '',
  city: '',
  gender: '',
  bankAccount: '',
  shopName: ''
})

interface WalletData {
  balance: number;
}

interface PointsData {
  points: number;
}

const wallet = ref<WalletData>({ balance: 0 })
const points = ref<PointsData>({ points: 0 })
const isEditing = ref(false)

onMounted(() => {
  loadUserInfo()
})

const loadUserInfo = () => {
  axios.get('/api/users/info', { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      userInfo.value = res.data.data
      initEditForm()
    }
  }).catch(err => {
    console.error('加载用户信息失败', err)
  })
  
  axios.get('/api/wallet', { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      wallet.value = res.data.data
    }
  }).catch(err => {
    console.error('加载钱包信息失败', err)
  })

  axios.get('/api/points', { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      points.value = res.data.data
    }
  }).catch(err => {
    console.error('加载积分信息失败', err)
  })
}

const initEditForm = () => {
  editForm.realName = userInfo.value.realName
  editForm.phone = userInfo.value.phone
  editForm.email = userInfo.value.email
  editForm.city = userInfo.value.city
  editForm.gender = userInfo.value.gender
  editForm.bankAccount = userInfo.value.bankAccount
  editForm.shopName = userInfo.value.shopName || ''
}

const startEdit = () => {
  initEditForm()
  isEditing.value = true
}

const saveEdit = () => {
  axios.put('/api/users/info', editForm, { withCredentials: true }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('修改成功')
      loadUserInfo()
      isEditing.value = false
    } else {
      ElMessage.error(res.data.message || '修改失败')
    }
  }).catch(err => {
    ElMessage.error('修改失败')
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

const getGenderText = (gender: string) => {
  return gender === 'MALE' ? '男' : gender === 'FEMALE' ? '女' : ''
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

const getMerchantRate = (level: number) => {
  const rateMap: Record<number, string> = {
    1: '0.1',
    2: '0.2',
    3: '0.5',
    4: '0.75',
    5: '1'
  }
  return rateMap[level] || '0.1'
}

const formatBankAccount = (account: string) => {
  if (!account) return ''
  return account.slice(0, 4) + '****' + account.slice(-4)
}

const recharge = () => {
  const amount = prompt('请输入充值金额')
  if (amount && parseFloat(amount) > 0) {
    axios.post('/api/wallet/recharge', {}, { 
      params: { amount },
      withCredentials: true 
    }).then(res => {
      if (res.data.code === 200) {
        ElMessage.success('充值成功')
        wallet.value.balance = parseFloat(wallet.value.balance) + parseFloat(amount)
      } else {
        ElMessage.error(res.data.message || '充值失败')
      }
    })
  }
}

const logout = () => {
  axios.post('/api/users/logout', {}, { withCredentials: true }).then(() => {
    localStorage.clear()
    window.location.href = '/login'
  })
}
</script>

<style scoped>
.profile {
  padding: 2rem;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.back-btn {
  padding: 0.5rem 1rem;
  background-color: #95a5a6;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.edit-btn, .save-btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.edit-btn {
  background-color: #3498db;
  color: #fff;
}

.save-btn {
  background-color: #27ae60;
  color: #fff;
}

.user-info {
  background-color: #fff;
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid #eee;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item label {
  color: #666;
  flex: 1;
}

.info-item span, .info-item input, .info-item select {
  flex: 2;
  text-align: right;
}

.info-item input, .info-item select {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
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

.wallet-section, .points-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.wallet-section h3, .points-section h3 {
  margin-bottom: 1rem;
}

.balance {
  font-size: 2rem;
  color: #e74c3c;
  font-weight: bold;
  margin-bottom: 1rem;
}

.wallet-section button {
  padding: 0.5rem 1.5rem;
  background-color: #e74c3c;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.points {
  font-size: 1.5rem;
  color: #f39c12;
  font-weight: bold;
}

.points-desc {
  font-size: 0.85rem;
  color: #999;
  margin-top: 0.5rem;
}

.logout-btn {
  width: 100%;
  padding: 0.75rem;
  background-color: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  color: #666;
}
</style>