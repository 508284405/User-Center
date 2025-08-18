<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    title="批量导入关键词规则"
    width="600px"
    :close-on-click-modal="false"
  >
    <div class="import-dialog">
      <!-- 导入方式选择 -->
      <el-card class="method-card">
        <template #header>
          <span>导入方式</span>
        </template>
        
        <el-radio-group v-model="importMethod" class="method-group">
          <el-radio label="file">
            <el-icon><Document /></el-icon>
            文件导入
          </el-radio>
          <el-radio label="text">
            <el-icon><EditPen /></el-icon>
            文本导入
          </el-radio>
        </el-radio-group>
      </el-card>

      <!-- 基本配置 -->
      <el-card class="config-card">
        <template #header>
          <span>基本配置</span>
        </template>
        
        <el-form :model="configData" label-width="120px" class="config-form">
          <el-form-item label="违规分类" required>
            <el-select v-model="configData.categoryId" placeholder="选择默认分类" style="width: 100%">
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              >
                <div class="category-option">
                  <span>{{ category.name }}</span>
                  <el-tag size="small" :type="getSeverityTag(category.severityLevel)">
                    {{ getSeverityLabel(category.severityLevel) }}
                  </el-tag>
                </div>
              </el-option>
            </el-select>
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="规则类型">
                <el-select v-model="configData.ruleType" style="width: 100%">
                  <el-option label="精确匹配" value="EXACT" />
                  <el-option label="模糊匹配" value="FUZZY" />
                  <el-option label="子字符串" value="SUBSTRING" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="语言">
                <el-select v-model="configData.language" style="width: 100%">
                  <el-option label="中文" value="zh-CN" />
                  <el-option label="英文" value="en-US" />
                  <el-option label="其他" value="other" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="优先级">
                <el-input-number
                  v-model="configData.priority"
                  :min="1"
                  :max="9999"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="严重度权重">
                <el-input-number
                  v-model="configData.severityWeight"
                  :min="0.1"
                  :max="10"
                  :step="0.1"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="默认启用">
            <el-switch v-model="configData.isActive" />
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 文件导入 -->
      <el-card v-if="importMethod === 'file'" class="import-card">
        <template #header>
          <span>文件导入</span>
          <div class="header-actions">
            <el-button type="text" size="small" @click="downloadTemplate">
              <el-icon><Download /></el-icon>
              下载模板
            </el-button>
          </div>
        </template>
        
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :show-file-list="true"
          :limit="1"
          accept=".csv,.txt,.xlsx"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          class="upload-area"
        >
          <el-button type="primary">选择文件</el-button>
          <template #tip>
            <div class="upload-tip">
              <p>支持 CSV、TXT、XLSX 格式文件</p>
              <p>文件大小不超过 10MB，单次最多导入 10000 条记录</p>
            </div>
          </template>
        </el-upload>

        <!-- 文件预览 -->
        <div v-if="filePreview.length > 0" class="file-preview">
          <h4>文件预览 (前 10 行)</h4>
          <el-table :data="filePreview" size="small" border>
            <el-table-column 
              v-for="(column, index) in previewColumns" 
              :key="index"
              :prop="column.prop"
              :label="column.label"
              show-overflow-tooltip
            />
          </el-table>
        </div>
      </el-card>

      <!-- 文本导入 -->
      <el-card v-if="importMethod === 'text'" class="import-card">
        <template #header>
          <span>文本导入</span>
        </template>
        
        <div class="text-import">
          <el-input
            v-model="textContent"
            type="textarea"
            :rows="10"
            placeholder="请输入关键词，每行一个，或用逗号分隔&#10;例如：&#10;敏感词1&#10;敏感词2&#10;或：敏感词1,敏感词2,敏感词3"
            maxlength="50000"
            show-word-limit
            class="text-input"
          />
          
          <div class="text-config">
            <el-form inline>
              <el-form-item label="分隔符">
                <el-radio-group v-model="textSeparator">
                  <el-radio label="line">换行</el-radio>
                  <el-radio label="comma">逗号</el-radio>
                  <el-radio label="semicolon">分号</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="parseTextContent">
                  <el-icon><Tools /></el-icon>
                  解析预览
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 解析预览 -->
          <div v-if="parsedKeywords.length > 0" class="parsed-preview">
            <h4>解析结果 (共 {{ parsedKeywords.length }} 个关键词)</h4>
            <div class="keywords-list">
              <el-tag 
                v-for="(keyword, index) in parsedKeywords.slice(0, 50)" 
                :key="index"
                size="small"
                class="keyword-tag"
              >
                {{ keyword }}
              </el-tag>
              <span v-if="parsedKeywords.length > 50" class="more-indicator">
                ...还有 {{ parsedKeywords.length - 50 }} 个
              </span>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 导入选项 -->
      <el-card class="options-card">
        <template #header>
          <span>导入选项</span>
        </template>
        
        <el-form inline>
          <el-form-item label="重复处理">
            <el-radio-group v-model="importOptions.duplicateHandling">
              <el-radio label="skip">跳过</el-radio>
              <el-radio label="update">更新</el-radio>
              <el-radio label="error">报错</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="批量大小">
            <el-input-number
              v-model="importOptions.batchSize"
              :min="100"
              :max="1000"
              :step="100"
            />
          </el-form-item>
          
          <el-form-item label="验证规则">
            <el-switch v-model="importOptions.validateRules" />
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 导入进度 -->
      <el-card v-if="importing" class="progress-card">
        <template #header>
          <span>导入进度</span>
        </template>
        
        <div class="import-progress">
          <el-progress 
            :percentage="importProgress.percentage" 
            :status="importProgress.status"
            :stroke-width="8"
          />
          <div class="progress-info">
            <span>{{ importProgress.message }}</span>
            <span class="progress-detail">
              {{ importProgress.processed }} / {{ importProgress.total }}
            </span>
          </div>
        </div>
      </el-card>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel" :disabled="importing">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleImport" 
          :loading="importing"
          :disabled="!canImport"
        >
          开始导入
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, EditPen, Download, Tools } from '@element-plus/icons-vue'
import moderationApi, { type ModerationCategory } from '@/api/smartcs/moderation'

