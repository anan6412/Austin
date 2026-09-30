<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Ico from './components/Ico.vue'
import ToastHost from './components/ToastHost.vue'
import { state } from './store/app'

const route = useRoute()
const router = useRouter()

const NAV = [
  { path: '/', label: '附近', desc: '附近动态', icon: 'compass' },
  { path: '/publish', label: '发布', desc: '发布动态', icon: 'pen' },
  { path: '/shops', label: '店铺', desc: '附近店铺', icon: 'store' },
  { path: '/mine', label: '我的', desc: '设置', icon: 'user' }
]

const current = computed(() => route.path)

const backend = computed(() => {
  const b = state.backend
  if (!b.checked) return { cls: 'idle', text: '未检测', dot: 'dot-off' }
  if (b.online) return { cls: 'ok', text: '服务正常', dot: 'dot-live' }
  return { cls: 'off', text: '演示数据', dot: 'dot-off' }
})

function toggleAccent() {
  state.accent = state.accent === 'rose' ? 'mint' : 'rose'
}
</script>

<template>
  <div class="shell">
    <div class="app-bg" />

    <!-- 桌面侧边栏 -->
    <aside class="rail">
      <div class="brand">
        <span class="logo">
          <svg viewBox="0 0 64 64" width="30" height="30" aria-hidden="true">
            <defs>
              <linearGradient id="lg" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0" stop-color="var(--brand-500)" />
                <stop offset="1" stop-color="var(--accent-500)" />
              </linearGradient>
            </defs>
            <rect width="64" height="64" rx="18" fill="url(#lg)" />
            <circle cx="32" cy="30" r="9" fill="#fff" />
            <path d="M32 56l-9-15h18z" fill="#fff" />
          </svg>
        </span>
        <span class="brand-txt">
          <b>生活圈</b>
          <i>Life Circle</i>
        </span>
      </div>

      <nav class="nav">
        <button
          v-for="n in NAV"
          :key="n.path"
          class="nav-item"
          :class="{ 'is-on': current === n.path }"
          @click="router.push(n.path)"
        >
          <span class="nav-ico"><Ico :name="n.icon" :size="18" /></span>
          <span class="nav-txt">
            <b>{{ n.label }}</b>
            <i>{{ n.desc }}</i>
          </span>
          <span class="nav-bar" />
        </button>
      </nav>

      <div class="rail-foot">
        <button class="status" :class="`status-${backend.cls}`" @click="router.push('/mine')">
          <span class="dot" :class="backend.dot" />
          <span class="status-txt">
            <b>{{ backend.text }}</b>
            <i class="mono">{{ state.lng.toFixed(3) }}, {{ state.lat.toFixed(3) }}</i>
          </span>
        </button>
        <button class="accent-btn" @click="toggleAccent">
          <Ico name="palette" :size="15" />
          切换为{{ state.accent === 'rose' ? '薄荷蓝' : '玫粉' }}主色
        </button>
        <p class="ver">Vue 3 · Vite · Spring Boot 3.2</p>
      </div>
    </aside>

    <!-- 主区域 -->
    <div class="main">
      <!-- 移动端顶栏 -->
      <header class="topbar">
        <div class="row" style="gap: 9px; min-width: 0">
          <span class="logo logo-sm">
            <svg viewBox="0 0 64 64" width="26" height="26" aria-hidden="true">
              <defs>
                <linearGradient id="lg2" x1="0" y1="0" x2="1" y2="1">
                  <stop offset="0" stop-color="var(--brand-500)" />
                  <stop offset="1" stop-color="var(--accent-500)" />
                </linearGradient>
              </defs>
              <rect width="64" height="64" rx="18" fill="url(#lg2)" />
              <circle cx="32" cy="30" r="9" fill="#fff" />
              <path d="M32 56l-9-15h18z" fill="#fff" />
            </svg>
          </span>
          <span class="brand-txt">
            <b>生活圈</b>
          </span>
        </div>
        <div class="row" style="gap: 8px">
          <span class="tag" :class="backend.cls === 'ok' ? 'tag-ok' : 'tag-neutral'" style="gap: 5px">
            <span class="dot" :class="backend.dot" />
            {{ backend.text }}
          </span>
          <button class="btn btn-ghost btn-icon" title="切换主色" @click="toggleAccent">
            <Ico name="palette" :size="17" />
          </button>
        </div>
      </header>

      <main class="content">
        <div class="inner">
          <router-view v-slot="{ Component }">
            <transition name="fade-up" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </main>

      <!-- 移动底栏 -->
      <nav class="tabbar">
        <button
          v-for="n in NAV"
          :key="n.path"
          class="tab"
          :class="{ 'is-on': current === n.path }"
          @click="router.push(n.path)"
        >
          <span class="tab-ico"><Ico :name="n.icon" :size="20" /></span>
          <span class="tab-lb">{{ n.label }}</span>
        </button>
      </nav>
    </div>

    <ToastHost />
  </div>
</template>

<style scoped>
.shell {
  position: relative;
  min-height: 100vh;
  display: flex;
  z-index: 1;
}

