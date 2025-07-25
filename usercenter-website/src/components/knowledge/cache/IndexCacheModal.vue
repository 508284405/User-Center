<template>
  <el-dialog
    v-model="dialogVisible"
    :title="`索引缓存管理: ${indexName}`"
    width="70%"
    :before-close="handleClose"
  >
    <div class="cache-management">
      <!-- 操作区域 -->
      <div class="action-section">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-button 
              type="primary" 
              @click="loadCacheKeys"
              :loading="keysLoading"
            >
              <el-icon><Refresh /></el-icon>
              刷新缓存键列表
            </el-button>
          </el-col>
          <el-col :span="12">
            <el-button 
              type="danger" 
              @click="confirmClearCache"
              :loading="clearLoading"
            >
              <el-icon><Delete /></el-icon>
              清空所有缓存
            </el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 缓存键列表 -->
      <div class="keys-section">
        <h4>缓存键列表 ({{ cacheKeys.length }} 个)</h4>
        
        <div v-if="keysLoading" class="loading-container">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载缓存键中...</span>
        </div>
        
        <div v-else-if="cacheKeys.length === 0" class="empty-container">
          <el-empty description="暂无缓存键" />
        </div>
        
        <el-table 
          v-else
          :data="cacheKeys" 
          stripe
          style="width: 100%"
          max-height="400"
        >
          <el-table-column prop="key" label="缓存键" min-width="200">
            <template #default="{ row }">
              <el-text class="cache-key">{{ row }}</el-text>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button 
                type="primary" 
                size="small" 
                @click="viewCacheValue(row)"
              >
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
    
    <!-- 缓存值详情模态框 -->
    <cache-value-detail-modal
      v-model:visible="valueDetailVisible"
      :cache-key="selectedCacheKey"
    />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Refresh, Delete, Loading } from '@element-plus/icons-vue';
import { clearIndexCache, listIndexCacheKeys } from '@/api/smartcs/indexCache';
import CacheValueDetailModal from './CacheValueDetailModal.vue';

interface Props {
  visible: boolean;
  indexName: string;
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

const cacheKeys = ref<string[]>([]);
const keysLoading = ref(false);
const clearLoading = ref(false);
const valueDetailVisible = ref(false);
const selectedCacheKey = ref('');

// 加载缓存键列表
const loadCacheKeys = async () => {
  if (!props.indexName) return;
  
  keysLoading.value = true;
  try {
    const res = await listIndexCacheKeys({ indexName: props.indexName });
    if (res.success && res.data) {
      cacheKeys.value = res.data;
    } else {
      ElMessage.error('获取缓存键列表失败: ' + res.errMessage);
      cacheKeys.value = [];
    }
  } catch (error: any) {
    ElMessage.error('获取缓存键列表异常: ' + (error.message || '未知错误'));
    cacheKeys.value = [];
  } finally {
    keysLoading.value = false;
  }
};

// 确认清空缓存
const confirmClearCache = () => {
  ElMessageBox.confirm(
    `确定要清空索引 '${props.indexName}' 下的所有缓存吗？此操作不可撤销！`,
    '危险操作确认',
    {
      confirmButtonText: '确定清空',
      cancelButtonText: '取消',
      type: 'warning',
      dangerouslyUseHTMLString: true,
    }
  ).then(() => {
    handleClearCache();
  }).catch(() => {
    // 用户取消操作
  });
};

// 执行清空缓存
const handleClearCache = async () => {
  clearLoading.value = true;
  try {
    const res = await clearIndexCache({ indexName: props.indexName });
    if (res.success) {
      ElMessage.success('缓存清空成功');
      // 清空成功后重新加载键列表
      loadCacheKeys();
    } else {
      ElMessage.error('清空缓存失败: ' + res.errMessage);
    }
  } catch (error: any) {
    ElMessage.error('清空缓存异常: ' + (error.message || '未知错误'));
  } finally {
    clearLoading.value = false;
  }
};

// 查看缓存值详情
const viewCacheValue = (cacheKey: string) => {
  selectedCacheKey.value = cacheKey;
  valueDetailVisible.value = true;
};

// 关闭模态框
const handleClose = () => {
  dialogVisible.value = false;
  cacheKeys.value = [];
  selectedCacheKey.value = '';
};

// 监听模态框显示状态，自动加载数据
watch(() => props.visible, (visible) => {
  if (visible && props.indexName) {
    loadCacheKeys();
  }
});

// 监听索引名称变化
watch(() => props.indexName, (newIndexName) => {
  if (newIndexName && props.visible) {
    loadCacheKeys();
  }
});
</script>

<style scoped lang="scss">
.cache-management {
  .action-section {
    margin-bottom: 20px;
    padding: 16px;
    background-color: #f5f7fa;
    border-radius: 6px;
  }

  .keys-section {
    h4 {
      margin-bottom: 16px;
      font-weight: 600;
      color: #303133;
    }
  }

  .loading-container {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px;
    
    .el-icon {
      margin-right: 8px;
    }
  }

  .empty-container {
    padding: 20px 0;
  }

  .cache-key {
    font-family: 'Courier New', monospace;
    font-size: 13px;
    color: #606266;
  }
}
</style> 