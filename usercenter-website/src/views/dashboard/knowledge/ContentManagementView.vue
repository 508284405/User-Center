<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue';
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus';
import { Search, Plus, Edit, Delete, Upload, Document, View } from '@element-plus/icons-vue';
import { contentApi } from '@/api/smartcs/content';
import { knowledgeBaseApi } from '@/api/smartcs/knowledgeBase';
import { uploadImage } from '@/api/client-web/file';
import { useRouter } from 'vue-router';

// 定义内容数据类型
interface Content {
  id?: number;
  knowledgeBaseId?: number;
  title?: string;
  contentType?: string;
  fileUrl?: string;
  textExtracted?: string;
  status?: string;
  createdBy?: number;
  createdAt?: number;
  updatedAt?: number;
  fileType?: string;
}

// 定义知识库数据类型
interface KnowledgeBase {
  id: number;
  name: string;
  code: string;
}

// 定义向量检索请求类型
interface DocumentSearchRequest {
  query: string;
  contentId?: number;
  topK?: number;
}

// 定义向量检索结果类型
interface DocumentSearchResultDTO {
  id?: number;
  text?: string;
  score?: number;
  metadata?: any;
}

// 查询参数
const searchParams = reactive({
  knowledgeBaseId: undefined as number | undefined,
  title: '',
  contentType: '',
  status: '',
  pageIndex: 1,
  pageSize: 10,
  needTotalCount: true,
});

// 表格数据和加载状态
const tableData = ref<Content[]>([]);
const totalCount = ref(0);
const loading = ref(false);

// 知识库列表
const knowledgeBaseList = ref<KnowledgeBase[]>([]);

// 新增/编辑对话框
const dialogVisible = ref(false);
const dialogTitle = ref('');
const isEdit = ref(false);
const contentForm = reactive({
  id: undefined as number | undefined,
  knowledgeBaseId: undefined as number | undefined,
  title: '',
  contentType: 'document',
  file: null as File | null,
  ossUrl: '',
  fileSize: 0,
  fileType: '',
});

// 向量检索对话框
const vectorSearchVisible = ref(false);
const vectorSearchLoading = ref(false);
const vectorSearchForm = reactive({
  query: '',
  contentId: undefined as number | undefined,
  contentTitle: '',
});
const vectorSearchResults = ref<DocumentSearchResultDTO[]>([]);



// 内容类型选项
const contentTypeOptions = [
  { value: 'document', label: '文档' },
  { value: 'audio', label: '音频' },
  { value: 'video', label: '视频' },
];

// 状态选项
const statusOptions = [
  { value: 'uploaded', label: '已上传' },
  { value: 'parsed', label: '已解析' },
  { value: 'vectorized', label: '已向量化' },
];



// 路由
const router = useRouter();

// 文件扩展名到文件类型的映射
const fileTypeMapping: { [key: string]: string } = {
  '.pdf': 'PDF',
  '.txt': 'TXT',
  '.doc': 'DOC',
  '.docx': 'DOCX',
  '.md': 'MD',
  '.html': 'HTML',
};

