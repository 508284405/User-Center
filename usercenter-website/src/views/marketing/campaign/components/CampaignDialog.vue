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
          <el-form-item label="活动名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入活动名称" maxlength="100" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="活动类型" prop="type">
            <el-select v-model="form.type" placeholder="选择活动类型" :disabled="isEdit">
              <el-option label="满减活动" value="FULL_REDUCTION" />
              <el-option label="折扣活动" value="DISCOUNT" />
              <el-option label="秒杀活动" value="SECKILL" />
              <el-option label="团购活动" value="GROUP_BUY" />
              <el-option label="新人专享" value="NEW_USER" />
              <el-option label="会员专享" value="MEMBER_ONLY" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="活动描述">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入活动描述"
          maxlength="500"
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
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
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
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 活动规则配置 -->
      <el-divider content-position="left">活动规则</el-divider>

      <!-- 满减活动规则 -->
      <template v-if="form.type === 'FULL_REDUCTION'">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="门槛金额" prop="threshold">
              <el-input-number
                v-model="form.threshold"
                :min="0"
                :precision="2"
                placeholder="满多少元"
                style="width: 100%"
              />
              <span class="input-hint">单位：元</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="减免金额" prop="discount">
              <el-input-number
                v-model="form.discount"
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

      <!-- 折扣活动规则 -->
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
                v-model="form.threshold"
                :min="0"
                :precision="2"
                placeholder="满多少元可享折扣，0表示无门槛"
                style="width: 100%"
              />
              <span class="input-hint">单位：元，可选</span>
            </el-form-item>
          </el-col>
        </el-row>
      </template>

      <!-- 秒杀活动规则 -->
      <template v-else-if="form.type === 'SECKILL'">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="秒杀价" prop="seckillPrice">
              <el-input-number
                v-model="form.seckillPrice"
                :min="0.01"
                :precision="2"
                placeholder="秒杀价格"
                style="width: 100%"
              />
              <span class="input-hint">单位：元</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库存数量" prop="totalStock">
              <el-input-number
                v-model="form.totalStock"
                :min="1"
                placeholder="秒杀库存"
                style="width: 100%"
              />
              <span class="input-hint">件</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="限购数量">
              <el-input-number
                v-model="form.limitPerUser"
                :min="1"
                :max="10"
                placeholder="每人限购"
                style="width: 100%"
              />
              <span class="input-hint">件，可选</span>
            </el-form-item>
          </el-col>
        </el-row>
      </template>

      <!-- 团购活动规则 -->
      <template v-else-if="form.type === 'GROUP_BUY'">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="团购价" prop="groupPrice">
              <el-input-number
                v-model="form.groupPrice"
                :min="0.01"
                :precision="2"
                placeholder="团购价格"
                style="width: 100%"
              />
              <span class="input-hint">单位：元</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="成团人数" prop="groupSize">
              <el-input-number
                v-model="form.groupSize"
                :min="2"
                :max="100"
                placeholder="成团人数"
                style="width: 100%"
              />
              <span class="input-hint">人</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="团购库存">
              <el-input-number
                v-model="form.totalStock"
                :min="1"
                placeholder="团购库存"
                style="width: 100%"
              />
              <span class="input-hint">件，可选</span>
            </el-form-item>
          </el-col>
        </el-row>
      </template>

      <!-- 其他活动类型的简单配置 -->
      <template v-else>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="折扣率">
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
                v-model="form.threshold"
                :min="0"
                :precision="2"
                placeholder="满多少元可享优惠"
                style="width: 100%"
              />
              <span class="input-hint">单位：元，可选</span>
            </el-form-item>
          </el-col>
        </el-row>
      </template>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="活动优先级">
            <el-input-number
              v-model="form.priority"
              :min="1"
              :max="100"
              placeholder="数字越大优先级越高"
              style="width: 100%"
            />
            <span class="input-hint">1-100，默认50</span>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="使用限制" v-if="!isView">
        <el-input
          v-model="form.constraints"
          type="textarea"
          :rows="2"
          placeholder="JSON格式的使用限制条件，如商品类别、用户等级等"
        />
        <div class="form-hint">
          示例：{"productCategories": ["electronics"], "minUserLevel": 1, "maxUsagePerUser": 1}
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
import type { CampaignDTO } from '@/types/campaign'
import { campaignApi } from '@/api/campaign'

