<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { logsApi, LogEntry, LogPageQuery, ApiResponse } from '@/api/usercenter/logs'

// 数据模型定义
interface FilterForm {
  pageIndex: number
  pageSize: number
  userId?: number
  username?: string
  operationType?: string
  module?: string
  startTime?: string
  endTime?: string
  ipAddress?: string
  operationResult?: string
  severityLevel?: string
  onlySensitive?: boolean
  relatedOperationId?: number
  description?: string
  targetId?: number
  dateRange?: [Date, Date] | null
  searchQuery?: string
}

// 可选值列表
const operationTypeOptions = ref<string[]>([
  '创建', '修改', '删除', '查询', '导出', '导入', '登录', '退出', '审批', '授权', '其他'
])

const moduleOptions = ref<string[]>([
  '用户管理', '角色管理', '权限管理', '日志管理', '系统设置', '数据管理', '其他'
])

const operationResultOptions = ref<string[]>([
  '成功', '失败', '部分成功', '处理中'
])

const severityLevelOptions = ref<string[]>([
  '普通', '重要', '关键'
])

// 状态管理
const logs = ref<LogEntry[]>([])
const total = ref(0)
const loading = ref(false)
const showAdvancedFilter = ref(false)
const showStatistics = ref(false)
const currentLog = ref<LogEntry | null>(null)
const showDetailDrawer = ref(false)
const showOperationChain = ref(false)
const chainLogs = ref<LogEntry[]>([])
const statisticsChartRef = ref<HTMLElement | null>(null)
const moduleChartRef = ref<HTMLElement | null>(null)
const operationTypeChartRef = ref<HTMLElement | null>(null)
const statisticsLoading = ref(false)

// 查询条件表单
const filterForm = reactive<FilterForm>({
  pageIndex: 1,
  pageSize: 10,
  dateRange: null
})

// 图表实例
let statisticsChart: echarts.ECharts | null = null
let moduleChart: echarts.ECharts | null = null
let operationTypeChart: echarts.ECharts | null = null

// 表格列设置
const tableColumns = ref([
  { prop: 'id', label: 'ID', width: '80px', visible: true },
  { prop: 'username', label: '用户名', visible: true },
  { prop: 'operationType', label: '操作类型', visible: true },
  { prop: 'module', label: '模块', visible: true },
  { prop: 'description', label: '操作描述', visible: true },
  { prop: 'operationResult', label: '操作结果', visible: true },
  { prop: 'severityLevel', label: '操作级别', visible: true },
  { prop: 'isSensitive', label: '敏感操作', visible: false },
  { prop: 'ipAddress', label: 'IP地址', visible: true },
  { prop: 'createdAt', label: '操作时间', visible: true }
])

// 可见列计算属性
const visibleColumns = computed(() => {
  return tableColumns.value.filter(col => col.visible)
})

// 获取日志列表
async function fetchLogs() {
  loading.value = true
  try {
    // 转换日期范围为API所需的格式
    const params: LogPageQuery = {
      ...filterForm,
      pageNum: filterForm.pageIndex
    }
    
    // 如果有日期范围，转换为startTime和endTime
    if (filterForm.dateRange) {
      params.startTime = filterForm.dateRange[0].toISOString()
      params.endTime = filterForm.dateRange[1].toISOString()
    }
    
    // 如果有搜索关键词，则放入description进行搜索
    if (filterForm.searchQuery) {
      params.description = filterForm.searchQuery
    }
    
    const res = await logsApi.page(params)
    
    if (res.data.success) {
      logs.value = res.data.data
      total.value = res.data.totalCount || 0
    } else {
      ElMessage.error(res.data.errMessage || '获取日志列表失败')
    }
  } catch (error) {
    console.error('获取日志列表错误:', error)
    ElMessage.error('获取日志列表失败')
  } finally {
    loading.value = false
  }
}

