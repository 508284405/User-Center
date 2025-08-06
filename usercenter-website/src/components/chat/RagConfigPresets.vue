<template>
  <div class="rag-config-presets">
    <el-card class="presets-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="title">配置预设</span>
          <el-tooltip content="导入/导出配置">
            <el-dropdown @command="handleMenuCommand">
              <el-button type="text" size="small" :icon="MoreFilled" />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="export" :icon="Download">
                    导出配置
                  </el-dropdown-item>
                  <el-dropdown-item command="import" :icon="Upload">
                    导入配置
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-tooltip>
        </div>
      </template>

      <!-- 预设列表 -->
      <div class="presets-list">
        <div 
          v-for="preset in ragStore.presetDefinitions"
          :key="preset.id"
          class="preset-item"
          :class="{ 
            active: ragStore.selectedPreset === preset.id && !ragStore.isCustomConfig,
            'partially-active': ragStore.isPresetConfig(preset.id) && ragStore.isCustomConfig
          }"
          @click="handlePresetSelect(preset.id)"
        >
          <div class="preset-header">
            <div class="preset-info">
              <h4 class="preset-name">
                {{ preset.name }}
                <el-tag
                  v-if="ragStore.selectedPreset === preset.id && !ragStore.isCustomConfig"
                  type="primary"
                  size="small"
                  class="active-tag"
                >
                  当前
                </el-tag>
                <el-tag
                  v-else-if="ragStore.isPresetConfig(preset.id) && ragStore.isCustomConfig"
                  type="success"
                  size="small"
                  class="match-tag"
                >
                  匹配
                </el-tag>
              </h4>
              <p class="preset-description">{{ preset.description }}</p>
            </div>
            <div class="preset-actions">
              <el-button
                size="small"
                type="primary"
                :disabled="ragStore.selectedPreset === preset.id && !ragStore.isCustomConfig"
                @click.stop="handlePresetApply(preset.id)"
              >
                应用
              </el-button>
            </div>
          </div>

          <!-- 预设详细配置展示（可折叠） -->
          <el-collapse-transition>
            <div v-if="expandedPreset === preset.id" class="preset-details">
              <div class="config-summary">
                <div class="summary-section">
                  <h5>内容聚合器</h5>
                  <ul>
                    <li>最大结果数: {{ preset.config.contentAggregator.maxResults }}</li>
                    <li>最小分数: {{ preset.config.contentAggregator.minScore }}</li>
                  </ul>
                </div>
                
                <div class="summary-section">
                  <h5>查询转换器</h5>
                  <ul>
                    <li>扩展数量: {{ preset.config.queryTransformer.n }}</li>
                  </ul>
                </div>
                
                <div class="summary-section">
                  <h5>查询路由器</h5>
                  <ul>
                    <li>网络搜索: {{ preset.config.queryRouter.webSearchEnabled ? '启用' : '禁用' }}</li>
                    <li>知识库搜索: {{ preset.config.queryRouter.knowledgeSearchEnabled ? '启用' : '禁用' }}</li>
                  </ul>
                </div>
                
                <div v-if="preset.config.queryRouter.webSearchEnabled" class="summary-section">
                  <h5>网络搜索</h5>
                  <ul>
                    <li>最大结果: {{ preset.config.webSearch.maxResults }}</li>
                    <li>超时时间: {{ preset.config.webSearch.timeout }}秒</li>
                  </ul>
                </div>
                
                <div v-if="preset.config.queryRouter.knowledgeSearchEnabled" class="summary-section">
                  <h5>知识库搜索</h5>
                  <ul>
                    <li>Top K: {{ preset.config.knowledgeSearch.topK }}</li>
                    <li>分数阈值: {{ preset.config.knowledgeSearch.scoreThreshold }}</li>
                  </ul>
                </div>
              </div>
            </div>
          </el-collapse-transition>

          <!-- 展开/收起按钮 -->
          <div class="preset-expand">
            <el-button
              type="text"
              size="small"
              @click.stop="togglePresetDetails(preset.id)"
            >
              <el-icon>
                <ArrowDown v-if="expandedPreset !== preset.id" />
                <ArrowUp v-else />
              </el-icon>
              {{ expandedPreset === preset.id ? '收起详情' : '查看详情' }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 自定义配置提示 -->
      <div v-if="ragStore.isCustomConfig" class="custom-config-notice">
        <el-alert
          type="info"
          :closable="false"
          show-icon
        >
          <template #title>
            当前使用自定义配置
          </template>
          <span>
            您可以选择一个预设来快速切换配置，或继续使用当前的自定义设置。
          </span>
        </el-alert>
      </div>

      <!-- 操作栏 -->
      <div class="presets-actions">
        <el-button @click="handleCreateCustom">
          <el-icon><Setting /></el-icon>
          自定义配置
        </el-button>
        <el-button type="primary" @click="handleQuickApply">
          快速应用
        </el-button>
      </div>
    </el-card>

    <!-- 导入配置对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="导入配置"
      width="500px"
      destroy-on-close
    >
      <el-input
        v-model="importConfigText"
        type="textarea"
        :rows="10"
        placeholder="请粘贴配置JSON..."
        resize="none"
      />
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleImportConfig">导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  MoreFilled,
  Download,
  Upload,
  ArrowDown,
  ArrowUp,
  Setting
} from '@element-plus/icons-vue';
import { useRagConfigStore } from '@/stores/ragConfig';
import { RagConfigPreset } from '@/types/chat';

