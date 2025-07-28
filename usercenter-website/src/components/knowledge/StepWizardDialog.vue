<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue';
import { ElMessage, ElLoading } from 'element-plus';
import { Search, Plus, Edit, Delete, Upload, Document, Link, Filter, Loading, Warning, Check } from '@element-plus/icons-vue';
import { uploadDocument } from '@/api/client-web/file';
import { knowledgeBaseApi } from '@/api/smartcs/knowledgeBase';
import { contentApi } from '@/api/smartcs/content';
import { modelApi, ModelType, ModelStatus, type Model } from '@/api/smartcs/model';
import { useRouter } from 'vue-router';

// Props 定义
interface StepWizardProps {
  visible: boolean;
  title: string;
  mode: 'knowledge-base' | 'document';
  knowledgeBaseId?: number;
  editMode?: boolean;
  editData?: any;
}

const props = withDefaults(defineProps<StepWizardProps>(), {
  title: '创建',
  mode: 'knowledge-base',
  editMode: false
});

// Emits 定义
const emit = defineEmits<{
  'update:visible': [value: boolean];
  'submit': [data: any];
  'cancel': [];
}>();

// Router
const router = useRouter();

// 响应式数据
const currentStep = ref(1);
const selectedDataSource = ref('text');
const uploadedFiles = ref<File[]>([]);

// 模型相关数据
const selectedModelId = ref<number | null>(null);
const modelOptions = ref<Model[]>([]);
const modelLoading = ref(false);
const modelRequest = reactive({
  modelId: null as number | null,
  modelName: '',
  temperature: 0.5,
  topP: null as number | null,
  topK: null as number | null,
  frequencyPenalty: 0.0,
  presencePenalty: 0.0,
  maxOutputTokens: 1024
});

// 分段设置
const segmentMode = ref('general');
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

// 文件缓存相关
const uploadedFileUrls = ref<Map<string, string>>(new Map());
const cacheStatus = ref<Map<string, 'uploading' | 'cached' | 'processing'>>(new Map());

// 数据源选项
const dataSourceOptions = [
  { value: 'text', label: '导入已有文本', icon: Document },
  { value: 'notion', label: '同步自 Notion 内容', icon: Link },
  { value: 'web', label: '同步自 Web 站点', icon: Link },
];

// 支持的文件格式
const supportedFormats = ['TXT', 'MARKDOWN', 'MDX', 'PDF', 'HTML', 'XLSX', 'XLS', 'DOCX', 'CSV', 'MD', 'HTM'];

// 计算属性
const canProceed = computed(() => {
  if (currentStep.value === 1) {
    return props.editMode || uploadedFiles.value.length > 0;
  }
  if (currentStep.value === 2) {
    // 第二步需要选择模型
    return selectedModelId.value !== null;
  }
  return true;
});

// 工具函数
const getFileIdentifier = (file: File): string => {
  return `${file.name}_${file.size}_${file.lastModified}`;
};

// 获取模型列表
const fetchModelList = async () => {
  try {
    modelLoading.value = true;
    const response = await modelApi.getPage({
      pageSize: 1000, // 获取所有模型
      status: ModelStatus.ACTIVE, // 只获取激活状态的模型
      modelType: [ModelType.LLM] // 只获取LLM类型的模型
    });
    
    if (response.success && response.data) {
      modelOptions.value = response.data;
      
      // 如果当前没有选中的模型且有可用模型，选择第一个
      if (!selectedModelId.value && response.data.length > 0) {
        const firstModel = response.data[0];
        selectedModelId.value = firstModel.id!;
        modelRequest.modelId = firstModel.id!;
        modelRequest.modelName = firstModel.label;
      }
    }
  } catch (error) {
    console.error('获取模型列表失败:', error);
    ElMessage.error('获取模型列表失败');
  } finally {
    modelLoading.value = false;
  }
};

