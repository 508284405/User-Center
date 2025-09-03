<template>
  <div class="marketing-dashboard">
    <div class="page-header">
      <h2>营销数据概览</h2>
      <div class="date-range-picker">
        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD HH:mm:ss"
          @change="handleDateRangeChange"
        />
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ stats.totalCampaigns }}</div>
            <div class="stat-label">总活动数</div>
          </div>
          <div class="stat-icon total-campaigns">
            <el-icon size="32"><TrendCharts /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ stats.activeCampaigns }}</div>
            <div class="stat-label">活跃活动</div>
          </div>
          <div class="stat-icon active-campaigns">
            <el-icon size="32"><Timer /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ formatNumber(stats.totalParticipants) }}</div>
            <div class="stat-label">总参与人数</div>
          </div>
          <div class="stat-icon participants">
            <el-icon size="32"><User /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">¥{{ formatMoney(stats.totalBudgetUsed) }}</div>
            <div class="stat-label">总预算消耗</div>
          </div>
          <div class="stat-icon budget">
            <el-icon size="32"><Money /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-section">
      <!-- 活动效果分析 -->
      <el-col :span="12">
        <el-card title="活动效果趋势" class="chart-card">
          <div ref="campaignTrendChart" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
      
      <!-- 活动类型分布 -->
      <el-col :span="12">
        <el-card title="活动类型分布" class="chart-card">
          <div ref="campaignTypeChart" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-section">
      <!-- 用户参与度分析 -->
      <el-col :span="16">
        <el-card title="用户参与度分析" class="chart-card">
          <div ref="userEngagementChart" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>
      
      <!-- 热门活动排行 -->
      <el-col :span="8">
        <el-card title="热门活动排行" class="chart-card">
          <div class="top-campaigns">
            <div 
              v-for="(campaign, index) in stats.topCampaigns" 
              :key="campaign.id"
              class="campaign-item"
            >
              <div class="rank">{{ index + 1 }}</div>
              <div class="campaign-info">
                <div class="campaign-name">{{ campaign.name }}</div>
                <div class="campaign-stats">
                  <span class="participants">{{ campaign.currentParticipants }}人参与</span>
                  <span class="budget">预算使用{{ Math.round((campaign.usedBudget || 0) / (campaign.budget || 1) * 100) }}%</span>
                </div>
              </div>
              <div class="campaign-type">
                <el-tag size="small" :type="getTypeTagType(campaign.type)">
                  {{ getTypeLabel(campaign.type) }}
                </el-tag>
              </div>
            </div>
            <div v-if="stats.topCampaigns.length === 0" class="no-data">
              暂无数据
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快速操作 -->
    <el-card title="快速操作" class="quick-actions">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-button type="primary" size="large" @click="createCampaign" style="width: 100%">
            <el-icon><Plus /></el-icon>
            创建营销活动
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="success" size="large" @click="createSeckill" style="width: 100%">
            <el-icon><Timer /></el-icon>
            创建秒杀活动
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="warning" size="large" @click="createPromotion" style="width: 100%">
            <el-icon><Discount /></el-icon>
            创建促销规则
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="info" size="large" @click="exportReport" style="width: 100%">
            <el-icon><Download /></el-icon>
            导出报表
          </el-button>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { TrendCharts, Timer, User, Money, Plus, Discount, Download } from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import { marketingStatsApi, type MarketingStats } from '@/api/marketing/promotion';

const router = useRouter();

// 响应式数据
const dateRange = ref<[string, string]>([
  new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().slice(0, 19).replace('T', ' '),
  new Date().toISOString().slice(0, 19).replace('T', ' ')
]);

const stats = reactive<MarketingStats>({
  totalCampaigns: 0,
  activeCampaigns: 0,
  totalParticipants: 0,
  totalBudgetUsed: 0,
  conversionRate: 0,
  topCampaigns: []
});

// ECharts 实例引用
const campaignTrendChart = ref();
const campaignTypeChart = ref();
const userEngagementChart = ref();

