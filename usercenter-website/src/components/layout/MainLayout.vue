<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Setting, Menu, Document } from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
// 导入认证相关API
import { authApi } from '@/api/usercenter/auth'
// 导入权限控制
import { hasPermission, userPermissionState, initPermissions } from '@/utils/permission'

interface MenuItem {
  key: string
  label: string
  icon?: string
  permission?: string // 添加权限字段
  children?: MenuItem[] // 添加子菜单支持
}

const router = useRouter()
const route = useRoute()
const activeMenu = ref('user-management')
const showDropdown = ref(false)
const userName = ref('管理员') // 用户名
const userAvatar = ref('https://via.placeholder.com/40') // 用户头像

// 菜单项定义，添加需要的权限
const allMenuItems: MenuItem[] = [
  { key: 'user-management', label: '用户管理', permission: 'user:view' },
  { key: 'role-management', label: '角色管理', permission: 'role:view' },
  { key: 'menu-management', label: '菜单管理', permission: 'menu:view' },
  { key: 'operation-logs', label: '操作日志', permission: 'log:view' },
  { key: 'product-management', label: '商品管理', permission: 'product:view' },
  { key: 'category-management', label: '商品分类', permission: 'product:category:view' },
  { 
    key: 'points-management-parent', 
    label: '积分管理',
    permission: 'points:view',
    children: [
      { key: 'points-management', label: '用户积分', permission: 'points:view' },
      { key: 'points-product-management', label: '积分商品', permission: 'points:product:view' },
      { key: 'exchange-management', label: '积分兑换', permission: 'points:exchange:view' }
    ]
  },
  { key: 'order-management', label: '订单管理', permission: 'order:view' },
  { key: 'logistics-management', label: '物流管理', permission: 'logistics:view', children: [
    { key: 'logistics-packages', label: '包裹管理', permission: 'logistics:packages:view' },
    { key: 'logistics-orders', label: '物流订单管理', permission: 'logistics:orders:view' },
    { key: 'logistics-analysis', label: '物流分析', permission: 'logistics:analysis:view' }
  ] },
  { 
    key: 'after-sales', 
    label: '售后管理',
    children: [
      { key: 'after-sales-list', label: '售后单管理' },
      { key: 'after-sales-refund', label: '售后退款管理', permission: 'aftersale:refund:view' }
    ]
  },
  {
    key: 'customer-service', 
    label: '客服管理',
    permission: 'customer-service:view',
    children: [
      { key: 'all-sessions', label: '会话列表', permission: 'customer-service:view' },
      { key: 'online-agents', label: '在线客服', permission: 'customer-service:view' },
      { key: 'agent-chat', label: '客服对话', permission: 'customer-service:chat' }
    ]
  },
  {
    key: 'knowledge-management-parent',
    label: 'AI管理',
    permission: 'knowledge:view', // 假设需要此权限
    children: [
      { key: 'faq-management', label: 'FAQ管理', permission: 'knowledge:faq:view' },
      { key: 'knowledge-base-management', label: '知识库管理', permission: 'knowledge:base:view' },
      { key: 'content-management', label: '内容管理', permission: 'knowledge:content:view' },
      { key: 'KnowledgeIndex', label: '索引管理', permission: 'knowledge:index:view' },
      { key: 'provider-management', label: '模型提供商管理', permission: 'knowledge:provider:view' },
      { key: 'model-management', label: '模型管理', permission: 'knowledge:model:view' },
      { key: 'prompt-template-management', label: '模板管理', permission: 'knowledge:template:view' },
      { key: 'chat-test', label: '智能聊天', permission: 'knowledge:chat:view' }
    ]
  },
  {
    key: 'seckill-management',
    label: '秒杀管理',
    children: [
      { key: 'seckill-activity', label: '秒杀活动管理' },
      { key: 'seckill-order', label: '秒杀订单管理' }
    ]
  },
  { key: 'profile', label: '个人信息' } // 个人信息不需要特殊权限
]