/* ---------------- 桌面侧栏 ---------------- */
.rail {
  display: none;
}
@media (min-width: 1024px) {
  .rail {
    position: fixed;
    inset: 0 auto 0 0;
    width: var(--rail-w);
    display: flex;
    flex-direction: column;
    gap: 22px;
    padding: 24px 18px;
    background: rgba(255, 255, 255, 0.72);
    border-right: 1px solid var(--line);
    backdrop-filter: blur(18px);
    z-index: 10;
  }
}

.brand {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 0 6px;
}
.logo {
  display: grid;
  place-items: center;
  border-radius: 12px;
  box-shadow: 0 8px 18px -8px var(--brand-shadow);
}
.brand-txt {
  display: flex;
  flex-direction: column;
  line-height: 1.24;
}
.brand-txt b {
  font-size: 16px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--ink-900);
}
.brand-txt i {
  font-style: normal;
  font-size: 10.5px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--ink-300);
}

.nav {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 13px;
  border-radius: var(--r-md);
  text-align: left;
  transition: all 0.24s var(--ease);
}
.nav-item:hover {
  background: var(--brand-50);
}
.nav-item.is-on {
  background: var(--grad-soft);
  border: 1px solid var(--brand-100);
}
.nav-ico {
  width: 34px;
  height: 34px;
  flex: none;
  display: grid;
  place-items: center;
  border-radius: var(--r-sm);
  color: var(--ink-400);
  background: var(--line);
  transition: all 0.24s var(--ease);
}
.nav-item.is-on .nav-ico {
  color: #fff;
  background: var(--grad-brand);
  box-shadow: 0 8px 16px -8px var(--brand-shadow);
}
.nav-txt {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
  min-width: 0;
}
.nav-txt b {
  font-size: 14px;
  font-weight: 700;
  color: var(--ink-700);
}
.nav-txt i {
  font-style: normal;
  font-size: 11px;
  color: var(--ink-300);
}
.nav-item.is-on .nav-txt b {
  color: var(--brand-700);
}
.nav-bar {
  position: absolute;
  right: 10px;
  width: 3px;
  height: 0;
  border-radius: 3px;
  background: var(--grad-brand);
  transition: height 0.26s var(--ease);
}
.nav-item.is-on .nav-bar {
  height: 20px;
}

.rail-foot {
  margin-top: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.status {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 13px;
  border-radius: var(--r-md);
  border: 1px solid var(--line);
  background: var(--surface);
  text-align: left;
  transition: all 0.22s var(--ease);
}
.status:hover {
  border-color: var(--brand-200);
}
.status-txt {
  display: flex;
  flex-direction: column;
  line-height: 1.34;
  min-width: 0;
}
.status-txt b {
  font-size: 12.5px;
  font-weight: 700;
  color: var(--ink-700);
}
.status-txt i {
  font-style: normal;
  font-size: 10.5px;
  color: var(--ink-300);
}

.accent-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  height: 38px;
  border-radius: var(--r-md);
  font-size: 12.5px;
  font-weight: 700;
  color: var(--brand-700);
  background: var(--brand-50);
  border: 1px dashed var(--brand-200);
  transition: all 0.22s var(--ease);
}
.accent-btn:hover {
  background: var(--brand-100);
  border-style: solid;
}
.ver {
  text-align: center;
  font-size: 10.5px;
  color: var(--ink-300);
  letter-spacing: 0.02em;
}

/* ---------------- 主区域 ---------------- */
.main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
@media (min-width: 1024px) {
  .main {
    margin-left: var(--rail-w);
  }
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  height: var(--top-h);
  padding: 0 16px;
  background: rgba(255, 255, 255, 0.8);
  border-bottom: 1px solid var(--line);
  backdrop-filter: blur(18px);
}
@media (min-width: 1024px) {
  .topbar {
    display: none;
  }
}
.logo-sm {
  border-radius: 9px;
}

.content {
  flex: 1;
  padding: 18px 16px calc(var(--tab-h) + 28px);
}
@media (min-width: 1024px) {
  .content {
    padding: 30px 34px 48px;
  }
}
.inner {
  width: 100%;
  max-width: var(--maxw);
  margin: 0 auto;
}

/* ---------------- 移动底栏 ---------------- */
.tabbar {
  position: fixed;
  inset: auto 0 0 0;
  z-index: 30;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 2px;
  padding: 6px 8px calc(6px + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.9);
  border-top: 1px solid var(--line);
  backdrop-filter: blur(20px);
}
@media (min-width: 1024px) {
  .tabbar {
    display: none;
  }
}
.tab {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 6px 0;
  border-radius: var(--r-md);
  color: var(--ink-400);
  transition: all 0.22s var(--ease);
}
.tab.is-on {
  color: var(--brand-600);
}
.tab-ico {
  display: grid;
  place-items: center;
  width: 34px;
  height: 26px;
  border-radius: var(--r-full);
  transition: all 0.26s var(--ease);
}
.tab.is-on .tab-ico {
  background: var(--brand-100);
}
.tab-lb {
  font-size: 10.5px;
  font-weight: 600;
  letter-spacing: 0.01em;
}

/* ---------------- 路由过渡 ---------------- */
.fade-up-enter-active {
  transition: all 0.3s var(--ease-out);
}
.fade-up-leave-active {
  transition: all 0.16s var(--ease);
}
.fade-up-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
.fade-up-leave-to {
  opacity: 0;
}
</style>
