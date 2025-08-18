<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="isEdit ? '编辑分类' : '新增分类'"
    width="600px"
    :close-on-click-modal="false"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      class="category-form"
    >
      <el-form-item label="父分类" prop="parentId" v-if="!isEdit || formData.parentId">
        <el-select
          v-model="formData.parentId"
          placeholder="请选择父分类（留空为一级分类）"
          clearable
          style="width: 100%"
          :disabled="isEdit && formData.parentId"
        >
          <el-option
            v-for="parent in parentCategories"
            :key="parent.id"
            :label="parent.name"
            :value="parent.id"
          />
        </el-select>
        <div class="form-hint" v-if="!formData.parentId">
          未选择父分类，将创建为一级分类
        </div>
      </el-form-item>

      <el-form-item label="分类名称" prop="name" required>
        <el-input
          v-model="formData.name"
          placeholder="请输入分类名称"
          maxlength="64"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="分类编码" prop="code" required>
        <el-input
          v-model="formData.code"
          placeholder="请输入分类编码（用于程序识别）"
          maxlength="32"
          show-word-limit
          :disabled="isEdit"
        >
          <template #suffix>
            <el-tooltip content="分类编码创建后不可修改" v-if="isEdit">
              <el-icon><InfoFilled /></el-icon>
            </el-tooltip>
          </template>
        </el-input>
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
          <el-radio-button label="WARN">
            <el-tag type="info" size="small">警告</el-tag>
          </el-radio-button>
          <el-radio-button label="REVIEW">
            <el-tag type="warning" size="small">审核</el-tag>
          </el-radio-button>
          <el-radio-button label="BLOCK">
            <el-tag type="danger" size="small">阻断</el-tag>
          </el-radio-button>
          <el-radio-button label="ESCALATE">
            <el-tag type="danger" size="small">升级</el-tag>
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
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import moderationApi, { type ModerationCategory } from '@/api/smartcs/moderation'

// Props 和 Emits
interface Props {
  modelValue: boolean
  category?: ModerationCategory | null
  parentCategories: ModerationCategory[]
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

// 响应式数据
const formRef = ref<FormInstance>()
const submitting = ref(false)

const formData = reactive<Partial<ModerationCategory>>({
  parentId: undefined,
  name: '',
  code: '',
  description: '',
  severityLevel: 'MEDIUM',
  actionType: 'BLOCK',
  isActive: true,
  sortOrder: 0
})

// 计算属性
const isEdit = computed(() => !!props.category?.id)

// 表单验证规则
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

// 监听器
watch(
  () => props.category,
  (newCategory) => {
    if (newCategory && props.modelValue) {
      // 编辑模式，填充表单数据
      Object.assign(formData, {
        ...newCategory,
        parentId: newCategory.parentId || undefined
      })
    } else if (!newCategory && props.modelValue) {
      // 新增模式，重置表单
      resetForm()
    }
  },
  { immediate: true, deep: true }
)

watch(
  () => props.modelValue,
  (show) => {
    if (show) {
      nextTick(() => {
        formRef.value?.clearValidate()
      })
    } else {
      // 对话框关闭时清理表单状态
      resetForm()
    }
  }
)

// 方法定义
const resetForm = () => {
  Object.assign(formData, {
    parentId: undefined,
    name: '',
    code: '',
    description: '',
    severityLevel: 'MEDIUM',
    actionType: 'BLOCK',
    isActive: true,
    sortOrder: 0
  })
  formRef.value?.clearValidate()
}

const handleCancel = () => {
  emit('update:modelValue', false)
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
    const submitData = {
      ...formData,
      parentId: formData.parentId || null
    }

    if (isEdit.value) {
      await moderationApi.updateCategory(props.category!.id!, submitData)
      ElMessage.success('更新成功')
    } else {
      await moderationApi.createCategory(submitData)
      ElMessage.success('创建成功')
    }

    emit('success')
  } catch (error: any) {
    console.error('Submit failed:', error)
    const message = error.response?.data?.message || '操作失败'
    ElMessage.error(message)
  } finally {
    submitting.value = false
  }
}

// 生成建议的分类编码
const generateSuggestedCode = () => {
  if (!formData.name) return
  
  const pinyin = formData.name
    .replace(/[^\u4e00-\u9fa5a-zA-Z0-9]/g, '_')
    .toUpperCase()
  
  // 这里可以集成拼音库，暂时使用简单转换
  const suggested = pinyin.replace(/[\u4e00-\u9fa5]/g, 'X')
  formData.code = suggested
}

// 暴露方法（如果需要）
defineExpose({
  generateSuggestedCode
})
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

  :deep(.el-input-number) {
    .el-input__inner {
      text-align: left;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

// 响应式设计
@media (max-width: 768px) {
  .severity-radio-group,
  .action-radio-group {
    :deep(.el-radio-button) {
      display: block;
      margin-bottom: 8px;
      margin-right: 0;
    }
  }
}
</style>