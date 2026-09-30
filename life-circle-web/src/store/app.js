import { reactive, computed, watch } from 'vue'

const KEY = {
  userId: 'lc.userId',
  lng: 'lc.lng',
  lat: 'lc.lat',
  label: 'lc.label',
  radius: 'lc.radius',
  shopRadius: 'lc.shopRadius',
  accent: 'lc.accent',
  apiBase: 'lc.apiBase',
  demo: 'lc.demo',
  likes: 'lc.likes',
  draft: 'lc.draft'
}

function get(k) {
  try {
    return localStorage.getItem(k)
  } catch {
    return null
  }
}
function set(k, v) {
  try {
    if (v === null || v === undefined || v === '') localStorage.removeItem(k)
    else localStorage.setItem(k, String(v))
  } catch {
    /* 隐私模式忽略 */
  }
}
function getJSON(k, fallback) {
  try {
    const raw = localStorage.getItem(k)
    return raw ? JSON.parse(raw) : fallback
  } catch {
    return fallback
  }
}

/** 常用城市预设，省得每次手填经纬度 */
export const CITY_PRESETS = [
  { name: '北京 · 天安门', lng: 116.397428, lat: 39.90923 },
  { name: '上海 · 人民广场', lng: 121.473701, lat: 31.230416 },
  { name: '广州 · 珠江新城', lng: 113.32452, lat: 23.119657 },
  { name: '深圳 · 市民中心', lng: 114.057868, lat: 22.543099 },
  { name: '杭州 · 西湖', lng: 120.15507, lat: 30.274085 },
  { name: '成都 · 天府广场', lng: 104.065735, lat: 30.659462 },
  { name: '武汉 · 江汉路', lng: 114.303431, lat: 30.584846 },
  { name: '西安 · 钟楼', lng: 108.93977, lat: 34.341574 }
]

function randomUserId() {
  return 'user_' + Math.random().toString(36).slice(2, 8)
}

export const state = reactive({
  userId: get(KEY.userId) || randomUserId(),
  lng: Number(get(KEY.lng)) || CITY_PRESETS[0].lng,
  lat: Number(get(KEY.lat)) || CITY_PRESETS[0].lat,
  label: get(KEY.label) || CITY_PRESETS[0].name,

  radius: Number(get(KEY.radius)) || 5000,
  shopRadius: Number(get(KEY.shopRadius)) || 3000,

  accent: get(KEY.accent) || 'rose',
  apiBase: get(KEY.apiBase) || '',
  demo: get(KEY.demo) === '1',

  /** 后端连通状态 */
  backend: { checked: false, online: false, message: '', latency: null },

  likes: getJSON(KEY.likes, {})
})

/* ---------------- 派生 ---------------- */

export const coordText = computed(
  () => `${state.lng.toFixed(6)}, ${state.lat.toFixed(6)}`
)

export const radiusOptions = [1000, 3000, 5000, 10000, 20000]

/** 当前是否处于演示数据模式（显式开启，或后端不可达） */
export const usingDemo = computed(
  () => state.demo || (state.backend.checked && !state.backend.online)
)

/* ---------------- 持久化 ---------------- */

watch(
  () => [state.userId, state.lng, state.lat, state.label, state.radius, state.shopRadius, state.accent, state.apiBase],
  ([userId, lng, lat, label, radius, shopRadius, accent, apiBase]) => {
    set(KEY.userId, userId)
    set(KEY.lng, lng)
    set(KEY.lat, lat)
    set(KEY.label, label)
    set(KEY.radius, radius)
    set(KEY.shopRadius, shopRadius)
    set(KEY.accent, accent)
    set(KEY.apiBase, apiBase)
  }
)

watch(
  () => state.demo,
  (v) => set(KEY.demo, v ? '1' : ''),
  { immediate: true }
)

watch(
  () => state.accent,
  (v) => {
    document.documentElement.dataset.accent = v
    const meta = document.querySelector('meta[name="theme-color"]')
    if (meta) meta.setAttribute('content', v === 'rose' ? '#FF5C8D' : '#33C6B7')
  },
  { immediate: true }
)

watch(
  () => state.likes,
  (v) => {
    try {
      localStorage.setItem(KEY.likes, JSON.stringify(v))
    } catch {
      /* ignore */
    }
  },
  { deep: true }
)

/* ---------------- 动作 ---------------- */

export function toggleLike(id) {
  state.likes[id] = !state.likes[id]
}

export function isLiked(id) {
  return !!state.likes[id]
}

export function setLocation(lng, lat, label) {
  state.lng = Number(lng)
  state.lat = Number(lat)
  if (label) state.label = label
}

export function useCity(city) {
  setLocation(city.lng, city.lat, city.name)
}

/** 浏览器定位；失败时抛出可读中文错误 */
export function locate() {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('当前浏览器不支持定位，请手动填写经纬度'))
      return
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const { longitude, latitude } = pos.coords
        setLocation(longitude, latitude, '当前位置')
        resolve({ lng: longitude, lat: latitude })
      },
      (err) => {
        const map = {
          1: '定位权限被拒绝，可在浏览器地址栏左侧重新授权，或手动填写经纬度',
          2: '定位信息不可用，请手动填写经纬度',
          3: '定位超时，请重试或手动填写经纬度'
        }
        reject(new Error(map[err.code] || '定位失败，请手动填写经纬度'))
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 }
    )
  })
}

/** 草稿：发帖页内容落地，切页不丢 */
export function saveDraft(d) {
  try {
    sessionStorage.setItem(KEY.draft, JSON.stringify(d))
  } catch {
    /* ignore */
  }
}
export function loadDraft() {
  try {
    const raw = sessionStorage.getItem(KEY.draft)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}
export function clearDraft() {
  try {
    sessionStorage.removeItem(KEY.draft)
  } catch {
    /* ignore */
  }
}

export function resetAllLocalData() {
  Object.values(KEY).forEach((k) => {
    try {
      localStorage.removeItem(k)
      sessionStorage.removeItem(k)
    } catch {
      /* ignore */
    }
  })
}
