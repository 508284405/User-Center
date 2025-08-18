<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { logisticsApi, OrdersQueryParams } from '@/api/client-web/logistics'
import FilterForm from '@/components/logistics/FilterForm.vue'
import DataTable from '@/components/logistics/DataTable.vue'

const router = useRouter()

// 查询参数
const queryParams = reactive<OrdersQueryParams>({
  pageSize: 10,
  pageIndex: 1,
  needTotalCount: true
})

// 数据列表
const orderList = ref<any[]>([])

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
  { prop: 'id', label: '订单ID', width: 80 },
  { prop: 'refNo', label: '业务单号', width: 120 },
  { prop: 'waybillNo', label: '运单号', width: 150 },
  { 
    prop: 'providerCode', 
    label: '物流商', 
    width: 120,
    formatter: (row: any) => formatProvider(row.providerCode)
  },
  { 
    prop: 'status', 
    label: '状态', 
    width: 120,
    formatter: (row: any) => formatStatus(row.status)
  },
  { 
    prop: 'bizType', 
    label: '业务类型', 
    width: 120,
    formatter: (row: any) => formatBizType(row.bizType)
  },
  { prop: 'senderName', label: '发件人', width: 100 },
  { prop: 'senderPhone', label: '发件人电话', width: 120 },
  { prop: 'receiverName', label: '收件人', width: 100 },
  { prop: 'receiverPhone', label: '收件人电话', width: 120 },
  { 
    prop: 'createTime', 
    label: '创建时间', 
    width: 160,
    formatter: (row: any) => formatTime(row.createTime)
  },
  { 
    prop: 'updateTime', 
    label: '更新时间', 
    width: 160,
    formatter: (row: any) => formatTime(row.updateTime)
  }
]

// 筛选字段定义
const filterFields = [
  { name: 'refNo', label: '业务单号', type: 'input' },
  { name: 'waybillNo', label: '运单号', type: 'input' },
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
    name: 'bizType', 
    label: '业务类型', 
    type: 'select',
    options: [
      { label: '发货', value: 'DELIVERY' },
      { label: '补发', value: 'REDELIVERY' },
      { label: '退货', value: 'RETURN' },
      { label: '换货发出', value: 'EXCHANGE_SEND' },
      { label: '换货回收', value: 'EXCHANGE_BACK' }
    ]
  },
  { name: 'senderName', label: '发件人', type: 'input' },
  { name: 'senderMobile', label: '发件人电话', type: 'input' },
  { name: 'receiverName', label: '收件人', type: 'input' },
  { name: 'receiverMobile', label: '收件人电话', type: 'input' },
  { 
    name: 'dateRange', 
    label: '时间范围', 
    type: 'daterange',
    valueFormat: 'x' // 时间戳格式
  }
]

// 获取订单列表
const fetchOrders = async () => {
  try {
    loading.value = true
    
    // 处理日期范围
    const params = { ...queryParams }
    const dateRange = (params as any).dateRange
    if (dateRange && Array.isArray(dateRange) && dateRange.length === 2) {
      params.startTime = dateRange[0]
      params.endTime = dateRange[1]
      delete (params as any).dateRange
    }
    
    const response = await logisticsApi.getOrders(params)
    if (response.success) {
      orderList.value = response.data || []
      pagination.total = response.totalCount || 0
      pagination.currentPage = queryParams.pageIndex || 1
      pagination.pageSize = queryParams.pageSize || 10
    } else {
      ElMessage.error(response.errMessage || '获取物流订单列表失败')
    }
  } catch (error) {
    console.error('获取物流订单列表失败:', error)
    ElMessage.error('获取物流订单列表失败')
  } finally {
    loading.value = false
  }
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

// 格式化业务类型
const formatBizType = (bizType: string): string => {
  const bizTypeMap: Record<string, string> = {
    'DELIVERY': '发货',
    'REDELIVERY': '补发',
    'RETURN': '退货',
    'EXCHANGE_SEND': '换货发出',
    'EXCHANGE_BACK': '换货回收'
  }
  return bizTypeMap[bizType] || bizType
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
  fetchOrders()
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
  fetchOrders()
}

// 处理页码变化
const handlePageChange = (page: number) => {
  queryParams.pageIndex = page
  fetchOrders()
}

// 处理每页条数变化
const handleSizeChange = (size: number) => {
  queryParams.pageSize = size
  queryParams.pageIndex = 1
  fetchOrders()
}

// 查看订单详情
const handleViewDetail = (row: any) => {
  router.push(`/dashboard/logistics/orders/${row.id}`)
}

// 取消订单
const handleCancelOrder = (row: any) => {
  ElMessageBox.confirm(
    `确定要取消订单 ${row.waybillNo || row.id} 吗？`, 
    '取消订单', 
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      loading.value = true
      const response = await logisticsApi.cancelOrder(row.id)
      if (response.success) {
        ElMessage.success('订单取消成功')
        fetchOrders() // 刷新列表
      } else {
        ElMessage.error(response.errMessage || '取消订单失败')
      }
    } catch (error) {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败')
    } finally {
      loading.value = false
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 查看物流轨迹
const handleViewTracking = (row: any) => {
  if (row.waybillNo) {
    // 在实际应用中，可能会打开一个对话框显示物流轨迹
    // 或者跳转到专门的物流轨迹页面
    console.log('查看物流轨迹:', row.waybillNo)
  } else {
    ElMessage.warning('该订单暂无运单号')
  }
}

// 新建订单
const handleCreateOrder = () => {
  router.push({
    name: 'LogisticsOrderCreate'
  });
}

// 组件挂载时获取数据
onMounted(() => {
  fetchOrders()
})
</script>

<template>
  <div class="orders-container">
    <h2 class="page-title">
      物流订单管理
      <el-button type="primary" style="margin-left: 16px;" @click="handleCreateOrder">新建订单</el-button>
    </h2>
    
    <!-- 筛选表单 -->
    <FilterForm
      :fields="filterFields"
      :initial-values="queryParams"
      @search="handleSearch"
      @reset="handleReset"
    />
    
    <!-- 数据表格 -->
    <DataTable
      :data="orderList"
      :columns="columns"
      :loading="loading"
      :pagination="pagination"
      :row-key="'id'"
      :border="true"
      :stripe="true"
      @page-change="handlePageChange"
      @size-change="handleSizeChange"
      @row-click="handleViewDetail"
    >
      <template #operations>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="text" size="small" @click.stop="handleViewDetail(row)">详情</el-button>
            <el-button 
              type="text" 
              size="small" 
              @click.stop="handleViewTracking(row)"
              :disabled="!row.waybillNo"
            >
              轨迹
            </el-button>
            <el-button 
              type="text" 
              size="small" 
              @click.stop="handleCancelOrder(row)"
              :disabled="row.status === 'SIGNED' || row.status === 'CANCELED'"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </template>
    </DataTable>
  </div>
</template>

<style scoped>
.orders-container {
  padding: 16px 0;
}

.page-title {
  margin-top: 0;
  margin-bottom: 24px;
  font-size: 20px;
  font-weight: 500;
}
</style> 