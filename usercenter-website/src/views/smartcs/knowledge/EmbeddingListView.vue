<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue';
import { ElMessage, ElLoading } from 'element-plus';
import { Search, ArrowLeft } from '@element-plus/icons-vue';
import { knowledgeApi } from '@/api/smartcs/knowledge';
import { useRoute, useRouter } from 'vue-router';

// 定义向量数据类型
interface EmbeddingItem {
  id: number;
  docId: number;
  sectionIdx: number;
  contentSnip: string;
  createdAt: number;
  updatedAt: number;
}

// 分段策略选项
const strategyOptions = [
  { value: 'HYBRID', label: '混合分段策略' },
  { value: 'CHAR_COUNT', label: '按字符数分段策略' },
  { value: 'PARAGRAPH', label: '按段落分段策略' },
  { value: 'SEMANTIC', label: '语义分段策略' },
  { value: 'SENTENCE', label: '按句子分段策略' },
];

// 路由相关
const route = useRoute();
const router = useRouter();

// 获取文档ID
const docId = computed(() => {
  const id = route.query.docId as string;
  return id ? Number(id) : 0;
});

// 响应式数据
const activeStrategy = ref('PARAGRAPH'); // 默认选中段落策略
const tableData = ref<EmbeddingItem[]>([]);
const total = ref(0);
const loading = ref(false);

// 分页参数
const pagination = reactive({
  page: 1,
  size: 10,
});

// 查询向量列表
const fetchEmbeddings = async () => {
  if (!docId.value) {
    ElMessage.error('文档ID不能为空');
    return;
  }

  loading.value = true;
  try {
    const response = await knowledgeApi.listEmbeddings({
      docId: docId.value,
      strategyName: activeStrategy.value,
      pageIndex: pagination.page,
      pageSize: pagination.size,
    });
    
    if (response && response.success) {
      tableData.value = response.data || [];
      total.value = response.totalCount || 0;
    } else {
      ElMessage.error('获取向量列表失败');
    }
  } catch (error) {
    console.error('获取向量列表出错:', error);
    ElMessage.error('获取向量列表出错');
  } finally {
    loading.value = false;
  }
};

// 策略切换
const handleStrategyChange = (strategyValue: string) => {
  activeStrategy.value = strategyValue;
  pagination.page = 1; // 重置到第一页
  fetchEmbeddings();
};

// 分页变化
const handleCurrentChange = (current: number) => {
  pagination.page = current;
  fetchEmbeddings();
};

const handleSizeChange = (size: number) => {
  pagination.size = size;
  pagination.page = 1;
  fetchEmbeddings();
};

// 返回文档管理页面
const goBack = () => {
  router.push('/platform/ai-management/content');
};

// 格式化时间戳
const formatTimestamp = (timestamp: number): string => {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  if (isNaN(date.getTime())) return '';
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const hours = date.getHours();
  const minutes = date.getMinutes();
  const seconds = date.getSeconds();

  const pad = (num: number): string => (num < 10 ? '0' + num : String(num));

  return `${year}-${pad(month)}-${pad(day)} ${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;
};

// 获取内容摘要
const getContentSummary = (content: string, maxLength: number = 100): string => {
  if (!content) return '';
  if (content.length <= maxLength) return content;
  return content.substring(0, maxLength) + '...';
};

// 页面加载时获取数据
onMounted(() => {
  if (docId.value) {
    fetchEmbeddings();
  } else {
    ElMessage.error('缺少文档ID参数');
  }
});
</script>

<template>
  <div class="embedding-list">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack" :icon="ArrowLeft" circle />
        <div class="title-section">
          <h2>向量列表</h2>
          <p>文档ID: {{ docId }}</p>
        </div>
      </div>
    </div>

    <!-- 策略切换标签 -->
    <div class="strategy-tabs">
      <el-tabs v-model="activeStrategy" @tab-change="handleStrategyChange">
        <el-tab-pane 
          v-for="option in strategyOptions" 
          :key="option.value"
          :label="option.label" 
          :name="option.value"
        />
      </el-tabs>
    </div>

    <!-- 向量表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      style="width: 100%; margin-top: 20px"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="sectionIdx" label="段落序号" width="100" />
      <el-table-column label="内容片段" min-width="400">
        <template #default="scope">
          <div class="content-cell">
            <el-tooltip :content="scope.row.contentSnip" placement="top">
              <span>{{ getContentSummary(scope.row.contentSnip, 150) }}</span>
            </el-tooltip>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="180">
        <template #default="scope">
          {{ formatTimestamp(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="更新时间" width="180">
        <template #default="scope">
          {{ formatTimestamp(scope.row.updatedAt) }}
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<style scoped>
.embedding-list {
  min-height: 70vh;
}

.page-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.title-section h2 {
  margin: 0 0 5px 0;
  font-size: 24px;
}

.title-section p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.strategy-tabs {
  margin-bottom: 20px;
}

.content-cell {
  line-height: 1.5;
  word-break: break-word;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 