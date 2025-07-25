<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus';
import { Search, Plus, Edit, Delete, Upload, Document, Link, Filter, Loading, Warning } from '@element-plus/icons-vue';
import { knowledgeBaseApi } from '@/api/smartcs/knowledgeBase';
import { uploadDocument } from '@/api/client-web/file';

// 定义知识库数据类型
interface KnowledgeBase {
  id?: number;
  name: string;
  code: string;
  description?: string;
  ownerId?: number;
  visibility: string;
  createdBy?: number;
  createdAt?: number;
  updatedAt?: number;
  // Mock 数据字段
  documentCount?: number;
  charCount?: number;
  associatedApps?: number;
}

// 查询参数
const searchParams = reactive({
  name: '',
  visibility: '',
  pageIndex: 1,
  pageSize: 12,
  needTotalCount: true,
});

// 表格数据和加载状态
const tableData = ref<KnowledgeBase[]>([]);
const totalCount = ref(0);
const loading = ref(false);

// Tab 相关
const activeTab = ref('knowledge');

// 路由
const router = useRouter();

// 创建知识库弹窗相关
const createDialogVisible = ref(false);
const currentStep = ref(1);
const selectedDataSource = ref('text');
const uploadedFiles = ref<File[]>([]);

// 新增/编辑对话框
const dialogVisible = ref(false);
const dialogTitle = ref('');
const isEdit = ref(false);
const knowledgeBaseForm = reactive({
  id: undefined as number | undefined,
  name: '',
  code: '',
  description: '',
  visibility: 'public',
});

// 可见性选项
const visibilityOptions = [
  { value: '', label: '全部' },
  { value: 'public', label: '公开' },
  { value: 'private', label: '私有' },
];

// 数据源选项
const dataSourceOptions = [
  { value: 'text', label: '导入已有文本', icon: Document },
  { value: 'notion', label: '同步自 Notion 内容', icon: Link },
  { value: 'web', label: '同步自 Web 站点', icon: Link },
];

// 支持的文件格式
const supportedFormats = ['TXT', 'MARKDOWN', 'MDX', 'PDF', 'HTML', 'XLSX', 'XLS', 'DOCX', 'CSV', 'MD', 'HTM'];

// 分段设置
const segmentMode = ref('general'); // 'general' 或 'parent_child'
const segmentSettings = reactive({
  identifier: '\n\n',
  maxLength: 500,
  overlapLength: 50,
  replaceConsecutiveSpaces: true,
  removeAllUrls: false,
  useQASegmentation: false,
  qaLanguage: 'Chinese'
});

// 父子分段设置
const parentChildSettings = reactive({
  parentIdentifier: '\n\n',
  parentMaxLength: 500,
  childIdentifier: '\n',
  childMaxLength: 200,
  replaceConsecutiveSpaces: true,
  removeAllUrls: false
});

// 索引方式
const indexMethod = ref('high_quality');

// 检索设置
const retrievalSettings = reactive({
  method: 'vector_search',
  rerankModel: 'gte-rerank',
  topK: 3,
  scoreThreshold: 0.5,
  fullTextSearch: false,
  hybridSearch: false
});

// 预览块相关
const previewChunks = ref<any[]>([]);
const previewLoading = ref(false);
const previewError = ref('');
const uploadProgress = ref('');
const chunkProgress = ref('');

// 生成 Mock 数据
const generateMockData = (kb: KnowledgeBase): KnowledgeBase => {
  return {
    ...kb,
    documentCount: Math.floor(Math.random() * 100) + 1,
    charCount: Math.floor(Math.random() * 100000) + 1000,
    associatedApps: Math.floor(Math.random() * 10),
  };
};

// 查询知识库列表
const fetchKnowledgeBases = async () => {
  loading.value = true;
  try {
    const validSearchParams: Record<string, any> = {};
    for (const key in searchParams) {
      const value = (searchParams as any)[key];
      if (value !== undefined && value !== null && value !== '') {
        validSearchParams[key] = value;
      }
    }

    const response = await knowledgeBaseApi.list(validSearchParams);
    
    if (response && response.success) {
      // 为每个知识库添加 mock 数据
      tableData.value = (response.data || []).map(generateMockData);
      totalCount.value = response.totalCount || 0;
    } else {
      ElMessage.error('获取知识库列表失败');
    }
  } catch (error) {
    console.error('获取知识库列表出错:', error);
    ElMessage.error('获取知识库列表出错');
  } finally {
    loading.value = false;
  }
};

// 搜索知识库
const handleSearch = () => {
  searchParams.pageIndex = 1;
  fetchKnowledgeBases();
};

