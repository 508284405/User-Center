<template>
  <div class="rag-config-panel">
    <el-card class="config-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="title">RAG组件配置</span>
          <div class="header-actions">
            <el-tooltip content="重置为默认配置">
              <el-button 
                type="info" 
                size="small" 
                :icon="RefreshRight"
                @click="handleReset"
              />
            </el-tooltip>
            <el-tooltip content="应用配置">
              <el-button 
                type="primary" 
                size="small"
                :icon="Check"
                :disabled="!ragStore.isValidConfig"
                @click="handleApply"
              />
            </el-tooltip>
          </div>
        </div>
      </template>

      <!-- 配置验证错误提示 -->
      <el-alert
        v-if="!ragStore.isValidConfig"
        type="warning"
        :closable="false"
        class="validation-alert"
      >
        <template #title>
          <span>配置验证失败，发现 {{ ragStore.configErrors.length }} 个错误</span>
        </template>
        <ul class="error-list">
          <li v-for="error in ragStore.configErrors" :key="error.field">
            {{ error.message }}
          </li>
        </ul>
        <el-button
          type="text"
          size="small"
          @click="ragStore.applyCorrectedConfig"
        >
          应用修正配置
        </el-button>
      </el-alert>

      <!-- 配置面板内容 -->
      <div class="config-content">
        <!-- 内容聚合器配置 -->
        <el-collapse v-model="activeCollapse">
          <el-collapse-item name="contentAggregator">
            <template #title>
              <div class="section-title">
                <el-icon><Document /></el-icon>
                <span>内容聚合器 (Content Aggregator)</span>
              </div>
            </template>
            
            <div class="config-section">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item 
                    label="最大结果数" 
                    :error="getFieldError('contentAggregator.maxResults')"
                  >
                    <el-input-number
                      v-model="localConfig.contentAggregator.maxResults"
                      :min="1"
                      :max="50"
                      :step="1"
                      controls-position="right"
                      @change="handleConfigChange"
                    />
                    <div class="field-help">返回的最大文档数量 (1-50)</div>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item 
                    label="最小分数阈值" 
                    :error="getFieldError('contentAggregator.minScore')"
                  >
                    <el-slider
                      v-model="localConfig.contentAggregator.minScore"
                      :min="0"
                      :max="1"
                      :step="0.1"
                      :format-tooltip="(val) => val.toFixed(1)"
                      @change="handleConfigChange"
                    />
                    <div class="field-help">过滤低相关性文档的分数阈值 (0.0-1.0)</div>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item 
                    label="打分模型（可选）"
                    :error="getFieldError('contentAggregator.scoringModelId')"
                  >
                    <ModelSelector
                      v-model="localConfig.contentAggregator.scoringModelId"
                      placeholder="选择评分模型，未选择时使用会话级模型"
                      clearable
                      @change="handleConfigChange"
                    />
                    <div class="field-help">用于重排序打分的LLM模型</div>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </el-collapse-item>

          <!-- 查询转换器配置 -->
          <el-collapse-item name="queryTransformer">
            <template #title>
              <div class="section-title">
                <el-icon><Edit /></el-icon>
                <span>查询转换器 (Query Transformer)</span>
              </div>
            </template>
            
            <div class="config-section">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item 
                    label="查询扩展数量" 
                    :error="getFieldError('queryTransformer.n')"
                  >
                    <el-input-number
                      v-model="localConfig.queryTransformer.n"
                      :min="1"
                      :max="10"
                      :step="1"
                      controls-position="right"
                      @change="handleConfigChange"
                    />
                    <div class="field-help">生成的扩展查询数量 (1-10)</div>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item 
                    label="LLM（可选）"
                    :error="getFieldError('queryTransformer.modelId')"
                  >
                    <ModelSelector
                      v-model="localConfig.queryTransformer.modelId"
                      placeholder="选择查询转换模型，未选择时使用会话级模型"
                      clearable
                      @change="handleConfigChange"
                    />
                    <div class="field-help">用于查询转换的LLM模型</div>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </el-collapse-item>

          <!-- 查询路由器配置 -->
          <el-collapse-item name="queryRouter">
            <template #title>
              <div class="section-title">
                <el-icon><Connection /></el-icon>
                <span>查询路由器 (Query Router)</span>
              </div>
            </template>
            
            <div class="config-section">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="网络搜索">
                    <el-switch
                      v-model="localConfig.queryRouter.webSearchEnabled"
                      active-text="启用"
                      inactive-text="禁用"
                      @change="handleConfigChange"
                    />
                    <div class="field-help">是否启用网络搜索检索器</div>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="知识库搜索">
                    <el-switch
                      v-model="localConfig.queryRouter.knowledgeSearchEnabled"
                      active-text="启用"
                      inactive-text="禁用"
                      @change="handleConfigChange"
                    />
                    <div class="field-help">是否启用知识库检索器</div>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item 
                    label="LLM（可选）"
                    :error="getFieldError('queryRouter.modelId')"
                  >
                    <ModelSelector
                      v-model="localConfig.queryRouter.modelId"
                      placeholder="选择路由判别模型，未选择时使用会话级模型"
                      clearable
                      @change="handleConfigChange"
                    />
                    <div class="field-help">用于路由决策的LLM模型</div>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </el-collapse-item>

          <!-- 网络搜索配置 -->
          <el-collapse-item name="webSearch">
            <template #title>
              <div class="section-title">
                <el-icon><Search /></el-icon>
                <span>网络搜索 (Web Search)</span>
              </div>
            </template>
            
            <div class="config-section">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item 
                    label="最大搜索结果数" 
                    :error="getFieldError('webSearch.maxResults')"
                  >
                    <el-input-number
                      v-model="localConfig.webSearch.maxResults"
                      :min="1"
                      :max="20"
                      :step="1"
                      :disabled="!localConfig.queryRouter.webSearchEnabled"
                      controls-position="right"
                      @change="handleConfigChange"
                    />
                    <div class="field-help">网络搜索返回的最大结果数 (1-20)</div>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item 
                    label="搜索超时时间 (秒)" 
                    :error="getFieldError('webSearch.timeout')"
                  >
                    <el-input-number
                      v-model="localConfig.webSearch.timeout"
                      :min="1"
                      :max="60"
                      :step="1"
                      :disabled="!localConfig.queryRouter.webSearchEnabled"
                      controls-position="right"
                      @change="handleConfigChange"
                    />
                    <div class="field-help">网络搜索的超时时间 (1-60秒)</div>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </el-collapse-item>

          <!-- 知识库搜索配置 -->
          <el-collapse-item name="knowledgeSearch">
            <template #title>
              <div class="section-title">
                <el-icon><FolderOpened /></el-icon>
                <span>知识库搜索 (Knowledge Search)</span>
              </div>
            </template>
            
            <div class="config-section">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item 
                    label="Top K 结果数" 
                    :error="getFieldError('knowledgeSearch.topK')"
                  >
                    <el-input-number
                      v-model="localConfig.knowledgeSearch.topK"
                      :min="1"
                      :max="100"
                      :step="1"
                      :disabled="!localConfig.queryRouter.knowledgeSearchEnabled"
                      controls-position="right"
                      @change="handleConfigChange"
                    />
                    <div class="field-help">返回的最相关结果数量 (1-100)</div>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item 
                    label="分数阈值" 
                    :error="getFieldError('knowledgeSearch.scoreThreshold')"
                  >
                    <el-slider
                      v-model="localConfig.knowledgeSearch.scoreThreshold"
                      :min="0"
                      :max="1"
                      :step="0.1"
                      :disabled="!localConfig.queryRouter.knowledgeSearchEnabled"
                      :format-tooltip="(val) => val.toFixed(1)"
                      @change="handleConfigChange"
                    />
                    <div class="field-help">知识库搜索的分数阈值 (0.0-1.0)</div>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </el-collapse-item>

          <!-- 内容注入器配置 -->
          <el-collapse-item name="contentInjector">
            <template #title>
              <div class="section-title">
                <el-icon><EditPen /></el-icon>
                <span>内容注入器 (Content Injector)</span>
              </div>
            </template>
            
            <div class="config-section">
              <el-row :gutter="16">
                <el-col :span="24">
                  <el-form-item 
                    label="提示模板"
                    :error="getFieldError('contentInjector.promptTemplate')"
                  >
                    <el-input
                      v-model="localConfig.contentInjector.promptTemplate"
                      type="textarea"
                      :rows="4"
                      placeholder="自定义内容注入的提示模板，为空时使用系统默认模板"
                      maxlength="4000"
                      show-word-limit
                      @change="handleConfigChange"
                    />
                    <div class="field-help">用于格式化注入内容的提示模板（最多4000字符）</div>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="16">
                <el-col :span="24">
                  <el-form-item 
                    label="元数据键列表"
                    :error="getFieldError('contentInjector.metadataKeysToInclude')"
                  >
                    <el-select
                      v-model="localConfig.contentInjector.metadataKeysToInclude"
                      multiple
                      filterable
                      allow-create
                      default-first-option
                      :reserve-keyword="false"
                      placeholder="输入要包含的元数据键，按回车添加"
                      @change="handleConfigChange"
                    >
                      <el-option
                        v-for="key in commonMetadataKeys"
                        :key="key"
                        :label="key"
                        :value="key"
                      />
                    </el-select>
                    <div class="field-help">指定哪些元数据键应该包含在注入的内容中（最多20个）</div>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>

      <!-- 底部操作栏 -->
      <div class="config-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button 
          type="primary" 
          :disabled="!ragStore.isValidConfig"
          @click="handleApply"
        >
          应用配置
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import {
  Document,
  Edit,
  EditPen,
  Connection,
  Search,
  FolderOpened,
  RefreshRight,
  Check
} from '@element-plus/icons-vue';
import { useRagConfigStore } from '@/stores/ragConfig';
import { RagComponentConfig } from '@/types/chat';
import ModelSelector from '@/components/app/ModelSelector.vue';

