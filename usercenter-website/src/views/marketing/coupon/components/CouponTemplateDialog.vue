<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="800px"
    :close-on-click-modal="false"
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
          <el-form-item label="模板名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入模板名称" maxlength="50" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="模板编码" prop="tplCode">
            <el-input 
              v-model="form.tplCode" 
              placeholder="请输入模板编码" 
              maxlength="20"
              :disabled="isEdit"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="优惠券类型" prop="type">
            <el-select v-model="form.type" placeholder="选择类型" :disabled="isEdit">
              <el-option label="满减券" value="FULL_REDUCTION" />
              <el-option label="折扣券" value="DISCOUNT" />
              <el-option label="代金券" value="CASH_VOUCHER" />
              <el-option label="免邮券" value="FREE_SHIPPING" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="发放总量" prop="total">
            <el-input-number
              v-model="form.total"
              :min="1"
              :max="999999999"
              placeholder="发放总量，0表示不限制"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 优惠信息配置 -->
      <el-divider content-position="left">优惠信息</el-divider>
      
      <!-- 满减券配置 -->
      <template v-if="form.type === 'FULL_REDUCTION'">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="门槛金额" prop="thresholdAmount">
              <el-input-number
                v-model="form.thresholdAmount"
                :min="0"
                :precision="2"
                placeholder="满多少元"
                style="width: 100%"
              />
              <span class="input-hint">单位：元</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="减免金额" prop="faceValue">
              <el-input-number
                v-model="form.faceValue"
                :min="0.01"
                :precision="2"
                placeholder="减多少元"
                style="width: 100%"
              />
              <span class="input-hint">单位：元</span>
            </el-form-item>
          </el-col>
        </el-row>
      </template>

      <!-- 折扣券配置 -->
      <template v-else-if="form.type === 'DISCOUNT'">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="折扣率" prop="discountRate">
              <el-input-number
                v-model="form.discountRate"
                :min="0.1"
                :max="9.9"
                :precision="1"
                :step="0.1"
                placeholder="折扣率"
                style="width: 100%"
              />
              <span class="input-hint">如：8.5表示8.5折</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="门槛金额">
              <el-input-number
                v-model="form.thresholdAmount"
                :min="0"
                :precision="2"
                placeholder="满多少元可用，0表示无门槛"
                style="width: 100%"
              />
              <span class="input-hint">单位：元，可选</span>
            </el-form-item>
          </el-col>
        </el-row>
      </template>

      <!-- 代金券配置 -->
      <template v-else-if="form.type === 'CASH_VOUCHER'">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="面额" prop="faceValue">
              <el-input-number
                v-model="form.faceValue"
                :min="0.01"
                :precision="2"
                placeholder="代金券面额"
                style="width: 100%"
              />
              <span class="input-hint">单位：元</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="门槛金额">
              <el-input-number
                v-model="form.thresholdAmount"
                :min="0"
                :precision="2"
                placeholder="满多少元可用，0表示无门槛"
                style="width: 100%"
              />
              <span class="input-hint">单位：元，可选</span>
            </el-form-item>
          </el-col>
        </el-row>
      </template>

      <!-- 有效期配置 -->
      <el-divider content-position="left">有效期设置</el-divider>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="生效时间" prop="validFrom">
            <el-date-picker
              v-model="form.validFrom"
              type="datetime"
              placeholder="选择生效时间"
              style="width: 100%"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="失效时间" prop="validTo">
            <el-date-picker
              v-model="form.validTo"
              type="datetime"
              placeholder="选择失效时间"
              style="width: 100%"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 发放期间配置 -->
      <el-divider content-position="left">发放期间</el-divider>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="发放开始" prop="issueStart">
            <el-date-picker
              v-model="form.issueStart"
              type="datetime"
              placeholder="选择发放开始时间"
              style="width: 100%"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="发放结束" prop="issueEnd">
            <el-date-picker
              v-model="form.issueEnd"
              type="datetime"
              placeholder="选择发放结束时间"
              style="width: 100%"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 使用规则 -->
      <el-divider content-position="left">使用规则</el-divider>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="每人限领">
            <el-input-number
              v-model="form.perUserLimit"
              :min="0"
              placeholder="每人最多领取数量，0表示不限制"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="使用次数">
            <el-input-number
              v-model="form.useLimit"
              :min="1"
              :max="99"
              placeholder="每张券可使用次数"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="使用说明">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入优惠券使用说明"
          maxlength="500"
        />
      </el-form-item>

      <el-form-item label="使用条件" v-if="!isView">
        <el-input
          v-model="form.constraints"
          type="textarea"
          :rows="2"
          placeholder="JSON格式的使用条件，如商品类别、用户等级等限制"
        />
        <div class="form-hint">
          示例：{"productCategories": ["electronics"], "minUserLevel": 1}
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button v-if="!isView" type="primary" @click="handleSubmit" :loading="loading">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import type { CouponTemplateDTO } from '@/types/coupon'
import { couponApi } from '@/api/coupon'