// 查看日志详情
async function viewLogDetail(log: LogEntry) {
  try {
    const res = await logsApi.getDetail(log.id)
    if (res.data.success) {
      currentLog.value = res.data.data
      showDetailDrawer.value = true
    } else {
      ElMessage.error(res.data.errMessage || '获取日志详情失败')
    }
  } catch (error) {
    console.error('获取日志详情错误:', error)
    ElMessage.error('获取日志详情失败')
  }
}

// 查看操作链
async function viewOperationChain(log: LogEntry) {
  if (!log.relatedOperationId) {
    ElMessage.warning('该日志没有关联操作')
    return
  }
  
  try {
    const res = await logsApi.getChain(log.relatedOperationId)
    if (res.data.success) {
      chainLogs.value = res.data.data
      showOperationChain.value = true
    } else {
      ElMessage.error(res.data.errMessage || '获取操作链失败')
    }
  } catch (error) {
    console.error('获取操作链错误:', error)
    ElMessage.error('获取操作链失败')
  }
}

// 加载统计数据并初始化图表
async function loadStatistics() {
  if (!showStatistics.value) return
  
  statisticsLoading.value = true
  try {
    // 获取模块统计数据
    const moduleRes = await logsApi.getModuleStatistics({
      modules: moduleOptions.value,
      startTime: filterForm.startTime,
      endTime: filterForm.endTime
    })
    
    // 获取操作类型统计数据
    const typeRes = await logsApi.getOperationTypeStatistics({
      operationTypes: operationTypeOptions.value,
      startTime: filterForm.startTime,
      endTime: filterForm.endTime
    })
    
    if (moduleRes.data.success && typeRes.data.success) {
      initModuleChart(moduleRes.data.data)
      initOperationTypeChart(typeRes.data.data)
    } else {
      ElMessage.error('获取统计数据失败')
    }
  } catch (error) {
    console.error('加载统计数据错误:', error)
    ElMessage.error('加载统计数据失败')
  } finally {
    statisticsLoading.value = false
  }
}

// 初始化模块统计图表
function initModuleChart(data: any[]) {
  if (!moduleChartRef.value) return
  
  // 销毁旧图表实例
  if (moduleChart) {
    moduleChart.dispose()
  }
  
  // 处理数据
  const modules: string[] = []
  const operationCounts: number[] = []
  const successCounts: number[] = []
  const failCounts: number[] = []
  
  data.forEach(item => {
    modules.push(item.module)
    operationCounts.push(item.operationCount)
    successCounts.push(item.successCount)
    failCounts.push(item.failCount)
  })
  
  // 创建新图表
  moduleChart = echarts.init(moduleChartRef.value)
  moduleChart.setOption({
    title: {
      text: '模块操作统计',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['操作总数', '成功', '失败'],
      bottom: 0
    },
    xAxis: {
      type: 'category',
      data: modules
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '操作总数',
        type: 'bar',
        data: operationCounts
      },
      {
        name: '成功',
        type: 'bar',
        data: successCounts
      },
      {
        name: '失败',
        type: 'bar',
        data: failCounts
      }
    ]
  })
}

// 初始化操作类型统计图表
function initOperationTypeChart(data: any[]) {
  if (!operationTypeChartRef.value) return
  
  // 销毁旧图表实例
  if (operationTypeChart) {
    operationTypeChart.dispose()
  }
  
  // 处理数据
  const types: string[] = []
  const counts: number[] = []
  
  data.forEach(item => {
    types.push(item.operationType)
    counts.push(item.operationCount)
  })
  
  // 创建新图表
  operationTypeChart = echarts.init(operationTypeChartRef.value)
  operationTypeChart.setOption({
    title: {
      text: '操作类型分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 0,
      data: types
    },
    series: [
      {
        name: '操作类型',
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
            fontSize: '14',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: types.map((type, index) => ({
          value: counts[index],
          name: type
        }))
      }
    ]
  })
}

