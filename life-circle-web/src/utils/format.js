/** 通用格式化工具 */

/** 距离（米）→ 人类可读；输入的可能是数字或后端返回的字符串 */
export function formatDistance(v) {
  const m = Number(v)
  if (!isFinite(m) || m < 0) return '—'
  if (m < 1000) return `${Math.round(m)} m`
  if (m < 10000) return `${(m / 1000).toFixed(2)} km`
  return `${(m / 1000).toFixed(1)} km`
}

/** ISO 字符串 / Date → 相对时间 */
export function formatRelativeTime(input) {
  if (!input) return '—'
  const d = input instanceof Date ? input : new Date(String(input).replace(' ', 'T'))
  if (isNaN(d.getTime())) return String(input)
  const diff = Date.now() - d.getTime()
  const sec = Math.floor(diff / 1000)
  if (sec < 60) return '刚刚'
  const min = Math.floor(sec / 60)
  if (min < 60) return `${min} 分钟前`
  const hour = Math.floor(min / 60)
  if (hour < 24) return `${hour} 小时前`
  const day = Math.floor(hour / 24)
  if (day < 7) return `${day} 天前`
  const y = d.getFullYear()
  const nowY = new Date().getFullYear()
  const md = `${d.getMonth() + 1} 月 ${d.getDate()} 日`
  return y === nowY ? md : `${y} 年 ${md}`
}