let campaignTrendChartInstance: echarts.ECharts | null = null;
let campaignTypeChartInstance: echarts.ECharts | null = null;
let userEngagementChartInstance: echarts.ECharts | null = null;

// 工具函数
const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万';
  }
  return num.toString();
};

const formatMoney = (amount: number): string => {
  return (amount / 100).toFixed(2);
};

const getTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    DISCOUNT: '折扣活动',
    POINTS_MULTIPLY: '积分翻倍',
    COUPON_RAIN: '优惠券雨',
    LUCKY_DRAW: '抽奖活动',
    LIMITED_TIME_OFFER: '限时优惠'
  };
  return labels[type] || type;
};

const getTypeTagType = (type: string) => {
  const types: Record<string, string> = {
    DISCOUNT: 'success',
    POINTS_MULTIPLY: 'warning',
    COUPON_RAIN: 'info',
    LUCKY_DRAW: 'danger',
    LIMITED_TIME_OFFER: 'primary'
  };
  return types[type] || '';
};

// 数据获取
const fetchStats = async () => {
  try {
    // Mock data - replace with actual API call
    const mockStats: MarketingStats = {
      totalCampaigns: 156,
      activeCampaigns: 12,
      totalParticipants: 28450,
      totalBudgetUsed: 1250000,
      conversionRate: 15.8,
      topCampaigns: [
        {
          id: 1,
          name: '春节积分翻倍活动',
          type: 'POINTS_MULTIPLY',
          status: 'ACTIVE',
          startTime: '2024-02-08 00:00:00',
          endTime: '2024-02-18 23:59:59',
          budget: 1000000,
          usedBudget: 750000,
          currentParticipants: 8500
        },
        {
          id: 2,
          name: '新用户优惠券雨',
          type: 'COUPON_RAIN',
          status: 'ACTIVE',
          startTime: '2024-01-01 00:00:00',
          endTime: '2024-03-31 23:59:59',
          budget: 500000,
          usedBudget: 120000,
          currentParticipants: 3200
        },
        {
          id: 3,
          name: '周年庆抽奖活动',
          type: 'LUCKY_DRAW',
          status: 'COMPLETED',
          startTime: '2024-06-01 00:00:00',
          endTime: '2024-06-07 23:59:59',
          budget: 2000000,
          usedBudget: 1800000,
          currentParticipants: 15000
        }
      ]
    };
    
    Object.assign(stats, mockStats);
    
    // 初始化图表
    await nextTick();
    initCharts();
    
  } catch (error) {
    ElMessage.error('获取统计数据失败');
    console.error(error);
  }
};

// 图表初始化
const initCharts = () => {
  initCampaignTrendChart();
  initCampaignTypeChart();
  initUserEngagementChart();
};

const initCampaignTrendChart = () => {
  if (campaignTrendChartInstance) {
    campaignTrendChartInstance.dispose();
  }
  
  campaignTrendChartInstance = echarts.init(campaignTrendChart.value);
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['活动数量', '参与人数', '预算消耗']
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月']
    },
    yAxis: [
      {
        type: 'value',
        name: '数量',
        position: 'left'
      },
      {
        type: 'value',
        name: '金额(万元)',
        position: 'right'
      }
    ],
    series: [
      {
        name: '活动数量',
        type: 'bar',
        data: [12, 18, 25, 22, 28, 35, 42]
      },
      {
        name: '参与人数',
        type: 'line',
        data: [1200, 1800, 2500, 2200, 2800, 3200, 3800]
      },
      {
        name: '预算消耗',
        type: 'line',
        yAxisIndex: 1,
        data: [5.2, 7.8, 12.5, 10.1, 15.6, 18.9, 22.3]
      }
    ]
  };
  
  campaignTrendChartInstance.setOption(option);
};

const initCampaignTypeChart = () => {
  if (campaignTypeChartInstance) {
    campaignTypeChartInstance.dispose();
  }
  
  campaignTypeChartInstance = echarts.init(campaignTypeChart.value);
  
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
        name: '活动类型',
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
        data: [
          { value: 35, name: '折扣活动' },
          { value: 28, name: '积分翻倍' },
          { value: 22, name: '优惠券雨' },
          { value: 18, name: '抽奖活动' },
          { value: 12, name: '限时优惠' }
        ]
      }
    ]
  };
  
  campaignTypeChartInstance.setOption(option);
};

