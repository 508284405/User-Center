<script setup lang="ts">
import { ref, reactive, onMounted, defineEmits } from 'vue'
import { ElMessage } from 'element-plus'
import { logisticsApi, ExceptionsParams } from '@/api/client-web/logistics'
import FilterForm from '@/components/logistics/FilterForm.vue'
import DataTable from '@/components/logistics/DataTable.vue'

// 定义事件
const emit = defineEmits(['loading-change'])

// 查询参数
const queryParams = reactive<ExceptionsParams>({
  pageSize: 10,
  pageIndex: 1,
  needTotalCount: true
})

// 数据列表
const exceptionList = ref<any[]>([])

// 分页数据
const pagination = reactive({
  total: 0,
  pageSize: 10,
  currentPage: 1
})

// 加载状态
const loading = ref(false)

// 表格列定义
const columns = [
  { prop: 'id', label: '异常ID', width: 80 },
  { prop: 'waybillNo', label: '运单号', width: 150 },
  { prop: 'exceptionType', label: '异常类型', width: 120 },
  { prop: 'exceptionDesc', label: '异常描述' },
  { 
    prop: 'status', 
    label: '异常状态', 
    width: 120,
    formatter: (row: any) => formatStatus(row.status)
  },
  { 
    prop: 'providerCode', 
    label: '物流商', 
    width: 120,
    formatter: (row: any) => formatProvider(row.providerCode)
  },
  { 
    prop: 'occurTime', 
    label: '发生时间', 
    width: 160,
    formatter: (row: any) => formatTime(row.occurTime)
  },
  { 
    prop: 'processTime', 
    label: '处理时间', 
    width: 160,
    formatter: (row: any) => formatTime(row.processTime)
  },
  { prop: 'processResult', label: '处理结果' },
  { prop: 'processor', label: '处理人', width: 100 }
]

// 筛选字段定义
const filterFields = [
  { name: 'waybillNo', label: '运单号', type: 'input' },
  { 
    name: 'exceptionType', 
    label: '异常类型', 
    type: 'select',
    options: [
      { label: '揽收异常', value: 'PICKUP_EXCEPTION' },
      { label: '运输异常', value: 'TRANSIT_EXCEPTION' },
      { label: '派送异常', value: 'DELIVERY_EXCEPTION' },
      { label: '签收异常', value: 'SIGN_EXCEPTION' }
    ]
  },
  { 
    name: 'status', 
    label: '状态', 
    type: 'select',
    options: [
      { label: '初始状态', value: 'INIT' },
      { label: '运单已创建', value: 'WAYBILL_CREATED' },
      { label: '已预约揽收', value: 'PICKUP_SCHEDULED' },
      { label: '已揽收', value: 'PICKED_UP' },
      { label: '运输中', value: 'IN_TRANSIT' },
      { label: '异常件', value: 'EXCEPTION' },
      { label: '派送中', value: 'OUT_FOR_DELIVERY' },
      { label: '已签收', value: 'SIGNED' },
      { label: '已取消', value: 'CANCELED' }
    ]
  },
  { 
    name: 'providerCode', 
    label: '物流商', 
    type: 'select',
    options: [
      { label: '菜鸟裹裹', value: 'CAINIAO' },
      { label: '顺丰快递', value: 'SF' },
      { label: '京东物流', value: 'JD' },
      { label: '圆通快递', value: 'YTO' },
      { label: '中通快递', value: 'ZTO' },
      { label: '申通快递', value: 'STO' },
      { label: '韵达快递', value: 'YD' },
      { label: '百世快递', value: 'BEST' }
    ]
  },
  { 
    name: 'dateRange', 
    label: '时间范围', 
    type: 'daterange',
    valueFormat: 'x' // 时间戳格式
  }
]

