<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus';
import { Search, Filter, Edit, Delete, Check, Close, Plus } from '@element-plus/icons-vue';
import { contentApi, type ContentDTO, type ContentListQuery } from '@/api/smartcs/content';
import { uploadImage } from '@/api/client-web/file';
import StepWizardDialog from '@/components/knowledge/StepWizardDialog.vue';
import ImportFromUrlDialog from '@/components/knowledge/ImportFromUrlDialog.vue';

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

// 分步创建对话框
const stepWizardVisible = ref(false);
const stepWizardEditMode = ref(false);
const stepWizardEditData = ref<any>(null);
const importUrlVisible = ref(false);

// 文件扩展名到文件类型的映射
const fileTypeMapping: { [key: string]: string } = {
  '.pdf': 'PDF',
  '.txt': 'TXT',
  '.doc': 'DOC',
  '.docx': 'DOCX',
  '.md': 'MD',
  '.html': 'HTML',
};

// 过滤选项
const contentTypeOptions = [
  { value: '', label: '全部类型' },
  { value: 'document', label: '文档' },
  { value: 'audio', label: '音频' },
  { value: 'video', label: '视频' },
];

const statusOptions = [
  { value: '', label: '全部状态' },
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
    const validSearchParams: Record<string, any> = {};
    for (const key in searchParams) {
      const value = (searchParams as any)[key];
      if (value !== undefined && value !== null && value !== '') {
        validSearchParams[key] = value;
      }
    }

    const response = await contentApi.listContents(validSearchParams);
    
    if (response && response.success) {
      tableData.value = response.data || [];
      totalCount.value = response.totalCount || 0;
    } else {
      ElMessage.error('获取文档列表失败');
    }
  } catch (error) {
    console.error('获取文档列表出错:', error);
    ElMessage.error('获取文档列表出错');
  } finally {
    loading.value = false;
  }
};

// 搜索
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

// 打开新建文档对话框
const openCreateDialog = () => {
  stepWizardEditMode.value = false;
  stepWizardEditData.value = null;
  stepWizardVisible.value = true;
};

// 打开编辑文档对话框
const openEditDialog = async (row: ContentDTO) => {
  const loadingInstance = ElLoading.service({
    lock: true,
    text: '加载文档信息...',
    background: 'rgba(0, 0, 0, 0.7)'
  });

  try {
    const response = await contentApi.getById(row.id);
    
    if (response && response.success && response.data) {
      stepWizardEditMode.value = true;
      stepWizardEditData.value = response.data;
      stepWizardVisible.value = true;
    } else {
      ElMessage.error('获取文档信息失败');
    }
  } catch (error) {
    console.error('获取文档信息出错:', error);
    ElMessage.error('获取文档信息出错');
  } finally {
    loadingInstance.close();
  }
};

// 处理分步创建提交
const handleStepWizardSubmit = async (data: any) => {
  const loadingInstance = ElLoading.service({
    lock: true,
    text: stepWizardEditMode.value ? '更新文档中...' : '创建文档中...',
    background: 'rgba(0, 0, 0, 0.7)'
  });

  try {
    if (stepWizardEditMode.value) {
      // 编辑模式：更新文档
      const response = await contentApi.updateContent({
        id: stepWizardEditData.value.id,
        knowledgeBaseId: knowledgeBaseId.value,
        title: stepWizardEditData.value.title,
        segmentMode: data.segmentMode,
        segmentSettings: data.segmentSettings,
        parentChildSettings: data.parentChildSettings
      });

      if (response && response.success) {
        ElMessage.success('文档更新成功');
        fetchContents();
      } else {
        ElMessage.error(response?.errMessage || '文档更新失败');
      }
    } else {
      // 创建模式：先上传文件
      if (data.uploadedFiles.length === 0) {
        ElMessage.warning('请选择文件');
        loadingInstance.close();
        return;
      }

      const file = data.uploadedFiles[0];
      const ossUrl = await uploadImage(file);

      // 创建文档
      const response = await contentApi.create({
        knowledgeBaseId: knowledgeBaseId.value,
        title: file.name.substring(0, file.name.lastIndexOf('.')) || file.name,
        contentType: 'document',
        ossUrl: ossUrl,
        fileSize: file.size,
        fileType: fileTypeMapping[file.name.substring(file.name.lastIndexOf('.')).toLowerCase()] || 'UNKNOWN',
      });

      if (response && response.success) {
        ElMessage.success('文档创建成功');
        fetchContents();
      } else {
        ElMessage.error(response?.errMessage || '文档创建失败');
      }
    }
  } catch (error: any) {
    console.error(stepWizardEditMode.value ? '更新文档出错:' : '创建文档出错:', error);
    ElMessage.error((stepWizardEditMode.value ? '更新文档出错:' : '创建文档出错:') + error.message);
  } finally {
    loadingInstance.close();
  }
};

