<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getLogisticsPackageList, getLogisticsOrder } from '@/api/client-web/logistics'
import FilterForm from '@/components/logistics/FilterForm.vue'
import DataTable from '@/components/logistics/DataTable.vue'
import type { FilterField, TableColumn, LogisticsPackage, LogisticsOrder } from '@/types/logistics'

const router = useRouter()
const route = useRoute()

// 查询参数 - 如果URL中有orderId参数，则使用它
const orderId = route.query.orderId ? Number(route.query.orderId) : undefined

// 数据列表
const packageList = ref<LogisticsPackage[]>([])

// 加载状态
const loading = ref(false)

// 订单信息
const orderInfo = ref<LogisticsOrder | null>(null)

// 表格列定义
const columns: TableColumn[] = [
  { prop: 'id', label: '包裹ID', width: 80 },
  { prop: 'logisticsOrderId', label: '物流订单ID', width: 100 },
  { prop: 'waybillNo', label: '运单号', width: 150 },
  { prop: 'productName', label: '商品名称' },
  { prop: 'skuId', label: 'SKU ID', width: 120 },
  { prop: 'qty', label: '数量', width: 80 },
  { prop: 'weight', label: '重量(克)', width: 100 },
  { prop: 'length', label: '长度(mm)', width: 100 },
  { prop: 'width', label: '宽度(mm)', width: 100 },
  { prop: 'height', label: '高度(mm)', width: 100 },
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
const filterFields: FilterField[] = [
  { name: 'orderId', label: '物流订单ID', type: 'input' }
]

// 格式化时间
const formatTime = (timestamp: number): string => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  return date.toLocaleString()
}

// 获取包裹列表
const fetchPackages = async (logisticsOrderId?: number) => {
  if (!logisticsOrderId) {
    ElMessage.warning('请输入物流订单ID')
    return
  }
  
  try {
    loading.value = true
    const response = await getLogisticsPackageList({ logisticsOrderId })
    packageList.value = response.data || []
    // 如果包裹列表不为空，尝试获取对应的订单信息
    if (packageList.value.length > 0) {
      fetchOrderInfo(logisticsOrderId)
    } else {
      orderInfo.value = null
      ElMessage.warning('该订单下没有包裹信息')
    }
  } catch (error) {
    console.error('获取包裹列表失败:', error)
    ElMessage.error('获取包裹列表失败')
  } finally {
    loading.value = false
  }
}

// 获取订单信息
const fetchOrderInfo = async (logisticsOrderId: number) => {
  try {
    const response = await getLogisticsOrder(logisticsOrderId)
    orderInfo.value = response.data
  } catch (error) {
    console.error('获取订单信息失败:', error)
  }
}

// 处理查询
const handleSearch = (formData: Record<string, any>) => {
  const searchOrderId = Number(formData.orderId)
  if (searchOrderId) {
    fetchPackages(searchOrderId)
  } else {
    ElMessage.warning('请输入物流订单ID')
  }
}

// 查看包裹详情
const handleViewDetail = (row: LogisticsPackage) => {
  router.push(`/dashboard/logistics/packages/${row.id}`)
}

// 初始化时，如果有orderId参数则自动查询
onMounted(() => {
  if (orderId) {
    fetchPackages(orderId)
  }
})
</script>

<template>
  <div class="packages-container">
    <h2 class="page-title">包裹管理</h2>
    
    <!-- 筛选表单 -->
    <FilterForm
      :fields="filterFields"
      :initial-values="{ orderId }"
      @search="handleSearch"
    />
    
    <!-- 订单信息卡片 -->
    <div v-if="orderInfo" class="order-info-card">
      <h3>订单信息</h3>
      <div class="order-info-content">
        <p><strong>订单ID:</strong> {{ orderInfo.id }}</p>
        <p><strong>订单号:</strong> {{ orderInfo.orderNo || '-' }}</p>
        <p><strong>状态:</strong> {{ orderInfo.statusText }}</p>
        <p><strong>服务类型:</strong> {{ orderInfo.serviceType || '-' }}</p>
        <p><strong>发件人:</strong> {{ orderInfo.senderName }}</p>
        <p><strong>收件人:</strong> {{ orderInfo.receiverName }}</p>
      </div>
    </div>
    
    <!-- 数据表格 -->
    <DataTable
      :data="packageList"
      :columns="columns"
      :loading="loading"
      :row-key="'id'"
      :border="true"
      :stripe="true"
      @row-click="handleViewDetail"
    >
      <template #operations>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="text" size="small" @click.stop="handleViewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </template>
    </DataTable>
    
    <!-- 空数据提示 -->
    <div v-if="!loading && packageList.length === 0" class="empty-data">
      <p>暂无包裹数据，请输入物流订单ID进行查询</p>
    </div>
  </div>
</template>

<style scoped>
.packages-container {
  padding: 16px 0;
}

.page-title {
  margin-top: 0;
  margin-bottom: 24px;
  font-size: 20px;
  font-weight: 500;
}

.order-info-card {
  background-color: #f0f9eb;
  border-radius: 4px;
  padding: 16px;
  margin-bottom: 20px;
  border: 1px solid #e1f3d8;
}

.order-info-card h3 {
  margin-top: 0;
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 500;
  color: #67c23a;
}

.order-info-content {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.order-info-content p {
  margin: 0;
  flex: 1;
  min-width: 200px;
}

.empty-data {
  text-align: center;
  padding: 40px 0;
  color: #909399;
  background-color: #fff;
  border-radius: 4px;
  margin-top: 20px;
}
</style> 