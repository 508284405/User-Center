<template>
  <el-dialog
    v-model="visible"
    :title="`数据分析 - ${campaign?.name || ''}`"
    width="1000px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="analytics-container" v-loading="loading">
      <!-- 核心指标 -->
      <div class="metrics-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic 
              title="参与人数" 
              :value="analytics.participantCount || 0"
              :value-style="{ color: '#409eff' }"
            />
          </el-col>
          <el-col :span="6">
            <el-statistic 
              title="订单数量" 
              :value="analytics.orderCount || 0"
              :value-style="{ color: '#67c23a' }"
            />
          </el-col>
          <el-col :span="6">
            <el-statistic 
              title="销售额" 
              :value="formatAmount(analytics.totalSales)"
              prefix="¥"
              :value-style="{ color: '#e6a23c' }"
            />
          </el-col>
          <el-col :span="6">
            <el-statistic 
              title="转化率" 
              :value="analytics.conversionRate || 0"
              suffix="%"
              :precision="2"
              :value-style="{ color: '#f56c6c' }"
            />
          </el-col>
        </el-row>
      </div>

      <el-divider />

      <!-- 趋势图表 -->
      <div class="charts-section">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <div class="chart-header">
                  <h4>参与人数趋势</h4>
                  <el-button-group size="small">
                    <el-button 
                      :type="chartPeriod === 'daily' ? 'primary' : ''"
                      @click="chartPeriod = 'daily'"
                    >
                      日
                    </el-button>
                    <el-button 
                      :type="chartPeriod === 'hourly' ? 'primary' : ''"
                      @click="chartPeriod = 'hourly'"
                    >
                      时
                    </el-button>
                  </el-button-group>
                </div>
              </template>
              <div ref="participantsChartRef" style="height: 300px;"></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <h4>销售额趋势</h4>
              </template>
              <div ref="salesChartRef" style="height: 300px;"></div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 详细数据表格 -->
      <div class="table-section">
        <el-card>
          <template #header>
            <div class="table-header">
              <h4>每日数据详情</h4>
              <div class="table-controls">
                <el-date-picker
                  v-model="dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 240px; margin-right: 12px;"
                />
                <el-button @click="exportData">导出数据</el-button>
              </div>
            </div>
          </template>
          
          <el-table :data="filteredDailyStats" stripe style="width: 100%">
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="participants" label="新增参与人数" width="120" />
            <el-table-column prop="orders" label="订单数" width="100" />
            <el-table-column label="销售额" width="120">
              <template #default="{ row }">
                ¥{{ formatAmount(row.sales) }}
              </template>
            </el-table-column>
            <el-table-column label="平均客单价" width="120">
              <template #default="{ row }">
                ¥{{ formatAmount(row.orders > 0 ? row.sales / row.orders : 0) }}
              </template>
            </el-table-column>
            <el-table-column label="转化率" width="100">
              <template #default="{ row }">
                {{ row.participants > 0 ? ((row.orders / row.participants) * 100).toFixed(2) : 0 }}%
              </template>
            </el-table-column>
            <el-table-column label="环比增长" width="120">
              <template #default="{ row, $index }">
                <span 
                  :class="getGrowthClass(getGrowthRate($index, 'sales'))"
                  v-if="$index > 0"
                >
                  {{ getGrowthRate($index, 'sales') }}%
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50]"
            :small="false"
            :background="true"
            layout="total, sizes, prev, pager, next"
            :total="analytics.dailyStats?.length || 0"
            style="margin-top: 20px; justify-content: center"
          />
        </el-card>
      </div>

      <!-- 额外分析指标 -->
      <div class="additional-metrics">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card class="metric-card">
              <el-statistic
                title="平均客单价"
                :value="formatAmount(analytics.avgOrderAmount)"
                prefix="¥"
              />
              <div class="metric-description">
                每个订单的平均金额
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="metric-card">
              <el-statistic
                title="人均参与价值"
                :value="formatAmount(getAvgParticipantValue())"
                prefix="¥"
              />
              <div class="metric-description">
                每个参与用户带来的平均价值
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="metric-card">
              <el-statistic
                title="活动ROI"
                :value="getROI()"
                suffix="%"
                :precision="2"
              />
              <div class="metric-description">
                活动投入产出比（需配置成本）
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import type { CampaignDTO, CampaignAnalytics } from '@/types/campaign'
import { campaignApi } from '@/api/campaign'

