<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="formRules"
    label-width="120px"
    class="category-form"
  >
    <el-form-item label="分类名称" prop="name" required>
      <el-input
        v-model="formData.name"
        placeholder="请输入分类名称"
        maxlength="64"
        show-word-limit
      />
    </el-form-item>

    <el-form-item label="分类编码" prop="code" required v-if="!isEditing">
      <el-input
        v-model="formData.code"
        placeholder="请输入分类编码（用于程序识别）"
        maxlength="32"
        show-word-limit
      />
      <div class="form-hint">
        建议使用大写字母和下划线，如：HATE_SPEECH
      </div>
    </el-form-item>

    <el-form-item label="严重程度" prop="severityLevel" required>
      <el-radio-group v-model="formData.severityLevel" class="severity-radio-group">
        <el-radio-button label="LOW">
          <el-tag type="" size="small">低风险</el-tag>
        </el-radio-button>
        <el-radio-button label="MEDIUM">
          <el-tag type="warning" size="small">中风险</el-tag>
        </el-radio-button>
        <el-radio-button label="HIGH">
          <el-tag type="danger" size="small">高风险</el-tag>
        </el-radio-button>
        <el-radio-button label="CRITICAL">
          <el-tag type="danger" size="small" class="critical-tag">极高风险</el-tag>
        </el-radio-button>
      </el-radio-group>
    </el-form-item>

    <el-form-item label="默认动作" prop="actionType" required>
      <el-radio-group v-model="formData.actionType" class="action-radio-group">
        <el-radio-button label="APPROVE">
          <el-tag type="success" size="small">通过</el-tag>
        </el-radio-button>
        <el-radio-button label="REJECT">
          <el-tag type="danger" size="small">拒绝</el-tag>
        </el-radio-button>
        <el-radio-button label="MANUAL_REVIEW">
          <el-tag type="warning" size="small">人工审核</el-tag>
        </el-radio-button>
        <el-radio-button label="AUTO_FIX">
          <el-tag type="primary" size="small">自动修复</el-tag>
        </el-radio-button>
      </el-radio-group>
    </el-form-item>

    <el-form-item label="排序权重" prop="sortOrder">
      <el-input-number
        v-model="formData.sortOrder"
        :min="0"
        :max="9999"
        placeholder="数字越小优先级越高"
        style="width: 200px"
      />
      <span class="form-hint">数字越小，显示顺序越靠前</span>
    </el-form-item>

    <el-form-item label="描述信息" prop="description">
      <el-input
        v-model="formData.description"
        type="textarea"
        :rows="3"
        placeholder="请输入分类描述（可选）"
        maxlength="512"
        show-word-limit
      />
    </el-form-item>

    <el-form-item label="启用状态" prop="isActive">
      <el-switch
        v-model="formData.isActive"
        active-text="启用"
        inactive-text="禁用"
      />
    </el-form-item>

    <div class="form-footer">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        {{ isEditing ? '更新' : '创建' }}
      </el-button>
    </div>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive, watch, nextTick } from 'vue'
import { type FormInstance, type FormRules } from 'element-plus'
import type { ModerationCategory } from '@/api/smartcs/moderation'

interface Props {
  category?: ModerationCategory | null
  isEditing: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  submit: [formData: any]
  cancel: []
}>()

const formRef = ref<FormInstance>()
const submitting = ref(false)

const formData = reactive({
  name: '',
  code: '',
  description: '',
  severityLevel: 'MEDIUM',
  actionType: 'MANUAL_REVIEW',
  isActive: true,
  sortOrder: 0
})

const formRules: FormRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 64, message: '分类名称长度在 2 到 64 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入分类编码', trigger: 'blur' },
    { min: 2, max: 32, message: '分类编码长度在 2 到 32 个字符', trigger: 'blur' },
    { 
      pattern: /^[A-Z][A-Z0-9_]*$/, 
      message: '分类编码必须以大写字母开头，只能包含大写字母、数字和下划线', 
      trigger: 'blur' 
    }
  ],
  severityLevel: [
    { required: true, message: '请选择严重程度', trigger: 'change' }
  ],
  actionType: [
    { required: true, message: '请选择默认动作', trigger: 'change' }
  ],
  sortOrder: [
    { type: 'number', min: 0, max: 9999, message: '排序权重必须在 0 到 9999 之间', trigger: 'blur' }
  ]
}

const resetForm = () => {
  Object.assign(formData, {
    name: '',
    code: '',
    description: '',
    severityLevel: 'MEDIUM',
    actionType: 'MANUAL_REVIEW',
    isActive: true,
    sortOrder: 0
  })
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

watch(
  () => props.category,
  (newCategory) => {
    if (newCategory) {
      Object.assign(formData, newCategory)
    } else {
      resetForm()
    }
  },
  { immediate: true, deep: true }
)

const handleCancel = () => {
  emit('cancel')
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    emit('submit', { ...formData })
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.category-form {
  .form-hint {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
    line-height: 1.4;
  }

  .severity-radio-group,
  .action-radio-group {
    :deep(.el-radio-button) {
      margin-right: 12px;
      margin-bottom: 8px;
    }

    .critical-tag {
      background: linear-gradient(45deg, #f56565, #e53e3e);
      color: white;
      font-weight: bold;
    }
  }

  :deep(.el-form-item__label) {
    font-weight: 500;
  }

  .form-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 24px;
    padding-top: 16px;
    border-top: 1px solid #e4e7ed;
  }
}
</style>