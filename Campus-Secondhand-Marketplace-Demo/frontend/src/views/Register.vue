<template>
  <div class="register-page">
    <div class="register-box">
      <h2>用户注册</h2>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label>用户名 <span class="required">*</span></label>
          <input type="text" v-model="form.username" placeholder="请输入用户名" required>
        </div>
        <div class="form-group">
          <label>密码 <span class="required">*</span></label>
          <input type="password" v-model="form.password" placeholder="请输入密码" required>
        </div>
        <div class="form-group">
          <label>姓名 <span class="required">*</span></label>
          <input type="text" v-model="form.realName" placeholder="请输入真实姓名" required>
        </div>
        <div class="form-group">
          <label>手机号 <span class="required">*</span></label>
          <input type="tel" v-model="form.phone" placeholder="请输入手机号" required>
        </div>
        <div class="form-group">
          <label>邮箱</label>
          <input type="email" v-model="form.email" placeholder="请输入邮箱">
        </div>
        <div class="form-group">
          <label>城市</label>
          <input type="text" v-model="form.city" placeholder="请输入城市">
        </div>
        <div class="form-group">
          <label>性别 <span class="required">*</span></label>
          <select v-model="form.gender" required>
            <option value="">请选择</option>
            <option value="男">男</option>
            <option value="女">女</option>
          </select>
        </div>
        <div class="form-group">
          <label>银行账号（16位数字）</label>
          <input type="text" v-model="form.bankAccount" placeholder="请输入16位银行账号" maxlength="16">
        </div>
        <div class="form-group">
          <label>用户类型 <span class="required">*</span></label>
          <select v-model="form.role" required>
            <option value="USER">普通用户</option>
            <option value="MERCHANT">商家</option>
          </select>
        </div>
        
        <div v-if="form.role === 'MERCHANT'" class="merchant-section">
          <h3>商家资质认证（必填）</h3>
          <div class="form-group">
            <label>营业执照 <span class="required">*</span></label>
            <div class="upload-box" @click="triggerUpload('businessLicense')">
              <input type="file" ref="businessLicenseInput" @change="handleFileChange($event, 'businessLicense')" accept="image/*" style="display: none">
              <div v-if="!previews.businessLicense" class="upload-placeholder">
                <span class="upload-icon">+</span>
                <span>点击上传营业执照</span>
              </div>
              <img v-else :src="previews.businessLicense" class="preview-img">
            </div>
          </div>
          <div class="form-group">
            <label>身份证正面 <span class="required">*</span></label>
            <div class="upload-box" @click="triggerUpload('idCardFront')">
              <input type="file" ref="idCardFrontInput" @change="handleFileChange($event, 'idCardFront')" accept="image/*" style="display: none">
              <div v-if="!previews.idCardFront" class="upload-placeholder">
                <span class="upload-icon">+</span>
                <span>点击上传身份证正面</span>
              </div>
              <img v-else :src="previews.idCardFront" class="preview-img">
            </div>
          </div>
          <div class="form-group">
            <label>身份证反面 <span class="required">*</span></label>
            <div class="upload-box" @click="triggerUpload('idCardBack')">
              <input type="file" ref="idCardBackInput" @change="handleFileChange($event, 'idCardBack')" accept="image/*" style="display: none">
              <div v-if="!previews.idCardBack" class="upload-placeholder">
                <span class="upload-icon">+</span>
                <span>点击上传身份证反面</span>
              </div>
              <img v-else :src="previews.idCardBack" class="preview-img">
            </div>
          </div>
        </div>

        <div class="form-group captcha-group">
          <div>
            <label>验证码 <span class="required">*</span></label>
            <input type="text" v-model="form.captcha" placeholder="请输入验证码" required>
          </div>
          <div class="captcha-wrapper">
            <img :src="captchaUrl" @click="refreshCaptcha" class="captcha-img" :alt="captchaError ? '加载失败，点击重试' : '验证码'" :class="{ 'captcha-error': captchaError }">
            <span class="captcha-refresh" @click="refreshCaptcha">点击刷新</span>
          </div>
        </div>

        <button type="submit" class="register-btn" :disabled="isSubmitting">
          {{ isSubmitting ? '注册中...' : '立即注册' }}
        </button>
      </form>
      <p class="login-link">已有账号？<span @click="$router.push('/login')">立即登录</span></p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const form = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  city: '',
  gender: '',
  bankAccount: '',
  role: 'USER',
  captcha: ''
})

const files = reactive({
  businessLicense: null as File | null,
  idCardFront: null as File | null,
  idCardBack: null as File | null
})

const previews = reactive({
  businessLicense: '',
  idCardFront: '',
  idCardBack: ''
})

