<script setup>
import { computed } from 'vue'
import Ico from './Ico.vue'
import { relativeMeters } from '../api/mock'
import { formatDistance } from '../utils/format'

/**
 * 自绘「附近雷达」——把帖子 / 店铺按真实相对方位画在极坐标上。
 * 不依赖任何地图 JS API 或 Key，离线也能看。
 */
const props = defineProps({
  center: { type: Object, required: true },
  radius: { type: Number, default: 5000 },
  points: { type: Array, default: () => [] },
  height: { type: Number, default: 320 }
})

const R = 132
const C = 160

const plotted = computed(() =>
  props.points
    .filter((p) => p.lng != null && p.lat != null)
    .map((p, i) => {
      const { dx, dy } = relativeMeters(props.center, p.lng, p.lat)
      const dist = Math.hypot(dx, dy)
      const scale = Math.min(1, dist / props.radius)
      const ang = Math.atan2(dy, dx)
      return {
        ...p,
        i,
        dist,
        x: C + Math.cos(ang) * R * scale,
        y: C - Math.sin(ang) * R * scale,
        rings: [0.25, 0.5, 0.75, 1]
      }
    })
)

const rings = [0.25, 0.5, 0.75, 1]

const postCount = computed(() => props.points.filter((p) => p.kind === 'post').length)
const shopCount = computed(() => props.points.filter((p) => p.kind === 'shop').length)
const nearest = computed(() => {
  const list = plotted.value
  if (!list.length) return null
  return list.reduce((a, b) => (a.dist < b.dist ? a : b))
})
</script>

<template>
  <div class="radar card card-pad">
    <div class="between" style="gap: 12px; margin-bottom: 4px">
      <div>
        <h3 class="title">
          <Ico name="compass" :size="16" :stroke="2" />
          附近雷达
        </h3>
        <p class="text-xs muted" style="margin-top: 3px">
          以你为圆心，半径 {{ formatDistance(radius) }} 内的分布
        </p>
      </div>
      <div class="legend">
        <span class="lg"><i class="d d-post" />帖子 {{ postCount }}</span>
        <span class="lg"><i class="d d-shop" />店铺 {{ shopCount }}</span>
      </div>
    </div>

    <div class="wrap-chart" :style="{ height: `${height}px` }">
      <svg viewBox="0 0 320 320" class="chart">
        <defs>
          <radialGradient id="rf" cx="50%" cy="50%" r="50%">
            <stop offset="0%" stop-color="var(--brand-200)" stop-opacity=".55" />
            <stop offset="100%" stop-color="var(--brand-50)" stop-opacity=".12" />
          </radialGradient>
          <linearGradient id="rsweep" x1="0" y1="0" x2="1" y2="0">
            <stop offset="0%" stop-color="var(--brand-400)" stop-opacity=".38" />
            <stop offset="100%" stop-color="var(--brand-400)" stop-opacity="0" />
          </linearGradient>
        </defs>

        <circle :cx="C" :cy="C" :r="R" fill="url(#rf)" />

        <!-- 距离环 -->
        <circle
          v-for="k in rings"
          :key="k"
          :cx="C"
          :cy="C"
          :r="R * k"
          fill="none"
          stroke="var(--line-strong)"
          :stroke-dasharray="k === 1 ? 'none' : '3 5'"
          stroke-width="1"
        />
        <!-- 十字轴 -->
        <line :x1="C - R" :y1="C" :x2="C + R" :y2="C" stroke="var(--line)" stroke-width="1" />
        <line :x1="C" :y1="C - R" :x2="C" :y2="C + R" stroke="var(--line)" stroke-width="1" />

        <!-- 环标注 -->
        <text :x="C + 6" :y="C - R + 13" class="ring-t">{{ formatDistance(radius / 4) }}</text>
        <text :x="C + 6" :y="C - R / 2 + 13" class="ring-t">{{ formatDistance(radius / 2) }}</text>

        <!-- 扫描 -->
        <g class="sweep" :style="{ transformOrigin: `${C}px ${C}px` }">
          <path
            :d="`M${C} ${C} L${C + R} ${C - 46} A ${R} ${R} 0 0 1 ${C + R} ${C + 46} Z`"
            fill="url(#rsweep)"
          />
        </g>

        <!-- 数据点 -->
        <g v-for="p in plotted" :key="p.i">
          <title>{{ p.label }} · {{ formatDistance(p.dist) }}</title>
          <circle
            :cx="p.x"
            :cy="p.y"
            r="9"
            :fill="p.kind === 'post' ? 'var(--rose-300)' : 'var(--mint-300)'"
            opacity=".26"
          />
          <circle
            :cx="p.x"
            :cy="p.y"
            r="3.6"
            :fill="p.kind === 'post' ? 'var(--rose-500)' : 'var(--mint-500)'"
            stroke="#fff"
            stroke-width="1.4"
          />
        </g>

        <!-- 圆心：你 -->
        <circle :cx="C" :cy="C" r="11" fill="#fff" stroke="var(--brand-300)" stroke-width="1.4" />
        <circle :cx="C" :cy="C" r="4.2" fill="var(--brand-500)" />
        <text :x="C" :y="C + 26" class="me-t">你在这里</text>

        <text :x="C" y="20" class="axis-t">北</text>
        <text :x="C" :y="312" class="axis-t">南</text>
        <text x="10" :y="C + 4" class="axis-t">西</text>
        <text x="300" :y="C + 4" class="axis-t">东</text>
      </svg>
    </div>

    <p v-if="nearest" class="text-xs muted center" style="margin-top: 6px">
      最近的一个点：
      <b style="color: var(--brand-600)">{{ nearest.label }}</b>
      ，距离 {{ formatDistance(nearest.dist) }}
    </p>
    <p v-else class="text-xs muted center" style="margin-top: 6px">
      当前半径内没有点位，试试放大搜索范围
    </p>
  </div>
</template>

<style scoped>
.radar {
  overflow: hidden;
}

.title {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 15px;
  font-weight: 700;
  color: var(--ink-900);
}
.title :deep(.ico) {
  color: var(--brand-500);
}

.legend {
  display: flex;
  gap: 12px;
  flex: none;
}
.lg {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  color: var(--ink-500);
}
.d {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.d-post {
  background: var(--rose-500);
}
.d-shop {
  background: var(--mint-500);
}

.wrap-chart {
  margin: 4px 0;
}
.chart {
  width: 100%;
  height: 100%;
  display: block;
}

.ring-t,
.axis-t,
.me-t {
  font-family: var(--font-num);
  font-size: 9px;
  fill: var(--ink-300);
  text-anchor: middle;
}
.axis-t {
  font-size: 10px;
  fill: var(--ink-400);
  letter-spacing: 0.08em;
}
.me-t {
  font-size: 10px;
  fill: var(--brand-600);
  font-weight: 600;
}

.sweep {
  animation: sweep 5.5s linear infinite;
}
@keyframes sweep {
  to {
    transform: rotate(360deg);
  }
}
</style>