// 搜索日志
function handleSearch() {
  filterForm.pageIndex = 1
  fetchLogs()
}

// 重置筛选条件
function resetFilter() {
  const defaultForm: FilterForm = {
    pageIndex: 1,
    pageSize: 10,
    dateRange: null
  }
  
  Object.assign(filterForm, defaultForm)
  
  Object.keys(filterForm).forEach(key => {
    if (!Object.prototype.hasOwnProperty.call(defaultForm, key)) {
      delete filterForm[key as keyof typeof filterForm]
    }
  })
  
  handleSearch()
}

// 处理分页变化
function handlePageChange(page: number) {
  filterForm.pageIndex = page
  fetchLogs()
}

// 处理每页数量变化
function handleSizeChange(size: number) {
  filterForm.pageSize = size
  filterForm.pageIndex = 1
  fetchLogs()
}

// 处理日期范围变化
function handleDateRangeChange() {
  handleSearch()
}

// 切换高级筛选显示状态
function toggleAdvancedFilter() {
  showAdvancedFilter.value = !showAdvancedFilter.value
}

// 切换统计数据显示状态
function toggleStatistics() {
  showStatistics.value = !showStatistics.value
  if (showStatistics.value) {
    // 在下一个DOM更新周期后初始化图表
    setTimeout(() => {
      loadStatistics()
    }, 0)
  }
}

// 导出日志
async function exportLogs(format: 'CSV' | 'EXCEL' | 'PDF') {
  try {
    const params: LogPageQuery = {
      ...filterForm,
      pageNum: 1,
      pageSize: 1000 // 导出更多数据
    }
    
    // 如果有日期范围，转换为startTime和endTime
    if (filterForm.dateRange) {
      params.startTime = filterForm.dateRange[0].toISOString()
      params.endTime = filterForm.dateRange[1].toISOString()
    }
    
    ElMessage.info(`正在导出${format}格式日志，请稍候...`)
    const res = await logsApi.export(format, params)
    
    if (res.data.success) {
      // 假设返回的是下载链接
      if (res.data.data) {
        const a = document.createElement('a')
        a.href = res.data.data
        a.download = `操作日志_${new Date().toISOString().split('T')[0]}.${format.toLowerCase()}`
        document.body.appendChild(a)
        a.click()
        document.body.removeChild(a)
        ElMessage.success(`${format}格式日志导出成功`)
      } else {
        ElMessage.warning('导出成功，但未收到下载链接')
      }
    } else {
      ElMessage.error(res.data.errMessage || '导出日志失败')
    }
  } catch (error) {
    console.error('导出日志错误:', error)
    ElMessage.error('导出日志失败')
  }
}

// 检测异常操作
async function detectAbnormal() {
  ElMessageBox.prompt('请输入用户ID和检测时间范围（分钟）', '检测异常操作', {
    confirmButtonText: '检测',
    cancelButtonText: '取消',
    inputPattern: /^\d+,\d+$/,
    inputErrorMessage: '格式错误，请输入"用户ID,时间范围"，例如：1,60'
  }).then(({ value }) => {
    const [userId, timeRange] = value.split(',').map(Number)
    
    if (!userId || !timeRange) {
      ElMessage.error('参数无效')
      return
    }
    
    logsApi.detectAbnormal(userId, timeRange).then(res => {
      if (res.data.success) {
        if (res.data.data.length > 0) {
          ElMessage({
            type: 'warning',
            message: `检测到${res.data.data.length}条异常操作`
          })
          logs.value = res.data.data
          total.value = res.data.data.length
        } else {
          ElMessage({
            type: 'success',
            message: '未检测到异常操作'
          })
        }
      } else {
        ElMessage.error(res.data.errMessage || '检测异常操作失败')
      }
    }).catch(error => {
      console.error('检测异常操作错误:', error)
      ElMessage.error('检测异常操作失败')
    })
  }).catch(() => {
    // 用户取消输入
  })
}