const initUserEngagementChart = () => {
  if (userEngagementChartInstance) {
    userEngagementChartInstance.dispose();
  }
  
  userEngagementChartInstance = echarts.init(userEngagementChart.value);
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['新用户参与', '老用户参与', '转化率']
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: [
      {
        type: 'value',
        name: '人数',
        position: 'left'
      },
      {
        type: 'value',
        name: '转化率(%)',
        position: 'right',
        max: 100
      }
    ],
    series: [
      {
        name: '新用户参与',
        type: 'bar',
        stack: '参与人数',
        data: [320, 456, 645, 523, 789, 1024, 876]
      },
      {
        name: '老用户参与',
        type: 'bar',
        stack: '参与人数',
        data: [680, 744, 855, 677, 1011, 1276, 1124]
      },
      {
        name: '转化率',
        type: 'line',
        yAxisIndex: 1,
        data: [15.2, 18.6, 22.4, 19.8, 25.6, 28.9, 24.3]
      }
    ]
  };
  
  userEngagementChartInstance.setOption(option);
};

// 事件处理
const handleDateRangeChange = (range: [string, string] | null) => {
  if (range) {
    dateRange.value = range;
    fetchStats();
  }
};

const createCampaign = () => {
  router.push('/marketing/campaigns?action=create');
};

const createSeckill = () => {
  router.push('/dashboard/seckill/activities?action=create');
};

const createPromotion = () => {
  router.push('/marketing/promotions?action=create');
};

const exportReport = () => {
  ElMessage.info('导出功能开发中...');
};

// 窗口大小变化时重绘图表
const handleResize = () => {
  campaignTrendChartInstance?.resize();
  campaignTypeChartInstance?.resize();
  userEngagementChartInstance?.resize();
};

// 组件挂载
onMounted(async () => {
  await fetchStats();
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize);
});

// 组件卸载
onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  campaignTrendChartInstance?.dispose();
  campaignTypeChartInstance?.dispose();
  userEngagementChartInstance?.dispose();
});
</script>

<style scoped>
.marketing-dashboard {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  .stat-content {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
  }
  
  .stat-number {
    font-size: 32px;
    font-weight: bold;
    color: #333;
    line-height: 1;
  }
  
  .stat-label {
    font-size: 14px;
    color: #666;
    margin-top: 8px;
  }
  
  .el-card__body {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .stat-icon {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &.total-campaigns {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
    }
    
    &.active-campaigns {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      color: white;
    }
    
    &.participants {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      color: white;
    }
    
    &.budget {
      background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
      color: white;
    }
  }
}

.charts-section {
  margin-bottom: 20px;
}

.chart-card {
  height: 100%;
  
  :deep(.el-card__header) {
    padding: 18px 20px;
    border-bottom: 1px solid #f0f0f0;
  }
  
  :deep(.el-card__body) {
    padding: 20px;
  }
}

.top-campaigns {
  .campaign-item {
    display: flex;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    .rank {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: #f5f7fa;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: bold;
      color: #409eff;
      margin-right: 12px;
    }
    
    .campaign-info {
      flex: 1;
      
      .campaign-name {
        font-weight: 500;
        margin-bottom: 4px;
        font-size: 14px;
      }
      
      .campaign-stats {
        font-size: 12px;
        color: #666;
        
        .participants {
          margin-right: 8px;
        }
      }
    }
    
    .campaign-type {
      margin-left: 8px;
    }
  }
  
  .no-data {
    text-align: center;
    color: #999;
    padding: 40px 0;
  }
}

.quick-actions {
  :deep(.el-card__header) {
    padding: 18px 20px;
    border-bottom: 1px solid #f0f0f0;
  }
  
  :deep(.el-card__body) {
    padding: 20px;
  }
}
</style>