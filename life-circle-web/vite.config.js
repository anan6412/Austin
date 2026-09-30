import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 后端默认端口：life-circle-api/src/main/resources/application.properties -> server.port=8081
const API_TARGET = process.env.VITE_API_TARGET || 'http://127.0.0.1:8081'

export default defineConfig({
  plugins: [vue()],
  server: {
    host: '127.0.0.1',
    port: 5173,
    proxy: {
      // 开发期把 /api/** 转发给 Spring Boot，避免跨域
      '/api': {
        target: API_TARGET,
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    chunkSizeWarningLimit: 1200
  }
})