// 查看资源历史操作
async function viewResourceHistory() {
  ElMessageBox.prompt('请输入模块和目标ID', '查看资源历史', {
    confirmButtonText: '查看',
    cancelButtonText: '取消',
    inputPattern: /^.+,\d+$/,
    inputErrorMessage: '格式错误，请输入"模块,目标ID"，例如：用户管理,1'
  }).then(({ value }) => {
    const [module, targetId] = value.split(',')
    
    if (!module || !targetId) {
      ElMessage.error('参数无效')
      return
    }
    
    logsApi.getHistory({
      module,
      targetId: Number(targetId)
    }).then(res => {
      if (res.data.success) {
        if (res.data.data.length > 0) {
          ElMessage({
            type: 'success',
            message: `获取到${res.data.data.length}条历史操作记录`
          })
          logs.value = res.data.data
          total.value = res.data.data.length
        } else {
          ElMessage({
            type: 'info',
            message: '未找到相关历史记录'
          })
        }
      } else {
        ElMessage.error(res.data.errMessage || '获取资源历史失败')
      }
    }).catch(error => {
      console.error('获取资源历史错误:', error)
      ElMessage.error('获取资源历史失败')
    })
  }).catch(() => {
    // 用户取消输入
  })
}

// 自定义列显示设置
function toggleColumnVisibility(column: any) {
  column.visible = !column.visible
}

// 查看操作内容详情
function formatOperationContent(content: Record<string, any> | null) {
  if (!content) return '无详细内容'
  try {
    return JSON.stringify(content, null, 2)
  } catch (e) {
    return '内容格式错误'
  }
}

// 在组件挂载时加载日志数据
onMounted(() => {
  fetchLogs()
})
</script>