// 菜单项和后端菜单的映射关系
// 将前端菜单key与后端菜单ID建立映射关系
const menuKeyToIdMap: Record<string, number> = {
  'user-management': 8, // 用户管理
  'role-management': 15, // 角色管理
  'menu-management': 16, // 菜单管理
  'operation-logs': 5, // 操作日志
  'product-management': 20, // 商品管理
  'category-management': 21, // 商品分类
  'points-management-parent': 25, // 积分管理
  'points-management': 26, // 用户积分
  'points-product-management': 27, // 积分商品
  'exchange-management': 28, // 积分兑换
  'order-management': 30, // 订单管理
  'logistics-management': 35, // 物流管理
  'logistics-packages': 36, // 包裹管理
  'logistics-orders': 37, // 物流订单管理
  'logistics-analysis': 38, // 物流分析
  'after-sales': 40, // 售后管理
  'after-sales-list': 41, // 售后单管理
  'after-sales-refund': 42, // 售后退款管理
  'customer-service': 11, // 客服管理 
  'all-sessions': 14, // 会话列表
  'online-agents': 15, // 在线客服
  'agent-chat': 19, // 客服对话
  'profile': 50, // 个人信息
  'knowledge-management-parent': 55, // AI管理
  'faq-management': 56, // FAQ管理
  'content-management': 57, // 内容管理
  'KnowledgeIndex': 58, // 索引管理
  'provider-management': 62, // 模型提供商管理
  'model-management': 63, // 模型管理
  'prompt-template-management': 60, // 模板管理
  'chat-test': 61, // 智能聊天
  'seckill-management': 70, // 秒杀管理
  'seckill-activity': 71, // 秒杀活动管理
  'seckill-order': 72 // 秒杀订单管理
}

// 过滤出用户有权限访问的菜单项
const visibleMenuItems = computed(() => {
  // // 如果是管理员或权限未初始化，显示所有菜单
  // if (userPermissionState.isAdmin || !userPermissionState.initialized) {
  //   return allMenuItems
  // }
  
  // // 如果后端未返回可见菜单ID列表，则使用原有权限逻辑
  // if (!userPermissionState.visibleMenuIds || userPermissionState.visibleMenuIds.length === 0) {
  //   // 原有基于permission的过滤逻辑
  //   return allMenuItems.filter(item => {
  //     // 没有指定权限要求的菜单项（如个人信息）或有对应权限的菜单项
  //     if (!item.permission || hasPermission(item.permission)) {
  //       // 如果有子菜单，则也需要过滤子菜单
  //       if (item.children) {
  //         const filteredChildren = item.children.filter(child => 
  //           !child.permission || hasPermission(child.permission)
  //         );
  //         // 返回过滤后的子菜单
  //         return filteredChildren.length > 0;
  //       }
  //       return true;
  //     }
  //     return false;
  //   });
  // }
  
  // // 根据后端返回的菜单ID列表过滤前端菜单
  // return allMenuItems.filter(item => {
  //   // 个人信息菜单始终可见
  //   if (item.key === 'profile') {
  //     return true;
  //   }
    
  //   // 获取当前菜单项的ID
  //   const menuId = menuKeyToIdMap[item.key];
    
  //   // 检查当前菜单是否在可见菜单ID列表中
  //   const isMenuVisible = menuId && userPermissionState.visibleMenuIds.includes(menuId);
    
  //   // 如果当前菜单不可见且有子菜单，检查子菜单是否有可见的
  //   if (!isMenuVisible && item.children && item.children.length > 0) {
  //     // 过滤子菜单
  //     const filteredChildren = item.children.filter(child => {
  //       const childMenuId = menuKeyToIdMap[child.key];
  //       return childMenuId && userPermissionState.visibleMenuIds.includes(childMenuId);
  //     });
      
  //     // 如果有可见的子菜单，则父菜单也显示
  //     if (filteredChildren.length > 0) {
  //       // 注意：这里只是创建了一个过滤后的子菜单数组，而不是直接修改原对象
  //       // 创建一个克隆的菜单项，并设置过滤后的子菜单
  //       return true;
  //     }
  //     return false;
  //   }
    
  //   return isMenuVisible;
  // }).map(item => {
  //   // 如果有子菜单，需要过滤子菜单
  //   if (item.children && item.children.length > 0) {
  //     // 创建一个新对象，避免修改原对象
  //     const newItem = { ...item };
  //     newItem.children = item.children.filter(child => {
  //       const childMenuId = menuKeyToIdMap[child.key];
  //       // 个人信息子菜单始终可见，或者在可见菜单ID列表中的菜单可见
  //       return child.key === 'profile' || (childMenuId && userPermissionState.visibleMenuIds.includes(childMenuId));
  //     });
  //     return newItem;
  //   }
  //   return item;
  // });
  return allMenuItems
})