// 处理模型选择变化
const handleModelChange = (modelId: number) => {
  const selectedModel = modelOptions.value.find(model => model.id === modelId);
  if (selectedModel) {
    modelRequest.modelId = modelId;
    modelRequest.modelName = selectedModel.label;
  }
};

const getCacheStatusText = (fileId: string): string => {
  const status = cacheStatus.value.get(fileId);
  switch (status) {
    case 'uploading': return '上传中';
    case 'cached': return '已缓存';
    case 'processing': return '处理中';
    default: return '';
  }
};

const getCacheTagType = (fileId: string): string => {
  const status = cacheStatus.value.get(fileId);
  switch (status) {
    case 'uploading': return 'warning';
    case 'cached': return 'success';
    case 'processing': return 'info';
    default: return '';
  }
};

// 监听弹窗显示状态
watch(() => props.visible, async (newVal) => {
  if (newVal) {
    // 获取模型列表
    await fetchModelList();
    
    if (props.editMode && props.editData) {
      // 编辑模式：直接进入第二步，加载现有数据
      currentStep.value = 2;
      loadEditData();
    } else {
      // 创建模式：重置状态
      currentStep.value = 1;
      selectedDataSource.value = 'text';
      uploadedFiles.value = [];
      previewChunks.value = [];
      previewError.value = '';
      uploadProgress.value = '';
      chunkProgress.value = '';
      
      // 清理缓存
      uploadedFileUrls.value.clear();
      cacheStatus.value.clear();
    }
  }
});

// 加载编辑数据
const loadEditData = () => {
  if (props.editData) {
    // 加载分段设置
    segmentMode.value = props.editData.segmentMode || 'general';
    
    if (props.editData.segmentSettings) {
      Object.assign(segmentSettings, props.editData.segmentSettings);
    }
    
    if (props.editData.parentChildSettings) {
      Object.assign(parentChildSettings, props.editData.parentChildSettings);
    }
  }
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
  const file = uploadedFiles.value[index];
  const fileId = getFileIdentifier(file);
  
  // 清理对应的缓存
  uploadedFileUrls.value.delete(fileId);
  cacheStatus.value.delete(fileId);
  
  uploadedFiles.value.splice(index, 1);
};

// 下一步
const handleNext = () => {
  if (currentStep.value === 1) {
    if (uploadedFiles.value.length === 0) {
      ElMessage.warning('请先上传文件');
      return;
    }
    currentStep.value = 2;
  } else if (currentStep.value === 2) {
    if (!selectedModelId.value) {
      ElMessage.warning('请先选择模型');
      return;
    }
    currentStep.value = 3;
  } else {
    handleSubmit();
  }
};

// 上一步
const handlePrev = () => {
  if (currentStep.value > 1) {
    currentStep.value--;
  }
};

// 取消
const handleCancel = () => {
  emit('update:visible', false);
  emit('cancel');
};