// 获取知识库列表
const fetchKnowledgeBases = async () => {
  try {
    const response = await knowledgeBaseApi.list({
      pageSize: 100,
      pageIndex: 1,
      needTotalCount: false,
    });
    
    if (response && response.success) {
      knowledgeBaseList.value = response.data || [];
    }
  } catch (error) {
    console.error('获取知识库列表出错:', error);
  }
};

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

    const response = await contentApi.list(validSearchParams);
    
    if (response && response.success) {
      tableData.value = response.data || [];
      totalCount.value = response.totalCount || 0;
    } else {
      ElMessage.error('获取内容列表失败');
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
  searchParams.knowledgeBaseId = undefined;
  searchParams.title = '';
  searchParams.contentType = '';
  searchParams.status = '';
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

// 打开新增对话框
const openCreateDialog = () => {
  dialogTitle.value = '新增内容';
  isEdit.value = false;
  contentForm.id = undefined;
  contentForm.knowledgeBaseId = undefined;
  contentForm.title = '';
  contentForm.contentType = 'document';
  contentForm.file = null;
  contentForm.ossUrl = '';
  contentForm.fileSize = 0;
  contentForm.fileType = '';
  dialogVisible.value = true;
};

// 打开编辑对话框
const openEditDialog = (row: Content) => {
  dialogTitle.value = '编辑内容';
  isEdit.value = true;
  contentForm.id = row.id;
  contentForm.knowledgeBaseId = row.knowledgeBaseId;
  contentForm.title = row.title || '';
  contentForm.contentType = row.contentType || 'document';
  contentForm.file = null;
  contentForm.ossUrl = row.fileUrl || '';
  dialogVisible.value = true;
};

// 处理文件选择
const handleFileChange = (e: Event) => {
  const target = e.target as HTMLInputElement;
  const files = target.files;
  if (files && files.length > 0) {
    contentForm.file = files[0];
    if (!contentForm.title) {
      const fileName = files[0].name;
      contentForm.title = fileName.substring(0, fileName.lastIndexOf('.')) || fileName;
    }
    const fileName = files[0].name;
    const fileExtension = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
    contentForm.fileType = fileTypeMapping[fileExtension] || 'UNKNOWN';
    contentForm.fileSize = files[0].size;
  }
};

// 提交表单
const handleSubmit = async () => {
  if (!contentForm.title || !contentForm.knowledgeBaseId) {
    ElMessage.warning('请填写标题并选择知识库');
    return;
  }

  if (!isEdit.value && !contentForm.file) {
    ElMessage.warning('请选择文件');
    return;
  }

  const loadingInstance = ElLoading.service({
    lock: true,
    text: isEdit.value ? '更新中...' : '创建中...',
    background: 'rgba(0, 0, 0, 0.7)'
  });

  try {
    let response;
    if (isEdit.value && contentForm.id) {
      response = await contentApi.update({
        id: contentForm.id,
        title: contentForm.title,
      });
    } else {
      // 先上传文件
      if (!contentForm.file) {
        ElMessage.warning('请选择文件');
        loadingInstance.close();
        return;
      }
      const ossUrl = await uploadImage(contentForm.file);

      response = await contentApi.create({
        knowledgeBaseId: contentForm.knowledgeBaseId,
        title: contentForm.title,
        contentType: contentForm.contentType,
        ossUrl: ossUrl,
        fileSize: contentForm.fileSize,
        fileType: contentForm.fileType,
      });
    }

    if (response && response.success) {
      ElMessage.success(isEdit.value ? '内容更新成功' : '内容创建成功');
      dialogVisible.value = false;
      fetchContents();
    } else {
      ElMessage.error(response?.errMessage || '操作失败');
    }
  } catch (error: any) {
    console.error('操作内容出错:', error);
    ElMessage.error('操作内容出错:' + error.message);
  } finally {
    loadingInstance.close();
  }
};

// 删除内容
const handleDelete = (row: Content) => {
  if (!row.id) return;
  
  ElMessageBox.confirm('确定要删除这个内容吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await contentApi.delete(row.id!);
      
      if (response && response.success) {
        ElMessage.success('内容删除成功');
        fetchContents();
      } else {
        ElMessage.error('内容删除失败');
      }
    } catch (error) {
      console.error('删除内容出错:', error);
      ElMessage.error('删除内容出错');
    }
  }).catch(() => {
    // 取消删除
  });
};

// 解析内容
const handleParse = async (row: Content) => {
  if (!row.id) return;
  
  const loadingInstance = ElLoading.service({
    lock: true,
    text: '解析中...',
    background: 'rgba(0, 0, 0, 0.7)'
  });

  try {
    const response = await contentApi.parse(row.id);
    
    if (response && response.success) {
      ElMessage.success('内容解析已触发，请稍后刷新查看状态');
      setTimeout(() => {
        fetchContents();
      }, 2000);
    } else {
      ElMessage.error('触发内容解析失败');
    }
  } catch (error) {
    console.error('解析内容出错:', error);
    ElMessage.error('解析内容出错');
  } finally {
    loadingInstance.close();
  }
};



