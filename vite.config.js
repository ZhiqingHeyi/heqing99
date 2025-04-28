import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  build: {
    outDir: path.resolve(__dirname, 'src/main/resources/static'),
    emptyOutDir: true,
    assetsDir: 'assets'
  },
  server: {
    host: '0.0.0.0',
    port: 3000,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8090',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path
      }
    }
  }
})