// 预览块功能
const handlePreviewChunks = async () => {
  if (!props.editMode && uploadedFiles.value.length === 0) {
    ElMessage.warning('请先上传文件');
    return;
  }
  
  if (!selectedModelId.value) {
    ElMessage.warning('请先选择模型');
    return;
  }

  previewLoading.value = true;
  previewError.value = '';
  previewChunks.value = [];
  uploadProgress.value = '';
  chunkProgress.value = '';

  try {
    let fileUrl: string;
    
    if (props.editMode && props.editData) {
      // 编辑模式：使用现有文档的文件URL
      fileUrl = props.editData.fileUrl;
      uploadProgress.value = '使用现有文档';
      chunkProgress.value = '开始文档分块处理...';
    } else {
      // 创建模式：上传文件
      const file = uploadedFiles.value[0];
      const fileId = getFileIdentifier(file);

      // 检查文件是否已缓存
      if (uploadedFileUrls.value.has(fileId)) {
        fileUrl = uploadedFileUrls.value.get(fileId)!;
        uploadProgress.value = '使用缓存文件';
        cacheStatus.value.set(fileId, 'cached');
      } else {
        // 第一步：上传文件获取URL
        uploadProgress.value = '正在上传文件...';
        cacheStatus.value.set(fileId, 'uploading');
        
        fileUrl = await uploadDocument(file);
        
        // 缓存文件URL
        uploadedFileUrls.value.set(fileId, fileUrl);
        uploadProgress.value = '文件上传完成';
        cacheStatus.value.set(fileId, 'cached');
      }
      
      // 第二步：调用分块API
      chunkProgress.value = '开始文档分块处理...';
      cacheStatus.value.set(fileId, 'processing');
    }
    
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
        qaLanguage: segmentSettings.qaLanguage,
        modelRequest: modelRequest
      };
      
      chunkProgress.value = '执行通用分块策略...';
      response = await knowledgeBaseApi.generalChunk(chunkRequest);
    } else {
      // 父子分块
      const chunkRequest = {
        fileUrl: fileUrl,
        parentChunkSize: parentChildSettings.parentMaxLength,
        parentChunkSeparator: parentChildSettings.parentIdentifier,
        childChunkSize: parentChildSettings.childMaxLength,
        childChunkSeparator: parentChildSettings.childIdentifier,
        minChunkSize: 10,
        maxChunkSize: 5000,
        keepSeparator: true,
        stripWhitespace: parentChildSettings.replaceConsecutiveSpaces,
        removeAllUrls: parentChildSettings.removeAllUrls,
        modelRequest: modelRequest
      };
      
      chunkProgress.value = '执行父子分块策略...';
      response = await knowledgeBaseApi.parentChildChunk(chunkRequest as any);
    }
    
    if (response && response.success) {
      previewChunks.value = response.data || [];
      chunkProgress.value = '分块处理完成';
      if (!props.editMode) {
        const file = uploadedFiles.value[0];
        const fileId = getFileIdentifier(file);
        cacheStatus.value.set(fileId, 'cached');
      }
      ElMessage.success('预览块生成成功');
    } else {
      previewError.value = response?.errMessage || '分块处理失败';
      ElMessage.error('预览块生成失败');
    }
  } catch (error: any) {
    console.error('预览块出错:', error);
    previewError.value = error.message || '预览块处理出错';
    ElMessage.error('预览块处理出错');
  } finally {
    previewLoading.value = false;
  }
};

// 处理状态管理
const processingStatus = ref<'idle' | 'uploading' | 'chunking' | 'vectorizing' | 'completed' | 'error'>('idle');
const processingProgress = ref(0);
const processingError = ref('');
const processingResults = ref<any>(null);

