<script setup lang="ts">
import { ref, onMounted, computed, defineEmits } from 'vue'
import { ElRow, ElCol, ElCard, ElDatePicker, ElButton } from 'element-plus'
import ChartCard from '@/components/logistics/ChartCard.vue'
import { logisticsApi, StatisticsParams } from '@/api/client-web/logistics'

// 定义事件
const emit = defineEmits(['loading-change'])

// 日期范围
const dateRange = ref<[Date, Date]>([
  new Date(new Date().getTime() - 30 * 24 * 60 * 60 * 1000), // 30天前
  new Date() // 今天
])

// 物流商筛选
const providerCode = ref('')

// 统计数据
const statisticsData = ref<any>(null)

// 加载状态
const loading = ref(false)

// 获取统计数据
const fetchStatistics = async () => {
  if (!dateRange.value) return
  
  try {
    emit('loading-change', true)
    loading.value = true
    
    const [startDate, endDate] = dateRange.value
    
    const params: StatisticsParams = {
      startTime: startDate.getTime(),
      endTime: endDate.getTime()
    }
    
    if (providerCode.value) {
      params.providerCode = providerCode.value
    }
    
    const response = await logisticsApi.getStatistics(params)
    if (response.success) {
      statisticsData.value = response.data
    }
  } catch (error) {
    console.error('获取物流统计数据失败:', error)
  } finally {
    loading.value = false
    emit('loading-change', false)
  }
}

// 订单状态分布图表配置
const statusChartOptions = computed(() => {
  if (!statisticsData.value) return {}
  
  const { ordersByStatus } = statisticsData.value
  const data = Object.entries(ordersByStatus || {}).map(([status, count]) => ({
    name: getStatusName(status),
    value: count
  }))
  
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      data: data.map(item => item.name)
    },
    series: [
      {
        name: '订单状态',
        type: 'pie',
        radius: ['50%', '70%'],
        avoidLabelOverlap: false,
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '14',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: data
      }
    ]
  }
})

// 物流商分布图表配置
const providerChartOptions = computed(() => {
  if (!statisticsData.value) return {}
  
  const { ordersByProvider } = statisticsData.value
  const data = Object.entries(ordersByProvider || {}).map(([code, count]) => ({
    name: getProviderName(code),
    value: count
  }))
  
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      data: data.map(item => item.name)
    },
    series: [
      {
        name: '物流商',
        type: 'pie',
        radius: ['50%', '70%'],
        avoidLabelOverlap: false,
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '14',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: data
      }
    ]
  }
})

// 日趋势图表配置
const trendChartOptions = computed(() => {
  if (!statisticsData.value) return {}
  
  const { dailyTrend } = statisticsData.value
  if (!dailyTrend || !dailyTrend.length) return {}
  
  const dates = dailyTrend.map(item => formatDate(new Date(item.date)))
  const orderCounts = dailyTrend.map(item => item.orderCount)
  const exceptionCounts = dailyTrend.map(item => item.exceptionCount)
  
  return {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['订单数量', '异常订单']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '订单数量',
        type: 'line',
        smooth: true,
        data: orderCounts
      },
      {
        name: '异常订单',
        type: 'line',
        smooth: true,
        data: exceptionCounts
      }
    ]
  }
})

// 格式化日期
const formatDate = (date: Date): string => {
  return `${date.getMonth() + 1}/${date.getDate()}`
}

// 获取状态名称
const getStatusName = (status: string): string => {
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

// 获取物流商名称
const getProviderName = (code: string): string => {
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

// 处理日期变更
const handleDateChange = () => {
  fetchStatistics()
}

// 获取摘要数据
const summaryItems = computed(() => {
  if (!statisticsData.value) return []
  
  return [
    {
      label: '总订单数',
      value: statisticsData.value.totalOrders || 0
    },
    {
      label: '平均配送时长',
      value: statisticsData.value.avgDeliveryTime?.toFixed(1) || 0,
      unit: '小时'
    },
    {
      label: '延迟配送率',
      value: statisticsData.value.delayRate?.toFixed(2) || 0,
      unit: '%',
      trend: 'up',
      color: '#f56c6c'
    }
  ]
})

// 组件挂载时获取数据
onMounted(() => {
  fetchStatistics()
})
</script>

<template>
  <div class="statistics-container">
    <!-- 筛选条件 -->
    <div class="filter-bar">
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        @change="handleDateChange"
      />
      
      <el-button type="primary" @click="fetchStatistics">刷新数据</el-button>
    </div>
    
    <!-- 关键指标 -->
    <ChartCard
      title="订单概览"
      :loading="loading"
      :summary="summaryItems"
      :height="100"
    />
    
    <!-- 图表展示 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <ChartCard
          title="订单状态分布"
          :loading="loading"
          :options="statusChartOptions"
        />
      </el-col>
      
      <el-col :span="12">
        <ChartCard
          title="物流商分布"
          :loading="loading"
          :options="providerChartOptions"
        />
      </el-col>
    </el-row>
    
    <ChartCard
      title="订单量趋势"
      :loading="loading"
      :options="trendChartOptions"
    />
  </div>
</template>

<style scoped>
.statistics-container {
  padding: 16px 0;
}

.filter-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 16px;
  align-items: center;
}
</style> 