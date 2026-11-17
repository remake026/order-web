const BASE = '/api'

async function request(path, options = {}) {
  const res = await fetch(BASE + path, {
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
    ...options,
  })

  const text = await res.text()
  let data = null
  if (text) {
    try {
      data = JSON.parse(text)
    } catch {
      data = text
    }
  }

  if (!res.ok) {
    const msg = data && data.message ? data.message : `请求失败（${res.status}）`
    throw new Error(msg)
  }
  return data
}

export const api = {
  // 分类
  getCategories: () => request('/categories'),
  createCategory: (data) => request('/categories', { method: 'POST', body: JSON.stringify(data) }),
  updateCategory: (id, data) => request(`/categories/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteCategory: (id) => request(`/categories/${id}`, { method: 'DELETE' }),

  // 菜品
  getDishes: (params = {}) => {
    const qs = new URLSearchParams()
    if (params.categoryId) qs.set('categoryId', params.categoryId)
    if (params.availableOnly) qs.set('availableOnly', 'true')
    return request(`/dishes${qs.toString() ? '?' + qs.toString() : ''}`)
  },
  createDish: (data) => request('/dishes', { method: 'POST', body: JSON.stringify(data) }),
  updateDish: (id, data) => request(`/dishes/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteDish: (id) => request(`/dishes/${id}`, { method: 'DELETE' }),
  toggleDish: (id) => request(`/dishes/${id}/toggle`, { method: 'PATCH' }),

  // 订单
  submitOrder: (data) => request('/orders', { method: 'POST', body: JSON.stringify(data) }),
  getOrders: (status) => request(`/orders${status ? '?status=' + status : ''}`),
  updateOrderStatus: (id, status) =>
    request(`/orders/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) }),

  // 登录
  login: (username, password) =>
    request('/auth/login', { method: 'POST', body: JSON.stringify({ username, password }) }),

  // 图片上传（multipart，不能用上面的 JSON 封装）
  async upload(file) {
    const fd = new FormData()
    fd.append('file', file)
    const res = await fetch(BASE + '/upload', { method: 'POST', body: fd })
    const text = await res.text()
    let data = null
    if (text) {
      try {
        data = JSON.parse(text)
      } catch {
        data = text
      }
    }
    if (!res.ok) {
      throw new Error(data && data.message ? data.message : '图片上传失败')
    }
    return data
  },
}
