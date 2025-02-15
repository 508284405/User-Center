<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Setting, Menu, Document } from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
// 导入认证相关API
import { authApi } from '@/api/auth'

interface MenuItem {
  key: string
  label: string
  icon?: string
}

const router = useRouter()
const activeMenu = ref('user-management')
const showDropdown = ref(false)

const menuItems: MenuItem[] = [
  { key: 'user-management', label: '用户管理' },
  { key: 'role-management', label: '角色管理' },
  { key: 'menu-management', label: '菜单管理' },
  { key: 'operation-logs', label: '操作日志' },
  { key: 'profile', label: '个人信息' }
]

function handleMenuClick(key: string) {
  activeMenu.value = key
  // 根据菜单key跳转到对应的路由
  switch (key) {
    case 'user-management':
      router.push('/dashboard/user')
      break
    case 'role-management':
      router.push('/dashboard/role')
      break
    case 'menu-management':
      router.push('/dashboard/menu')
      break
    case 'operation-logs':
      router.push('/dashboard/log')
      break
    case 'profile':
      router.push('/dashboard/profile')
      break
  }
}

function handleLogout() {
  authApi.logout().then(() => {
    // 清除cookie
    document.cookie.split(';').forEach(cookie => {
      const [name] = cookie.split('=').map(c => c.trim())
      document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/`
    })
    router.push('/login')
  })
}
</script>

<template>
  <div class="layout-container">
    <header class="header">
      <div class="logo">用户中心</div>
      <div class="user-info">
        <el-dropdown trigger="click">
          <div class="el-dropdown-link">
            <el-avatar :size="40" src="https://via.placeholder.com/40" />
            <span class="username">Admin</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleMenuClick('profile')">个人信息</el-dropdown-item>
              <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <div class="main-container">
      <aside class="sidebar">
        <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical"
          @select="handleMenuClick"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <el-menu-item v-for="item in menuItems" :key="item.key" :index="item.key">
            <template #title>
              <el-icon v-if="item.icon"><component :is="item.icon" /></el-icon>
              <span>{{ item.label }}</span>
            </template>
          </el-menu-item>
        </el-menu>
      </aside>

      <main class="content">
        <router-view></router-view>
      </main>
    </div>

    <footer class="footer">
      <p>&copy; 2024 用户中心. All rights reserved.</p>
    </footer>
  </div>
</template>

<style scoped>
.layout-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
  color: #0044cc;
}

.user-info {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0 8px;
  &:hover {
    background: rgba(0,0,0,0.025);
  }
}

.username {
  color: #333;
  font-weight: 500;
  margin-left: 8px;
}

.main-container {
  flex: 1;
  display: flex;
}

.sidebar {
  width: 200px;
  background-color: #304156;
  height: 100%;
}

.el-menu-vertical {
  border-right: none;
}

.el-menu-item {
  &:hover {
    background-color: #263445 !important;
  }
  &.is-active {
    background-color: #1890ff !important;
  }
}

.content {
  flex: 1;
  padding: 20px;
  background-color: #f5f5f5;
}

.footer {
  padding: 15px;
  background-color: #f1f1f1;
  text-align: center;
  font-size: 14px;
  color: #666;
}
</style>