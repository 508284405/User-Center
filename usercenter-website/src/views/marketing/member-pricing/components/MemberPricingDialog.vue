<template>
  <el-dialog
    :model-value="modelValue"
    :title="isEdit ? '编辑会员价配置' : '新增会员价配置'"
    width="600px"
    @update:model-value="$emit('update:modelValue', $event)"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
    >
      <el-form-item label="商品ID" prop="productId">
        <el-input v-model.number="form.productId" placeholder="请输入商品ID" :disabled="isEdit" />
      </el-form-item>

      <el-form-item label="会员等级" prop="memberLevel">
        <el-select v-model="form.memberLevel" placeholder="选择会员等级" :disabled="isEdit" style="width: 100%">
          <el-option label="银卡会员" value="SILVER" />
          <el-option label="金卡会员" value="GOLD" />
          <el-option label="白金会员" value="PLATINUM" />
          <el-option label="钻石会员" value="DIAMOND" />
        </el-select>
      </el-form-item>

      <el-form-item label="折扣类型" prop="discountType">
        <el-select v-model="form.discountType" placeholder="选择折扣类型" style="width: 100%">
          <el-option label="百分比折扣" value="PERCENTAGE" />
          <el-option label="固定金额减免" value="FIXED_AMOUNT" />
          <el-option label="固定价格" value="FIXED_PRICE" />
        </el-select>
      </el-form-item>

      <el-form-item label="折扣值" prop="discountValue">
        <el-input-number
          v-model="form.discountValue"
          :precision="2"
          :step="0.01"
          :min="0"
          :max="getMaxDiscountValue()"
          style="width: 100%"
        >
          <template #append>{{ getDiscountUnit() }}</template>
        </el-input-number>
        <div class="form-hint">{{ getDiscountHint() }}</div>
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="最小购买量">
            <el-input-number v-model="form.minPurchaseQuantity" :min="1" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="最大购买量">
            <el-input-number v-model="form.maxPurchaseQuantity" :min="1" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="开始时间">
            <el-date-picker
              v-model="form.startTime"
              type="datetime"
              placeholder="选择开始时间"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="结束时间">
            <el-date-picker
              v-model="form.endTime"
              type="datetime"
              placeholder="选择结束时间"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="状态">
        <el-switch v-model="form.isActive" active-text="启用" inactive-text="停用" />
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { MemberPricingDTO } from '@/types/member-pricing'
import { memberPricingApi } from '@/api/member-pricing'

interface Props {
  modelValue: boolean
  formData: Partial<MemberPricingDTO>
  isEdit: boolean
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive<Partial<MemberPricingDTO>>({
  productId: undefined,
  memberLevel: '',
  discountType: 'PERCENTAGE',
  discountValue: 0,
  minPurchaseQuantity: 1,
  maxPurchaseQuantity: undefined,
  startTime: undefined,
  endTime: undefined,
  isActive: true
})

const rules: FormRules = {
  productId: [
    { required: true, message: '请输入商品ID', trigger: 'blur' },
    { type: 'number', message: '商品ID必须是数字', trigger: 'blur' }
  ],
  memberLevel: [
    { required: true, message: '请选择会员等级', trigger: 'change' }
  ],
  discountType: [
    { required: true, message: '请选择折扣类型', trigger: 'change' }
  ],
  discountValue: [
    { required: true, message: '请输入折扣值', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (form.discountType === 'PERCENTAGE' && value > 100) {
          callback(new Error('百分比折扣不能超过100%'))
        } else if (value <= 0) {
          callback(new Error('折扣值必须大于0'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 计算属性
const getMaxDiscountValue = () => {
  return form.discountType === 'PERCENTAGE' ? 100 : 999999
}

const getDiscountUnit = () => {
  switch (form.discountType) {
    case 'PERCENTAGE':
      return '%'
    case 'FIXED_AMOUNT':
    case 'FIXED_PRICE':
      return '元'
    default:
      return ''
  }
}

const getDiscountHint = () => {
  switch (form.discountType) {
    case 'PERCENTAGE':
      return '输入折扣百分比，如：95表示95折'
    case 'FIXED_AMOUNT':
      return '输入减免金额，单位：元'
    case 'FIXED_PRICE':
      return '输入固定价格，单位：元'
    default:
      return ''
  }
}

// 监听表单数据变化
watch(
  () => props.formData,
  (newData) => {
    if (newData) {
      Object.assign(form, {
        ...newData,
        startTime: newData.startTime ? new Date(newData.startTime) : undefined,
        endTime: newData.endTime ? new Date(newData.endTime) : undefined
      })
    }
  },
  { immediate: true, deep: true }
)

// 监听折扣类型变化，调整折扣值
watch(
  () => form.discountType,
  (newType, oldType) => {
    if (newType !== oldType && form.discountValue) {
      if (newType === 'PERCENTAGE') {
        // 切换到百分比，如果当前值大于100则重置
        if (form.discountValue > 100) {
          form.discountValue = 95
        }
      } else if (oldType === 'PERCENTAGE') {
        // 从百分比切换到其他类型，重置为0
        form.discountValue = 0
      }
    }
  }
)

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    const submitData = {
      ...form,
      discountValue: form.discountType === 'PERCENTAGE' 
        ? form.discountValue 
        : (form.discountValue || 0) * 100, // 转换为分
      createdBy: props.isEdit ? undefined : 'current-user',
      updatedBy: props.isEdit ? 'current-user' : undefined
    }

    const response = props.isEdit
      ? await memberPricingApi.update(form.id!, submitData)
      : await memberPricingApi.create(submitData)

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
  })
}
</script>

<style scoped>
.form-hint {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>