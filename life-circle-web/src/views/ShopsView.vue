<script setup>
import { computed, onMounted, ref } from 'vue'
import Ico from '../components/Ico.vue'
import ShopCard from '../components/ShopCard.vue'
import EmptyState from '../components/EmptyState.vue'
import LocationSheet from '../components/LocationSheet.vue'
import { api } from '../api'
import { mockShops } from '../api/mock'
import { coordText, state } from '../store/app'
import { formatDistance } from '../utils/format'
import { toast } from '../composables/useToast'

const keyword = ref('')
const shops = ref([])
const loading = ref(true)
const refreshing = ref(false)
const source = ref('live')
const lastError = ref('')
const locOpen = ref(false)
const slider = ref(state.shopRadius)

const QUICK = ['咖啡', '美食', '便利店', '药店', '超市', '银行', '书店', '加油站']

const center = computed(() => ({ lng: state.lng, lat: state.lat }))

/** 演示数据的原因说明，避免把「手动开的演示模式」说成「后端挂了」 */
const demoReason = computed(() => {
  if (state.demo) return '你手动开启了演示模式。'
  if (lastError.value) return `后端未响应：${lastError.value}。`
  return ''
})

const nearest = computed(() => {
  if (!shops.value.length) return null
  return shops.value.reduce((a, b) => (Number(a.distance) < Number(b.distance) ? a : b))
})

