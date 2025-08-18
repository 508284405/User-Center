<template>
  <div class="statistics-view">
    <div class="header-section">
      <h2>审核统计报表</h2>
      <div class="header-actions">
        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          format="YYYY-MM-DD HH:mm"
          value-format="x"
          style="width: 350px"
          @change="handleDateRangeChange"
        />
        <el-button type="primary" @click="refreshData" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
        <el-button @click="exportReport">
          <el-icon><Download /></el-icon>
          导出报告
        </el-button>
      </div>
    </div>

    <!-- 核心指标卡片 -->
    <el-row :gutter="20" class="metrics-section">
      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-value">{{ formatNumber(statistics.totalRecords) }}</div>
              <div class="metric-label">总审核记录</div>
              <div class="metric-trend">
                <span :class="getTrendClass(statistics.todayRecords, 'positive')">
                  今日: {{ statistics.todayRecords }}
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-icon success">
              <el-icon><Check /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-value">{{ formatNumber(statistics.approvedCount) }}</div>
              <div class="metric-label">通过记录</div>
              <div class="metric-trend">
                <span class="rate">
                  通过率: {{ getApprovalRate() }}%
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-icon danger">
              <el-icon><Close /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-value">{{ formatNumber(statistics.rejectedCount) }}</div>
              <div class="metric-label">拒绝记录</div>
              <div class="metric-trend">
                <span class="rate">
                  拒绝率: {{ getRejectionRate() }}%
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-icon warning">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-value">{{ statistics.averageProcessingTime }}ms</div>
              <div class="metric-label">平均处理时间</div>
              <div class="metric-trend">
                <span class="rate">
                  待审核: {{ statistics.pendingCount }}
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-section">
      <!-- 违规趋势图 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>违规趋势分析</span>
              <el-radio-group v-model="trendPeriod" size="small" @change="loadViolationTrends">
                <el-radio-button label="7d">7天</el-radio-button>
                <el-radio-button label="30d">30天</el-radio-button>
                <el-radio-button label="90d">90天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <!-- 风险等级分布 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>风险等级分布</span>
          </template>
          <div ref="riskChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-section">
      <!-- 违规分类排行 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>热门违规分类 TOP10</span>
          </template>
          <div ref="categoryChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <!-- 处理效率分析 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>处理效率分析</span>
          </template>
          <div ref="efficiencyChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细数据表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span>详细统计数据</span>
          <div class="table-actions">
            <el-select v-model="tableType" size="small" style="width: 150px" @change="loadTableData">
              <el-option label="违规分类统计" value="category" />
              <el-option label="用户违规统计" value="user" />
              <el-option label="时段分布统计" value="time" />
            </el-select>
          </div>
        </div>
      </template>

      <el-table v-loading="tableLoading" :data="tableData" stripe>
        <!-- 违规分类统计列 -->
        <template v-if="tableType === 'category'">
          <el-table-column prop="categoryName" label="违规分类" />
          <el-table-column prop="violationCount" label="违规次数" sortable />
          <el-table-column prop="percentage" label="占比" sortable>
            <template #default="{ row }">{{ row.percentage }}%</template>
          </el-table-column>
          <el-table-column prop="avgConfidence" label="平均置信度" sortable>
            <template #default="{ row }">{{ (row.avgConfidence * 100).toFixed(1) }}%</template>
          </el-table-column>
          <el-table-column prop="trend" label="趋势">
            <template #default="{ row }">
              <el-tag :type="getTrendTagType(row.trend)" size="small">
                {{ getTrendText(row.trend) }}
              </el-tag>
            </template>
          </el-table-column>
        </template>

        <!-- 用户违规统计列 -->
        <template v-if="tableType === 'user'">
          <el-table-column prop="userId" label="用户ID" />
          <el-table-column prop="violationCount" label="违规次数" sortable />
          <el-table-column prop="riskLevel" label="风险等级" sortable>
            <template #default="{ row }">
              <el-tag :type="getRiskTagType(row.riskLevel)" size="small">
                {{ row.riskLevel }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="lastViolationAt" label="最近违规" sortable>
            <template #default="{ row }">
              {{ formatDateTime(row.lastViolationAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="text" size="small" @click="viewUserDetail(row)">
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </template>

        <!-- 时段分布统计列 -->
        <template v-if="tableType === 'time'">
          <el-table-column prop="timeSlot" label="时段" />
          <el-table-column prop="totalCount" label="总记录数" sortable />
          <el-table-column prop="violationCount" label="违规数" sortable />
          <el-table-column prop="violationRate" label="违规率" sortable>
            <template #default="{ row }">{{ (row.violationRate * 100).toFixed(1) }}%</template>
          </el-table-column>
          <el-table-column prop="avgProcessingTime" label="平均处理时间" sortable>
            <template #default="{ row }">{{ row.avgProcessingTime }}ms</template>
          </el-table-column>
        </template>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="tablePagination.page"
          v-model:page-size="tablePagination.size"
          :total="tablePagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Download, Document, Check, Close, Clock } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import moderationApi, { type ModerationStatistics, type ViolationTrend } from '@/api/smartcs/moderation'
import { formatDateTime } from '@/utils/dateFormat'

// 响应式数据
const loading = ref(false)
const tableLoading = ref(false)
const dateRange = ref<[string, string] | null>(null)
const trendPeriod = ref('7d')
const tableType = ref('category')

// 图表引用
const trendChartRef = ref<HTMLElement>()
const riskChartRef = ref<HTMLElement>()
const categoryChartRef = ref<HTMLElement>()
const efficiencyChartRef = ref<HTMLElement>()

// 图表实例
let trendChart: echarts.ECharts | null = null
let riskChart: echarts.ECharts | null = null
let categoryChart: echarts.ECharts | null = null
let efficiencyChart: echarts.ECharts | null = null

// 统计数据
const statistics = reactive<ModerationStatistics>({
  totalRecords: 0,
  approvedCount: 0,
  rejectedCount: 0,
  pendingCount: 0,
  blockedCount: 0,
  averageProcessingTime: 0,
  todayRecords: 0,
  riskLevelDistribution: {
    lowCount: 0,
    mediumCount: 0,
    highCount: 0,
    criticalCount: 0
  }
})

// 表格数据
const tableData = ref<any[]>([])
const tablePagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 生命周期
onMounted(async () => {
  // 设置默认时间范围（最近7天）
  const endTime = Date.now()
  const startTime = endTime - 7 * 24 * 60 * 60 * 1000
  dateRange.value = [startTime.toString(), endTime.toString()]
  
  await nextTick()
  initCharts()
  refreshData()
})

// 方法定义
const initCharts = () => {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
  }
  if (riskChartRef.value) {
    riskChart = echarts.init(riskChartRef.value)
  }
  if (categoryChartRef.value) {
    categoryChart = echarts.init(categoryChartRef.value)
  }
  if (efficiencyChartRef.value) {
    efficiencyChart = echarts.init(efficiencyChartRef.value)
  }
}

const refreshData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadStatistics(),
      loadViolationTrends(),
      loadTableData()
    ])
  } catch (error) {
    console.error('Failed to load data:', error)
    ElMessage.error('数据加载失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const response = await moderationApi.getStatistics()
    Object.assign(statistics, response.data)
    renderRiskChart()
  } catch (error) {
    console.error('Failed to load statistics:', error)
  }
}