// 获取异常数据
const fetchExceptions = async () => {
  try {
    emit('loading-change', true)
    loading.value = true
    
    // 处理日期范围
    const params = { ...queryParams }
    const dateRange = (params as any).dateRange
    if (dateRange && Array.isArray(dateRange) && dateRange.length === 2) {
      params.startTime = dateRange[0]
      params.endTime = dateRange[1]
      delete (params as any).dateRange
    }
    
    const response = await logisticsApi.getExceptions(params)
    if (response.success) {
      exceptionList.value = response.data || []
      pagination.total = response.totalCount || 0
      pagination.currentPage = queryParams.pageIndex || 1
      pagination.pageSize = queryParams.pageSize || 10
    } else {
      ElMessage.error(response.errMessage || '获取异常数据失败')
    }
  } catch (error) {
    console.error('获取异常数据失败:', error)
    ElMessage.error('获取异常数据失败')
  } finally {
    loading.value = false
    emit('loading-change', false)
  }
}

// 格式化状态
const formatStatus = (status: string): string => {
  const statusMap: Record<string, string> = {
    'INIT': '初始状态',
    'WAYBILL_CREATED': '运单已创建',
    'PICKUP_SCHEDULED': '已预约揽收',
    'PICKED_UP': '已揽收',
    'IN_TRANSIT': '运输中',
    'EXCEPTION': '异常件',
    'OUT_FOR_DELIVERY': '派送中',
    'SIGNED': '已签收',
    'CANCELED': '已取消'
  }
  return statusMap[status] || status
}

// 格式化物流商
const formatProvider = (code: string): string => {
  const providerMap: Record<string, string> = {
    'CAINIAO': '菜鸟裹裹',
    'SF': '顺丰快递',
    'JD': '京东物流',
    'YTO': '圆通快递',
    'ZTO': '中通快递',
    'STO': '申通快递',
    'YD': '韵达快递',
    'BEST': '百世快递'
  }
  return providerMap[code] || code
}

// 格式化时间
const formatTime = (timestamp: number): string => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  return date.toLocaleString()
}

// 处理查询
const handleSearch = (formData: Record<string, any>) => {
  // 合并查询参数
  Object.assign(queryParams, formData)
  queryParams.pageIndex = 1 // 重置到第一页
  fetchExceptions()
}

// 处理重置
const handleReset = () => {
  // 保留分页信息，重置其他查询条件
  const { pageSize } = queryParams
  Object.keys(queryParams).forEach(key => {
    if (key !== 'pageSize' && key !== 'pageIndex' && key !== 'needTotalCount') {
      delete (queryParams as any)[key]
    }
  })
  queryParams.pageIndex = 1
  queryParams.pageSize = pageSize
  fetchExceptions()
}

// 处理页码变化
const handlePageChange = (page: number) => {
  queryParams.pageIndex = page
  fetchExceptions()
}

// 处理每页条数变化
const handleSizeChange = (size: number) => {
  queryParams.pageSize = size
  queryParams.pageIndex = 1
  fetchExceptions()
}

// 处理行点击
const handleRowClick = (row: any) => {
  console.log('查看异常详情:', row)
  // 在实际应用中可能会跳转到详情页或者打开详情对话框
}

// 组件挂载时获取数据
onMounted(() => {
  fetchExceptions()
})
</script>

<template>
  <div class="exceptions-container">
    <!-- 筛选表单 -->
    <FilterForm
      :fields="filterFields"
      :initial-values="queryParams"
      @search="handleSearch"
      @reset="handleReset"
    />
    
    <!-- 数据表格 -->
    <DataTable
      :data="exceptionList"
      :columns="columns"
      :loading="loading"
      :pagination="pagination"
      :row-key="'id'"
      :border="true"
      :stripe="true"
      @page-change="handlePageChange"
      @size-change="handleSizeChange"
      @row-click="handleRowClick"
    >
      <template #operations>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="text" size="small" @click.stop="handleRowClick(row)">查看</el-button>
          </template>
        </el-table-column>
      </template>
    </DataTable>
  </div>
</template>

<style scoped>
.exceptions-container {
  padding: 16px 0;
}
</style> 