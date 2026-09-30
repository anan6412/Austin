<script setup>
import { computed, onMounted, ref } from 'vue'
import Ico from '../components/Ico.vue'
import LocationSheet from '../components/LocationSheet.vue'
import { api } from '../api'
import { coordText, resetAllLocalData, state } from '../store/app'
import { gradientOf, initials } from '../utils/format'
import { toast } from '../composables/useToast'

const locOpen = ref(false)
const checking = ref(false)
const showEndpoints = ref(true)

const avatarGrad = computed(() => gradientOf(state.userId))
const avatarText = computed(() => initials(state.userId))

const likeCount = computed(() => Object.values(state.likes || {}).filter(Boolean).length)

const status = computed(() => {
  const b = state.backend
  if (!b.checked) return { tone: 'idle', text: '尚未检测', cls: 'tag-neutral' }
  if (b.online) return { tone: 'ok', text: b.latency != null ? `在线 · ${b.latency} ms` : '在线', cls: 'tag-ok' }
  return { tone: 'off', text: '不可达', cls: 'tag-warn' }
})

const ENDPOINTS = [
  { m: 'GET', p: '/api/hello', d: '健康检查，返回空 data 的 Result' },
  { m: 'GET', p: '/api/posts/nearby', d: '附近帖子：?lng&lat&radius（默认 5000）' },
  { m: 'POST', p: '/api/posts', d: '创建帖子：{ userId, text, images[], lng, lat }' },
  { m: 'GET', p: '/api/shops/nearby', d: '附近店铺：?lng&lat&radius&keyword' }
]

async function checkBackend() {
  checking.value = true
  const t0 = performance.now()
  try {
    await api.health()
    state.backend = {
      checked: true,
      online: true,
      message: '',
      latency: Math.round(performance.now() - t0)
    }
    toast.success('后端连接正常')
  } catch (e) {
    state.backend = { checked: true, online: false, message: e.message, latency: null }
    toast.error('后端不可达：' + e.message)
  } finally {
    checking.value = false
  }
}

function persistApiBase() {
  toast.success(state.apiBase ? '已切换到 ' + state.apiBase : '已恢复使用同源 / 代理地址')
  checkBackend()
}

function clearLocal() {
  if (!window.confirm('将清空本机保存的用户 ID、位置、点赞与主题偏好，确定继续？')) return
  resetAllLocalData()
  toast.success('本地数据已清空，刷新后生效')
  setTimeout(() => window.location.reload(), 700)
}

onMounted(() => {
  if (!state.backend.checked) checkBackend()
})
</script>

