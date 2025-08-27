<template>
  <div class="policy-form">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      @submit.prevent="handleSubmit"
    >
      <div class="form-section">
        <h3 class="section-title">基本信息</h3>
        
        <el-form-item label="策略名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入策略名称，如：标准用户聊天审核"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="策略编码" prop="code">
          <el-input
            v-model="formData.code"
            placeholder="请输入策略编码，如：STANDARD_USER_CHAT"
            maxlength="64"
            :disabled="isEdit"
          />
          <template #extra>
            <div class="form-tip">
              策略编码用于系统识别，创建后不可修改，建议使用大写字母和下划线
            </div>
          </template>
        </el-form-item>

        <el-form-item label="策略描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请描述此策略的用途和特点"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </div>

      <div class="form-section">
        <h3 class="section-title">策略配置</h3>
        
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="适用场景" prop="scenario">
              <el-select v-model="formData.scenario" style="width: 100%">
                <el-option
                  label="用户聊天"
                  value="USER_CHAT"
                  description="用户在聊天中发送的消息内容审核"
                />
                <el-option
                  label="机器人回复"
                  value="BOT_REPLY"
                  description="机器人生成的回复内容审核"
                />
                <el-option
                  label="内容发布"
                  value="CONTENT_PUBLISH"
                  description="用户发布的文档、文章等内容审核"
                />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="策略类型" prop="policyType">
              <el-select v-model="formData.policyType" style="width: 100%">
                <el-option label="标准" value="STANDARD" />
                <el-option label="严格" value="STRICT" />
                <el-option label="宽松" value="LENIENT" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="默认风险级别" prop="defaultRiskLevel">
              <el-select v-model="formData.defaultRiskLevel" style="width: 100%">
                <el-option label="低风险" value="LOW" />
                <el-option label="中风险" value="MEDIUM" />
                <el-option label="高风险" value="HIGH" />
                <el-option label="极高风险" value="CRITICAL" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="默认处理动作" prop="defaultAction">
              <el-select v-model="formData.defaultAction" style="width: 100%">
                <el-option label="通过" value="APPROVE" />
                <el-option label="拒绝" value="REJECT" />
                <el-option label="人工审核" value="MANUAL_REVIEW" />
                <el-option label="自动修复" value="AUTO_FIX" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-input-number
                v-model="formData.priority"
                :min="1"
                :max="999"
                style="width: 100%"
              />
              <template #extra>
                <div class="form-tip">数值越小优先级越高，相同场景下优先级高的策略会被优先使用</div>
              </template>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="关联模板">
              <el-select
                v-model="formData.templateId"
                placeholder="选择策略模板"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="template in templates"
                  :key="template.id"
                  :label="template.name"
                  :value="template.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="启用状态">
          <el-switch
            v-model="formData.isActive"
            active-text="启用"
            inactive-text="禁用"
          />
          <template #extra>
            <div class="form-tip">只有启用的策略才会被系统使用</div>
          </template>
        </el-form-item>
      </div>

      <div class="form-section">
        <h3 class="section-title">高级配置</h3>
        
        <el-form-item label="配置参数">
          <div class="config-params">
            <el-input
              v-model="configParamsText"
              type="textarea"
              :rows="6"
              placeholder="请输入JSON格式的配置参数，例如：&#10;{&#10;  &quot;timeout&quot;: 30,&#10;  &quot;maxRetries&quot;: 3,&#10;  &quot;enableCache&quot;: true&#10;}"
            />
            <div class="config-tip">
              <el-icon><Warning /></el-icon>
              配置参数必须是有效的JSON格式，错误的格式会导致策略无法正常使用
            </div>
          </div>
        </el-form-item>
      </div>

      <div class="form-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import { moderationApi, type ModerationPolicy, type ModerationPolicyTemplate } from '@/api/smartcs/moderation'

interface Props {
  modelValue: ModerationPolicy
  isEdit?: boolean
}

interface Emits {
  (e: 'update:modelValue', value: ModerationPolicy): void
  (e: 'submit', value: ModerationPolicy): void
}

