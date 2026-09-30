<script setup>
/**
 * 内联 SVG 图标集，避免引入图标库。
 * 统一 24x24 视口、stroke=currentColor，尺寸由 size 控制。
 */
const props = defineProps({
  name: { type: String, required: true },
  size: { type: [Number, String], default: 20 },
  stroke: { type: [Number, String], default: 1.8 },
  fill: { type: String, default: 'none' }
})

const ICONS = {
  compass:
    '<circle cx="12" cy="12" r="9"/><path d="M15.6 8.4l-2 5.2-5.2 2 2-5.2z"/>',
  sparkle:
    '<path d="M12 3l1.7 4.6L18 9.3l-4.3 1.7L12 15.6l-1.7-4.6L6 9.3l4.3-1.7z"/><path d="M18.5 15.5l.7 1.9 1.9.7-1.9.7-.7 1.9-.7-1.9-1.9-.7 1.9-.7z"/>',
  pen: '<path d="M4 20h4l10.5-10.5a2.1 2.1 0 0 0-3-3L5 17v3z"/><path d="M13.5 6.5l4 4"/>',
  store:
    '<path d="M4 9.5V19a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1V9.5"/><path d="M3 9.5L5 4h14l2 5.5"/><path d="M3 9.5a2.5 2.5 0 0 0 5 0 2.5 2.5 0 0 0 5 0 2.5 2.5 0 0 0 5 0 2.5 2.5 0 0 0 3-2.3"/><path d="M9 20v-4.5h6V20"/>',
  user: '<circle cx="12" cy="8" r="3.6"/><path d="M4.8 20a7.2 7.2 0 0 1 14.4 0"/>',
  heart:
    '<path d="M12 20s-7.2-4.4-7.2-9.4A4.4 4.4 0 0 1 12 8a4.4 4.4 0 0 1 7.2 2.6C19.2 15.6 12 20 12 20z"/>',
  message:
    '<path d="M20 12a7.5 7.5 0 0 1-10.9 6.7L4.5 20l1.3-4.4A7.5 7.5 0 1 1 20 12z"/>',
  pin: '<path d="M12 21s6.5-6 6.5-11a6.5 6.5 0 1 0-13 0C5.5 15 12 21 12 21z"/><circle cx="12" cy="10" r="2.4"/>',
  navigation: '<path d="M3.5 12L20.5 4l-8 16.5-2.2-6.3z"/>',
  search: '<circle cx="11" cy="11" r="6.5"/><path d="M16 16l4 4"/>',
  refresh:
    '<path d="M20 11a8 8 0 1 0-2.4 6.5"/><path d="M20 5v6h-6"/>',
  close: '<path d="M6 6l12 12M18 6L6 18"/>',
  check: '<path d="M5 13l4.2 4.2L19 7"/>',
  alert:
    '<path d="M12 4.5l8.5 14.8H3.5z"/><path d="M12 10v4"/><circle cx="12" cy="16.6" r=".9" fill="currentColor" stroke="none"/>',
  info: '<circle cx="12" cy="12" r="8.5"/><path d="M12 11v5.5"/><circle cx="12" cy="8.2" r=".9" fill="currentColor" stroke="none"/>',
  chevron: '<path d="M9.5 5.5l6.5 6.5-6.5 6.5"/>',
  chevronDown: '<path d="M5.5 9.5l6.5 6.5 6.5-6.5"/>',
  image:
    '<rect x="3.5" y="4.5" width="17" height="15" rx="2.5"/><circle cx="9" cy="10" r="1.6"/><path d="M4 17l4.6-4.6a1.8 1.8 0 0 1 2.5 0L16 17.5"/><path d="M14.5 14.5l1.6-1.6a1.8 1.8 0 0 1 2.5 0l2 2"/>',
  camera:
    '<path d="M4 8.5h2.6l1.3-2.2h8.2l1.3 2.2H20a1 1 0 0 1 1 1V18a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V9.5a1 1 0 0 1 1-1z"/><circle cx="12" cy="13.2" r="3.4"/>',
  trash:
    '<path d="M4.5 7h15"/><path d="M9.5 7V4.8h5V7"/><path d="M7 7l.9 12a1 1 0 0 0 1 .9h6.2a1 1 0 0 0 1-.9L17 7"/><path d="M10.5 11v5M13.5 11v5"/>',
  target:
    '<circle cx="12" cy="12" r="8"/><circle cx="12" cy="12" r="3"/><path d="M12 2v3M12 19v3M2 12h3M19 12h3"/>',
  sliders:
    '<path d="M4 7h9M17 7h3M4 12h3M11 12h9M4 17h9M17 17h3"/><circle cx="15" cy="7" r="2"/><circle cx="9" cy="12" r="2"/><circle cx="15" cy="17" r="2"/>',
  link: '<path d="M10.5 13.5a3.5 3.5 0 0 0 5 0l3-3a3.5 3.5 0 0 0-5-5l-1 1"/><path d="M13.5 10.5a3.5 3.5 0 0 0-5 0l-3 3a3.5 3.5 0 0 0 5 5l1-1"/>',
  clock: '<circle cx="12" cy="12" r="8.5"/><path d="M12 7.5V12l3 2"/>',
  layers:
    '<path d="M12 3.5l8 4.2-8 4.2-8-4.2z"/><path d="M4 12.5l8 4.2 8-4.2"/><path d="M4 16.5l8 4.2 8-4.2"/>',
  server:
    '<rect x="3.5" y="4" width="17" height="6.5" rx="2"/><rect x="3.5" y="13.5" width="17" height="6.5" rx="2"/><path d="M7.5 7.2h.01M7.5 16.7h.01"/>',
  wifiOff:
    '<path d="M3 4l18 16"/><path d="M8.8 12.4a5 5 0 0 1 6.4 0"/><path d="M5.6 9.2a10 10 0 0 1 4-2.2"/><path d="M14.4 7a10 10 0 0 1 4 2.2"/><path d="M12 17.6h.01"/>',
  wifi: '<path d="M4.2 9.3a12 12 0 0 1 15.6 0"/><path d="M7.4 12.6a7.6 7.6 0 0 1 9.2 0"/><path d="M10.4 15.8a3.4 3.4 0 0 1 3.2 0"/><path d="M12 19h.01"/>',
  plus: '<path d="M12 5v14M5 12h14"/>',
  minus: '<path d="M5 12h14"/>',
  grid: '<rect x="3.5" y="3.5" width="7" height="7" rx="1.6"/><rect x="13.5" y="3.5" width="7" height="7" rx="1.6"/><rect x="3.5" y="13.5" width="7" height="7" rx="1.6"/><rect x="13.5" y="13.5" width="7" height="7" rx="1.6"/>',
  flame:
    '<path d="M12 21c3.6 0 6-2.4 6-5.6 0-4-3.4-5.2-3.4-9.4-2.2 1-3.6 3-3.6 5 0 0-1.6-1-1.6-3.2C7.6 9 6 11.6 6 15.4 6 18.6 8.4 21 12 21z"/>',
  palette:
    '<path d="M12 3.5a8.5 8.5 0 0 0 0 17c1.3 0 2-.8 2-1.8s-.8-1.7-.8-2.5c0-.9.7-1.6 1.7-1.6h1.6a4.5 4.5 0 0 0 4.5-4.5c0-3.7-4-6.6-9-6.6z"/><circle cx="8" cy="10" r="1.1" fill="currentColor" stroke="none"/><circle cx="11.6" cy="7.6" r="1.1" fill="currentColor" stroke="none"/><circle cx="15.6" cy="9.4" r="1.1" fill="currentColor" stroke="none"/>',
  github:
    '<path d="M9.2 20.4c-4.2 1.2-4.2-2.1-5.9-2.6m11.8 5.2v-3.3c0-1 .1-1.3-.5-1.9 2.4-.3 4.4-1.2 4.4-5.2a4 4 0 0 0-1.1-2.8 3.7 3.7 0 0 0-.1-2.8s-1-.3-3.4 1.3a8.6 8.6 0 0 0-4.5 0C7.5 6.7 6.5 7 6.5 7a3.7 3.7 0 0 0-.1 2.8A4 4 0 0 0 5.3 12.6c0 4 2 4.9 4.4 5.2-.4.4-.5.9-.5 1.5v3.5"/>',
  filter: '<path d="M4 6h16M7 12h10M10 18h4"/>',
  expand:
    '<path d="M9 4H4v5M15 4h5v5M15 20h5v-5M9 20H4v-5"/>',
  zap: '<path d="M13.5 3.5L5.5 13.5h5.2l-.7 7 8-10h-5.2z"/>'
}

const inner = () => ICONS[props.name] || ICONS.info
</script>

<template>
  <svg
    class="ico"
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    :fill="fill"
    :stroke="fill === 'none' ? 'currentColor' : 'none'"
    :stroke-width="stroke"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
    v-html="inner()"
  />
</template>

<style scoped>
.ico {
  display: block;
  flex: none;
  overflow: visible;
}
</style>
