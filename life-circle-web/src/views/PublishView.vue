<script setup>
import { computed, onMounted, onBeforeUnmount, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import Ico from '../components/Ico.vue'
import LocationSheet from '../components/LocationSheet.vue'
import Sheet from '../components/Sheet.vue'
import PostCard from '../components/PostCard.vue'
import { api } from '../api'
import { mockCreatePost } from '../api/mock'
import { clearDraft, coordText, loadDraft, locate, saveDraft, state } from '../store/app'
import { fileToCompressedDataURL, humanSize, byteSize } from '../utils/format'
import { toast } from '../composables/useToast'

const router = useRouter()

const MAX_LEN = 500
const MAX_IMAGES = 9
const MAX_IMAGE_BYTES = 600 * 1024

const text = ref('')
const images = ref([])
const urlInput = ref('')
const submitting = ref(false)
const locOpen = ref(false)
const urlOpen = ref(false)
const fileInput = ref(null)
const locating = ref(false)
const errors = ref({})

const draft = loadDraft()
if (draft) {
  text.value = draft.text || ''
  images.value = draft.images || []
}

const len = computed(() => text.value.length)
const totalBytes = computed(() => images.value.reduce((n, s) => n + byteSize(s), 0))
const canSubmit = computed(() => text.value.trim().length > 0 && !submitting.value)

const previewPost = computed(() => ({
  id: 'preview',
  userId: state.userId,
  text: text.value || '这里会实时预览你的动态内容…',
  images: images.value,
  lng: state.lng,
  lat: state.lat,
  address: state.label,
  createdAt: new Date().toISOString().slice(0, 19),
  distance: 0
}))

watch(
  () => [text.value, images.value],
  () => saveDraft({ text: text.value, images: images.value }),
  { deep: true }
)

const EMOJI = ['🙂', '😍', '🌿', '☕️', '🌸', '🌇', '🍜', '🐱', '📍', '✨']

function insertEmoji(e) {
  text.value += e
}

async function onPickFiles(ev) {
  const files = Array.from(ev.target.files || [])
  ev.target.value = ''
  if (!files.length) return

  const room = MAX_IMAGES - images.value.length
  if (room <= 0) {
    toast.error(`最多 ${MAX_IMAGES} 张图片`)
    return
  }

  let added = 0
  for (const f of files.slice(0, room)) {
    try {
      const dataUrl = await fileToCompressedDataURL(f, 1080, 0.72)
      if (byteSize(dataUrl) > MAX_IMAGE_BYTES) {
        toast.error(`${f.name} 压缩后仍超过 600KB，已跳过`)
        continue
      }
      images.value.push(dataUrl)
      added++
    } catch (e) {
      toast.error(`${f.name}：${e.message}`)
    }
  }
  if (added) toast.success(`已添加 ${added} 张图片（已本地压缩，无需图床）`)
}

function addByUrl() {
  const u = urlInput.value.trim()
  if (!u) return
  if (images.value.length >= MAX_IMAGES) {
    toast.error(`最多 ${MAX_IMAGES} 张图片`)
    return
  }
  if (!/^(https?:)?\/\//i.test(u) && !u.startsWith('data:')) {
    toast.error('请填写 http(s) 开头的图片地址')
    return
  }
  images.value.push(u)
  urlInput.value = ''
  toast.success('图片已添加')
}

function removeImage(i) {
  images.value.splice(i, 1)
}

function moveImage(i, step) {
  const j = i + step
  if (j < 0 || j >= images.value.length) return
  const arr = images.value
  ;[arr[i], arr[j]] = [arr[j], arr[i]]
}

async function doLocate() {
  locating.value = true
  try {
    await locate()
    toast.success('已使用当前定位')
  } catch (e) {
    toast.error(e.message)
    locOpen.value = true
  } finally {
    locating.value = false
  }
}

function validate() {
  const e = {}
  if (!state.userId.trim()) e.userId = '请先填写用户 ID'
  if (!text.value.trim()) e.text = '正文不能为空'
  if (!isFinite(state.lng) || !isFinite(state.lat)) e.loc = '位置信息无效，请重新选择'
  errors.value = e
  return Object.keys(e).length === 0
}

async function submit() {
  if (!validate()) {
    toast.error(Object.values(errors.value)[0])
    return
  }

  submitting.value = true
  const payload = {
    userId: state.userId.trim(),
    text: text.value.trim(),
    images: images.value,
    lng: state.lng,
    lat: state.lat
  }

  try {
    const created = await api.createPost(payload)
    toast.success('发布成功，已在附近动态中')
    clearDraft()
    text.value = ''
    images.value = []
    const id = created && created.id
    router.push({ path: '/', query: id ? { highlight: id } : {} })
  } catch (e) {
    if (e.offline) {
      // 后端没起来：仍然让用户看到交互闭环，但明确说明数据没落库
      mockCreatePost(payload, { lng: state.lng, lat: state.lat })
      toast.error(e.demo ? '演示模式：内容未提交到后端' : '后端未连接，本次内容未真正保存')
      state.backend = { checked: true, online: false, message: e.message, latency: null }
    } else {
      toast.error('发布失败：' + e.message)
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (!state.userId) state.userId = 'user_' + Math.random().toString(36).slice(2, 8)
})

onBeforeUnmount(() => saveDraft({ text: text.value, images: images.value }))
</script>

<template>
  <div class="page stack" style="gap: 18px">
    <header class="head">
      <h1 class="h1">发布动态</h1>
      <p class="sub">
        内容会带上你的经纬度，后端自动做逆地理编码补全地址，并写入 Redis GEO 供附近的人查到。
      </p>
    </header>

    <div class="cols">
      <!-- 左：表单 -->
      <div class="card card-pad stack" style="gap: 20px">
        <label class="field">
          <span class="label"><Ico name="user" :size="14" /> 用户 ID <b class="req">*</b></span>
          <input v-model="state.userId" class="input" placeholder="例如：xiaoanan" maxlength="32" />
          <span class="hint">后端不做账号体系，userId 由前端自行标识，会一起落到 posts.user_id。</span>
        </label>

        <div class="field">
          <div class="between" style="margin-bottom: 8px">
            <span class="label" style="margin: 0"><Ico name="pen" :size="14" /> 正文 <b class="req">*</b></span>
            <span class="counter" :class="{ 'is-over': len > MAX_LEN }">{{ len }} / {{ MAX_LEN }}</span>
          </div>
          <textarea
            v-model="text"
            class="textarea"
            :maxlength="MAX_LEN"
            placeholder="此刻你在想什么？描述一下眼前的场景、味道或者心情…"
          />
          <div class="row wrap" style="gap: 6px; margin-top: 10px">
            <button v-for="e in EMOJI" :key="e" class="emoji" type="button" @click="insertEmoji(e)">
              {{ e }}
            </button>
          </div>
        </div>

        <!-- 图片 -->
        <div class="field">
          <div class="between" style="margin-bottom: 8px">
            <span class="label" style="margin: 0">
              <Ico name="image" :size="14" /> 图片
              <span class="tag tag-neutral">{{ images.length }} / {{ MAX_IMAGES }}</span>
            </span>
            <span v-if="images.length" class="text-xs muted mono">{{ humanSize(totalBytes) }}</span>
          </div>

          <div v-if="images.length" class="thumbs">
            <div v-for="(img, i) in images" :key="i" class="thumb">
              <img :src="img" :alt="`待发布图片 ${i + 1}`" />
              <span v-if="i === 0" class="thumb-badge">封面</span>
              <div class="thumb-ops">
                <button type="button" title="前移" :disabled="i === 0" @click="moveImage(i, -1)">
                  <Ico name="chevron" :size="13" style="transform: rotate(-90deg)" />
                </button>
                <button type="button" title="后移" :disabled="i === images.length - 1" @click="moveImage(i, 1)">
                  <Ico name="chevron" :size="13" style="transform: rotate(90deg)" />
                </button>
                <button type="button" title="移除" @click="removeImage(i)">
                  <Ico name="close" :size="13" />
                </button>
              </div>
            </div>
          </div>

          <div class="row wrap" style="gap: 10px; margin-top: 12px">
            <button
              class="btn btn-soft"
              type="button"
              :disabled="images.length >= MAX_IMAGES"
              @click="fileInput.click()"
            >
              <Ico name="camera" :size="16" /> 从本机选择
            </button>
            <button class="btn btn-ghost" type="button" @click="urlOpen = true">
              <Ico name="link" :size="15" /> 填图片链接
            </button>
            <button v-if="images.length" class="btn btn-ghost" type="button" @click="images = []">
              <Ico name="trash" :size="15" /> 清空
            </button>
          </div>          <input
            ref="fileInput"
            class="hidden-file"
            type="file"
            accept="image/*"
            multiple
            @change="onPickFiles"
          />
          <span class="hint">
            图片会在浏览器本地压缩到最长边 1080px 再转成 dataURL 提交，所以不需要自备图床；后端
            <span class="mono">posts.images</span> 存的是这段 JSON 字符串。
          </span>
        </div>

        <!-- 位置 -->
        <div class="field">
          <span class="label"><Ico name="pin" :size="14" /> 位置 <b class="req">*</b></span>
          <div class="loc-box" :class="{ 'is-bad': errors.loc }">
            <div class="grow" style="min-width: 0">
              <p class="loc-title ellipsis">{{ state.label }}</p>
              <p class="mono loc-coord">{{ coordText }}</p>
            </div>
            <button class="btn btn-ghost btn-sm" type="button" :disabled="locating" @click="doLocate">
              <Ico name="target" :size="14" />
              {{ locating ? '定位中…' : '自动定位' }}
            </button>
            <button class="btn btn-ghost btn-sm" type="button" @click="locOpen = true">
              <Ico name="sliders" :size="14" /> 手动
            </button>
          </div>
        </div>

        <div class="row" style="gap: 10px">
          <button class="btn btn-primary btn-lg grow" :disabled="!canSubmit" @click="submit">
            <Ico name="zap" :size="17" />
            {{ submitting ? '正在发布…' : '发布动态' }}
          </button>
          <button class="btn btn-ghost btn-lg" type="button" @click="router.back()">返回</button>
        </div>

        <p class="api-line">
          <Ico name="server" :size="13" />
          <span class="mono">POST /api/posts</span>
          <span class="muted">· 请求体 {{ '{' }} userId, text, images[], lng, lat {{ '}' }}</span>
        </p>
      </div>

      <!-- 右：实时预览 -->
      <aside class="stack" style="gap: 12px">
        <div class="between">
          <h2 class="sec-title"><Ico name="sparkle" :size="16" :stroke="2" /> 实时预览</h2>
          <span class="tag tag-brand">就是这么展示</span>
        </div>
        <PostCard :post="previewPost" :me="state.userId" />
        <div class="card card-pad tips">
          <p class="label" style="margin-bottom: 10px"><Ico name="info" :size="14" /> 发布后会发生什么</p>
          <ol class="steps">
            <li><span>1</span>后端调用高德逆地理编码，把经纬度换成可读地址</li>
            <li><span>2</span>帖子写入 MySQL 的 <span class="mono">posts</span> 表</li>
            <li><span>3</span>坐标写入 Redis GEO，附近查询走这一层</li>
            <li><span>4</span>清理 <span class="mono">post:nearby</span> 缓存，别人立刻能刷到</li>
          </ol>
        </div>
      </aside>
    </div>

    <!-- 图片链接弹层 -->
    <Sheet v-model="urlOpen" title="用图片链接添加">
      <div class="stack" style="gap: 14px">
        <label class="field">
          <span class="label">图片地址</span>
          <input
            v-model="urlInput"
            class="input input-mono"
            placeholder="https://example.com/photo.jpg"
            @keyup.enter="addByUrl"
          />
          <span class="hint">支持 http / https，或 data:image/... 的 base64 字符串。</span>
        </label>
        <button class="btn btn-primary btn-block btn-lg" @click="addByUrl">
          <Ico name="plus" :size="16" /> 添加到图片列表
        </button>
      </div>
    </Sheet>

    <LocationSheet v-model="locOpen" />
  </div>
</template>

<style scoped>
.head .h1 {
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -0.025em;
  color: var(--ink-900);
}
.head .sub {
  margin-top: 7px;
  font-size: 13.5px;
  color: var(--ink-400);
  max-width: 640px;
  line-height: 1.7;
}

.cols {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}
@media (max-width: 960px) {
  .cols {
    grid-template-columns: 1fr;
  }
}

.counter {
  font-family: var(--font-num);
  font-size: 12px;
  color: var(--ink-400);
}
.counter.is-over {
  color: var(--danger);
}

.emoji {
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  border-radius: var(--r-sm);
  font-size: 17px;
  background: var(--surface-2);
  border: 1px solid var(--line);
  transition: all 0.2s var(--ease);
}
.emoji:hover {
  border-color: var(--brand-300);
  background: var(--brand-50);
  transform: translateY(-2px);
}

.thumbs {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 10px;
}
.thumb {
  position: relative;
  aspect-ratio: 1/1;
  border-radius: var(--r-md);
  overflow: hidden;
  border: 1px solid var(--line-strong);
  background: var(--line);
}
.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.thumb-badge {
  position: absolute;
  left: 6px;
  top: 6px;
  padding: 2px 7px;
  border-radius: var(--r-full);
  background: var(--grad-brand);
  color: #fff;
  font-size: 10.5px;
  font-weight: 700;
}
.thumb-ops {
  position: absolute;
  inset: auto 0 0 0;
  display: flex;
  justify-content: flex-end;
  gap: 2px;
  padding: 5px;
  background: linear-gradient(to top, rgba(20, 15, 26, 0.72), transparent);
}
.thumb-ops button {
  width: 24px;
  height: 24px;
  display: grid;
  place-items: center;
  border-radius: 6px;
  color: #fff;
  background: rgba(255, 255, 255, 0.16);
  transition: background 0.2s var(--ease);
}
.thumb-ops button:hover:not(:disabled) {
  background: var(--brand-500);
}
.thumb-ops button:disabled {
  opacity: 0.34;
  cursor: not-allowed;
}

.hidden-file {
  display: none;
}

.loc-box {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-radius: var(--r-md);
  background: var(--grad-soft);
  border: 1.5px solid var(--brand-100);
}
.loc-box.is-bad {
  border-color: var(--danger);
  background: rgba(242, 84, 91, 0.05);
}
.loc-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--ink-900);
}
.loc-coord {
  font-size: 11.5px;
  color: var(--ink-400);
  margin-top: 1px;
}

.api-line {
  display: flex;
  align-items: center;
  gap: 7px;
  flex-wrap: wrap;
  padding-top: 14px;
  border-top: 1px dashed var(--line);
  font-size: 12px;
  color: var(--ink-400);
}

.sec-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: var(--ink-900);
}
.sec-title :deep(.ico) {
  color: var(--brand-500);
}

.tips {
  background: linear-gradient(160deg, #fff, var(--accent-50));
  border-color: var(--accent-100);
}
.tips .label :deep(.ico) {
  color: var(--accent-600);
}
.steps {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin: 0;
  padding: 0;
  list-style: none;
}
.steps li {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-size: 13px;
  color: var(--ink-500);
  line-height: 1.62;
}
.steps li span {
  flex: none;
  width: 20px;
  height: 20px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: var(--accent-100);
  color: var(--accent-700);
  font-size: 11px;
  font-weight: 700;
  font-family: var(--font-num);
  margin-top: 1px;
}
</style>