// Emits
interface Emits {
  (event: 'preset-applied', preset: RagConfigPreset): void;
  (event: 'custom-config-requested'): void;
  (event: 'quick-apply-requested'): void;
}

const emit = defineEmits<Emits>();

// Store
const ragStore = useRagConfigStore();

// 本地状态
const expandedPreset = ref<string | null>(null);
const importDialogVisible = ref(false);
const importConfigText = ref('');

// 方法

/**
 * 处理预设选择
 */
const handlePresetSelect = (presetId: RagConfigPreset) => {
  // 如果点击已选中的预设，切换详情展开状态
  if (ragStore.selectedPreset === presetId && !ragStore.isCustomConfig) {
    togglePresetDetails(presetId);
  } else {
    // 否则选择该预设
    handlePresetApply(presetId);
  }
};

/**
 * 应用预设配置
 */
const handlePresetApply = (presetId: RagConfigPreset) => {
  ragStore.applyPreset(presetId);
  emit('preset-applied', presetId);
  
  const presetName = ragStore.presetDefinitions.find(p => p.id === presetId)?.name;
  ElMessage.success(`已应用"${presetName}"预设配置`);
};

/**
 * 切换预设详情展示
 */
const togglePresetDetails = (presetId: string) => {
  if (expandedPreset.value === presetId) {
    expandedPreset.value = null;
  } else {
    expandedPreset.value = presetId;
  }
};

/**
 * 处理菜单命令
 */
const handleMenuCommand = (command: string) => {
  switch (command) {
    case 'export':
      handleExportConfig();
      break;
    case 'import':
      handleShowImportDialog();
      break;
  }
};

/**
 * 导出配置
 */
