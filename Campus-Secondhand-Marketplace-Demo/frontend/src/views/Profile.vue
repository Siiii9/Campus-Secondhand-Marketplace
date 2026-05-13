<template>
  <div class="profile">
    <h2>个人中心</h2>
    
    <div class="user-info">
      <div class="info-item">
        <label>用户名</label>
        <span>{{ userInfo.username }}</span>
      </div>
      <div class="info-item">
        <label>姓名</label>
        <span>{{ userInfo.realName }}</span>
      </div>
      <div class="info-item">
        <label>手机号</label>
        <span>{{ userInfo.phone }}</span>
      </div>
      <div class="info-item">
        <label>邮箱</label>
        <span>{{ userInfo.email }}</span>
      </div>
      <div class="info-item">
        <label>角色</label>
        <span>{{ getRoleText(userInfo.role) }}</span>
      </div>
    </div>

    <div class="wallet-section">
      <h3>钱包余额</h3>
      <div class="balance">¥{{ wallet.balance }}</div>
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
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const userInfo = ref({
  username: '',
  realName: '',
  phone: '',
  email: '',
  role: ''
})

const wallet = ref({ balance: 0 })
const points = ref({ points: 0 })

onMounted(() => {
  axios.get('/api/users/info').then(res => {
    userInfo.value = res.data.data
  })
  
  axios.get('/api/wallet').then(res => {
    wallet.value = res.data.data
  })
})

const getRoleText = (role: string) => {
  const roleMap: Record<string, string> = {
    'USER': '普通用户',
    'MERCHANT': '商家',
    'ADMIN': '管理员'
  }
  return roleMap[role] || role
}

const recharge = () => {
  const amount = prompt('请输入充值金额')
  if (amount && parseFloat(amount) > 0) {
    axios.post('/api/wallet/recharge', {}, { params: { amount } }).then(res => {
      if (res.data.code === 200) {
        ElMessage.success('充值成功')
        wallet.value.balance = parseFloat(wallet.value.balance) + parseFloat(amount)
      }
    })
  }
}

const logout = () => {
  axios.post('/api/users/logout').then(() => {
    localStorage.clear()
    window.location.href = '/login'
  })
}
</script>

<style scoped>
.profile {
  padding: 2rem;
}

.profile h2 {
  margin-bottom: 1.5rem;
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
  padding: 0.75rem 0;
  border-bottom: 1px solid #eee;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item label {
  color: #666;
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