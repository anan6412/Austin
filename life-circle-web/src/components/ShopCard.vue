<script setup>
import { computed } from 'vue'
import Ico from './Ico.vue'
import { amapMarkerUrl, formatDistance, lastTypeSegment } from '../utils/format'
import { toast } from '../composables/useToast'

const props = defineProps({
  shop: { type: Object, required: true },
  index: { type: Number, default: 0 }
})

const kind = computed(() => lastTypeSegment(props.shop.type) || '周边')

// 高德返回的 location 形如 "116.397428,39.909230"
const coordParts = computed(() => String(props.shop.location || '').split(','))
const lng = computed(() => coordParts.value[0] || '')
const lat = computed(() => coordParts.value[1] || '')
const hasCoord = computed(() => coordParts.value.length === 2 && !!lng.value && !!lat.value)

const tone = computed(() => {
  const palette = ['brand', 'accent', 'brand', 'accent']
  return palette[props.index % palette.length]
})

async function copyLocation() {
  const t = props.shop.location || ''
  if (!t) {
    toast.error('这个地点没有返回坐标')
    return
  }
  try {
    await navigator.clipboard.writeText(t)
    toast.success('坐标已复制：' + t)
  } catch {
    toast.error('复制失败')
  }
}

function go() {
  if (!hasCoord.value) {
    toast.error('这个地点没有返回坐标，无法导航')
    return
  }
  window.open(amapMarkerUrl(lng.value, lat.value, props.shop.name), '_blank', 'noopener')
}
</script>

<template>
  <article class="card shop card-hover">
    <span class="rank" :class="`rank-${tone}`">{{ String(index + 1).padStart(2, '0') }}</span>

    <div class="grow" style="min-width: 0">
      <div class="row wrap" style="gap: 8px">
        <h3 class="name ellipsis">{{ shop.name || '未命名地点' }}</h3>
        <span class="tag" :class="`tag-${tone}`">{{ kind }}</span>
      </div>

      <p class="addr">
        <Ico name="pin" :size="13.5" />
        <span class="ellipsis">{{ shop.address || '地址未知' }}</span>
      </p>

      <div class="row wrap" style="gap: 8px; margin-top: 10px">
        <span class="metric">
          <span class="metric-k">直线距离</span>
          <span class="metric-v mono">{{ formatDistance(shop.distance) }}</span>
        </span>
        <span class="metric">
          <span class="metric-k">坐标</span>
          <span class="metric-v mono">{{ hasCoord ? lng + ', ' + lat : '未返回' }}</span>
        </span>
      </div>
    </div>

    <div class="ops">
      <button class="btn btn-ghost btn-sm" type="button" title="复制坐标" @click="copyLocation">
        <Ico name="link" :size="14" />
      </button>
      <button class="btn btn-primary btn-sm" type="button" :disabled="!hasCoord" @click="go">
        <Ico name="navigation" :size="14" />
        导航
      </button>
    </div>
  </article>
</template>

<style scoped>
.shop {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 17px 18px;
  border-radius: var(--r-md);
}

.rank {
  flex: none;
  width: 40px;
  height: 40px;
  display: grid;
  place-items: center;
  border-radius: var(--r-sm);
  font-family: var(--font-num);
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.02em;
}
.rank-brand {
  background: var(--brand-100);
  color: var(--brand-700);
}
.rank-accent {
  background: var(--accent-100);
  color: var(--accent-700);
}

.name {
  font-size: 15.5px;
  font-weight: 700;
  color: var(--ink-900);
  letter-spacing: -0.01em;
  max-width: 100%;
}

.addr {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 6px;
  font-size: 13px;
  color: var(--ink-400);
  min-width: 0;
}

.metric {
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
  padding: 4px 10px;
  border-radius: var(--r-xs);
  background: var(--surface-2);
  border: 1px solid var(--line);
}
.metric-k {
  font-size: 11.5px;
  color: var(--ink-400);
}
.metric-v {
  font-size: 12.5px;
  font-weight: 600;
  color: var(--ink-700);
}

.ops {
  display: flex;
  gap: 8px;
  flex: none;
}

@media (max-width: 640px) {
  .shop {
    flex-wrap: wrap;
  }
  .ops {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