// 根据当前路由路径设置激活的菜单项
const setActiveMenu = () => {
  const path = route.path
  console.log('当前路由路径:', path)
  
  if (path.includes('/after-sales/refund')) {
    activeMenu.value = 'after-sales-refund'
  } else if (path.includes('/after-sales')) {
    activeMenu.value = 'after-sales-list'
  } else if (path.includes('/dashboard/user')) {
    activeMenu.value = 'user-management'
  } else if (path.includes('/dashboard/role')) {
    activeMenu.value = 'role-management'
  } else if (path.includes('/dashboard/menu')) {
    activeMenu.value = 'menu-management'
  } else if (path.includes('/dashboard/log')) {
    activeMenu.value = 'operation-logs'
  } else if (path.includes('/dashboard/profile')) {
    activeMenu.value = 'profile'
  } else if (path.includes('/dashboard/product') && !path.includes('/dashboard/product/category')) {
    activeMenu.value = 'product-management'
  } else if (path.includes('/dashboard/product/category')) {
    activeMenu.value = 'category-management'
  } else if (path.includes('/dashboard/points/exchange')) {
    activeMenu.value = 'exchange-management'
    // 设置父菜单也为激活状态
    setTimeout(() => {
      const pointsSubMenu = document.querySelector('.el-sub-menu[data-menu-key="points-management-parent"]');
      if (pointsSubMenu) {
        pointsSubMenu.classList.add('is-opened');
      }
    }, 100);
  } else if (path.includes('/dashboard/points/product')) {
    activeMenu.value = 'points-product-management'
    // 设置父菜单也为激活状态
    setTimeout(() => {
      const pointsSubMenu = document.querySelector('.el-sub-menu[data-menu-key="points-management-parent"]');
      if (pointsSubMenu) {
        pointsSubMenu.classList.add('is-opened');
      }
    }, 100);
  } else if (path.includes('/dashboard/points')) {
    activeMenu.value = 'points-management'
    // 设置父菜单也为激活状态
    setTimeout(() => {
      const pointsSubMenu = document.querySelector('.el-sub-menu[data-menu-key="points-management-parent"]');
      if (pointsSubMenu) {
        pointsSubMenu.classList.add('is-opened');
      }
    }, 100);
  } else if (path.includes('/dashboard/order')) {
    activeMenu.value = 'order-management'
  } else if (path.includes('/logistics/packages')) {
    activeMenu.value = 'logistics-packages'
  } else if (path.includes('/logistics/orders')) {
    activeMenu.value = 'logistics-orders'
  } else if (path.includes('/logistics/analysis')) {
    activeMenu.value = 'logistics-analysis'
  } else if (path.includes('/customer-service/pending')) {
    activeMenu.value = 'pending-sessions'
  } else if (path.includes('/customer-service/sessions')) {
    activeMenu.value = 'all-sessions'
  } else if (path.includes('/customer-service/agents')) {
    activeMenu.value = 'online-agents'
  } else if (path.includes('/customer-service/chat')) {
    activeMenu.value = 'agent-chat'
  } else if (path.includes('/dashboard/knowledge/faq')) {
    activeMenu.value = 'faq-management';
    // Ensure parent menu is opened
    setTimeout(() => {
      const knowledgeSubMenu = document.querySelector('.el-sub-menu[data-menu-key="knowledge-management-parent"]');
      if (knowledgeSubMenu) {
        knowledgeSubMenu.classList.add('is-opened');
      }
    }, 100);
  } else if (path.includes('/dashboard/knowledge/content')) {
    activeMenu.value = 'content-management';
    setTimeout(() => {
      const knowledgeSubMenu = document.querySelector('.el-sub-menu[data-menu-key="knowledge-management-parent"]');
      if (knowledgeSubMenu) {
        knowledgeSubMenu.classList.add('is-opened');
      }
    }, 100);
  } else if (path.includes('/dashboard/knowledge/search')) {
    activeMenu.value = 'knowledge-search';
    setTimeout(() => {
      const knowledgeSubMenu = document.querySelector('.el-sub-menu[data-menu-key="knowledge-management-parent"]');
      if (knowledgeSubMenu) {
        knowledgeSubMenu.classList.add('is-opened');
      }
    }, 100);
  } else if (path.includes('/dashboard/knowledge/prompt-template')) {
    activeMenu.value = 'prompt-template-management';
    setTimeout(() => {
      const knowledgeSubMenu = document.querySelector('.el-sub-menu[data-menu-key="knowledge-management-parent"]');
      if (knowledgeSubMenu) {
        knowledgeSubMenu.classList.add('is-opened');
      }
    }, 100);
  } else if (path.includes('/dashboard/knowledge/chat')) {
    activeMenu.value = 'chat-test';
    setTimeout(() => {
      const knowledgeSubMenu = document.querySelector('.el-sub-menu[data-menu-key="knowledge-management-parent"]');
      if (knowledgeSubMenu) {
        knowledgeSubMenu.classList.add('is-opened');
      }
    }, 100);
  } else if (path.includes('/dashboard/knowledge/provider')) {
    activeMenu.value = 'provider-management';
    setTimeout(() => {
      const knowledgeSubMenu = document.querySelector('.el-sub-menu[data-menu-key="knowledge-management-parent"]');
      if (knowledgeSubMenu) {
        knowledgeSubMenu.classList.add('is-opened');
      }
    }, 100);
  } else if (path.includes('/dashboard/knowledge/model')) {
    activeMenu.value = 'model-management';
    setTimeout(() => {
      const knowledgeSubMenu = document.querySelector('.el-sub-menu[data-menu-key="knowledge-management-parent"]');
      if (knowledgeSubMenu) {
        knowledgeSubMenu.classList.add('is-opened');
      }
    }, 100);
  } else if (path.includes('/seckill/activities')) {
    activeMenu.value = 'seckill-activity';
    setTimeout(() => {
      const seckillSubMenu = document.querySelector('.el-sub-menu[data-menu-key="seckill-management"]');
      if (seckillSubMenu) {
        seckillSubMenu.classList.add('is-opened');
      }
    }, 100);
  } else if (path.includes('/seckill/orders')) {
    activeMenu.value = 'seckill-order';
    setTimeout(() => {
      const seckillSubMenu = document.querySelector('.el-sub-menu[data-menu-key="seckill-management"]');
      if (seckillSubMenu) {
        seckillSubMenu.classList.add('is-opened');
      }
    }, 100);
  } else {
    // 查找第一个有权限的菜单作为默认激活菜单
    const firstVisibleMenu = visibleMenuItems.value[0]
    if (firstVisibleMenu) {
      activeMenu.value = firstVisibleMenu.key
    } else {
      // 如果没有可见菜单，默认到个人信息
      activeMenu.value = 'profile'
    }
  }
  
  console.log('设置当前活动菜单:', activeMenu.value)
}