const loadViolationTrends = async () => {
  if (!dateRange.value) return
  
  try {
    const [startTime, endTime] = dateRange.value
    const response = await moderationApi.getViolationTrends(Number(startTime), Number(endTime))
    renderTrendChart(response.data || [])
  } catch (error) {
    console.error('Failed to load violation trends:', error)
  }
}

const loadTableData = async () => {
  tableLoading.value = true
  try {
    // 根据表格类型加载不同的数据
    const mockData = generateMockTableData(tableType.value)
    tableData.value = mockData.data
    tablePagination.total = mockData.total
  } catch (error) {
    console.error('Failed to load table data:', error)
  } finally {
    tableLoading.value = false
  }
}

const generateMockTableData = (type: string) => {
  // 生成模拟数据
  switch (type) {
    case 'category':
      return {
        data: [
          { categoryName: '色情内容', violationCount: 1250, percentage: 35.2, avgConfidence: 0.92, trend: 'up' },
          { categoryName: '暴力血腥', violationCount: 980, percentage: 27.6, avgConfidence: 0.88, trend: 'down' },
          { categoryName: '政治敏感', violationCount: 760, percentage: 21.4, avgConfidence: 0.85, trend: 'stable' },
          { categoryName: '广告垃圾', violationCount: 420, percentage: 11.8, avgConfidence: 0.78, trend: 'up' },
          { categoryName: '其他违规', violationCount: 142, percentage: 4.0, avgConfidence: 0.65, trend: 'down' }
        ],
        total: 5
      }
    case 'user':
      return {
        data: [
          { userId: 'user_001', violationCount: 25, riskLevel: 'HIGH', lastViolationAt: Date.now() - 3600000 },
          { userId: 'user_002', violationCount: 18, riskLevel: 'MEDIUM', lastViolationAt: Date.now() - 7200000 },
          { userId: 'user_003', violationCount: 12, riskLevel: 'MEDIUM', lastViolationAt: Date.now() - 10800000 },
          { userId: 'user_004', violationCount: 8, riskLevel: 'LOW', lastViolationAt: Date.now() - 14400000 }
        ],
        total: 4
      }
    case 'time':
      return {
        data: [
          { timeSlot: '00:00-06:00', totalCount: 1200, violationCount: 180, violationRate: 0.15, avgProcessingTime: 245 },
          { timeSlot: '06:00-12:00', totalCount: 3500, violationCount: 420, violationRate: 0.12, avgProcessingTime: 198 },
          { timeSlot: '12:00-18:00', totalCount: 4200, violationCount: 630, violationRate: 0.15, avgProcessingTime: 212 },
          { timeSlot: '18:00-24:00', totalCount: 3800, violationCount: 456, violationRate: 0.12, avgProcessingTime: 189 }
        ],
        total: 4
      }
    default:
      return { data: [], total: 0 }
  }
}

