<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="isEdit ? '编辑规则' : '新增规则'"
    width="700px"
    :close-on-click-modal="false"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      class="rule-form"
    >
      <el-form-item label="规则名称" prop="ruleName" required>
        <el-input
          v-model="formData.ruleName"
          placeholder="请输入规则名称"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="关键词" prop="keyword" required>
        <el-input
          v-model="formData.keyword"
          placeholder="请输入关键词"
          maxlength="200"
          show-word-limit
        >
          <template #suffix>
            <el-tooltip content="支持逗号分隔的多个关键词" v-if="formData.ruleType === 'EXACT'">
              <el-icon><InfoFilled /></el-icon>
            </el-tooltip>
          </template>
        </el-input>
        <div class="form-hint">
          <span v-if="formData.ruleType === 'REGEX'">
            支持正则表达式，如：(敏感词1|敏感词2)
          </span>
          <span v-else-if="formData.ruleType === 'FUZZY'">
            支持模糊匹配，如：敏感* 或 *敏感*
          </span>
          <span v-else>
            支持逗号分隔的多个关键词
          </span>
        </div>
      </el-form-item>

      <el-form-item label="违规分类" prop="categoryId" required>
        <el-select v-model="formData.categoryId" placeholder="请选择违规分类" style="width: 100%">
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

      <el-form-item label="规则类型" prop="ruleType" required>
        <el-radio-group v-model="formData.ruleType" class="rule-type-group">
          <el-radio-button label="EXACT">
            精确匹配
          </el-radio-button>
          <el-radio-button label="FUZZY">
            模糊匹配
          </el-radio-button>
          <el-radio-button label="SUBSTRING">
            子字符串
          </el-radio-button>
          <el-radio-button label="REGEX">
            正则表达式
          </el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="匹配模式" prop="matchMode" required>
        <el-radio-group v-model="formData.matchMode">
          <el-radio label="FULL">完整匹配</el-radio>
          <el-radio label="PARTIAL">部分匹配</el-radio>
          <el-radio label="WORD_BOUNDARY">词边界匹配</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="优先级" prop="priority" required>
            <el-input-number
              v-model="formData.priority"
              :min="1"
              :max="9999"
              placeholder="数字越小优先级越高"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="严重度权重" prop="severityWeight" required>
            <el-input-number
              v-model="formData.severityWeight"
              :min="0.1"
              :max="10"
              :step="0.1"
              placeholder="权重值"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="语言" prop="language" required>
            <el-select v-model="formData.language" placeholder="选择语言" style="width: 100%">
              <el-option label="中文" value="zh-CN" />
              <el-option label="英文" value="en-US" />
              <el-option label="其他" value="other" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="大小写敏感" prop="caseSensitive">
            <el-switch
              v-model="formData.caseSensitive"
              active-text="敏感"
              inactive-text="不敏感"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 模糊匹配特有配置 -->
      <template v-if="formData.ruleType === 'FUZZY'">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="相似度阈值" prop="similarityThreshold">
              <el-input-number
                v-model="formData.similarityThreshold"
                :min="0.1"
                :max="1"
                :step="0.05"
                placeholder="0.1-1.0"
                style="width: 100%"
              />
              <div class="form-hint">值越高匹配越严格</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上下文窗口" prop="contextWindow">
              <el-input-number
                v-model="formData.contextWindow"
                :min="0"
                :max="100"
                placeholder="字符数"
                style="width: 100%"
              />
              <div class="form-hint">0表示不限制</div>
            </el-form-item>
          </el-col>
        </el-row>
      </template>

      <el-form-item label="动作覆盖" prop="actionOverride">
        <el-select v-model="formData.actionOverride" placeholder="默认使用分类配置" clearable style="width: 100%">
          <el-option label="警告" value="WARN" />
          <el-option label="审核" value="REVIEW" />
          <el-option label="阻断" value="BLOCK" />
          <el-option label="升级" value="ESCALATE" />
        </el-select>
        <div class="form-hint">如不设置，将使用违规分类的默认动作</div>
      </el-form-item>

      <el-form-item label="有效期" prop="effectiveTime">
        <el-date-picker
          v-model="effectiveRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="生效时间"
          end-placeholder="失效时间"
          format="YYYY-MM-DD HH:mm:ss"
          value-format="x"
          style="width: 100%"
        />
        <div class="form-hint">留空表示永久有效</div>
      </el-form-item>

      <el-form-item label="标签" prop="tags">
        <el-input
          v-model="formData.tags"
          placeholder="用逗号分隔的标签，如：色情,暴力,政治"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="规则描述（可选）"
          maxlength="500"
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

    <!-- 预览和测试 -->
    <el-card class="preview-card" v-if="formData.keyword">
      <template #header>
        <span>规则预览</span>
        <el-button type="text" size="small" @click="testCurrentRule">
          <el-icon><Tools /></el-icon>
          测试规则
        </el-button>
      </template>
      
      <div class="rule-preview">
        <div class="preview-item">
          <span class="label">关键词:</span>
          <el-tag :type="getRuleTypeTag(formData.ruleType)" size="small">
            {{ getRuleTypeLabel(formData.ruleType) }}
          </el-tag>
          <code class="keyword-code">{{ formData.keyword }}</code>
        </div>
        <div class="preview-item">
          <span class="label">匹配模式:</span>
          <span>{{ getMatchModeLabel(formData.matchMode) }}</span>
        </div>
        <div class="preview-item" v-if="formData.similarityThreshold && formData.ruleType === 'FUZZY'">
          <span class="label">相似度阈值:</span>
          <span>{{ formData.similarityThreshold }}</span>
        </div>
      </div>
    </el-card>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </div>
    </template>

    <!-- 测试对话框 -->
    <RuleTestDialog
      v-model="showTestDialog"
      :rule="testRule"
    />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { InfoFilled, Tools } from '@element-plus/icons-vue'
