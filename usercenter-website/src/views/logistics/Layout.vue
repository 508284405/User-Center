<script setup lang="ts">
// 物流管理模块的外壳组件，用于承载子路由
import { ref } from 'vue'
</script>

<template>
  <div class="logistics-layout">
    <router-view></router-view>
  </div>
</template>

<style scoped>
.logistics-layout {
  padding: 16px;
}
</style> 