// 提交数据
const handleSubmit = async () => {
  if (processingStatus.value !== 'idle' && processingStatus.value !== 'error') {
    ElMessage.warning('正在处理中，请等待完成');
    return;
  }
  
  if (!selectedModelId.value) {
    ElMessage.warning('请先选择模型');
    return;
  }

  processingStatus.value = 'uploading';
  processingProgress.value = 0;
  processingError.value = '';
  processingResults.value = null;

  try {
    // 第一步：文件上传（如果需要）
    let fileUrl: string = '';
    if (!props.editMode && uploadedFiles.value.length > 0) {
      processingProgress.value = 10;
      const file = uploadedFiles.value[0]; // 目前只处理第一个文件
      const fileId = getFileIdentifier(file);
      
      // 检查缓存
      if (uploadedFileUrls.value.has(fileId)) {
        fileUrl = uploadedFileUrls.value.get(fileId)!;
      } else {
        // 上传文件
        fileUrl = await uploadDocument(file);
        uploadedFileUrls.value.set(fileId, fileUrl);
      }
    } else if (props.editMode && props.editData?.fileUrl) {
      fileUrl = props.editData.fileUrl;
    }

    processingProgress.value = 30;
    processingStatus.value = 'chunking';

    // 第二步：调用新的文档处理API
    const processData = {
      knowledgeBaseId: props.knowledgeBaseId!,
      title: props.editMode ? props.editData?.title : uploadedFiles.value[0]?.name || '新文档',
      fileUrl: fileUrl,
      fileType: props.editMode ? props.editData?.fileType : (uploadedFiles.value[0]?.name.split('.').pop() || ''),
      fileSize: props.editMode ? props.editData?.fileSize || 0 : uploadedFiles.value[0]?.size || 0,
      segmentMode: segmentMode.value as 'general' | 'parent_child',
      segmentSettings: segmentMode.value === 'general' ? segmentSettings : {},
      parentChildSettings: segmentMode.value === 'parent_child' ? parentChildSettings : undefined,
      indexMethod: indexMethod.value,
      retrievalSettings: retrievalSettings,
      editMode: props.editMode || false,
      editData: props.editData,
      modelId: modelRequest.modelId,
      modelRequest: modelRequest
    };

    processingProgress.value = 50;
    processingStatus.value = 'vectorizing';

    // 调用新的 processDocument API
    const response = await contentApi.processDocument(processData);
    
    if (response && response.success) {
      processingProgress.value = 100;
      processingStatus.value = 'completed';
      processingResults.value = response.data;
      
      ElMessage.success('文档处理完成');
      
      // 处理成功后的跳转逻辑
      if (response.data?.contentId) {
        // 延迟关闭弹窗，然后跳转到分块管理页面
        setTimeout(() => {
          emit('submit', {
            ...processData,
            results: response.data
          });
          emit('update:visible', false);
          resetProcessingState();
          
          // 跳转到分块管理页面
          if (response.data?.contentId) {
            navigateToChunkPage(response.data.contentId);
          }
        }, 2000);
      } else {
        // 没有 contentId 时，直接关闭弹窗
        setTimeout(() => {
          emit('submit', {
            ...processData,
            results: response.data
          });
          emit('update:visible', false);
          resetProcessingState();
        }, 2000);
      }
    } else {
      throw new Error(response?.errMessage || '文档处理失败');
    }

  } catch (error: any) {
    console.error('文档处理出错:', error);
    processingStatus.value = 'error';
    processingError.value = error.message || '文档处理出错';
    ElMessage.error('文档处理失败: ' + processingError.value);
  }
};

// 重置处理状态
const resetProcessingState = () => {
  processingStatus.value = 'idle';
  processingProgress.value = 0;
  processingError.value = '';
  processingResults.value = null;
};

// 跳转到分块管理页面
const navigateToChunkPage = (contentId: number) => {
  try {
    router.push({
      path: `/dashboard/knowledge/chunk/${contentId}`,
      query: {
        from: 'upload'
      }
    });
    ElMessage.success('已跳转到分块管理页面');
  } catch (error) {
    console.error('页面跳转失败:', error);
    ElMessage.warning('文档处理完成，但跳转失败，请手动查看分块列表');
  }
};

// 获取处理状态文本
const getProcessingStatusText = () => {
  switch (processingStatus.value) {
    case 'uploading': return '正在上传文件...';
    case 'chunking': return '正在分块处理...';
    case 'vectorizing': return '正在生成向量...';
    case 'completed': return '处理完成';
    case 'error': return '处理失败';
    default: return '准备中';
  }
};

// 获取进度详情
const getProgressDetail = () => {
  switch (processingStatus.value) {
    case 'uploading': return '文件上传中，请稍候...';
    case 'chunking': return '正在对文档进行分块处理，这可能需要一些时间...';
    case 'vectorizing': return '正在生成向量并存储到数据库...';
    default: return '';
  }
};
</script>