// Props 和 Emits
interface Props {
  modelValue: boolean
  categories: ModerationCategory[]
}

defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

// 响应式数据
const uploadRef = ref()
const importing = ref(false)
const importMethod = ref<'file' | 'text'>('file')
const textContent = ref('')
const textSeparator = ref<'line' | 'comma' | 'semicolon'>('line')
const parsedKeywords = ref<string[]>([])
const filePreview = ref<any[]>([])
const previewColumns = ref<any[]>([])
const selectedFile = ref<File | null>(null)

const configData = reactive({
  categoryId: undefined as number | undefined,
  ruleType: 'EXACT' as 'EXACT' | 'FUZZY' | 'SUBSTRING',
  language: 'zh-CN',
  priority: 100,
  severityWeight: 1.0,
  isActive: true
})

const importOptions = reactive({
  duplicateHandling: 'skip' as 'skip' | 'update' | 'error',
  batchSize: 500,
  validateRules: true
})

const importProgress = reactive({
  percentage: 0,
  status: 'success' as 'success' | 'exception' | 'warning',
  message: '',
  processed: 0,
  total: 0
})

// 计算属性
const canImport = computed(() => {
  if (!configData.categoryId) return false
  
  if (importMethod.value === 'file') {
    return selectedFile.value !== null
  } else {
    return parsedKeywords.value.length > 0
  }
})

// 方法定义
const handleFileChange = (file: any) => {
  selectedFile.value = file.raw
  parseFileContent(file.raw)
}

const handleFileRemove = () => {
  selectedFile.value = null
  filePreview.value = []
  previewColumns.value = []
}

const parseFileContent = (file: File) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    const content = e.target?.result as string
    
    if (file.name.endsWith('.csv') || file.name.endsWith('.txt')) {
      parseCSVContent(content)
    } else {
      ElMessage.warning('暂不支持此文件格式的预览')
    }
  }
  reader.readAsText(file, 'UTF-8')
}

const parseCSVContent = (content: string) => {
  const lines = content.split('\n').filter(line => line.trim())
  if (lines.length === 0) return
  
  // 假设第一行是标题
  const headers = lines[0].split(',').map(h => h.trim())
  previewColumns.value = headers.map((header, index) => ({
    prop: `col${index}`,
    label: header || `列${index + 1}`
  }))
  
  // 解析数据行（最多10行预览）
  const dataLines = lines.slice(1, 11)
  filePreview.value = dataLines.map(line => {
    const values = line.split(',').map(v => v.trim())
    const row: any = {}
    values.forEach((value, index) => {
      row[`col${index}`] = value
    })
    return row
  })
}

const parseTextContent = () => {
  if (!textContent.value.trim()) {
    ElMessage.warning('请输入要导入的关键词')
    return
  }
  
  let keywords: string[] = []
  
  switch (textSeparator.value) {
    case 'line':
      keywords = textContent.value.split('\n')
      break
    case 'comma':
      keywords = textContent.value.split(',')
      break
    case 'semicolon':
      keywords = textContent.value.split(';')
      break
  }
  
  // 清理和去重
  keywords = keywords
    .map(k => k.trim())
    .filter(k => k.length > 0)
    .filter((k, index, arr) => arr.indexOf(k) === index)
  
  parsedKeywords.value = keywords
  
  if (keywords.length === 0) {
    ElMessage.warning('未解析到有效的关键词')
  } else {
    ElMessage.success(`成功解析 ${keywords.length} 个关键词`)
  }
}