async function search({ silent = false } = {}) {
  if (!silent) loading.value = true
  refreshing.value = true
  lastError.value = ''
  const params = {
    lng: state.lng,
    lat: state.lat,
    radius: state.shopRadius,
    keyword: keyword.value.trim()
  }
  try {
    const data = await api.nearbyShops(params)
    shops.value = Array.isArray(data) ? data : []
    source.value = 'live'
    if (!silent) {
      toast.success(shops.value.length ? `找到 ${shops.value.length} 个地点` : '附近没有匹配的地点')
    }
  } catch (e) {
    lastError.value = e.message
    shops.value = mockShops(center.value, state.shopRadius, keyword.value)
    source.value = 'demo'
    if (!e.offline) toast.error('接口异常：' + e.message)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function pickQuick(k) {
  keyword.value = keyword.value === k ? '' : k
  search()
}

function commitRadius() {
  state.shopRadius = Number(slider.value)
  search({ silent: false })
}

function reset() {
  keyword.value = ''
  slider.value = 3000
  state.shopRadius = 3000
  search()
}

onMounted(() => search())
</script>

<template>
  <div class="page stack" style="gap: 18px">
    <header class="head">
      <h1 class="h1">附近店铺</h1>
      <p class="sub">
        数据来自后端 <span class="mono">/api/shops/nearby</span>，它再向高德「周边搜索」
        <span class="mono">/v3/place/around</span> 取 POI，并带 Redis 缓存。
      </p>
    </header>

    <!-- 搜索区 -->
    <section class="card card-pad stack" style="gap: 16px">
      <div class="search">
        <Ico name="search" :size="17" class="s-ico" />
        <input
          v-model="keyword"
          class="s-input"
          placeholder="搜关键词，例如：咖啡、药店、地铁站"
          @keyup.enter="search()"
        />
        <button v-if="keyword" class="s-clear" title="清空" @click="(keyword = ''), search()">
          <Ico name="close" :size="14" />
        </button>
        <button class="btn btn-primary btn-sm" :disabled="refreshing" @click="search()">
          <Ico name="search" :size="14" />
          搜索
        </button>
      </div>

      <div class="row wrap" style="gap: 8px">
        <button
          v-for="k in QUICK"
          :key="k"
          class="chip"
          :class="{ 'is-active': keyword === k }"
          @click="pickQuick(k)"
        >
          {{ k }}
        </button>
      </div>

      <div class="radius">
        <div class="between" style="gap: 10px">
          <span class="label" style="margin: 0"><Ico name="target" :size="14" /> 搜索半径</span>
          <span class="radius-v mono">{{ formatDistance(state.shopRadius) }}</span>
        </div>
        <input
          v-model.number="slider"
          class="range"
          type="range"
          min="100"
          max="50000"
          step="100"
          @change="commitRadius"
        />
        <div class="between text-xs muted">
          <span>100 m</span>
          <span>25 km</span>
          <span>50 km</span>
        </div>
      </div>

      <div class="between wrap" style="gap: 10px">
        <button class="btn btn-ghost btn-sm" @click="locOpen = true">
          <Ico name="pin" :size="14" />
          <span class="ellipsis" style="max-width: 220px">{{ state.label }}</span>
        </button>
        <span class="mono text-xs muted">{{ coordText }}</span>
      </div>
    </section>

    <div v-if="source === 'demo'" class="banner banner-warn">
      <Ico name="wifiOff" :size="16" style="margin-top: 1px" />
      <div>
        <b>当前展示的是演示 POI。</b>{{ demoReason }}
        真实数据依赖后端配置的高德 Key 与 Redis 缓存。
      </div>
    </div>

    <!-- 概览 -->
    <section class="mini">
      <div class="mini-item">
        <span class="mini-k">命中地点</span>
        <span class="mini-v mono">{{ shops.length }}</span>
      </div>
      <div class="mini-item">
        <span class="mini-k">最近</span>
        <span class="mini-v mono">{{ nearest ? formatDistance(nearest.distance) : '—' }}</span>
      </div>
      <div class="mini-item">
        <span class="mini-k">最远</span>
        <span class="mini-v mono">
          {{ shops.length ? formatDistance(Math.max(...shops.map((s) => Number(s.distance)))) : '—' }}
        </span>
      </div>
      <div class="mini-item">
        <span class="mini-k">关键词</span>
        <span class="mini-v">{{ keyword || '不限' }}</span>
      </div>
    </section>

    <!-- 结果 -->
    <section class="stack" style="gap: 12px">
      <div class="between">
        <h2 class="sec-title"><Ico name="store" :size="16" :stroke="2" /> 搜索结果</h2>
        <button class="btn btn-ghost btn-sm" @click="reset">
          <Ico name="refresh" :size="14" /> 重置
        </button>
      </div>

      <div v-if="loading" class="stack" style="gap: 10px">
        <div v-for="i in 5" :key="i" class="card sk-row">
          <div class="sk" style="width: 40px; height: 40px; border-radius: 12px" />
          <div class="grow stack" style="gap: 8px">
            <div class="sk" style="width: 42%; height: 13px" />
            <div class="sk" style="width: 68%; height: 10px" />
          </div>
        </div>
      </div>

      <EmptyState
        v-else-if="!shops.length"
        icon="search"
        title="没有找到匹配的地点"
        :desc="keyword ? `换个关键词，或者把半径调大一点再试试「${keyword}」。` : '把半径调大一点再试试。'"
        action-text="重置条件"
        @action="reset"
      />

      <div v-else class="stack stagger" style="gap: 10px">
        <ShopCard v-for="(s, i) in shops" :key="(s.name || '') + i" :shop="s" :index="i" />
      </div>
    </section>

    <LocationSheet v-model="locOpen" />
  </div>
</template>

<style scoped>
.head .h1 {
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -0.025em;
  color: var(--ink-900);
}
.head .sub {
  margin-top: 7px;
  font-size: 13.5px;
  color: var(--ink-400);
  max-width: 680px;
  line-height: 1.7;
}

.search {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 6px 6px 15px;
  border-radius: var(--r-full);
  background: var(--surface);
  border: 1.5px solid var(--line-strong);
  transition: all 0.22s var(--ease);
}
.search:focus-within {
  border-color: var(--brand-400);
  box-shadow: 0 0 0 4px var(--brand-100);
}
.s-ico {
  color: var(--ink-400);
}
.s-input {
  flex: 1;
  min-width: 0;
  height: 36px;
  border: 0;
  outline: none;
  background: none;
  font-size: 14px;
}
.s-input::placeholder {
  color: var(--ink-300);
}
.s-clear {
  width: 24px;
  height: 24px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: var(--ink-400);
  background: var(--line);
}
.s-clear:hover {
  color: var(--ink-700);
}

.radius {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.radius-v {
  font-size: 15px;
  font-weight: 700;
  color: var(--brand-600);
}

.mini {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1px;
  background: var(--line);
  border: 1px solid var(--line);
  border-radius: var(--r-md);
  overflow: hidden;
}
@media (max-width: 700px) {
  .mini {
    grid-template-columns: repeat(2, 1fr);
  }
}
.mini-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 13px 16px;
  background: var(--surface);
}
.mini-k {
  font-size: 11.5px;
  font-weight: 600;
  color: var(--ink-400);
}
.mini-v {
  font-size: 16px;
  font-weight: 700;
  color: var(--ink-900);
  letter-spacing: -0.015em;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sec-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 17px;
  font-weight: 700;
  color: var(--ink-900);
}
.sec-title :deep(.ico) {
  color: var(--brand-500);
}

.sk-row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 17px 18px;
  border-radius: var(--r-md);
}
</style>