<template>
  <div class="page stack" style="gap: 18px">
    <header class="head">
      <h1 class="h1">我的</h1>
      <p class="sub">身份、位置、主题与后端连接，都集中在这里。</p>
    </header>

    <!-- 身份 -->
    <section class="card card-pad card-accent id-card">
      <div class="row" style="gap: 14px">
        <div class="avatar avatar-lg" :style="{ background: avatarGrad }">{{ avatarText }}</div>
        <div class="grow" style="min-width: 0">
          <p class="id-name ellipsis">{{ state.userId }}</p>
          <p class="text-xs muted" style="margin-top: 3px">
            帖子的 <span class="mono">user_id</span> 就用这个值，换一个等于换身份
          </p>
        </div>
      </div>
      <div class="row" style="gap: 10px; margin-top: 16px">
        <input
          v-model="state.userId"
          class="input grow"
          placeholder="修改用户 ID"
          maxlength="32"
        />
        <button class="btn btn-soft" @click="toast.success('身份已更新为 ' + state.userId)">
          <Ico name="check" :size="15" /> 保存
        </button>
      </div>
    </section>

    <!-- 主题 -->
    <section class="card card-pad stack" style="gap: 16px">
      <div class="between">
        <h2 class="sec-title"><Ico name="palette" :size="16" :stroke="2" /> 主色调</h2>
        <span class="tag tag-neutral">玫粉色 × 薄荷蓝</span>
      </div>

      <div class="themes">
        <button
          class="theme"
          :class="{ 'is-on': state.accent === 'rose' }"
          @click="state.accent = 'rose'"
        >
          <div class="theme-bar" style="background: linear-gradient(135deg, #ff5c8d, #ffa8c4 55%, #55d6c9)">
            <span class="swatch" style="background: #ff5c8d" />
            <span class="swatch" style="background: #ffa8c4" />
            <span class="swatch" style="background: #55d6c9" />
          </div>
          <div class="theme-meta">
            <span class="theme-name">玫粉为主</span>
            <span class="text-xs muted">#FF5C8D</span>
          </div>
          <span v-if="state.accent === 'rose'" class="theme-tick"><Ico name="check" :size="12" :stroke="3" /></span>
        </button>

        <button
          class="theme"
          :class="{ 'is-on': state.accent === 'mint' }"
          @click="state.accent = 'mint'"
        >
          <div class="theme-bar" style="background: linear-gradient(135deg, #33c6b7, #7fe3da 55%, #ff85ac)">
            <span class="swatch" style="background: #33c6b7" />
            <span class="swatch" style="background: #7fe3da" />
            <span class="swatch" style="background: #ff85ac" />
          </div>
          <div class="theme-meta">
            <span class="theme-name">薄荷蓝为主</span>
            <span class="text-xs muted">#33C6B7</span>
          </div>
          <span v-if="state.accent === 'mint'" class="theme-tick"><Ico name="check" :size="12" :stroke="3" /></span>
        </button>
      </div>

      <p class="hint">
        两个色值是同一套语义变量，切换主题时按钮、标签、图表、雷达图会整体换色，而不是简单改一个背景。
      </p>
    </section>

    <!-- 位置 -->
    <section class="card card-pad stack" style="gap: 12px">
      <div class="between">
        <h2 class="sec-title"><Ico name="pin" :size="16" :stroke="2" /> 当前位置</h2>
        <button class="btn btn-ghost btn-sm" @click="locOpen = true">
          <Ico name="sliders" :size="14" /> 修改
        </button>
      </div>
      <div class="kv">
        <div class="kv-row">
          <span class="kv-k">别名</span>
          <span class="kv-v">{{ state.label }}</span>
        </div>
        <div class="kv-row">
          <span class="kv-k">经纬度</span>
          <span class="kv-v mono">{{ coordText }}</span>
        </div>
      </div>
    </section>

    <!-- 后端连接 -->
    <section class="card card-pad stack" style="gap: 16px">
      <div class="between">
        <h2 class="sec-title"><Ico name="server" :size="16" :stroke="2" /> 后端连接</h2>
        <span class="tag" :class="status.cls">
          <span class="dot" :class="status.tone === 'ok' ? 'dot-live' : 'dot-off'" />
          {{ status.text }}
        </span>
      </div>

      <label class="field">
        <span class="label">接口根地址 <span class="muted" style="font-weight: 500">（留空 = 同源 / vite 代理）</span></span>
        <div class="row" style="gap: 10px">
          <input
            v-model="state.apiBase"
            class="input input-mono grow"
            placeholder="http://127.0.0.1:8081"
          />
          <button class="btn btn-soft" @click="persistApiBase">
            <Ico name="check" :size="15" /> 应用
          </button>
        </div>
        <span class="hint">
          后端默认端口来自 <span class="mono">application.properties</span> 的
          <span class="mono">server.port=8081</span>。开发模式下前端跑在 5173，vite 会把
          <span class="mono">/api</span> 代理过去。
        </span>
      </label>

      <div class="row wrap" style="gap: 10px">
        <button class="btn btn-primary" :disabled="checking" @click="checkBackend">
          <Ico name="refresh" :size="15" :class="{ spin: checking }" />
          {{ checking ? '检测中…' : '检测连接' }}
        </button>
        <button class="btn btn-ghost" @click="state.demo = !state.demo">
          <Ico name="layers" :size="15" />
          {{ state.demo ? '关闭演示模式' : '强制演示模式' }}
        </button>
      </div>

      <div v-if="state.backend.checked && !state.backend.online" class="banner banner-warn">
        <Ico name="alert" :size="16" style="margin-top: 1px" />
        <div>
          <b>后端暂不可达。</b>{{ state.backend.message }}
          <br />
          启动顺序：MySQL → Redis → <span class="mono">mvn spring-boot:run</span>。
          若只是 <span class="mono">/api/shops/nearby</span> 报错，通常是高德 Key 未配置。
        </div>
      </div>
      <div v-else-if="state.demo" class="banner banner-info">
        <Ico name="info" :size="16" style="margin-top: 1px" />
        <div>演示模式已开启：所有请求都走本地假数据，不会触碰后端。</div>
      </div>
    </section>

    <!-- 接口清单 -->
    <section class="card card-pad stack" style="gap: 14px">
      <button class="between" style="width: 100%; text-align: left" @click="showEndpoints = !showEndpoints">
        <h2 class="sec-title"><Ico name="link" :size="16" :stroke="2" /> 对接的接口</h2>
        <Ico :name="showEndpoints ? 'chevronDown' : 'chevron'" :size="16" style="color: var(--ink-400)" />
      </button>

      <div v-if="showEndpoints" class="eps">
        <div v-for="e in ENDPOINTS" :key="e.p + e.m" class="ep">
          <span class="ep-m" :class="`ep-m-${e.m.toLowerCase()}`">{{ e.m }}</span>
          <div class="grow" style="min-width: 0">
            <p class="ep-p mono ellipsis">{{ e.p }}</p>
            <p class="text-xs muted">{{ e.d }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 本地数据 -->
    <section class="card card-pad stack" style="gap: 14px">
      <h2 class="sec-title"><Ico name="layers" :size="16" :stroke="2" /> 本地数据</h2>
      <div class="kv">
        <div class="kv-row">
          <span class="kv-k">点赞记录</span>
          <span class="kv-v mono">{{ likeCount }} 条</span>
        </div>
        <div class="kv-row">
          <span class="kv-k">主题偏好</span>
          <span class="kv-v">{{ state.accent === 'rose' ? '玫粉为主' : '薄荷蓝为主' }}</span>
        </div>
        <div class="kv-row">
          <span class="kv-k">存储位置</span>
          <span class="kv-v mono">localStorage · lc.*</span>
        </div>
      </div>
      <p class="hint">
        点赞是纯前端交互（后端没有点赞接口），只存在你本机。其余偏好同理。
      </p>
      <button class="btn btn-ghost" style="align-self: flex-start" @click="clearLocal">
        <Ico name="trash" :size="15" /> 清空本地数据
      </button>
    </section>

    <footer class="foot">
      <p>
        <b class="grad-text">生活圈 Life Circle</b> · 前端 Vue 3 + Vite · 后端 Spring Boot 3.2 +
        MyBatis-Plus + MySQL + Redis
      </p>
      <p class="text-xs">
        玫粉色 #FF5C8D 与薄荷蓝 #33C6B7 为全局主色，可在上方一键互换角色。
      </p>
    </footer>

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
}

