<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import Ico from './Ico.vue'

const props = defineProps({
  images: { type: Array, default: () => [] },
  index: { type: Number, default: 0 },
  open: { type: Boolean, default: false }
})
const emit = defineEmits(['update:open', 'update:index'])

const cur = ref(props.index)
watch(
  () => props.index,
  (v) => (cur.value = v)
)
watch(
  () => props.open,
  (o) => {
    if (o) {
      window.addEventListener('keydown', onKey)
      document.body.style.overflow = 'hidden'
    } else {
      window.removeEventListener('keydown', onKey)
      document.body.style.overflow = ''
    }
  }
)

const list = computed(() => props.images || [])
const total = computed(() => list.value.length)

function go(step) {
  if (!total.value) return
  cur.value = (cur.value + step + total.value) % total.value
}

function onKey(e) {
  if (e.key === 'Escape') emit('update:open', false)
  if (e.key === 'ArrowLeft') go(-1)
  if (e.key === 'ArrowRight') go(1)
}

onBeforeUnmount(() => {
  window.removeEventListener('keydown', onKey)
  document.body.style.overflow = ''
})
</script>

<template>
  <Teleport to="body">
    <transition name="lb">
      <div v-if="open && total" class="lb" @click.self="emit('update:open', false)">
        <div class="lb-bar">
          <span class="mono lb-count">{{ cur + 1 }} / {{ total }}</span>
          <button class="lb-btn" aria-label="关闭" @click="emit('update:open', false)">
            <Ico name="close" :size="18" />
          </button>
        </div>

        <div class="lb-stage">
          <button v-if="total > 1" class="lb-nav lb-prev" aria-label="上一张" @click="go(-1)">
            <Ico name="chevron" :size="20" style="transform: rotate(180deg)" />
          </button>
          <transition name="lb-swap" mode="out-in">
            <img :key="cur" :src="list[cur]" :alt="`图片 ${cur + 1}`" class="lb-img" />
          </transition>
          <button v-if="total > 1" class="lb-nav lb-next" aria-label="下一张" @click="go(1)">
            <Ico name="chevron" :size="20" />
          </button>
        </div>

        <div v-if="total > 1" class="lb-dots">
          <button
            v-for="(_, i) in list"
            :key="i"
            class="lb-dot"
            :class="{ 'is-on': i === cur }"
            :aria-label="`第 ${i + 1} 张`"
            @click="cur = i"
          />
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<style scoped>
.lb {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  flex-direction: column;
  background: rgba(20, 15, 26, 0.9);
  backdrop-filter: blur(10px);
  padding: 16px;
}
.lb-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex: none;
}
.lb-count {
  color: rgba(255, 255, 255, 0.72);
  font-size: 13px;
}
.lb-btn {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  background: rgba(255, 255, 255, 0.12);
  transition: background 0.2s var(--ease);
}
.lb-btn:hover {
  background: rgba(255, 255, 255, 0.22);
}

.lb-stage {
  flex: 1;
  min-height: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  position: relative;
}
.lb-img {
  max-width: min(92vw, 1000px);
  max-height: 76vh;
  border-radius: var(--r-md);
  box-shadow: 0 40px 80px -30px rgba(0, 0, 0, 0.8);
  object-fit: contain;
}

.lb-nav {
  width: 44px;
  height: 44px;
  flex: none;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  background: rgba(255, 255, 255, 0.12);
  transition: all 0.2s var(--ease);
}
.lb-nav:hover {
  background: var(--brand-500);
}

.lb-dots {
  flex: none;
  display: flex;
  justify-content: center;
  gap: 7px;
  padding-top: 14px;
}
.lb-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transition: all 0.25s var(--ease);
}
.lb-dot.is-on {
  width: 22px;
  border-radius: 999px;
  background: var(--brand-400);
}

.lb-enter-active,
.lb-leave-active {
  transition: opacity 0.28s var(--ease);
}
.lb-enter-from,
.lb-leave-to {
  opacity: 0;
}
.lb-swap-enter-active,
.lb-swap-leave-active {
  transition: all 0.24s var(--ease-out);
}
.lb-swap-enter-from {
  opacity: 0;
  transform: scale(0.97);
}
.lb-swap-leave-to {
  opacity: 0;
  transform: scale(1.01);
}

@media (max-width: 640px) {
  .lb-nav {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    z-index: 2;
  }
  .lb-prev {
    left: 0;
  }
  .lb-next {
    right: 0;
  }
}
</style>