// Props 和 Emits
interface Props {
  modelValue?: boolean; // 控制面板显示/隐藏
}

interface Emits {
  (event: 'update:modelValue', value: boolean): void;
  (event: 'config-applied', config: RagComponentConfig): void;
  (event: 'config-cancelled'): void;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: true
});

const emit = defineEmits<Emits>();

// Store
const ragStore = useRagConfigStore();

// 本地状态
const activeCollapse = ref(['contentAggregator', 'queryRouter']);
const localConfig = ref<RagComponentConfig>(ragStore.getConfigCopy());

// 常用的元数据键选项
const commonMetadataKeys = ref([
  'title',
  'source',
  'url', 
  'author',
  'date',
  'category',
  'tags',
  'document_type',
  'file_name',
  'page_number'
]);

// 监听store配置变化，同步到本地配置
watch(() => ragStore.currentConfig, (newConfig) => {
  localConfig.value = JSON.parse(JSON.stringify(newConfig));
}, { deep: true, immediate: true });

// 计算属性
const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
});

// 方法

/**
 * 获取字段验证错误信息
 */
const getFieldError = (fieldPath: string): string => {
  const error = ragStore.configErrors.find(err => err.field === fieldPath);
  return error ? error.message : '';
};

/**
 * 处理配置变更
 */
