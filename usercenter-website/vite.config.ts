import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
// @ts-ignore
import { fileURLToPath } from 'url'

const __dirname = fileURLToPath(new URL('.', import.meta.url));

// https://vite.dev/config/
export default defineConfig({
  base: './',
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  }
})