// 重置搜索
const resetSearch = () => {
  searchParams.name = '';
  searchParams.visibility = '';
  searchParams.pageIndex = 1;
  fetchKnowledgeBases();
};

// 分页变化
const handleCurrentChange = (current: number) => {
  searchParams.pageIndex = current;
  fetchKnowledgeBases();
};

const handleSizeChange = (size: number) => {
  searchParams.pageSize = size;
  searchParams.pageIndex = 1;
  fetchKnowledgeBases();
};

// Tab 切换
const handleTabChange = (tab: string) => {
  if (tab === 'api') {
    ElMessage.info('该功能暂未实现');
    activeTab.value = 'knowledge';
  }
};

// 打开创建知识库弹窗
const openCreateDialog = () => {
  currentStep.value = 1;
  selectedDataSource.value = 'text';
  uploadedFiles.value = [];
  createDialogVisible.value = true;
};

// 处理数据源选择
const handleDataSourceSelect = (source: string) => {
  if (source === 'notion' || source === 'web') {
    ElMessage.info('该功能暂未实现');
    return;
  }
  selectedDataSource.value = source;
};

// 文件上传前的校验
const beforeUpload = (file: File) => {
  const extension = file.name.split('.').pop()?.toUpperCase();
  if (!extension || !supportedFormats.includes(extension)) {
    ElMessage.error(`不支持的文件格式，请上传 ${supportedFormats.join('、')} 格式的文件`);
    return false;
  }
  if (file.size > 15 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 15MB');
    return false;
  }
  return true;
};

// 处理文件上传
const handleFileUpload = (file: File) => {
  if (beforeUpload(file)) {
    uploadedFiles.value.push(file);
  }
  return false; // 阻止自动上传
};

// 移除文件
const handleFileRemove = (index: number) => {
  uploadedFiles.value.splice(index, 1);
};

// 创建知识库下一步
const handleCreateNext = () => {
  if (currentStep.value === 1) {
    if (uploadedFiles.value.length === 0) {
      ElMessage.warning('请先上传文件');
      return;
    }
    currentStep.value = 2;
  } else if (currentStep.value === 2) {
    currentStep.value = 3;
  } else {
    ElMessage.info('该功能暂未实现');
  }
};

// 上一步
const handleCreatePrev = () => {
  if (currentStep.value > 1) {
    currentStep.value--;
  }
};

// 创建空知识库
const createEmptyKnowledgeBase = () => {
  createDialogVisible.value = false;
  openEditDialog();
};

// 打开编辑对话框
const openEditDialog = (row?: KnowledgeBase) => {
  dialogTitle.value = row ? '编辑知识库' : '新增知识库';
  isEdit.value = !!row;
  if (row) {
    knowledgeBaseForm.id = row.id;
    knowledgeBaseForm.name = row.name;
    knowledgeBaseForm.code = row.code;
    knowledgeBaseForm.description = row.description || '';
    knowledgeBaseForm.visibility = row.visibility;
  } else {
    knowledgeBaseForm.id = undefined;
    knowledgeBaseForm.name = '';
    knowledgeBaseForm.code = '';
    knowledgeBaseForm.description = '';
    knowledgeBaseForm.visibility = 'public';
  }
  dialogVisible.value = true;
};

// 提交表单
const handleSubmit = async () => {
  if (!knowledgeBaseForm.name || !knowledgeBaseForm.code) {
    ElMessage.warning('请填写名称和编码');
    return;
  }

  const loadingInstance = ElLoading.service({
    lock: true,
    text: isEdit.value ? '更新中...' : '创建中...',
    background: 'rgba(0, 0, 0, 0.7)'
  });

  try {
    let response;
    if (isEdit.value && knowledgeBaseForm.id) {
      response = await knowledgeBaseApi.update({
        id: knowledgeBaseForm.id,
        name: knowledgeBaseForm.name,
        description: knowledgeBaseForm.description,
        visibility: knowledgeBaseForm.visibility,
      });
    } else {
      response = await knowledgeBaseApi.create({
        name: knowledgeBaseForm.name,
        code: knowledgeBaseForm.code,
        description: knowledgeBaseForm.description,
        visibility: knowledgeBaseForm.visibility,
      });
    }

    if (response && response.success) {
      ElMessage.success(isEdit.value ? '知识库更新成功' : '知识库创建成功');
      dialogVisible.value = false;
      fetchKnowledgeBases();
    } else {
      ElMessage.error(response?.errMessage || '操作失败');
    }
  } catch (error: any) {
    console.error('操作知识库出错:', error);
    ElMessage.error('操作知识库出错:' + error.message);
  } finally {
    loadingInstance.close();
  }
};

