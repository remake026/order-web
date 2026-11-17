<template>
  <div class="menu-page">
    <!-- 顶部栏 -->
    <header class="topbar">
      <div class="brand">
        <span class="logo">🍜</span>
        <span class="brand-name">好味小馆</span>
      </div>
      <div class="topbar-right">
        <span class="table-pill">桌号 {{ tableId }}</span>
        <button class="btn btn-ghost btn-sm" @click="leave">换桌</button>
      </div>
    </header>

    <!-- 分类导航 -->
    <nav class="cats">
      <button class="cat" :class="{ active: activeCategory === null }" @click="activeCategory = null">
        全部
      </button>
      <button
        v-for="c in categories"
        :key="c.id"
        class="cat"
        :class="{ active: activeCategory === c.id }"
        @click="activeCategory = c.id"
      >
        {{ c.name }}
      </button>
    </nav>

    <!-- 主体 -->
    <main class="layout">
      <section class="dishes-area">
        <div v-if="loading" class="grid">
          <div v-for="i in 6" :key="i" class="dish skeleton"></div>
        </div>

        <div v-else-if="error" class="empty">
          <p>{{ error }}</p>
          <button class="btn" style="margin-top: 16px" @click="load">重新加载</button>
        </div>

        <div v-else-if="!filteredDishes.length" class="empty">该分类下暂时没有菜品</div>

        <div v-else class="grid">
          <article v-for="d in filteredDishes" :key="d.id" class="dish">
            <div
              class="thumb"
              :class="{ 'has-img': !!d.imageUrl }"
              :style="d.imageUrl ? { backgroundImage: `url('${d.imageUrl}')` } : {}"
            >
              <span v-if="!d.imageUrl" class="ph">{{ d.name.slice(0, 1) }}</span>
              <span v-if="qtyOf(d.id)" class="count-badge">{{ qtyOf(d.id) }}</span>
            </div>
            <div class="dish-info">
              <h3>{{ d.name }}</h3>
              <p class="desc">{{ d.description || '店长推荐' }}</p>
              <div class="price-row">
                <span class="price">¥{{ money(d.price) }}</span>
                <button class="add-btn" :aria-label="`加入 ${d.name}`" @click="add(d)">＋</button>
              </div>
            </div>
          </article>
        </div>
      </section>

      <!-- 购物车：桌面常驻 / 移动端抽屉 -->
      <aside class="cart-panel" :class="{ open: drawerOpen }">
        <div class="cart-head">
          <h3>已点菜品</h3>
          <button class="close-x mobile-only" @click="drawerOpen = false">✕</button>
          <span class="pill">{{ totalQty }} 份</span>
        </div>

        <div v-if="!cart.length" class="cart-empty">
          <div class="ce-icon">🥢</div>
          <p>还没有选菜哦</p>
          <p class="ce-sub">点击菜品上的 + 加入</p>
        </div>

        <ul v-else class="cart-list">
          <li v-for="item in cart" :key="item.dish.id" class="cart-item">
            <div class="ci-main">
              <div class="ci-name">{{ item.dish.name }}</div>
              <div class="ci-price">¥{{ money(item.dish.price) }}</div>
            </div>
            <div class="stepper">
              <button @click="dec(item)">−</button>
              <span>{{ item.qty }}</span>
              <button @click="add(item.dish)">＋</button>
            </div>
            <div class="ci-sub">¥{{ money(item.dish.price * item.qty) }}</div>
          </li>
        </ul>

        <div class="cart-foot">
          <textarea v-model="remark" rows="2" placeholder="备注：如少辣、不要香菜、餐具份数" />
          <div class="total-row">
            <span>合计</span>
            <strong>¥{{ money(total) }}</strong>
          </div>
          <button
            class="btn btn-primary submit-btn"
            :disabled="!cart.length || submitting"
            @click="submit"
          >
            {{ submitting ? '提交中…' : '提交订单' }}
          </button>
        </div>
      </aside>

      <!-- 移动端底部结算条 -->
      <div v-if="!drawerOpen" class="mobile-bar">
        <div class="mb-info">
          <span class="mb-total">¥{{ money(total) }}</span>
          <span class="mb-qty">共 {{ totalQty }} 份</span>
        </div>
        <button class="btn btn-primary" :disabled="!cart.length" @click="drawerOpen = true">
          去结算
        </button>
      </div>
    </main>

    <!-- 下单成功 -->
    <div v-if="successOrder" class="overlay" @click.self="closeSuccess">
      <div class="modal success-modal">
        <div class="ok-icon">✓</div>
        <h3>下单成功</h3>
        <p class="ok-sub">订单号 #{{ successOrder.id }} · 桌号 {{ successOrder.tableId }}</p>
        <p class="ok-amount">¥{{ money(successOrder.totalAmount) }}</p>
        <p class="ok-tip">菜品已送达后厨，请耐心等待</p>
        <button class="btn btn-primary" style="width: 100%; margin-top: 8px" @click="closeSuccess">
          继续点菜
        </button>
      </div>
    </div>

    <div v-if="toast" class="toast" :class="{ error: toastError }">{{ toast }}</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'

