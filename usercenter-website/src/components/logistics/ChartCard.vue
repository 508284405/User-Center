<script setup lang="ts">
import { ref, onMounted, defineProps, defineExpose, watch } from 'vue'
import * as echarts from 'echarts/core'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

// 注册必要的组件
echarts.use([
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  BarChart,
  LineChart,
  PieChart,
  CanvasRenderer
])

// 定义组件属性
const props = defineProps<{
  title: string;
  loading?: boolean;
  height?: string | number;
  options?: any; // ECharts 配置选项
  summary?: {
    label: string;
    value: string | number;
    unit?: string;
    trend?: 'up' | 'down' | 'flat';
    color?: string;
  }[]
}>()

// 图表DOM引用
const chartRef = ref<HTMLDivElement | null>(null)
let chartInstance: echarts.ECharts | null = null

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return
  
  // 销毁旧实例
  if (chartInstance) {
    chartInstance.dispose()
  }
  
  // 创建新实例
  chartInstance = echarts.init(chartRef.value)
  
  // 更新图表
  updateChart()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
}

// 更新图表
const updateChart = () => {
  if (chartInstance && props.options) {
    chartInstance.setOption(props.options)
  }
}

// 处理大小变化
const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

// 监听选项变化
watch(() => props.options, () => {
  updateChart()
}, { deep: true })

// 在组件卸载时清理
const dispose = () => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  window.removeEventListener('resize', handleResize)
}

// 暴露方法
defineExpose({
  getChartInstance: () => chartInstance,
  resize: handleResize,
  dispose
})

// 组件挂载后初始化
onMounted(() => {
  initChart()
})
</script>

<template>
  <div class="chart-card">
    <div class="chart-header">
      <h3 class="chart-title">{{ title }}</h3>
      <slot name="actions"></slot>
    </div>
    
    <div class="chart-content" v-loading="loading">
      <!-- 摘要信息 -->
      <div v-if="summary && summary.length" class="summary-container">
        <div 
          v-for="(item, index) in summary" 
          :key="index" 
          class="summary-item"
          :style="{ color: item.color }"
        >
          <div class="summary-label">{{ item.label }}</div>
          <div class="summary-value">
            {{ item.value }}
            <span v-if="item.unit" class="summary-unit">{{ item.unit }}</span>
            <i 
              v-if="item.trend" 
              :class="[
                'trend-icon', 
                `trend-${item.trend}`,
                item.trend === 'up' ? 'el-icon-caret-top' : (item.trend === 'down' ? 'el-icon-caret-bottom' : 'el-icon-minus')
              ]"
            ></i>
          </div>
        </div>
      </div>
      
      <!-- 图表 -->
      <div 
        ref="chartRef" 
        class="chart-container"
        :style="{ height: height || '300px' }"
      ></div>
    </div>
  </div>
</template>

<style scoped>
.chart-card {
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  overflow: hidden;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.chart-title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.chart-content {
  padding: 16px;
  position: relative;
}

.summary-container {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.summary-item {
  flex: 1;
  min-width: 120px;
  padding: 12px;
  text-align: center;
}

.summary-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.summary-value {
  font-size: 24px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
}

.summary-unit {
  font-size: 14px;
  margin-left: 4px;
  font-weight: normal;
}

.trend-icon {
  margin-left: 4px;
  font-size: 16px;
}

.trend-up {
  color: #f56c6c;
}

.trend-down {
  color: #67c23a;
}

.trend-flat {
  color: #909399;
}

.chart-container {
  width: 100%;
}
</style> 