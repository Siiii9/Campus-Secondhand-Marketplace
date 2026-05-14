import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import ProductList from '../views/ProductList.vue'
import ProductDetail from '../views/ProductDetail.vue'
import Cart from '../views/Cart.vue'
import OrderList from '../views/OrderList.vue'
import Profile from '../views/Profile.vue'
import Admin from '../views/Admin.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import MerchantCenter from '../views/MerchantCenter.vue'
import Shop from '../views/Shop.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'Home', component: Home },
    { path: '/login', name: 'Login', component: Login, meta: { isPublic: true } },
    { path: '/register', name: 'Register', component: Register, meta: { isPublic: true } },
    { path: '/products', name: 'ProductList', component: ProductList },
    { path: '/product/:id', name: 'ProductDetail', component: ProductDetail },
    { path: '/cart', name: 'Cart', component: Cart, meta: { requiresAuth: true } },
    { path: '/orders', name: 'OrderList', component: OrderList, meta: { requiresAuth: true } },
    { path: '/profile', name: 'Profile', component: Profile, meta: { requiresAuth: true } },
    { path: '/merchant', name: 'MerchantCenter', component: MerchantCenter, meta: { requiresAuth: true } },
    { 
      path: '/admin', 
      name: 'Admin', 
      component: Admin,
      meta: { requiresAdmin: true }
    },
    { 
      path: '/shop/:id', 
      name: 'Shop', 
      component: Shop 
    }
  ]
})

router.beforeEach((to, from, next) => {
  const isLoggedIn = localStorage.getItem('userToken')
  const isAdmin = localStorage.getItem('isAdmin') === 'true'

  if (to.meta.requiresAuth && !isLoggedIn) {
    next('/login')
    return
  }

  if (to.meta.requiresAdmin && !isAdmin) {
    next('/')
    return
  }

  if (to.meta.isPublic && isLoggedIn && to.path !== '/login') {
    next('/')
    return
  }

  next()
})

export default router