<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElTabs, ElTabPane, ElDescriptions, ElDescriptionsItem, ElCard, ElButton, ElDivider } from 'element-plus'
import { logisticsApi } from '@/api/client-web/logistics'
import DataTable from '@/components/logistics/DataTable.vue'

const route = useRoute()
const router = useRouter()

// 获取订单ID
const orderId = ref<number>(Number(route.params.id))

// 订单详情
const orderDetail = ref<any>(null)

// 物流轨迹
const trackingList = ref<any[]>([])

// 加载状态
const loading = reactive({
  orderDetail: false,
  tracking: false
})

// 当前活动标签
const activeTab = ref('base')

// 包裹表格列定义
const packageColumns = [
  { prop: 'id', label: '包裹ID', width: 80 },
  { prop: 'waybillNo', label: '运单号', width: 150 },
  { prop: 'productName', label: '商品名称' },
  { prop: 'skuId', label: 'SKU ID', width: 120 },
  { prop: 'qty', label: '数量', width: 80 },
  { prop: 'weight', label: '重量(克)', width: 100 },
  { prop: 'length', label: '长度(mm)', width: 100 },
  { prop: 'width', label: '宽度(mm)', width: 100 },
  { prop: 'height', label: '高度(mm)', width: 100 }
]

// 物流轨迹表格列定义
const trackingColumns = [
  { 
    prop: 'eventTime', 
    label: '事件时间', 
    width: 160,
    formatter: (row: any) => formatTime(row.eventTime)
  },
  { prop: 'eventCode', label: '事件代码', width: 120 },
  { prop: 'location', label: '地点', width: 150 },
  { prop: 'remark', label: '事件描述' }
]

