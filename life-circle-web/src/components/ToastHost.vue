<script setup>
import { toasts } from '../composables/useToast'
import Ico from './Ico.vue'

const meta = {
  ok: { ico: 'check' },
  err: { ico: 'close' },
  info: { ico: 'sparkle' }
}
</script>

<template>
  <transition-group name="toast-list" tag="div" class="toast-host">
    <div v-for="t in toasts" :key="t.id" class="toast" :class="`toast-${t.type}`">
      <span class="toast-ico">
        <Ico :name="meta[t.type]?.ico || 'info'" :size="12" :stroke="2.6" />
      </span>
      <span>{{ t.message }}</span>
    </div>
  </transition-group>
</template>

<style scoped>
.toast-list-enter-active,
.toast-list-leave-active {
  transition: all 0.32s var(--ease-out);
}
.toast-list-enter-from {
  opacity: 0;
  transform: translateY(14px) scale(0.94);
}
.toast-list-leave-to {
  opacity: 0;
  transform: translateY(-6px) scale(0.96);
}
</style>