// 启用/禁用文档
const handleToggleStatus = async (row: ContentDTO) => {
  const newStatus = row.status === 'enabled' ? 'disabled' : 'enabled';
  const actionText = newStatus === 'enabled' ? '启用' : '禁用';
  
  try {
    await ElMessageBox.confirm(`确定要${actionText}这个文档吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const response = await contentApi.updateContentStatus({
      contentId: row.id,
      status: newStatus
    });
    
    if (response && response.success) {
      ElMessage.success(`文档${actionText}成功`);
      row.status = newStatus;
    } else {
      ElMessage.error(`文档${actionText}失败`);
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${actionText}文档出错:`, error);
      ElMessage.error(`${actionText}文档出错`);
    }
  }
};

// 删除文档
const handleDelete = async (row: ContentDTO) => {
  try {
    await ElMessageBox.confirm('确定要删除这个文档吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const response = await contentApi.delete(row.id);
    
    if (response && response.success) {
      ElMessage.success('文档删除成功');
      fetchContents();
    } else {
      ElMessage.error('文档删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除文档出错:', error);
      ElMessage.error('删除文档出错');
    }
  }
};

// 格式化数字
const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w';
  }
  return num.toString();
};

// 获取状态标签类型
const getStatusTagType = (status: string): string => {
  switch (status) {
    case 'enabled':
      return 'success';
    case 'disabled':
      return 'danger';
    default:
      return 'info';
  }
};

// 获取状态文本
const getStatusText = (status: string): string => {
  switch (status) {
    case 'enabled':
      return '启用';
    case 'disabled':
      return '禁用';
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

// 查看文档详情
const handleViewDocument = (row: ContentDTO) => {
  router.push({
    name: 'DocumentDetail',
    params: { 
      id: row.id,
      knowledgeBaseId: knowledgeBaseId.value
    }
  })
}

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
        <el-button type="primary" icon="Plus" @click="openCreateDialog">新建文档</el-button>
        <el-button @click="importUrlVisible = true">从URL导入</el-button>
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
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button 
              text 
              type="primary" 
              @click="handleViewDocument(row)"
              class="title-link"
            >
              {{ row.title }}
            </el-button>
          </template>
        </el-table-column>
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
            <span>{{ new Date(row.updatedAt).toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div style="display: flex; gap: 5px; flex-wrap: wrap;">
              <el-button 
                type="primary" 
                size="small" 
                @click="openEditDialog(row)"
              >
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button 
                :type="row.status === 'enabled' ? 'danger' : 'success'"
                size="small"
                @click="handleToggleStatus(row)"
              >
                <el-icon>
                  <component :is="row.status === 'enabled' ? Close : Check" />
                </el-icon>
                {{ row.status === 'enabled' ? '禁用' : '启用' }}
              </el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
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

    <!-- 分步创建对话框 -->
    <StepWizardDialog
      v-model:visible="stepWizardVisible"
      :title="stepWizardEditMode ? '编辑文档' : '新建文档'"
      mode="document"
      :edit-mode="stepWizardEditMode"
      :edit-data="stepWizardEditData"
      :knowledge-base-id="knowledgeBaseId"
      @submit="handleStepWizardSubmit"
      @cancel="stepWizardVisible = false"
    />

    <!-- 通过URL导入对话框 -->
    <ImportFromUrlDialog
      v-model="importUrlVisible"
      :knowledge-base-id="knowledgeBaseId"
      @success="() => { fetchContents(); }"
    />
  </div>
</template>

<style scoped>
.content-list-view {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  min-height: calc(100vh - 120px);
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
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
  background: #f5f7fa;
  border-radius: 6px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-input {
  width: 250px;
}

.filter-select {
  width: 120px;
}

.table-container {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 分步弹窗样式 */
:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-steps) {
  margin-bottom: 30px;
}

:deep(.el-step__title) {
  font-size: 14px;
}

:deep(.el-step__head.is-process) {
  color: #409eff;
}

:deep(.el-step__head.is-finish) {
  color: #67c23a;
}
</style> 