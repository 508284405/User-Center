<template>
  <div class="smartcs-admin-layout">
    <!-- 顶部横向 TAB 导航 -->
    <el-main class="admin-main">
      <div class="main-content">
        <div class="page-header">
          <h1 class="page-title">意图管理</h1>
          <p class="page-description">统一的意图分类、目录、快照与测试管理</p>
        </div>

        <el-tabs
          v-model="activeTab"
          class="intent-tabs"
          @tab-click="onTabClick"
        >
          <el-tab-pane label="意图分类管理" name="classification" />
          <el-tab-pane label="意图目录管理" name="catalog" />
          <el-tab-pane label="快照管理" name="snapshot" />
          <el-tab-pane label="分类测试" name="test" />
        </el-tabs>

        <router-view />
      </div>
    </el-main>
  </div>
  
</template>

<script setup>
import { computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const activeTab = computed({
  get() {
    const p = route.path
    if (p.includes('/dashboard/intent-management/catalog')) return 'catalog'
    if (p.includes('/dashboard/intent-management/snapshot')) return 'snapshot'
    if (p.includes('/dashboard/intent-management/test')) return 'test'
    // 默认分类管理
    return 'classification'
  },
  set(val) {
    switch (val) {
      case 'catalog':
        router.push('/dashboard/intent-management/catalog')
        break
      case 'snapshot':
        router.push('/dashboard/intent-management/snapshot')
        break
      case 'test':
        router.push('/dashboard/intent-management/test')
        break
      default:
        router.push('/dashboard/intent-management/classification')
    }
  }
})

function onTabClick() {
  // 占位：由 activeTab 的 setter 处理路由
}

// 确保直接访问父路由时跳转到默认 TAB
watch(
  () => route.fullPath,
  (p) => {
    if (p === '/dashboard/intent-management' || p === '/dashboard/intent-management/') {
      router.replace('/dashboard/intent-management/classification')
    }
  },
  { immediate: true }
)
</script>

<style lang="scss">
@use './admin-styles.scss';
</style>