const props = withDefaults(defineProps<Props>(), {
  isEdit: false
})

const emit = defineEmits<Emits>()

// 表单数据
const formData = reactive<ModerationPolicy>({ ...props.modelValue })
const formRef = ref()
const submitting = ref(false)
const templates = ref<ModerationPolicyTemplate[]>([])

// 配置参数文本
const configParamsText = ref('')

// 更新配置参数文本
const updateConfigParamsText = () => {
  if (formData.configParams) {
    configParamsText.value = JSON.stringify(formData.configParams, null, 2)
  } else {
    configParamsText.value = ''
  }
}

// 表单规则
const formRules = {
  name: [
    { required: true, message: '请输入策略名称', trigger: 'blur' },
    { min: 2, max: 100, message: '策略名称长度应在2-100字符之间', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入策略编码', trigger: 'blur' },
    { 
      pattern: /^[A-Z][A-Z0-9_]*$/, 
      message: '策略编码只能包含大写字母、数字和下划线，且必须以字母开头', 
      trigger: 'blur' 
    }
  ],
  scenario: [
    { required: true, message: '请选择适用场景', trigger: 'change' }
  ],
  policyType: [
    { required: true, message: '请选择策略类型', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请输入优先级', trigger: 'blur' },
    { type: 'number', min: 1, max: 999, message: '优先级应在1-999之间', trigger: 'blur' }
  ]
}

// 监听 props 变化
watch(
  () => props.modelValue,
  (newValue) => {
    Object.assign(formData, newValue)
    updateConfigParamsText()
  },
  { deep: true, immediate: true }
)

// 监听表单数据变化
watch(
  formData,
  (newValue) => {
    emit('update:modelValue', newValue)
  },
  { deep: true }
)

// 监听配置参数文本变化
watch(
  configParamsText,
  (newValue) => {
    if (newValue.trim()) {
      try {
        formData.configParams = JSON.parse(newValue)
      } catch (error) {
        // JSON格式错误时不更新formData，保持原有值
      }
    } else {
      formData.configParams = undefined
    }
  }
)

// 页面加载时获取模板列表
onMounted(async () => {
  await loadTemplates()
  updateConfigParamsText()
})

// 加载模板列表
const loadTemplates = async () => {
  try {
    const response = await moderationApi.getAllActiveTemplates()
    templates.value = response.data
  } catch (error) {
    console.error('加载模板失败:', error)
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
    
    // 验证配置参数JSON格式
    if (configParamsText.value.trim()) {
      try {
        JSON.parse(configParamsText.value)
      } catch (error) {
        ElMessage.error('配置参数不是有效的JSON格式')
        return
      }
    }
    
    submitting.value = true
    emit('submit', { ...formData })
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    submitting.value = false
  }
}

// 取消
const handleCancel = () => {
  // 由父组件处理取消逻辑
}

// 暴露方法给父组件
defineExpose({
  validate: () => formRef.value?.validate(),
  resetFields: () => formRef.value?.resetFields()
})
</script>

<style scoped lang="scss">
.policy-form {
  .form-section {
    margin-bottom: 32px;
    
    .section-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 16px 0;
      padding-bottom: 8px;
      border-bottom: 2px solid #e4e7ed;
    }
  }

  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
    line-height: 1.4;
  }

  .config-params {
    width: 100%;
    
    .config-tip {
      display: flex;
      align-items: center;
      gap: 4px;
      margin-top: 8px;
      padding: 8px 12px;
      background: #fdf6ec;
      border: 1px solid #fcdcb6;
      border-radius: 4px;
      font-size: 12px;
      color: #e6a23c;
    }
  }

  .form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding-top: 24px;
    border-top: 1px solid #e4e7ed;
  }
}

// Element Plus 表单样式自定义
:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-input__wrapper) {
  border-radius: 6px;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 6px;
}

:deep(.el-textarea .el-textarea__inner) {
  border-radius: 6px;
}

:deep(.el-input-number .el-input__wrapper) {
  border-radius: 6px;
}
</style>