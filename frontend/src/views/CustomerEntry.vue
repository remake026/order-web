<template>
  <div class="entry-page">
    <div class="entry-card">
      <div class="brand-mark">🍜</div>
      <h1>欢迎光临</h1>
      <p class="sub">请输入桌号，开始点餐</p>

      <input
        ref="inputRef"
        v-model="tableId"
        class="table-input"
        placeholder="例如 A01"
        maxlength="20"
        @keyup.enter="enter"
      />

      <button class="btn btn-primary enter-btn" :disabled="!tableId.trim()" @click="enter">
        进入点菜 →
      </button>

      <p class="hint">桌号印在桌角的小卡片上</p>
    </div>

    <router-link class="admin-link" to="/admin/login">后台管理</router-link>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const tableId = ref('')
const inputRef = ref(null)

onMounted(() => inputRef.value?.focus())

function enter() {
  const id = tableId.value.trim()
  if (!id) return
  router.push(`/menu/${encodeURIComponent(id)}`)
}
</script>

<style scoped>
.entry-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: radial-gradient(circle at 20% 0%, #fbe9e2 0%, transparent 45%),
    radial-gradient(circle at 100% 100%, #f7f0e3 0%, transparent 50%), var(--paper);
}

.entry-card {
  width: 100%;
  max-width: 380px;
  background: #fff;
  border-radius: 22px;
  padding: 40px 32px 32px;
  text-align: center;
  box-shadow: 0 20px 60px rgba(43, 33, 26, 0.1);
  border: 1px solid var(--line);
}

.brand-mark {
  font-size: 44px;
  line-height: 1;
  margin-bottom: 16px;
}

h1 {
  font-size: 26px;
  letter-spacing: 2px;
  margin-bottom: 8px;
}

.sub {
  color: var(--muted);
  font-size: 14px;
  margin-bottom: 28px;
}

.table-input {
  text-align: center;
  font-size: 22px;
  letter-spacing: 4px;
  font-weight: 600;
  padding: 14px;
  text-transform: uppercase;
}

.enter-btn {
  width: 100%;
  margin-top: 14px;
  padding: 15px;
  font-size: 16px;
  border-radius: 12px;
}

.hint {
  margin-top: 18px;
  font-size: 12px;
  color: var(--muted);
}

.admin-link {
  margin-top: 28px;
  font-size: 13px;
  color: var(--muted);
  text-decoration: none;
}

.admin-link:hover {
  color: var(--brand);
}
</style>
