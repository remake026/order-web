<template>
  <div class="page">
    <div class="toolbar">
      <h2>订单管理</h2>
      <div class="tools">
        <div class="segmented">
          <button :class="{ active: filter === null }" @click="setFilter(null)">全部</button>
          <button :class="{ active: filter === 'PENDING' }" @click="setFilter('PENDING')">待处理</button>
          <button :class="{ active: filter === 'COMPLETED' }" @click="setFilter('COMPLETED')">已完成</button>
        </div>
        <button class="btn btn-sm" :disabled="loading" @click="load">刷新</button>
      </div>
    </div>

    <div v-if="loading" class="empty">加载中…</div>
    <div v-else-if="error" class="empty">{{ error }}</div>
    <div v-else-if="!orders.length" class="empty">还没有订单</div>

    <div v-else class="order-grid">
      <article v-for="o in orders" :key="o.id" class="order-card card">
        <header class="oc-head">
          <div>
            <span class="oc-id">#{{ o.id }}</span>
            <span class="oc-table">桌号 {{ o.tableId }}</span>
          </div>
          <span class="badge" :class="o.status === 'COMPLETED' ? 'done' : 'pending'">
            {{ o.status === 'COMPLETED' ? '已完成' : '待处理' }}
          </span>
        </header>

        <ul class="oc-items">
          <li v-for="it in o.items" :key="it.id">
            <span class="it-name">{{ it.dishName }}</span>
            <span class="it-qty">×{{ it.quantity }}</span>
            <span class="it-sub">¥{{ money(Number(it.price) * it.quantity) }}</span>
          </li>
        </ul>

        <p v-if="o.remark" class="oc-remark">
          <span class="rk">备注</span>{{ o.remark }}
        </p>

        <footer class="oc-foot">
          <span class="oc-time">{{ fmtTime(o.createdAt) }}</span>
          <strong class="oc-total">¥{{ money(o.totalAmount) }}</strong>
        </footer>

        <div class="oc-actions">
          <button
            v-if="o.status !== 'COMPLETED'"
            class="btn btn-sm btn-primary"
            @click="setStatus(o, 'COMPLETED')"
          >
            标记完成
          </button>
          <button v-else class="btn btn-sm" @click="setStatus(o, 'PENDING')">恢复待处理</button>
        </div>
      </article>
    </div>

    <div v-if="toast" class="toast" :class="{ error: toastError }">{{ toast }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../api'

const orders = ref([])
const filter = ref(null)
const loading = ref(true)
const error = ref('')
const toast = ref('')
const toastError = ref(false)

let timer = null
function showToast(msg, isError = false) {
  toast.value = msg
  toastError.value = isError
  clearTimeout(timer)
  timer = setTimeout(() => (toast.value = ''), 2400)
}

function money(v) {
  return Number(v || 0).toFixed(2)
}

function fmtTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    orders.value = await api.getOrders(filter.value)
  } catch (e) {
    error.value = '加载失败：' + e.message
  } finally {
    loading.value = false
  }
}

function setFilter(v) {
  filter.value = v
  load()
}

async function setStatus(order, status) {
  try {
    await api.updateOrderStatus(order.id, status)
    showToast(status === 'COMPLETED' ? '已标记完成' : '已恢复待处理')
    await load()
  } catch (e) {
    showToast(e.message, true)
  }
}

onMounted(load)
</script>

<style scoped>
.page {
  max-width: 1180px;
  margin: 0 auto;
  padding: 24px 22px 60px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.toolbar h2 {
  font-size: 19px;
}

.tools {
  display: flex;
  align-items: center;
  gap: 10px;
}

.segmented {
  display: flex;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 3px;
}

.segmented button {
  padding: 7px 14px;
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-soft);
  border-radius: 7px;
}

.segmented button.active {
  background: var(--ink);
  color: #fff;
}

.order-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(310px, 1fr));
  gap: 16px;
}

.order-card {
  padding: 16px 18px;
}

.oc-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.oc-id {
  font-weight: 700;
  font-size: 15px;
  margin-right: 10px;
}

.oc-table {
  font-size: 13px;
  color: var(--ink-soft);
  background: var(--brand-soft);
  color: var(--brand);
  padding: 3px 9px;
  border-radius: 999px;
  font-weight: 600;
}

.badge {
  font-size: 12px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 999px;
}

.badge.pending {
  background: #fff4e5;
  color: #b26a00;
}

.badge.done {
  background: #e9f6ee;
  color: var(--ok);
}

.oc-items {
  list-style: none;
  margin: 0 0 10px;
  padding: 10px 0;
  border-top: 1px dashed var(--line);
  border-bottom: 1px dashed var(--line);
}

.oc-items li {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  padding: 4px 0;
}

.it-name {
  flex: 1;
}

.it-qty {
  color: var(--muted);
}

.it-sub {
  width: 70px;
  text-align: right;
  font-weight: 600;
}

.oc-remark {
  font-size: 13px;
  color: var(--ink-soft);
  background: #faf7f2;
  border-radius: 8px;
  padding: 8px 11px;
  margin-bottom: 10px;
  line-height: 1.5;
}

.rk {
  font-size: 11px;
  font-weight: 700;
  color: var(--muted);
  margin-right: 6px;
}

.oc-foot {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 12px;
}

.oc-time {
  font-size: 12px;
  color: var(--muted);
}

.oc-total {
  font-size: 18px;
  color: var(--brand);
}

.oc-actions {
  display: flex;
  gap: 8px;
}

.oc-actions .btn {
  flex: 1;
}
</style>
