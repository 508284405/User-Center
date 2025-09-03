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
          <el-form-item label="商品ID" prop="productId">
            <el-input-number v-model="form.productId" placeholder="请输入商品ID" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="商品名称" prop="productName">
        <el-input v-model="form.productName" placeholder="请输入商品名称" maxlength="200" />
      </el-form-item>

      <el-form-item label="活动描述">
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

      <el-divider content-position="left">价格设置</el-divider>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="商品原价" prop="originalPrice">
            <el-input-number
              v-model="form.originalPrice"
              :precision="2"
              :step="1"
              :min="0.01"
              style="width: 100%"
            >
              <template #append>元</template>
            </el-input-number>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="团购价格" prop="groupPrice">
            <el-input-number
              v-model="form.groupPrice"
              :precision="2"
              :step="1"
              :min="0.01"
              :max="form.originalPrice"
              style="width: 100%"
            >
              <template #append>元</template>
            </el-input-number>
          </el-form-item>
        </el-col>
      </el-row>

      <div v-if="form.originalPrice && form.groupPrice" class="discount-info">
        <el-alert
          :title="`团购优惠: ¥${(form.originalPrice - form.groupPrice).toFixed(2)} 
                  (${(((form.originalPrice - form.groupPrice) / form.originalPrice) * 100).toFixed(1)}% 折扣)`"
          type="success"
          show-icon
          :closable="false"
        />
      </div>

      <el-divider content-position="left">团购规则</el-divider>

      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="成团人数" prop="requiredParticipants">
            <el-input-number v-model="form.requiredParticipants" :min="2" :max="100" style="width: 100%" />
            <div class="form-hint">至少需要多少人才能成团</div>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="最大人数" prop="maxParticipants">
            <el-input-number 
              v-model="form.maxParticipants" 
              :min="form.requiredParticipants || 2" 
              :max="1000" 
              style="width: 100%" 
            />
            <div class="form-hint">单个团组最大参团人数</div>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="用户限购" prop="limitPerUser">
            <el-input-number v-model="form.limitPerUser" :min="1" :max="100" style="width: 100%" />
            <div class="form-hint">单个用户最多购买数量</div>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="成团超时" prop="groupTimeoutHours">
            <el-input-number v-model="form.groupTimeoutHours" :min="1" :max="168" style="width: 100%" />
            <template #append>小时</template>
            <div class="form-hint">团组成团的超时时间</div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="总库存" prop="totalStock">
            <el-input-number v-model="form.totalStock" :min="1" style="width: 100%" />
            <div class="form-hint">活动总库存数量</div>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 统计信息（仅查看时显示） -->
      <template v-if="isView && form.id">
        <el-divider content-position="left">活动统计</el-divider>
        
        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic title="可用库存" :value="form.availableStock || 0" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="已售数量" :value="form.soldCount || 0" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="开团总数" :value="form.totalGroups || 0" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="成功团数" :value="form.successfulGroups || 0" />
          </el-col>
        </el-row>

        <div style="margin-top: 20px;">
          <el-progress 
            :percentage="getSuccessRate()" 
            :color="getProgressColor()"
            :stroke-width="20"
          >
            <template #default="{ percentage }">
              <span class="percentage-value">成团成功率 {{ percentage }}%</span>
            </template>
          </el-progress>
        </div>
      </template>
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
import type { GroupBuyActivityDTO } from '@/types/group-buy'
import { groupBuyApi } from '@/api/group-buy'

interface Props {
  modelValue: boolean
  formData: Partial<GroupBuyActivityDTO>
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

const form = reactive<Partial<GroupBuyActivityDTO>>({
  name: '',
  description: '',
  productId: undefined,
  productName: '',
  startTime: undefined,
  endTime: undefined,
  originalPrice: undefined,
  groupPrice: undefined,
  requiredParticipants: 2,
  maxParticipants: 10,
  limitPerUser: 1,
  groupTimeoutHours: 24,
  totalStock: undefined
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入活动名称', trigger: 'blur' },
    { min: 2, max: 100, message: '活动名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  productId: [
    { required: true, message: '请输入商品ID', trigger: 'blur' }
  ],
  productName: [
    { required: true, message: '请输入商品名称', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ],
  originalPrice: [
    { required: true, message: '请输入商品原价', trigger: 'blur' }
  ],
  groupPrice: [
    { required: true, message: '请输入团购价格', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (form.originalPrice && value >= form.originalPrice) {
          callback(new Error('团购价格必须小于原价'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  requiredParticipants: [
    { required: true, message: '请设置成团人数', trigger: 'blur' }
  ],
  maxParticipants: [
    { required: true, message: '请设置最大人数', trigger: 'blur' }
  ],
  totalStock: [
    { required: true, message: '请输入总库存', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  if (props.isView) return '查看团购活动'
  return props.isEdit ? '编辑团购活动' : '创建团购活动'
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
        originalPrice: newData.originalPrice ? newData.originalPrice / 100 : undefined, // 转换为元
        groupPrice: newData.groupPrice ? newData.groupPrice / 100 : undefined // 转换为元
      })
    }
  },
  { immediate: true, deep: true }
)

// 监听最大人数，确保不小于成团人数
watch(
  () => form.requiredParticipants,
  (newVal) => {
    if (newVal && form.maxParticipants && form.maxParticipants < newVal) {
      form.maxParticipants = newVal
    }
  }
)

// 获取成功率
const getSuccessRate = () => {
  if (!form.totalGroups || form.totalGroups === 0) return 0
  return Math.round((form.successfulGroups || 0) / form.totalGroups * 100)
}

// 获取进度条颜色
const getProgressColor = () => {
  const rate = getSuccessRate()
  if (rate >= 80) return '#67c23a'
  if (rate >= 60) return '#e6a23c'
  if (rate >= 40) return '#f56c6c'
  return '#909399'
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    const submitData = {
      ...form,
      originalPrice: Math.round((form.originalPrice || 0) * 100), // 转换为分
      groupPrice: Math.round((form.groupPrice || 0) * 100), // 转换为分
      createdBy: props.isEdit ? undefined : 'current-user',
      updatedBy: props.isEdit ? 'current-user' : undefined
    }

    const response = props.isEdit
      ? await groupBuyApi.updateActivity(form.id!, submitData)
      : await groupBuyApi.createActivity(submitData)

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

.discount-info {
  margin-bottom: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.percentage-value {
  font-size: 14px;
  font-weight: bold;
}
</style>