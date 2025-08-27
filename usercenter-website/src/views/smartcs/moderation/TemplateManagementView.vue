<template>
  <div class="template-management">
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <el-icon><Document /></el-icon>
          模板管理
        </h2>
        <p class="page-subtitle">管理审核策略使用的prompt模板，支持多语言和动态变量</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增模板
        </el-button>
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 模板列表 -->
    <div class="template-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span class="card-title">策略模板</span>
            <span class="template-count">共 {{ templates.length }} 个模板</span>
          </div>
        </template>

        <div v-loading="loading" class="templates-container">
          <div
            v-for="template in templates"
            :key="template.id"
            class="template-card"
            @click="handleViewTemplate(template)"
          >
            <div class="card-header">
              <div class="template-info">
                <h3 class="template-name">{{ template.name }}</h3>
                <p class="template-code">{{ template.code }}</p>
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
              </div>
              <div class="template-version">
                v{{ template.version || '1.0' }}
              </div>
            </div>

            <div class="card-content">
              <p class="template-desc">
                {{ template.description || '暂无描述' }}
              </p>
              
              <div class="template-preview">
                <div class="preview-label">模板预览：</div>
                <div class="preview-content">
                  {{ truncateText(template.promptTemplate, 200) }}
                </div>
              </div>

              <div class="template-meta">
                <div class="meta-item">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ formatDate(template.updatedAt) }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><User /></el-icon>
                  <span>{{ template.updatedBy || '系统' }}</span>
                </div>
              </div>
            </div>

            <div class="card-actions">
              <el-button type="primary" link @click.stop="handleViewTemplate(template)">
                查看详情
              </el-button>
              <el-button type="primary" link @click.stop="handleTestTemplate(template)">
                模板测试
              </el-button>
              <el-button type="primary" link @click.stop="handleEdit(template)">
                编辑
              </el-button>
              <el-button type="danger" link @click.stop="handleDelete(template)">
                删除
              </el-button>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="templates.length === 0 && !loading" class="empty-state">
            <el-icon><Box /></el-icon>
            <p>暂无模板数据</p>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 模板详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="`模板详情 - ${currentTemplate?.name}`"
      width="1000px"
      top="5vh"
    >
      <template-detail
        v-if="detailDialogVisible && currentTemplate"
        :template="currentTemplate"
      />
    </el-dialog>

    <!-- 模板测试对话框 -->
    <el-dialog
      v-model="testDialogVisible"
      :title="`模板测试 - ${currentTemplate?.name}`"
      width="800px"
    >
      <template-test
        v-if="testDialogVisible && currentTemplate"
        :template="currentTemplate"
      />
    </el-dialog>

    <!-- 新增/编辑模板对话框 -->
    <el-dialog
      v-model="formDialogVisible"
      :title="isEditing ? '编辑模板' : '新增模板'"
      width="800px"
    >
      <template-form
        v-if="formDialogVisible"
        :template="currentTemplate"
        :is-editing="isEditing"
        @submit="handleFormSubmit"
        @cancel="formDialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Calendar, User, Box, Plus, Refresh } from '@element-plus/icons-vue'
import { moderationApi, type ModerationPolicyTemplate } from '@/api/smartcs/moderation'
import TemplateDetail from './components/TemplateDetail.vue'
import TemplateTest from './components/TemplateTest.vue'
import TemplateForm from './components/TemplateForm.vue'

// 数据状态
const loading = ref(false)
const templates = ref<ModerationPolicyTemplate[]>([])
const detailDialogVisible = ref(false)
const testDialogVisible = ref(false)
const currentTemplate = ref<ModerationPolicyTemplate | null>(null)
const formDialogVisible = ref(false)
const isEditing = ref(false)

// 页面加载
onMounted(() => {
  loadData()
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const response = await moderationApi.getAllActiveTemplates()
    templates.value = response.data
  } catch (error) {
    console.error('加载模板数据失败:', error)
    ElMessage.error('加载模板数据失败')
  } finally {
    loading.value = false
  }
}

// 查看模板详情
const handleViewTemplate = (template: ModerationPolicyTemplate) => {
  currentTemplate.value = template
  detailDialogVisible.value = true
}

