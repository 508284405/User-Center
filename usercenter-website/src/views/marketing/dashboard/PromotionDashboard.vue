<template>
  <div class="promotion-dashboard">
    <!-- 系统概览 -->
    <div class="overview-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <h3>促销系统概览</h3>
            <el-button-group size="small">
              <el-button :type="timeRange === 'today' ? 'primary' : ''" @click="timeRange = 'today'">今日</el-button>
              <el-button :type="timeRange === 'week' ? 'primary' : ''" @click="timeRange = 'week'">本周</el-button>
              <el-button :type="timeRange === 'month' ? 'primary' : ''" @click="timeRange = 'month'">本月</el-button>
            </el-button-group>
          </div>
        </template>

        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-card">
              <el-statistic 
                title="活跃活动数" 
                :value="systemStats.activeActivities"
                :value-style="{ color: '#67c23a' }"
              />
              <div class="stat-trend">
                <el-icon :class="getTrendClass(systemStats.activitiesTrend)">
                  <component :is="getTrendIcon(systemStats.activitiesTrend)" />
                </el-icon>
                <span :class="getTrendClass(systemStats.activitiesTrend)">
                  {{ Math.abs(systemStats.activitiesTrend || 0) }}%
                </span>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <el-statistic 
                title="参与用户数" 
                :value="systemStats.totalParticipants"
                :value-style="{ color: '#409eff' }"
              />
              <div class="stat-trend">
                <el-icon :class="getTrendClass(systemStats.participantsTrend)">
                  <component :is="getTrendIcon(systemStats.participantsTrend)" />
                </el-icon>
                <span :class="getTrendClass(systemStats.participantsTrend)">
                  {{ Math.abs(systemStats.participantsTrend || 0) }}%
                </span>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <el-statistic 
                title="总订单数" 
                :value="systemStats.totalOrders"
                :value-style="{ color: '#e6a23c' }"
              />
              <div class="stat-trend">
                <el-icon :class="getTrendClass(systemStats.ordersTrend)">
                  <component :is="getTrendIcon(systemStats.ordersTrend)" />
                </el-icon>
                <span :class="getTrendClass(systemStats.ordersTrend)">
                  {{ Math.abs(systemStats.ordersTrend || 0) }}%
                </span>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <el-statistic 
                title="促销销售额" 
                :value="formatAmount(systemStats.totalSales)"
                prefix="¥"
                :value-style="{ color: '#f56c6c' }"
              />
              <div class="stat-trend">
                <el-icon :class="getTrendClass(systemStats.salesTrend)">
                  <component :is="getTrendIcon(systemStats.salesTrend)" />
                </el-icon>
                <span :class="getTrendClass(systemStats.salesTrend)">
                  {{ Math.abs(systemStats.salesTrend || 0) }}%
                </span>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- 活动监控 -->
    <el-row :gutter="20" class="monitoring-section">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <h4>活动实时监控</h4>
              <el-switch
                v-model="autoRefresh"
                active-text="自动刷新"
                inactive-text="停止刷新"
                @change="toggleAutoRefresh"
              />
            </div>
          </template>
          
          <el-table :data="monitoringData" stripe style="width: 100%" size="small">
            <el-table-column prop="name" label="活动名称" width="200" show-overflow-tooltip />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="getActivityTypeColor(row.type)">
                  {{ getActivityTypeText(row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="getStatusColor(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="参与/目标" width="120">
              <template #default="{ row }">
                <div class="progress-cell">
                  <el-progress
                    :percentage="getProgressPercentage(row.participants, row.target)"
                    :stroke-width="4"
                    :show-text="false"
                    :color="getProgressColor(row.participants, row.target)"
                  />
                  <span class="progress-text">{{ row.participants }}/{{ row.target || '∞' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="orderCount" label="订单数" width="80" align="center" />
            <el-table-column label="实时销售额" width="120" align="right">
              <template #default="{ row }">
                <span class="sales-amount">¥{{ formatAmount(row.sales) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="转化率" width="80" align="center">
              <template #default="{ row }">
                <span :class="getConversionClass(row.conversion)">
                  {{ row.conversion }}%
                </span>
              </template>
            </el-table-column>
            <el-table-column label="系统负载" width="100">
              <template #default="{ row }">
                <el-progress
                  :percentage="row.systemLoad"
                  :stroke-width="6"
                  :color="getLoadColor(row.systemLoad)"
                  :show-text="false"
                />
                <div class="load-text">{{ row.systemLoad }}%</div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button type="text" size="small" @click="viewActivityDetail(row)">详情</el-button>
                <el-button type="text" size="small" @click="viewActivityLogs(row)">日志</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="system-health-card">
          <template #header>
            <h4>系统健康状态</h4>
          </template>
          
          <div class="health-metrics">
            <div class="health-item">
              <div class="health-label">服务状态</div>
              <div class="health-value">
                <el-tag :type="systemHealth.serviceStatus === 'healthy' ? 'success' : 'danger'">
                  {{ systemHealth.serviceStatus === 'healthy' ? '正常' : '异常' }}
                </el-tag>
              </div>
            </div>
            
            <div class="health-item">
              <div class="health-label">数据库连接</div>
              <div class="health-value">
                <el-progress
                  :percentage="systemHealth.dbConnection"
                  :color="systemHealth.dbConnection > 90 ? '#67c23a' : systemHealth.dbConnection > 70 ? '#e6a23c' : '#f56c6c'"
                  :stroke-width="8"
                />
              </div>
            </div>
            
            <div class="health-item">
              <div class="health-label">缓存性能</div>
              <div class="health-value">
                <el-progress
                  :percentage="systemHealth.cachePerformance"
                  :color="systemHealth.cachePerformance > 90 ? '#67c23a' : systemHealth.cachePerformance > 70 ? '#e6a23c' : '#f56c6c'"
                  :stroke-width="8"
                />
              </div>
            </div>
            
            <div class="health-item">
              <div class="health-label">消息队列</div>
              <div class="health-value">
                <el-tag :type="systemHealth.mqStatus === 'healthy' ? 'success' : 'warning'">
                  {{ systemHealth.mqStatus === 'healthy' ? '正常' : '延迟' }}
                </el-tag>
              </div>
            </div>
            
            <div class="health-item">
              <div class="health-label">API响应时间</div>
              <div class="health-value">
                <span :class="getResponseTimeClass(systemHealth.avgResponseTime)">
                  {{ systemHealth.avgResponseTime }}ms
                </span>
              </div>
            </div>
            
            <div class="health-item">
              <div class="health-label">错误率</div>
              <div class="health-value">
                <span :class="getErrorRateClass(systemHealth.errorRate)">
                  {{ systemHealth.errorRate }}%
                </span>
              </div>
            </div>
          </div>
          
          <div class="health-actions">
            <el-button type="primary" size="small" @click="refreshHealthStatus">刷新状态</el-button>
            <el-button type="warning" size="small" @click="viewSystemLogs">系统日志</el-button>
            <el-button type="danger" size="small" @click="emergencyStop">紧急停止</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 性能分析图表 -->
    <el-row :gutter="20" class="charts-section">
      <el-col :span="12">
        <el-card>
          <template #header>
            <h4>活动参与趋势</h4>
          </template>
          <div ref="participationChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <h4>系统性能监控</h4>
          </template>
          <div ref="performanceChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 告警和通知 -->
    <el-card class="alerts-section">
      <template #header>
        <div class="card-header">
          <h4>系统告警</h4>
          <el-badge :value="unreadAlerts" :max="99">
            <el-button size="small" @click="markAlertsRead">标记已读</el-button>
          </el-badge>
        </div>
      </template>
      
      <el-table :data="alertsData" stripe style="width: 100%" size="small">
        <el-table-column label="告警级别" width="100">
          <template #default="{ row }">
            <el-tag :type="getAlertTypeColor(row.level)">
              {{ getAlertLevelText(row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="告警内容" show-overflow-tooltip />
        <el-table-column prop="source" label="来源" width="120" />
        <el-table-column prop="time" label="时间" width="150">
          <template #default="{ row }">
            {{ formatDateTime(row.time) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 'resolved' ? 'success' : 'danger'">
              {{ row.status === 'resolved' ? '已解决' : '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="text" size="small" @click="viewAlertDetail(row)">详情</el-button>
            <el-button 
              v-if="row.status !== 'resolved'" 
              type="text" 
              size="small" 
              @click="resolveAlert(row)"
            >
              解决
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowUp, ArrowDown, Minus } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

interface SystemStats {
  activeActivities: number
  totalParticipants: number
  totalOrders: number
  totalSales: number
  activitiesTrend: number
  participantsTrend: number
  ordersTrend: number
  salesTrend: number
}

interface SystemHealth {
  serviceStatus: 'healthy' | 'unhealthy'
  dbConnection: number
  cachePerformance: number
  mqStatus: 'healthy' | 'delayed'
  avgResponseTime: number
  errorRate: number
}

interface MonitoringActivity {
  id: number
  name: string
  type: string
  status: string
  participants: number
  target?: number
  orderCount: number
  sales: number
  conversion: number
  systemLoad: number
}

interface AlertItem {
  id: number
  level: 'critical' | 'warning' | 'info'
  title: string
  source: string
  time: string
  status: 'pending' | 'resolved'
}

const timeRange = ref<'today' | 'week' | 'month'>('today')
const autoRefresh = ref(true)
const refreshTimer = ref<NodeJS.Timeout | null>(null)
const unreadAlerts = ref(5)

const participationChartRef = ref<HTMLDivElement>()
const performanceChartRef = ref<HTMLDivElement>()

const systemStats = ref<SystemStats>({
  activeActivities: 12,
  totalParticipants: 8450,
  totalOrders: 2680,
  totalSales: 1250000,
  activitiesTrend: 15.8,
  participantsTrend: 23.5,
  ordersTrend: -8.2,
  salesTrend: 12.3
})

const systemHealth = ref<SystemHealth>({
  serviceStatus: 'healthy',
  dbConnection: 95,
  cachePerformance: 88,
  mqStatus: 'healthy',
  avgResponseTime: 120,
  errorRate: 0.5
})

const monitoringData = ref<MonitoringActivity[]>([
  { id: 1, name: '双11大促销', type: 'FULL_REDUCTION', status: 'ACTIVE', participants: 1250, target: 2000, orderCount: 380, sales: 125600, conversion: 30.4, systemLoad: 75 },
  { id: 2, name: '新用户专享', type: 'NEW_USER', status: 'ACTIVE', participants: 680, target: 1000, orderCount: 120, sales: 45200, conversion: 17.6, systemLoad: 45 },
  { id: 3, name: '会员折扣日', type: 'DISCOUNT', status: 'ACTIVE', participants: 920, target: 1500, orderCount: 280, sales: 89400, conversion: 30.4, systemLoad: 60 },
  { id: 4, name: '限时秒杀', type: 'SECKILL', status: 'ACTIVE', participants: 2100, target: 1000, orderCount: 850, sales: 68000, conversion: 40.5, systemLoad: 90 }
])

const alertsData = ref<AlertItem[]>([
  { id: 1, level: 'critical', title: '秒杀活动系统负载过高，响应时间超过5秒', source: '系统监控', time: new Date().toISOString(), status: 'pending' },
  { id: 2, level: 'warning', title: '数据库连接池使用率达到85%', source: '数据库监控', time: new Date(Date.now() - 300000).toISOString(), status: 'pending' },
  { id: 3, level: 'info', title: '新用户专享活动达到目标参与人数的68%', source: '活动监控', time: new Date(Date.now() - 600000).toISOString(), status: 'resolved' },
  { id: 4, level: 'warning', title: '优惠券发放接口调用频率异常', source: 'API监控', time: new Date(Date.now() - 900000).toISOString(), status: 'pending' },
  { id: 5, level: 'critical', title: 'Redis缓存服务连接中断', source: '缓存监控', time: new Date(Date.now() - 1200000).toISOString(), status: 'resolved' }
])

const toggleAutoRefresh = (enabled: boolean) => {
  if (enabled) {
    startAutoRefresh()
  } else {
    stopAutoRefresh()
  }
}

const startAutoRefresh = () => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
  }
  refreshTimer.value = setInterval(() => {
    refreshMonitoringData()
  }, 5000)
}

const stopAutoRefresh = () => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
    refreshTimer.value = null
  }
}

const refreshMonitoringData = () => {
  // 模拟数据更新
  monitoringData.value = monitoringData.value.map(item => ({
    ...item,
    participants: item.participants + Math.floor(Math.random() * 10) - 5,
    orderCount: item.orderCount + Math.floor(Math.random() * 5),
    sales: item.sales + Math.floor(Math.random() * 1000) - 500,
    systemLoad: Math.max(0, Math.min(100, item.systemLoad + Math.floor(Math.random() * 10) - 5))
  }))
}

const initCharts = () => {
  nextTick(() => {
    if (participationChartRef.value) {
      const participationChart = echarts.init(participationChartRef.value)
      const participationOption = {
        tooltip: { trigger: 'axis' },
        legend: { data: ['参与人数', '订单数'] },
        xAxis: {
          type: 'category',
          data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00']
        },
        yAxis: { type: 'value' },
        series: [
          {
            name: '参与人数',
            type: 'line',
            data: [120, 200, 450, 680, 920, 1100, 1250],
            smooth: true
          },
          {
            name: '订单数',
            type: 'line',
            data: [50, 80, 180, 250, 320, 380, 420],
            smooth: true
          }
        ]
      }
      participationChart.setOption(participationOption)
    }

    if (performanceChartRef.value) {
      const performanceChart = echarts.init(performanceChartRef.value)
      const performanceOption = {
        tooltip: { trigger: 'axis' },
        legend: { data: ['CPU使用率', '内存使用率', '响应时间'] },
        xAxis: {
          type: 'category',
          data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00']
        },
        yAxis: [
          { type: 'value', name: '使用率(%)', max: 100 },
          { type: 'value', name: '响应时间(ms)', position: 'right' }
        ],
        series: [
          {
            name: 'CPU使用率',
            type: 'line',
            data: [45, 52, 68, 75, 82, 78, 65],
            yAxisIndex: 0
          },
          {
            name: '内存使用率',
            type: 'line',
            data: [60, 65, 70, 85, 90, 88, 75],
            yAxisIndex: 0
          },
          {
            name: '响应时间',
            type: 'line',
            data: [80, 90, 120, 150, 200, 180, 120],
            yAxisIndex: 1
          }
        ]
      }
      performanceChart.setOption(performanceOption)
    }
  })
}

// 辅助函数
const formatAmount = (amount: number) => (amount / 100).toFixed(2)
const formatDateTime = (dateTime: string) => new Date(dateTime).toLocaleString('zh-CN')

const getTrendClass = (trend: number) => {
  if (trend > 0) return 'trend-up'
  if (trend < 0) return 'trend-down'
  return 'trend-neutral'
}

const getTrendIcon = (trend: number) => {
  if (trend > 0) return ArrowUp
  if (trend < 0) return ArrowDown
  return Minus
}

const getActivityTypeColor = (type: string) => {
  const colors: Record<string, string> = {
    FULL_REDUCTION: 'success',
    DISCOUNT: 'warning',
    SECKILL: 'danger',
    NEW_USER: 'info'
  }
  return colors[type] || ''
}

const getActivityTypeText = (type: string) => {
  const texts: Record<string, string> = {
    FULL_REDUCTION: '满减',
    DISCOUNT: '折扣',
    SECKILL: '秒杀',
    NEW_USER: '新用户'
  }
  return texts[type] || type
}

const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    ACTIVE: 'success',
    PAUSED: 'warning',
    ENDED: 'info'
  }
  return colors[status] || ''
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    ACTIVE: '进行中',
    PAUSED: '已暂停',
    ENDED: '已结束'
  }
  return texts[status] || status
}

const getProgressPercentage = (current: number, target?: number) => {
  if (!target) return 0
  return Math.min(100, (current / target) * 100)
}

const getProgressColor = (current: number, target?: number) => {
  const percentage = getProgressPercentage(current, target)
  if (percentage >= 100) return '#f56c6c'
  if (percentage >= 80) return '#e6a23c'
  if (percentage >= 60) return '#409eff'
  return '#67c23a'
}

const getLoadColor = (load: number) => {
  if (load >= 90) return '#f56c6c'
  if (load >= 70) return '#e6a23c'
  return '#67c23a'
}

const getConversionClass = (conversion: number) => {
  if (conversion >= 30) return 'conversion-high'
  if (conversion >= 20) return 'conversion-medium'
  return 'conversion-low'
}

const getResponseTimeClass = (time: number) => {
  if (time >= 500) return 'response-slow'
  if (time >= 200) return 'response-medium'
  return 'response-fast'
}

const getErrorRateClass = (rate: number) => {
  if (rate >= 5) return 'error-high'
  if (rate >= 1) return 'error-medium'
  return 'error-low'
}

const getAlertTypeColor = (level: string) => {
  const colors: Record<string, string> = {
    critical: 'danger',
    warning: 'warning',
    info: 'info'
  }
  return colors[level] || ''
}

const getAlertLevelText = (level: string) => {
  const texts: Record<string, string> = {
    critical: '严重',
    warning: '警告',
    info: '信息'
  }
  return texts[level] || level
}

// 事件处理函数
const viewActivityDetail = (activity: MonitoringActivity) => {
  ElMessage.info(`查看活动详情: ${activity.name}`)
}

const viewActivityLogs = (activity: MonitoringActivity) => {
  ElMessage.info(`查看活动日志: ${activity.name}`)
}

const refreshHealthStatus = () => {
  ElMessage.success('系统状态已刷新')
}

const viewSystemLogs = () => {
  ElMessage.info('打开系统日志')
}

const emergencyStop = async () => {
  try {
    await ElMessageBox.confirm('确定要执行紧急停止操作吗？这将停止所有正在进行的促销活动！', '紧急停止', {
      confirmButtonText: '确认停止',
      cancelButtonText: '取消',
      type: 'error'
    })
    ElMessage.success('紧急停止操作已执行')
  } catch {
    // 用户取消
  }
}

const markAlertsRead = () => {
  unreadAlerts.value = 0
  ElMessage.success('告警已标记为已读')
}

const viewAlertDetail = (alert: AlertItem) => {
  ElMessage.info(`查看告警详情: ${alert.title}`)
}

const resolveAlert = (alert: AlertItem) => {
  alert.status = 'resolved'
  unreadAlerts.value = Math.max(0, unreadAlerts.value - 1)
  ElMessage.success('告警已解决')
}

onMounted(() => {
  initCharts()
  if (autoRefresh.value) {
    startAutoRefresh()
  }
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.promotion-dashboard {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3,
.card-header h4 {
  margin: 0;
}

.overview-section {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  position: relative;
}

.stat-trend {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 8px;
  font-size: 12px;
}

.trend-up {
  color: #67c23a;
}

.trend-down {
  color: #f56c6c;
}

.trend-neutral {
  color: #909399;
}

.monitoring-section,
.charts-section {
  margin-bottom: 20px;
}

.system-health-card {
  height: 100%;
}

.health-metrics {
  margin-bottom: 20px;
}

.health-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.health-label {
  font-size: 14px;
  color: #606266;
}

.health-value {
  flex: 1;
  text-align: right;
  margin-left: 12px;
}

.health-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.progress-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-text {
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
}

.load-text {
  font-size: 10px;
  text-align: center;
  color: #606266;
  margin-top: 2px;
}

.sales-amount {
  color: #67c23a;
  font-weight: bold;
}

.conversion-high {
  color: #67c23a;
  font-weight: bold;
}

.conversion-medium {
  color: #e6a23c;
}

.conversion-low {
  color: #f56c6c;
}

.response-fast {
  color: #67c23a;
}

.response-medium {
  color: #e6a23c;
}

.response-slow {
  color: #f56c6c;
}

.error-low {
  color: #67c23a;
}

.error-medium {
  color: #e6a23c;
}

.error-high {
  color: #f56c6c;
}

.alerts-section {
  margin-top: 20px;
}

:deep(.el-statistic__number) {
  font-size: 24px;
  font-weight: bold;
}

:deep(.el-statistic__title) {
  font-size: 14px;
  margin-bottom: 8px;
}

:deep(.el-progress__text) {
  font-size: 10px !important;
}

:deep(.el-badge__content) {
  font-size: 10px;
}
</style>