<template>
  <el-dialog
    :model-value="visible"
    :title="title"
    width="60%"
    :close-on-click-modal="false"
    @close="handleCancel"
    @update:model-value="(val: boolean) => emit('update:visible', val)"
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
        <div v-if="selectedDataSource === 'text' && !props.editMode" class="upload-section">
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
              <div class="file-info">
                <span class="file-name">{{ file.name }}</span>
                <div class="file-status">
                  <el-tag 
                    v-if="cacheStatus.has(getFileIdentifier(file))" 
                    :type="getCacheTagType(getFileIdentifier(file))"
                    size="small"
                  >
                    {{ getCacheStatusText(getFileIdentifier(file)) }}
                  </el-tag>
                </div>
              </div>
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
                
                <!-- 模型选择和参数配置 -->
                <div class="setting-item">
                  <label>模型配置</label>
                  <div class="model-config-section">
                    <div class="model-selection">
                      <label class="model-label">选择模型</label>
                      <el-select 
                        v-model="selectedModelId" 
                        placeholder="请选择模型"
                        :loading="modelLoading"
                        @change="handleModelChange"
                        style="width: 100%;"
                      >
                        <el-option
                          v-for="model in modelOptions"
                          :key="model.id"
                          :label="model.label"
                          :value="model.id"
                        />
                      </el-select>
                    </div>
                    
                    <div v-if="selectedModelId" class="model-parameters">
                      <div class="parameter-row">
                        <div class="parameter-item">
                          <label>温度 (Temperature)</label>
                          <el-slider 
                            v-model="modelRequest.temperature" 
                            :min="0" 
                            :max="2" 
                            :step="0.1" 
                            show-input
                            :show-input-controls="false"
                          />
                        </div>
                        <div class="parameter-item">
                          <label>Top P</label>
                          <el-input-number 
                            v-model="modelRequest.topP" 
                            :min="0" 
                            :max="1" 
                            :step="0.1" 
                            :precision="2"
                            placeholder="可选参数"
                          />
                        </div>
                      </div>
                      
                      <div class="parameter-row">
                        <div class="parameter-item">
                          <label>Top K</label>
                          <el-input-number 
                            v-model="modelRequest.topK" 
                            :min="1" 
                            :max="100" 
                            placeholder="可选参数"
                          />
                        </div>
                        <div class="parameter-item">
                          <label>最大输出Token</label>
                          <el-input-number 
                            v-model="modelRequest.maxOutputTokens" 
                            :min="1" 
                            :max="4096" 
                          />
                        </div>
                      </div>
                      
                      <div class="parameter-row">
                        <div class="parameter-item">
                          <label>频率惩罚 (Frequency Penalty)</label>
                          <el-slider 
                            v-model="modelRequest.frequencyPenalty" 
                            :min="0" 
                            :max="2" 
                            :step="0.1" 
                            show-input
                            :show-input-controls="false"
                          />
                        </div>
                        <div class="parameter-item">
                          <label>存在惩罚 (Presence Penalty)</label>
                          <el-slider 
                            v-model="modelRequest.presencePenalty" 
                            :min="0" 
                            :max="2" 
                            :step="0.1" 
                            show-input
                            :show-input-controls="false"
                          />
                        </div>
                      </div>
                    </div>
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
                    
                    <!-- 模型选择和参数配置 -->
                    <div class="setting-item">
                      <label>模型配置</label>
                      <div class="model-config-section">
                        <div class="model-selection">
                          <label class="model-label">选择模型</label>
                          <el-select 
                            v-model="selectedModelId" 
                            placeholder="请选择模型"
                            :loading="modelLoading"
                            @change="handleModelChange"
                            style="width: 100%;"
                          >
                            <el-option
                              v-for="model in modelOptions"
                              :key="model.id"
                              :label="model.label"
                              :value="model.id"
                            />
                          </el-select>
                        </div>
                        
                        <div v-if="selectedModelId" class="model-parameters">
                          <div class="parameter-row">
                            <div class="parameter-item">
                              <label>温度 (Temperature)</label>
                              <el-slider 
                                v-model="modelRequest.temperature" 
                                :min="0" 
                                :max="2" 
                                :step="0.1" 
                                show-input
                                :show-input-controls="false"
                              />
                            </div>
                            <div class="parameter-item">
                              <label>Top P</label>
                              <el-input-number 
                                v-model="modelRequest.topP" 
                                :min="0" 
                                :max="1" 
                                :step="0.1" 
                                :precision="2"
                                placeholder="可选参数"
                              />
                            </div>
                          </div>
                          
                          <div class="parameter-row">
                            <div class="parameter-item">
                              <label>Top K</label>
                              <el-input-number 
                                v-model="modelRequest.topK" 
                                :min="1" 
                                :max="100" 
                                placeholder="可选参数"
                              />
                            </div>
                            <div class="parameter-item">
                              <label>最大输出Token</label>
                              <el-input-number 
                                v-model="modelRequest.maxOutputTokens" 
                                :min="1" 
                                :max="4096" 
                              />
                            </div>
                          </div>
                          
                          <div class="parameter-row">
                            <div class="parameter-item">
                              <label>频率惩罚 (Frequency Penalty)</label>
                              <el-slider 
                                v-model="modelRequest.frequencyPenalty" 
                                :min="0" 
                                :max="2" 
                                :step="0.1" 
                                show-input
                                :show-input-controls="false"
                              />
                            </div>
                            <div class="parameter-item">
                              <label>存在惩罚 (Presence Penalty)</label>
                              <el-slider 
                                v-model="modelRequest.presencePenalty" 
                                :min="0" 
                                :max="2" 
                                :step="0.1" 
                                show-input
                                :show-input-controls="false"
                              />
                            </div>
                          </div>
                        </div>
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
                <span>{{ props.editMode && props.editData ? props.editData.title : (uploadedFiles[0]?.name || 'rule.txt') }}</span>
                <span class="file-size">{{ previewChunks.length }} 预览块</span>
              </div>
              <div class="preview-content">
                <!-- 预览加载状态 -->
                <div v-if="previewLoading" class="preview-loading">
                  <el-icon class="rotating"><Loading /></el-icon>
                  <div class="loading-progress">
                    <p v-if="uploadProgress" class="progress-step">
                      {{ uploadProgress }}
                      <el-tag v-if="uploadProgress === '使用缓存文件'" type="success" size="small" class="cache-indicator">缓存加速</el-tag>
                    </p>
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

      <!-- 第三步：处理并完成 -->
      <div v-if="currentStep === 3" class="step-content">
        <div class="completion-section">
          <h3>{{ props.editMode ? '更新并处理' : '处理并完成' }}</h3>
          
          <!-- 处理状态显示 -->
          <div v-if="processingStatus !== 'idle'" class="processing-status">
            <div class="status-header">
              <el-icon v-if="processingStatus === 'error'" class="status-icon error"><Warning /></el-icon>
              <el-icon v-else-if="processingStatus === 'completed'" class="status-icon success"><Check /></el-icon>
              <el-icon v-else class="status-icon loading rotating"><Loading /></el-icon>
              <span class="status-text">{{ getProcessingStatusText() }}</span>
            </div>
            
            <div v-if="processingStatus !== 'completed'" class="progress-container">
              <el-progress :percentage="Math.round(processingProgress)" :status="processingStatus === 'error' ? 'exception' : undefined" />
              <p class="progress-detail">{{ getProgressDetail() }}</p>
            </div>
            
            <div v-if="processingError" class="error-container">
              <p class="error-message">{{ processingError }}</p>
              <el-button size="small" @click="resetProcessingState">重试</el-button>
            </div>
            
            <div v-if="processingResults" class="results-container">
              <div class="results-summary">
                <div class="result-item">
                  <span class="label">处理完成</span>
                  <span class="value">{{ processingResults.contentCount || 0 }} 个内容</span>
                </div>
                <div class="result-item">
                  <span class="label">生成分块</span>
                  <span class="value">{{ processingResults.chunkCount || 0 }} 个分块</span>
                </div>
                <div class="result-item">
                  <span class="label">向量化</span>
                  <span class="value">完成</span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 基本信息摘要 -->
          <div v-else class="completion-summary">
            <div class="summary-item">
              <span class="label">{{ props.editMode ? '文档标题：' : '已上传文件：' }}</span>
              <span class="value">{{ props.editMode && props.editData ? props.editData.title : (uploadedFiles.length + ' 个文件') }}</span>
            </div>
            <div class="summary-item">
              <span class="label">分段模式：</span>
              <span class="value">{{ segmentMode === 'general' ? '通用分段' : '父子分段' }}</span>
            </div>
            <div class="summary-item">
              <span class="label">索引方式：</span>
              <span class="value">高质量索引</span>
            </div>
            <div class="summary-item">
              <span class="label">检索方式：</span>
              <span class="value">向量检索</span>
            </div>
          </div>
          
          <div v-if="processingStatus === 'idle'" class="completion-tip">
            <el-icon><Warning /></el-icon>
            <p>点击"{{ props.editMode ? '保存并更新' : '保存并处理' }}"后将{{ props.editMode ? '更新文档设置并重新处理文档' : '开始处理文档' }}，处理时间取决于文档大小和复杂度。</p>
          </div>
        </div>
      </div>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <div class="footer-left">
          <a v-if="currentStep === 1 && mode === 'knowledge-base'" href="javascript:;" @click="handleCancel">创建一个空知识库</a>
        </div>
        <div class="footer-right">
          <el-button v-if="currentStep > 1" @click="handlePrev">上一步</el-button>
          <el-button @click="handleCancel">取消</el-button>
          <el-button 
            v-if="currentStep < 3" 
            type="primary" 
            :disabled="!canProceed" 
            @click="handleNext"
          >
            下一步
          </el-button>
          <el-button 
            v-if="currentStep === 3" 
            type="primary" 
            :loading="processingStatus !== 'idle' && processingStatus !== 'error' && processingStatus !== 'completed'"
            :disabled="processingStatus === 'completed'"
            @click="handleNext"
          >
            {{ processingStatus === 'completed' ? '已完成' : (props.editMode ? '保存并更新' : '保存并处理') }}
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
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

