<template>
  <el-dialog
    v-model="visible"
    title="发放优惠券"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div v-if="template" class="template-info">
      <el-descriptions title="模板信息" :column="2" border>
        <el-descriptions-item label="模板名称">{{ template.name }}</el-descriptions-item>
        <el-descriptions-item label="模板编码">{{ template.tplCode }}</el-descriptions-item>
        <el-descriptions-item label="优惠券类型">
          <el-tag :type="getCouponTypeColor(template.type)">
            {{ getCouponTypeText(template.type) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="优惠信息">
          <div class="coupon-value">
            <div v-if="template.type === 'FULL_REDUCTION'" class="full-reduction">
              <span class="threshold">满¥{{ (template.thresholdAmount / 100).toFixed(2) }}</span>
              <span class="value">减¥{{ (template.faceValue / 100).toFixed(2) }}</span>
            </div>
            <div v-else-if="template.type === 'DISCOUNT'" class="discount">
              <span class="rate">{{ template.discountRate }}折</span>
              <span v-if="template.thresholdAmount" class="threshold">
                满¥{{ (template.thresholdAmount / 100).toFixed(2) }}
              </span>
            </div>
            <div v-else class="cash-voucher">
              <span class="value">¥{{ (template.faceValue / 100).toFixed(2) }}</span>
            </div>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="剩余库存">
          <el-tag :type="getStockType(template)">
            {{ template.total ? (template.total - (template.issued || 0)) : '不限' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="有效期">
          <div class="validity-period">
            {{ formatDateTime(template.validFrom) }} 至 {{ formatDateTime(template.validTo) }}
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <el-divider />

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="发放方式" prop="issueType">
        <el-radio-group v-model="form.issueType" @change="handleIssueTypeChange">
          <el-radio-button value="SINGLE">单个用户</el-radio-button>
          <el-radio-button value="BATCH">批量发放</el-radio-button>
          <el-radio-button value="PUBLIC">公开领取</el-radio-button>
        </el-radio-group>
      </el-form-item>

      <!-- 单个用户发放 -->
      <template v-if="form.issueType === 'SINGLE'">
        <el-form-item label="用户ID" prop="userId">
          <el-input
            v-model="form.userId"
            placeholder="请输入用户ID"
            clearable
          />
        </el-form-item>
        <el-form-item label="发放数量" prop="count">
          <el-input-number
            v-model="form.count"
            :min="1"
            :max="getMaxCount()"
            placeholder="发放数量"
            style="width: 100%"
          />
        </el-form-item>
      </template>

      <!-- 批量发放 -->
      <template v-else-if="form.issueType === 'BATCH'">
        <el-form-item label="用户列表" prop="userIds">
          <el-input
            v-model="form.userListText"
            type="textarea"
            :rows="5"
            placeholder="请输入用户ID列表，每行一个或用逗号分隔"
            @blur="parseUserList"
          />
          <div class="form-hint">
            支持每行一个用户ID或用逗号分隔，最多支持1000个用户
          </div>
        </el-form-item>
        <el-form-item label="每人数量" prop="count">
          <el-input-number
            v-model="form.count"
            :min="1"
            :max="10"
            placeholder="每人发放数量"
            style="width: 100%"
          />
        </el-form-item>
      </template>

      <!-- 公开领取 -->
      <template v-else-if="form.issueType === 'PUBLIC'">
        <el-form-item label="领取链接">
          <el-input
            :model-value="getPublicLink()"
            readonly
            placeholder="保存后生成领取链接"
          >
            <template #append>
              <el-button @click="copyLink" :disabled="!getPublicLink()">
                复制链接
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="领取说明">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入领取说明，将在领取页面显示"
            maxlength="200"
          />
        </el-form-item>
      </template>

      <el-form-item label="发放备注">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="2"
          placeholder="请输入发放备注"
          maxlength="100"
        />
      </el-form-item>
    </el-form>

    <!-- 发放预览 -->
    <div v-if="form.issueType === 'BATCH' && parsedUserList.length > 0" class="issue-preview">
      <el-divider content-position="left">发放预览</el-divider>
      <div class="preview-stats">
        <el-statistic title="发放用户数" :value="parsedUserList.length" />
        <el-statistic title="单人数量" :value="form.count || 1" />
        <el-statistic title="发放总量" :value="(parsedUserList.length * (form.count || 1))" />
      </div>
      <div class="user-list">
        <el-tag
          v-for="userId in parsedUserList.slice(0, 20)"
          :key="userId"
          size="small"
          style="margin-right: 8px; margin-bottom: 4px;"
        >
          {{ userId }}
        </el-tag>
        <span v-if="parsedUserList.length > 20" class="more-users">
          ...还有{{ parsedUserList.length - 20 }}个用户
        </span>
      </div>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          {{ form.issueType === 'PUBLIC' ? '创建领取链接' : '立即发放' }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, reactive, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import type { CouponTemplateDTO } from '@/types/coupon'
import { couponApi } from '@/api/coupon'

interface Props {
  modelValue: boolean
  template: CouponTemplateDTO | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formRef = ref<FormInstance>()
const loading = ref(false)
const parsedUserList = ref<string[]>([])

const form = reactive({
  issueType: 'SINGLE',
  userId: '',
  userIds: [] as string[],
  userListText: '',
  count: 1,
  remark: '',
  description: ''
})

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const rules: FormRules = {
  issueType: [
    { required: true, message: '请选择发放方式', trigger: 'change' }
  ],
  userId: [
    {
      validator: (rule, value, callback) => {
        if (form.issueType === 'SINGLE' && !value) {
          callback(new Error('请输入用户ID'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  userIds: [
    {
      validator: (rule, value, callback) => {
        if (form.issueType === 'BATCH' && parsedUserList.value.length === 0) {
          callback(new Error('请输入用户ID列表'))
          return
        }
        if (parsedUserList.value.length > 1000) {
          callback(new Error('批量发放最多支持1000个用户'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  count: [
    { required: true, message: '请输入发放数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '发放数量不能小于1', trigger: 'blur' }
  ]
}

const getMaxCount = () => {
  if (!props.template) return 1
  const perUserLimit = props.template.perUserLimit || 999
  return Math.min(perUserLimit, 10)
}

const getStockType = (template: CouponTemplateDTO) => {
  if (!template.total) return 'success'
  const remaining = template.total - (template.issued || 0)
  if (remaining <= 0) return 'danger'
  if (remaining <= template.total * 0.1) return 'warning'
  return 'success'
}

const parseUserList = () => {
  const text = form.userListText.trim()
  if (!text) {
    parsedUserList.value = []
    return
  }

  const userIds = text
    .split(/[\n,\s]+/)
    .map(id => id.trim())
    .filter(id => id)
    .filter((id, index, self) => self.indexOf(id) === index)

  parsedUserList.value = userIds
  form.userIds = userIds
}

const handleIssueTypeChange = () => {
  form.userId = ''
  form.userListText = ''
  form.userIds = []
  parsedUserList.value = []
  form.count = 1
  form.description = ''
}

const getPublicLink = () => {
  if (!props.template?.id) return ''
  return `${window.location.origin}/coupon/claim/${props.template.id}`
}

const copyLink = async () => {
  try {
    await navigator.clipboard.writeText(getPublicLink())
    ElMessage.success('链接已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value || !props.template) return

  try {
    await formRef.value.validate()
    
    await ElMessageBox.confirm(
      `确定要${form.issueType === 'PUBLIC' ? '创建领取链接' : '发放优惠券'}吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true

    if (form.issueType === 'SINGLE') {
      // 单个用户发放
      const response = await couponApi.batchIssueCoupon({
        templateId: props.template.id!,
        userId: form.userId,
        remark: form.remark
      }, form.count)

      if (response.success) {
        ElMessage.success(`成功为用户 ${form.userId} 发放 ${form.count} 张优惠券`)
        emit('success')
      } else {
        ElMessage.error(response.errMessage || '发放失败')
      }
    } else if (form.issueType === 'BATCH') {
      // 批量发放
      const promises = parsedUserList.value.map(userId =>
        couponApi.batchIssueCoupon({
          templateId: props.template.id!,
          userId,
          remark: form.remark
        }, form.count)
      )

      const results = await Promise.allSettled(promises)
      const successCount = results.filter(r => r.status === 'fulfilled' && r.value.success).length
      const failCount = results.length - successCount

      if (successCount > 0) {
        ElMessage.success(`成功发放 ${successCount} 个用户，失败 ${failCount} 个用户`)
        emit('success')
      } else {
        ElMessage.error('批量发放失败')
      }
    } else {
      // 公开领取 - 这里应该调用创建公开领取活动的API
      ElMessage.success('领取链接创建成功')
      emit('success')
    }

  } catch (error) {
    if (error !== 'cancel') {
      console.error('发放失败:', error)
      ElMessage.error('发放失败')
    }
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  formRef.value?.resetFields()
  parsedUserList.value = []
  Object.assign(form, {
    issueType: 'SINGLE',
    userId: '',
    userIds: [],
    userListText: '',
    count: 1,
    remark: '',
    description: ''
  })
  emit('update:modelValue', false)
}

const getCouponTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    FULL_REDUCTION: 'success',
    DISCOUNT: 'warning',
    CASH_VOUCHER: 'danger',
    FREE_SHIPPING: 'info'
  }
  return colorMap[type] || ''
}

const getCouponTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    FULL_REDUCTION: '满减券',
    DISCOUNT: '折扣券',
    CASH_VOUCHER: '代金券',
    FREE_SHIPPING: '免邮券'
  }
  return textMap[type] || type
}

const formatDateTime = (dateTime: string | null) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

watch(() => form.userListText, parseUserList)
</script>

<style scoped>
.template-info {
  margin-bottom: 20px;
}

.coupon-value {
  font-size: 12px;
}

.full-reduction .threshold {
  color: #909399;
  margin-right: 8px;
}

.full-reduction .value {
  color: #f56c6c;
  font-weight: bold;
}

.discount .rate {
  color: #e6a23c;
  font-weight: bold;
  margin-right: 8px;
}

.discount .threshold {
  color: #909399;
}

.cash-voucher .value {
  color: #67c23a;
  font-weight: bold;
}

.validity-period {
  font-size: 12px;
  line-height: 1.2;
}

.form-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.issue-preview {
  margin-top: 20px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.preview-stats {
  display: flex;
  gap: 40px;
  margin-bottom: 16px;
}

.user-list {
  max-height: 120px;
  overflow-y: auto;
}

.more-users {
  color: #909399;
  font-size: 12px;
}

.dialog-footer {
  text-align: right;
}

:deep(.el-statistic__number) {
  font-size: 20px;
}

:deep(.el-statistic__title) {
  font-size: 12px;
}
</style>