// 删除知识库
const handleDelete = (row: KnowledgeBase) => {
  if (!row.id) return;
  
  ElMessageBox.confirm('确定要删除这个知识库吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await knowledgeBaseApi.delete(row.id!);
      
      if (response && response.success) {
        ElMessage.success('知识库删除成功');
        fetchKnowledgeBases();
      } else {
        ElMessage.error('知识库删除失败');
      }
    } catch (error) {
      console.error('删除知识库出错:', error);
      ElMessage.error('删除知识库出错');
    }
  }).catch(() => {
    // 取消删除
  });
};

// 格式化数字
const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w';
  }
  return num.toString();
};

// 外部知识库 API
const handleExternalAPI = () => {
  ElMessage.info('该功能暂未实现');
};

// 连接外部知识库
const handleConnectExternal = () => {
  ElMessage.info('该功能暂未实现');
};

// 计算是否可以进行下一步
const canProceed = computed(() => {
  if (currentStep.value === 1) {
    return uploadedFiles.value.length > 0;
  }
  return true;
});

// 处理知识库卡片点击
const handleCardClick = (kb: KnowledgeBase) => {
  router.push(`/dashboard/knowledge/base/${kb.id}/contents`);
};

// 预览块功能
const handlePreviewChunks = async () => {
  if (uploadedFiles.value.length === 0) {
    ElMessage.warning('请先上传文件');
    return;
  }

  previewLoading.value = true;
  previewError.value = '';
  previewChunks.value = [];
  uploadProgress.value = '';
  chunkProgress.value = '';

  try {
    // 第一步：上传文件获取URL
    uploadProgress.value = '正在上传文件...';
    const fileUrl = await uploadDocument(uploadedFiles.value[0]);
    uploadProgress.value = '文件上传完成';
    
    // 第二步：调用分块API
    chunkProgress.value = '开始文档分块处理...';
    
    let response;
    
    if (segmentMode.value === 'general') {
      // 通用分块
      const chunkRequest = {
        fileUrl: fileUrl,
        chunkSize: segmentSettings.maxLength,
        overlapSize: segmentSettings.overlapLength,
        chunkSeparator: segmentSettings.identifier,
        minChunkSize: 10,
        maxChunkSize: 5000,
        keepSeparator: true,
        stripWhitespace: segmentSettings.replaceConsecutiveSpaces,
        removeAllUrls: segmentSettings.removeAllUrls,
        useQASegmentation: segmentSettings.useQASegmentation,
        qaLanguage: segmentSettings.qaLanguage
      };
      
      chunkProgress.value = '执行通用分块策略...';
      response = await knowledgeBaseApi.generalChunk(chunkRequest);
    } else {
      // 父子分块 - 暂时使用formData方式，等后续统一修改
      const formData = new FormData();
      formData.append('file', uploadedFiles.value[0]);
      formData.append('parentChunkSize', parentChildSettings.parentMaxLength.toString());
      formData.append('childChunkSize', parentChildSettings.childMaxLength.toString());
      formData.append('contextParagraphs', '3');
      formData.append('parentOverlapSize', '200');
      formData.append('childOverlapSize', '100');
      formData.append('chunkSeparator', parentChildSettings.parentIdentifier);
      formData.append('minChunkSize', '10');
      formData.append('maxChunkSize', '10000');
      formData.append('keepSeparator', 'true');
      formData.append('stripWhitespace', parentChildSettings.replaceConsecutiveSpaces.toString());
      formData.append('removeAllUrls', parentChildSettings.removeAllUrls.toString());
      
      chunkProgress.value = '执行父子分块策略...';
      response = await knowledgeBaseApi.parentChildChunk(formData);
    }

    if (response && response.success) {
      previewChunks.value = response.data || [];
      chunkProgress.value = `分块处理完成，生成 ${previewChunks.value.length} 个分块`;
      ElMessage.success(`预览成功，共生成 ${previewChunks.value.length} 个分块`);
    } else {
      previewError.value = response?.errMessage || '分块处理失败';
      ElMessage.error(previewError.value);
    }
  } catch (error: any) {
    console.error('预览分块失败:', error);
    
    // 更详细的错误处理
    let errorMessage = '未知错误';
    if (error.message) {
      errorMessage = error.message;
    } else if (error.response?.data?.errMessage) {
      errorMessage = error.response.data.errMessage;
    } else if (error.response?.statusText) {
      errorMessage = `请求失败: ${error.response.status} ${error.response.statusText}`;
    }
    
    previewError.value = '处理失败: ' + errorMessage;
    ElMessage.error(previewError.value);
    
    // 根据错误类型给出具体提示
    if (errorMessage.includes('upload') || errorMessage.includes('文件上传')) {
      uploadProgress.value = '文件上传失败';
    } else {
      chunkProgress.value = '分块处理失败';
    }
  } finally {
    previewLoading.value = false;
  }
};



