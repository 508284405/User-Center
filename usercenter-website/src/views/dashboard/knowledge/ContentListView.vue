<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus';
import { Search, Filter, Edit, Delete, Check, Close } from '@element-plus/icons-vue';
import { contentApi, type ContentDTO, type ContentListQuery } from '@/api/smartcs/content';

// 路由参数
const route = useRoute();
const knowledgeBaseId = computed(() => Number(route.params.knowledgeBaseId));

// 查询参数
const searchParams = reactive<ContentListQuery>({
  knowledgeBaseId: knowledgeBaseId.value,
  title: '',
  contentType: '',
  status: '',
  segmentMode: '',
  pageIndex: 1,
  pageSize: 20,
});

// 表格数据和加载状态
const tableData = ref<ContentDTO[]>([]);
const totalCount = ref(0);
const loading = ref(false);

// 过滤选项
const contentTypeOptions = [
  { value: '', label: '全部类型' },
  { value: 'document', label: '文档' },
  { value: 'audio', label: '音频' },
  { value: 'video', label: '视频' },
];

const statusOptions = [
  { value: '', label: '全部状态' },
  { value: 'uploaded', label: '已上传' },
  { value: 'parsed', label: '已解析' },
  { value: 'vectorized', label: '已向量化' },
  { value: 'enabled', label: '启用' },
  { value: 'disabled', label: '禁用' },
];

const segmentModeOptions = [
  { value: '', label: '全部模式' },
  { value: 'general', label: '通用' },
  { value: 'parent_child', label: '父子分段' },
];

// 查询内容列表
const fetchContents = async () => {
  loading.value = true;
  try {
    const response = await contentApi.listContents(searchParams);
    
    if (response && response.success) {
      tableData.value = response.data || [];
      totalCount.value = response.totalCount || 0;
    } else {
      ElMessage.error(response?.errMessage || '获取内容列表失败');
    }
  } catch (error) {
    console.error('获取内容列表出错:', error);
    ElMessage.error('获取内容列表出错');
  } finally {
    loading.value = false;
  }
};

// 搜索内容
const handleSearch = () => {
  searchParams.pageIndex = 1;
  fetchContents();
};

// 重置搜索
const resetSearch = () => {
  searchParams.title = '';
  searchParams.contentType = '';
  searchParams.status = '';
  searchParams.segmentMode = '';
  searchParams.pageIndex = 1;
  fetchContents();
};

// 分页变化
const handleCurrentChange = (current: number) => {
  searchParams.pageIndex = current;
  fetchContents();
};

const handleSizeChange = (size: number) => {
  searchParams.pageSize = size;
  searchParams.pageIndex = 1;
  fetchContents();
};

// 状态切换
const handleStatusToggle = async (row: ContentDTO) => {
  const newStatus = row.status === 'enabled' ? 'disabled' : 'enabled';
  const actionText = newStatus === 'enabled' ? '启用' : '禁用';
  
  try {
    await ElMessageBox.confirm(`确定要${actionText}这个内容吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const loadingInstance = ElLoading.service({
      lock: true,
      text: `${actionText}中...`,
      background: 'rgba(0, 0, 0, 0.7)'
    });
    
    try {
      const response = await contentApi.updateContentStatus({
        contentId: row.id,
        status: newStatus
      });
      
      if (response && response.success) {
        ElMessage.success(`${actionText}成功`);
        fetchContents();
      } else {
        ElMessage.error(response?.errMessage || `${actionText}失败`);
      }
    } catch (error) {
      console.error(`${actionText}出错:`, error);
      ElMessage.error(`${actionText}出错`);
    } finally {
      loadingInstance.close();
    }
  } catch {
    // 取消操作
  }
};

// 格式化数字
const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w';
  }
  return num.toString();
};

// 格式化时间
const formatTime = (timestamp: number): string => {
  return new Date(timestamp).toLocaleString();
};

// 获取状态标签类型
const getStatusTagType = (status: string): string => {
  switch (status) {
    case 'enabled':
      return 'success';
    case 'disabled':
      return 'danger';
    case 'vectorized':
      return 'primary';
    case 'parsed':
      return 'warning';
    case 'uploaded':
      return 'info';
    default:
      return '';
  }
};

// 获取状态文本
const getStatusText = (status: string): string => {
  switch (status) {
    case 'enabled':
      return '启用';
    case 'disabled':
      return '禁用';
    case 'vectorized':
      return '已向量化';
    case 'parsed':
      return '已解析';
    case 'uploaded':
      return '已上传';
    default:
      return status;
  }
};

// 获取分段模式文本
const getSegmentModeText = (mode: string): string => {
  switch (mode) {
    case 'general':
      return '通用';
    case 'parent_child':
      return '父子分段';
    default:
      return mode;
  }
};

// 页面加载时获取数据
onMounted(() => {
  fetchContents();
});
</script>

<template>
  <div class="content-list-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>文档列表</h2>
      <p>知识库ID: {{ knowledgeBaseId }}</p>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="searchParams.title"
          placeholder="搜索文档标题"
          class="search-input"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select v-model="searchParams.contentType" placeholder="内容类型" class="filter-select">
          <el-option
            v-for="option in contentTypeOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        
        <el-select v-model="searchParams.status" placeholder="状态" class="filter-select">
          <el-option
            v-for="option in statusOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        
        <el-select v-model="searchParams.segmentMode" placeholder="分段模式" class="filter-select">
          <el-option
            v-for="option in segmentModeOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </div>
      
      <div class="toolbar-right">
        <el-button @click="handleSearch" type="primary">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button @click="resetSearch">
          <el-icon><Filter /></el-icon>
          重置
        </el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%"
        stripe
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="contentType" label="类型" width="100" />
        <el-table-column prop="segmentMode" label="分段模式" width="120">
          <template #default="{ row }">
            <span>{{ getSegmentModeText(row.segmentMode) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="charCount" label="字符数" width="120">
          <template #default="{ row }">
            <span>{{ formatNumber(row.charCount || 0) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="recallCount" label="召回次数" width="120">
          <template #default="{ row }">
            <span>{{ formatNumber(row.recallCount || 0) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180">
          <template #default="{ row }">
            <span>{{ formatTime(row.updatedAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'vectorized' || row.status === 'disabled'"
              type="success"
              size="small"
              @click="handleStatusToggle(row)"
            >
              <el-icon><Check /></el-icon>
              启用
            </el-button>
            <el-button
              v-if="row.status === 'enabled'"
              type="danger"
              size="small"
              @click="handleStatusToggle(row)"
            >
              <el-icon><Close /></el-icon>
              禁用
            </el-button>
            <el-button type="primary" size="small">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="searchParams.pageIndex"
        v-model:page-size="searchParams.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalCount"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<style scoped>
.content-list-view {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.page-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 8px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-input {
  width: 280px;
}

.filter-select {
  width: 140px;
}

.table-container {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: center;
}
</style> 