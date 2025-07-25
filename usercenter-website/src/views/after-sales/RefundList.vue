<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { afterSalesApi } from '@/api/client-web/afterSales'
import { AfterSaleRefundItem } from '@/api/client-web/afterSaleRefund'

const router = useRouter()

// 表格数据
const tableData = ref<AfterSaleRefundItem[]>([])
const loading = ref(false)
const total = ref(0)

// 日期范围
const dateRange = ref<[string, string]>(['', ''])

// 查询参数
const queryParams = reactive({
  refundNo: '',
  afterSaleNo: '',
  refundStatus: '',
  startTime: undefined as number | undefined,
  endTime: undefined as number | undefined,
  pageIndex: 1,
  pageSize: 10,
  needTotalCount: true
})

// 退款状态选项
const statusOptions = [
  { value: '', label: '全部状态' },
  { value: 'PENDING', label: '待处理' },
  { value: 'PROCESSING', label: '处理中' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'REJECTED', label: '已拒绝' },
  { value: 'FAILED', label: '退款失败' }
]

// 获取标签类型
const getStatusTagType = (status: string): string => {
  switch (status) {
    case 'COMPLETED':
      return 'success';
    case 'REJECTED':
    case 'FAILED':
      return 'danger';
    case 'PENDING':
      return 'info';
    case 'PROCESSING':
      return 'primary';
    default:
      return 'warning';
  }
}

// 监听日期变化
const handleDateChange = (val: [string, string]) => {
  queryParams.startTime = val[0] ? new Date(val[0]).getTime() : undefined
  queryParams.endTime = val[1] ? new Date(val[1]).getTime() : undefined
}

// 获取退款列表数据
const getList = async () => {
  loading.value = true
  try {
    const res: any = await afterSalesApi.pageRefundRecords(queryParams)
    // 取出实际数据字段
    if (res.data && Array.isArray(res.data)) {
      tableData.value = res.data
      total.value = res.totalCount || 0
    } else {
      tableData.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取退款列表失败:', error)
    ElMessage.error('获取退款列表失败')
  } finally {
    loading.value = false
  }
}

// 重置查询条件
const resetQuery = () => {
  queryParams.refundNo = ''
  queryParams.afterSaleNo = ''
  queryParams.refundStatus = ''
  queryParams.startTime = undefined
  queryParams.endTime = undefined
  dateRange.value = ['', '']
  queryParams.pageIndex = 1
  getList()
}

// 处理查询
const handleQuery = () => {
  queryParams.pageIndex = 1
  getList()
}

// 处理分页变化
const handleSizeChange = (val: number) => {
  queryParams.pageSize = val
  getList()
}

const handleCurrentChange = (val: number) => {
  queryParams.pageIndex = val
  getList()
}

// 查看详情
const handleViewDetail = (row: AfterSaleRefundItem) => {
  router.push(`/after-sales/refund/${row.refundNo}`)
}

// 处理退款
const handleProcessRefund = async (row: AfterSaleRefundItem) => {
  try {
    ElMessageBox.confirm('确认处理此退款申请吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      await afterSalesApi.processRefund(row.refundNo)
      ElMessage.success('退款处理成功')
      getList()
    }).catch(() => {
      // 用户取消操作
    })
  } catch (error) {
    console.error('处理退款失败:', error)
    ElMessage.error('处理退款失败')
  }
}

// 拒绝退款
const handleRejectRefund = async (row: AfterSaleRefundItem) => {
  try {
    ElMessageBox.prompt('请输入拒绝原因', '拒绝退款', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '拒绝原因不能为空'
    }).then(async ({ value }) => {
      await afterSalesApi.rejectRefund(row.refundNo, value)
      ElMessage.success('已拒绝退款申请')
      getList()
    }).catch(() => {
      // 用户取消操作
    })
  } catch (error) {
    console.error('拒绝退款失败:', error)
    ElMessage.error('拒绝退款失败')
  }
}

// 导出退款数据
const handleExport = async () => {
  try {
    loading.value = true
    // 标注为 any 避免类型报错
    const res: any = await afterSalesApi.exportAfterSales(queryParams as any)
    
    // 处理blob数据
    const blob = new Blob([res], { type: 'application/vnd.ms-excel' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `退款数据_${new Date().getTime()}.xlsx`
    link.click()
    URL.revokeObjectURL(link.href)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出退款数据失败:', error)
    ElMessage.error('导出退款数据失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (ts: string | number) => {
  if (!ts) return '';
  const date = new Date(Number(ts));
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  const hh = String(date.getHours()).padStart(2, '0');
  const mm = String(date.getMinutes()).padStart(2, '0');
  const ss = String(date.getSeconds()).padStart(2, '0');
  return `${y}-${m}-${d} ${hh}:${mm}:${ss}`;
}

// 页面加载时获取数据
onMounted(() => {
  getList()
})
</script>

<template>
  <div class="refund-container">
    <div class="refund-card">
      <!-- 搜索区域 -->
      <div class="search-section">
        <el-form :model="queryParams" inline>
          <el-form-item label="退款编号">
            <el-input
              v-model="queryParams.refundNo"
              placeholder="请输入退款编号"
              clearable
              style="width: 200px"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="售后单号">
            <el-input
              v-model="queryParams.afterSaleNo"
              placeholder="请输入售后单号"
              clearable
              style="width: 200px"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="退款状态">
            <el-select
              v-model="queryParams.refundStatus"
              placeholder="请选择状态"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 240px"
              @change="handleDateChange"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleQuery">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        
        <div class="right-btn-group">
          <el-button type="success" :icon="Download" @click="handleExport">导出</el-button>
        </div>
      </div>
      
      <!-- 表格区域 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%"
        border
        stripe
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="afterSaleNo" label="售后单号" width="150" />
        <el-table-column prop="orderNumber" label="订单编号" width="150" />
        <el-table-column prop="refundNo" label="退款编号" width="150" />
        <el-table-column prop="refundAmount" label="退款金额" width="100">
          <template #default="{ row }">
            {{ row.refundAmount ? `¥${row.refundAmount.toFixed(2)}` : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="refundStatus" label="退款状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.refundStatus)">
              {{ statusOptions.find(item => item.value === row.refundStatus)?.label || row.refundStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="refundTime" label="退款时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.refundTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="退款原因" min-width="150" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              size="small"
              @click="handleViewDetail(row)"
            >
              查看详情
            </el-button>
            <el-button
              v-if="row.refundStatus === 'PENDING'"
              type="success"
              link
              size="small"
              @click="handleProcessRefund(row)"
            >
              处理
            </el-button>
            <el-button
              v-if="row.refundStatus === 'PENDING'"
              type="danger"
              link
              size="small"
              @click="handleRejectRefund(row)"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页区域 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageIndex"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.refund-container {
  padding: $spacing-medium;
  
  .refund-card {
    background-color: $color-white;
    border-radius: $border-radius;
    box-shadow: $box-shadow;
    padding: $spacing-medium;
    
    .search-section {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: $spacing-medium;
      
      .right-btn-group {
        display: flex;
        gap: $spacing-small;
      }
    }
    
    .pagination-container {
      display: flex;
      justify-content: flex-end;
      margin-top: $spacing-medium;
    }
  }
}
</style> 