// 获取订单详情
const fetchOrderDetail = async () => {
  try {
    loading.orderDetail = true
    const response = await logisticsApi.getOrderDetail(orderId.value)
    if (response.success) {
      orderDetail.value = response.data
    } else {
      ElMessage.error(response.errMessage || '获取订单详情失败')
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  } finally {
    loading.orderDetail = false
  }
}

// 获取物流轨迹
const fetchTracking = async () => {
  if (!orderDetail.value || !orderDetail.value.waybillNo) {
    ElMessage.warning('该订单暂无运单号，无法查询物流轨迹')
    return
  }
  
  try {
    loading.tracking = true
    const response = await logisticsApi.getOrderTracking(orderDetail.value.waybillNo)
    if (response.success) {
      trackingList.value = response.data || []
    } else {
      ElMessage.error(response.errMessage || '获取物流轨迹失败')
    }
  } catch (error) {
    console.error('获取物流轨迹失败:', error)
    ElMessage.error('获取物流轨迹失败')
  } finally {
    loading.tracking = false
  }
}

// 格式化时间
const formatTime = (timestamp: number): string => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  return date.toLocaleString()
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

// 返回列表页
const goBack = () => {
  router.push('/dashboard/logistics/orders')
}

// 取消订单
const handleCancelOrder = async () => {
  if (!orderDetail.value) return
  
  try {
    loading.orderDetail = true
    const response = await logisticsApi.cancelOrder(orderDetail.value.id)
    if (response.success) {
      ElMessage.success('订单取消成功')
      fetchOrderDetail() // 刷新详情
    } else {
      ElMessage.error(response.errMessage || '取消订单失败')
    }
  } catch (error) {
    console.error('取消订单失败:', error)
    ElMessage.error('取消订单失败')
  } finally {
    loading.orderDetail = false
  }
}

// 切换标签页
const handleTabChange = (tab: string) => {
  if (tab === 'tracking' && trackingList.value.length === 0) {
    // 切换到物流轨迹标签时，如果尚未加载轨迹数据则加载
    fetchTracking()
  }
}

// 组件挂载时获取数据
onMounted(() => {
  if (orderId.value) {
    fetchOrderDetail()
  } else {
    ElMessage.error('订单ID无效')
    router.push('/dashboard/logistics/orders')
  }
})
</script>

<template>
  <div class="order-detail-container" v-loading="loading.orderDetail">
    <!-- 头部操作 -->
    <div class="header-actions">
      <el-button @click="goBack">返回列表</el-button>
      <el-button 
        type="primary" 
        @click="fetchOrderDetail"
      >
        刷新
      </el-button>
      <el-button 
        type="danger" 
        @click="handleCancelOrder"
        :disabled="!orderDetail || orderDetail.status === 'SIGNED' || orderDetail.status === 'CANCELED'"
      >
        取消订单
      </el-button>
    </div>
    
    <h2 class="page-title">物流订单详情</h2>
    
    <template v-if="orderDetail">
      <!-- 订单概览 -->
      <el-card class="order-overview">
        <template #header>
          <div class="order-header">
            <span class="order-number">订单号: {{ orderDetail.id }}</span>
            <el-tag
              :type="orderDetail.status === 'SIGNED' ? 'success' : 
                    (orderDetail.status === 'CANCELED' ? 'info' : 
                    (orderDetail.status === 'EXCEPTION' ? 'danger' : 'primary'))"
            >
              {{ formatStatus(orderDetail.status) }}
            </el-tag>
          </div>
        </template>
        
        <el-descriptions :column="3" border>
          <el-descriptions-item label="运单号">{{ orderDetail.waybillNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="业务单号">{{ orderDetail.refNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="物流商">{{ formatProvider(orderDetail.providerCode) }}</el-descriptions-item>
          
          <el-descriptions-item label="业务类型">{{ formatBizType(orderDetail.bizType) }}</el-descriptions-item>
          <el-descriptions-item label="预计揽收时间">{{ orderDetail.expectedPickupAt || '-' }}</el-descriptions-item>
          <el-descriptions-item label="实际发货时间">{{ orderDetail.deliveredAt || '-' }}</el-descriptions-item>
          
          <el-descriptions-item label="创建时间">{{ formatTime(orderDetail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatTime(orderDetail.updateTime) }}</el-descriptions-item>
          <el-descriptions-item label="物流批次号">{{ orderDetail.batchNo || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
      
      <!-- 详细信息标签页 -->
      <el-tabs v-model="activeTab" class="detail-tabs" @tab-click="tab => handleTabChange(tab.props.name)">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="base">
          <div class="address-info">
            <div class="address-card sender">
              <h3>发件人信息</h3>
              <p><strong>姓名：</strong>{{ orderDetail.senderName }}</p>
              <p><strong>电话：</strong>{{ orderDetail.senderPhone }}</p>
              <p><strong>地址：</strong>{{ orderDetail.senderProvince }} {{ orderDetail.senderCity }} {{ orderDetail.senderDistrict }} {{ orderDetail.senderAddress }}</p>
            </div>
            
            <div class="address-card receiver">
              <h3>收件人信息</h3>
              <p><strong>姓名：</strong>{{ orderDetail.receiverName }}</p>
              <p><strong>电话：</strong>{{ orderDetail.receiverPhone }}</p>
              <p><strong>地址：</strong>{{ orderDetail.receiverProvince }} {{ orderDetail.receiverCity }} {{ orderDetail.receiverDistrict }} {{ orderDetail.receiverAddress }}</p>
            </div>
          </div>
          
          <el-divider content-position="left">包裹信息</el-divider>
          
          <DataTable
            :data="orderDetail.packages || []"
            :columns="packageColumns"
            :row-key="'id'"
            :border="true"
            :stripe="true"
          />
        </el-tab-pane>
        
        <!-- 物流轨迹 -->
        <el-tab-pane label="物流轨迹" name="tracking">
          <div v-loading="loading.tracking">
            <div v-if="!orderDetail.waybillNo" class="empty-tracking">
              <p>该订单暂无运单号，无法查询物流轨迹</p>
            </div>
            <div v-else-if="trackingList.length === 0 && !loading.tracking" class="empty-tracking">
              <p>暂无物流轨迹信息</p>
            </div>
            <div v-else>
              <DataTable
                :data="trackingList"
                :columns="trackingColumns"
                :row-key="'id'"
                :border="true"
                :stripe="true"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </template>
    
    <div v-else-if="!loading.orderDetail" class="empty-detail">
      <p>订单信息不存在或已被删除</p>
    </div>
  </div>
</template>

<style scoped>
.order-detail-container {
  padding: 16px 0;
}

.header-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
  gap: 8px;
}

.page-title {
  margin-top: 0;
  margin-bottom: 24px;
  font-size: 20px;
  font-weight: 500;
}

.order-overview {
  margin-bottom: 20px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-number {
  font-size: 16px;
  font-weight: 500;
}

.address-info {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 20px;
}

.address-card {
  flex: 1;
  min-width: 280px;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.address-card h3 {
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 500;
  color: #409eff;
}

.detail-tabs {
  margin-top: 20px;
}

.empty-tracking, .empty-detail {
  text-align: center;
  padding: 40px 0;
  color: #909399;
}
</style> 