const renderTrendChart = (trends: ViolationTrend[]) => {
  if (!trendChart) return
  
  const dates = trends.map(t => t.date)
  const counts = trends.map(t => t.violationCount)
  
  const option = {
    title: {
      text: '违规趋势',
      left: 'center',
      textStyle: { fontSize: 16 }
    },
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>违规次数: {c}'
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '违规次数'
    },
    series: [{
      data: counts,
      type: 'line',
      smooth: true,
      itemStyle: {
        color: '#f56565'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(245, 101, 101, 0.3)' },
            { offset: 1, color: 'rgba(245, 101, 101, 0.1)' }
          ]
        }
      }
    }],
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true
    }
  }
  
  trendChart.setOption(option)
}

const renderRiskChart = () => {
  if (!riskChart) return
  
  const { riskLevelDistribution } = statistics
  const data = [
    { value: riskLevelDistribution.lowCount, name: '低风险', itemStyle: { color: '#67c23a' } },
    { value: riskLevelDistribution.mediumCount, name: '中风险', itemStyle: { color: '#e6a23c' } },
    { value: riskLevelDistribution.highCount, name: '高风险', itemStyle: { color: '#f56565' } },
    { value: riskLevelDistribution.criticalCount, name: '极高风险', itemStyle: { color: '#d63031' } }
  ]
  
  const option = {
    title: {
      text: '风险等级分布',
      left: 'center',
      textStyle: { fontSize: 16 }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      bottom: '5%',
      left: 'center'
    },
    series: [{
      name: '风险等级',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: '18',
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data
    }]
  }
  
  riskChart.setOption(option)
}

