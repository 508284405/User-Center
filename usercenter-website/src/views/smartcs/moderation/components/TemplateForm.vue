<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="formRules"
    label-width="120px"
    class="template-form"
  >
    <el-form-item label="模板名称" prop="name" required>
      <el-input
        v-model="formData.name"
        placeholder="请输入模板名称"
        maxlength="64"
        show-word-limit
      />
    </el-form-item>

    <el-form-item label="模板编码" prop="code" required v-if="!isEditing">
      <el-input
        v-model="formData.code"
        placeholder="请输入模板编码（用于程序识别）"
        maxlength="32"
        show-word-limit
      />
      <div class="form-hint">
        建议使用大写字母和下划线，如：DETAILED_REVIEW_TEMPLATE
      </div>
    </el-form-item>

    <el-form-item label="模板类型" prop="templateType" required>
      <el-radio-group v-model="formData.templateType" class="template-type-group">
        <el-radio-button label="DETAILED">
          <el-tag type="primary" size="small">详细审核</el-tag>
        </el-radio-button>
        <el-radio-button label="QUICK">
          <el-tag type="success" size="small">快速审核</el-tag>
        </el-radio-button>
        <el-radio-button label="STRUCTURED">
          <el-tag type="warning" size="small">结构化审核</el-tag>
        </el-radio-button>
      </el-radio-group>
    </el-form-item>

    <el-form-item label="语言" prop="language">
      <el-select v-model="formData.language" placeholder="请选择语言" style="width: 200px">
        <el-option label="中文" value="zh-CN" />
        <el-option label="英文" value="en-US" />
        <el-option label="日文" value="ja-JP" />
        <el-option label="韩文" value="ko-KR" />
      </el-select>
    </el-form-item>

    <el-form-item label="版本号" prop="version">
      <el-input
        v-model="formData.version"
        placeholder="如: 1.0.0"
        style="width: 200px"
        :disabled="isEditing"
      />
    </el-form-item>

    <el-form-item label="描述信息" prop="description">
      <el-input
        v-model="formData.description"
        type="textarea"
        :rows="2"
        placeholder="请输入模板描述（可选）"
        maxlength="512"
        show-word-limit
      />
    </el-form-item>

    <el-form-item label="Prompt模板" prop="promptTemplate" required>
      <el-input
        v-model="formData.promptTemplate"
        type="textarea"
        :rows="8"
        placeholder="请输入prompt模板内容，支持变量如：{{content}}、{{categories}}等"
        show-word-limit
      />
      <div class="form-hint">
        支持变量：{{content}} - 待审核内容，{{categories}} - 违规分类，{{language}} - 内容语言
      </div>
    </el-form-item>

    <el-form-item label="维度模板" prop="dimensionTemplate">
      <el-input
        v-model="formData.dimensionTemplate"
        type="textarea"
        :rows="4"
        placeholder="请输入维度检查模板（可选）"
        show-word-limit
      />
    </el-form-item>

    <el-form-item label="响应模板" prop="responseTemplate">
      <el-input
        v-model="formData.responseTemplate"
        type="textarea"
        :rows="4"
        placeholder="请输入响应格式模板（可选）"
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
import type { ModerationPolicyTemplate } from '@/api/smartcs/moderation'

interface Props {
  template?: ModerationPolicyTemplate | null
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
  templateType: 'DETAILED',
  promptTemplate: '',
  dimensionTemplate: '',
  responseTemplate: '',
  language: 'zh-CN',
  version: '1.0.0',
  isActive: true
})

const resetForm = () => {
  Object.assign(formData, {
    name: '',
    code: '',
    description: '',
    templateType: 'DETAILED',
    promptTemplate: '',
    dimensionTemplate: '',
    responseTemplate: '',
    language: 'zh-CN',
    version: '1.0.0',
    isActive: true
  })
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

const formRules: FormRules = {
  name: [
    { required: true, message: '请输入模板名称', trigger: 'blur' },
    { min: 2, max: 64, message: '模板名称长度在 2 到 64 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入模板编码', trigger: 'blur' },
    { min: 2, max: 32, message: '模板编码长度在 2 到 32 个字符', trigger: 'blur' },
    { 
      pattern: /^[A-Z][A-Z0-9_]*$/, 
      message: '模板编码必须以大写字母开头，只能包含大写字母、数字和下划线', 
      trigger: 'blur' 
    }
  ],
  templateType: [
    { required: true, message: '请选择模板类型', trigger: 'change' }
  ],
  promptTemplate: [
    { required: true, message: '请输入Prompt模板内容', trigger: 'blur' },
    { min: 10, message: 'Prompt模板内容至少需要10个字符', trigger: 'blur' }
  ]
}

watch(
  () => props.template,
  (newTemplate) => {
    if (newTemplate) {
      Object.assign(formData, newTemplate)
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
.template-form {
  .form-hint {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
    line-height: 1.4;
  }

  .template-type-group {
    :deep(.el-radio-button) {
      margin-right: 12px;
      margin-bottom: 8px;
    }
  }

  :deep(.el-form-item__label) {
    font-weight: 500;
  }

  :deep(.el-textarea .el-textarea__inner) {
    font-family: 'Courier New', monospace;
    font-size: 13px;
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