// 测试模板
const handleTestTemplate = (template: ModerationPolicyTemplate) => {
  currentTemplate.value = template
  testDialogVisible.value = true
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

const truncateText = (text: string, maxLength: number) => {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

const formatDate = (timestamp: number | undefined) => {
  if (!timestamp) return '-'
  return new Date(timestamp).toLocaleDateString('zh-CN')
}

// 新增模板
const handleCreate = () => {
  currentTemplate.value = null
  isEditing.value = false
  formDialogVisible.value = true
}

// 编辑模板
const handleEdit = (template: ModerationPolicyTemplate) => {
  currentTemplate.value = template
  isEditing.value = true
  formDialogVisible.value = true
}

// 删除模板
const handleDelete = async (template: ModerationPolicyTemplate) => {
  try {
    await ElMessageBox.confirm(
      `确认删除模板 "${template.name}" 吗？删除后无法恢复。`,
      '删除确认',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await moderationApi.deleteTemplate(template.id!)
    ElMessage.success('删除成功')
    await loadData()
  } catch (error: any) {
    if (error === 'cancel') return
    console.error('删除失败:', error)
    ElMessage.error(error?.message || '删除失败')
  }
}

// 刷新数据
const handleRefresh = () => {
  loadData()
}

// 表单提交
const handleFormSubmit = async (formData: any) => {
  try {
    if (isEditing.value) {
      await moderationApi.updateTemplate(currentTemplate.value!.id!, formData)
      ElMessage.success('更新成功')
    } else {
      await moderationApi.createTemplate(formData)
      ElMessage.success('创建成功')
    }
    
    formDialogVisible.value = false
    await loadData()
  } catch (error: any) {
    console.error('操作失败:', error)
    ElMessage.error(error?.message || '操作失败')
  }
}
</script>

<style scoped lang="scss">
.template-management {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-bottom: 24px;

    .header-content {
      .page-title {
        font-size: 24px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 8px 0;
        display: flex;
        align-items: center;
        gap: 8px;
      }

      .page-subtitle {
        color: #606266;
        font-size: 14px;
        margin: 0;
      }
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .template-section {
    .el-card {
      border-radius: 8px;
      border: none;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .card-title {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
        }

        .template-count {
          color: #909399;
          font-size: 14px;
        }
      }

      .templates-container {
        .template-card {
          border: 1px solid #e4e7ed;
          border-radius: 8px;
          padding: 20px;
          margin-bottom: 16px;
          cursor: pointer;
          transition: all 0.3s ease;

          &:hover {
            border-color: #409eff;
            box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
          }

          &:last-child {
            margin-bottom: 0;
          }

          .card-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 16px;

            .template-info {
              flex: 1;

              .template-name {
                font-size: 18px;
                font-weight: 600;
                color: #303133;
                margin: 0 0 4px 0;
              }

              .template-code {
                font-family: 'Courier New', monospace;
                color: #909399;
                font-size: 13px;
                margin: 0 0 8px 0;
              }

              .template-tags {
                display: flex;
                gap: 6px;
                flex-wrap: wrap;
              }
            }

            .template-version {
              background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
              color: white;
              padding: 4px 12px;
              border-radius: 16px;
              font-size: 12px;
              font-weight: 500;
            }
          }

          .card-content {
            .template-desc {
              color: #606266;
              line-height: 1.5;
              margin: 0 0 16px 0;
            }

            .template-preview {
              background: #f8f9fa;
              border-radius: 6px;
              padding: 12px;
              margin-bottom: 16px;

              .preview-label {
                color: #909399;
                font-size: 12px;
                margin-bottom: 6px;
              }

              .preview-content {
                font-family: 'Courier New', monospace;
                font-size: 13px;
                color: #303133;
                line-height: 1.4;
                white-space: pre-wrap;
                word-break: break-word;
              }
            }

            .template-meta {
              display: flex;
              gap: 20px;

              .meta-item {
                display: flex;
                align-items: center;
                gap: 4px;
                color: #909399;
                font-size: 13px;
              }
            }
          }

          .card-actions {
            display: flex;
            gap: 12px;
            padding-top: 16px;
            border-top: 1px solid #f0f0f0;
            flex-wrap: wrap;

            .el-button {
              flex-shrink: 0;
            }
          }
        }

        .empty-state {
          text-align: center;
          padding: 60px 20px;
          color: #909399;

          .el-icon {
            font-size: 64px;
            margin-bottom: 16px;
          }

          p {
            margin: 0;
            font-size: 16px;
          }
        }
      }
    }
  }
}
</style>