const captchaUrl = ref('')
const captchaError = ref(false)
const isSubmitting = ref(false)

const businessLicenseInput = ref<HTMLInputElement>()
const idCardFrontInput = ref<HTMLInputElement>()
const idCardBackInput = ref<HTMLInputElement>()

const refreshCaptcha = () => {
  captchaError.value = false
  const timestamp = Date.now()
  axios.get('/api/captcha', {
    params: { t: timestamp },
    withCredentials: true
  }).then(res => {
    captchaUrl.value = res.data
  }).catch(err => {
    console.error('验证码加载失败:', err)
    captchaError.value = true
    captchaUrl.value = ''
    ElMessage.error('验证码加载失败，请检查后端服务是否启动')
  })
}

onMounted(() => {
  refreshCaptcha()
})

const triggerUpload = (type: string) => {
  const inputMap: Record<string, any> = {
    businessLicense: businessLicenseInput,
    idCardFront: idCardFrontInput,
    idCardBack: idCardBackInput
  }
  inputMap[type]?.value?.click()
}

const handleFileChange = (event: Event, type: 'businessLicense' | 'idCardFront' | 'idCardBack') => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) {
    files[type] = file
    previews[type] = URL.createObjectURL(file)
  }
}

const handleRegister = async () => {
  if (!form.username || !form.password || !form.realName || !form.phone || !form.gender) {
    ElMessage.error('请填写必填项')
    return
  }

  if (form.role === 'MERCHANT') {
    if (!files.businessLicense || !files.idCardFront || !files.idCardBack) {
      ElMessage.error('商家注册必须上传营业执照和身份证照片')
      return
    }
  }

  isSubmitting.value = true

  try {
    let res

    if (form.role === 'MERCHANT') {
      const formData = new FormData()
      formData.append('username', form.username)
      formData.append('password', form.password)
      formData.append('realName', form.realName)
      formData.append('phone', form.phone)
      formData.append('email', form.email)
      formData.append('city', form.city)
      formData.append('gender', form.gender)
      formData.append('bankAccount', form.bankAccount)
      formData.append('shopName', form.username + '的店铺')
      formData.append('captcha', form.captcha)
      formData.append('businessLicense', files.businessLicense!)
      formData.append('idCardFront', files.idCardFront!)
      formData.append('idCardBack', files.idCardBack!)

      res = await axios.post('/api/users/register/merchant', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        withCredentials: true
      })
    } else {
      res = await axios.post('/api/users/register', {
        username: form.username,
        password: form.password,
        realName: form.realName,
        phone: form.phone,
        email: form.email,
        city: form.city,
        gender: form.gender,
        bankAccount: form.bankAccount,
        role: form.role,
        captcha: form.captcha
      }, { withCredentials: true })
    }

    if (res.data.code === 200) {
      ElMessage.success(res.data.message || '注册成功，请等待审核')
      setTimeout(() => {
        window.location.href = '/login'
      }, 1500)
    } else {
      ElMessage.error(res.data.message || '注册失败')
      refreshCaptcha()
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '注册失败，请检查网络')
    refreshCaptcha()
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style>
html, body {
  margin: 0;
  padding: 0;
  background-color: #f5f5f5;
  min-height: 100vh;
}
</style>

<style scoped>
.register-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 40px 20px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.register-box {
  background-color: #fff;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  width: 500px;
  max-height: calc(100vh - 80px);
  overflow-y: auto;
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

.required {
  color: #e74c3c;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.merchant-section {
  margin-top: 1rem;
  padding: 1rem;
  background-color: #fafafa;
  border: 2px dashed #e74c3c;
  border-radius: 8px;
}

.merchant-section h3 {
  color: #e74c3c;
  font-size: 14px;
  margin-bottom: 1rem;
}

.upload-box {
  width: 100%;
  height: 120px;
  border: 2px dashed #ddd;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  overflow: hidden;
}

.upload-box:hover {
  border-color: #e74c3c;
  background-color: #fdf5f5;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #999;
}

.upload-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.captcha-group {
  display: flex;
  gap: 1rem;
  align-items: flex-start;
  padding-bottom: 0.5rem;
}

.captcha-group > div:first-child {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.captcha-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 2.0rem;
}

.captcha-img {
  width: 120px;
  height: 40px;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #ddd;
  background-color: #fff;
}

.captcha-img.captcha-error {
  background-color: #fdecea;
  border-color: #e74c3c;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #e74c3c;
  font-size: 12px;
}

.captcha-refresh {
  font-size: 12px;
  color: #e74c3c;
  cursor: pointer;
  margin-top: 5px;
  text-align: center;
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

.register-btn:hover:not(:disabled) {
  background-color: #c0392b;
}

.register-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
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