<template>
  <div class="knowledge-base-settings">
    <!-- 页面标题 -->
    <div class="settings-header">
      <h3>知识库设置</h3>
      <p class="description">在这里，你可以修改出知识库的属性和检索设置</p>
    </div>

    <div class="settings-content" v-loading="loading">
      <!-- 基本信息设置 -->
      <div class="settings-section">
        <h4 class="section-title">基本信息</h4>
        
        <el-form :model="settings" label-width="120px" class="settings-form">
          <el-form-item label="知识库名称">
            <el-input 
              v-model="settings.name" 
              placeholder="丰富智能企业知识库"
              maxlength="50"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="知识库描述">
            <el-input
              v-model="settings.description"
              type="textarea"
              :rows="3"
              placeholder="useful for when you want to answer queries about the product information"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="可见权限">
            <el-select v-model="settings.visibility" placeholder="选择可见权限">
              <el-option 
                v-for="option in visibilityOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              >
                <div class="visibility-option">
                  <el-icon class="option-icon">
                    <component :is="option.icon" />
                  </el-icon>
                  <span>{{ option.label }}</span>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <!-- 索引模式设置 -->
      <div class="settings-section">
        <h4 class="section-title">索引模式</h4>
        
        <div class="indexing-modes">
          <div 
            v-for="mode in indexingModes"
            :key="mode.value"
            class="mode-card"
            :class="{ active: settings.indexingMode === mode.value }"
            @click="settings.indexingMode = mode.value"
          >
            <div class="mode-header">
              <el-radio 
                v-model="settings.indexingMode" 
                :value="mode.value"
                @click.stop
              />
              <div class="mode-info">
                <div class="mode-title">{{ mode.title }}</div>
                <div class="mode-tag">
                  <el-tag 
                    :type="mode.tagType" 
                    size="small"
                    effect="plain"
                  >
                    {{ mode.tag }}
                  </el-tag>
                </div>
              </div>
            </div>
            <div class="mode-description">{{ mode.description }}</div>
            <div class="mode-details">{{ mode.details }}</div>
          </div>
        </div>
      </div>

      <!-- Embedding模型 -->
      <div class="settings-section">
        <h4 class="section-title">Embedding 模型</h4>
        
        <el-select 
          v-model="settings.embeddingModel" 
          placeholder="选择Embedding模型"
          class="model-select"
        >
          <el-option
            v-for="model in embeddingModels"
            :key="model.value"
            :label="model.label"
            :value="model.value"
          >
            <div class="model-option">
              <div class="model-name">{{ model.label }}</div>
              <div class="model-desc">{{ model.description }}</div>
            </div>
          </el-option>
        </el-select>
      </div>

      <!-- 检索设置 -->
      <div class="settings-section">
        <h4 class="section-title">检索设置</h4>
        <p class="section-description">了解更多关于检索方法。</p>
        
        <div class="retrieval-settings">
          <!-- 向量检索 -->
          <div class="retrieval-method">
            <div class="method-header">
              <div class="method-info">
                <el-icon class="method-icon"><Histogram /></el-icon>
                <div class="method-content">
                  <h5>向量检索</h5>
                  <p>通过生成向量并与用户查询进行相似度匹配来显示最相关的文本分段</p>
                </div>
              </div>
              <el-switch 
                v-model="settings.retrievalSettings.vectorSearch.enabled"
                @change="handleRetrievalChange"
              />
            </div>
            
            <div 
              v-if="settings.retrievalSettings.vectorSearch.enabled" 
              class="method-config"
            >
              <div class="config-item">
                <label class="config-label">Top K</label>
                <div class="config-control">
                  <el-tooltip content="返回最相似的K个文档分段" placement="top">
                    <el-icon class="info-icon"><QuestionFilled /></el-icon>
                  </el-tooltip>
                  <el-input-number
                    v-model="settings.retrievalSettings.vectorSearch.topK"
                    :min="1"
                    :max="20"
                    controls-position="right"
                    size="small"
                    style="width: 100px"
                  />
                </div>
              </div>
              
              <div class="config-item">
                <label class="config-label">Score 阈值</label>
                <div class="config-control">
                  <el-tooltip content="只返回相似度分数高于此阈值的结果" placement="top">
                    <el-icon class="info-icon"><QuestionFilled /></el-icon>
                  </el-tooltip>
                  <el-slider
                    v-model="settings.retrievalSettings.vectorSearch.scoreThreshold"
                    :min="0"
                    :max="1"
                    :step="0.01"
                    :format-tooltip="formatThreshold"
                    show-input
                    style="flex: 1"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- 全文检索 -->
          <div class="retrieval-method">
            <div class="method-header">
              <div class="method-info">
                <el-icon class="method-icon"><Search /></el-icon>
                <div class="method-content">
                  <h5>全文检索</h5>
                  <p>索引文档中的所有词汇，从而允许用户查询档中的所有词汇行精确匹配</p>
                </div>
              </div>
              <el-switch 
                v-model="settings.retrievalSettings.fullTextSearch.enabled"
                @change="handleRetrievalChange"
              />
            </div>
          </div>

          <!-- 混合检索 -->
          <div class="retrieval-method">
            <div class="method-header">
              <div class="method-info">
                <el-icon class="method-icon"><Connection /></el-icon>
                <div class="method-content">
                  <h5>混合检索</h5>
                  <p class="recommended">推荐</p>
                  <p>同时使用全文检索和向量检索，并应用重新排序算法，从完善单词对象中选择最优的答案结果，用户可以设置设置重新排权。</p>
                </div>
              </div>
              <el-switch 
                v-model="settings.retrievalSettings.hybridSearch.enabled"
                @change="handleRetrievalChange"
              />
            </div>
            
            <div 
              v-if="settings.retrievalSettings.hybridSearch.enabled" 
              class="method-config"
            >
              <div class="config-item">
                <label class="config-label">Rerank 模型</label>
                <div class="config-control">
                  <el-tooltip content="启用后，将基础检索结果重新排序以提高准确性" placement="top">
                    <el-icon class="info-icon"><QuestionFilled /></el-icon>
                  </el-tooltip>
                  <el-switch 
                    v-model="settings.retrievalSettings.hybridSearch.rerankEnabled"
                    size="small"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 保存按钮 -->
      <div class="settings-actions">
        <el-button @click="handleReset" :disabled="saving">重置</el-button>
        <el-button 
          type="primary" 
          @click="handleSave"
          :loading="saving"
        >
          保存
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  User, 
  Lock, 
  Histogram, 
  Search, 
  Connection,
  QuestionFilled
} from '@element-plus/icons-vue'
import {
  getKnowledgeBaseSettings,
  updateKnowledgeBaseSettings,
  type KnowledgeBaseSettings
} from '@/api/smartcs/knowledgeBase'