.id-card {
  background: linear-gradient(150deg, #fff 0%, var(--brand-50) 55%, var(--accent-50) 100%);
}
.avatar-lg {
  width: 58px;
  height: 58px;
  border-radius: var(--r-lg);
  font-size: 21px;
}
.id-name {
  font-size: 19px;
  font-weight: 800;
  color: var(--ink-900);
  letter-spacing: -0.015em;
}

.sec-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: var(--ink-900);
}
.sec-title :deep(.ico) {
  color: var(--brand-500);
}

.themes {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
@media (max-width: 560px) {
  .themes {
    grid-template-columns: 1fr;
  }
}
.theme {
  position: relative;
  padding: 0;
  border-radius: var(--r-md);
  overflow: hidden;
  border: 2px solid var(--line);
  background: var(--surface);
  text-align: left;
  transition: all 0.24s var(--ease);
}
.theme:hover {
  border-color: var(--brand-200);
}
.theme.is-on {
  border-color: var(--brand-500);
  box-shadow: 0 0 0 4px var(--brand-100);
}
.theme-bar {
  display: flex;
  gap: 5px;
  padding: 14px;
}
.swatch {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.85);
  box-shadow: 0 2px 6px rgba(20, 15, 26, 0.16);
}
.theme-meta {
  display: flex;
  flex-direction: column;
  gap: 1px;
  padding: 0 14px 14px;
}
.theme-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--ink-900);
}
.theme-tick {
  position: absolute;
  right: 10px;
  top: 10px;
  width: 22px;
  height: 22px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: var(--grad-brand);
  color: #fff;
  box-shadow: 0 4px 10px -4px var(--brand-shadow);
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
  padding: 11px 15px;
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

.eps {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.ep {
  display: flex;
  align-items: flex-start;
  gap: 11px;
  padding: 11px 13px;
  border-radius: var(--r-sm);
  background: var(--surface-2);
  border: 1px solid var(--line);
}
.ep-m {
  flex: none;
  padding: 2px 8px;
  border-radius: 6px;
  font-family: var(--font-num);
  font-size: 10.5px;
  font-weight: 700;
  letter-spacing: 0.04em;
}
.ep-m-get {
  background: var(--accent-100);
  color: var(--accent-700);
}
.ep-m-post {
  background: var(--brand-100);
  color: var(--brand-700);
}
.ep-p {
  font-size: 12.5px;
  font-weight: 600;
  color: var(--ink-800);
}

.foot {
  padding: 20px 4px 8px;
  text-align: center;
  font-size: 12.5px;
  color: var(--ink-400);
  line-height: 1.8;
}

.spin {
  animation: spin 0.9s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