const downloadTemplate = () => {
  const template = `规则名称,关键词,规则类型,优先级,严重度权重,描述
示例规则1,敏感词1,EXACT,100,1.0,这是一个示例规则
示例规则2,敏感词2,FUZZY,200,1.5,这是另一个示例规则`
  
  const blob = new Blob(['\uFEFF' + template], { type: 'text/csv;charset=utf-8' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = 'keyword_rules_template.csv'
  link.click()
  URL.revokeObjectURL(link.href)
}

const handleImport = async () => {
  if (!configData.categoryId) {
    ElMessage.error('请选择违规分类')
    return
  }
  
  importing.value = true
  importProgress.percentage = 0
  importProgress.status = 'success'
  importProgress.processed = 0
  
  try {
    if (importMethod.value === 'file') {
      await importFromFile()
    } else {
      await importFromText()
    }
    
    ElMessage.success('导入成功')
    emit('success')
  } catch (error: any) {
    console.error('Import failed:', error)
    ElMessage.error('导入失败: ' + (error.message || '未知错误'))
    importProgress.status = 'exception'
  } finally {
    importing.value = false
  }
}

const importFromFile = async () => {
  if (!selectedFile.value) return
  
  importProgress.message = '正在上传文件...'
  importProgress.total = 1
  
  try {
    await moderationApi.importRules(selectedFile.value, configData.categoryId!)
    importProgress.percentage = 100
    importProgress.processed = 1
    importProgress.message = '文件导入完成'
  } catch (error) {
    throw error
  }
}

const importFromText = async () => {
  const keywords = parsedKeywords.value
  importProgress.total = keywords.length
  importProgress.message = '正在批量创建规则...'
  
  const batchSize = importOptions.batchSize
  let processed = 0
  
  for (let i = 0; i < keywords.length; i += batchSize) {
    const batch = keywords.slice(i, i + batchSize)
    
    const promises = batch.map(keyword => {
      const ruleData = {
        ruleName: `导入规则_${keyword}`,
        keyword,
        categoryId: configData.categoryId!,
        ruleType: configData.ruleType,
        language: configData.language,
        priority: configData.priority,
        severityWeight: configData.severityWeight,
        isActive: configData.isActive,
        matchMode: 'FULL',
        caseSensitive: false,
        source: 'IMPORT'
      }
      
      return moderationApi.createRule(ruleData)
    })
    
    await Promise.all(promises)
    
    processed += batch.length
    importProgress.processed = processed
    importProgress.percentage = Math.round((processed / keywords.length) * 100)
    importProgress.message = `正在处理 ${processed}/${keywords.length}`
    
    // 短暂延迟避免过快请求
    if (i + batchSize < keywords.length) {
      await new Promise(resolve => setTimeout(resolve, 100))
    }
  }
  
  importProgress.message = '导入完成'
}

const handleCancel = () => {
  emit('update:modelValue', false)
}

// 辅助函数
const getSeverityTag = (level: string) => {
  const tags: Record<string, string> = {
    LOW: 'success',
    MEDIUM: 'warning',
    HIGH: 'danger',
    CRITICAL: 'danger'
  }
  return tags[level] || ''
}

const getSeverityLabel = (level: string) => {
  const labels: Record<string, string> = {
    LOW: '低',
    MEDIUM: '中',
    HIGH: '高',
    CRITICAL: '极高'
  }
  return labels[level] || level
}
</script>

<style scoped lang="scss">
.import-dialog {
  .method-card,
  .config-card,
  .import-card,
  .options-card,
  .progress-card {
    margin-bottom: 16px;
    
    &:last-child {
      margin-bottom: 0;
    }
  }

  .method-group {
    .el-radio {
      display: flex;
      align-items: center;
      margin-right: 24px;
      margin-bottom: 12px;
    }
  }

  .category-option {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }

  .upload-area {
    .upload-tip {
      margin-top: 8px;
      
      p {
        margin: 4px 0;
        font-size: 12px;
        color: #909399;
      }
    }
  }

  .file-preview {
    margin-top: 16px;
    
    h4 {
      margin: 0 0 12px 0;
      color: #303133;
    }
  }

  .text-import {
    .text-input {
      margin-bottom: 16px;
    }

    .text-config {
      margin-bottom: 16px;
    }

    .parsed-preview {
      h4 {
        margin: 0 0 12px 0;
        color: #303133;
      }

      .keywords-list {
        max-height: 200px;
        overflow-y: auto;
        padding: 12px;
        background-color: #f5f7fa;
        border-radius: 4px;

        .keyword-tag {
          margin: 4px 8px 4px 0;
        }

        .more-indicator {
          color: #909399;
          font-style: italic;
        }
      }
    }
  }

  .import-progress {
    .progress-info {
      display: flex;
      justify-content: space-between;
      margin-top: 8px;
      font-size: 14px;
      color: #606266;

      .progress-detail {
        color: #909399;
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-card__header) {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

// 响应式设计
@media (max-width: 768px) {
  .method-group {
    .el-radio {
      display: block;
      margin-right: 0;
    }
  }
}
</style>