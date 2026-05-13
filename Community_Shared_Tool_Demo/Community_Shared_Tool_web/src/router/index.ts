// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import ToolMap from '../views/ToolMap.vue'
import ToolDetail from '../views/ToolDetail.vue'
import MyBorrow from '../views/MyBorrow.vue'
import MyPublished from '../views/MyPublished.vue'
import MessageCenter from '../views/MessageCenter.vue'
import CommunityChat from '../views/CommunityChat.vue'
import Profile from '../views/Profile.vue'
import Credit from '../views/Credit.vue'
import BorrowApplications from '../views/BorrowApplications.vue'
import MainLayout from '../views/MainLayout.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Admin from '../views/Admin.vue' // 🔹 新增导入管理员页面

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/tool-map' },
    { path: '/login', name: 'Login', component: Login, meta: { isPublic: true } },
    { path: '/register', name: 'Register', component: Register, meta: { isPublic: true } },
    {
      path: '/main',
      name: 'Main',
      component: MainLayout,
      meta: { requiresAuth: true },
      children: [
        { path: '/tool-map', name: 'ToolMap', component: ToolMap },
        { path: '/tool/:id', name: 'ToolDetail', component: ToolDetail },
        
        { path: '/my-borrow', name: 'MyBorrow', component: MyBorrow },
        { path: '/my-published', name: 'MyPublished', component: MyPublished },
        { path: '/message-center', name: 'MessageCenter', component: MessageCenter },
        { path: '/community-chat', name: 'CommunityChat', component: CommunityChat },
        { path: '/profile', name: 'Profile', component: Profile },
        { path: '/credit', name: 'Credit', component: Credit },
        { path: '/borrow-applications', name: 'BorrowApplications', component: BorrowApplications },
        // 🔹 新增管理员路由作为子路由
        { 
          path: '/admin/users', 
          name: 'AdminUsers', 
          component: Admin,
          meta: { requiresAdmin: true }
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  const isLoggedIn = localStorage.getItem('userToken')
  const isAdmin = localStorage.getItem('isAdmin') === 'true'

  // 需要认证但未登录
  if (to.meta.requiresAuth && !isLoggedIn) {
    next('/login')
    return
  }

  // 需要管理员权限但非管理员
  if (to.meta.requiresAdmin && !isAdmin) {
    // 如果是管理员页面但用户不是管理员，跳转到首页
    if (to.path.startsWith('/admin')) {
      next('/tool-map')
      return
    }
  }

  // 公开页面但已登录
  if (to.meta.isPublic && isLoggedIn) {
    // 如果用户是管理员且访问登录页面，跳转到用户管理页面
    if (isAdmin && to.path === '/login') {
      next('/admin/users')
    } else {
      next('/tool-map')
    }
    return
  }

  // 管理员访问登录页面，跳转到用户管理页面
  if (isAdmin && to.path === '/login') {
    next('/admin/users')
    return
  }

  next()
})

export default router