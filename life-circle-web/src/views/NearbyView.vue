<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import Ico from '../components/Ico.vue'
import PostCard from '../components/PostCard.vue'
import ShopCard from '../components/ShopCard.vue'
import SkeletonCard from '../components/SkeletonCard.vue'
import EmptyState from '../components/EmptyState.vue'
import RadarMap from '../components/RadarMap.vue'
import Lightbox from '../components/Lightbox.vue'
import Sheet from '../components/Sheet.vue'
import LocationSheet from '../components/LocationSheet.vue'
import { api } from '../api'
import { mockPosts, mockShops } from '../api/mock'
import { coordText, radiusOptions, state } from '../store/app'
import { formatDistance, formatCoord, formatDateTime } from '../utils/format'
import { toast } from '../composables/useToast'

const router = useRouter()

const posts = ref([])
const shops = ref([])
const loading = ref(true)
const refreshing = ref(false)
const source = ref('live') // live | demo
const lastError = ref('')

const locOpen = ref(false)
const detail = ref(null)
const lb = ref({ open: false, images: [], index: 0 })

const center = computed(() => ({ lng: state.lng, lat: state.lat }))

/** 演示数据的原因说明，避免把「手动开的演示模式」说成「后端挂了」 */
const demoReason = computed(() => {
  if (state.demo) return '你手动开启了演示模式。'
  if (lastError.value) return `后端未响应：${lastError.value}。`
  return ''
})

const stats = computed(() => {
  const list = posts.value
  const withDist = list.filter((p) => p.distance != null)
  const nearest = withDist.length ? Math.min(...withDist.map((p) => p.distance)) : null
  const farthest = withDist.length ? Math.max(...withDist.map((p) => p.distance)) : null
  const withImg = list.filter((p) => (p.images || []).length).length
  return { total: list.length, nearest, farthest, withImg }
})

const radarPoints = computed(() => [  ...posts.value
    .filter((p) => p.lng != null)
    .map((p) => ({
      lng: p.lng,
      lat: p.lat,
      kind: 'post',
      label: p.userId + ' 的帖子'
    })),
  ...shops.value
    .filter((s) => s.location)
    .map((s) => {
      const [a, b] = String(s.location).split(',')
      return { lng: Number(a), lat: Number(b), kind: 'shop', label: s.name }
    })
])

async function loadRadiusData({ silent = false } = {}) {
  if (!silent) loading.value = true
  refreshing.value = true
  lastError.value = ''

  const params = { lng: state.lng, lat: state.lat, radius: state.radius }

  try {
    const data = await api.nearbyPosts(params)
    posts.value = Array.isArray(data) ? data : []
    source.value = 'live'
    state.backend = { checked: true, online: true, message: '', latency: state.backend.latency }
    if (!silent) {
      toast.success(
        posts.value.length ? `已刷新，附近 ${posts.value.length} 条动态` : '已刷新，附近暂无动态'
      )
    }
  } catch (e) {
    lastError.value = e.message
    state.backend = { checked: true, online: false, message: e.message, latency: null }
    posts.value = mockPosts(center.value, state.radius)
    source.value = 'demo'
    if (!e.offline) toast.error('接口异常：' + e.message)
  }

  // 顺带拉一次店铺，供雷达图与概览使用（失败不影响主流程）
  try {
    const s = await api.nearbyShops({ lng: state.lng, lat: state.lat, radius: state.radius })
    shops.value = Array.isArray(s) ? s : []
  } catch {
    shops.value = source.value === 'demo' ? mockShops(center.value, state.radius) : []
  }

  loading.value = false
  refreshing.value = false
}

function setRadius(r) {
  state.radius = r
  loadRadiusData({ silent: true })
}

function openLightbox({ images, index }) {
  lb.value = { open: true, images, index }
}

function openDetail(post) {
  detail.value = post
}

function centerOn(post) {
  state.lng = post.lng
  state.lat = post.lat
  state.label = post.address || '帖子位置'
  detail.value = null
  toast.info('已把「' + (post.address || '这个位置') + '」设为中心')
  loadRadiusData({ silent: true })
}

function copyCoord(text) {
  navigator.clipboard
    .writeText(text)
    .then(() => toast.success('已复制：' + text))
    .catch(() => toast.error('复制失败'))
}

onMounted(() => loadRadiusData())

defineExpose({ loadRadiusData })
</script>