interface Props {
  knowledgeBaseId: number
}

const props = defineProps<Props>()

// 状态管理
const loading = ref(false)
const saving = ref(false)
const originalSettings = ref<KnowledgeBaseSettings | null>(null)

// 设置数据
const settings = reactive<KnowledgeBaseSettings>({
  id: props.knowledgeBaseId,
  name: '',
  description: '',
  visibility: 'private',
  indexingMode: 'high_quality',
  embeddingModel: 'text-embedding-v1',
  retrievalSettings: {
    vectorSearch: {
      enabled: true,
      topK: 2,
      scoreThreshold: 0.5
    },
    fullTextSearch: {
      enabled: false
    },
    hybridSearch: {
      enabled: false,
      rerankEnabled: false
    }
  }
})

// 可见权限选项
const visibilityOptions = [
  {
    value: 'public',
    label: '只有我',
    icon: User
  },
  {
    value: 'private', 
    label: '仅私有',
    icon: Lock
  }
]

// 索引模式选项
const indexingModes = [
  {
    value: 'high_quality',
    title: '高质量',
    tag: '推荐',
    tagType: 'success',
    description: '通过原文预处理关键文本信息以实现更精确的检索，可以有助于提高查询性能，',
    details: '适合使用 10 个关键词进行精准，不消耗 tokens，但会降低算费处理简单。'
  },
  {
    value: 'economy',
    title: '经济',
    tag: '省钱',
    tagType: 'primary',
    description: '节省使用 10 个关键词进行精准，不消耗 tokens，但会降低算费处理简单。',
    details: ''
  }
]

// Embedding模型选项
const embeddingModels = [
  {
    value: 'text-embedding-v1',
    label: 'text-embedding-v1',
    description: '通用文本向量化模型'
  }
]

// 格式化阈值显示
const formatThreshold = (val: number) => {
  return `${(val * 100).toFixed(0)}%`
}

