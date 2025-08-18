<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { afterSalesApi, type AfterSalesItem, type AfterSalesQuery } from '@/api/client-web/afterSales'
import { AfterSaleStatus } from '@/types/afterSaleStatus'
import { AfterSaleType } from '@/types/afterSaleType'

const router = useRouter()

// 表格数据
const tableData = ref<AfterSalesItem[]>([])
const loading = ref(false)
const total = ref(0)

// 日期范围
const dateRange = ref<[string, string]>(['', ''])

// 查询参数
const queryParams = reactive<AfterSalesQuery>({
  keyword: '',
  status: '',
  afterSaleType: undefined,
  startTime: '',
  endTime: '',
  pageNum: 1,
  pageSize: 10
})

// 状态选项
const statusOptions = [
  { value: '', label: '全部状态' },
  { value: AfterSaleStatus.APPLYING, label: '待审核' },
  { value: AfterSaleStatus.APPROVED, label: '审核通过' },
  { value: AfterSaleStatus.REJECTED, label: '审核拒绝' },
  { value: AfterSaleStatus.CANCELED, label: '已撤销' },
  { value: AfterSaleStatus.WAIT_BUYER_RETURN_GOODS, label: '等待用户退货' },
  { value: AfterSaleStatus.WAIT_SELLER_CONFIRM, label: '商家待收货' },
  { value: AfterSaleStatus.WAIT_USER_RETURN_GOODS, label: '换货待退货' },
  { value: AfterSaleStatus.WAIT_SELLER_RESEND, label: '商家待发货' },
  { value: AfterSaleStatus.REFUNDING, label: '退款中' },
  { value: AfterSaleStatus.RESENT, label: '已发货' },
  { value: AfterSaleStatus.COMPLETED, label: '已完成' }
]

// 售后类型选项
const typeOptions = [
  { value: '', label: '全部类型' },
  { value: AfterSaleType.ONLY_REFUND, label: '仅退款' },
  { value: AfterSaleType.RETURN_REFUND, label: '退货退款' },
  { value: AfterSaleType.EXCHANGE, label: '换货' }
]

// 售后类型格式化
const formatType = (type: AfterSaleType) => {
  return {
    [AfterSaleType.ONLY_REFUND]: '仅退款',
    [AfterSaleType.RETURN_REFUND]: '退货退款',
    [AfterSaleType.EXCHANGE]: '换货'
  }[type] || '未知';
}

// 监听日期变化
const handleDateChange = (val: [string, string]) => {
  queryParams.startTime = val[0] || ''
  queryParams.endTime = val[1] || ''
}

// 获取售后列表数据
const getList = async () => {
  loading.value = true
  try {
    const res: any = await afterSalesApi.getAfterSalesList(queryParams)
    // 取出实际数据字段
    tableData.value = res.data || []
    total.value = res.totalCount || 0
  } catch (error) {
    console.error('获取售后列表失败:', error)
    ElMessage.error('获取售后列表失败')
  } finally {
    loading.value = false
  }
}

// 重置查询条件
const resetQuery = () => {
  queryParams.keyword = ''
  queryParams.status = ''
  queryParams.afterSaleType = undefined
  queryParams.startTime = ''
  queryParams.endTime = ''
  dateRange.value = ['', '']
  queryParams.pageNum = 1
  getList()
}

// 处理查询
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 处理分页变化
const handleSizeChange = (val: number) => {
  queryParams.pageSize = val
  getList()
}

const handleCurrentChange = (val: number) => {
  queryParams.pageNum = val
  getList()
}

// 查看详情
const handleViewDetail = (row: AfterSalesItem) => {
  router.push(`/after-sales/${row.afterSaleNo}`)
}

// 分配客服
const handleAssignService = async (row: AfterSalesItem) => {
  try {
    // 这里可以打开一个选择客服的弹窗，这里简化为直接分配
    await afterSalesApi.assignCustomerService(row.id, 'service001')
    ElMessage.success('分配客服成功')
    getList()
  } catch (error) {
    console.error('分配客服失败:', error)
    ElMessage.error('分配客服失败')
  }
}

// 导出售后数据
const handleExport = async () => {
  try {
    loading.value = true
    const res = await afterSalesApi.exportAfterSales(queryParams)
    // 处理blob数据
    const blob = new Blob([res.data], { type: 'application/vnd.ms-excel' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `售后数据_${new Date().getTime()}.xlsx`
    link.click()
    URL.revokeObjectURL(link.href)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出售后数据失败:', error)
    ElMessage.error('导出售后数据失败')
  } finally {
    loading.value = false
  }
}

// 格式化状态
const formatStatus = (status: string) => {
  const statusMap: Record<AfterSaleStatus, { label: string; type: string }> = {
    [AfterSaleStatus.APPLYING]: { label: '待审核', type: 'info' },
    [AfterSaleStatus.APPROVED]: { label: '审核通过', type: 'success' },
    [AfterSaleStatus.REJECTED]: { label: '审核拒绝', type: 'danger' },
    [AfterSaleStatus.CANCELED]: { label: '已撤销', type: 'info' },
    [AfterSaleStatus.WAIT_BUYER_RETURN_GOODS]: { label: '等待用户退货', type: 'warning' },
    [AfterSaleStatus.WAIT_SELLER_CONFIRM]: { label: '商家待收货', type: 'warning' },
    [AfterSaleStatus.WAIT_USER_RETURN_GOODS]: { label: '换货待退货', type: 'warning' },
    [AfterSaleStatus.WAIT_SELLER_RESEND]: { label: '商家待发货', type: 'warning' },
    [AfterSaleStatus.REFUNDING]: { label: '退款中', type: 'primary' },
    [AfterSaleStatus.RESENT]: { label: '已发货', type: 'success' },
    [AfterSaleStatus.COMPLETED]: { label: '已完成', type: 'success' }
  }
  return statusMap[status as AfterSaleStatus] || { label: '未知', type: 'info' }
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
  <div class="after-sales-container">
    <div class="after-sales-card">
      <!-- 搜索区域 -->
      <div class="search-section">
        <el-form :model="queryParams" inline>
          <el-form-item label="类型">
            <el-select
              v-model="queryParams.afterSaleType"
              placeholder="请选择类型"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="item in typeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="queryParams.status"
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
          
          <el-form-item label="关键词">
            <el-input
              v-model="queryParams.keyword"
              placeholder="订单号/用户名/手机号"
              clearable
              style="width: 200px"
              @keyup.enter="handleQuery"
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
        <el-table-column prop="afterSaleNo" label="售后编号" width="180" />
        <el-table-column prop="orderNumber" label="关联订单" width="180" />
        <el-table-column prop="userId" label="用户名" width="120" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="afterSaleType" label="售后类型" width="100">
          <template #default="{ row }">
            {{ formatType(row.afterSaleType) }}
          </template>
        </el-table-column>
        <el-table-column prop="refundAmount" label="申请金额" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="formatStatus(row.status).type">
              {{ formatStatus(row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.applyTime) }}
          </template>
        </el-table-column>
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
              v-if="row.status === AfterSaleStatus.APPLYING"
              type="success"
              link
              size="small"
              @click="handleAssignService(row)"
            >
              分配客服
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页区域 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
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
@use '@/styles/variables.scss' as *;

.after-sales-container {
  padding: $spacing-medium;
  
  .after-sales-card {
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
