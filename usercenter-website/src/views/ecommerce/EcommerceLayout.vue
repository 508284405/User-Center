<template>
  <div class="ecommerce-layout">
    <!-- 子页面内容 -->
    <div class="module-content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
// 移除所有标签页相关的逻辑，只保留基本结构
</script>

<style scoped>
.ecommerce-layout {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 16px;
}

.module-content {
  min-height: 600px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .ecommerce-layout {
    padding: 0 12px;
  }
}
</style>