<template>
  <el-dialog
    v-model="dialogVisible"
    :title="`缓存详情: ${cacheKey}`"
    width="60%"
    :before-close="handleClose"
  >
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
    
    <div v-else-if="cacheData" class="cache-detail">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="缓存键">
          {{ cacheData.cacheKey }}
        </el-descriptions-item>
        <el-descriptions-item label="过期时间">
          {{ formatTTL(cacheData.ttl) }}
        </el-descriptions-item>
      </el-descriptions>
      
      <div class="cache-value-section">
        <h4>缓存值:</h4>
        <el-input
          type="textarea"
          v-model="formattedValue"
          :rows="15"
          readonly
          placeholder="暂无数据"
        />
      </div>
    </div>
    
    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <el-button type="primary" @click="refreshData">刷新</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { Loading } from '@element-plus/icons-vue';
import { getCacheValue, type CacheValueDTO } from '@/api/smartcs/indexCache';

interface Props {
  visible: boolean;
  cacheKey: string;
}

interface Emits {
  (e: 'update:visible', visible: boolean): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
});

const loading = ref(false);
const cacheData = ref<CacheValueDTO | null>(null);

const formattedValue = computed(() => {
  if (!cacheData.value?.value) return '';
  try {
    return JSON.stringify(cacheData.value.value, null, 2);
  } catch (error) {
    return String(cacheData.value.value);
  }
});

const formatTTL = (ttl: number): string => {
  if (ttl === -1) return '永不过期';
  if (ttl <= 0) return '已过期';
  
  const hours = Math.floor(ttl / 3600);
  const minutes = Math.floor((ttl % 3600) / 60);
  const seconds = ttl % 60;
  
  if (hours > 0) {
    return `${hours}小时${minutes}分钟${seconds}秒`;
  } else if (minutes > 0) {
    return `${minutes}分钟${seconds}秒`;
  } else {
    return `${seconds}秒`;
  }
};

const fetchCacheValue = async () => {
  if (!props.cacheKey) return;
  
  loading.value = true;
  try {
    const res = await getCacheValue({ cacheKey: props.cacheKey });
    if (res.success && res.data) {
      cacheData.value = res.data;
    } else {
      ElMessage.error('获取缓存值失败: ' + res.errMessage);
      cacheData.value = null;
    }
  } catch (error: any) {
    ElMessage.error('获取缓存值异常: ' + (error.message || '未知错误'));
    cacheData.value = null;
  } finally {
    loading.value = false;
  }
};

const refreshData = () => {
  fetchCacheValue();
};

const handleClose = () => {
  dialogVisible.value = false;
  cacheData.value = null;
};

// 监听缓存键变化，自动获取数据
watch(() => props.cacheKey, (newKey) => {
  if (newKey && props.visible) {
    fetchCacheValue();
  }
});

// 监听模态框显示状态
watch(() => props.visible, (visible) => {
  if (visible && props.cacheKey) {
    fetchCacheValue();
  }
});
</script>

<style scoped lang="scss">
.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  
  .el-icon {
    margin-right: 8px;
  }
}

.cache-detail {
  .cache-value-section {
    margin-top: 20px;
    
    h4 {
      margin-bottom: 10px;
      font-weight: 600;
    }
  }
}
</style> 