const props = defineProps({
  tableId: { type: String, required: true },
})

const router = useRouter()
const categories = ref([])
const dishes = ref([])
const activeCategory = ref(null)
const cart = ref([])
const remark = ref('')
const loading = ref(true)
const error = ref('')
const submitting = ref(false)
const drawerOpen = ref(false)
const successOrder = ref(null)
const toast = ref('')
const toastError = ref(false)

let toastTimer = null

function showToast(msg, isError = false) {
  toast.value = msg
  toastError.value = isError
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value = ''), 2600)
}

function money(v) {
  return Number(v || 0).toFixed(2)
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [cats, list] = await Promise.all([api.getCategories(), api.getDishes({ availableOnly: true })])
    categories.value = cats || []
    dishes.value = list || []
  } catch (e) {
    error.value = '菜单加载失败：' + e.message
  } finally {
    loading.value = false
  }
}

onMounted(load)

const filteredDishes = computed(() => {
  if (activeCategory.value === null) return dishes.value
  return dishes.value.filter((d) => d.categoryId === activeCategory.value)
})

const totalQty = computed(() => cart.value.reduce((s, i) => s + i.qty, 0))
const total = computed(() =>
  cart.value.reduce((s, i) => s + Number(i.dish.price) * i.qty, 0),
)

function qtyOf(dishId) {
  const hit = cart.value.find((i) => i.dish.id === dishId)
  return hit ? hit.qty : 0
}

function add(dish) {
  const hit = cart.value.find((i) => i.dish.id === dish.id)
  if (hit) hit.qty++
  else cart.value.push({ dish, qty: 1 })
}

function dec(item) {
  item.qty--
  if (item.qty <= 0) {
    cart.value = cart.value.filter((i) => i !== item)
  }
}

async function submit() {
  if (!cart.value.length || submitting.value) return
  submitting.value = true
  try {
    const res = await api.submitOrder({
      tableId: props.tableId,
      remark: remark.value,
      items: cart.value.map((i) => ({ dishId: i.dish.id, quantity: i.qty })),
    })
    successOrder.value = res
    cart.value = []
    remark.value = ''
    drawerOpen.value = false
  } catch (e) {
    showToast(e.message, true)
  } finally {
    submitting.value = false
  }
}

function closeSuccess() {
  successOrder.value = null
  load()
}

function leave() {
  router.push('/')
}
</script>

<style scoped>
.menu-page {
  min-height: 100vh;
  padding-bottom: 84px;
}

/* ---------- 顶部 ---------- */
.topbar {
  position: sticky;
  top: 0;
  z-index: 30;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  background: rgba(250, 247, 242, 0.92);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--line);
}

.brand {
  display: flex;
  align-items: center;
  gap: 9px;
}

.logo {
  font-size: 22px;
}

.brand-name {
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 1px;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.table-pill {
  background: var(--brand-soft);
  color: var(--brand);
  font-size: 13px;
  font-weight: 700;
  padding: 6px 12px;
  border-radius: 999px;
}

/* ---------- 分类 ---------- */
.cats {
  position: sticky;
  top: 57px;
  z-index: 29;
  display: flex;
  gap: 8px;
  padding: 12px 20px;
  overflow-x: auto;
  background: var(--paper);
  border-bottom: 1px solid var(--line);
  scrollbar-width: none;
}

.cats::-webkit-scrollbar {
  display: none;
}

.cat {
  flex: 0 0 auto;
  padding: 8px 16px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-soft);
  background: #fff;
  border: 1px solid var(--line);
  transition: all 0.15s;
}

.cat.active {
  background: var(--ink);
  border-color: var(--ink);
  color: #fff;
}

/* ---------- 布局 ---------- */
.layout {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
  max-width: 1180px;
  margin: 0 auto;
  padding: 20px;
}

.dishes-area {
  min-width: 0;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
}

/* ---------- 菜品卡片 ---------- */
.dish {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.15s, box-shadow 0.15s;
}

.dish:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.thumb {
  position: relative;
  aspect-ratio: 4 / 3;
  background: linear-gradient(135deg, #f6ede2 0%, #efe3d3 100%);
  background-size: cover;
  background-position: center;
  display: grid;
  place-items: center;
}

.thumb.has-img .ph {
  display: none;
}

.ph {
  font-size: 34px;
  font-weight: 700;
  color: #cbb79c;
  user-select: none;
}

.count-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  min-width: 22px;
  height: 22px;
  padding: 0 6px;
  border-radius: 999px;
  background: var(--brand);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  display: grid;
  place-items: center;
}