/* 数据源选择 */
.data-source-section {
  margin-bottom: 24px;
}

.data-source-section h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 500;
}

.data-source-options {
  display: flex;
  gap: 16px;
}

.data-source-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px;
  background: #fff;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.data-source-card:hover {
  border-color: #409eff;
}

.data-source-card.active {
  border-color: #409eff;
  background-color: #f0f9ff;
}

/* 文件上传 */
.upload-section {
  margin-top: 24px;
}

.upload-section h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 500;
}

.upload-area {
  width: 100%;
}

.uploaded-files {
  margin-top: 16px;
}

.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 8px;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.file-name {
  flex: 1;
}

.file-status {
  display: flex;
  align-items: center;
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

/* 分段设置 */
.segment-settings {
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

.input-with-unit {
  display: flex;
  align-items: center;
  gap: 8px;
}

.unit-label {
  color: #666;
  font-size: 14px;
}

.checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.preview-section {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.preview-tabs {
  display: flex;
  gap: 8px;
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

/* 索引方式 */
.index-method-card {
  padding: 16px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  margin-bottom: 16px;
}

.method-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.method-header h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 500;
}

.method-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
  line-height: 1.4;
}

.recommended {
  background: #67c23a;
  color: #fff;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  margin-left: 8px;
}

.embedding-section {
  margin-top: 16px;
}

.embedding-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 500;
}

.embedding-model {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 8px;
}

.embedding-section p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.embedding-section a {
  color: #409eff;
  text-decoration: none;
}

/* 检索设置 */
.retrieval-card {
  padding: 16px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  margin-bottom: 16px;
}

.retrieval-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-weight: 500;
}

