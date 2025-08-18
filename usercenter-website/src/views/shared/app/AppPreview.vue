<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import AppRunner from '@/components/app/AppRunner.vue';

const route = useRoute();
const router = useRouter();

const appId = ref<number>(0);
const appName = ref<string>('');

// 获取路由参数
onMounted(() => {
  const id = route.params.id;
  if (typeof id === 'string') {
    appId.value = parseInt(id, 10);
  }
  
  if (!appId.value || isNaN(appId.value)) {
    ElMessage.error('无效的应用ID');
    router.back();
    return;
  }
});

// 关闭预览
const handleClose = () => {
  window.close();
  
  // 如果无法关闭窗口（例如不是通过window.open打开），则返回上一页
  setTimeout(() => {
    router.back();
  }, 100);
};
</script>

<template>
  <div class="app-preview">
    <AppRunner
      v-if="appId"
      :app-id="appId"
      mode="admin"
      :app-name="appName"
      @close="handleClose"
    />
  </div>
</template>

<style scoped>
.app-preview {
  height: 100vh;
  width: 100vw;
}
</style>