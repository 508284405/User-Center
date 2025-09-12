<template>
  <div class="merchant-analytics">
    <!-- 页面标题 -->
    <el-page-header @back="goBack" title="商家分析">
      <template #content>
        <span class="text-large font-600 mr-3">商家数据分析</span>
      </template>
    </el-page-header>

    <!-- 筛选条件 -->
    <el-card class="filter-card mt-4">
      <el-form inline :model="filterForm" @submit.prevent>
        <el-form-item label="商家名称">
          <el-input
            v-model="filterForm.merchantName"
            placeholder="请输入商家名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="x"
            @change="handleDateRangeChange"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 概览统计 -->
    <el-row :gutter="20" class="stats-row mt-4">
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic
            title="总商家数"
            :value="overviewStats.totalMerchants"
            class="stat-item"
          >
            <template #suffix>
              <el-icon class="stat-icon"><Shop /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic
            title="总收入"
            :value="overviewStats.totalRevenue"
            :precision="2"
            class="stat-item"
          >
            <template #prefix>¥</template>
            <template #suffix>
              <el-icon class="stat-icon success"><Money /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic
            title="平台佣金"
            :value="overviewStats.totalCommission"
            :precision="2"
            class="stat-item"
          >
            <template #prefix>¥</template>
            <template #suffix>
              <el-icon class="stat-icon warning"><Coin /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic
            title="活跃商家"
            :value="overviewStats.activeMerchants"
            class="stat-item"
          >
            <template #suffix>
              <el-icon class="stat-icon info"><UserFilled /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row mt-4">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>收入趋势</span>
              <el-button-group size="small">
                <el-button 
                  :type="chartTimeRange === 'week' ? 'primary' : ''"
                  @click="setChartTimeRange('week')"
                >
                  周
                </el-button>
                <el-button 
                  :type="chartTimeRange === 'month' ? 'primary' : ''"
                  @click="setChartTimeRange('month')"
                >
                  月
                </el-button>
              </el-button-group>
            </div>
          </template>
          <div id="revenueChart" style="height: 300px"></div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>订单统计</span>
          </template>
          <div id="orderChart" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 商家列表 -->
    <el-card class="table-card mt-4">
      <template #header>
        <div class="card-header">
          <span>商家分析列表</span>
          <div>
            <el-button type="primary" @click="handleBatchOperation">
              <el-icon><Operation /></el-icon>
              批量操作
            </el-button>
            <el-button @click="handleExport">
              <el-icon><Download /></el-icon>
              导出数据
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table
        :data="analyticsData"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        stripe
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="merchantName" label="商家名称" width="150" />
        <el-table-column prop="totalShops" label="店铺数" width="80" align="center" />
        <el-table-column prop="totalProducts" label="商品数" width="80" align="center" />
        <el-table-column prop="totalOrders" label="订单数" width="80" align="center" />
        <el-table-column prop="monthlyRevenue" label="月收入" width="120">
          <template #default="{ row }">
            <span class="revenue-text">¥{{ row.monthlyRevenue?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="monthlyCommission" label="月佣金" width="120">
          <template #default="{ row }">
            <span class="commission-text">¥{{ row.monthlyCommission?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="conversionRate" label="转化率" width="100">
          <template #default="{ row }">
            <el-tag :type="getConversionRateType(row.conversionRate)">
              {{ (row.conversionRate * 100).toFixed(2) }}%
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后活跃" width="120">
          <template #default="{ row }">
            {{ formatDate(row.lastLoginTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button size="small" type="primary" @click="handleViewReport(row)">
              <el-icon><DataAnalysis /></el-icon>
              报表
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 商家详情对话框 -->
    <merchant-analytics-detail
      v-model="detailDialogVisible"
      :merchant="currentMerchant"
    />

    <!-- 批量操作对话框 -->
    <batch-operation-dialog
      v-model="batchDialogVisible"
      :selected-merchants="selectedMerchants"
      @success="handleBatchSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Search,
  Refresh,
  Shop,
  Money,
  Coin,
  UserFilled,
  Operation,
  Download,
  View,
  DataAnalysis
} from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import {
  getMerchantAnalyticsList,
  type MerchantAnalytics,
  type MerchantPageQuery
} from '@/api/client-web/merchant';
import MerchantAnalyticsDetail from './components/MerchantAnalyticsDetail.vue';
import BatchOperationDialog from './components/BatchOperationDialog.vue';

// 筛选表单
const filterForm = reactive({
  merchantName: '',
  startTime: null as number | null,
  endTime: null as number | null
});

const dateRange = ref<[number, number] | null>(null);

// 表格数据
const analyticsData = ref<MerchantAnalytics[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const selectedMerchants = ref<MerchantAnalytics[]>([]);

// 概览统计
const overviewStats = reactive({
  totalMerchants: 0,
  totalRevenue: 0,
  totalCommission: 0,
  activeMerchants: 0
});

// 图表相关
const chartTimeRange = ref<'week' | 'month'>('month');
let revenueChart: echarts.ECharts | null = null;
let orderChart: echarts.ECharts | null = null;

// 对话框
const detailDialogVisible = ref(false);
const batchDialogVisible = ref(false);
const currentMerchant = ref<MerchantAnalytics | null>(null);

// 获取数据
const fetchData = async () => {
  loading.value = true;
  try {
    const query: MerchantPageQuery = {
      pageNo: currentPage.value,
      pageSize: pageSize.value,
      merchantName: filterForm.merchantName || undefined
    };
    
    const response = await getMerchantAnalyticsList(query);
    if (response.success) {
      analyticsData.value = response.data.data;
      total.value = response.data.total;
      
      // 更新概览统计
      updateOverviewStats();
    } else {
      ElMessage.error(response.errMessage || '获取分析数据失败');
    }
  } catch (error) {
    console.error('获取分析数据出错:', error);
    ElMessage.error('获取分析数据失败');
  } finally {
    loading.value = false;
  }
};

// 更新概览统计
const updateOverviewStats = () => {
  const data = analyticsData.value;
  overviewStats.totalMerchants = data.length;
  overviewStats.totalRevenue = data.reduce((sum, item) => sum + (item.monthlyRevenue || 0), 0);
  overviewStats.totalCommission = data.reduce((sum, item) => sum + (item.monthlyCommission || 0), 0);
  overviewStats.activeMerchants = data.filter(item => 
    item.lastLoginTime && (Date.now() - item.lastLoginTime < 7 * 24 * 60 * 60 * 1000)
  ).length;
};

// 初始化图表
const initCharts = async () => {
  await nextTick();
  
  // 收入趋势图
  const revenueEl = document.getElementById('revenueChart');
  if (revenueEl) {
    revenueChart = echarts.init(revenueEl);
    updateRevenueChart();
  }
  
  // 订单统计图
  const orderEl = document.getElementById('orderChart');
  if (orderEl) {
    orderChart = echarts.init(orderEl);
    updateOrderChart();
  }
};

// 更新收入图表
const updateRevenueChart = () => {
  if (!revenueChart) return;
  
  // 模拟数据
  const dates = [];
  const revenues = [];
  const commissions = [];
  
  for (let i = 29; i >= 0; i--) {
    const date = new Date();
    date.setDate(date.getDate() - i);
    dates.push(date.toISOString().split('T')[0]);
    revenues.push(Math.random() * 5000 + 1000);
    commissions.push(revenues[revenues.length - 1] * 0.05);
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['收入', '佣金']
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        formatter: (value: string) => value.split('-').slice(1).join('/')
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '¥{value}'
      }
    },
    series: [
      {
        name: '收入',
        type: 'line',
        data: revenues,
        smooth: true,
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '佣金',
        type: 'line',
        data: commissions,
        smooth: true,
        itemStyle: { color: '#67C23A' }
      }
    ]
  };
  
  revenueChart.setOption(option);
};

// 更新订单图表
const updateOrderChart = () => {
  if (!orderChart) return;
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '订单状态',
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
          { value: 335, name: '已完成' },
          { value: 310, name: '进行中' },
          { value: 274, name: '已取消' }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  };
  
  orderChart.setOption(option);
};

// 转化率类型
const getConversionRateType = (rate: number) => {
  if (rate >= 0.08) return 'success';
  if (rate >= 0.05) return 'warning';
  return 'danger';
};

// 格式化日期
const formatDate = (timestamp: number) => {
  if (!timestamp) return '未知';
  const now = Date.now();
  const diff = now - timestamp;
  const days = Math.floor(diff / (24 * 60 * 60 * 1000));
  
  if (days === 0) return '今天';
  if (days === 1) return '昨天';
  if (days <= 7) return `${days}天前`;
  
  return new Date(timestamp).toLocaleDateString();
};

// 事件处理
const goBack = () => {
  window.history.back();
};

const handleDateRangeChange = (dates: [number, number] | null) => {
  if (dates) {
    filterForm.startTime = dates[0];
    filterForm.endTime = dates[1];
  } else {
    filterForm.startTime = null;
    filterForm.endTime = null;
  }
};

const handleSearch = () => {
  currentPage.value = 1;
  fetchData();
};

const handleReset = () => {
  filterForm.merchantName = '';
  filterForm.startTime = null;
  filterForm.endTime = null;
  dateRange.value = null;
  currentPage.value = 1;
  fetchData();
};

const setChartTimeRange = (range: 'week' | 'month') => {
  chartTimeRange.value = range;
  updateRevenueChart();
};

const handleSelectionChange = (selection: MerchantAnalytics[]) => {
  selectedMerchants.value = selection;
};

const handleViewDetail = (merchant: MerchantAnalytics) => {
  currentMerchant.value = merchant;
  detailDialogVisible.value = true;
};

const handleViewReport = (merchant: MerchantAnalytics) => {
  // 跳转到详细报表页面
  console.log('查看报表:', merchant.merchantName);
};

const handleBatchOperation = () => {
  if (selectedMerchants.value.length === 0) {
    ElMessage.warning('请选择要操作的商家');
    return;
  }
  batchDialogVisible.value = true;
};

const handleBatchSuccess = () => {
  selectedMerchants.value = [];
  fetchData();
};

const handleExport = () => {
  ElMessage.success('导出功能开发中');
};

const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
  fetchData();
};

const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  fetchData();
};

// 初始化
onMounted(() => {
  fetchData();
  initCharts();
});
</script>

<style scoped>
.merchant-analytics {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.stat-item :deep(.el-statistic__number) {
  font-size: 28px;
  font-weight: bold;
}

.stat-icon {
  font-size: 24px;
  margin-left: 8px;
}

.stat-icon.success {
  color: #67C23A;
}

.stat-icon.warning {
  color: #E6A23C;
}

.stat-icon.info {
  color: #909399;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 380px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-card {
  margin-bottom: 20px;
}

.revenue-text {
  color: #67C23A;
  font-weight: bold;
}

.commission-text {
  color: #E6A23C;
  font-weight: bold;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: right;
}
</style>