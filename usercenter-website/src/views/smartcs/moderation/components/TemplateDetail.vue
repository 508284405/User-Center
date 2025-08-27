<template>
  <div class="template-detail">
    <div class="detail-header">
      <div class="template-info">
        <div class="template-tags">
          <el-tag :type="getTemplateTypeTagType(template.templateType)">
            {{ getTemplateTypeText(template.templateType) }}
          </el-tag>
          <el-tag v-if="template.language" size="small">
            {{ template.language.toUpperCase() }}
          </el-tag>
          <el-tag v-if="!template.isActive" type="info" size="small">
            已禁用
          </el-tag>
        </div>
        <p class="template-desc">{{ template.description || '暂无描述' }}</p>
      </div>
      <div class="template-version">
        版本 {{ template.version || '1.0' }}
      </div>
    </div>

    <el-divider />

    <div class="detail-content">
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <div class="tab-content">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="模板名称">
                {{ template.name }}
              </el-descriptions-item>
              <el-descriptions-item label="模板编码">
                <code>{{ template.code }}</code>
              </el-descriptions-item>
              <el-descriptions-item label="模板类型">
                <el-tag :type="getTemplateTypeTagType(template.templateType)">
                  {{ getTemplateTypeText(template.templateType) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="语言">
                {{ template.language?.toUpperCase() || '未指定' }}
              </el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="template.isActive ? 'success' : 'info'">
                  {{ template.isActive ? '启用' : '禁用' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="版本">
                {{ template.version || '1.0' }}
              </el-descriptions-item>
              <el-descriptions-item label="创建者">
                {{ template.createdBy || '系统' }}
              </el-descriptions-item>
              <el-descriptions-item label="更新者">
                {{ template.updatedBy || '系统' }}
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">
                {{ formatDate(template.createdAt) }}
              </el-descriptions-item>
              <el-descriptions-item label="更新时间">
                {{ formatDate(template.updatedAt) }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-tab-pane>

        <!-- Prompt模板 -->
        <el-tab-pane label="Prompt模板" name="prompt">
          <div class="tab-content">
            <div class="template-editor">
              <div class="editor-header">
                <span class="editor-title">主要Prompt模板</span>
                <div class="editor-actions">
                  <el-button size="small" @click="copyTemplate('promptTemplate')">
                    <el-icon><CopyDocument /></el-icon>
                    复制
                  </el-button>
                  <el-button size="small" @click="formatTemplate('promptTemplate')">
                    <el-icon><MagicStick /></el-icon>
                    格式化
                  </el-button>
                </div>
              </div>
              <div class="editor-content">
                <el-input
                  v-model="displayPromptTemplate"
                  type="textarea"
                  :rows="12"
                  readonly
                  placeholder="暂无模板内容"
                />
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 维度模板 -->
        <el-tab-pane label="维度模板" name="dimension">
          <div class="tab-content">
            <div class="template-editor">
              <div class="editor-header">
                <span class="editor-title">维度模板</span>
                <div class="editor-actions">
                  <el-button size="small" @click="copyTemplate('dimensionTemplate')">
                    <el-icon><CopyDocument /></el-icon>
                    复制
                  </el-button>
                </div>
              </div>
              <div class="editor-content">
                <el-input
                  v-model="displayDimensionTemplate"
                  type="textarea"
                  :rows="8"
                  readonly
                  placeholder="暂无维度模板"
                />
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 响应模板 -->
        <el-tab-pane label="响应模板" name="response">
          <div class="tab-content">
            <div class="template-editor">
              <div class="editor-header">
                <span class="editor-title">响应模板</span>
                <div class="editor-actions">
                  <el-button size="small" @click="copyTemplate('responseTemplate')">
                    <el-icon><CopyDocument /></el-icon>
                    复制
                  </el-button>
                </div>
              </div>
              <div class="editor-content">
                <el-input
                  v-model="displayResponseTemplate"
                  type="textarea"
                  :rows="8"
                  readonly
                  placeholder="暂无响应模板"
                />
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 变量配置 -->
        <el-tab-pane label="变量配置" name="variables">
          <div class="tab-content">
            <el-row :gutter="24">
              <el-col :span="12">
                <div class="variable-section">
                  <h4 class="section-title">可用变量</h4>
                  <div v-if="template.variables && Object.keys(template.variables).length > 0">
                    <div
                      v-for="[key, value] in Object.entries(template.variables)"
                      :key="key"
                      class="variable-item"
                    >
                      <div class="variable-name">
                        <code>&#123;&#123;{{ key }}&#125;&#125;</code>
                      </div>
                      <div class="variable-desc">{{ value }}</div>
                    </div>
                  </div>
                  <div v-else class="empty-variables">
                    <el-icon><Box /></el-icon>
                    <span>暂无定义变量</span>
                  </div>
                </div>
              </el-col>
              
              <el-col :span="12">
                <div class="variable-section">
                  <h4 class="section-title">默认值</h4>
                  <div v-if="template.defaultValues && Object.keys(template.defaultValues).length > 0">
                    <div
                      v-for="[key, value] in Object.entries(template.defaultValues)"
                      :key="key"
                      class="variable-item"
                    >
                      <div class="variable-name">
                        <code>{{ key }}</code>
                      </div>
                      <div class="variable-value">{{ formatVariableValue(value) }}</div>
                    </div>
                  </div>
                  <div v-else class="empty-variables">
                    <el-icon><Box /></el-icon>
                    <span>暂无默认值</span>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { CopyDocument, MagicStick, Box } from '@element-plus/icons-vue'
import type { ModerationPolicyTemplate } from '@/api/smartcs/moderation'

interface Props {
  template: ModerationPolicyTemplate
}

const props = defineProps<Props>()

const activeTab = ref('basic')

// 格式化后的模板内容
const displayPromptTemplate = computed(() => {
  return props.template.promptTemplate || ''
})

const displayDimensionTemplate = computed(() => {
  return props.template.dimensionTemplate || ''
})

const displayResponseTemplate = computed(() => {
  return props.template.responseTemplate || ''
})

// 复制模板内容
const copyTemplate = async (templateType: string) => {
  let content = ''
  switch (templateType) {
    case 'promptTemplate':
      content = props.template.promptTemplate || ''
      break
    case 'dimensionTemplate':
      content = props.template.dimensionTemplate || ''
      break
    case 'responseTemplate':
      content = props.template.responseTemplate || ''
      break
  }

  if (!content) {
    ElMessage.warning('模板内容为空')
    return
  }

  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('模板内容已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  }
}

// 格式化模板（简单的JSON格式化）
const formatTemplate = (templateType: string) => {
  // 这里可以实现更复杂的模板格式化逻辑
  ElMessage.info('格式化功能待实现')
}

// 工具函数
const getTemplateTypeText = (type: string) => {
  const map: Record<string, string> = {
    'DETAILED': '详细审核',
    'QUICK': '快速审核',
    'STRUCTURED': '结构化审核'
  }
  return map[type] || type
}

const getTemplateTypeTagType = (type: string) => {
  const map: Record<string, string> = {
    'DETAILED': 'primary',
    'QUICK': 'success',
    'STRUCTURED': 'warning'
  }
  return map[type] || ''
}

const formatDate = (timestamp: number | undefined) => {
  if (!timestamp) return '-'
  return new Date(timestamp).toLocaleString('zh-CN')
}

const formatVariableValue = (value: any) => {
  if (typeof value === 'object') {
    return JSON.stringify(value)
  }
  return String(value)
}
</script>

<style scoped lang="scss">
.template-detail {
  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;

    .template-info {
      flex: 1;

      .template-tags {
        display: flex;
        gap: 6px;
        margin-bottom: 8px;
        flex-wrap: wrap;
      }

      .template-desc {
        color: #606266;
        line-height: 1.5;
        margin: 0;
      }
    }

    .template-version {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      padding: 6px 16px;
      border-radius: 20px;
      font-size: 14px;
      font-weight: 500;
    }
  }

  .detail-content {
    .tab-content {
      padding: 16px 0;
    }

    .template-editor {
      .editor-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;

        .editor-title {
          font-weight: 500;
          color: #303133;
        }

        .editor-actions {
          display: flex;
          gap: 8px;
        }
      }

      .editor-content {
        :deep(.el-textarea__inner) {
          font-family: 'Courier New', monospace;
          font-size: 13px;
          line-height: 1.5;
          background: #f8f9fa;
          border: 1px solid #e4e7ed;
        }
      }
    }

    .variable-section {
      .section-title {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 16px 0;
        padding-bottom: 8px;
        border-bottom: 2px solid #e4e7ed;
      }

      .variable-item {
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;

        &:last-child {
          border-bottom: none;
        }

        .variable-name {
          margin-bottom: 4px;

          code {
            background: #f0f8ff;
            color: #409eff;
            padding: 2px 6px;
            border-radius: 4px;
            font-size: 13px;
          }
        }

        .variable-desc {
          color: #606266;
          font-size: 13px;
          line-height: 1.4;
        }

        .variable-value {
          color: #303133;
          font-size: 13px;
          font-family: 'Courier New', monospace;
          background: #f5f7fa;
          padding: 4px 8px;
          border-radius: 4px;
          word-break: break-all;
        }
      }

      .empty-variables {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        padding: 40px 20px;
        color: #909399;
        background: #fafafa;
        border-radius: 6px;
        text-align: center;
      }
    }
  }
}

// Element Plus 样式自定义
:deep(.el-descriptions__label) {
  font-weight: 500;
  width: 120px;
}

:deep(.el-descriptions__content) {
  code {
    background: #f5f7fa;
    color: #303133;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 13px;
  }
}

:deep(.el-tabs__content) {
  padding: 0;
}
</style>
