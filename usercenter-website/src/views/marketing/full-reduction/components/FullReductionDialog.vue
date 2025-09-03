<template>
  <el-dialog
    :model-value="modelValue"
    :title="dialogTitle"
    width="800px"
    @update:model-value="$emit('update:modelValue', $event)"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      :disabled="isView"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="活动名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入活动名称" maxlength="100" show-word-limit />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="优先级" prop="priority">
            <el-input-number v-model="form.priority" :min="1" :max="10" style="width: 100%" />
            <div class="form-hint">数值越大优先级越高</div>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="活动描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入活动描述"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker
              v-model="form.startTime"
              type="datetime"
              placeholder="选择开始时间"
              style="width: 100%"
              :disabled-date="(time) => time.getTime() < Date.now() - 86400000"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker
              v-model="form.endTime"
              type="datetime"
              placeholder="选择结束时间"
              style="width: 100%"
              :disabled-date="(time) => form.startTime && time.getTime() < form.startTime.getTime()"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="最小订单金额">
            <el-input-number
              v-model="form.minOrderAmount"
              :precision="2"
              :step="1"
              :min="0"
              style="width: 100%"
            >
              <template #append>元</template>
            </el-input-number>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="总使用次数限制">
            <el-input-number v-model="form.totalUsageLimit" :min="1" style="width: 100%" />
            <div class="form-hint">不填则不限制</div>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="用户使用次数限制">
            <el-input-number v-model="form.userUsageLimit" :min="1" style="width: 100%" />
            <div class="form-hint">单个用户最多使用次数</div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="组合使用设置">
            <div class="checkbox-group">
              <el-checkbox v-model="form.canCombineWithCoupon">可与优惠券叠加</el-checkbox>
              <el-checkbox v-model="form.canCombineWithMemberDiscount">可与会员折扣叠加</el-checkbox>
            </div>
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider content-position="left">适用范围</el-divider>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="适用商品">
            <el-input
              v-model="form.applicableProducts"
              type="textarea"
              :rows="2"
              placeholder="商品ID列表，用逗号分隔，留空表示全部商品"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="适用分类">
            <el-input
              v-model="form.applicableCategories"
              type="textarea"
              :rows="2"
              placeholder="分类ID列表，用逗号分隔，留空表示全部分类"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="排除商品">
        <el-input
          v-model="form.excludeProducts"
          type="textarea"
          :rows="2"
          placeholder="排除的商品ID列表，用逗号分隔"
        />
      </el-form-item>

      <el-divider content-position="left">满减规则</el-divider>

      <div class="rules-section">
        <div class="rules-header">
          <span>满减规则配置</span>
          <el-button type="primary" size="small" @click="addRule" :disabled="isView">添加规则</el-button>
        </div>

        <div v-if="form.rules && form.rules.length > 0" class="rules-list">
          <div v-for="(rule, index) in form.rules" :key="index" class="rule-item">
            <el-card>
              <div class="rule-content">
                <el-row :gutter="10" align="middle">
                  <el-col :span="5">
                    <el-form-item :label="`规则${index + 1}`" class="inline-form-item">
                      <el-input-number
                        v-model="rule.thresholdAmount"
                        :precision="2"
                        :step="1"
                        :min="0.01"
                        placeholder="满多少元"
                        style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="4">
                    <el-form-item label="减免类型" class="inline-form-item">
                      <el-select v-model="rule.reductionType" style="width: 100%">
                        <el-option label="固定金额" value="FIXED_AMOUNT" />
                        <el-option label="百分比折扣" value="PERCENTAGE" />
                        <el-option label="阶梯固定" value="TIERED_FIXED" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="4">
                    <el-form-item label="减免值" class="inline-form-item">
                      <el-input-number
                        v-model="rule.reductionValue"
                        :precision="2"
                        :step="0.01"
                        :min="0.01"
                        :max="getMaxReductionValue(rule)"
                        style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="4">
                    <el-form-item label="最大减免" class="inline-form-item">
                      <el-input-number
                        v-model="rule.maxReduction"
                        :precision="2"
                        :step="1"
                        :min="0"
                        placeholder="不限制"
                        style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="3">
                    <el-button type="danger" size="small" @click="removeRule(index)" :disabled="isView">删除</el-button>
                  </el-col>
                </el-row>
                <div class="rule-description">
                  {{ getRuleDescription(rule) }}
                </div>
              </div>
            </el-card>
          </div>
        </div>
        <el-empty v-else description="暂无规则，请添加满减规则" :image-size="100" />
      </div>
    </el-form>

    <template #footer v-if="!isView">
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { FullReductionActivityDTO, FullReductionRuleCmd } from '@/types/full-reduction'
import { fullReductionApi } from '@/api/full-reduction'