<template>
  <div class="operation-log">
    <div class="page-header">
      <h2>操作日志</h2>
      <div class="header-actions">
        <el-button type="primary" plain size="small" @click="toggleAdvancedFilter">
          {{ showAdvancedFilter ? '隐藏高级筛选' : '高级筛选' }}
        </el-button>
        <el-button type="primary" plain size="small" @click="toggleStatistics">
          {{ showStatistics ? '隐藏统计' : '查看统计' }}
        </el-button>
        <el-dropdown @command="exportLogs">
          <el-button type="primary" size="small">
            导出 <el-icon><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="CSV">CSV格式</el-dropdown-item>
              <el-dropdown-item command="EXCEL">Excel格式</el-dropdown-item>
              <el-dropdown-item command="PDF">PDF格式</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-dropdown>
          <el-button type="primary" size="small">
            更多操作 <el-icon><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="detectAbnormal">检测异常操作</el-dropdown-item>
              <el-dropdown-item @click="viewResourceHistory">查看资源历史</el-dropdown-item>
              <el-dropdown-item divided>
                <el-popover placement="right" trigger="click" width="300">
                  <template #reference>
                    <span>自定义显示列</span>
                  </template>
                  <div class="column-selector">
                    <el-checkbox 
                      v-for="col in tableColumns" 
                      :key="col.prop" 
                      v-model="col.visible" 
                      :label="col.label"
                    />
                  </div>
                </el-popover>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 基础筛选区 -->
    <div class="basic-filter">
      <el-date-picker
        v-model="filterForm.dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        @change="handleDateRangeChange"
        class="date-picker"
      />
      <el-input
        v-model="filterForm.searchQuery"
        placeholder="搜索操作描述"
        class="search-input"
        clearable
      >
        <template #append>
          <el-button @click="handleSearch">
            <el-icon><search /></el-icon>
          </el-button>
        </template>
      </el-input>
      <el-button @click="resetFilter" plain>重置</el-button>
    </div>

    <!-- 高级筛选区 -->
    <div v-if="showAdvancedFilter" class="advanced-filter">
      <el-form :model="filterForm" label-width="100px" inline>
        <el-form-item label="用户ID">
          <el-input v-model="filterForm.userId" placeholder="用户ID" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="filterForm.username" placeholder="用户名" />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="filterForm.operationType" placeholder="操作类型" clearable>
            <el-option
              v-for="type in operationTypeOptions"
              :key="type"
              :label="type"
              :value="type"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="模块">
          <el-select v-model="filterForm.module" placeholder="模块" clearable>
            <el-option
              v-for="module in moduleOptions"
              :key="module"
              :label="module"
              :value="module"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="IP地址">
          <el-input v-model="filterForm.ipAddress" placeholder="IP地址" />
        </el-form-item>
        <el-form-item label="操作结果">
          <el-select v-model="filterForm.operationResult" placeholder="操作结果" clearable>
            <el-option
              v-for="result in operationResultOptions"
              :key="result"
              :label="result"
              :value="result"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="操作级别">
          <el-select v-model="filterForm.severityLevel" placeholder="操作级别" clearable>
            <el-option
              v-for="level in severityLevelOptions"
              :key="level"
              :label="level"
              :value="level"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="敏感操作">
          <el-switch v-model="filterForm.onlySensitive" />
        </el-form-item>
        <el-form-item label="目标ID">
          <el-input v-model="filterForm.targetId" placeholder="目标ID" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 统计图表区 -->
    <div v-if="showStatistics" class="statistics-panel" v-loading="statisticsLoading">
      <div class="charts-container">
        <div ref="moduleChartRef" class="chart-item"></div>
        <div ref="operationTypeChartRef" class="chart-item"></div>
      </div>
    </div>

    <!-- 日志表格 -->
    <div class="table-container">
      <el-table 
        v-loading="loading" 
        :data="logs" 
        class="log-table"
        @row-click="viewLogDetail"
      >
        <el-table-column 
          v-for="col in visibleColumns" 
          :key="col.prop" 
          :prop="col.prop" 
          :label="col.label" 
          :width="col.width"
        >
          <template #default="{ row }" v-if="col.prop === 'operationResult'">
            <el-tag 
              :type="row.operationResult === '成功' ? 'success' : row.operationResult === '失败' ? 'danger' : 'warning'"
              size="small"
            >
              {{ row.operationResult }}
            </el-tag>
          </template>
          
          <template #default="{ row }" v-else-if="col.prop === 'severityLevel'">
            <el-tag 
              :type="row.severityLevel === '普通' ? 'info' : row.severityLevel === '重要' ? 'warning' : 'danger'"
              size="small"
            >
              {{ row.severityLevel }}
            </el-tag>
          </template>
          
          <template #default="{ row }" v-else-if="col.prop === 'isSensitive'">
            <el-tag 
              v-if="row.isSensitive === 1" 
              type="danger" 
              size="small"
            >
              敏感
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        
        <el-table-column fixed="right" label="操作" width="120">
          <template #default="{ row }">
            <el-button 
              link 
              type="primary" 
              size="small" 
              @click.stop="viewLogDetail(row)"
            >
              详情
            </el-button>
            <el-button 
              v-if="row.relatedOperationId" 
              link 
              type="primary" 
              size="small" 
              @click.stop="viewOperationChain(row)"
            >
              操作链
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="filterForm.pageIndex"
          v-model:page-size="filterForm.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 日志详情抽屉 -->
    <el-drawer
      v-model="showDetailDrawer"
      title="操作日志详情"
      direction="rtl"
      size="50%"
    >
      <div v-if="currentLog" class="log-detail">
        <div class="detail-item">
          <span class="detail-label">日志ID:</span>
          <span class="detail-value">{{ currentLog.id }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">用户ID:</span>
          <span class="detail-value">{{ currentLog.userId }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">用户名:</span>
          <span class="detail-value">{{ currentLog.username }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">操作类型:</span>
          <span class="detail-value">{{ currentLog.operationType }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">模块:</span>
          <span class="detail-value">{{ currentLog.module }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">操作描述:</span>
          <span class="detail-value">{{ currentLog.description }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">操作结果:</span>
          <span class="detail-value">{{ currentLog.operationResult }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">IP地址:</span>
          <span class="detail-value">{{ currentLog.ipAddress }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">操作级别:</span>
          <span class="detail-value">{{ currentLog.severityLevel }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">敏感操作:</span>
          <span class="detail-value">{{ currentLog.isSensitive === 1 ? '是' : '否' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">创建时间:</span>
          <span class="detail-value">{{ currentLog.createdAt }}</span>
        </div>
        <div class="detail-item-full">
          <span class="detail-label">操作内容:</span>
          <pre class="detail-code">{{ formatOperationContent(currentLog.operationContent) }}</pre>
        </div>
        <div v-if="currentLog.approvalInfo" class="detail-item-full">
          <span class="detail-label">审批信息:</span>
          <pre class="detail-code">{{ formatOperationContent(currentLog.approvalInfo) }}</pre>
        </div>
        <div class="detail-actions">
          <el-button 
            v-if="currentLog.relatedOperationId" 
            type="primary" 
            @click="viewOperationChain(currentLog)"
          >
            查看操作链
          </el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 操作链对话框 -->
    <el-dialog
      v-model="showOperationChain"
      title="操作链"
      width="70%"
    >
      <div v-if="chainLogs.length" class="operation-chain">
        <el-timeline>
          <el-timeline-item
            v-for="log in chainLogs"
            :key="log.id"
            :timestamp="log.createdAt"
            :type="log.operationResult === '成功' ? 'success' : log.operationResult === '失败' ? 'danger' : 'warning'"
          >
            <div class="chain-item">
              <h4>{{ log.operationType }} - {{ log.username }}</h4>
              <p>{{ log.description }}</p>
              <div class="chain-item-details">
                <span>模块: {{ log.module }}</span> | 
                <span>结果: {{ log.operationResult }}</span> | 
                <span>IP: {{ log.ipAddress }}</span>
              </div>
              <el-button 
                link 
                type="primary" 
                size="small" 
                @click="viewLogDetail(log)"
              >
                查看详情
              </el-button>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
      <div v-else class="empty-chain">
        <p>未找到关联操作记录</p>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.operation-log {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

h2 {
  margin: 0;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.basic-filter {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
  align-items: center;
}

.date-picker {
  width: 320px;
}

.search-input {
  width: 300px;
}

.advanced-filter {
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.statistics-panel {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.charts-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.chart-item {
  height: 300px;
  flex: 1;
  min-width: 300px;
}

.table-container {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.log-table {
  width: 100%;
}

.pagination {
  padding: 20px;
  display: flex;
  justify-content: flex-end;
}

.column-selector {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.log-detail {
  padding: 0 20px;
}

.detail-item {
  margin-bottom: 15px;
  display: flex;
}

.detail-item-full {
  margin-bottom: 15px;
}

.detail-label {
  font-weight: bold;
  width: 100px;
  color: #606266;
}

.detail-value {
  flex: 1;
}

.detail-code {
  background-color: #f8f9fa;
  padding: 10px;
  border-radius: 4px;
  margin-top: 5px;
  overflow: auto;
  max-height: 300px;
  font-family: monospace;
  white-space: pre-wrap;
}

.detail-actions {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.operation-chain {
  padding: 0 20px;
}

.chain-item {
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.chain-item h4 {
  margin: 0 0 10px 0;
  color: #303133;
}

.chain-item p {
  margin: 5px 0;
  color: #606266;
}

.chain-item-details {
  font-size: 12px;
  color: #909399;
  margin: 10px 0;
}

.empty-chain {
  text-align: center;
  padding: 30px;
  color: #909399;
}
</style>