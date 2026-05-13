<template>
  <div class="register-container">
    <div class="register-box">
      <h2>注册</h2>
      <form @submit.prevent="register">
        <div class="form-group">
          <label>用户名</label>
          <input type="text" v-model="username" placeholder="请输入用户名" required>
        </div>
        <div class="form-group">
          <label>密码</label>
          <input type="password" v-model="password" placeholder="请输入密码" required>
        </div>
        <div class="form-group">
          <label>姓名</label>
          <input type="text" v-model="realName" placeholder="请输入真实姓名" required>
        </div>
        <div class="form-group">
          <label>手机号</label>
          <input type="tel" v-model="phone" placeholder="请输入手机号" required>
        </div>
        <div class="form-group">
          <label>邮箱</label>
          <input type="email" v-model="email" placeholder="请输入邮箱">
        </div>
        <div class="form-group">
          <label>城市</label>
          <input type="text" v-model="city" placeholder="请输入城市">
        </div>
        <div class="form-group">
          <label>性别</label>
          <select v-model="gender">
            <option value="男">男</option>
            <option value="女">女</option>
          </select>
        </div>
        <div class="form-group">
          <label>银行账号</label>
          <input type="text" v-model="bankAccount" placeholder="请输入16位银行账号">
        </div>
        <div class="form-group">
          <label>用户类型</label>
          <select v-model="role" @change="roleChanged">
            <option value="USER">普通用户</option>
            <option value="MERCHANT">商家</option>
          </select>
        </div>
        <div v-if="role === 'MERCHANT'" class="form-group">
          <label>店铺名称</label>
          <input type="text" v-model="shopName" placeholder="请输入店铺名称">
        </div>
        <div class="form-group captcha-group">
          <div>
            <label>验证码</label>
            <input type="text" v-model="captcha" placeholder="请输入验证码" required>
          </div>
          <img :src="captchaUrl" @click="refreshCaptcha" class="captcha-img">
        </div>
        <button type="submit" class="register-btn">注册</button>
      </form>
      <p class="login-link">已有账号？<span @click="$router.push('/login')">立即登录</span></p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const username = ref('')
const password = ref('')
const realName = ref('')
const phone = ref('')
const email = ref('')
const city = ref('')
const gender = ref('男')
const bankAccount = ref('')
const role = ref('USER')
const shopName = ref('')
const captcha = ref('')
const captchaUrl = ref('/api/users/captcha?' + Date.now())

const refreshCaptcha = () => {
  captchaUrl.value = '/api/users/captcha?' + Date.now()
}

const roleChanged = () => {
  if (role.value === 'USER') {
    shopName.value = ''
  }
}

const register = async () => {
  try {
    const res = await axios.post('/api/users/register', {
      username: username.value,
      password: password.value,
      realName: realName.value,
      phone: phone.value,
      email: email.value,
      city: city.value,
      gender: gender.value,
      bankAccount: bankAccount.value,
      role: role.value,
      shopName: shopName.value,
      captcha: captcha.value
    })
    
    if (res.data.code === 200) {
      ElMessage.success(res.data.message)
      setTimeout(() => {
        window.location.href = '/login'
      }, 1500)
    } else {
      ElMessage.error(res.data.message)
      refreshCaptcha()
    }
  } catch (error) {
    ElMessage.error('注册失败')
    refreshCaptcha()
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f5f5;
}

.register-box {
  background-color: #fff;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  width: 500px;
}

.register-box h2 {
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

.form-group input,
.form-group select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.captcha-group {
  display: flex;
  gap: 1rem;
}

.captcha-group div {
  flex: 1;
}

.captcha-img {
  width: 120px;
  height: 40px;
  cursor: pointer;
  border-radius: 4px;
  margin-top: 20px;
}

.register-btn {
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

.login-link {
  text-align: center;
  margin-top: 1rem;
  color: #666;
}

.login-link span {
  color: #e74c3c;
  cursor: pointer;
}
</style>