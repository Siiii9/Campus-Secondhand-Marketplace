<template>
  <div class="admin">
    <h2>管理后台</h2>
    
    <div class="tabs">
      <button :class="{ active: activeTab === 'users' }" @click="activeTab = 'users'">用户管理</button>
      <button :class="{ active: activeTab === 'products' }" @click="activeTab = 'products'">商品审核</button>
    </div>

    <div v-if="activeTab === 'users'" class="tab-content">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>姓名</th>
            <th>角色</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.realName }}</td>
            <td>{{ getRoleText(user.role) }}</td>
            <td>{{ getStatusText(user.status) }}</td>
            <td>
              <button v-if="user.status === 0" @click="auditUser(user.id, 1)">通过</button>
              <button v-if="user.status === 0" @click="auditUser(user.id, 2)">拒绝</button>
              <button @click="deleteUser(user.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="activeTab === 'products'" class="tab-content">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>商品名</th>
            <th>商家</th>
            <th>价格</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="product in products" :key="product.id">
            <td>{{ product.id }}</td>
            <td>{{ product.name }}</td>
            <td>{{ product.merchantId }}</td>
            <td>¥{{ product.discountPrice }}</td>
            <td>{{ getProductStatus(product.auditStatus) }}</td>
            <td>
              <button v-if="product.auditStatus === 0" @click="auditProduct(product.id, 1)">通过</button>
              <button v-if="product.auditStatus === 0" @click="auditProduct(product.id, 2)">拒绝</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const activeTab = ref('users')
const users = ref<any[]>([])
const products = ref<any[]>([])

onMounted(() => {
  loadUsers()
})

const loadUsers = () => {
  axios.get('/api/admin/users').then(res => {
    users.value = res.data.data
  })
}

const loadProducts = () => {
  axios.get('/api/products/search?page=1&size=100').then(res => {
    products.value = res.data.data.records
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
    1: '已生效',
    2: '已禁用'
  }
  return statusMap[status] || '未知'
}

const getProductStatus = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝'
  }
  return statusMap[status] || '未知'
}

const auditUser = (userId: string, status: number) => {
  axios.post(`/api/admin/users/${userId}/audit`, {}, { params: { status } }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success(status === 1 ? '审核通过' : '审核拒绝')
      loadUsers()
    }
  })
}

const deleteUser = (userId: string) => {
  if (confirm('确定要删除该用户吗？')) {
    axios.delete(`/api/admin/users/${userId}`).then(res => {
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        loadUsers()
      }
    })
  }
}

const auditProduct = (productId: string, status: number) => {
  axios.post(`/api/products/${productId}/audit`, {}, { params: { status } }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success(status === 1 ? '审核通过' : '审核拒绝')
      loadProducts()
    }
  })
}
</script>

<style scoped>
.admin {
  padding: 2rem;
}

.admin h2 {
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
  background-color: #27ae60;
  color: #fff;
}

.tab-content button:nth-child(2) {
  background-color: #e74c3c;
  color: #fff;
}

.tab-content button:nth-child(3) {
  background-color: #95a5a6;
  color: #fff;
}
</style>