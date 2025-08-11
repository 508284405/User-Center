import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import { permissionDirective, setupPermissionGuard } from './utils/permission'
// 全局引入认证页主题变量与样式
import './styles/auth-theme.scss'
// 导入主题系统
import { initGlobalTheme } from './composables/useTheme'

const app = createApp(App)
const pinia = createPinia()

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册权限指令
app.directive('permission', permissionDirective)

// 设置权限路由守卫
setupPermissionGuard(router)

// 兼容 sockjs-client 浏览器环境
window.global = window;

// 初始化全局主题系统
initGlobalTheme()

app.use(pinia)
app.use(ElementPlus)
app.use(router)
app.mount('#app')