/** 格式化完整时间 */
export function formatDateTime(input) {
  if (!input) return '—'
  const d = input instanceof Date ? input : new Date(String(input).replace(' ', 'T'))
  if (isNaN(d.getTime())) return String(input)
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(
    d.getMinutes()
  )}`
}

/** 坐标 → 固定 6 位 */
export function formatCoord(v) {
  const n = Number(v)
  return isFinite(n) ? n.toFixed(6) : '—'
}

/** 取名字首字母做头像文字 */
export function initials(name) {
  const s = String(name || '?').trim()
  if (!s) return '?'
  // 中文取末字，英文取首字母
  if (/[\u4e00-\u9fa5]/.test(s)) return s.slice(-1)
  const parts = s.split(/[\s_\-.]+/).filter(Boolean)
  if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase()
  return s.slice(0, 2).toUpperCase()
}

/** 由任意字符串派生一个稳定的玫粉/薄荷蓝渐变色对 */
export function gradientOf(seed) {
  const palette = [
    ['#FF5C8D', '#FFA8C4'],
    ['#33C6B7', '#7FE3DA'],
    ['#FF85AC', '#55D6C9'],
    ['#EE3E75', '#FFCADC'],
    ['#22A99C', '#ABEEE8'],
    ['#FF5C8D', '#33C6B7'],
    ['#C92B5B', '#FFA8C4'],
    ['#17867C', '#7FE3DA']
  ]
  let h = 0
  const s = String(seed || '')
  for (let i = 0; i < s.length; i++) h = (h * 31 + s.charCodeAt(i)) >>> 0
  const [a, b] = palette[h % palette.length]
  return `linear-gradient(135deg, ${a}, ${b})`
}

/** 高德 POI type 形如「餐饮服务;咖啡厅;星巴克」，取最细一级 */
export function lastTypeSegment(type) {
  if (!type) return ''
  const segs = String(type).split(';').filter(Boolean)
  return segs.length ? segs[segs.length - 1] : ''
}

/**
 * 生成一张「像照片」的抽象渐变图（data URI）。
 * 演示数据用它当配图，好处是完全离线、一定渲染得出来，不依赖外网图床。
 */
export function gradientImage(seed, label = '') {
  let h = 0
  const s = String(seed || '')
  for (let i = 0; i < s.length; i++) h = (h * 33 + s.charCodeAt(i)) >>> 0

  // 玫粉 / 薄荷蓝两组基调，饱和度比纯马卡龙高一点，缩略图里才有存在感
  const palettes = [
    ['#ff9dbe', '#63d9cd', '#ffe3ee'],
    ['#4ed3c4', '#ffb0c9', '#e6faf7'],
    ['#ff8aae', '#8ce6dc', '#fff0f5'],
    ['#57d8ca', '#ffc0d3', '#e2f8f5'],
    ['#f9749c', '#7fe0d5', '#ffe6f0'],
    ['#3fc9ba', '#ffa7c0', '#eafbf8']
  ]
  const [c1, c2, c3] = palettes[h % palettes.length]

  const rnd = (n) => ((h >> n) % 100) / 100
  const bx = 40 + rnd(3) * 220
  const by = 50 + rnd(7) * 160
  const br = 60 + rnd(11) * 60
  const hx = 120 + rnd(5) * 150
  const hy = 20 + rnd(13) * 110
  const wave = 175 + rnd(17) * 45

  const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 300 300">
<defs>
<linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
<stop offset="0" stop-color="${c1}"/><stop offset="1" stop-color="${c2}"/>
</linearGradient>
<filter id="soft" x="-40%" y="-40%" width="180%" height="180%">
<feGaussianBlur stdDeviation="22"/>
</filter>
<filter id="soft2" x="-40%" y="-40%" width="180%" height="180%">
<feGaussianBlur stdDeviation="13"/>
</filter>
<linearGradient id="veil" x1="0" y1="0" x2="0" y2="1">
<stop offset="0" stop-color="#ffffff" stop-opacity=".34"/>
<stop offset="1" stop-color="#000000" stop-opacity=".10"/>
</linearGradient>
</defs>
<rect width="300" height="300" fill="url(#bg)"/>
<circle cx="${bx.toFixed(0)}" cy="${by.toFixed(0)}" r="${br.toFixed(0)}" fill="#ffffff" opacity=".62" filter="url(#soft)"/>
<circle cx="${hx.toFixed(0)}" cy="${hy.toFixed(0)}" r="58" fill="${c3}" opacity=".82" filter="url(#soft2)"/>
<circle cx="${(300 - bx * 0.55).toFixed(0)}" cy="${(300 - by * 0.5).toFixed(0)}" r="46" fill="#ffffff" opacity=".34" filter="url(#soft2)"/>
<path d="M0 ${wave.toFixed(0)} C 70 ${(wave - 34).toFixed(0)}, 130 ${(wave + 26).toFixed(0)}, 200 ${(wave - 12).toFixed(0)} S 280 ${(wave + 8).toFixed(0)}, 300 ${(wave - 6).toFixed(0)} L300 300 L0 300Z" fill="#ffffff" opacity=".36"/>
<rect width="300" height="300" fill="url(#veil)"/>
${label ? `<text x="18" y="284" font-family="system-ui,sans-serif" font-size="14" fill="#ffffff" opacity=".92">${label}</text>` : ''}
</svg>`
  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`
}

/** 稳定伪随机（同一 seed 结果一致） */
export function seededRandom(seed) {
  let h = 2166136261
  const s = String(seed)
  for (let i = 0; i < s.length; i++) {
    h ^= s.charCodeAt(i)
    h = Math.imul(h, 16777619)
  }
  return () => {
    h += 0x6d2b79f5
    let t = h
    t = Math.imul(t ^ (t >>> 15), t | 1)
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61)
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
}

/** 把本地图片文件压缩成 dataURL，避免用户必须自备图床 */
export function fileToCompressedDataURL(file, maxSide = 1080, quality = 0.72) {
  return new Promise((resolve, reject) => {
    if (!file.type.startsWith('image/')) {
      reject(new Error('只支持图片文件'))
      return
    }
    const reader = new FileReader()
    reader.onerror = () => reject(new Error('读取文件失败'))
    reader.onload = () => {
      const img = new Image()
      img.onerror = () => reject(new Error('图片解析失败'))
      img.onload = () => {
        let { width: w, height: h } = img
        const scale = Math.min(1, maxSide / Math.max(w, h))
        w = Math.max(1, Math.round(w * scale))
        h = Math.max(1, Math.round(h * scale))
        const canvas = document.createElement('canvas')
        canvas.width = w
        canvas.height = h
        const ctx = canvas.getContext('2d')
        ctx.fillStyle = '#ffffff'
        ctx.fillRect(0, 0, w, h)
        ctx.drawImage(img, 0, 0, w, h)
        resolve(canvas.toDataURL('image/jpeg', quality))
      }
      img.src = reader.result
    }
    reader.readAsDataURL(file)
  })
}

/** 粗略估算字符串字节数 */
export function byteSize(str) {
  const s = String(str || '')
  let bytes = 0
  for (let i = 0; i < s.length; i++) {
    const c = s.charCodeAt(i)
    if (c < 0x80) bytes += 1
    else if (c < 0x800) bytes += 2
    else if (c >= 0xd800 && c <= 0xdbff) {
      bytes += 4
      i++
    } else bytes += 3
  }
  return bytes
}

export function humanSize(bytes) {
  const b = Number(bytes)
  if (!isFinite(b) || b < 0) return '—'
  if (b < 1024) return `${b} B`
  if (b < 1024 * 1024) return `${(b / 1024).toFixed(1)} KB`
  return `${(b / 1024 / 1024).toFixed(2)} MB`
}

export function clamp(v, min, max) {
  return Math.min(max, Math.max(min, v))
}

/** 高德网页版标注链接（用于「导航」按钮） */
export function amapMarkerUrl(lng, lat, name) {
  const n = encodeURIComponent(name || '目的地')
  return `https://uri.amap.com/marker?position=${lng},${lat}&name=${n}&coordinate=gaode&callnative=0`
}
