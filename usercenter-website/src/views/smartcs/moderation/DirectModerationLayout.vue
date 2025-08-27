<template>
  <div class="direct-moderation-layout">
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <el-icon><Setting /></el-icon>
          审核策略管理
        </h2>
        <p class="page-subtitle">配置不同场景下的内容审核策略，管理审核维度和模板</p>
      </div>
    </div>

    <!-- 模块导航 -->
    <div class="moderation-nav">
      <el-tabs
        v-model="activeTab"
        class="moderation-tabs"
        @tab-click="onTabClick"
      >
        <el-tab-pane label="审核策略" name="policies" />
        <el-tab-pane label="审核维度" name="dimensions" />
        <el-tab-pane label="模板管理" name="templates" />
      </el-tabs>
    </div>

    <!-- 页面内容 -->
    <div class="moderation-content">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Setting } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

// 计算当前激活的标签页
const activeTab = computed({
  get() {
    const path = route.path
    if (path.includes('/platform/ai-management/moderation/dimensions')) return 'dimensions'
    if (path.includes('/platform/ai-management/moderation/templates')) return 'templates'
    return 'policies' // 默认
  },
  set(val) {
    switch (val) {
      case 'policies':
        router.push('/platform/ai-management/moderation')
        break
      case 'dimensions':
        router.push('/platform/ai-management/moderation/dimensions')
        break
      case 'templates':
        router.push('/platform/ai-management/moderation/templates')
        break
      default:
        router.push('/platform/ai-management/moderation')
    }
  }
})

const onTabClick = () => {
  // 由 activeTab 的 setter 处理路由跳转
}
</script>

<style scoped lang="scss">
.direct-moderation-layout {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;

  .page-header {
    margin-bottom: 24px;

    .header-content {
      .page-title {
        font-size: 24px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 8px 0;
        display: flex;
        align-items: center;
        gap: 8px;

        .el-icon {
          color: #409eff;
        }
      }

      .page-subtitle {
        color: #606266;
        font-size: 14px;
        margin: 0;
        line-height: 1.5;
      }
    }
  }

  .moderation-nav {
    margin-bottom: 24px;
  }

  .moderation-tabs {
    :deep(.el-tabs__header) {
      background-color: #f8f9fa;
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
      color: #409eff;
      font-weight: 600;
    }

    :deep(.el-tabs__active-bar) {
      background-color: #409eff;
      height: 3px;
    }
  }

  .moderation-content {
    min-height: 400px;
  }
}
</style>