.retrieval-card p {
  margin: 0 0 16px 0;
  color: #666;
  font-size: 14px;
  line-height: 1.4;
}

.retrieval-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.option-item label {
  margin: 0;
  font-weight: 500;
  min-width: 80px;
}

.option-row {
  display: flex;
  gap: 16px;
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
  padding: 12px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
}

.option-card h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 500;
}

.option-card p {
  margin: 0;
  color: #666;
  font-size: 12px;
  line-height: 1.4;
}

.beta {
  background: #e6a23c;
  color: #fff;
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 10px;
  margin-left: 4px;
}

/* 预览面板 */
.file-preview {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #e5e7eb;
}

.file-size {
  margin-left: auto;
  color: #666;
  font-size: 12px;
}

.preview-content {
  height: 500px;
  overflow-y: auto;
  padding: 16px;
}

.preview-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 16px;
}

.rotating {
  animation: rotate 2s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.loading-progress {
  text-align: center;
}

.progress-step {
  margin: 4px 0;
  color: #666;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
}

.cache-indicator {
  margin-left: 8px;
}

.preview-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 12px;
  color: #f56c6c;
}

.preview-error p {
  margin: 0;
  text-align: center;
}

.chunks-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chunk-item {
  padding: 12px;
  background: #fafafa;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
}

