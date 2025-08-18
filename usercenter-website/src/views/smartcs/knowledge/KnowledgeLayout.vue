<script setup lang="ts">
import { defineComponent } from 'vue';
</script>

<template>
  <div class="knowledge-container">
    <router-view></router-view>
  </div>
</template>

<style scoped>
.knowledge-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}
</style> 