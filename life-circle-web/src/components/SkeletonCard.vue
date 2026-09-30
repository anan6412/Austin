<script setup>
defineProps({
  count: { type: Number, default: 3 }
})
</script>

<template>
  <div class="stack" style="gap: 16px">
    <div v-for="i in count" :key="i" class="card card-pad">
      <div class="row" style="gap: 12px">
        <div class="sk" style="width: 42px; height: 42px; border-radius: 16px" />
        <div class="grow stack" style="gap: 7px">
          <div class="sk" style="width: 96px; height: 12px" />
          <div class="sk" style="width: 140px; height: 10px" />
        </div>
        <div class="sk" style="width: 58px; height: 22px; border-radius: 999px" />
      </div>
      <div class="stack" style="gap: 8px; margin-top: 16px">
        <div class="sk" style="width: 100%; height: 11px" />
        <div class="sk" style="width: 92%; height: 11px" />
        <div class="sk" style="width: 64%; height: 11px" />
      </div>
      <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 6px; margin-top: 16px">
        <div class="sk" style="aspect-ratio: 1/1" />
        <div class="sk" style="aspect-ratio: 1/1" />
        <div class="sk" style="aspect-ratio: 1/1" />
      </div>
    </div>
  </div>
</template>
