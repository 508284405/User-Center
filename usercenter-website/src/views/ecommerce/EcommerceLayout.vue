<template>
  <div class="ecommerce-layout">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">E-commerce Platform</h1>
        <p class="page-description">电商平台管理 - 商品、订单、物流与客户服务</p>
      </div>
    </div>

    <!-- 模块导航 -->
    <el-tabs
      v-model="activeModule"
      class="module-tabs"
      @tab-click="onTabClick"
    >
      <el-tab-pane label="商品管理" name="catalog" />
      <el-tab-pane label="订单管理" name="orders" />
      <el-tab-pane label="物流管理" name="logistics" />
      <el-tab-pane label="积分管理" name="loyalty" />
    </el-tabs>

    <!-- 子页面内容 -->
    <div class="module-content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 计算当前激活的模块
const activeModule = computed({
  get() {
    const path = route.path
    if (path.includes('/platform/ecommerce/catalog')) return 'catalog'
    if (path.includes('/platform/ecommerce/orders')) return 'orders'
    if (path.includes('/platform/ecommerce/logistics')) return 'logistics'
    if (path.includes('/platform/ecommerce/loyalty')) return 'loyalty'
    return 'catalog' // 默认
  },
  set(val) {
    switch (val) {
      case 'catalog':
        router.push('/platform/ecommerce/catalog')
        break
      case 'orders':
        router.push('/platform/ecommerce/orders')
        break
      case 'logistics':
        router.push('/platform/ecommerce/logistics')
        break
      case 'loyalty':
        router.push('/platform/ecommerce/loyalty')
        break
      default:
        router.push('/platform/ecommerce/catalog')
    }
  }
})

const onTabClick = () => {
  // 由 activeModule 的 setter 处理路由跳转
}
</script>

<style scoped>
.ecommerce-layout {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding: 24px 0 16px 0;
  border-bottom: 1px solid #e4e7ed;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 2rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #ff6b6b 0%, #feca57 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-description {
  color: #6c757d;
  margin: 0;
  font-size: 14px;
}

.module-tabs {
  margin-bottom: 24px;
}

.module-content {
  min-height: 600px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .ecommerce-layout {
    padding: 0 12px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .page-title {
    font-size: 1.5rem;
  }
}

/* Tab样式增强 */
:deep(.el-tabs__header) {
  background-color: #fff5f5;
  border-radius: 8px;
  padding: 0 16px;
  margin-bottom: 0;
}

:deep(.el-tabs__nav-wrap::after) {
  display: none;
}

:deep(.el-tabs__item) {
  font-weight: 500;
  color: #606266;
  transition: all 0.3s ease;
}

:deep(.el-tabs__item.is-active) {
  color: #ff6b6b;
  font-weight: 600;
}

:deep(.el-tabs__active-bar) {
  background-color: #ff6b6b;
  height: 3px;
}
</style>