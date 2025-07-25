<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getLogisticsPackage, getLogisticsOrder } from '@/api/client-web/logistics'
import type { LogisticsPackage, LogisticsOrder } from '@/types/logistics'

interface ApiResponse<T> {
  success: boolean;
  errCode?: string;
  errMessage?: string;
  data: T;
}

const route = useRoute()
const router = useRouter()
const packageId = Number(route.params.id)

// 包裹信息
const packageInfo = ref<LogisticsPackage | null>(null)
// 关联的订单信息
const orderInfo = ref<LogisticsOrder | null>(null)
// 加载状态
const loading = ref(false)

// 获取包裹详情
const fetchPackageDetail = async () => {
  if (!packageId) {
    ElMessage.error('包裹ID无效')
    return
  }
  
  try {
    loading.value = true
    const response = await getLogisticsPackage(packageId) as ApiResponse<LogisticsPackage>
    
    if (response.success) {
      packageInfo.value = response.data
      // 获取关联的订单信息
      if (packageInfo.value?.logisticsOrderId) {
        fetchOrderInfo(packageInfo.value.logisticsOrderId)
      }
    } else {
      ElMessage.error(response.errMessage || '获取包裹详情失败')
    }
  } catch (error) {
    console.error('获取包裹详情失败:', error)
    ElMessage.error('获取包裹详情失败')
  } finally {
    loading.value = false
  }
}

// 获取订单信息
const fetchOrderInfo = async (orderId: number) => {
  try {
    const response = await getLogisticsOrder(orderId) as ApiResponse<LogisticsOrder>
    if (response.success) {
      orderInfo.value = response.data
    }
  } catch (error) {
    console.error('获取订单信息失败:', error)
  }
}

// 格式化时间
const formatTime = (timestamp: number): string => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  return date.toLocaleString()
}

// 返回列表页
const goBack = () => {
  router.go(-1)
}

// 组件挂载时获取数据
onMounted(() => {
  fetchPackageDetail()
})
</script>

<template>
  <div class="package-detail-container">
    <div class="page-header">
      <el-button type="primary" plain @click="goBack">
        返回列表
      </el-button>
      <h2 class="page-title">包裹详情</h2>
    </div>
    
    <el-card v-loading="loading">
      <!-- 订单信息 -->
      <template v-if="orderInfo">
        <div class="section-title">订单信息</div>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="订单ID">{{ orderInfo.id }}</el-descriptions-item>
          <el-descriptions-item label="订单号">{{ orderInfo.orderNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ orderInfo.statusText }}</el-descriptions-item>
          <el-descriptions-item label="服务类型">{{ orderInfo.serviceType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="渠道来源">{{ orderInfo.sourceChannel || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(orderInfo.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="发件人" :span="2">{{ orderInfo.senderName }}</el-descriptions-item>
          <el-descriptions-item label="发件人电话">{{ orderInfo.senderPhone }}</el-descriptions-item>
          <el-descriptions-item label="发件地址" :span="3">{{ orderInfo.senderAddress }}</el-descriptions-item>
          <el-descriptions-item label="收件人" :span="2">{{ orderInfo.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="收件人电话">{{ orderInfo.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收件地址" :span="3">{{ orderInfo.receiverAddress }}</el-descriptions-item>
        </el-descriptions>
      </template>
      
      <!-- 包裹详情 -->
      <template v-if="packageInfo">
        <div class="section-title">包裹信息</div>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="包裹ID">{{ packageInfo.id }}</el-descriptions-item>
          <el-descriptions-item label="物流订单ID">{{ packageInfo.logisticsOrderId }}</el-descriptions-item>
          <el-descriptions-item label="运单号">{{ packageInfo.waybillNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="商品名称">{{ packageInfo.productName }}</el-descriptions-item>
          <el-descriptions-item label="SKU ID">{{ packageInfo.skuId }}</el-descriptions-item>
          <el-descriptions-item label="数量">{{ packageInfo.qty }}</el-descriptions-item>
          <el-descriptions-item label="重量">{{ packageInfo.weight }} 克</el-descriptions-item>
          <el-descriptions-item label="尺寸">
            {{ packageInfo.length }} × {{ packageInfo.width }} × {{ packageInfo.height }} mm
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(packageInfo.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间" :span="3">{{ formatTime(packageInfo.updateTime) }}</el-descriptions-item>
        </el-descriptions>
      </template>
      
      <!-- 空数据展示 -->
      <div v-if="!loading && !packageInfo" class="empty-data">
        <el-empty description="未找到包裹信息" />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.package-detail-container {
  padding: 16px 0;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  margin: 0 0 0 16px;
  font-size: 20px;
  font-weight: 500;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  margin: 16px 0;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}

.section-title:first-child {
  margin-top: 0;
}

.empty-data {
  padding: 40px 0;
}
</style> 