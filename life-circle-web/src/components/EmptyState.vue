<script setup>
import Ico from './Ico.vue'

defineProps({
  icon: { type: String, default: 'compass' },
  title: { type: String, default: '这里还没有内容' },
  desc: { type: String, default: '' },
  actionText: { type: String, default: '' }
})
const emit = defineEmits(['action'])
</script>

<template>
  <div class="empty">
    <div class="empty-ico">
      <Ico :name="icon" :size="30" :stroke="1.6" />
    </div>
    <p class="empty-title">{{ title }}</p>
    <p v-if="desc" class="empty-desc">{{ desc }}</p>
    <button v-if="actionText" class="btn btn-soft btn-sm" style="margin-top: 12px" @click="emit('action')">
      <Ico name="refresh" :size="15" />
      {{ actionText }}
    </button>
  </div>
</template>
