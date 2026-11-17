<template>
  <div class="login-page">
    <div class="login-card">
      <h1>后台管理</h1>
      <p class="sub">菜品与订单管理</p>

      <div class="field">
        <label>用户名</label>
        <input v-model="form.username" placeholder="admin" autocomplete="username" />
      </div>

      <div class="field">
        <label>密码</label>
        <input
          v-model="form.password"
          type="password"
          placeholder="admin123"
          autocomplete="current-password"
          @keyup.enter="login"
        />
      </div>

      <button class="btn btn-primary login-btn" :disabled="loading" @click="login">
        {{ loading ? '登录中…' : '登录' }}
      </button>

      <p class="tip">默认账号 admin / admin123</p>
      <router-link class="back" to="/">← 返回顾客端</router-link>
    </div>

    <div v-if="error" class="toast error">{{ error }}</div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'

const router = useRouter()
const loading = ref(false)
const error = ref('')
const form = reactive({ username: 'admin', password: 'admin123' })

async function login() {
  if (loading.value) return
  loading.value = true
  error.value = ''
  try {
    const res = await api.login(form.username, form.password)
    if (res && res.success) {
      localStorage.setItem('adminToken', res.token)
      router.push('/admin')
    } else {
      error.value = '账号或密码错误'
      setTimeout(() => (error.value = ''), 2500)
    }
  } catch (e) {
    error.value = e.message
    setTimeout(() => (error.value = ''), 2500)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: var(--paper);
}

.login-card {
  width: 100%;
  max-width: 380px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 20px;
  padding: 36px 32px 30px;
  box-shadow: 0 16px 50px rgba(43, 33, 26, 0.08);
}

h1 {
  font-size: 22px;
  margin-bottom: 6px;
}

.sub {
  color: var(--muted);
  font-size: 13px;
  margin-bottom: 26px;
}

.login-btn {
  width: 100%;
  padding: 13px;
  margin-top: 6px;
}

.tip {
  text-align: center;
  font-size: 12px;
  color: var(--muted);
  margin-top: 16px;
}

.back {
  display: block;
  text-align: center;
  margin-top: 20px;
  font-size: 13px;
  color: var(--muted);
  text-decoration: none;
}

.back:hover {
  color: var(--brand);
}
</style>
