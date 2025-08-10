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
    window.close();
    return;
  }
});

// 关闭应用
const handleClose = () => {
  window.close();
  
  // 如果无法关闭窗口（例如不是通过window.open打开），则跳转到登录页
  setTimeout(() => {
    router.push('/login');
  }, 100);
};
</script>

<template>
  <div class="app-run">
    <AppRunner
      v-if="appId"
      :app-id="appId"
      mode="public"
      :app-name="appName"
      @close="handleClose"
    />
  </div>
</template>

<style scoped>
.app-run {
  height: 100vh;
  width: 100vw;
}
</style>