const handleExportConfig = () => {
  try {
    const configJson = ragStore.exportConfig();
    
    // 创建下载链接
    const blob = new Blob([configJson], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `rag-config-${new Date().toISOString().slice(0, 10)}.json`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    
    ElMessage.success('配置已导出');
  } catch (error) {
    console.error('导出配置失败:', error);
    ElMessage.error('导出配置失败');
  }
};

/**
 * 显示导入对话框
 */
const handleShowImportDialog = () => {
  importConfigText.value = '';
  importDialogVisible.value = true;
};

/**
 * 导入配置
 */
const handleImportConfig = () => {
  if (!importConfigText.value.trim()) {
    ElMessage.warning('请输入配置内容');
    return;
  }
  
  const success = ragStore.importConfig(importConfigText.value);
  
  if (success) {
    ElMessage.success('配置导入成功');
    importDialogVisible.value = false;
  } else {
    ElMessage.error('配置导入失败，请检查格式是否正确');
  }
};

/**
 * 处理自定义配置请求
 */
const handleCreateCustom = () => {
  emit('custom-config-requested');
};

/**
 * 处理快速应用请求
 */
const handleQuickApply = () => {
  if (!ragStore.isValidConfig) {
    ElMessage.warning('当前配置无效，请先修正配置错误');
    return;
  }
  
  emit('quick-apply-requested');
  ElMessage.success('配置已快速应用');
};

// 获取预设配置的简短描述
const getPresetSummary = (preset: any) => {
  const parts = [];
  
  if (preset.config.queryRouter.webSearchEnabled) {
    parts.push(`Web搜索(${preset.config.webSearch.maxResults})`);
  }
  
  if (preset.config.queryRouter.knowledgeSearchEnabled) {
    parts.push(`知识库(Top${preset.config.knowledgeSearch.topK})`);
  }
  
  parts.push(`聚合(${preset.config.contentAggregator.maxResults}结果)`);
  
  return parts.join(' + ');
};
</script>

<style scoped lang="scss">
.rag-config-presets {
  .presets-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .title {
        font-weight: bold;
        font-size: 16px;
      }
    }
  }
  
  .presets-list {
    .preset-item {
      border: 1px solid var(--el-border-color-lighter);
      border-radius: 8px;
      padding: 16px;
      margin-bottom: 12px;
      cursor: pointer;
      transition: all 0.3s ease;
      
      &:hover {
        border-color: var(--el-color-primary);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }
      
      &.active {
        border-color: var(--el-color-primary);
        background-color: var(--el-color-primary-light-9);
        
        .preset-name {
          color: var(--el-color-primary);
        }
      }
      
      &.partially-active {
        border-color: var(--el-color-success);
        background-color: var(--el-color-success-light-9);
      }
      
      .preset-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        
        .preset-info {
          flex: 1;
          
          .preset-name {
            margin: 0 0 8px 0;
            font-size: 16px;
            font-weight: 600;
            display: flex;
            align-items: center;
            gap: 8px;
            
            .active-tag, .match-tag {
              font-size: 12px;
            }
          }
          
          .preset-description {
            margin: 0;
            color: var(--el-text-color-secondary);
            line-height: 1.5;
            font-size: 14px;
          }
        }
        
        .preset-actions {
          margin-left: 16px;
        }
      }
      
      .preset-details {
        margin-top: 16px;
        padding-top: 16px;
        border-top: 1px solid var(--el-border-color-lighter);
        
        .config-summary {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
          gap: 16px;
          
          .summary-section {
            h5 {
              margin: 0 0 8px 0;
              font-size: 14px;
              font-weight: 600;
              color: var(--el-text-color-primary);
            }
            
            ul {
              margin: 0;
              padding: 0;
              list-style: none;
              
              li {
                font-size: 13px;
                color: var(--el-text-color-regular);
                margin: 4px 0;
                padding-left: 12px;
                position: relative;
                
                &:before {
                  content: '•';
                  position: absolute;
                  left: 0;
                  color: var(--el-color-primary);
                }
              }
            }
          }
        }
      }
      
      .preset-expand {
        margin-top: 12px;
        text-align: center;
        border-top: 1px solid var(--el-border-color-lighter);
        padding-top: 12px;
        
        .el-button {
          font-size: 12px;
        }
      }
    }
  }
  
  .custom-config-notice {
    margin: 16px 0;
  }
  
  .presets-actions {
    display: flex;
    justify-content: center;
    gap: 12px;
    margin-top: 20px;
    padding-top: 16px;
    border-top: 1px solid var(--el-border-color-lighter);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .rag-config-presets {
    .presets-list {
      .preset-item {
        .preset-header {
          flex-direction: column;
          align-items: flex-start;
          
          .preset-actions {
            margin-left: 0;
            margin-top: 12px;
          }
        }
        
        .preset-details {
          .config-summary {
            grid-template-columns: 1fr;
          }
        }
      }
    }
    
    .presets-actions {
      flex-direction: column;
      align-items: center;
      
      .el-button {
        width: 100%;
        max-width: 200px;
      }
    }
  }
}
</style>