const handleDateRangeChange = () => {
  if (dateRange.value) {
    loadViolationTrends()
  }
}

const handleSizeChange = (size: number) => {
  tablePagination.size = size
  tablePagination.page = 1
  loadTableData()
}

const handleCurrentChange = (page: number) => {
  tablePagination.page = page
  loadTableData()
}

const viewUserDetail = (row: any) => {
  ElMessage.info(`查看用户 ${row.userId} 的详细信息`)
}

const exportReport = () => {
  ElMessage.info('报告导出功能正在开发中')
}

// 辅助函数
const formatNumber = (num: number) => {
  return num.toLocaleString()
}

const getApprovalRate = () => {
  const total = statistics.totalRecords
  return total > 0 ? ((statistics.approvedCount / total) * 100).toFixed(1) : '0.0'
}

const getRejectionRate = () => {
  const total = statistics.totalRecords
  return total > 0 ? ((statistics.rejectedCount / total) * 100).toFixed(1) : '0.0'
}

const getTrendClass = (value: number, type: 'positive' | 'negative') => {
  return value > 0 ? (type === 'positive' ? 'trend-up' : 'trend-down') : ''
}

const getTrendTagType = (trend: string) => {
  const types: Record<string, string> = {
    up: 'danger',
    down: 'success',
    stable: 'info'
  }
  return types[trend] || 'info'
}

const getTrendText = (trend: string) => {
  const texts: Record<string, string> = {
    up: '上升',
    down: '下降',
    stable: '稳定'
  }
  return texts[trend] || trend
}

const getRiskTagType = (risk: string) => {
  const types: Record<string, string> = {
    LOW: 'success',
    MEDIUM: 'warning',
    HIGH: 'danger',
    CRITICAL: 'danger'
  }
  return types[risk] || 'info'
}
</script>

<style scoped lang="scss">
.statistics-view {
  padding: 20px;

  .header-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
      color: #303133;
    }

    .header-actions {
      display: flex;
      gap: 12px;
      align-items: center;
    }
  }

  .metrics-section {
    margin-bottom: 20px;

    .metric-card {
      .metric-content {
        display: flex;
        align-items: center;
        padding: 10px 0;

        .metric-icon {
          width: 60px;
          height: 60px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24px;
          color: white;
          background: linear-gradient(135deg, #409eff, #79bbff);
          margin-right: 16px;

          &.success {
            background: linear-gradient(135deg, #67c23a, #95d475);
          }

          &.danger {
            background: linear-gradient(135deg, #f56565, #f78989);
          }

          &.warning {
            background: linear-gradient(135deg, #e6a23c, #ebb563);
          }
        }

        .metric-info {
          flex: 1;

          .metric-value {
            font-size: 28px;
            font-weight: bold;
            color: #303133;
            line-height: 1;
            margin-bottom: 8px;
          }

          .metric-label {
            color: #909399;
            font-size: 14px;
            margin-bottom: 4px;
          }

          .metric-trend {
            font-size: 12px;

            .rate {
              color: #606266;
            }

            .trend-up {
              color: #f56565;
            }

            .trend-down {
              color: #67c23a;
            }
          }
        }
      }
    }
  }

  .charts-section {
    margin-bottom: 20px;

    .chart-card {
      .chart-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .chart-container {
        height: 300px;
        width: 100%;
      }
    }
  }

  .table-card {
    .table-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .pagination-container {
      display: flex;
      justify-content: center;
      padding: 20px 0;
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .metrics-section {
    .metric-content {
      flex-direction: column;
      text-align: center;

      .metric-icon {
        margin-right: 0;
        margin-bottom: 12px;
      }
    }
  }
}

@media (max-width: 768px) {
  .statistics-view {
    padding: 12px;
  }

  .header-section {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .chart-header {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
  }

  .table-header {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
  }
}
</style>