import moderationApi, { type KeywordRule, type ModerationCategory } from '@/api/smartcs/moderation'
import RuleTestDialog from './RuleTestDialog.vue'

// Props 和 Emits
interface Props {
  modelValue: boolean
  rule?: KeywordRule | null
  categories: ModerationCategory[]
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

// 响应式数据
const formRef = ref<FormInstance>()
const submitting = ref(false)
const showTestDialog = ref(false)
const effectiveRange = ref<[string, string] | null>(null)
const testRule = ref<KeywordRule | null>(null)

const formData = reactive<Partial<KeywordRule>>({
  ruleName: '',
  keyword: '',
  categoryId: undefined,
  ruleType: 'EXACT',
  matchMode: 'FULL',
  caseSensitive: false,
  severityWeight: 1.0,
  priority: 100,
  actionOverride: undefined,
  isActive: true,
  similarityThreshold: 0.8,
  contextWindow: 0,
  description: '',
  language: 'zh-CN',
  tags: '',
  effectiveFrom: undefined,
  effectiveUntil: undefined,
  source: 'MANUAL'
})

// 计算属性
const isEdit = computed(() => !!props.rule?.id)

// 表单验证规则
const formRules: FormRules = {
  ruleName: [
    { required: true, message: '请输入规则名称', trigger: 'blur' },
    { min: 2, max: 100, message: '规则名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  keyword: [
    { required: true, message: '请输入关键词', trigger: 'blur' },
    { min: 1, max: 200, message: '关键词长度在 1 到 200 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择违规分类', trigger: 'change' }
  ],
  ruleType: [
    { required: true, message: '请选择规则类型', trigger: 'change' }
  ],
  matchMode: [
    { required: true, message: '请选择匹配模式', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请设置优先级', trigger: 'blur' },
    { type: 'number', min: 1, max: 9999, message: '优先级必须在 1 到 9999 之间', trigger: 'blur' }
  ],
  severityWeight: [
    { required: true, message: '请设置严重度权重', trigger: 'blur' },
    { type: 'number', min: 0.1, max: 10, message: '严重度权重必须在 0.1 到 10 之间', trigger: 'blur' }
  ],
  language: [
    { required: true, message: '请选择语言', trigger: 'change' }
  ]
}

// 监听器
watch(
  () => props.rule,
  (newRule) => {
    if (newRule && props.modelValue) {
      // 编辑模式，填充表单数据
      Object.assign(formData, {
        ...newRule,
        categoryId: newRule.categoryId || undefined
      })
      
      if (newRule.effectiveFrom && newRule.effectiveUntil) {
        effectiveRange.value = [
          newRule.effectiveFrom.toString(),
          newRule.effectiveUntil.toString()
        ]
      }
    } else if (!newRule && props.modelValue) {
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
      resetForm()
    }
  }
)

watch(effectiveRange, (newRange) => {
  if (newRange && newRange.length === 2) {
    formData.effectiveFrom = Number(newRange[0])
    formData.effectiveUntil = Number(newRange[1])
  } else {
    formData.effectiveFrom = undefined
    formData.effectiveUntil = undefined
  }
})

// 方法定义
const resetForm = () => {
  Object.assign(formData, {
    ruleName: '',
    keyword: '',
    categoryId: undefined,
    ruleType: 'EXACT',
    matchMode: 'FULL',
    caseSensitive: false,
    severityWeight: 1.0,
    priority: 100,
    actionOverride: undefined,
    isActive: true,
    similarityThreshold: 0.8,
    contextWindow: 0,
    description: '',
    language: 'zh-CN',
    tags: '',
    effectiveFrom: undefined,
    effectiveUntil: undefined,
    source: 'MANUAL'
  })
  effectiveRange.value = null
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
      categoryId: formData.categoryId || undefined,
      hitCount: formData.hitCount || 0
    }

    if (isEdit.value) {
      await moderationApi.updateRule(props.rule!.id!, submitData)
      ElMessage.success('更新成功')
    } else {
      await moderationApi.createRule(submitData)
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

const testCurrentRule = () => {
  if (!formData.keyword) {
    ElMessage.warning('请先输入关键词')
    return
  }
  
  testRule.value = { ...formData } as KeywordRule
  showTestDialog.value = true
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

const getRuleTypeTag = (type: string) => {
  const tags: Record<string, string> = {
    EXACT: 'success',
    FUZZY: 'warning',
    REGEX: 'danger',
    SUBSTRING: 'info'
  }
  return tags[type] || ''
}

const getRuleTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    EXACT: '精确',
    FUZZY: '模糊',
    REGEX: '正则',
    SUBSTRING: '子串'
  }
  return labels[type] || type
}

const getMatchModeLabel = (mode: string) => {
  const labels: Record<string, string> = {
    FULL: '完整匹配',
    PARTIAL: '部分匹配',
    WORD_BOUNDARY: '词边界匹配'
  }
  return labels[mode] || mode
}
</script>

<style scoped lang="scss">
.rule-form {
  .form-hint {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
    line-height: 1.4;
  }

  .rule-type-group {
    :deep(.el-radio-button) {
      margin-right: 12px;
      margin-bottom: 8px;
    }
  }

  .category-option {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }
}

.preview-card {
  margin-top: 20px;

  .rule-preview {
    .preview-item {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;

      &:last-child {
        margin-bottom: 0;
      }

      .label {
        font-weight: 500;
        color: #606266;
        min-width: 80px;
      }

      .keyword-code {
        background-color: #f5f7fa;
        padding: 2px 6px;
        border-radius: 3px;
        font-family: 'Courier New', monospace;
        font-size: 12px;
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
  .rule-type-group {
    :deep(.el-radio-button) {
      display: block;
      margin-bottom: 8px;
      margin-right: 0;
    }
  }
}
</style>