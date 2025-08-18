<template>
  <div class="forbidden-container">
    <el-result
      icon="error"
      title="403"
      sub-title="抱歉，您没有权限访问此页面"
    >
      <template #extra>
        <el-button type="primary" @click="goBack">返回上一页</el-button>
        <el-button @click="goHome">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

// 返回上一页
function goBack() {
  router.back()
}

// 返回首页或者默认页面
function goHome() {
  router.push('/dashboard')
}
</script>

<style scoped>
.forbidden-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}
</style>
