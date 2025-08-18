<template>
  <el-dialog
    v-model="dialogVisible"
    title="编辑应用"
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
      <!-- 应用类型展示（不可编辑） -->
      <el-form-item label="应用类型">
        <div class="type-display">
          <div class="type-info">
            <span class="type-icon">{{ getTypeIcon() }}</span>
            <div class="type-text">
              <span class="type-name">{{ getTypeName() }}</span>
              <span class="type-description">{{ getTypeDescription() }}</span>
            </div>
          </div>
          <el-tag type="info" size="small">{{ formData.type }}</el-tag>
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
          <el-form-item label="应用编码">
            <el-input
              v-model="formData.code"
              disabled
              placeholder="应用编码"
            />
            <div class="form-tip">应用编码创建后不可修改</div>
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

      <!-- 应用状态信息 -->
      <el-form-item label="应用状态">
        <div class="status-info">
          <el-tag :type="getStatusTagType()" size="large">
            {{ getStatusName() }}
          </el-tag>
          <span class="status-tip">
            {{ getStatusTip() }}
          </span>
        </div>
      </el-form-item>

      <!-- 高级配置 -->
      <el-form-item label="应用配置" v-if="showAdvancedConfig">
        <el-input
          v-model="configJson"
          type="textarea"
          :rows="6"
          placeholder="JSON格式的应用配置"
          @blur="validateConfig"
        />
        <div class="form-tip">
          高级用户可以直接编辑JSON配置，请确保格式正确
        </div>
      </el-form-item>

      <el-form-item>
        <el-checkbox v-model="showAdvancedConfig">
          显示高级配置选项
        </el-checkbox>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          保存修改
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, nextTick, watch } from 'vue'
import { ElMessage, ElInput } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { updateApp, getAppTypeInfo, getAppStatusInfo, type AiAppDTO, type AiAppUpdateRequest } from '@/api/smartcs/app'

interface Props {
  visible: boolean
  app: AiAppDTO | null
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
const formData = reactive<AiAppUpdateRequest>({
  id: 0,
  name: '',
  description: '',
  icon: '',
  tags: [],
  config: {}
})

const submitting = ref(false)
const showAdvancedConfig = ref(false)
const configJson = ref('')

// 标签输入相关
const tagInputVisible = ref(false)
const tagInputValue = ref('')
const tagInputRef = ref<InstanceType<typeof ElInput>>()

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
  description: [
    { max: 500, message: '描述长度不能超过500个字符', trigger: 'blur' }
  ]
}

// 监听 app 属性变化，更新表单数据
watch(() => props.app, (newApp) => {
  if (newApp) {
    Object.assign(formData, {
      id: newApp.id,
      name: newApp.name,
      description: newApp.description || '',
      icon: newApp.icon || '',
      tags: newApp.tags ? [...newApp.tags] : [],
      config: newApp.config ? { ...newApp.config } : {}
    })
    configJson.value = JSON.stringify(newApp.config, null, 2)
  }
}, { immediate: true })

// 获取应用类型信息
const getTypeIcon = () => {
  const typeInfo = getAppTypeInfo(props.app?.type || '')
  return typeInfo?.icon || '📱'
}

const getTypeName = () => {
  const typeInfo = getAppTypeInfo(props.app?.type || '')
  return typeInfo?.label || props.app?.typeName || ''
}

const getTypeDescription = () => {
  const typeInfo = getAppTypeInfo(props.app?.type || '')
  return typeInfo?.description || props.app?.typeDescription || ''
}

// 获取应用状态信息
const getStatusTagType = () => {
  switch (props.app?.status) {
    case 'PUBLISHED':
      return 'success'
    case 'DISABLED':
      return 'danger'
    case 'DRAFT':
    default:
      return 'info'
  }
}

const getStatusName = () => {
  const statusInfo = getAppStatusInfo(props.app?.status || '')
  return statusInfo?.label || props.app?.statusName || ''
}

const getStatusTip = () => {
  switch (props.app?.status) {
    case 'PUBLISHED':
      return '应用已发布，正在运行中'
    case 'DISABLED':
      return '应用已停用，用户无法访问'
    case 'DRAFT':
    default:
      return '应用处于草稿状态，仅创建者可见'
  }
}

// 验证配置JSON格式
const validateConfig = () => {
  if (!configJson.value.trim()) {
    formData.config = {}
    return
  }

  try {
    formData.config = JSON.parse(configJson.value)
  } catch (error) {
    ElMessage.warning('配置格式不正确，已还原为原始配置')
    configJson.value = JSON.stringify(formData.config, null, 2)
  }
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

    // 验证配置格式
    if (showAdvancedConfig.value) {
      validateConfig()
    }

    submitting.value = true

    const response = await updateApp(formData)
    if (response.success) {
      ElMessage.success('应用更新成功')
      emit('success')
    } else {
      ElMessage.error(response.errMessage || '更新失败')
    }
  } catch (error) {
    console.error('更新应用失败:', error)
    ElMessage.error('更新失败')
  } finally {
    submitting.value = false
  }
}

// 取消
const handleCancel = () => {
  dialogVisible.value = false
}

// 对话框关闭时重置状态
const handleClose = () => {
  showAdvancedConfig.value = false
  tagInputVisible.value = false
  tagInputValue.value = ''
  configJson.value = ''
}
</script>

<style scoped lang="scss">
.type-display {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background-color: #f9fafb;
  border-radius: 8px;
  border: 1px solid #e5e7eb;

  .type-info {
    display: flex;
    align-items: center;
    gap: 12px;

    .type-icon {
      font-size: 24px;
    }

    .type-text {
      display: flex;
      flex-direction: column;
      gap: 2px;

      .type-name {
        font-weight: 600;
        color: #1f2937;
        font-size: 14px;
      }

      .type-description {
        font-size: 12px;
        color: #6b7280;
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

.status-info {
  display: flex;
  align-items: center;
  gap: 12px;

  .status-tip {
    font-size: 12px;
    color: #6b7280;
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