<template>
  <div class="page stack" style="gap: 18px">
    <!-- Hero -->
    <section class="hero card">
      <div class="hero-glow" />
      <div class="hero-top">
        <div>
          <p class="hero-hi">
            <Ico name="sparkle" :size="15" />
            嗨，{{ state.userId }}
          </p>
          <h1 class="hero-title">
            看看你<span class="grad-text"> 身边 </span>发生了什么
          </h1>
        </div>
        <button class="btn btn-ghost btn-icon" :disabled="refreshing" title="刷新" @click="loadRadiusData()">
          <Ico name="refresh" :size="17" :class="{ spin: refreshing }" />
        </button>
      </div>

      <button class="loc-pill" type="button" @click="locOpen = true">
        <Ico name="pin" :size="15" />
        <span class="ellipsis">{{ state.label }}</span>
        <span class="mono loc-coord">{{ coordText }}</span>
        <Ico name="chevronDown" :size="14" class="loc-arrow" />
      </button>

      <div class="row wrap" style="gap: 8px; margin-top: 14px">
        <button
          v-for="r in radiusOptions"
          :key="r"
          class="chip"
          :class="{ 'is-active': state.radius === r }"
          @click="setRadius(r)"
        >
          {{ formatDistance(r) }}
        </button>
      </div>
    </section>

    <!-- 数据来源提示 -->
    <div v-if="source === 'demo'" class="banner banner-warn">
      <Ico name="wifiOff" :size="16" style="margin-top: 1px" />
      <div>
        <b>当前展示的是演示数据。</b>{{ demoReason }}
        启动 <span class="mono">life-circle-api</span>（默认 8081）并确保 MySQL / Redis 就绪后点击刷新即可看到真实数据。
      </div>
    </div>

    <!-- 概览 -->
    <section class="stats">
      <div class="stat card">
        <span class="stat-k">附近动态</span>
        <span class="stat-v mono">{{ stats.total }}</span>
        <span class="stat-s">条 · 半径内</span>
      </div>
      <div class="stat card">
        <span class="stat-k">最近一条</span>
        <span class="stat-v mono">{{ stats.nearest != null ? formatDistance(stats.nearest) : '—' }}</span>
        <span class="stat-s">离你多近</span>
      </div>
      <div class="stat card">
        <span class="stat-k">带图的</span>
        <span class="stat-v mono">{{ stats.withImg }}</span>
        <span class="stat-s">条 · 有照片</span>
      </div>
      <div class="stat card">
        <span class="stat-k">覆盖半径</span>
        <span class="stat-v mono">{{ formatDistance(state.radius) }}</span>
        <span class="stat-s">当前筛选</span>
      </div>
    </section>

    <!-- 雷达 -->
    <RadarMap :center="center" :radius="state.radius" :points="radarPoints" />

    <!-- 动态流 -->
    <section class="stack" style="gap: 14px">
      <div class="between">
        <h2 class="sec-title">
          <Ico name="flame" :size="16" :stroke="2" />
          附近动态
        </h2>
        <span class="text-xs muted">
          按发布时间倒序 · 共 {{ posts.length }} 条
        </span>
      </div>

      <SkeletonCard v-if="loading" :count="3" />

      <EmptyState
        v-else-if="!posts.length"
        icon="compass"
        title="这个范围内还没有动态"
        desc="换个更大的半径试试，或者去发一条属于你的第一条动态。"
        action-text="刷新一下"
        @action="loadRadiusData()"
      />

      <div v-else class="stack stagger" style="gap: 14px">
        <PostCard
          v-for="p in posts"
          :key="p.id"
          :post="p"
          :me="state.userId"
          @preview="openLightbox"
          @locate="openDetail"
        />
      </div>

      <button class="btn btn-soft btn-block btn-lg" @click="router.push('/publish')">
        <Ico name="pen" :size="17" />
        发一条新动态
      </button>
    </section>

    <!-- 附近店铺速览 -->
    <section v-if="shops.length" class="stack" style="gap: 14px">
      <div class="between">
        <h2 class="sec-title">
          <Ico name="store" :size="16" :stroke="2" />
          半径内的店铺
        </h2>
        <button class="btn btn-ghost btn-sm" @click="router.push('/shops')">
          全部 <Ico name="chevron" :size="14" />
        </button>
      </div>
      <div class="stack stagger" style="gap: 10px">
        <ShopCard v-for="(s, i) in shops.slice(0, 3)" :key="s.name + i" :shop="s" :index="i" />
      </div>
    </section>

    <!-- 帖子位置详情 -->
    <Sheet :model-value="!!detail" title="位置详情" @update:model-value="detail = null">
      <div v-if="detail" class="stack" style="gap: 16px">
        <div class="detail-head">
          <div class="row" style="gap: 8px">
            <Ico name="user" :size="15" />
            <b>{{ detail.userId }}</b>
          </div>
          <p class="text-xs muted" style="margin-top: 4px">
            发布于 {{ formatDateTime(detail.createdAt) }}
          </p>
        </div>

        <div class="kv">
          <div class="kv-row">
            <span class="kv-k">地址</span>
            <span class="kv-v">{{ detail.address || '未知' }}</span>
          </div>
          <div class="kv-row">
            <span class="kv-k">经度</span>
            <span class="kv-v mono">{{ formatCoord(detail.lng) }}</span>
          </div>
          <div class="kv-row">
            <span class="kv-k">纬度</span>
            <span class="kv-v mono">{{ formatCoord(detail.lat) }}</span>
          </div>
          <div class="kv-row">
            <span class="kv-k">与你的距离</span>
            <span class="kv-v mono">{{ formatDistance(detail.distance) }}</span>
          </div>
        </div>

        <div class="row" style="gap: 10px">
          <button class="btn btn-ghost grow" @click="copyCoord(`${detail.lng},${detail.lat}`)">
            <Ico name="link" :size="15" /> 复制坐标
          </button>
          <button class="btn btn-primary grow" @click="centerOn(detail)">
            <Ico name="target" :size="15" /> 以此为中心
          </button>
        </div>
      </div>
    </Sheet>

    <LocationSheet v-model="locOpen" @update:model-value="(v) => v === false && loadRadiusData({ silent: true })" />
    <Lightbox
      v-model:open="lb.open"
      v-model:index="lb.index"
      :images="lb.images"
    />
  </div>
