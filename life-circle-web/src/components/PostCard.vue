<script setup>
import { computed, ref } from 'vue'
import Ico from './Ico.vue'
import { formatDistance, formatRelativeTime, gradientOf, initials } from '../utils/format'
import { isLiked, toggleLike } from '../store/app'
import { toast } from '../composables/useToast'

const props = defineProps({
  post: { type: Object, required: true },
  me: { type: String, default: '' }
})
const emit = defineEmits(['preview', 'locate'])

const grad = computed(() => gradientOf(props.post.userId))
const initial = computed(() => initials(props.post.userId))
const isMine = computed(() => props.me && props.post.userId === props.me)

const shown = computed(() => (props.post.images || []).slice(0, 9))
const extra = computed(() => Math.max(0, (props.post.images || []).length - 9))

const failed = ref({})
function onImgError(i) {
  failed.value = { ...failed.value, [i]: true }
}

const liked = computed(() => isLiked(props.post.id))
function onLike() {
  toggleLike(props.post.id)
  toast(liked.value ? '已点赞' : '已取消点赞', 'info', 1600)
}

async function copyCoord() {
  const t = `${props.post.lng},${props.post.lat}`
  try {
    await navigator.clipboard.writeText(t)
    toast.success('坐标已复制：' + t)
  } catch {
    toast.error('复制失败，请手动选择坐标文本')
  }
}

function navigate() {
  const url = `https://uri.amap.com/marker?position=${props.post.lng},${props.post.lat}&name=${encodeURIComponent(
    props.post.address || '帖子位置'
  )}&coordinate=gaode&callnative=0`
  window.open(url, '_blank', 'noopener')
}
</script>

<template>
  <article class="card card-pad card-hover post">
    <header class="row" style="gap: 12px">
      <div class="avatar" :style="{ background: grad }">{{ initial }}</div>
      <div class="grow">
        <div class="row" style="gap: 7px">
          <span class="strong ellipsis" style="color: var(--ink-900); max-width: 180px">
            {{ post.userId }}
          </span>
          <span v-if="isMine" class="tag tag-brand">我</span>
        </div>
        <div class="row text-xs muted" style="gap: 6px; margin-top: 2px">
          <Ico name="clock" :size="12.5" />
          <span>{{ formatRelativeTime(post.createdAt) }}</span>
        </div>
      </div>
      <span v-if="post.distance != null" class="tag tag-accent dist">
        <Ico name="navigation" :size="11.5" :stroke="2" />
        {{ formatDistance(post.distance) }}
      </span>
    </header>

    <p v-if="post.text" class="body-text clamp-6">{{ post.text }}</p>

    <div v-if="shown.length" class="grid9" :class="`grid9-${shown.length}`" style="margin-top: 14px">
      <div
        v-for="(img, i) in shown"
        :key="i"
        class="grid9-cell"
        @click="emit('preview', { images: post.images, index: i })"
      >
        <img
          v-if="!failed[i]"
          :src="img"
          :alt="`图片 ${i + 1}`"
          loading="lazy"
          @error="onImgError(i)"
        />
        <div v-else class="img-fallback">
          <Ico name="image" :size="22" />
        </div>
        <span v-if="i === 8 && extra" class="grid9-more">+{{ extra }}</span>
      </div>
    </div>

    <footer class="row" style="gap: 10px; margin-top: 16px; padding-top: 13px; border-top: 1px dashed var(--line)">
      <button class="loc grow" type="button" @click="emit('locate', post)">
        <Ico name="pin" :size="14.5" />
        <span class="ellipsis">{{ post.address || '未知地址' }}</span>
      </button>

      <button class="act" :class="{ 'is-on': liked }" type="button" @click="onLike">
        <Ico name="heart" :size="15.5" :fill="liked ? 'currentColor' : 'none'" />
        <span v-if="liked">已赞</span>
      </button>
      <button class="act" type="button" title="复制坐标" @click="copyCoord">
        <Ico name="link" :size="15" />
      </button>
      <button class="act" type="button" title="在高德地图中查看" @click="navigate">
        <Ico name="navigation" :size="15" />
      </button>
    </footer>
  </article>
</template>

<style scoped>
.post {
  position: relative;
}
.post::before {
  content: '';
  position: absolute;
  left: 0;
  top: 22px;
  bottom: 22px;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: var(--grad-brand);
  opacity: 0.85;
}

.body-text {
  margin-top: 13px;
  font-size: 15px;
  line-height: 1.78;
  color: var(--ink-700);
  white-space: pre-wrap;
  word-break: break-word;
}

.dist {
  font-family: var(--font-num);
  letter-spacing: 0.01em;
}

.img-fallback {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  color: var(--ink-300);
  background: var(--grad-soft);
}

.loc {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  padding: 6px 2px;
  font-size: 12.5px;
  color: var(--ink-400);
  text-align: left;
  transition: color 0.2s var(--ease);
}
.loc:hover {
  color: var(--brand-600);
}

.act {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 30px;
  padding: 0 11px;
  border-radius: var(--r-full);
  font-size: 12.5px;
  font-weight: 600;
  color: var(--ink-400);
  background: var(--surface-2);
  border: 1px solid var(--line);
  transition: all 0.2s var(--ease);
}
.act:hover {
  color: var(--brand-600);
  border-color: var(--brand-200);
  background: var(--brand-50);
}
.act.is-on {
  color: var(--rose-600);
  border-color: var(--rose-200);
  background: var(--rose-50);
}
</style>