interface Props {
  modelValue: boolean
  formData: Partial<FullReductionActivityDTO>
  isEdit: boolean
  isView: boolean
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive<Partial<FullReductionActivityDTO> & { rules: FullReductionRuleCmd[] }>({
  name: '',
  description: '',
  startTime: undefined,
  endTime: undefined,
  minOrderAmount: 0,
  totalUsageLimit: undefined,
  userUsageLimit: 1,
  priority: 5,
  canCombineWithCoupon: true,
  canCombineWithMemberDiscount: true,
  applicableProducts: '',
  applicableCategories: '',
  excludeProducts: '',
  rules: []
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入活动名称', trigger: 'blur' },
    { min: 2, max: 100, message: '活动名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '活动描述不能超过500个字符', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请设置优先级', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  if (props.isView) return '查看满减活动'
  return props.isEdit ? '编辑满减活动' : '创建满减活动'
})

// 监听表单数据变化
watch(
  () => props.formData,
  (newData) => {
    if (newData) {
      Object.assign(form, {
        ...newData,
        startTime: newData.startTime ? new Date(newData.startTime) : undefined,
        endTime: newData.endTime ? new Date(newData.endTime) : undefined,
        minOrderAmount: newData.minOrderAmount ? newData.minOrderAmount / 100 : 0, // 转换为元
        rules: newData.rules?.map(rule => ({
          thresholdAmount: rule.thresholdAmount / 100, // 转换为元
          reductionType: rule.reductionType,
          reductionValue: rule.reductionValue,
          maxReduction: rule.maxReduction ? rule.maxReduction / 100 : undefined // 转换为元
        })) || []
      })
    }
  },
  { immediate: true, deep: true }
)

// 规则操作
const addRule = () => {
  form.rules.push({
    thresholdAmount: 100,
    reductionType: 'FIXED_AMOUNT',
    reductionValue: 10,
    maxReduction: undefined
  })
}

const removeRule = (index: number) => {
  form.rules.splice(index, 1)
}

const getMaxReductionValue = (rule: FullReductionRuleCmd) => {
  switch (rule.reductionType) {
    case 'PERCENTAGE':
      return 100
    case 'FIXED_AMOUNT':
    case 'TIERED_FIXED':
      return 999999
    default:
      return 999999
  }
}

const getRuleDescription = (rule: FullReductionRuleCmd) => {
  if (!rule.thresholdAmount || !rule.reductionValue) return ''
  
  const threshold = rule.thresholdAmount
  const value = rule.reductionValue
  const maxReduction = rule.maxReduction
  
  switch (rule.reductionType) {
    case 'FIXED_AMOUNT':
      return `满 ${threshold} 元减 ${value} 元${maxReduction ? `（最多减 ${maxReduction} 元）` : ''}`
    case 'PERCENTAGE':
      return `满 ${threshold} 元享 ${value}% 折扣${maxReduction ? `（最多减 ${maxReduction} 元）` : ''}`
    case 'TIERED_FIXED':
      return `满 ${threshold} 元减 ${value} 元（阶梯式）${maxReduction ? `（最多减 ${maxReduction} 元）` : ''}`
    default:
      return ''
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    
    if (form.rules.length === 0) {
      ElMessage.error('请至少添加一个满减规则')
      return
    }
    
    submitting.value = true

    const submitData = {
      ...form,
      minOrderAmount: Math.round((form.minOrderAmount || 0) * 100), // 转换为分
      createdBy: props.isEdit ? undefined : 'current-user',
      updatedBy: props.isEdit ? 'current-user' : undefined,
      rules: form.rules.map(rule => ({
        ...rule,
        thresholdAmount: Math.round(rule.thresholdAmount * 100), // 转换为分
        maxReduction: rule.maxReduction ? Math.round(rule.maxReduction * 100) : undefined // 转换为分
      }))
    }

    const response = props.isEdit
      ? await fullReductionApi.update(form.id!, submitData)
      : await fullReductionApi.create(submitData)

    if (response.success) {
      ElMessage.success(props.isEdit ? '更新成功' : '创建成功')
      emit('success')
    } else {
      ElMessage.error(response.errMessage || '操作失败')
    }
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    submitting.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  emit('update:modelValue', false)
  nextTick(() => {
    formRef.value?.resetFields()
    form.rules = []
  })
}
</script>

<style scoped>
.form-hint {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rules-section {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 16px;
  background-color: #f9f9f9;
}

.rules-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-weight: bold;
}

.rules-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rule-item {
  background: white;
  border-radius: 4px;
}

.rule-content {
  padding: 0;
}

.inline-form-item {
  margin-bottom: 0;
}

.inline-form-item :deep(.el-form-item__label) {
  font-size: 12px;
  padding-bottom: 4px;
}

.rule-description {
  margin-top: 8px;
  padding: 8px 12px;
  background-color: #f0f9ff;
  border: 1px solid #bfdbfe;
  border-radius: 4px;
  font-size: 13px;
  color: #1e40af;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>