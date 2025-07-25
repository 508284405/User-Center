import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import { permissionDirective, setupPermissionGuard } from './utils/permission'

const app = createApp(App)

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

app.use(ElementPlus)
app.use(router)
app.mount('#app')
