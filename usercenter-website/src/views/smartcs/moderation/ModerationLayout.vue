<template>
  <div class="moderation-layout">
    <!-- 模块导航 -->
    <div class="moderation-nav">
      <el-tabs
        v-model="activeTab"
        class="moderation-tabs"
        @tab-click="onTabClick"
      >
        <el-tab-pane label="人工审核" name="review" />
        <el-tab-pane label="审核记录" name="records" />
        <el-tab-pane label="审核配置" name="config" />
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

const route = useRoute()
const router = useRouter()

// 计算当前激活的标签页
const activeTab = computed({
  get() {
    const path = route.path
    if (path.includes('/platform/smartcs/moderation/review')) return 'review'
    if (path.includes('/platform/smartcs/moderation/records')) return 'records'
    if (path.includes('/platform/smartcs/moderation/config')) return 'config'
    return 'review' // 默认
  },
  set(val) {
    switch (val) {
      case 'review':
        router.push('/platform/smartcs/moderation/review')
        break
      case 'records':
        router.push('/platform/smartcs/moderation/records')
        break
      case 'config':
        router.push('/platform/smartcs/moderation/config')
        break
      default:
        router.push('/platform/smartcs/moderation/review')
    }
  }
})

const onTabClick = () => {
  // 由 activeTab 的 setter 处理路由跳转
}
</script>

<style scoped lang="scss">
.moderation-layout {
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