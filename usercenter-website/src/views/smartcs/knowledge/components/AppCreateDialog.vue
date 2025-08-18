<template>
  <el-dialog
    v-model="dialogVisible"
    title="创建应用"
    width="600px"
    :close-on-click-modal="false"
    @closed="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      label-position="top"
    >
      <!-- 应用类型选择 -->
      <el-form-item label="选择应用类型" prop="type" required>
        <div class="type-selector">
          <div
            v-for="type in appTypes"
            :key="type.value"
            class="type-card"
            :class="{ active: formData.type === type.value }"
            @click="selectType(type.value)"
          >
            <div class="type-icon">{{ type.icon }}</div>
            <div class="type-info">
              <h4>{{ type.label }}</h4>
              <p>{{ type.description }}</p>
            </div>
          </div>
        </div>
      </el-form-item>

      <!-- 基础信息 -->
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="应用名称" prop="name" required>
            <el-input
              v-model="formData.name"
              placeholder="请输入应用名称"
              maxlength="128"
              show-word-limit
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="应用编码" prop="code" required>
            <el-input
              v-model="formData.code"
              placeholder="请输入应用编码"
              maxlength="64"
              show-word-limit
            />
            <div class="form-tip">编码唯一，只能包含字母、数字和下划线</div>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="应用描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入应用描述"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <!-- 应用标签 -->
      <el-form-item label="应用标签">
        <div class="tags-input">
          <el-tag
            v-for="tag in formData.tags"
            :key="tag"
            closable
            @close="removeTag(tag)"
          >
            {{ tag }}
          </el-tag>
          <el-input
            v-if="tagInputVisible"
            ref="tagInputRef"
            v-model="tagInputValue"
            size="small"
            @keyup.enter="addTag"
            @blur="addTag"
            class="tag-input"
            placeholder="输入标签"
          />
          <el-button
            v-else
            size="small"
            @click="showTagInput"
            class="add-tag-btn"
          >
            + 添加标签
          </el-button>
        </div>
      </el-form-item>

      <!-- 应用图标 -->
      <el-form-item label="应用图标">
        <el-input
          v-model="formData.icon" 
          placeholder="输入图标URL或选择emoji"
          maxlength="255"
        >
          <template #append>
            <el-popover placement="bottom" width="300" trigger="click">
              <template #reference>
                <el-button>选择</el-button>
              </template>
              <div class="icon-picker">
                <div class="emoji-grid">
                  <span
                    v-for="emoji in commonEmojis"
                    :key="emoji"
                    class="emoji-item"
                    @click="selectIcon(emoji)"
                  >
                    {{ emoji }}
                  </span>
                </div>
              </div>
            </el-popover>
          </template>
        </el-input>
      </el-form-item>

      <!-- 应用配置预览 -->
      <el-form-item label="初始配置" v-if="formData.type">
        <div class="config-preview">
          <p class="config-tip">
            选择的应用类型: <strong>{{ getSelectedTypeInfo()?.label }}</strong>
          </p>
          <p class="config-description">
            {{ getSelectedTypeInfo()?.description }}
          </p>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          创建应用
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, nextTick, watch } from 'vue'
import { ElMessage, ElInput } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { createApp, getAppTypeInfo, type AiAppCreateRequest, APP_TYPES } from '@/api/smartcs/app'

