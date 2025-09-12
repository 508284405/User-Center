<template>
  <el-dialog
    v-model="dialogVisible"
    :title="`${merchant?.merchantName || ''} - 详细分析`"
    width="1000px"
    :before-close="handleClose"
  >
    <div v-if="merchant" class="merchant-analytics-detail">
      <!-- 基本信息卡片 -->
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="info-card">
            <template #header>
              <div class="card-header">
                <el-icon><Shop /></el-icon>
                <span>基本信息</span>
              </div>
            </template>
            <div class="info-content">
              <div class="info-item">
                <span class="label">商家编号：</span>
                <span class="value">{{ merchant.merchantNo }}</span>
              </div>
              <div class="info-item">
                <span class="label">商家名称：</span>
                <span class="value">{{ merchant.merchantName }}</span>
              </div>
              <div class="info-item">
                <span class="label">店铺数量：</span>
                <span class="value">{{ merchant.totalShops }}</span>
              </div>
              <div class="info-item">
                <span class="label">商品数量：</span>
                <span class="value">{{ merchant.totalProducts }}</span>
              </div>
              <div class="info-item">
                <span class="label">活跃商品：</span>
                <span class="value">{{ merchant.activeProductCount }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card class="info-card">
            <template #header>
              <div class="card-header">
                <el-icon><Money /></el-icon>
                <span>收入统计</span>
              </div>
            </template>
            <div class="info-content">
              <div class="info-item">
                <span class="label">总收入：</span>
                <span class="value revenue">¥{{ merchant.totalRevenue?.toFixed(2) }}</span>
              </div>
              <div class="info-item">
                <span class="label">月收入：</span>
                <span class="value revenue">¥{{ merchant.monthlyRevenue?.toFixed(2) }}</span>
              </div>
              <div class="info-item">
                <span class="label">总佣金：</span>
                <span class="value commission">¥{{ merchant.totalCommission?.toFixed(2) }}</span>
              </div>
              <div class="info-item">
                <span class="label">月佣金：</span>
                <span class="value commission">¥{{ merchant.monthlyCommission?.toFixed(2) }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card class="info-card">
            <template #header>
              <div class="card-header">
                <el-icon><DataAnalysis /></el-icon>
                <span>业务指标</span>
              </div>
            </template>
            <div class="info-content">
              <div class="info-item">
                <span class="label">总订单：</span>
                <span class="value">{{ merchant.totalOrders }}</span>
              </div>
              <div class="info-item">
                <span class="label">月订单：</span>
                <span class="value">{{ merchant.monthlyOrders }}</span>
              </div>
              <div class="info-item">
                <span class="label">转化率：</span>
                <span class="value" :class="getConversionRateClass(merchant.conversionRate)">
                  {{ (merchant.conversionRate * 100).toFixed(2) }}%
                </span>
              </div>
              <div class="info-item">
                <span class="label">最后活跃：</span>
                <span class="value">{{ formatDate(merchant.lastLoginTime) }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="20" class="mt-4">
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <span>收入趋势 (最近30天)</span>
            </template>
            <div id="detailRevenueChart" style="height: 300px"></div>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <span>订单趋势 (最近30天)</span>
            </template>
            <div id="detailOrderChart" style="height: 300px"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 详细数据表格 -->
      <el-card class="table-card mt-4">
        <template #header>
          <div class="card-header">
            <span>详细数据</span>
            <el-button-group size="small">
              <el-button 
                :type="activeTab === 'revenue' ? 'primary' : ''"
                @click="activeTab = 'revenue'"
              >
                收入明细
              </el-button>
              <el-button 
                :type="activeTab === 'orders' ? 'primary' : ''"
                @click="activeTab = 'orders'"
              >
                订单明细
              </el-button>
            </el-button-group>
          </div>
        </template>
        
        <!-- 收入明细表 -->
        <el-table 
          v-if="activeTab === 'revenue'"
          :data="merchant.revenueTrend?.slice(-10) || []"
          stripe
          style="width: 100%"
        >
          <el-table-column prop="date" label="日期" width="120" />
          <el-table-column prop="revenue" label="收入" width="120">
            <template #default="{ row }">
              <span class="revenue-text">¥{{ row.revenue?.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="commission" label="佣金" width="120">
            <template #default="{ row }">
              <span class="commission-text">¥{{ row.commission?.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="佣金率" width="100">
            <template #default="{ row }">
              {{ ((row.commission / row.revenue) * 100).toFixed(2) }}%
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 订单明细表 -->
        <el-table 
          v-if="activeTab === 'orders'"
          :data="merchant.orderTrend?.slice(-10) || []"
          stripe
          style="width: 100%"
        >
          <el-table-column prop="date" label="日期" width="120" />
          <el-table-column prop="orderCount" label="订单总数" width="120" align="center" />
          <el-table-column prop="completedCount" label="完成订单" width="120" align="center" />
          <el-table-column label="完成率" width="100">
            <template #default="{ row }">
              <el-tag :type="getCompletionRateType(row.completedCount / row.orderCount)">
                {{ ((row.completedCount / row.orderCount) * 100).toFixed(1) }}%
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
    
    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <el-button type="primary" @click="handleExportReport">
        <el-icon><Download /></el-icon>
        导出报表
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import {
  Shop,
  Money,
  DataAnalysis,
  Download
} from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import type { MerchantAnalytics } from '@/api/client-web/merchant';

// Props
interface Props {
  modelValue: boolean;
  merchant: MerchantAnalytics | null;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  merchant: null
});

// Events
const emit = defineEmits<{
  'update:modelValue': [value: boolean];
}>();

// Data
const activeTab = ref<'revenue' | 'orders'>('revenue');
let detailRevenueChart: echarts.ECharts | null = null;
let detailOrderChart: echarts.ECharts | null = null;

// Computed
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

// Methods
const getConversionRateClass = (rate: number) => {
  if (rate >= 0.08) return 'high-rate';
  if (rate >= 0.05) return 'medium-rate';
  return 'low-rate';
};

const getCompletionRateType = (rate: number) => {
  if (rate >= 0.9) return 'success';
  if (rate >= 0.7) return 'warning';
  return 'danger';
};

const formatDate = (timestamp: number) => {
  if (!timestamp) return '未知';
  return new Date(timestamp).toLocaleDateString();
};

const initDetailCharts = async () => {
  if (!props.merchant) return;
  
  await nextTick();
  
  // 收入趋势图
  const revenueEl = document.getElementById('detailRevenueChart');
  if (revenueEl) {
    detailRevenueChart = echarts.init(revenueEl);
    updateDetailRevenueChart();
  }
  
  // 订单趋势图
  const orderEl = document.getElementById('detailOrderChart');
  if (orderEl) {
    detailOrderChart = echarts.init(orderEl);
    updateDetailOrderChart();
  }
};

const updateDetailRevenueChart = () => {
  if (!detailRevenueChart || !props.merchant?.revenueTrend) return;
  
  const data = props.merchant.revenueTrend;
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' },
      formatter: (params: any) => {
        let result = params[0].axisValue + '<br/>';
        params.forEach((item: any) => {
          result += `${item.seriesName}: ¥${item.value.toFixed(2)}<br/>`;
        });
        return result;
      }
    },
    legend: {
      data: ['收入', '佣金']
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.date),
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
        type: 'bar',
        data: data.map(item => item.revenue),
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '佣金',
        type: 'line',
        data: data.map(item => item.commission),
        smooth: true,
        itemStyle: { color: '#67C23A' }
      }
    ]
  };
  
  detailRevenueChart.setOption(option);
};

const updateDetailOrderChart = () => {
  if (!detailOrderChart || !props.merchant?.orderTrend) return;
  
  const data = props.merchant.orderTrend;
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: ['总订单', '完成订单']
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.date),
      axisLabel: {
        formatter: (value: string) => value.split('-').slice(1).join('/')
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '总订单',
        type: 'bar',
        data: data.map(item => item.orderCount),
        itemStyle: { color: '#E6A23C' }
      },
      {
        name: '完成订单',
        type: 'bar',
        data: data.map(item => item.completedCount),
        itemStyle: { color: '#67C23A' }
      }
    ]
  };
  
  detailOrderChart.setOption(option);
};

const handleClose = () => {
  emit('update:modelValue', false);
};

const handleExportReport = () => {
  ElMessage.success('导出功能开发中');
};

// Watch
watch(() => props.modelValue, (visible) => {
  if (visible && props.merchant) {
    initDetailCharts();
  }
});
</script>

<style scoped>
.merchant-analytics-detail {
  padding: 10px;
}

.info-card {
  height: 280px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
}

.info-content {
  padding: 10px 0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.label {
  font-weight: bold;
  color: #666;
}

.value {
  color: #333;
}

.value.revenue {
  color: #67C23A;
  font-weight: bold;
}

.value.commission {
  color: #E6A23C;
  font-weight: bold;
}

.high-rate {
  color: #67C23A;
  font-weight: bold;
}

.medium-rate {
  color: #E6A23C;
  font-weight: bold;
}

.low-rate {
  color: #F56C6C;
  font-weight: bold;
}

.chart-card {
  height: 380px;
}

.table-card {
  margin-top: 20px;
}

.revenue-text {
  color: #67C23A;
  font-weight: bold;
}

.commission-text {
  color: #E6A23C;
  font-weight: bold;
}
</style>