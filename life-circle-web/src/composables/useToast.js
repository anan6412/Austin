import { reactive } from 'vue'

let seq = 0
export const toasts = reactive([])

export function toast(message, type = 'info', duration = 2800) {
  const id = ++seq
  toasts.push({ id, message, type })
  if (toasts.length > 4) toasts.shift()
  setTimeout(() => {
    const i = toasts.findIndex((t) => t.id === id)
    if (i > -1) toasts.splice(i, 1)
  }, duration)
  return id
}

toast.success = (m, d) => toast(m, 'ok', d)
toast.error = (m, d) => toast(m, 'err', d ?? 3600)
toast.info = (m, d) => toast(m, 'info', d)