// 页面加载时获取数据
onMounted(() => {
  fetchKnowledgeBases();
});
</script>

<template>
  <div class="knowledge-base-management">
    <!-- Tab 栏 -->
    <div class="tabs-container">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="知识库" name="knowledge"></el-tab-pane>
        <el-tab-pane label="API" name="api"></el-tab-pane>
      </el-tabs>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-dropdown trigger="click" class="filter-dropdown">
          <el-button>
            <el-icon><Filter /></el-icon>
            筛选
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-for="option in visibilityOptions" :key="option.value" @click="searchParams.visibility = option.value">
                {{ option.label }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        
        <div class="tag-filter">
          <span class="tag-label">标签</span>
          <el-tag>全部标签</el-tag>
        </div>
      </div>
      
      <div class="toolbar-right">
        <el-input
          v-model="searchParams.name"
          placeholder="搜索"
          class="search-input"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-button @click="handleExternalAPI">
          <el-icon><Link /></el-icon>
          外部知识库 API
        </el-button>
      </div>
    </div>

    <!-- 知识库卡片列表 -->
    <div v-loading="loading" class="knowledge-base-grid">
      <!-- 创建知识库卡片 -->
      <div class="knowledge-card create-card" @click="openCreateDialog">
        <div class="create-icon">
          <el-icon :size="32"><Plus /></el-icon>
        </div>
        <h3>创建知识库</h3>
        <p>导入您自己的文本数据或通过 Webhook 实时写入数据以增强 LLM 的上下文。</p>
      </div>
      
      <!-- 知识库卡片 -->
      <div v-for="kb in tableData" :key="kb.id" class="knowledge-card" @click="handleCardClick(kb)">
        <div class="card-header">
          <el-icon :size="24" class="folder-icon"><Document /></el-icon>
          <h3>{{ kb.name }}</h3>
        </div>
        
        <div class="card-stats">
          <div class="stat-item">
            <span class="stat-value">{{ formatNumber(kb.documentCount || 0) }}</span>
            <span class="stat-label">文档</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ formatNumber(kb.charCount || 0) }}</span>
            <span class="stat-label">字符</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ kb.associatedApps || 0 }}</span>
            <span class="stat-label">关联应用</span>
          </div>
        </div>
        
        <p class="card-description">{{ kb.description || '暂无描述' }}</p>
        
        <div class="card-actions">
          <el-button type="primary" size="small" @click="openEditDialog(kb)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(kb)">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </div>
      </div>
    </div>

    <!-- 连接外部知识库 -->
    <div class="external-link">
      <a href="javascript:;" @click="handleConnectExternal">连接外部知识库 →</a>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="searchParams.pageIndex"
        v-model:page-size="searchParams.pageSize"
        :page-sizes="[12, 24, 48, 96]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalCount"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 创建知识库弹窗 -->
    <el-dialog
      v-model="createDialogVisible"
      title="创建知识库"
      width="60%"
      :close-on-click-modal="false"
    >
      <div class="create-dialog-content">
        <!-- 步骤条 -->
        <el-steps :active="currentStep" align-center>
          <el-step title="选择数据源" />
          <el-step title="文本分段与清洗" />
          <el-step title="处理并完成" />
        </el-steps>
        
        <!-- 第一步：数据源选择 -->
        <div v-if="currentStep === 1" class="step-content">
          <div class="data-source-section">
            <h3>选择数据源</h3>
            <div class="data-source-options">
              <div
                v-for="source in dataSourceOptions"
                :key="source.value"
                :class="['data-source-card', { active: selectedDataSource === source.value }]"
                @click="handleDataSourceSelect(source.value)"
              >
                <el-icon :size="20"><component :is="source.icon" /></el-icon>
                <span>{{ source.label }}</span>
              </div>
            </div>
          </div>
          
          <!-- 文件上传区域 -->
          <div v-if="selectedDataSource === 'text'" class="upload-section">
            <h3>上传文本文件</h3>
            <el-upload
              class="upload-area"
              drag
              multiple
              :auto-upload="false"
              :on-change="(file: any) => handleFileUpload(file.raw)"
              :show-file-list="false"
            >
              <el-icon class="el-icon--upload"><Upload /></el-icon>
              <div class="el-upload__text">
                拖拽文件至此，或者 <em>选择文件</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  已支持 {{ supportedFormats.join('、') }}、每个文件不超过 15MB。
                </div>
              </template>
            </el-upload>
            
            <!-- 已上传文件列表 -->
            <div v-if="uploadedFiles.length > 0" class="uploaded-files">
              <div v-for="(file, index) in uploadedFiles" :key="index" class="file-item">
                <span>{{ file.name }}</span>
                <el-button type="danger" size="small" text @click="handleFileRemove(index)">删除</el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 第二步：文本分段与清洗 -->
        <div v-if="currentStep === 2" class="step-content">
          <div class="step-two-layout">
            <div class="settings-panel">
              <!-- 分段设置 -->
              <div class="setting-section">
                <h3>分段设置</h3>
                
                <!-- 分段模式选择 -->
                <div class="segment-mode-selection">
                  <div 
                    :class="['segment-mode-card', { active: segmentMode === 'general' }]"
                    @click="segmentMode = 'general'"
                  >
                    <div class="mode-header">
                      <el-icon><Document /></el-icon>
                      <span>通用</span>
                    </div>
                    <p>通用文本分段模式，检索和召回的快速相关的</p>
                  </div>
                  
                  <div 
                    :class="['segment-mode-card', { active: segmentMode === 'parent_child' }]"
                    @click="segmentMode = 'parent_child'"
                  >
                    <div class="mode-header">
                      <el-icon color="#f56c6c"><Document /></el-icon>
                      <span>父子分段</span>
                    </div>
                    <p>使用父子模式时，子块用于检索，父块用作上下文</p>
                  </div>
                </div>
                
                <!-- 通用分段设置 -->
                <div v-if="segmentMode === 'general'" class="segment-settings">
                  <div class="setting-item">
                    <label>分段标识符</label>
                    <el-input v-model="segmentSettings.identifier" placeholder="\n\n" />
                  </div>
                  
                  <div class="setting-row">
                    <div class="setting-item">
                      <label>分段最大长度</label>
                      <div class="input-with-unit">
                        <el-input-number v-model="segmentSettings.maxLength" :min="1" :max="4000" />
                        <span class="unit-label">tokens</span>
                      </div>
                    </div>
                    
                    <div class="setting-item">
                      <label>分段重叠长度</label>
                      <div class="input-with-unit">
                        <el-input-number v-model="segmentSettings.overlapLength" :min="0" :max="1000" />
                        <span class="unit-label">tokens</span>
                      </div>
                    </div>
                  </div>
                  
                  <div class="setting-item">
                    <label>文本预处理规则</label>
                    <div class="checkbox-group">
                      <el-checkbox v-model="segmentSettings.replaceConsecutiveSpaces">
                        替换连续的空格、换行符制表符
                      </el-checkbox>
                      <el-checkbox v-model="segmentSettings.removeAllUrls">
                        删除所有 URL 和电子邮件地址
                      </el-checkbox>
                    </div>
                  </div>
                  
                  <div class="setting-item">
                    <el-checkbox v-model="segmentSettings.useQASegmentation">
                      使用 Q&A 分段，适合
                    </el-checkbox>
                    <el-select v-model="segmentSettings.qaLanguage" style="margin-left: 8px;">
                      <el-option label="Chinese" value="Chinese" />
                      <el-option label="English" value="English" />
                    </el-select>
                  </div>
                  
                  <div class="preview-section">
                    <div class="preview-tabs">
                      <el-button type="primary" size="small" :loading="previewLoading" @click="handlePreviewChunks">预览块</el-button>
                      <el-button size="small" @click="previewChunks = []; previewError = ''">重置</el-button>
                    </div>
                  </div>
                </div>
                
                <!-- 父子分段设置 -->
                <div v-if="segmentMode === 'parent_child'" class="segment-settings">
                  <div class="parent-child-section">
                    <h4>父块用作上下文</h4>
                    
                    <div class="segment-method">
                      <div class="method-option active">
                        <el-icon><Document /></el-icon>
                        <div>
                          <h5>段落</h5>
                          <p>此模式根据段落和最大长度将文本分为父块，使用拆分文本作为构建的父块</p>
                        </div>
                      </div>
                      
                      <div class="method-option">
                        <el-icon><Document /></el-icon>
                        <div>
                          <h5>全文</h5>
                          <p>整个文档用作父块并直接检索。请注意，出于性能原因，超过10000个标记的文本将被动态。</p>
                        </div>
                      </div>
                    </div>
                    
                    <div class="parent-settings">
                      <div class="setting-row">
                        <div class="setting-item">
                          <label>分段标识符</label>
                          <el-input v-model="parentChildSettings.parentIdentifier" placeholder="\n\n" />
                        </div>
                        <div class="setting-item">
                          <label>分段最大长度</label>
                          <div class="input-with-unit">
                            <el-input-number v-model="parentChildSettings.parentMaxLength" :min="1" :max="4000" />
                            <span class="unit-label">tokens</span>
                          </div>
                        </div>
                      </div>
                      
                      <h5>子块用于检索</h5>
                      <div class="setting-row">
                        <div class="setting-item">
                          <label>分段标识符</label>
                          <el-input v-model="parentChildSettings.childIdentifier" placeholder="\n" />
                        </div>
                        <div class="setting-item">
                          <label>分段最大长度</label>
                          <div class="input-with-unit">
                            <el-input-number v-model="parentChildSettings.childMaxLength" :min="1" :max="4000" />
                            <span class="unit-label">tokens</span>
                          </div>
                        </div>
                      </div>
                      
                      <div class="setting-item">
                        <label>文本预处理规则</label>
                        <div class="checkbox-group">
                          <el-checkbox v-model="parentChildSettings.replaceConsecutiveSpaces">
                            替换连续的空格、换行符和制表符
                          </el-checkbox>
                          <el-checkbox v-model="parentChildSettings.removeAllUrls">
                            删除所有 URL 和电子邮件地址
                          </el-checkbox>
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  <div class="preview-section">
                    <div class="preview-tabs">
                      <el-button type="primary" size="small" :loading="previewLoading" @click="handlePreviewChunks">预览块</el-button>
                      <el-button size="small" @click="previewChunks = []; previewError = ''">重置</el-button>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 索引方式 -->
              <div class="setting-section">
                <h3>索引方式</h3>
                <div class="index-method-card">
                  <div class="method-header">
                    <el-icon color="#f56c6c"><Document /></el-icon>
                    <div>
                      <h4>高质量 <span class="recommended">推荐</span></h4>
                      <p>通过嵌入模型处理文档以实现更精确的检索，可以帮助 LLM 获得高质量的答案。</p>
                    </div>
                  </div>
                </div>
                
                <div class="embedding-section">
                  <h4>Embedding 模型</h4>
                  <div class="embedding-model">
                    <el-icon><Document /></el-icon>
                    <span>text-embedding-v1</span>
                  </div>
                  <p>更多改索引方法和 embedding 模型，请转到<a href="#" @click.prevent="ElMessage.info('该功能暂未实现')">知识库设置</a>。</p>
                </div>
              </div>
              
              <!-- 检索设置 -->
              <div class="setting-section">
                <h3>检索设置</h3>
                <div class="retrieval-card">
                  <div class="retrieval-header">
                    <el-icon><Search /></el-icon>
                    <span>向量检索</span>
                  </div>
                  <p>通过生成查询嵌入以并与文档块的嵌入进行相似性匹配，从而实现语义的文本分段</p>
                  
                  <div class="retrieval-options">
                    <div class="option-item">
                      <el-checkbox v-model="retrievalSettings.fullTextSearch">Rerank 模型</el-checkbox>
                      <el-select v-model="retrievalSettings.rerankModel" style="margin-left: 8px;">
                        <el-option label="gte-rerank" value="gte-rerank" />
                      </el-select>
                    </div>
                    
                    <div class="option-row">
                      <div class="option-item">
                        <label>Top K</label>
                        <el-input-number v-model="retrievalSettings.topK" :min="1" :max="100" />
                      </div>
                      <div class="option-item">
                        <label>Score 阈值</label>
                        <el-slider v-model="retrievalSettings.scoreThreshold" :min="0" :max="1" :step="0.1" />
                      </div>
                    </div>
                  </div>
                </div>
                
                <div class="additional-options">
                  <div class="option-card">
                    <el-icon><Document /></el-icon>
                    <div>
                      <h4>全文检索</h4>
                      <p>索引文档中的所有词汇，从而允许用户查询任意词汇，并返回包含这些词汇的文本分段</p>
                    </div>
                  </div>
                  
                  <div class="option-card">
                    <el-icon><Search /></el-icon>
                    <div>
                      <h4>混合检索 <span class="beta">推荐</span></h4>
                      <p>同时执行全文检索和向量检索，并用重排算法选择匹配用户问题的最佳结果，用户可以选择重排算法或配置用户问题。</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="preview-panel">
              <div class="file-preview">
                <div class="file-info">
                  <el-icon><Document /></el-icon>
                  <span>{{ uploadedFiles[0]?.name || 'rule.txt' }}</span>
                  <span class="file-size">{{ previewChunks.length }} 预览块</span>
                </div>
                <div class="preview-content">
                  <!-- 预览加载状态 -->
                  <div v-if="previewLoading" class="preview-loading">
                    <el-icon class="rotating"><Loading /></el-icon>
                    <div class="loading-progress">
                      <p v-if="uploadProgress" class="progress-step">{{ uploadProgress }}</p>
                      <p v-if="chunkProgress" class="progress-step">{{ chunkProgress }}</p>
                      <p v-if="!uploadProgress && !chunkProgress">正在准备处理...</p>
                    </div>
                  </div>
                  
                  <!-- 预览错误状态 -->
                  <div v-else-if="previewError" class="preview-error">
                    <el-icon><Warning /></el-icon>
                    <p>{{ previewError }}</p>
                  </div>
                  
                  <!-- 预览块列表 -->
                  <div v-else-if="previewChunks.length > 0" class="chunks-list">
                    <div v-for="(chunk, index) in previewChunks" :key="index" class="chunk-item">
                      <div class="chunk-header">
                        <span class="chunk-index">块 {{ index + 1 }}</span>
                        <span class="chunk-size">{{ chunk.tokenSize || 0 }} tokens</span>
                      </div>
                      <div class="chunk-content">
                        {{ chunk.content }}
                      </div>
                      <div v-if="chunk.metadata" class="chunk-metadata">
                        <el-tag size="small" type="info">{{ chunk.metadata }}</el-tag>
                      </div>
                    </div>
                  </div>
                  
                  <!-- 默认占位符 -->
                  <div v-else class="preview-placeholder">
                    <el-icon><Search /></el-icon>
                    <p>点击左侧的"预览块"按钮来添加预览</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <div class="footer-left">
            <a v-if="currentStep === 1" href="javascript:;" @click="createEmptyKnowledgeBase">创建一个空知识库</a>
          </div>
          <div class="footer-right">
            <el-button v-if="currentStep > 1" @click="handleCreatePrev">上一步</el-button>
            <el-button @click="createDialogVisible = false">取消</el-button>
            <el-button 
              v-if="currentStep < 3" 
              type="primary" 
              :disabled="!canProceed" 
              @click="handleCreateNext"
            >
              下一步
            </el-button>
            <el-button 
              v-if="currentStep === 3" 
              type="primary" 
              @click="handleCreateNext"
            >
              保存并处理
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="50%"
    >
      <el-form label-width="100px">
        <el-form-item label="知识库名称">
          <el-input v-model="knowledgeBaseForm.name" placeholder="请输入知识库名称" />
        </el-form-item>
        <el-form-item label="知识库编码">
          <el-input 
            v-model="knowledgeBaseForm.code" 
            placeholder="请输入知识库编码" 
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="描述信息">
          <el-input 
            v-model="knowledgeBaseForm.description" 
            type="textarea"
            :rows="3"
            placeholder="请输入描述信息"
          />
        </el-form-item>
        <el-form-item label="可见性">
          <el-radio-group v-model="knowledgeBaseForm.visibility">
            <el-radio label="public">公开</el-radio>
            <el-radio label="private">私有</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.knowledge-base-management {
  min-height: 70vh;
}