</template>

<style scoped>
/* ---------- Hero ---------- */
.hero {
  position: relative;
  overflow: hidden;
  padding: 22px;
  border: 1px solid var(--brand-100);
  background: linear-gradient(150deg, #fff 0%, var(--brand-50) 46%, var(--accent-50) 100%);
}
.hero-glow {
  position: absolute;
  width: 280px;
  height: 280px;
  right: -90px;
  top: -120px;
  border-radius: 50%;
  background: radial-gradient(circle, var(--accent-200) 0%, transparent 66%);
  filter: blur(6px);
  pointer-events: none;
}
.hero-top {
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
}
.hero-hi {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--brand-600);
}
.hero-title {
  margin-top: 5px;
  font-size: 23px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--ink-900);
  line-height: 1.32;
}
@media (min-width: 720px) {
  .hero-title {
    font-size: 27px;
  }
}

.loc-pill {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  margin-top: 16px;
  padding: 10px 14px;
  border-radius: var(--r-full);
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid var(--brand-200);
  font-size: 13.5px;
  font-weight: 600;
  color: var(--ink-800);
  text-align: left;
  backdrop-filter: blur(8px);
  transition: all 0.22s var(--ease);
}
.loc-pill:hover {
  border-color: var(--brand-400);
  box-shadow: 0 8px 20px -12px var(--brand-shadow);
}
.loc-pill :deep(.ico:first-child) {
  color: var(--brand-500);
}
.loc-coord {
  font-size: 11.5px;
  font-weight: 500;
  color: var(--ink-400);
  flex: none;
}
.loc-arrow {
  color: var(--ink-400);
  flex: none;
}
@media (max-width: 560px) {
  .loc-coord {
    display: none;
  }
}

/* ---------- 概览 ---------- */
.stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
@media (max-width: 760px) {
  .stats {
    grid-template-columns: repeat(2, 1fr);
  }
}
.stat {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 15px 16px;
  border-radius: var(--r-md);
}
.stat-k {
  font-size: 12px;
  font-weight: 600;
  color: var(--ink-400);
}
.stat-v {
  font-size: 21px;
  font-weight: 700;
  color: var(--ink-900);
  letter-spacing: -0.02em;
}
.stat-s {
  font-size: 11.5px;
  color: var(--ink-300);
}

/* ---------- 区块标题 ---------- */
.sec-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 17px;
  font-weight: 700;
  color: var(--ink-900);
  letter-spacing: -0.01em;
}
.sec-title :deep(.ico) {
  color: var(--brand-500);
}

.spin {
  animation: spin 0.9s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* ---------- 详情 ---------- */
.detail-head {
  padding: 14px 16px;
  border-radius: var(--r-md);
  background: var(--grad-soft);
  border: 1px solid var(--brand-100);
}
.detail-head :deep(.ico) {
  color: var(--brand-500);
}
.kv {
  border: 1px solid var(--line);
  border-radius: var(--r-md);
  overflow: hidden;
}
.kv-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 14px;
  padding: 12px 15px;
  font-size: 13.5px;
  background: var(--surface);
}
.kv-row + .kv-row {
  border-top: 1px solid var(--line);
}
.kv-k {
  color: var(--ink-400);
  flex: none;
}
.kv-v {
  color: var(--ink-900);
  font-weight: 600;
  text-align: right;
  word-break: break-all;
}
</style>
