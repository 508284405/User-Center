<template>
  <el-dialog
    v-model="visible"
    :title="`券码管理 - ${template?.name || ''}`"
    width="1200px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="coupon-manage-container">
      <!-- 统计信息 -->
      <div class="stats-section" v-if="template">
        <el-row :gutter="20">
          <el-col :span="4">
            <el-statistic title="总发放量" :value="template.issued || 0" />
          </el-col>
          <el-col :span="4">
            <el-statistic title="剩余库存" :value="getRemainingStock()" />
          </el-col>
          <el-col :span="4">
            <el-statistic 
              title="已使用" 
              :value="stats.used || 0"
              :value-style="{ color: '#67c23a' }"
            />
          </el-col>
          <el-col :span="4">
            <el-statistic 
              title="已预占" 
              :value="stats.reserved || 0"
              :value-style="{ color: '#e6a23c' }"
            />
          </el-col>
          <el-col :span="4">
            <el-statistic 
              title="已过期" 
              :value="stats.expired || 0"
              :value-style="{ color: '#f56c6c' }"
            />
          </el-col>
          <el-col :span="4">
            <el-statistic 
              title="使用率" 
              :value="getUsageRate()"
              suffix="%"
              :value-style="{ color: '#409eff' }"
            />
          </el-col>
        </el-row>
      </div>

      <el-divider />

      <!-- 查询条件 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="优惠券码">
          <el-input 
            v-model="searchForm.couponCode" 
            placeholder="请输入优惠券码" 
            clearable 
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input 
            v-model="searchForm.userId" 
            placeholder="请输入用户ID" 
            clearable 
            style="width: 150px;"
          />
        </el-form-item>
        <el-form-item label="券状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 120px;">
            <el-option label="未使用" value="UNUSED" />
            <el-option label="已预占" value="RESERVED" />
            <el-option label="已使用" value="USED" />
            <el-option label="已过期" value="EXPIRED" />
            <el-option label="已失效" value="INVALID" />
          </el-select>
        </el-form-item>
        <el-form-item label="发放时间">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 350px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="exportCoupons">导出</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="couponCode" label="优惠券码" width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button type="text" @click="copyCouponCode(row.couponCode)">
              {{ row.couponCode }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" width="120" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getCouponStatusType(row.status)">
              {{ getCouponStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优惠信息" width="150">
          <template #default="{ row }">
            <div class="coupon-value" v-if="template">
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
          </template>
        </el-table-column>
        <el-table-column prop="issueTime" label="发放时间" width="150">
          <template #default="{ row }">
            {{ formatDateTime(row.issueTime) }}
          </template>
        </el-table-column>
        <el-table-column label="有效期" width="200">
          <template #default="{ row }">
            <div class="validity-period">
              <div>{{ formatDateTime(row.validFrom) }}</div>
              <div style="text-align: center; color: #909399;">至</div>
              <div>{{ formatDateTime(row.validTo) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="orderId" label="订单ID" width="120" show-overflow-tooltip />
        <el-table-column prop="useTime" label="使用时间" width="150">
          <template #default="{ row }">
            {{ formatDateTime(row.useTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewCouponDetail(row)">详情</el-button>
            <el-button 
              v-if="row.status === 'RESERVED'" 
              type="warning" 
              size="small" 
              @click="cancelReservation(row)"
            >
              取消预占
            </el-button>
            <el-button 
              v-if="row.status === 'UNUSED' && !isExpired(row)" 
              type="danger" 
              size="small" 
              @click="expireCoupon(row)"
            >
              强制过期
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :small="false"
        :disabled="false"
        :background="true"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: center"
      />
    </div>

    <!-- 优惠券详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="优惠券详情"
      width="500px"
      append-to-body
    >
      <el-descriptions v-if="selectedCoupon" :column="1" border>
        <el-descriptions-item label="优惠券码">{{ selectedCoupon.couponCode }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ selectedCoupon.userId }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getCouponStatusType(selectedCoupon.status)">
            {{ getCouponStatusText(selectedCoupon.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发放时间">{{ formatDateTime(selectedCoupon.issueTime) }}</el-descriptions-item>
        <el-descriptions-item label="有效期开始">{{ formatDateTime(selectedCoupon.validFrom) }}</el-descriptions-item>
        <el-descriptions-item label="有效期结束">{{ formatDateTime(selectedCoupon.validTo) }}</el-descriptions-item>
        <el-descriptions-item label="订单ID" v-if="selectedCoupon.orderId">{{ selectedCoupon.orderId }}</el-descriptions-item>
        <el-descriptions-item label="使用时间" v-if="selectedCoupon.useTime">{{ formatDateTime(selectedCoupon.useTime) }}</el-descriptions-item>
        <el-descriptions-item label="预占时间" v-if="selectedCoupon.reserveTime">{{ formatDateTime(selectedCoupon.reserveTime) }}</el-descriptions-item>
        <el-descriptions-item label="备注" v-if="selectedCoupon.remark">{{ selectedCoupon.remark }}</el-descriptions-item>
        <el-descriptions-item label="发放批次" v-if="selectedCoupon.batchId">{{ selectedCoupon.batchId }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { CouponTemplateDTO, CouponDTO } from '@/types/coupon'
import { couponApi } from '@/api/coupon'

interface Props {
  modelValue: boolean
  template: CouponTemplateDTO | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const loading = ref(false)
const tableData = ref<CouponDTO[]>([])
const detailDialogVisible = ref(false)
const selectedCoupon = ref<CouponDTO | null>(null)
const dateRange = ref<[string, string] | null>(null)

const searchForm = reactive({
  couponCode: '',
  userId: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const stats = reactive({
  used: 0,
  reserved: 0,
  expired: 0,
  unused: 0
})

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const getRemainingStock = () => {
  if (!props.template?.total) return '不限'
  return props.template.total - (props.template.issued || 0)
}

const getUsageRate = () => {
  const total = props.template?.issued || 0
  if (total === 0) return 0
  return Math.round((stats.used / total) * 100)
}

const fetchCoupons = async () => {
  if (!props.template?.id) return

  try {
    loading.value = true
    const params: any = {
      pageNum: pagination.page,
      pageSize: pagination.size,
      templateId: props.template.id
    }

    if (searchForm.couponCode) params.couponCode = searchForm.couponCode
    if (searchForm.userId) params.userId = searchForm.userId
    if (searchForm.status) params.status = searchForm.status
    if (dateRange.value) {
      params.issueTimeStart = dateRange.value[0]
      params.issueTimeEnd = dateRange.value[1]
    }

    const response = await couponApi.getTemplateCoupons(params)
    if (response.success) {
      tableData.value = response.data || []
      pagination.total = response.totalCount || 0
      
      // 更新统计信息
      updateStats()
    }
  } catch (error) {
    ElMessage.error('获取券码数据失败')
  } finally {
    loading.value = false
  }
}

const updateStats = () => {
  stats.used = tableData.value.filter(c => c.status === 'USED').length
  stats.reserved = tableData.value.filter(c => c.status === 'RESERVED').length
  stats.expired = tableData.value.filter(c => c.status === 'EXPIRED').length
  stats.unused = tableData.value.filter(c => c.status === 'UNUSED').length
}

const handleSearch = () => {
  pagination.page = 1
  fetchCoupons()
}

const handleReset = () => {
  Object.assign(searchForm, {
    couponCode: '',
    userId: '',
    status: ''
  })
  dateRange.value = null
  pagination.page = 1
  fetchCoupons()
}

const handleSizeChange = (val: number) => {
  pagination.size = val
  fetchCoupons()
}

const handleCurrentChange = (val: number) => {
  pagination.page = val
  fetchCoupons()
}

const viewCouponDetail = (coupon: CouponDTO) => {
  selectedCoupon.value = coupon
  detailDialogVisible.value = true
}

const copyCouponCode = async (code: string) => {
  try {
    await navigator.clipboard.writeText(code)
    ElMessage.success('优惠券码已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

const cancelReservation = async (coupon: CouponDTO) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消优惠券 ${coupon.couponCode} 的预占状态吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await couponApi.cancelReservation(coupon.couponCode, coupon.orderId || '')
    if (response.success) {
      ElMessage.success('取消预占成功')
      fetchCoupons()
    } else {
      ElMessage.error(response.errMessage || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const expireCoupon = async (coupon: CouponDTO) => {
  try {
    await ElMessageBox.confirm(
      `确定要强制过期优惠券 ${coupon.couponCode} 吗？过期后将不能再使用！`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await couponApi.expireCoupon(coupon.couponCode)
    if (response.success) {
      ElMessage.success('强制过期成功')
      fetchCoupons()
    } else {
      ElMessage.error(response.errMessage || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const exportCoupons = async () => {
  try {
    ElMessage.info('正在导出，请稍候...')
    // 这里应该调用导出API
    const params = {
      templateId: props.template?.id,
      ...searchForm,
      ...(dateRange.value ? {
        issueTimeStart: dateRange.value[0],
        issueTimeEnd: dateRange.value[1]
      } : {})
    }
    
    // 模拟导出
    setTimeout(() => {
      ElMessage.success('导出完成')
    }, 2000)
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const isExpired = (coupon: CouponDTO) => {
  return new Date(coupon.validTo) < new Date()
}

const getCouponStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    UNUSED: 'success',
    RESERVED: 'warning',
    USED: 'info',
    EXPIRED: 'danger',
    INVALID: 'danger'
  }
  return typeMap[status] || ''
}

const getCouponStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    UNUSED: '未使用',
    RESERVED: '已预占',
    USED: '已使用',
    EXPIRED: '已过期',
    INVALID: '已失效'
  }
  return textMap[status] || status
}

const formatDateTime = (dateTime: string | null) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const handleClose = () => {
  emit('update:modelValue', false)
  // 重置数据
  tableData.value = []
  Object.assign(searchForm, {
    couponCode: '',
    userId: '',
    status: ''
  })
  dateRange.value = null
  pagination.page = 1
  Object.assign(stats, {
    used: 0,
    reserved: 0,
    expired: 0,
    unused: 0
  })
}

onMounted(() => {
  if (props.modelValue && props.template) {
    fetchCoupons()
  }
})

// 监听对话框打开
watch(() => props.modelValue, (newVal) => {
  if (newVal && props.template) {
    fetchCoupons()
  }
}, { immediate: true })
</script>

<style scoped>
.coupon-manage-container {
  max-height: 70vh;
  overflow-y: auto;
}

.stats-section {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.search-form {
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

:deep(.el-statistic__number) {
  font-size: 18px;
}

:deep(.el-statistic__title) {
  font-size: 12px;
}

:deep(.el-button--text) {
  padding: 0;
  color: #409eff;
}

:deep(.el-button--text:hover) {
  color: #66b1ff;
}
</style>