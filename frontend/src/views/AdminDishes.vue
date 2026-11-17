<template>
  <div class="page">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="left">
        <h2>菜品管理</h2>
        <span class="count">{{ filtered.length }} 个菜品</span>
      </div>
      <div class="tools">
        <input v-model="keyword" class="search" placeholder="搜索菜品名称" />
        <select v-model="catFilter" class="filter">
          <option :value="null">全部分类</option>
          <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
        </select>
        <button class="btn btn-sm" @click="openCategoryModal">分类管理</button>
        <button class="btn btn-sm btn-primary" @click="openCreate">＋ 新建菜品</button>
      </div>
    </div>

    <div v-if="loading" class="empty">加载中…</div>
    <div v-else-if="error" class="empty">{{ error }}</div>

    <div v-else class="table-wrap card">
      <table>
        <thead>
          <tr>
            <th class="w-img">图片</th>
            <th>名称</th>
            <th>分类</th>
            <th class="w-price">价格</th>
            <th class="w-sort">排序</th>
            <th class="w-status">状态</th>
            <th class="w-ops">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!filtered.length">
            <td colspan="7" class="empty-row">暂无菜品，点右上角「新建菜品」添加</td>
          </tr>
          <tr v-for="d in filtered" :key="d.id">
            <td>
              <div class="thumb" :style="d.imageUrl ? { backgroundImage: `url('${d.imageUrl}')` } : {}">
                <span v-if="!d.imageUrl">{{ d.name.slice(0, 1) }}</span>
              </div>
            </td>
            <td>
              <div class="name">{{ d.name }}</div>
              <div class="desc">{{ d.description || '—' }}</div>
            </td>
            <td>{{ categoryName(d.categoryId) }}</td>
            <td class="price">¥{{ money(d.price) }}</td>
            <td>{{ d.sortNo }}</td>
            <td>
              <span class="badge" :class="d.isAvailable ? 'on' : 'off'">
                {{ d.isAvailable ? '上架' : '下架' }}
              </span>
            </td>
            <td>
              <div class="ops">
                <button class="btn btn-sm" @click="openEdit(d)">编辑</button>
                <button class="btn btn-sm" @click="toggle(d)">{{ d.isAvailable ? '下架' : '上架' }}</button>
                <button class="btn btn-sm btn-danger" @click="remove(d)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 新增 / 编辑弹窗 -->
    <div v-if="editing" class="overlay" @click.self="closeEdit">
      <div class="modal">
        <div class="modal-head">
          <h3>{{ form.id ? '编辑菜品' : '新建菜品' }}</h3>
          <button class="close-x" @click="closeEdit">✕</button>
        </div>

        <div class="modal-body">
          <div class="field">
            <label>菜品名称 *</label>
            <input v-model="form.name" placeholder="例如 宫保鸡丁" maxlength="50" />
          </div>

          <div class="row2">
            <div class="field">
              <label>分类 *</label>
              <select v-model.number="form.categoryId">
                <option :value="null" disabled>请选择分类</option>
                <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
              </select>
            </div>
            <div class="field">
              <label>价格（元）*</label>
              <input v-model.number="form.price" type="number" min="0" step="0.01" placeholder="0.00" />
            </div>
          </div>

          <div class="field">
            <label>描述</label>
            <textarea v-model="form.description" rows="2" placeholder="一句话介绍，例如：经典川菜，微辣" />
          </div>

          <div class="field">
            <label>菜品图片</label>
            <div class="uploader">
              <div class="preview" :style="form.imageUrl ? { backgroundImage: `url('${form.imageUrl}')` } : {}">
                <span v-if="!form.imageUrl">暂无图片</span>
              </div>
              <div class="upload-actions">
                <label class="btn btn-sm" :class="{ disabled: uploading }">
                  {{ uploading ? '上传中…' : '选择图片' }}
                  <input type="file" accept="image/*" hidden :disabled="uploading" @change="onFile" />
                </label>
                <button v-if="form.imageUrl" class="btn btn-sm btn-danger" @click="form.imageUrl = ''">
                  移除
                </button>
                <p class="tip">支持 jpg / png / webp，最大 5MB</p>
              </div>
            </div>
          </div>

          <div class="row2">
            <div class="field">
              <label>排序号（越小越靠前）</label>
              <input v-model.number="form.sortNo" type="number" min="0" step="1" />
            </div>
            <div class="field">
              <label>上架状态</label>
              <button
                class="switch"
                :class="{ on: form.isAvailable === 1 }"
                @click="form.isAvailable = form.isAvailable === 1 ? 0 : 1"
              >
                <span class="knob"></span>
                {{ form.isAvailable === 1 ? '上架中' : '已下架' }}
              </button>
            </div>
          </div>
        </div>

        <div class="modal-foot">
          <button class="btn" @click="closeEdit">取消</button>
          <button class="btn btn-primary" :disabled="saving" @click="save">
            {{ saving ? '保存中…' : '保存' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 分类管理弹窗 -->
    <div v-if="catModal" class="overlay" @click.self="catModal = false">
      <div class="modal cat-modal">
        <div class="modal-head">
          <h3>分类管理</h3>
          <button class="close-x" @click="catModal = false">✕</button>
        </div>
        <div class="modal-body">
          <ul class="cat-list">
            <li v-for="c in categories" :key="c.id">
              <span class="cat-order">{{ c.sortNo }}</span>
              <span class="cat-name">{{ c.name }}</span>
              <span class="cat-id">#{{ c.id }}</span>
              <button class="btn btn-sm btn-danger" @click="removeCategory(c)">删除</button>
            </li>
            <li v-if="!categories.length" class="empty-row">还没有分类</li>
          </ul>

          <div class="cat-add">
            <input v-model="newCat.name" placeholder="新分类名称，如 汤羹" maxlength="20" />
            <input v-model.number="newCat.sortNo" type="number" min="0" class="sort-input" placeholder="排序" />
            <button class="btn btn-primary" @click="addCategory">添加</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="toast" class="toast" :class="{ error: toastError }">{{ toast }}</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { api } from '../api'

const dishes = ref([])
const categories = ref([])
const keyword = ref('')
const catFilter = ref(null)
const loading = ref(true)
const error = ref('')
const editing = ref(false)
const saving = ref(false)
const uploading = ref(false)
const catModal = ref(false)
const newCat = ref({ name: '', sortNo: 0 })
const toast = ref('')
const toastError = ref(false)

let toastTimer = null
function showToast(msg, isError = false) {
  toast.value = msg
  toastError.value = isError
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value = ''), 2500)
}