// 处理检索方式变化
const handleRetrievalChange = () => {
  // 确保至少有一种检索方式启用
  const { vectorSearch, fullTextSearch, hybridSearch } = settings.retrievalSettings
  
  if (!vectorSearch.enabled && !fullTextSearch.enabled && !hybridSearch.enabled) {
    ElMessage.warning('至少需要启用一种检索方式')
    // 默认启用向量检索
    settings.retrievalSettings.vectorSearch.enabled = true
  }
}

// 加载设置
const loadSettings = async () => {
  try {
    loading.value = true
    const response = await getKnowledgeBaseSettings(props.knowledgeBaseId)
    
    if (response.success && response.data) {
      Object.assign(settings, response.data)
      originalSettings.value = JSON.parse(JSON.stringify(response.data))
    } else {
      throw new Error(response.errMessage || '获取设置失败')
    }
  } catch (error: any) {
    console.error('加载知识库设置失败:', error)
    ElMessage.error(error.message || '加载设置失败')
  } finally {
    loading.value = false
  }
}

// 保存设置
const handleSave = async () => {
  try {
    saving.value = true
    
    const response = await updateKnowledgeBaseSettings(settings)
    
    if (response.success) {
      ElMessage.success('设置保存成功')
      originalSettings.value = JSON.parse(JSON.stringify(settings))
    } else {
      throw new Error(response.errMessage || '保存设置失败')
    }
  } catch (error: any) {
    console.error('保存知识库设置失败:', error)
    ElMessage.error(error.message || '保存设置失败')
  } finally {
    saving.value = false
  }
}

// 重置设置
const handleReset = () => {
  if (originalSettings.value) {
    Object.assign(settings, JSON.parse(JSON.stringify(originalSettings.value)))
    ElMessage.success('设置已重置')
  }
}

// 监听知识库ID变化
watch(() => props.knowledgeBaseId, () => {
  if (props.knowledgeBaseId) {
    settings.id = props.knowledgeBaseId
    loadSettings()
  }
}, { immediate: true })

onMounted(() => {
  loadSettings()
})
</script>