.chunk-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.chunk-index {
  font-weight: 500;
  color: #409eff;
}

.chunk-size {
  font-size: 12px;
  color: #666;
}

.chunk-content {
  font-size: 14px;
  line-height: 1.5;
  color: #333;
  margin-bottom: 8px;
  word-break: break-word;
}

.chunk-metadata {
  display: flex;
  gap: 4px;
}

.preview-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 12px;
  color: #999;
}

.preview-placeholder p {
  margin: 0;
  text-align: center;
}

/* 完成步骤 */
.completion-section {
  padding: 24px;
  background: #fafafa;
  border-radius: 8px;
}

.completion-section h3 {
  margin: 0 0 24px 0;
  font-size: 18px;
  font-weight: 500;
}

.completion-summary {
  margin-bottom: 24px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #e5e7eb;
}

.summary-item:last-child {
  border-bottom: none;
}

.summary-item .label {
  font-weight: 500;
  color: #333;
}

.summary-item .value {
  color: #666;
}

.completion-tip {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: #fdf6ec;
  border: 1px solid #f5dab1;
  border-radius: 6px;
  color: #e6a23c;
}

.completion-tip p {
  margin: 0;
  font-size: 14px;
  line-height: 1.4;
}

/* 底部按钮 */
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
  gap: 12px;
}

/* 处理状态样式 */
.processing-status {
  margin-bottom: 24px;
  padding: 20px;
  background: #f8f9ff;
  border: 1px solid #e1e6ff;
  border-radius: 8px;
}

.status-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.status-icon {
  font-size: 20px;
}

.status-icon.loading {
  color: #409eff;
}

.status-icon.success {
  color: #67c23a;
}

.status-icon.error {
  color: #f56c6c;
}

.status-text {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.progress-container {
  margin-bottom: 16px;
}

.progress-detail {
  margin: 8px 0 0 0;
  color: #666;
  font-size: 14px;
}

.error-container {
  padding: 16px;
  background: #fef0f0;
  border: 1px solid #fde2e2;
  border-radius: 6px;
  margin-bottom: 16px;
}

.error-message {
  margin: 0 0 12px 0;
  color: #f56c6c;
  font-size: 14px;
}

.results-container {
  background: #f0f9ff;
  border: 1px solid #b6d7ff;
  border-radius: 6px;
  padding: 16px;
}

.results-summary {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.result-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #e1e6ff;
}

.result-item:last-child {
  border-bottom: none;
}

.result-item .label {
  font-weight: 500;
  color: #333;
}

.result-item .value {
  color: #67c23a;
  font-weight: 500;
}

/* 模型配置样式 */
.model-config-section {
  background: #f9f9f9;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 16px;
  margin-top: 8px;
}

.model-selection {
  margin-bottom: 16px;
}

.model-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.model-parameters {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.parameter-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.parameter-item {
  flex: 1;
}

.parameter-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
  font-size: 13px;
}

.parameter-item .el-slider {
  margin: 8px 0;
}

.parameter-item .el-input-number {
  width: 100%;
}
</style>