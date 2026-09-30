<script setup>
import { computed, ref } from 'vue'
import Sheet from './Sheet.vue'
import Ico from './Ico.vue'
import { CITY_PRESETS, coordText, locate, state, useCity } from '../store/app'
import { toast } from '../composables/useToast'
import { clamp } from '../utils/format'

const open = defineModel({ type: Boolean, default: false })

const mode = ref('gps')
const form = ref({ lng: '', lat: '', label: '' })
const locating = ref(false)

const current = computed(() => `${state.lng}, ${state.lat}`)

function syncForm() {
  form.value = {
    lng: String(state.lng),
    lat: String(state.lat),
    label: state.label
  }
}
syncForm()

async function doLocate() {
  locating.value = true
  try {
    await locate()
    toast.success('定位成功', 1800)
    syncForm()
    open.value = false
  } catch (e) {
    toast.error(e.message)
  } finally {
    locating.value = false
  }
}

function applyManual() {
  const lng = Number(form.value.lng)
  const lat = Number(form.value.lat)
  if (!isFinite(lng) || !isFinite(lat)) {
    toast.error('经纬度必须是数字')
    return
  }
  if (lng < -180 || lng > 180) {
    toast.error('经度需在 -180 ~ 180 之间')
    return
  }
  if (lat < -90 || lat > 90) {
    toast.error('纬度需在 -90 ~ 90 之间')
    return
  }
  state.lng = clamp(lng, -180, 180)
  state.lat = clamp(lat, -90, 90)
  state.label = form.value.label?.trim() || '自定义位置'
  toast.success('位置已更新')
  open.value = false
}

function pickCity(city) {
  useCity(city)
  syncForm()
  toast.success('已切换到 ' + city.name)
  open.value = false
}
</script>

<template>
  <Sheet v-model="open" title="选择位置">
    <div class="stack" style="gap: 18px">
      <!-- 当前 -->
      <div class="now">
        <div class="row" style="gap: 9px">
          <Ico name="target" :size="16" />
          <span class="strong" style="font-size: 13.5px">当前生效位置</span>
        </div>
        <p class="now-label">{{ state.label }}</p>
        <p class="mono now-coord">{{ current }}</p>
      </div>

      <div class="seg" style="align-self: flex-start">
        <button class="seg-item" :class="{ 'is-active': mode === 'gps' }" @click="mode = 'gps'">
          自动定位
        </button>
        <button class="seg-item" :class="{ 'is-active': mode === 'manual' }" @click="mode = 'manual'">
          手动输入
        </button>
      </div>

      <!-- 自动定位 -->
      <div v-if="mode === 'gps'" class="stack" style="gap: 14px">
        <button class="btn btn-primary btn-lg btn-block" :disabled="locating" @click="doLocate">
          <Ico name="target" :size="17" />
          {{ locating ? '正在定位…' : '使用浏览器定位' }}
        </button>
        <p class="hint">
          浏览器需要你授权位置权限。若在非 HTTPS 域名下打开，定位可能被拒绝，此时请改用手动输入。
        </p>
      </div>

      <!-- 手动 -->
      <div v-else class="stack" style="gap: 14px">
        <div class="two">
          <label class="field">
            <span class="label">经度 lng <b class="req">*</b></span>
            <input v-model="form.lng" class="input input-mono" placeholder="116.397428" inputmode="decimal" />
          </label>
          <label class="field">
            <span class="label">纬度 lat <b class="req">*</b></span>
            <input v-model="form.lat" class="input input-mono" placeholder="39.909230" inputmode="decimal" />
          </label>
        </div>
        <label class="field">
          <span class="label">位置别名</span>
          <input v-model="form.label" class="input" placeholder="例如：公司楼下" />
        </label>
        <button class="btn btn-primary btn-lg btn-block" @click="applyManual">
          <Ico name="check" :size="17" />
          应用这个位置
        </button>
        <p class="hint">
          小贴士：在高德地图网页版右键任意地点 →「这是哪里」即可看到经纬度，格式为
          <span class="mono">经度,纬度</span>。
        </p>
      </div>

      <!-- 常用城市 -->
      <div>
        <p class="label" style="margin-bottom: 10px">
          <Ico name="layers" :size="14" />
          常用城市
        </p>
        <div class="row wrap" style="gap: 8px">
          <button
            v-for="c in CITY_PRESETS"
            :key="c.name"
            class="chip"
            :class="{ 'is-active': state.label === c.name }"
            @click="pickCity(c)"
          >
            {{ c.name }}
          </button>
        </div>
      </div>
    </div>
  </Sheet>
</template>

<style scoped>
.now {
  padding: 15px 16px;
  border-radius: var(--r-md);
  background: var(--grad-soft);
  border: 1px solid var(--brand-100);
}
.now :deep(.ico) {
  color: var(--brand-500);
}
.now-label {
  margin-top: 8px;
  font-size: 15px;
  font-weight: 700;
  color: var(--ink-900);
}
.now-coord {
  margin-top: 2px;
  font-size: 12.5px;
  color: var(--ink-400);
  letter-spacing: 0.02em;
}

.two {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
@media (max-width: 480px) {
  .two {
    grid-template-columns: 1fr;
  }
}
</style>
