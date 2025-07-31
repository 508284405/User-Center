<template>
  <div class="variable-manager">
    <div class="section-header">
      <div class="title-row">
        <span class="title">变量</span>
        <el-button text type="primary" @click="showAddDialog" size="small">
          <el-icon><Plus /></el-icon>
          添加
        </el-button>
      </div>
      <div class="section-description">
        用户输入此变量值对应的内容
      </div>
    </div>

    <div class="variables-list">
      <div 
        v-for="(variable, index) in variables" 
        :key="variable.key"
        class="variable-item"
      >
        <div class="variable-header">
          <div class="variable-info">
            <span class="variable-key">{{ variable.key }}</span>
            <el-tag 
              :type="variable.required ? 'danger' : 'info'" 
              size="small"
              class="required-tag"
            >
              {{ variable.required ? 'REQUIRED' : 'OPTIONAL' }}
            </el-tag>
            <el-tag size="small" class="type-tag">
              {{ getTypeLabel(variable.type) }}
            </el-tag>
          </div>
          <div class="variable-actions">
            <el-button text @click="editVariable(index)" size="small">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button text @click="removeVariable(index)" size="small" type="danger">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
        
        <div class="variable-details" v-if="variable.label !== variable.key || variable.defaultValue">
          <div class="variable-label" v-if="variable.label !== variable.key">
            <span class="label">显示名称:</span>
            <span class="value">{{ variable.label }}</span>
          </div>
          <div class="variable-default" v-if="variable.defaultValue">
            <span class="label">默认值:</span>
            <span class="value">{{ variable.defaultValue }}</span>
          </div>
        </div>
      </div>

      <div v-if="variables.length === 0" class="empty-state">
        <div class="empty-icon">📝</div>
        <div class="empty-text">暂无变量</div>
        <div class="empty-description">在提示词中使用 {{变量名}} 格式会自动检测变量</div>
      </div>
    </div>

    <!-- 变量编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingIndex >= 0 ? '编辑变量' : '添加变量'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="变量名" prop="key">
          <el-input
            v-model="formData.key"
            placeholder="请输入变量名"
            :disabled="editingIndex >= 0"
          />
          <div class="form-tip">变量名只能包含字母、数字和下划线</div>
        </el-form-item>

        <el-form-item label="显示名称" prop="label">
          <el-input
            v-model="formData.label"
            placeholder="请输入显示名称"
          />
        </el-form-item>

        <el-form-item label="变量类型" prop="type">
          <el-select v-model="formData.type" style="width: 100%">
            <el-option
              v-for="type in variableTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="是否必填" prop="required">
          <el-switch v-model="formData.required" />
        </el-form-item>

        <el-form-item label="默认值" prop="defaultValue" v-if="!formData.required">
          <el-input
            v-model="formData.defaultValue"
            :type="formData.type === 'number' ? 'number' : 'text'"
            placeholder="请输入默认值（可选）"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave">
            {{ editingIndex >= 0 ? '保存' : '添加' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

interface Variable {
  key: string
  label: string
  type: string
  required: boolean
  defaultValue?: any
}

interface Props {
  variables: Variable[]
}

interface Emits {
  (e: 'update:variables', value: Variable[]): void
  (e: 'change'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 变量类型选项
const variableTypes = [
  { value: 'string', label: '文本' },
  { value: 'number', label: '数字' },
  { value: 'select', label: '选择' },
  { value: 'textarea', label: '长文本' }
]

// 对话框相关
const dialogVisible = ref(false)
const editingIndex = ref(-1)
const formRef = ref<FormInstance>()

const formData = reactive({
  key: '',
  label: '',
  type: 'string',
  required: true,
  defaultValue: ''
})

const formRules: FormRules = {
  key: [
    { required: true, message: '请输入变量名', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '变量名只能以字母开头，包含字母、数字和下划线', trigger: 'blur' }
  ],
  label: [
    { required: true, message: '请输入显示名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择变量类型', trigger: 'change' }
  ]
}

// 获取类型标签
const getTypeLabel = (type: string) => {
  const typeInfo = variableTypes.find(t => t.value === type)
  return typeInfo?.label || type
}

// 显示添加对话框
const showAddDialog = () => {
  editingIndex.value = -1
  Object.assign(formData, {
    key: '',
    label: '',
    type: 'string',
    required: true,
    defaultValue: ''
  })
  dialogVisible.value = true
}

// 编辑变量
const editVariable = (index: number) => {
  editingIndex.value = index
  const variable = props.variables[index]
  Object.assign(formData, {
    key: variable.key,
    label: variable.label,
    type: variable.type,
    required: variable.required,
    defaultValue: variable.defaultValue || ''
  })
  dialogVisible.value = true
}

// 删除变量
const removeVariable = async (index: number) => {
  const variable = props.variables[index]
  
  try {
    await ElMessageBox.confirm(
      `确认删除变量"${variable.key}"吗？`,
      '删除变量',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const newVariables = [...props.variables]
    newVariables.splice(index, 1)
    emit('update:variables', newVariables)
    emit('change')
    
    ElMessage.success('变量删除成功')
  } catch (error) {
    // 用户取消删除
  }
}

// 保存变量
const handleSave = async () => {
  if (!formRef.value) return

  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    // 检查变量名是否重复
    const existingIndex = props.variables.findIndex(v => v.key === formData.key)
    if (existingIndex >= 0 && existingIndex !== editingIndex.value) {
      ElMessage.error('变量名已存在')
      return
    }

    const newVariable: Variable = {
      key: formData.key,
      label: formData.label,
      type: formData.type,
      required: formData.required,
      defaultValue: formData.defaultValue || undefined
    }

    const newVariables = [...props.variables]
    
    if (editingIndex.value >= 0) {
      // 编辑模式
      newVariables[editingIndex.value] = newVariable
    } else {
      // 添加模式
      newVariables.push(newVariable)
    }

    emit('update:variables', newVariables)
    emit('change')
    
    dialogVisible.value = false
    ElMessage.success(editingIndex.value >= 0 ? '变量更新成功' : '变量添加成功')
  } catch (error) {
    console.error('保存变量失败:', error)
  }
}

// 响应式计算属性
const variables = computed({
  get: () => props.variables,
  set: (value) => emit('update:variables', value)
})
</script>

<style scoped lang="scss">
.variable-manager {
  .section-header {
    margin-bottom: 16px;

    .title-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .title {
        font-weight: 500;
        color: #374151;
        font-size: 14px;
      }
    }

    .section-description {
      font-size: 12px;
      color: #6b7280;
      line-height: 1.4;
    }
  }

  .variables-list {
    .variable-item {
      border: 1px solid #e5e7eb;
      border-radius: 8px;
      padding: 16px;
      margin-bottom: 12px;
      transition: all 0.2s;

      &:hover {
        border-color: #d1d5db;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
      }

      .variable-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .variable-info {
          display: flex;
          align-items: center;
          gap: 8px;

          .variable-key {
            font-family: 'SF Mono', Monaco, 'Cascadia Code', 'Roboto Mono', Consolas, 'Courier New', monospace;
            font-weight: 600;
            color: #1f2937;
            font-size: 14px;
          }

          .required-tag {
            height: 20px;
            font-size: 10px;
            font-weight: 600;
          }

          .type-tag {
            height: 20px;
            font-size: 10px;
            background-color: #f3f4f6;
            color: #6b7280;
            border: none;
          }
        }

        .variable-actions {
          display: flex;
          gap: 4px;
          opacity: 0;
          transition: opacity 0.2s;
        }
      }

      &:hover .variable-actions {
        opacity: 1;
      }

      .variable-details {
        font-size: 12px;
        color: #6b7280;
        
        .variable-label,
        .variable-default {
          margin-bottom: 4px;

          .label {
            font-weight: 500;
            margin-right: 8px;
          }

          .value {
            color: #374151;
          }
        }
      }
    }

    .empty-state {
      text-align: center;
      padding: 40px 20px;

      .empty-icon {
        font-size: 48px;
        margin-bottom: 16px;
      }

      .empty-text {
        font-size: 16px;
        font-weight: 500;
        color: #374151;
        margin-bottom: 8px;
      }

      .empty-description {
        font-size: 14px;
        color: #6b7280;
        line-height: 1.4;
      }
    }
  }

  .form-tip {
    font-size: 12px;
    color: #6b7280;
    margin-top: 4px;
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}
</style>