<style scoped lang="scss">
.knowledge-base-settings {
  padding: 24px;
  background: #fff;
  min-height: 100%;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);

  .settings-header {
    margin-bottom: 40px;
    padding-bottom: 16px;
    border-bottom: 1px solid #f0f2f5;

    h3 {
      margin: 0 0 8px 0;
      font-size: 20px;
      font-weight: 600;
      color: #1f2937;
      background: linear-gradient(135deg, #409eff 0%, #36cfc9 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      display: flex;
      align-items: center;
      gap: 8px;
      
      &::before {
        content: '⚙️';
        font-size: 22px;
        -webkit-text-fill-color: initial;
      }
    }

    .description {
      margin: 0;
      font-size: 14px;
      color: #6b7280;
      line-height: 1.6;
    }
  }

  .settings-content {
    .settings-section {
      margin-bottom: 40px;
      padding: 24px;
      background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
      border-radius: 12px;
      border: 1px solid #e2e8f0;
      box-shadow: 0 1px 6px rgba(0, 0, 0, 0.02);
      transition: all 0.3s ease;

      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
        transform: translateY(-1px);
      }

      &:last-child {
        margin-bottom: 0;
      }

      .section-title {
        margin: 0 0 20px 0;
        font-size: 18px;
        font-weight: 600;
        color: #374151;
        display: flex;
        align-items: center;
        gap: 8px;
        
        &::before {
          font-size: 18px;
        }
        
        &:first-of-type::before {
          content: '📊';
        }
      }

      .section-description {
        margin: 0 0 20px 0;
        font-size: 14px;
        color: #6b7280;
        line-height: 1.6;
        background: #f9fafb;
        padding: 12px 16px;
        border-radius: 8px;
        border-left: 4px solid #e5e7eb;
      }

      .settings-form {
        max-width: 600px;

        :deep(.el-form-item) {
          margin-bottom: 28px;
          transition: all 0.2s ease;

          .el-form-item__label {
            font-weight: 600;
            color: #4b5563;
            margin-bottom: 8px;
          }

          .el-input,
          .el-select {
            border-radius: 8px;
          }
        }

        .visibility-option {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 12px 16px;
          border: 2px solid #e5e7eb;
          border-radius: 8px;
          transition: all 0.2s ease;
          cursor: pointer;

          &:hover {
            border-color: #409eff;
            background: #f0f9ff;
          }

          .option-icon {
            font-size: 18px;
            color: #409eff;
          }
        }
      }
    }
  }

  .indexing-modes {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;

    .mode-card {
      padding: 24px;
      border: 2px solid #e2e8f0;
      border-radius: 12px;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      position: relative;
      overflow: hidden;
      background: #ffffff;

      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 4px;
        background: linear-gradient(90deg, #409eff 0%, #36cfc9 100%);
        opacity: 0;
        transition: opacity 0.3s ease;
      }

      &:hover {
        border-color: #409eff;
        box-shadow: 0 6px 20px rgba(64, 158, 255, 0.15);
        transform: translateY(-2px);
        
        &::before {
          opacity: 0.3;
        }
      }

      &.active {
        border-color: #409eff;
        background: linear-gradient(135deg, rgba(64, 158, 255, 0.05) 0%, rgba(54, 207, 201, 0.05) 100%);
        box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
        
        &::before {
          opacity: 1;
        }
      }

      .mode-header {
        display: flex;
        align-items: flex-start;
        gap: 16px;
        margin-bottom: 16px;

        .mode-info {
          flex: 1;

          .mode-title {
            font-size: 18px;
            font-weight: 700;
            color: #1f2937;
            margin-bottom: 8px;
          }

          .mode-tag {
            margin-bottom: 12px;
            
            .el-tag {
              font-weight: 600;
              border-radius: 16px;
              padding: 4px 12px;
            }
          }
        }
      }

      .mode-description {
        font-size: 14px;
        color: #4b5563;
        line-height: 1.6;
        margin-bottom: 12px;
        background: #f9fafb;
        padding: 12px;
        border-radius: 6px;
      }

      .mode-details {
        font-size: 13px;
        color: #6b7280;
        line-height: 1.5;
        font-style: italic;
      }
    }
  }

  .model-select {
    width: 400px;

    .model-option {
      .model-name {
        font-weight: 500;
        color: #303133;
        margin-bottom: 2px;
      }

      .model-desc {
        font-size: 12px;
        color: #909399;
      }
    }
  }

  .retrieval-settings {
    .retrieval-method {
      margin-bottom: 24px;
      padding: 24px;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
      box-shadow: 0 1px 6px rgba(0, 0, 0, 0.02);
      transition: all 0.3s ease;

      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
        transform: translateY(-1px);
      }

      &:last-child {
        margin-bottom: 0;
      }

      .method-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 20px;

        .method-info {
          display: flex;
          gap: 16px;
          flex: 1;

          .method-icon {
            font-size: 24px;
            color: #409eff;
            margin-top: 2px;
            background: rgba(64, 158, 255, 0.1);
            width: 40px;
            height: 40px;
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
          }

          .method-content {
            h5 {
              margin: 0 0 8px 0;
              font-size: 16px;
              font-weight: 600;
              color: #1f2937;
            }

            p {
              margin: 0;
              font-size: 14px;
              color: #6b7280;
              line-height: 1.5;

              &.recommended {
                display: inline-block;
                margin: 0 0 8px 0;
                padding: 4px 12px;
                background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
                color: #fff;
                border-radius: 16px;
                font-size: 12px;
                font-weight: 600;
                box-shadow: 0 2px 4px rgba(103, 194, 58, 0.3);
              }
            }
          }
        }
        
        .el-switch {
          transform: scale(1.2);
        }
      }

      .method-config {
        padding-left: 56px;
        padding-top: 8px;
        border-top: 1px solid #f0f2f5;
        margin-top: 16px;

        .config-item {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 20px;
          padding: 12px 16px;
          background: rgba(255, 255, 255, 0.5);
          border-radius: 8px;
          transition: all 0.2s ease;

          &:hover {
            background: rgba(255, 255, 255, 0.8);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
          }

          &:last-child {
            margin-bottom: 0;
          }

          .config-label {
            font-size: 14px;
            font-weight: 600;
            color: #374151;
            min-width: 100px;
          }

          .config-control {
            display: flex;
            align-items: center;
            gap: 12px;
            flex: 1;
            justify-content: flex-end;

            .el-input-number {
              width: 120px;
            }

            .el-slider {
              width: 140px;
            }

            .info-icon {
              font-size: 16px;
              color: #9ca3af;
              cursor: help;
              transition: color 0.2s ease;
              
              &:hover {
                color: #409eff;
              }
            }
          }
        }
      }
    }
  }

  .settings-actions {
    display: flex;
    justify-content: flex-end;
    gap: 16px;
    padding: 24px;
    margin-top: 32px;
    background: #f8fafc;
    border-radius: 12px;
    border: 1px solid #e2e8f0;
    
    .el-button {
      border-radius: 8px;
      font-weight: 600;
      padding: 12px 24px;
      transition: all 0.3s ease;
      
      &.el-button--primary {
        box-shadow: 0 2px 6px rgba(64, 158, 255, 0.3);
        
        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .knowledge-base-settings {
    .indexing-modes {
      gap: 16px;
      
      .mode-card {
        padding: 20px;
      }
    }
  }
}

@media (max-width: 1024px) {
  .knowledge-base-settings {
    .indexing-modes {
      grid-template-columns: 1fr;
    }

    .model-select {
      width: 100%;
    }

    .retrieval-settings {
      .retrieval-method {
        .method-config {
          padding-left: 24px;

          .config-item {
            flex-direction: column;
            align-items: stretch;
            gap: 12px;

            .config-control {
              width: 100%;
              justify-content: flex-start;
            }
          }
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .knowledge-base-settings {
    padding: 16px;
    border-radius: 8px;
    
    .settings-header {
      margin-bottom: 32px;
      
      h3 {
        font-size: 18px;
        
        &::before {
          font-size: 20px;
        }
      }
    }

    .settings-content {
      .settings-section {
        padding: 20px;
        margin-bottom: 32px;
        
        .section-title {
          font-size: 16px;
        }
      }
      
      .settings-form {
        max-width: none;

        :deep(.el-form-item) {
          display: block;
          margin-bottom: 24px;

          .el-form-item__label {
            width: auto !important;
            margin-bottom: 8px;
            padding-right: 0;
          }

          .el-form-item__content {
            margin-left: 0 !important;
          }
        }
        
        .visibility-option {
          padding: 16px;
          
          .option-icon {
            font-size: 20px;
          }
        }
      }
    }
    
    .indexing-modes {
      .mode-card {
        padding: 20px;
        
        .mode-header {
          gap: 12px;
          
          .mode-info {
            .mode-title {
              font-size: 16px;
            }
          }
        }
      }
    }

    .retrieval-settings {
      .retrieval-method {
        padding: 20px;

        .method-header {
          flex-direction: column;
          align-items: stretch;
          gap: 16px;
          
          .method-info {
            .method-icon {
              width: 36px;
              height: 36px;
              font-size: 20px;
            }
            
            .method-content {
              h5 {
                font-size: 15px;
              }
            }
          }
          
          .el-switch {
            transform: scale(1.1);
            align-self: flex-start;
          }
        }
        
        .method-config {
          padding-left: 0;
          padding-top: 16px;
          margin-top: 16px;
          
          .config-item {
            padding: 16px;
            flex-direction: column;
            align-items: stretch;
            gap: 12px;
            
            .config-label {
              font-size: 14px;
              margin-bottom: 8px;
            }
            
            .config-control {
              width: 100%;
              justify-content: stretch;
              
              .el-input-number {
                width: 100%;
              }
              
              .el-slider {
                width: 100%;
              }
            }
          }
        }
      }
    }

    .settings-actions {
      flex-direction: column-reverse;
      gap: 12px;
      padding: 20px;
      margin-top: 24px;

      .el-button {
        width: 100%;
        padding: 14px 24px;
      }
    }
  }
}

@media (max-width: 480px) {
  .knowledge-base-settings {
    padding: 12px;
    
    .settings-header {
      margin-bottom: 24px;
      
      h3 {
        font-size: 16px;
      }
      
      .description {
        font-size: 13px;
      }
    }
    
    .settings-content {
      .settings-section {
        padding: 16px;
        margin-bottom: 24px;
        
        .section-title {
          font-size: 15px;
        }
        
        .section-description {
          padding: 10px 12px;
          font-size: 13px;
        }
      }
    }
    
    .indexing-modes {
      .mode-card {
        padding: 16px;
        
        .mode-description {
          padding: 10px;
          font-size: 13px;
        }
      }
    }
    
    .retrieval-settings {
      .retrieval-method {
        padding: 16px;
      }
    }
    
    .settings-actions {
      padding: 16px;
      margin-top: 20px;
    }
  }
}

// 动画效果
@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.8;
  }
}

.knowledge-base-settings {
  animation: slideInUp 0.5s ease-out;
  
  .settings-section {
    animation: slideInUp 0.6s ease-out;
  }
  
  .mode-card.active {
    animation: pulse 2s ease-in-out;
  }
}
</style>