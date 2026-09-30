import axios from 'axios'
import { state } from '../store/app'

/** 构建产物里可写死后端地址；开发期留空走 vite 代理 */
const ENV_BASE = import.meta.env.VITE_API_BASE || ''

export function resolveBase() {
  return state.apiBase || ENV_BASE
}

const client = axios.create({ timeout: 12000 })

client.interceptors.request.use((cfg) => {
  // 手动开启演示模式时，直接短路，不发出任何真实请求
  if (state.demo) {
    const e = new Error('已开启演示模式')
    e.offline = true
    e.demo = true
    return Promise.reject(e)
  }
  cfg.baseURL = resolveBase()
  return cfg
})

/**
 * 统一解包后端 Result 结构：{ code, msg, data }
 * 网络层失败会抛出带 offline=true 的错误，方便上层降级到演示数据。
 */
client.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 200) return body.data
      const err = new Error(body.msg || `请求失败（code=${body.code}）`)
      err.code = body.code
      err.serverMsg = body.msg
      return Promise.reject(err)
    }
    return body
  },
  (error) => {
    // 由请求拦截器抛出来的（例如演示模式短路），已经是成品错误，原样往下传
    if (error && error.offline) return Promise.reject(error)

    const status = error.response ? error.response.status : null
    // 5xx 也算「没连上」：vite / nginx 这类反向代理会把后端连接失败兜成 500，
    // 这种情况下用户看到的仍然应该是「去启动后端」，而不是一条红色的接口报错。
    const offline = !error.response || (status >= 500 && status <= 599)

    let msg
    if (error.code === 'ECONNABORTED') {
      msg = '请求超时，后端响应过慢'
    } else if (error.response) {
      const d = error.response.data
      const serverMsg = d && (d.msg || d.message)
      msg = serverMsg || `后端返回 HTTP ${status}`
      if (!serverMsg && offline) {
        msg = `无法连接后端（HTTP ${status}）—— 服务可能未启动，或 MySQL / Redis 未就绪`
      }
    } else {
      msg = error.message || '网络异常，无法连接到后端服务'
    }

    const e = new Error(msg)
    e.offline = offline
    e.status = status
    return Promise.reject(e)
  }
)

export default client
