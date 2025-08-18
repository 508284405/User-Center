<script setup lang="ts">
import { ref, reactive, onMounted, computed, defineEmits } from 'vue'
import { ElMessage } from 'element-plus'
import { logisticsApi, SlaParams } from '@/api/client-web/logistics'
import ChartCard from '@/components/logistics/ChartCard.vue'
import DataTable from '@/components/logistics/DataTable.vue'

// 定义事件
const emit = defineEmits(['loading-change'])

// 查询参数
const queryParams = reactive<SlaParams>({
  bizType: 'PENDING_WAYBILL' // 默认业务类型
})

// SLA数据
const slaData = ref<any>(null)

// 加载状态
const loading = ref(false)

// 业务类型选项
const bizTypeOptions = [
  { label: '待出运单', value: 'PENDING_WAYBILL' },
  { label: '待揽收', value: 'PENDING_PICKUP' },
  { label: '待签收', value: 'PENDING_SIGN' }
]

// 物流商SLA表格列
const providerSlaColumns = [
  { prop: 'providerCode', label: '物流商代码', width: 120 },
  { prop: 'providerName', label: '物流商名称', width: 150 },
  { 
    prop: 'slaComplianceRate', 
    label: 'SLA达成率', 
    width: 120,
    formatter: (row: any) => `${(row.slaComplianceRate * 100).toFixed(2)}%`
  },
  { 
    prop: 'avgDeliveryTime', 
    label: '平均配送时长(小时)', 
    width: 160,
    formatter: (row: any) => row.avgDeliveryTime.toFixed(2)
  },
  { prop: 'orderCount', label: '订单数量', width: 120 }
]

// 获取SLA数据
const fetchSlaData = async () => {
  try {
    emit('loading-change', true)
    loading.value = true
    
    const response = await logisticsApi.getSla(queryParams)
    if (response.success) {
      slaData.value = response.data
    } else {
      ElMessage.error(response.errMessage || '获取SLA数据失败')
    }
  } catch (error) {
    console.error('获取SLA数据失败:', error)
    ElMessage.error('获取SLA数据失败')
  } finally {
    loading.value = false
    emit('loading-change', false)
  }
}

// 切换业务类型
const changeBizType = (type: string) => {
  queryParams.bizType = type
  fetchSlaData()
}

// SLA达成率图表选项
const slaChartOptions = computed(() => {
  if (!slaData.value || !slaData.value.providerSlaList) return {}
  
  const providers = slaData.value.providerSlaList.map((item: any) => item.providerName)
  const rates = slaData.value.providerSlaList.map((item: any) => (item.slaComplianceRate * 100).toFixed(2))
  
  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'category',
      data: providers,
      axisLabel: {
        interval: 0,
        rotate: 30
      }
    },
    yAxis: {
      type: 'value',
      name: 'SLA达成率(%)',
      min: 0,
      max: 100
    },
    series: [
      {
        name: 'SLA达成率',
        type: 'bar',
        data: rates,
        itemStyle: {
          color: function(params: any) {
            const value = params.value
            if (value >= 90) return '#67C23A' // 绿色 - 优
            if (value >= 80) return '#E6A23C' // 黄色 - 良
            return '#F56C6C' // 红色 - 差
          }
        },
        label: {
          show: true,
          position: 'top',
          formatter: '{c}%'
        }
      }
    ]
  }
})

// 区域SLA分布选项
const regionalSlaChartOptions = computed(() => {
  if (!slaData.value || !slaData.value.regionalSla) return {}
  
  const regions = Object.keys(slaData.value.regionalSla)
  const rates = regions.map(region => (slaData.value.regionalSla[region] * 100).toFixed(2))
  
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c}%'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      data: regions
    },
    series: [
      {
        name: '区域SLA达成率',
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
        data: regions.map((region, index) => ({
          name: region,
          value: rates[index]
        }))
      }
    ]
  }
})

// 获取摘要数据
const summaryItems = computed(() => {
  if (!slaData.value) return []
  
  return [
    {
      label: 'SLA达成率',
      value: (slaData.value.slaComplianceRate * 100).toFixed(2),
      unit: '%',
      color: getSlaRateColor(slaData.value.slaComplianceRate * 100)
    },
    {
      label: '平均配送时长',
      value: slaData.value.avgDeliveryTime?.toFixed(2) || 0,
      unit: '小时'
    },
    {
      label: '订单数量',
      value: slaData.value.orderCount || 0
    },
    {
      label: '超时订单数',
      value: slaData.value.timeoutOrderCount || 0,
      color: '#F56C6C'
    }
  ]
})

// 根据SLA达成率获取颜色
const getSlaRateColor = (rate: number): string => {
  if (rate >= 90) return '#67C23A' // 绿色 - 优
  if (rate >= 80) return '#E6A23C' // 黄色 - 良
  return '#F56C6C' // 红色 - 差
}

// 组件挂载时获取数据
onMounted(() => {
  fetchSlaData()
})
</script>

<template>
  <div class="sla-container">
    <!-- 业务类型选择 -->
    <div class="business-type-selector">
      <el-radio-group v-model="queryParams.bizType" @change="changeBizType">
        <el-radio-button 
          v-for="option in bizTypeOptions" 
          :key="option.value" 
          :label="option.value"
        >
          {{ option.label }}
        </el-radio-button>
      </el-radio-group>
      
      <el-button 
        type="primary" 
        @click="fetchSlaData" 
        :loading="loading"
      >
        刷新数据
      </el-button>
    </div>
    
    <!-- 关键指标 -->
    <ChartCard
      title="SLA监控指标"
      :loading="loading"
      :summary="summaryItems"
      :height="120"
    />
    
    <!-- 物流商SLA表格 -->
    <div class="sla-card">
      <h3 class="card-title">物流商SLA达成率</h3>
      <DataTable
        :data="slaData?.providerSlaList || []"
        :columns="providerSlaColumns"
        :loading="loading"
        :border="true"
        :stripe="true"
        :row-key="'providerCode'"
      />
    </div>
    
    <!-- 图表区域 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <ChartCard
          title="物流商SLA达成率"
          :loading="loading"
          :options="slaChartOptions"
        />
      </el-col>
      
      <el-col :span="12">
        <ChartCard
          title="区域SLA分布"
          :loading="loading"
          :options="regionalSlaChartOptions"
        />
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.sla-container {
  padding: 16px 0;
}

.business-type-selector {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sla-card {
  background: #fff;
  border-radius: 4px;
  margin-bottom: 20px;
  padding: 16px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-title {
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 500;
}
</style> 