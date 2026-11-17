<template>
  <div class="admin">
    <header class="admin-bar">
      <div class="left">
        <span class="logo">🍜</span>
        <strong>好味小馆 · 后台</strong>
      </div>

      <nav class="tabs">
        <button :class="{ active: tab === 'dishes' }" @click="tab = 'dishes'">菜品管理</button>
        <button :class="{ active: tab === 'orders' }" @click="tab = 'orders'">订单管理</button>
      </nav>

      <div class="right">
        <a class="btn btn-ghost btn-sm" href="/" target="_blank" rel="noopener">顾客端 ↗</a>
        <button class="btn btn-ghost btn-sm" @click="logout">退出</button>
      </div>
    </header>

    <AdminDishes v-if="tab === 'dishes'" />
    <AdminOrders v-else />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import AdminDishes from './AdminDishes.vue'
import AdminOrders from './AdminOrders.vue'

const router = useRouter()
const tab = ref('dishes')

function logout() {
  localStorage.removeItem('adminToken')
  router.push('/admin/login')
}
</script>

<style scoped>
.admin {
  min-height: 100vh;
  background: var(--paper);
}

.admin-bar {
  position: sticky;
  top: 0;
  z-index: 30;
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 12px 22px;
  background: #fff;
  border-bottom: 1px solid var(--line);
  flex-wrap: wrap;
}

.left {
  display: flex;
  align-items: center;
  gap: 9px;
  font-size: 15px;
}

.logo {
  font-size: 20px;
}

.tabs {
  display: flex;
  gap: 4px;
  background: var(--paper);
  padding: 4px;
  border-radius: 10px;
}

.tabs button {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-soft);
  border-radius: 8px;
  transition: all 0.15s;
}

.tabs button.active {
  background: #fff;
  color: var(--ink);
  box-shadow: var(--shadow-sm);
}

.right {
  margin-left: auto;
  display: flex;
  gap: 4px;
}
</style>