const handleConfigChange = async () => {
  // 更新store中的配置
  ragStore.updateConfig(localConfig.value);
  
  // 等待下一次DOM更新后验证配置
  await nextTick();
  ragStore.validateCurrentConfig();
};

/**
 * 应用配置
 */
const handleApply = () => {
  if (!ragStore.isValidConfig) {
    ElMessage.warning('配置验证失败，请修复错误后再试');
    return;
  }
  
  // 保存配置
  ragStore.saveConfigToStorage();
  
  // 发送事件
  emit('config-applied', ragStore.getConfigCopy());
  
  ElMessage.success('RAG配置已应用');
};

/**
 * 取消配置
 */
const handleCancel = () => {
  // 重置本地配置为store中的配置
  localConfig.value = ragStore.getConfigCopy();
  
  emit('config-cancelled');
  
  ElMessage.info('已取消配置更改');
};

/**
 * 重置配置
 */
const handleReset = () => {
  ragStore.resetToDefault();
  localConfig.value = ragStore.getConfigCopy();
  
  ElMessage.success('已重置为默认配置');
};

// 组件挂载时验证配置
ragStore.validateCurrentConfig();
</script>

<style scoped lang="scss">
.rag-config-panel {
  .config-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .title {
        font-weight: bold;
        font-size: 16px;
      }
      
      .header-actions {
        display: flex;
        gap: 8px;
      }
    }
  }
  
  .validation-alert {
    margin-bottom: 16px;
    
    .error-list {
      margin: 8px 0;
      padding-left: 20px;
      
      li {
        margin: 4px 0;
        color: var(--el-color-warning-dark-2);
      }
    }
  }
  
  .config-content {
    .section-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 500;
    }
    
    .config-section {
      padding: 16px 0;
      
      .el-form-item {
        margin-bottom: 20px;
        
        .field-help {
          font-size: 12px;
          color: var(--el-text-color-secondary);
          margin-top: 4px;
          line-height: 1.4;
        }
      }
    }
  }
  
  .config-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 20px;
    padding-top: 16px;
    border-top: 1px solid var(--el-border-color-lighter);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .rag-config-panel {
    .config-content {
      .config-section {
        .el-col {
          margin-bottom: 16px;
        }
      }
    }
  }
}
</style>