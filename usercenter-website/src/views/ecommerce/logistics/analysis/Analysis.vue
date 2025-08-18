<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElTabs, ElTabPane } from 'element-plus'
import Statistics from './Statistics.vue'
import Exceptions from './Exceptions.vue'
import Sla from './Sla.vue'

// 激活的标签页
const activeTab = ref('statistics')

// 加载状态
const loading = reactive({
  statistics: false,
  exceptions: false,
  sla: false
})

// 更新加载状态
const updateLoading = (tab: string, status: boolean) => {
  if (tab in loading) {
    loading[tab as keyof typeof loading] = status
  }
}
</script>

<template>
  <div class="logistics-analysis">
    <h2 class="page-title">物流统计分析</h2>
    
    <el-tabs v-model="activeTab" type="card">
      <el-tab-pane label="统计概览" name="statistics">
        <Statistics @loading-change="status => updateLoading('statistics', status)" />
      </el-tab-pane>
      
      <el-tab-pane label="异常监控" name="exceptions">
        <Exceptions @loading-change="status => updateLoading('exceptions', status)" />
      </el-tab-pane>
      
      <el-tab-pane label="SLA监控" name="sla">
        <Sla @loading-change="status => updateLoading('sla', status)" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.logistics-analysis {
  background-color: #f0f2f5;
  border-radius: 4px;
}

.page-title {
  margin-top: 0;
  margin-bottom: 24px;
  font-size: 20px;
  font-weight: 500;
}
</style> 