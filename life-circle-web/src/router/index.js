import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'nearby',
    component: () => import('../views/NearbyView.vue'),
    meta: { title: '附近动态' }
  },
  {
    path: '/publish',
    name: 'publish',
    component: () => import('../views/PublishView.vue'),
    meta: { title: '发布动态' }
  },
  {
    path: '/shops',
    name: 'shops',
    component: () => import('../views/ShopsView.vue'),
    meta: { title: '附近店铺' }
  },
  {
    path: '/mine',
    name: 'mine',
    component: () => import('../views/MineView.vue'),
    meta: { title: '我的' }
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  // hash 模式：纯静态托管（含 GitHub Pages / 直接打开 dist/index.html）都不会 404
  history: createWebHashHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.afterEach((to) => {
  document.title = to.meta?.title ? `${to.meta.title} · 生活圈` : '生活圈 · Life Circle'
})

export default router