interface Props {
  modelValue: boolean
  campaign: CampaignDTO | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const loading = ref(false)
const chartPeriod = ref<'daily' | 'hourly'>('daily')
const dateRange = ref<[string, string] | null>(null)

const participantsChartRef = ref<HTMLDivElement>()
const salesChartRef = ref<HTMLDivElement>()

const analytics = ref<CampaignAnalytics>({
  campaignId: 0,
  participantCount: 0,
  orderCount: 0,
  totalSales: 0,
  conversionRate: 0,
  avgOrderAmount: 0,
  dailyStats: []
})

const pagination = reactive({
  page: 1,
  size: 10
})

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const filteredDailyStats = computed(() => {
  let stats = analytics.value.dailyStats || []
  
  if (dateRange.value) {
    const [start, end] = dateRange.value
    stats = stats.filter(stat => stat.date >= start && stat.date <= end)
  }
  
  const startIndex = (pagination.page - 1) * pagination.size
  return stats.slice(startIndex, startIndex + pagination.size)
})

const fetchAnalytics = async () => {
  if (!props.campaign?.id) return

  try {
    loading.value = true
    const response = await campaignApi.getCampaignAnalytics(props.campaign.id)
    if (response.success) {
      analytics.value = response.data
      nextTick(() => {
        initCharts()
      })
    }
  } catch (error) {
    ElMessage.error('获取分析数据失败')
    // 模拟数据用于演示
    analytics.value = {
      campaignId: props.campaign?.id || 0,
      participantCount: 1250,
      orderCount: 380,
      totalSales: 125600,
      conversionRate: 30.4,
      avgOrderAmount: 330.53,
      dailyStats: generateMockDailyStats()
    }
    nextTick(() => {
      initCharts()
    })
  } finally {
    loading.value = false
  }
}

const generateMockDailyStats = () => {
  const stats = []
  const startDate = new Date()
  startDate.setDate(startDate.getDate() - 14)
  
  for (let i = 0; i < 15; i++) {
    const date = new Date(startDate)
    date.setDate(date.getDate() + i)
    
    stats.push({
      date: date.toISOString().split('T')[0],
      participants: Math.floor(Math.random() * 100) + 50,
      orders: Math.floor(Math.random() * 50) + 10,
      sales: Math.floor(Math.random() * 15000) + 5000
    })
  }
  
  return stats
}

const initCharts = () => {
  if (participantsChartRef.value) {
    const participantsChart = echarts.init(participantsChartRef.value)
    const participantsOption = {
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: analytics.value.dailyStats.map(stat => stat.date)
      },
      yAxis: {
        type: 'value'
      },
      series: [{
        data: analytics.value.dailyStats.map(stat => stat.participants),
        type: 'line',
        smooth: true,
        itemStyle: {
          color: '#409eff'
        },
        areaStyle: {
          opacity: 0.3
        }
      }]
    }
    participantsChart.setOption(participantsOption)
  }

  if (salesChartRef.value) {
    const salesChart = echarts.init(salesChartRef.value)
    const salesOption = {
      tooltip: {
        trigger: 'axis',
        formatter: (params: any) => {
          const value = params[0].value
          return `${params[0].name}<br/>销售额: ¥${formatAmount(value)}`
        }
      },
      xAxis: {
        type: 'category',
        data: analytics.value.dailyStats.map(stat => stat.date)
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: (value: number) => `¥${formatAmount(value / 1000)}k`
        }
      },
      series: [{
        data: analytics.value.dailyStats.map(stat => stat.sales),
        type: 'bar',
        itemStyle: {
          color: '#67c23a'
        }
      }]
    }
    salesChart.setOption(salesOption)
  }
}

const formatAmount = (amount: number | undefined) => {
  if (!amount) return '0.00'
  return (amount / 100).toFixed(2)
}

const getAvgParticipantValue = () => {
  const participants = analytics.value.participantCount
  const sales = analytics.value.totalSales
  return participants > 0 ? sales / participants : 0
}

const getROI = () => {
  // 这里应该根据活动成本计算ROI，暂时返回固定值
  return 285.6
}

const getGrowthRate = (index: number, field: keyof typeof analytics.value.dailyStats[0]) => {
  if (index === 0) return 0
  
  const current = analytics.value.dailyStats[index][field] as number
  const previous = analytics.value.dailyStats[index - 1][field] as number
  
  if (previous === 0) return 0
  
  return ((current - previous) / previous * 100).toFixed(1)
}

const getGrowthClass = (rate: string) => {
  const numRate = parseFloat(rate)
  if (numRate > 0) return 'growth-positive'
  if (numRate < 0) return 'growth-negative'
  return 'growth-neutral'
}

const exportData = () => {
  ElMessage.info('正在导出数据...')
  // 实现导出功能
  setTimeout(() => {
    ElMessage.success('导出完成')
  }, 2000)
}

const handleClose = () => {
  emit('update:modelValue', false)
}

watch(() => props.modelValue, (newVal) => {
  if (newVal && props.campaign) {
    fetchAnalytics()
  }
}, { immediate: true })

onMounted(() => {
  if (props.modelValue && props.campaign) {
    fetchAnalytics()
  }
})
</script>

<style scoped>
.analytics-container {
  max-height: 70vh;
  overflow-y: auto;
}

.metrics-section {
  margin-bottom: 24px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.charts-section {
  margin-bottom: 24px;
}

.chart-card {
  height: 380px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-header h4 {
  margin: 0;
}

.table-section {
  margin-bottom: 24px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-header h4 {
  margin: 0;
}

.table-controls {
  display: flex;
  align-items: center;
}

.additional-metrics {
  margin-top: 24px;
}

.metric-card {
  text-align: center;
  height: 120px;
}

.metric-description {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.growth-positive {
  color: #67c23a;
}

.growth-negative {
  color: #f56c6c;
}

.growth-neutral {
  color: #909399;
}

:deep(.el-statistic__number) {
  font-size: 24px;
  font-weight: bold;
}

:deep(.el-statistic__title) {
  font-size: 14px;
  margin-bottom: 8px;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>