/* Tab 样式 */
.tabs-container {
  margin-bottom: 20px;
}

.tabs-container :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.tabs-container :deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 500;
}

/* 工具栏样式 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px 0;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-dropdown {
  margin-right: 8px;
}

.tag-filter {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tag-label {
  color: #666;
  font-size: 14px;
}

.search-input {
  width: 240px;
}

/* 知识库卡片网格 */
.knowledge-base-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

/* 知识库卡片样式 */
.knowledge-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  min-height: 200px;
  display: flex;
  flex-direction: column;
}

.knowledge-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

/* 创建卡片样式 */
.create-card {
  border: 2px dashed #d1d5db;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.create-card:hover {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.create-icon {
  width: 64px;
  height: 64px;
  background: #e5e7eb;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.create-card:hover .create-icon {
  background: #409eff;
  color: #fff;
}

.create-card h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 500;
}

.create-card p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
  line-height: 1.5;
}

/* 知识库卡片内容 */
.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.folder-icon {
  color: #409eff;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-stats {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.stat-label {
  font-size: 12px;
  color: #6b7280;
}

.card-description {
  flex: 1;
  margin: 0 0 16px 0;
  color: #6b7280;
  font-size: 14px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-actions {
  display: flex;
  gap: 8px;
}

/* 外部链接 */
.external-link {
  text-align: center;
  margin-bottom: 24px;
}

.external-link a {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}

.external-link a:hover {
  text-decoration: underline;
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
}

/* 创建知识库弹窗样式 */
.create-dialog-content {
  padding: 20px 0;
}

.data-source-section {
  margin-top: 32px;
}

.data-source-section h3 {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 16px;
}

.data-source-options {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
}

.data-source-card {
  flex: 1;
  padding: 16px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.data-source-card:hover {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.data-source-card.active {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.upload-section h3 {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 16px;
}

.upload-area {
  width: 100%;
}

.upload-area :deep(.el-upload-dragger) {
  padding: 40px;
}

.uploaded-files {
  margin-top: 16px;
}

.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f3f4f6;
  border-radius: 4px;
  margin-bottom: 8px;
}

/* 弹窗底部样式 */
.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-left a {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}

.footer-left a:hover {
  text-decoration: underline;
}

.footer-right {
  display: flex;
  gap: 8px;
}

/* 第二步样式 */
.step-content {
  margin-top: 24px;
}

.step-two-layout {
  display: flex;
  gap: 24px;
  min-height: 600px;
}

.settings-panel {
  flex: 1;
  max-width: 600px;
}

.preview-panel {
  flex: 1;
  max-width: 400px;
}

.setting-section {
  margin-bottom: 32px;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
}

.setting-section h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 500;
}

/* 分段模式选择 */
.segment-mode-selection {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.segment-mode-card {
  flex: 1;
  padding: 16px;
  background: #fff;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.segment-mode-card:hover {
  border-color: #409eff;
}

.segment-mode-card.active {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.mode-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-weight: 500;
}

.segment-mode-card p {
  margin: 0;
  color: #666;
  font-size: 14px;
  line-height: 1.4;
}

/* 父子分段特殊样式 */
.parent-child-section h4 {
  margin: 0 0 16px 0;
  font-size: 14px;
  font-weight: 500;
}

.parent-child-section h5 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 500;
}

.segment-method {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.method-option {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.method-option:hover {
  border-color: #409eff;
}

.method-option.active {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.method-option h5 {
  margin: 0 0 4px 0;
  font-size: 14px;
}

.method-option p {
  margin: 0;
  color: #666;
  font-size: 12px;
  line-height: 1.4;
}

.parent-settings {
  margin-top: 16px;
}

.setting-item {
  margin-bottom: 16px;
}

.setting-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.setting-row {
  display: flex;
  gap: 16px;
}

.setting-row .setting-item {
  flex: 1;
}

.input-with-unit {
  display: flex;
  gap: 8px;
}

.input-with-unit .el-input-number {
  flex: 1;
}

.input-with-unit .el-select {
  width: 100px;
}

.unit-label {
  padding: 0 12px;
  background: #f5f5f5;
  border: 1px solid #dcdfe6;
  border-left: none;
  border-radius: 0 4px 4px 0;
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

.checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.preview-section {
  margin-top: 24px;
}

.preview-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.preview-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  border: 2px dashed #d1d5db;
  border-radius: 8px;
  color: #666;
}

.preview-placeholder p {
  margin-top: 8px;
  font-size: 14px;
}

.index-method-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.method-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.method-header h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
}

.method-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.recommended {
  background: #f0f9ff;
  color: #0ea5e9;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: normal;
}

.beta {
  background: #fef3c7;
  color: #d97706;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: normal;
}

.embedding-section {
  margin-top: 16px;
}

.embedding-section h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 500;
}

.embedding-model {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: #f3f4f6;
  border-radius: 4px;
  margin-bottom: 8px;
}

.embedding-section p {
  margin: 0;
  font-size: 12px;
  color: #666;
}

.embedding-section a {
  color: #409eff;
  text-decoration: none;
}

.retrieval-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.retrieval-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-weight: 500;
}

.retrieval-options {
  margin-top: 16px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.option-item label {
  margin: 0;
  min-width: 80px;
  font-size: 14px;
}

.option-row {
  display: flex;
  gap: 16px;
}

.option-row .option-item {
  flex: 1;
}

.additional-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.option-card h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
}

.option-card p {
  margin: 0;
  color: #666;
  font-size: 12px;
  line-height: 1.4;
}

.file-preview {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  height: 100%;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.file-size {
  color: #666;
  font-size: 12px;
}

.preview-content {
  padding: 16px;
  height: calc(100% - 60px);
}

.file-preview .preview-placeholder {
  height: 100%;
  border: 2px dashed #d1d5db;
  border-radius: 8px;
  margin: 0;
}

/* 预览块相关样式 */
.preview-loading,
.preview-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #666;
}

.loading-progress {
  text-align: center;
  margin-top: 16px;
}

.progress-step {
  margin: 8px 0;
  font-size: 14px;
  color: #409eff;
  line-height: 1.4;
}

.preview-loading .rotating {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.preview-error {
  color: #f56c6c;
}

.chunks-list {
  height: 100%;
  overflow-y: auto;
  padding: 8px;
}

.chunk-item {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  margin-bottom: 12px;
  padding: 12px;
}

.chunk-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
}

.chunk-index {
  font-weight: 500;
  color: #409eff;
}

.chunk-size {
  color: #666;
}

.chunk-content {
  font-size: 14px;
  line-height: 1.5;
  color: #333;
  margin-bottom: 8px;
  max-height: 120px;
  overflow-y: auto;
  word-break: break-word;
}

.chunk-metadata {
  margin-top: 8px;
}
</style> 