function blank() {
  return {
    id: null,
    name: '',
    categoryId: null,
    price: 0,
    description: '',
    imageUrl: '',
    isAvailable: 1,
    sortNo: 0,
  }
}

const form = ref(blank())

function money(v) {
  return Number(v || 0).toFixed(2)
}

function categoryName(id) {
  return categories.value.find((c) => c.id === id)?.name || '—'
}

const filtered = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  return dishes.value.filter((d) => {
    if (catFilter.value !== null && d.categoryId !== catFilter.value) return false
    if (kw && !(d.name || '').toLowerCase().includes(kw)) return false
    return true
  })
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [cats, list] = await Promise.all([api.getCategories(), api.getDishes()])
    categories.value = cats || []
    dishes.value = list || []
  } catch (e) {
    error.value = '加载失败：' + e.message
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.value = blank()
  editing.value = true
}

function openEdit(d) {
  form.value = {
    id: d.id,
    name: d.name,
    categoryId: d.categoryId,
    price: Number(d.price),
    description: d.description || '',
    imageUrl: d.imageUrl || '',
    isAvailable: d.isAvailable,
    sortNo: d.sortNo ?? 0,
  }
  editing.value = true
}

function closeEdit() {
  editing.value = false
}

async function save() {
  if (saving.value) return
  if (!form.value.name.trim()) return showToast('请填写菜品名称', true)
  if (!form.value.categoryId) return showToast('请选择分类', true)
  if (form.value.price === null || form.value.price === '' || Number(form.value.price) < 0) {
    return showToast('请填写正确的价格', true)
  }

  saving.value = true
  try {
    const payload = {
      name: form.value.name.trim(),
      categoryId: form.value.categoryId,
      price: Number(form.value.price),
      description: form.value.description,
      imageUrl: form.value.imageUrl || '',
      isAvailable: form.value.isAvailable,
      sortNo: Number(form.value.sortNo) || 0,
    }
    if (form.value.id) {
      await api.updateDish(form.value.id, payload)
      showToast('已保存修改')
    } else {
      await api.createDish(payload)
      showToast('菜品已创建')
    }
    editing.value = false
    await load()
  } catch (e) {
    showToast(e.message, true)
  } finally {
    saving.value = false
  }
}

async function toggle(d) {
  try {
    await api.toggleDish(d.id)
    await load()
  } catch (e) {
    showToast(e.message, true)
  }
}

async function remove(d) {
  if (!confirm(`确定删除「${d.name}」吗？`)) return
  try {
    await api.deleteDish(d.id)
    showToast('已删除')
    await load()
  } catch (e) {
    showToast(e.message, true)
  }
}

async function onFile(e) {
  const file = e.target.files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const res = await api.upload(file)
    form.value.imageUrl = res.url
    showToast('图片上传成功')
  } catch (err) {
    showToast(err.message, true)
  } finally {
    uploading.value = false
    e.target.value = ''
  }
}

function openCategoryModal() {
  newCat.value = { name: '', sortNo: categories.value.length }
  catModal.value = true
}

