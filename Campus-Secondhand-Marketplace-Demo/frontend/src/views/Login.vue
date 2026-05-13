<template>
  <div class="login-container">
    <div class="login-box">
      <h2>登录</h2>
      <form @submit.prevent="login">
        <div class="form-group">
          <label>用户名</label>
          <input type="text" v-model="username" placeholder="请输入用户名" required>
        </div>
        <div class="form-group">
          <label>密码</label>
          <input type="password" v-model="password" placeholder="请输入密码" required>
        </div>
        <button type="submit" class="login-btn">登录</button>
      </form>
      <p class="register-link">还没有账号？<span @click="$router.push('/register')">立即注册</span></p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const username = ref('')
const password = ref('')

const login = async () => {
  try {
    const res = await axios.post('/api/users/login', {
      username: username.value,
      password: password.value
    })
    
    if (res.data.code === 200) {
      localStorage.setItem('userToken', res.data.data.id.toString())
      localStorage.setItem('username', res.data.data.username)
      localStorage.setItem('role', res.data.data.role)
      localStorage.setItem('isAdmin', res.data.data.role === 'ADMIN' ? 'true' : 'false')
      ElMessage.success('登录成功')
      
      if (res.data.data.role === 'ADMIN') {
        window.location.href = '/admin'
      } else {
        window.location.href = '/'
      }
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('登录失败')
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f5f5;
}

.login-box {
  background-color: #fff;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  width: 400px;
}

.login-box h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #333;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #666;
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.login-btn {
  width: 100%;
  padding: 0.75rem;
  background-color: #e74c3c;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  margin-top: 1rem;
}

.register-link {
  text-align: center;
  margin-top: 1rem;
  color: #666;
}

.register-link span {
  color: #e74c3c;
  cursor: pointer;
}
</style>