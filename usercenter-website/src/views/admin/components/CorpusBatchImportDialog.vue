<template>
  <el-dialog
    v-model="visible"
    title="批量导入语料"
    width="700px"
    @close="handleClose"
  >
    <div class="import-content">
      <!-- 导入说明 -->
      <el-alert
        title="导入说明"
        type="info"
        :closable="false"
        style="margin-bottom: 16px"
      >
        <div class="import-tips">
          <p>1. 支持文本格式导入，每行一条语料</p>
          <p>2. 格式：语料内容 [TAB] 样本类型（可选，默认为POSITIVE）</p>
          <p>3. 样本类型：POSITIVE（正样本）、NEGATIVE（负样本）、BOUNDARY（边界样本）</p>
          <p>4. 示例：</p>
          <div class="example-text">
            你好<br>
            帮我查询余额&nbsp;&nbsp;&nbsp;&nbsp;POSITIVE<br>
            再见&nbsp;&nbsp;&nbsp;&nbsp;NEGATIVE
          </div>
        </div>
      </el-alert>

      <!-- 文本输入区域 -->
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="语料内容" prop="content">
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="12"
            placeholder="请输入语料内容，每行一条..."
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="默认类型" prop="defaultType">
          <el-select
            v-model="formData.defaultType"
            placeholder="请选择默认样本类型"
            style="width: 200px"
          >
            <el-option label="正样本" value="POSITIVE" />
            <el-option label="负样本" value="NEGATIVE" />
            <el-option label="边界样本" value="BOUNDARY" />
          </el-select>
          <div class="form-tip">未指定类型的语料将使用此默认类型</div>
        </el-form-item>
      </el-form>

      <!-- 解析预览 -->
      <div v-if="previewList.length > 0" class="preview-section">
        <h4>解析预览（前10条）</h4>
        <el-table
          :data="previewList.slice(0, 10)"
          size="small"
          stripe
          max-height="200px"
        >
          <el-table-column prop="text" label="语料内容" min-width="200" show-overflow-tooltip />
          <el-table-column prop="type" label="样本类型" width="120" align="center">
            <template #default="scope">
              <el-tag :type="getTypeTagType(scope.row.type)" size="small">
                {{ getTypeText(scope.row.type) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <div class="preview-summary">
          共解析 <strong>{{ previewList.length }}</strong> 条语料
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          type="info"
          @click="parseContent"
          :disabled="!formData.content.trim()"
        >
          解析预览
        </el-button>
        <el-button
          type="primary"
          :loading="loading.import"
          @click="handleImport"
          :disabled="previewList.length === 0"
        >
          确认导入
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { sampleApi } from '@/api/smartcs/intent'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  intentId: {
    type: [String, Number],
    default: null
  }
})

const emit = defineEmits(['update:visible', 'import'])

// 响应式数据
const formRef = ref()
const previewList = ref([])

const loading = reactive({
  import: false
})

const formData = reactive({
  content: '',
  defaultType: 'POSITIVE'
})

const formRules = {
  content: [
    { required: true, message: '请输入语料内容', trigger: 'blur' }
  ],
  defaultType: [
    { required: true, message: '请选择默认样本类型', trigger: 'change' }
  ]
}

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

// 方法
const parseContent = () => {
  if (!formData.content.trim()) {
    ElMessage.warning('请输入语料内容')
    return
  }
  
  const lines = formData.content.split('\n')
  const parsed = []
  
  lines.forEach((line, index) => {
    const trimmedLine = line.trim()
    if (!trimmedLine) return
    
    // 解析格式：文本内容 [TAB] 类型
    const parts = trimmedLine.split('\t')
    const text = parts[0].trim()
    const type = parts[1]?.trim() || formData.defaultType
    
    if (text && ['POSITIVE', 'NEGATIVE', 'BOUNDARY'].includes(type)) {
      parsed.push({
        text,
        type,
        source: 'batch_import'
      })
    } else if (text) {
      // 如果类型不合法，使用默认类型
      parsed.push({
        text,
        type: formData.defaultType,
        source: 'batch_import'
      })
    }
  })
  
  previewList.value = parsed
  
  if (parsed.length === 0) {
    ElMessage.warning('未解析到有效的语料数据')
  } else {
    ElMessage.success(`成功解析 ${parsed.length} 条语料`)
  }
}

const handleImport = async () => {
  if (previewList.value.length === 0) {
    ElMessage.warning('请先解析语料内容')
    return
  }
  
  loading.import = true
  try {
    const response = await sampleApi.batchImportSamples(props.intentId, {
      samples: previewList.value
    })
    
    if (response.success) {
      const result = response.data
      const successCount = result?.successCount || previewList.value.length
      const failureCount = result?.failureCount || 0
      
      if (failureCount > 0) {
        ElMessage.warning(`导入完成：成功 ${successCount} 条，失败 ${failureCount} 条`)
      } else {
        ElMessage.success(`导入成功：共 ${successCount} 条语料`)
      }
      
      emit('import')
      handleClose()
    } else {
      ElMessage.error(response.errMessage || '导入失败')
    }
  } catch (error) {
    console.error('批量导入语料失败:', error)
    ElMessage.error('导入失败: ' + error.message)
  } finally {
    loading.import = false
  }
}

const handleClose = () => {
  visible.value = false
  resetForm()
}

const resetForm = () => {
  Object.assign(formData, {
    content: '',
    defaultType: 'POSITIVE'
  })
  previewList.value = []
  
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const getTypeTagType = (type) => {
  switch (type) {
    case 'POSITIVE':
      return 'success'
    case 'NEGATIVE':
      return 'danger'
    case 'BOUNDARY':
      return 'warning'
    default:
      return 'info'
  }
}

const getTypeText = (type) => {
  switch (type) {
    case 'POSITIVE':
      return '正样本'
    case 'NEGATIVE':
      return '负样本'
    case 'BOUNDARY':
      return '边界样本'
    default:
      return '未知'
  }
}
</script>

<style scoped>
.import-content {
  padding: 0;
}

.import-tips {
  font-size: 14px;
  line-height: 1.6;
}

.import-tips p {
  margin: 4px 0;
}

.example-text {
  background-color: #f5f5f5;
  padding: 8px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 12px;
  margin-top: 8px;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.preview-section {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.preview-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #333;
}

.preview-summary {
  margin-top: 8px;
  font-size: 14px;
  color: #666;
  text-align: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>