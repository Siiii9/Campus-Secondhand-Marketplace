import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

// 🔹 引入 Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css' // 引入样式

import axios from 'axios';
axios.defaults.baseURL = 'http://localhost:8084';
axios.defaults.headers.common['Content-Type'] = 'application/json';
axios.defaults.withCredentials = true;

// 添加请求拦截器，自动携带token
axios.interceptors.request.use(
  config => {
    const token = localStorage.getItem('userToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 引入Element Plus消息组件
import { ElMessage } from 'element-plus';

// 添加响应拦截器，处理错误和提供用户反馈
axios.interceptors.response.use(
  response => {
    return response;
  },
  error => {
    // 处理401错误（未授权）
    if (error.response && error.response.status === 401) {
      // 清除本地存储的用户信息
      localStorage.removeItem('userToken');
      localStorage.removeItem('userId');
      localStorage.removeItem('username');
      localStorage.removeItem('isAdmin');
      
      // 显示错误消息
      ElMessage.error('登录已过期，请重新登录');
      
      // 跳转到登录页
      setTimeout(() => {
        window.location.href = '/login';
      }, 1500);
    } 
    // 处理403错误（权限不足）
    else if (error.response && error.response.status === 403) {
      ElMessage.error('权限不足，无法执行此操作');
    } 
    // 处理404错误（资源不存在）
    else if (error.response && error.response.status === 404) {
      ElMessage.error('请求的资源不存在');
    } 
    // 处理500错误（服务器错误）
    else if (error.response && error.response.status >= 500) {
      ElMessage.error('服务器错误，请稍后重试');
    }
    // 处理其他错误
    else if (error.response && error.response.data && error.response.data.message) {
      ElMessage.error(error.response.data.message);
    }
    // 处理网络错误
    else if (!error.response) {
      ElMessage.error('网络错误，请检查网络连接');
    }
    // 处理未知错误
    else {
      ElMessage.error('操作失败，请重试');
    }
    
    return Promise.reject(error);
  }
);

const app = createApp(App)

// 🔹 使用 Element Plus
app.use(ElementPlus)

app.use(createPinia())
app.use(router)

app.mount('#app')