function handleMenuClick(key: string) {
  console.log('点击菜单:', key)
  activeMenu.value = key
  // 根据菜单key跳转到对应的路由
  switch (key) {
    case 'after-sales-list':
      router.push('/after-sales').catch(err => console.error('路由跳转失败:', err))
      break
    case 'after-sales-refund':
      router.push('/after-sales/refund').catch(err => console.error('路由跳转失败:', err))
      break
    case 'user-management':
      router.push('/dashboard/user').catch(err => {
        console.error('路由跳转失败:', err)
      })
      break
    case 'role-management':
      router.push('/dashboard/role').catch(err => {
        console.error('路由跳转失败:', err)
      })
      break
    case 'menu-management':
      router.push('/dashboard/menu').catch(err => {
        console.error('路由跳转失败:', err)
      })
      break
    case 'operation-logs':
      router.push('/dashboard/log').catch(err => {
        console.error('路由跳转失败:', err)
      })
      break
    case 'profile':
      router.push('/dashboard/profile').catch(err => {
        console.error('路由跳转失败:', err)
      })
      break
    case 'product-management':
      router.push('/dashboard/product').catch(err => {
        console.error('路由跳转失败:', err)
      })
      break
    case 'category-management':
      router.push('/dashboard/product/category').catch(err => {
        console.error('路由跳转失败:', err)
      })
      break
    case 'exchange-management':
      router.push('/dashboard/points/exchange').catch(err => {
        console.error('路由跳转失败:', err)
      })
      break
    case 'points-management':
      router.push('/dashboard/points').catch(err => {
        console.error('路由跳转失败:', err)
      })
      break
    case 'points-product-management':
      router.push('/dashboard/points/product').catch(err => {
        console.error('路由跳转失败:', err)
      })
      break
    case 'order-management':
      router.push('/dashboard/order/list').catch(err => console.error('路由跳转失败:', err))
      break
    case 'logistics-packages':
      router.push('/logistics/packages').catch(err => console.error('路由跳转失败:', err))
      break
    case 'logistics-orders':
      router.push('/logistics/orders').catch(err => console.error('路由跳转失败:', err))
      break
    case 'logistics-analysis':
      router.push('/logistics/analysis').catch(err => console.error('路由跳转失败:', err))
      break
    case 'pending-sessions':
      router.push('/customer-service/pending').catch(err => console.error('路由跳转失败:', err))
      break
    case 'all-sessions':
      router.push('/customer-service/sessions').catch(err => console.error('路由跳转失败:', err))
      break
    case 'online-agents':
      router.push('/customer-service/agents').catch(err => console.error('路由跳转失败:', err))
      break
    case 'agent-chat':
      router.push('/customer-service/chat').catch(err => console.error('路由跳转失败:', err))
      break
    case 'faq-management':
      router.push('/dashboard/knowledge/faq').catch(err => console.error('路由跳转失败:', err));
      break;
    case 'knowledge-base-management':
      router.push('/dashboard/knowledge/base').catch(err => console.error('路由跳转失败:', err));
      break;
    case 'content-management':
      router.push('/dashboard/knowledge/content').catch(err => console.error('路由跳转失败:', err));
      break;
    case 'KnowledgeIndex':
      router.push('/dashboard/knowledge/index').catch(err => console.error('路由跳转失败:', err));
      break;
    case 'provider-management':
      router.push('/dashboard/knowledge/provider').catch(err => console.error('路由跳转失败:', err));
      break;
    case 'model-management':
      router.push('/dashboard/knowledge/model').catch(err => console.error('路由跳转失败:', err));
      break;
    case 'prompt-template-management':
      router.push('/dashboard/knowledge/prompt-template').catch(err => console.error('路由跳转失败:', err));
      break;
    case 'chat-test':
      router.push('/dashboard/knowledge/chat').catch(err => console.error('路由跳转失败:', err));
      break;
    case 'seckill-activity':
      router.push('/seckill/activities').catch(err => console.error('路由跳转失败:', err));
      break;
    case 'seckill-order':
      router.push('/seckill/orders').catch(err => console.error('路由跳转失败:', err));
      break;
    case 'seckill-activity':
      router.push('/seckill/activities').catch(err => console.error('路由跳转失败:', err));
      break;
    case 'seckill-order':
      router.push('/seckill/orders').catch(err => console.error('路由跳转失败:', err));
      break;
  }
}

function handleLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  authApi.logout().then(() => {
    // 清除cookie
    document.cookie.split(';').forEach(cookie => {
      const [name] = cookie.split('=').map(c => c.trim())
      document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/`
    })
    router.push('/login')
  })
}

// 获取当前用户信息
async function loadUserInfo() {
  if (userPermissionState.userInfo) {
    // 如果权限系统已经加载了用户信息
    const userInfo = userPermissionState.userInfo
    userName.value = userInfo.username || userInfo.nickname || '用户'
    if (userInfo.avatar) {
      userAvatar.value = userInfo.avatar
    }
  } 
}

// 在组件挂载时初始化
onMounted(async () => {
  // console.log('MainLayout组件挂载，开始初始化...')
  
  // 确保权限已初始化
  if (!userPermissionState.initialized) {
    // console.log('权限未初始化，开始初始化权限...')
    await initPermissions()
    // console.log('权限初始化完成，可见菜单ID:', userPermissionState.visibleMenuIds)
  }
  
  // 加载用户信息
  await loadUserInfo()
  // console.log('用户信息加载完成:', userName.value)
  
  // 设置激活的菜单项
  setActiveMenu()
  // console.log('菜单激活设置完成, activeMenu:', activeMenu.value)
  
  // 检查可见菜单
  // console.log('可见菜单项:', visibleMenuItems.value.map(item => item.key))
})

// 监听路由变化，更新活动菜单
watch(() => route.path, (newPath, oldPath) => {
  console.log(`路由变化: 从 ${oldPath} 到 ${newPath}`)
  setActiveMenu()
})
</script>

<template>
  <div class="layout-container">
    <header class="header">
      <div class="logo">用户中心</div>
      <div class="user-info">
        <el-dropdown trigger="click">
          <div class="el-dropdown-link">
            <el-avatar :size="40" :src="userAvatar" />
            <span class="username">{{ userName }}</span>
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
          unique-opened
        >
          <template v-for="item in visibleMenuItems" :key="item.key">
            <!-- 有子菜单的情况 -->
            <el-sub-menu v-if="item.children && item.children.length > 0" :index="item.key">
              <template #title>
                <el-icon v-if="item.icon"><component :is="item.icon" /></el-icon>
                <span>{{ item.label }}</span>
              </template>
              <el-menu-item 
                v-for="child in item.children" 
                :key="child.key" 
                :index="child.key"
              >
                <template #title>
                  <el-icon v-if="child.icon"><component :is="child.icon" /></el-icon>
                  <span>{{ child.label }}</span>
                </template>
              </el-menu-item>
            </el-sub-menu>
            
            <!-- 没有子菜单的情况 -->
            <el-menu-item v-else :index="item.key">
            <template #title>
              <el-icon v-if="item.icon"><component :is="item.icon" /></el-icon>
              <span>{{ item.label }}</span>
            </template>
          </el-menu-item>
          </template>
        </el-menu>
      </aside>

      <main class="content">
        <Suspense>
          <template #default>
            <router-view></router-view>
          </template>
          <template #fallback>
            <div class="loading-container">
              <el-spinner size="large" />
              <p>加载中...</p>
            </div>
          </template>
        </Suspense>
      </main>
    </div>
  </div>
</template>

<style scoped>
.layout-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  background-color: #304156;
  color: white;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.logo {
  font-size: 20px;
  font-weight: bold;
}

.user-info {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.username {
  margin-left: 8px;
  color: white;
}

.main-container {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.sidebar {
  width: 200px;
  background-color: #304156;
  height: 100%;
  overflow-y: auto;
}

.content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f0f2f5;
}

.el-menu-vertical {
  border-right: none;
}

.loading-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
}
</style>