// 格式化文件大小
const formatFileSize = (bytes: number | undefined) => {
  if (!bytes || bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

// 格式化时间戳
const formatTimestamp = (timestamp: number | string | undefined): string => {
  if (!timestamp) return '';
  const numericTimestamp = typeof timestamp === 'string' ? parseInt(timestamp, 10) : timestamp;
  const date = new Date(numericTimestamp);
  if (isNaN(date.getTime())) return '';
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const hours = date.getHours();
  const minutes = date.getMinutes();
  const seconds = date.getSeconds();

  const pad = (num: number): string => (num < 10 ? '0' + num : String(num));

  return `${year}-${month}-${day} ${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;
};

// 获取知识库名称
const getKnowledgeBaseName = (id: number | undefined): string => {
  if (!id) return '';
  const kb = knowledgeBaseList.value.find(item => item.id === id);
  return kb ? kb.name : '';
};

// 查看切片详情
const handleViewChunks = (row: Content) => {
  if (!row.id) return;
  router.push(`/dashboard/knowledge/content/${row.id}/chunks`);
};

// 打开向量检索对话框
const openVectorSearchDialog = (row: Content) => {
  vectorSearchForm.contentId = row.id;
  vectorSearchForm.contentTitle = row.title || '';
  vectorSearchForm.query = '';
  vectorSearchResults.value = [];
  vectorSearchVisible.value = true;
};

// 执行向量检索
const handleVectorSearch = async () => {
  if (!vectorSearchForm.query.trim()) {
    ElMessage.warning('请输入检索内容');
    return;
  }

  vectorSearchLoading.value = true;
  try {
    const request: DocumentSearchRequest = {
      query: vectorSearchForm.query,
      contentId: vectorSearchForm.contentId,
      topK: 10,
    };

    const response = await contentApi.vectorSearch(request);
    
    if (response && response.success) {
      vectorSearchResults.value = response.data || [];
      if (vectorSearchResults.value.length === 0) {
        ElMessage.info('未检索到相关内容');
      }
    } else {
      ElMessage.error('向量检索失败');
    }
  } catch (error) {
    console.error('向量检索出错:', error);
    ElMessage.error('向量检索出错');
  } finally {
    vectorSearchLoading.value = false;
  }
};

// 复制文本到剪贴板
const copyToClipboard = async (text: string | undefined) => {
  if (!text) return;
  
  try {
    await navigator.clipboard.writeText(text);
    ElMessage.success('已复制到剪贴板');
  } catch (error) {
    console.error('复制失败:', error);
    ElMessage.error('复制失败');
  }
};

// 页面加载时获取数据
onMounted(() => {
  fetchKnowledgeBases();
  fetchContents();
});
</script>

<template>
  <div class="content-management">
    <div class="page-header">
      <h2>内容管理</h2>
      <p>管理知识库内容的创建、编辑、解析和向量化</p>
    </div>

    <!-- 搜索和工具栏 -->
    <div class="toolbar">
      <el-select v-model="searchParams.knowledgeBaseId" placeholder="选择知识库" clearable class="kb-select">
        <el-option
          v-for="kb in knowledgeBaseList"
          :key="kb.id"
          :label="kb.name"
          :value="kb.id"
        />
      </el-select>
      <el-input
        v-model="searchParams.title"
        placeholder="搜索内容标题"
        class="search-input"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="searchParams.contentType" placeholder="内容类型" clearable class="type-select">
        <el-option
          v-for="option in contentTypeOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
      <el-select v-model="searchParams.status" placeholder="状态" clearable class="status-select">
        <el-option
          v-for="option in statusOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
      <el-button type="primary" icon="Plus" @click="openCreateDialog">新增内容</el-button>
    </div>

    <!-- 内容表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      style="width: 100%; margin-top: 20px"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="知识库" width="150">
        <template #default="scope">
          {{ getKnowledgeBaseName(scope.row.knowledgeBaseId) }}
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column label="类型" width="100">
        <template #default="scope">
          <el-tag>{{ scope.row.contentType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="fileType" label="文件类型" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag 
            :type="scope.row.status === 'vectorized' ? 'success' : 
                   scope.row.status === 'parsed' ? 'warning' : 'info'"
          >
            {{ scope.row.status === 'uploaded' ? '已上传' : 
               scope.row.status === 'parsed' ? '已解析' : 
               scope.row.status === 'vectorized' ? '已向量化' : scope.row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="180">
        <template #default="scope">
          {{ formatTimestamp(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="提取文本" min-width="200">
        <template #default="scope">
          <el-tooltip
            effect="dark"
            :content="scope.row.textExtracted"
            placement="top"
            :disabled="!scope.row.textExtracted"
          >
            <div class="text-ellipsis">{{ scope.row.textExtracted }}</div>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="400" fixed="right">
        <template #default="scope">
          <div style="display: flex; gap: 5px; flex-wrap: wrap;">
            <el-button type="info" size="small" @click="handleViewChunks(scope.row)">
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
            <el-button type="primary" size="small" @click="openEditDialog(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button 
              type="warning" 
              size="small" 
              @click="handleParse(scope.row)"
            >
              <el-icon><Document /></el-icon>
              解析
            </el-button>
            <el-button 
              type="success" 
              size="small" 
              @click="openVectorSearchDialog(scope.row)"
            >
              <el-icon><Search /></el-icon>
              向量检索
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="50%"
    >
      <el-form label-width="100px">
        <el-form-item label="知识库">
          <el-select v-model="contentForm.knowledgeBaseId" placeholder="请选择知识库" style="width: 100%">
            <el-option
              v-for="kb in knowledgeBaseList"
              :key="kb.id"
              :label="kb.name"
              :value="kb.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="内容标题">
          <el-input v-model="contentForm.title" placeholder="请输入内容标题" />
        </el-form-item>
        <el-form-item label="内容类型">
          <el-radio-group v-model="contentForm.contentType" :disabled="isEdit">
            <el-radio label="document">文档</el-radio>
            <el-radio label="audio">音频</el-radio>
            <el-radio label="video">视频</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="选择文件" v-if="!isEdit">
          <input 
            type="file" 
            @change="handleFileChange" 
            accept=".pdf,.txt,.doc,.docx,.md,.html"
          />
          <div v-if="contentForm.file" class="selected-file">
            已选择文件: {{ contentForm.file.name }} ({{ formatFileSize(contentForm.file.size) }})
          </div>
          <div class="file-tip">
            支持的格式: PDF, TXT, DOC, DOCX, MD, HTML
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 向量检索对话框 -->
    <el-dialog
      v-model="vectorSearchVisible"
      title="向量检索"
      width="70%"
      :close-on-click-modal="false"
    >
      <div class="vector-search-content">
        <div class="search-header">
          <h4>内容：{{ vectorSearchForm.contentTitle }}</h4>
          <p class="search-tip">请输入自然语言问题或文本片段进行向量检索</p>
        </div>
        
        <div class="search-form">
          <el-input
            v-model="vectorSearchForm.query"
            type="textarea"
            :rows="3"
            placeholder="请输入检索问题或文本..."
            maxlength="500"
            show-word-limit
            @keyup.enter.ctrl="handleVectorSearch"
          />
          <div class="search-actions">
            <el-button 
              type="primary" 
              :loading="vectorSearchLoading"
              @click="handleVectorSearch"
            >
              开始检索
            </el-button>
            <span class="search-hint">提示：Ctrl + Enter 快速检索</span>
          </div>
        </div>

        <div class="search-results" v-if="vectorSearchResults.length > 0">
          <h4>检索结果（{{ vectorSearchResults.length }} 条）</h4>
          <div class="results-list">
            <div 
              v-for="(result, index) in vectorSearchResults" 
              :key="index"
              class="result-item"
            >
              <div class="result-header">
                <span class="result-index">#{{ index + 1 }}</span>
                <span class="result-score" v-if="result.score">
                  相关度: {{ (result.score * 100).toFixed(1) }}%
                </span>
              </div>
              <div class="result-content">
                {{ result.text }}
              </div>
              <div class="result-actions">
                <el-button 
                  size="small" 
                  type="text"
                  @click="copyToClipboard(result.text)"
                >
                  复制文本
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <div class="no-results" v-else-if="vectorSearchForm.query && !vectorSearchLoading">
          <el-empty description="未检索到相关内容" />
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="vectorSearchVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<style scoped>
.content-management {
  min-height: 70vh;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
}

.page-header p {
  margin: 0;
  color: #666;
}

.toolbar {
  display: flex;
  margin-bottom: 20px;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.kb-select {
  width: 200px;
}

.search-input {
  width: 250px;
}

.type-select {
  width: 120px;
}

.status-select {
  width: 120px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.selected-file {
  margin-top: 8px;
  font-size: 14px;
  color: #409EFF;
}

.file-tip {
  margin-top: 5px;
  font-size: 12px;
  color: #999;
}

/* 向量检索对话框样式 */
.vector-search-content {
  padding: 0;
}

.search-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.search-header h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.search-tip {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.search-form {
  margin-bottom: 20px;
}

.search-actions {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-hint {
  font-size: 12px;
  color: #909399;
}

.search-results h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.results-list {
  max-height: 400px;
  overflow-y: auto;
}

.result-item {
  margin-bottom: 16px;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background-color: #fafafa;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.result-index {
  font-size: 14px;
  font-weight: 600;
  color: #409eff;
}

.result-score {
  font-size: 12px;
  color: #67c23a;
  background-color: #f0f9ff;
  padding: 2px 8px;
  border-radius: 12px;
}

.result-content {
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
  margin-bottom: 12px;
  word-break: break-word;
}

.result-actions {
  display: flex;
  justify-content: flex-end;
}

.no-results {
  text-align: center;
  padding: 40px 0;
}


</style>