.dish-info {
  padding: 12px 13px 13px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.dish-info h3 {
  font-size: 15px;
  font-weight: 700;
}

.desc {
  font-size: 12px;
  color: var(--muted);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.price-row {
  margin-top: auto;
  padding-top: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price {
  font-size: 16px;
  font-weight: 700;
  color: var(--brand);
}

.add-btn {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--brand);
  color: #fff;
  font-size: 17px;
  line-height: 1;
  display: grid;
  place-items: center;
  transition: background 0.15s, transform 0.1s;
}

.add-btn:hover {
  background: var(--brand-dark);
}

.add-btn:active {
  transform: scale(0.92);
}

.skeleton {
  height: 230px;
  background: linear-gradient(90deg, #f4efe7 25%, #faf7f2 37%, #f4efe7 63%);
  background-size: 400% 100%;
  animation: shimmer 1.3s infinite;
}

@keyframes shimmer {
  from {
    background-position: 100% 50%;
  }
  to {
    background-position: 0 50%;
  }
}

/* ---------- 购物车 ---------- */
.cart-panel {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 160px);
}

.cart-head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 15px 18px;
  border-bottom: 1px solid var(--line);
}

.cart-head h3 {
  font-size: 15px;
  flex: 1;
}

.pill {
  background: var(--brand-soft);
  color: var(--brand);
  font-size: 12px;
  font-weight: 700;
  padding: 3px 9px;
  border-radius: 999px;
}

.cart-empty {
  text-align: center;
  padding: 44px 16px;
  color: var(--muted);
}

.ce-icon {
  font-size: 30px;
  margin-bottom: 10px;
}

.cart-empty p {
  font-size: 14px;
}

.ce-sub {
  font-size: 12px !important;
  margin-top: 4px;
  opacity: 0.8;
}

.cart-list {
  list-style: none;
  margin: 0;
  padding: 6px 18px;
  overflow-y: auto;
  flex: 1;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px dashed var(--line);
}

.cart-item:last-child {
  border-bottom: none;
}

.ci-main {
  flex: 1;
  min-width: 0;
}

.ci-name {
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ci-price {
  font-size: 12px;
  color: var(--muted);
}

.stepper {
  display: flex;
  align-items: center;
  gap: 2px;
  background: #faf7f2;
  border: 1px solid var(--line);
  border-radius: 999px;
  padding: 2px;
}

.stepper button {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  font-size: 14px;
  font-weight: 700;
  color: var(--ink-soft);
  display: grid;
  place-items: center;
}

.stepper button:hover {
  background: #fff;
  color: var(--brand);
}

.stepper span {
  min-width: 20px;
  text-align: center;
  font-size: 13px;
  font-weight: 700;
}

.ci-sub {
  width: 62px;
  text-align: right;
  font-size: 13px;
  font-weight: 700;
}

.cart-foot {
  padding: 14px 18px 18px;
  border-top: 1px solid var(--line);
}

.cart-foot textarea {
  font-size: 13px;
}

.total-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin: 12px 0 10px;
  font-size: 14px;
  color: var(--ink-soft);
}

.total-row strong {
  font-size: 22px;
  color: var(--brand);
}

.submit-btn {
  width: 100%;
  padding: 13px;
}

/* ---------- 移动端 ---------- */
.mobile-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 40;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 18px;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(10px);
  border-top: 1px solid var(--line);
}

.mb-info {
  flex: 1;
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.mb-total {
  font-size: 21px;
  font-weight: 700;
  color: var(--brand);
}

.mb-qty {
  font-size: 12px;
  color: var(--muted);
}

.mobile-only {
  display: none;
}

.cart-panel.open {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 60;
  max-height: 82vh;
  border-radius: 20px 20px 0 0;
  box-shadow: var(--shadow-lg);
}

@media (min-width: 960px) {
  .layout {
    grid-template-columns: minmax(0, 1fr) 350px;
    align-items: start;
  }

  .cart-panel {
    position: sticky;
    top: 128px;
  }

  .mobile-bar {
    display: none;
  }
}

@media (max-width: 959px) {
  .mobile-only {
    display: grid;
  }

  .cart-panel {
    display: none;
  }

  .cart-panel.open {
    display: flex;
  }

  .grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 12px;
  }
}

/* ---------- 成功弹窗 ---------- */
.success-modal {
  max-width: 340px;
  padding: 34px 26px 26px;
  text-align: center;
}

.ok-icon {
  width: 60px;
  height: 60px;
  margin: 0 auto 16px;
  border-radius: 50%;
  background: var(--ok);
  color: #fff;
  font-size: 30px;
  display: grid;
  place-items: center;
}

.success-modal h3 {
  font-size: 19px;
  margin-bottom: 8px;
}

.ok-sub {
  font-size: 13px;
  color: var(--muted);
  margin-bottom: 14px;
}

.ok-amount {
  font-size: 28px;
  font-weight: 700;
  color: var(--brand);
  margin-bottom: 8px;
}

.ok-tip {
  font-size: 12px;
  color: var(--muted);
  margin-bottom: 18px;
}
</style>