interface Props {
  visible: boolean
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 对话框显示状态
const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// 表单相关
const formRef = ref<FormInstance>()
const formData = reactive<AiAppCreateRequest>({
  name: '',
  code: '',
  description: '',
  type: '',
  icon: '',
  tags: [],
  config: {}
})

const submitting = ref(false)

// 标签输入相关
const tagInputVisible = ref(false)
const tagInputValue = ref('')
const tagInputRef = ref<InstanceType<typeof ElInput>>()

// 应用类型
const appTypes = APP_TYPES

// 常用emoji图标
const commonEmojis = [
  '🤖', '💬', '🧠', '⚡', '🔄', '📱', '💡', '🎯', 
  '🚀', '⭐', '🔥', '💎', '🎨', '📊', '🔧', '🎵'
]

// 表单验证规则
const formRules: FormRules = {
  name: [
    { required: true, message: '请输入应用名称', trigger: 'blur' },
    { min: 1, max: 128, message: '应用名称长度在1-128个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入应用编码', trigger: 'blur' },
    { min: 1, max: 64, message: '应用编码长度在1-64个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '编码只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择应用类型', trigger: 'change' }
  ],
  description: [
    { max: 500, message: '描述长度不能超过500个字符', trigger: 'blur' }
  ]
}

// 监听名称变化自动生成编码
watch(() => formData.name, (newName) => {
  if (newName && !formData.code) {
    // 自动生成编码：转换为小写、替换空格为下划线、移除特殊字符
    const code = newName
      .toLowerCase()
      .replace(/\s+/g, '_')
      .replace(/[^\w]/g, '')
      .slice(0, 64)
    formData.code = code
  }
})

// 选择应用类型
const selectType = (type: string) => {
  formData.type = type
  // 设置默认配置
  switch (type) {
    case 'WORKFLOW':
      formData.config = { 
        nodes: [], 
        edges: [], 
        variables: {} 
      }
      break
    case 'CHATFLOW':
      formData.config = { 
        conversation_variables: [], 
        opening_statement: '', 
        suggested_questions: [] 
      }
      break
    case 'CHAT_ASSISTANT':
      formData.config = { 
        prompt_template: '', 
        model: '', 
        temperature: 0.7 
      }
      break
    case 'AGENT':
      formData.config = { 
        tools: [], 
        prompt_template: '', 
        model: '', 
        max_iterations: 10 
      }
      break
  }
}

// 获取选中的应用类型信息
const getSelectedTypeInfo = () => {
  return getAppTypeInfo(formData.type)
}

// 添加标签
const addTag = () => {
  const tag = tagInputValue.value.trim()
  if (tag && !formData.tags?.includes(tag)) {
    if (!formData.tags) formData.tags = []
    formData.tags.push(tag)
  }
  tagInputValue.value = ''
  tagInputVisible.value = false
}

// 移除标签
const removeTag = (tag: string) => {
  const index = formData.tags?.indexOf(tag)
  if (index !== undefined && index > -1) {
    formData.tags?.splice(index, 1)
  }
}

// 显示标签输入框
const showTagInput = () => {
  tagInputVisible.value = true
  nextTick(() => {
    tagInputRef.value?.focus()
  })
}

// 选择图标
const selectIcon = (icon: string) => {
  formData.icon = icon
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    submitting.value = true

    const response = await createApp(formData)
    if (response.success) {
      ElMessage.success('应用创建成功')
      emit('success')
    } else {
      ElMessage.error(response.errMessage || '创建失败')
    }
  } catch (error) {
    console.error('创建应用失败:', error)
    ElMessage.error('创建失败')
  } finally {
    submitting.value = false
  }
}

// 取消
const handleCancel = () => {
  dialogVisible.value = false
}

// 对话框关闭时重置表单
const handleClose = () => {
  formRef.value?.resetFields()
  Object.assign(formData, {
    name: '',
    code: '',
    description: '',
    type: '',
    icon: '',
    tags: [],
    config: {}
  })
  tagInputVisible.value = false
  tagInputValue.value = ''
}
</script>

<style scoped lang="scss">
.type-selector {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;

  .type-card {
    padding: 16px;
    border: 2px solid #e5e7eb;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    display: flex;
    align-items: center;
    gap: 12px;

    &:hover {
      border-color: #3b82f6;
    }

    &.active {
      border-color: #3b82f6;
      background-color: #eff6ff;
    }

    .type-icon {
      font-size: 24px;
      min-width: 32px;
    }

    .type-info {
      h4 {
        margin: 0 0 4px 0;
        font-size: 14px;
        font-weight: 600;
        color: #1f2937;
      }

      p {
        margin: 0;
        font-size: 12px;
        color: #6b7280;
        line-height: 1.4;
      }
    }
  }
}

.form-tip {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
}

.tags-input {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;

  .tag-input {
    width: 100px;
  }

  .add-tag-btn {
    border-style: dashed;
  }
}

.icon-picker {
  .emoji-grid {
    display: grid;
    grid-template-columns: repeat(8, 1fr);
    gap: 8px;

    .emoji-item {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      border-radius: 4px;
      font-size: 16px;

      &:hover {
        background-color: #f3f4f6;
      }
    }
  }
}

.config-preview {
  padding: 16px;
  background-color: #f9fafb;
  border-radius: 6px;
  border-left: 4px solid #3b82f6;

  .config-tip {
    margin: 0 0 8px 0;
    font-size: 14px;
    color: #374151;

    strong {
      color: #3b82f6;
    }
  }

  .config-description {
    margin: 0;
    font-size: 12px;
    color: #6b7280;
    line-height: 1.4;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #374151;
}
</style>