interface Props {
  modelValue: boolean
  formData: Partial<CouponTemplateDTO>
  isEdit?: boolean
  isView?: boolean
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = withDefaults(defineProps<Props>(), {
  isEdit: false,
  isView: false
})

const emit = defineEmits<Emits>()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = ref<Partial<CouponTemplateDTO>>({
  name: '',
  tplCode: '',
  type: 'FULL_REDUCTION',
  total: 1000,
  faceValue: 0,
  thresholdAmount: 0,
  discountRate: 8.0,
  validFrom: '',
  validTo: '',
  issueStart: '',
  issueEnd: '',
  perUserLimit: 1,
  useLimit: 1,
  description: '',
  constraints: '',
  status: 'DRAFT'
})

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const dialogTitle = computed(() => {
  if (props.isView) return '查看优惠券模板'
  if (props.isEdit) return '编辑优惠券模板'
  return '创建优惠券模板'
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入模板名称', trigger: 'blur' },
    { min: 2, max: 50, message: '模板名称长度在2-50个字符', trigger: 'blur' }
  ],
  tplCode: [
    { required: true, message: '请输入模板编码', trigger: 'blur' },
    { pattern: /^[A-Z0-9_]{3,20}$/, message: '模板编码只能包含大写字母、数字和下划线，长度3-20位', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择优惠券类型', trigger: 'change' }
  ],
  total: [
    { required: true, message: '请输入发放总量', trigger: 'blur' },
    { type: 'number', min: 0, message: '发放总量不能小于0', trigger: 'blur' }
  ],
  faceValue: [
    { 
      validator: (rule, value, callback) => {
        if (form.value.type === 'FULL_REDUCTION' || form.value.type === 'CASH_VOUCHER') {
          if (!value || value <= 0) {
            callback(new Error('请输入有效的金额'))
            return
          }
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  discountRate: [
    {
      validator: (rule, value, callback) => {
        if (form.value.type === 'DISCOUNT') {
          if (!value || value <= 0 || value >= 10) {
            callback(new Error('折扣率必须在0.1-9.9之间'))
            return
          }
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  validFrom: [
    { required: true, message: '请选择生效时间', trigger: 'change' }
  ],
  validTo: [
    { required: true, message: '请选择失效时间', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (value && form.value.validFrom && new Date(value) <= new Date(form.value.validFrom)) {
          callback(new Error('失效时间必须晚于生效时间'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  issueStart: [
    { required: true, message: '请选择发放开始时间', trigger: 'change' }
  ],
  issueEnd: [
    { required: true, message: '请选择发放结束时间', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (value && form.value.issueStart && new Date(value) <= new Date(form.value.issueStart)) {
          callback(new Error('发放结束时间必须晚于发放开始时间'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ]
}

watch(() => props.formData, (newVal) => {
  if (newVal) {
    Object.assign(form.value, {
      ...newVal,
      faceValue: newVal.faceValue ? newVal.faceValue / 100 : 0,
      thresholdAmount: newVal.thresholdAmount ? newVal.thresholdAmount / 100 : 0
    })
  }
}, { immediate: true, deep: true })

watch(() => form.value.type, () => {
  nextTick(() => {
    formRef.value?.clearValidate()
  })
})

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    
    loading.value = true
    
    const submitData = {
      ...form.value,
      faceValue: form.value.faceValue ? Math.round(form.value.faceValue * 100) : 0,
      thresholdAmount: form.value.thresholdAmount ? Math.round(form.value.thresholdAmount * 100) : 0
    }

    let response
    if (props.isEdit) {
      response = await couponApi.updateTemplate(submitData.id!, submitData)
    } else {
      response = await couponApi.createTemplate(submitData)
    }

    if (response.success) {
      ElMessage.success(props.isEdit ? '更新成功' : '创建成功')
      emit('success')
    } else {
      ElMessage.error(response.errMessage || '操作失败')
    }
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  formRef.value?.resetFields()
  emit('update:modelValue', false)
}
</script>

<style scoped>
.input-hint {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.form-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.dialog-footer {
  text-align: right;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-divider__text) {
  font-weight: 500;
  color: #409eff;
}
</style>