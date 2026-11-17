import { createRouter, createWebHistory } from 'vue-router'
import CustomerEntry from '../views/CustomerEntry.vue'
import Menu from '../views/Menu.vue'
import AdminLogin from '../views/AdminLogin.vue'
import AdminHome from '../views/AdminHome.vue'

const routes = [
  { path: '/', name: 'entry', component: CustomerEntry },
  { path: '/menu/:tableId', name: 'menu', component: Menu, props: true },
  { path: '/admin/login', name: 'admin-login', component: AdminLogin },
  {
    path: '/admin',
    component: AdminHome,
    // 简易路由守卫：没登录就回登录页（测试环境，仅前端标记）
    beforeEnter: (to, from, next) => {
      if (localStorage.getItem('adminToken')) next()
      else next('/admin/login')
    },
  },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

export default router