async function addCategory() {
  if (!newCat.value.name.trim()) return showToast('请填写分类名称', true)
  try {
    await api.createCategory({ name: newCat.value.name.trim(), sortNo: Number(newCat.value.sortNo) || 0 })
    newCat.value = { name: '', sortNo: categories.value.length }
    showToast('分类已添加')
    await load()
  } catch (e) {
    showToast(e.message, true)
  }
}

async function removeCategory(c) {
  if (!confirm(`确定删除分类「${c.name}」吗？`)) return
  try {
    await api.deleteCategory(c.id)
    showToast('已删除分类')
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
  margin-bottom: 18px;
  flex-wrap: wrap;
}

.left {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.left h2 {
  font-size: 19px;
}

.count {
  font-size: 13px;
  color: var(--muted);
}

.tools {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.search {
  width: 190px;
  font-size: 14px;
  padding: 9px 12px;
}

.filter {
  width: auto;
  font-size: 14px;
  padding: 9px 12px;
}

/* ---------- 表格 ---------- */
.table-wrap {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
  min-width: 760px;
}

th {
  text-align: left;
  font-size: 12px;
  font-weight: 700;
  color: var(--muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 13px 14px;
  border-bottom: 1px solid var(--line);
  background: #fdfbf8;
  white-space: nowrap;
}

td {
  padding: 12px 14px;
  border-bottom: 1px solid var(--line);
  vertical-align: middle;
}

tbody tr:last-child td {
  border-bottom: none;
}

tbody tr:hover {
  background: #fdfbf8;
}

.w-img {
  width: 76px;
}

.w-price {
  width: 90px;
}

.w-sort {
  width: 70px;
}

.w-status {
  width: 90px;
}

.w-ops {
  width: 210px;
}

.thumb {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  background: linear-gradient(135deg, #f6ede2, #efe3d3);
  background-size: cover;
  background-position: center;
  display: grid;
  place-items: center;
  font-size: 18px;
  font-weight: 700;
  color: #cbb79c;
}

.name {
  font-weight: 600;
  margin-bottom: 2px;
}

.desc {
  font-size: 12px;
  color: var(--muted);
  max-width: 220px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.price {
  font-weight: 700;
  color: var(--brand);
}

.badge {
  font-size: 12px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 999px;
  white-space: nowrap;
}

.badge.on {
  background: #e9f6ee;
  color: var(--ok);
}

.badge.off {
  background: #f1eee9;
  color: var(--muted);
}

.ops {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.empty-row {
  text-align: center;
  color: var(--muted);
  padding: 40px 0 !important;
}

/* ---------- 弹窗表单 ---------- */
.row2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.uploader {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}

.preview {
  width: 96px;
  height: 96px;
  flex: 0 0 auto;
  border-radius: 12px;
  border: 1px dashed var(--line);
  background: #faf7f2 center / cover no-repeat;
  display: grid;
  place-items: center;
  font-size: 12px;
  color: var(--muted);
}

.upload-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-start;
}

.upload-actions .btn.disabled {
  opacity: 0.6;
  pointer-events: none;
}

.tip {
  font-size: 11px;
  color: var(--muted);
}

.switch {
  display: flex;
  align-items: center;
  gap: 9px;
  width: 100%;
  padding: 10px 13px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: #fff;
  font-size: 14px;
  color: var(--ink-soft);
}

.switch .knob {
  width: 38px;
  height: 22px;
  border-radius: 999px;
  background: #ddd5c9;
  position: relative;
  transition: background 0.15s;
  flex: 0 0 auto;
}

.switch .knob::after {
  content: '';
  position: absolute;
  top: 3px;
  left: 3px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #fff;
  transition: transform 0.15s;
}

.switch.on {
  border-color: #bfe3cd;
  color: var(--ok);
  background: #f6fdf9;
}

.switch.on .knob {
  background: var(--ok);
}

.switch.on .knob::after {
  transform: translateX(16px);
}

/* ---------- 分类 ---------- */
.cat-modal {
  max-width: 460px;
}

.cat-list {
  list-style: none;
  margin: 0 0 16px;
  padding: 0;
  max-height: 260px;
  overflow-y: auto;
}

.cat-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 0;
  border-bottom: 1px dashed var(--line);
  font-size: 14px;
}

.cat-order {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  background: #f5f0e9;
  color: var(--muted);
  font-size: 12px;
  font-weight: 700;
  display: grid;
  place-items: center;
}

.cat-name {
  flex: 1;
  font-weight: 600;
}

.cat-id {
  font-size: 12px;
  color: var(--muted);
}

.cat-add {
  display: flex;
  gap: 8px;
}

.cat-add input {
  font-size: 14px;
}

.sort-input {
  width: 80px;
  flex: 0 0 auto;
}
</style>