interface Props {
  modelValue: boolean
  formData: Partial<CampaignDTO>
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

const form = ref<Partial<CampaignDTO>>({
  name: '',
  type: 'FULL_REDUCTION',
  description: '',
  startTime: '',
  endTime: '',
  threshold: 0,
  discount: 0,
  discountRate: 8.0,
  seckillPrice: 0,
  totalStock: 0,
  groupPrice: 0,
  groupSize: 2,
  limitPerUser: 1,
  priority: 50,
  constraints: '',
  status: 'DRAFT'
})

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const dialogTitle = computed(() => {
  if (props.isView) return '查看促销活动'
  if (props.isEdit) return '编辑促销活动'
  return '创建促销活动'
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入活动名称', trigger: 'blur' },
    { min: 2, max: 100, message: '活动名称长度在2-100个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择活动类型', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (value && form.value.startTime && new Date(value) <= new Date(form.value.startTime)) {
          callback(new Error('结束时间必须晚于开始时间'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  threshold: [
    {
      validator: (rule, value, callback) => {
        if (form.value.type === 'FULL_REDUCTION' && (!value || value <= 0)) {
          callback(new Error('请输入有效的门槛金额'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  discount: [
    {
      validator: (rule, value, callback) => {
        if (form.value.type === 'FULL_REDUCTION' && (!value || value <= 0)) {
          callback(new Error('请输入有效的减免金额'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  discountRate: [
    {
      validator: (rule, value, callback) => {
        if (form.value.type === 'DISCOUNT' && (!value || value <= 0 || value >= 10)) {
          callback(new Error('折扣率必须在0.1-9.9之间'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  seckillPrice: [
    {
      validator: (rule, value, callback) => {
        if (form.value.type === 'SECKILL' && (!value || value <= 0)) {
          callback(new Error('请输入有效的秒杀价格'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  totalStock: [
    {
      validator: (rule, value, callback) => {
        if (form.value.type === 'SECKILL' && (!value || value <= 0)) {
          callback(new Error('请输入有效的库存数量'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  groupPrice: [
    {
      validator: (rule, value, callback) => {
        if (form.value.type === 'GROUP_BUY' && (!value || value <= 0)) {
          callback(new Error('请输入有效的团购价格'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  groupSize: [
    {
      validator: (rule, value, callback) => {
        if (form.value.type === 'GROUP_BUY' && (!value || value < 2)) {
          callback(new Error('成团人数不能少于2人'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

watch(() => props.formData, (newVal) => {
  if (newVal) {
    Object.assign(form.value, {
      ...newVal,
      threshold: newVal.threshold ? newVal.threshold / 100 : 0,
      discount: newVal.discount ? newVal.discount / 100 : 0,
      seckillPrice: newVal.seckillPrice ? newVal.seckillPrice / 100 : 0,
      groupPrice: newVal.groupPrice ? newVal.groupPrice / 100 : 0
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
      threshold: form.value.threshold ? Math.round(form.value.threshold * 100) : 0,
      discount: form.value.discount ? Math.round(form.value.discount * 100) : 0,
      seckillPrice: form.value.seckillPrice ? Math.round(form.value.seckillPrice * 100) : 0,
      groupPrice: form.value.groupPrice ? Math.round(form.value.groupPrice * 100) : 0
    }

    let response
    if (props.isEdit) {
      response = await campaignApi.updateCampaign(submitData.id!, submitData)
    } else {
      response = await campaignApi.createCampaign(submitData)
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