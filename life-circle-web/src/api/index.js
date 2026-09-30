import client from './http'

/**
 * 与 life-circle-api 的接口一一对应
 *   GET  /api/hello                           健康检查
 *   GET  /api/posts/nearby?lng&lat&radius     附近帖子
 *   POST /api/posts                           创建帖子
 *   GET  /api/shops/nearby?lng&lat&radius&keyword  附近店铺
 */
export const api = {
  health() {
    return client.get('/api/hello')
  },

  nearbyPosts({ lng, lat, radius }) {
    return client.get('/api/posts/nearby', { params: { lng, lat, radius } })
  },

  createPost(payload) {
    return client.post('/api/posts', payload)
  },

  nearbyShops({ lng, lat, radius, keyword }) {
    const params = { lng, lat, radius }
    if (keyword) params.keyword = keyword
    return client.get('/api/shops/nearby', { params })
  }
}

export default api
