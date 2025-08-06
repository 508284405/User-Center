<template>
  <div class="debug-info-panel">
    <el-card class="debug-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="title">
            <el-icon><Monitor /></el-icon>
            RAG调试信息
          </span>
          <div class="header-actions">
            <el-tooltip content="清除调试信息">
              <el-button 
                type="info" 
                size="small" 
                :icon="Delete"
                @click="handleClear"
              />
            </el-tooltip>
            <el-tooltip content="导出调试数据">
              <el-button 
                type="primary" 
                size="small" 
                :icon="Download"
                @click="handleExport"
              />
            </el-tooltip>
            <el-switch
              v-model="autoUpdate"
              active-text="自动更新"
              inactive-text=""
              size="small"
              class="auto-update-switch"
            />
          </div>
        </div>
      </template>

      <!-- 无调试信息状态 -->
      <el-empty 
        v-if="!ragStore.hasDebugInfo"
        description="暂无调试信息"
        :image-size="80"
      >
        <template #description>
          <p>发送消息后将显示RAG组件的详细调试信息</p>
        </template>
      </el-empty>

      <!-- 调试信息内容 -->
      <div v-else class="debug-content">
        <!-- 配置信息卡片 -->
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="section-title">
              <el-icon><Setting /></el-icon>
              <span>使用的配置</span>
              <el-tag size="small" class="timestamp-tag">
                {{ formatTimestamp(ragStore.debugInfo.timestamp) }}
              </el-tag>
            </div>
          </template>
          
          <div class="config-grid">
            <div class="config-item">
              <h4>内容聚合器</h4>
              <ul>
                <li>最大结果数: {{ ragStore.debugInfo.configUsed.contentAggregator.maxResults }}</li>
                <li>最小分数: {{ ragStore.debugInfo.configUsed.contentAggregator.minScore }}</li>
              </ul>
            </div>
            
            <div class="config-item">
              <h4>查询转换器</h4>
              <ul>
                <li>扩展数量: {{ ragStore.debugInfo.configUsed.queryTransformer.n }}</li>
              </ul>
            </div>
            
            <div class="config-item">
              <h4>查询路由器</h4>
              <ul>
                <li>
                  网络搜索: 
                  <el-tag 
                    :type="ragStore.debugInfo.configUsed.queryRouter.webSearchEnabled ? 'success' : 'info'" 
                    size="small"
                  >
                    {{ ragStore.debugInfo.configUsed.queryRouter.webSearchEnabled ? '启用' : '禁用' }}
                  </el-tag>
                </li>
                <li>
                  知识库搜索: 
                  <el-tag 
                    :type="ragStore.debugInfo.configUsed.queryRouter.knowledgeSearchEnabled ? 'success' : 'info'" 
                    size="small"
                  >
                    {{ ragStore.debugInfo.configUsed.queryRouter.knowledgeSearchEnabled ? '启用' : '禁用' }}
                  </el-tag>
                </li>
              </ul>
            </div>
            
            <div v-if="ragStore.debugInfo.configUsed.queryRouter.webSearchEnabled" class="config-item">
              <h4>网络搜索</h4>
              <ul>
                <li>最大结果: {{ ragStore.debugInfo.configUsed.webSearch.maxResults }}</li>
                <li>超时时间: {{ ragStore.debugInfo.configUsed.webSearch.timeout }}秒</li>
              </ul>
            </div>
            
            <div v-if="ragStore.debugInfo.configUsed.queryRouter.knowledgeSearchEnabled" class="config-item">
              <h4>知识库搜索</h4>
              <ul>
                <li>Top K: {{ ragStore.debugInfo.configUsed.knowledgeSearch.topK }}</li>
                <li>分数阈值: {{ ragStore.debugInfo.configUsed.knowledgeSearch.scoreThreshold }}</li>
              </ul>
            </div>
          </div>
        </el-card>

        <!-- 检索统计卡片 -->
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="section-title">
              <el-icon><DataAnalysis /></el-icon>
              <span>检索统计</span>
            </div>
          </template>
          
          <div class="stats-container">
            <div class="stats-grid">
              <div class="stat-item">
                <div class="stat-value">{{ ragStore.debugInfo.retrievalStats.totalDocuments }}</div>
                <div class="stat-label">总文档数</div>
              </div>
              
              <div class="stat-item">
                <div class="stat-value">{{ ragStore.debugInfo.retrievalStats.webSearchResults }}</div>
                <div class="stat-label">网络搜索结果</div>
              </div>
              
              <div class="stat-item">
                <div class="stat-value">{{ ragStore.debugInfo.retrievalStats.knowledgeSearchResults }}</div>
                <div class="stat-label">知识库搜索结果</div>
              </div>
              
              <div class="stat-item highlight">
                <div class="stat-value">{{ ragStore.debugInfo.retrievalStats.finalResults }}</div>
                <div class="stat-label">最终结果数</div>
              </div>
            </div>

            <!-- 结果分布图表 -->
            <div class="chart-container">
              <div class="chart-title">结果分布</div>
              <div class="result-chart">
                <div class="chart-bar">
                  <div class="bar-segment web-search" :style="webSearchWidth"></div>
                  <div class="bar-segment knowledge-search" :style="knowledgeSearchWidth"></div>
                  <div class="bar-segment filtered" :style="filteredWidth"></div>
                </div>
                <div class="chart-legend">
                  <div class="legend-item">
                    <div class="legend-color web-search"></div>
                    <span>网络搜索 ({{ ragStore.debugInfo.retrievalStats.webSearchResults }})</span>
                  </div>
                  <div class="legend-item">
                    <div class="legend-color knowledge-search"></div>
                    <span>知识库搜索 ({{ ragStore.debugInfo.retrievalStats.knowledgeSearchResults }})</span>
                  </div>
                  <div class="legend-item">
                    <div class="legend-color filtered"></div>
                    <span>过滤后结果 ({{ ragStore.debugInfo.retrievalStats.finalResults }})</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 性能指标卡片 -->
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="section-title">
              <el-icon><Timer /></el-icon>
              <span>性能指标</span>
            </div>
          </template>
          
          <div class="performance-container">
            <div class="performance-grid">
              <div class="perf-item">
                <div class="perf-icon">
                  <el-icon><Clock /></el-icon>
                </div>
                <div class="perf-info">
                  <div class="perf-value">{{ ragStore.debugInfo.performanceMetrics.totalResponseTime }}ms</div>
                  <div class="perf-label">总响应时间</div>
                </div>
              </div>
              
              <div class="perf-item">
                <div class="perf-icon">
                  <el-icon><Search /></el-icon>
                </div>
                <div class="perf-info">
                  <div class="perf-value">{{ ragStore.debugInfo.performanceMetrics.retrievalTime }}ms</div>
                  <div class="perf-label">检索时间</div>
                </div>
              </div>
              
              <div class="perf-item">
                <div class="perf-icon">
                  <el-icon><Connection /></el-icon>
                </div>
                <div class="perf-info">
                  <div class="perf-value">{{ ragStore.debugInfo.performanceMetrics.aggregationTime }}ms</div>
                  <div class="perf-label">聚合时间</div>
                </div>
              </div>
            </div>

            <!-- 性能分析 -->
            <div class="performance-analysis">
              <div class="analysis-title">性能分析</div>
              <div class="analysis-content">
                <el-tag 
                  :type="getPerformanceRating(ragStore.debugInfo.performanceMetrics.totalResponseTime).type"
                  class="performance-tag"
                >
                  {{ getPerformanceRating(ragStore.debugInfo.performanceMetrics.totalResponseTime).label }}
                </el-tag>
                <span class="analysis-text">
                  {{ getPerformanceAnalysis() }}
                </span>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 历史记录 -->
        <el-card v-if="debugHistory.length > 0" class="info-card" shadow="never">
          <template #header>
            <div class="section-title">
              <el-icon><Document /></el-icon>
              <span>历史记录</span>
              <el-button type="text" size="small" @click="clearHistory">
                清除历史
              </el-button>
            </div>
          </template>
          
          <div class="history-container">
            <div class="history-list">
              <div 
                v-for="(item, index) in recentHistory"
                :key="index"
                class="history-item"
                @click="loadHistoryItem(item)"
              >
                <div class="history-time">{{ formatTimestamp(item.timestamp) }}</div>
                <div class="history-summary">
                  检索: {{ item.retrievalStats.finalResults }}个结果 | 
                  耗时: {{ item.performanceMetrics.totalResponseTime }}ms
                </div>
              </div>
            </div>
            
            <div v-if="debugHistory.length > 5" class="history-more">
              <el-button type="text" size="small" @click="showAllHistory = !showAllHistory">
                {{ showAllHistory ? '收起' : `查看全部 ${debugHistory.length} 条记录` }}
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Monitor,
  Delete,
  Download,
  Setting,
  DataAnalysis,
  Timer,
  Clock,
  Search,
  Connection,
  Document
} from '@element-plus/icons-vue';
import { useRagConfigStore } from '@/stores/ragConfig';
import { RagDebugInfo } from '@/types/chat';

// Store
const ragStore = useRagConfigStore();

// 本地状态
const autoUpdate = ref(true);
const debugHistory = ref<RagDebugInfo[]>([]);
const showAllHistory = ref(false);

// 计算属性
const webSearchWidth = computed(() => {
  if (!ragStore.debugInfo) return { width: '0%' };
  const total = ragStore.debugInfo.retrievalStats.totalDocuments || 1;
  const width = (ragStore.debugInfo.retrievalStats.webSearchResults / total) * 100;
  return { width: `${width}%` };
});

const knowledgeSearchWidth = computed(() => {
  if (!ragStore.debugInfo) return { width: '0%' };
  const total = ragStore.debugInfo.retrievalStats.totalDocuments || 1;
  const width = (ragStore.debugInfo.retrievalStats.knowledgeSearchResults / total) * 100;
  return { width: `${width}%` };
});

const filteredWidth = computed(() => {
  if (!ragStore.debugInfo) return { width: '0%' };
  const total = ragStore.debugInfo.retrievalStats.totalDocuments || 1;
  const width = (ragStore.debugInfo.retrievalStats.finalResults / total) * 100;
  return { width: `${width}%` };
});

const recentHistory = computed(() => {
  return showAllHistory.value ? debugHistory.value : debugHistory.value.slice(0, 5);
});

// 方法

/**
 * 格式化时间戳
 */
const formatTimestamp = (timestamp: number): string => {
  return new Date(timestamp).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};

/**
 * 获取性能评级
 */
const getPerformanceRating = (responseTime: number) => {
  if (responseTime < 1000) {
    return { type: 'success', label: '优秀' };
  } else if (responseTime < 3000) {
    return { type: 'warning', label: '良好' };
  } else {
    return { type: 'danger', label: '需要优化' };
  }
};

/**
 * 获取性能分析文本
 */
const getPerformanceAnalysis = (): string => {
  if (!ragStore.debugInfo) return '';
  
  const metrics = ragStore.debugInfo.performanceMetrics;
  const total = metrics.totalResponseTime;
  const retrieval = metrics.retrievalTime;
  const aggregation = metrics.aggregationTime;
  
  if (retrieval / total > 0.8) {
    return '检索阶段耗时较长，考虑优化查询条件或增加缓存';
  } else if (aggregation / total > 0.5) {
    return '聚合阶段耗时较长，考虑减少结果数量或优化聚合算法';
  } else if (total < 1000) {
    return '响应时间良好，系统运行正常';
  } else {
    return '整体响应时间偏长，建议检查网络连接和服务器性能';
  }
};

/**
 * 清除调试信息
 */
const handleClear = () => {
  ElMessageBox.confirm(
    '确定要清除当前的调试信息吗？',
    '确认清除',
    {
      type: 'warning'
    }
  ).then(() => {
    ragStore.clearDebugInfo();
    ElMessage.success('调试信息已清除');
  }).catch(() => {
    // 用户取消
  });
};

/**
 * 导出调试数据
 */
const handleExport = () => {
  if (!ragStore.debugInfo) {
    ElMessage.warning('无调试信息可导出');
    return;
  }
  
  try {
    const exportData = {
      current: ragStore.debugInfo,
      history: debugHistory.value,
      exportTime: Date.now()
    };
    
    const blob = new Blob([JSON.stringify(exportData, null, 2)], { 
      type: 'application/json' 
    });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `rag-debug-${new Date().toISOString().slice(0, 10)}.json`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    
    ElMessage.success('调试数据已导出');
  } catch (error) {
    console.error('导出调试数据失败:', error);
    ElMessage.error('导出调试数据失败');
  }
};

/**
 * 清除历史记录
 */
const clearHistory = () => {
  ElMessageBox.confirm(
    '确定要清除所有历史记录吗？',
    '确认清除',
    {
      type: 'warning'
    }
  ).then(() => {
    debugHistory.value = [];
    ElMessage.success('历史记录已清除');
  }).catch(() => {
    // 用户取消
  });
};

/**
 * 加载历史项目
 */
const loadHistoryItem = (item: RagDebugInfo) => {
  ragStore.setDebugInfo(item);
  ElMessage.info('已加载历史调试信息');
};

// 监听调试信息变化，自动添加到历史记录
watch(() => ragStore.debugInfo, (newInfo) => {
  if (newInfo && autoUpdate.value) {
    // 避免重复添加相同的调试信息
    const exists = debugHistory.value.some(item => 
      item.timestamp === newInfo.timestamp
    );
    
    if (!exists) {
      debugHistory.value.unshift(newInfo);
      
      // 限制历史记录数量
      if (debugHistory.value.length > 20) {
        debugHistory.value = debugHistory.value.slice(0, 20);
      }
    }
  }
}, { deep: true });
</script>

<style scoped lang="scss">
.debug-info-panel {
  .debug-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: bold;
        font-size: 16px;
      }
      
      .header-actions {
        display: flex;
        align-items: center;
        gap: 12px;
        
        .auto-update-switch {
          margin-left: 8px;
        }
      }
    }
  }
  
  .debug-content {
    .info-card {
      margin-bottom: 16px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .section-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: 600;
        
        .timestamp-tag {
          margin-left: auto;
        }
      }
    }
  }
  
  // 配置网格
  .config-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 16px;
    
    .config-item {
      h4 {
        margin: 0 0 8px 0;
        font-size: 14px;
        font-weight: 600;
        color: var(--el-text-color-primary);
      }
      
      ul {
        margin: 0;
        padding: 0;
        list-style: none;
        
        li {
          margin: 4px 0;
          font-size: 13px;
          color: var(--el-text-color-regular);
          display: flex;
          align-items: center;
          gap: 8px;
        }
      }
    }
  }
  
  // 统计容器
  .stats-container {
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 16px;
      margin-bottom: 24px;
      
      .stat-item {
        text-align: center;
        padding: 16px;
        background: var(--el-bg-color-page);
        border-radius: 8px;
        
        &.highlight {
          background: var(--el-color-primary-light-9);
          border: 1px solid var(--el-color-primary-light-7);
        }
        
        .stat-value {
          font-size: 24px;
          font-weight: bold;
          color: var(--el-color-primary);
          margin-bottom: 4px;
        }
        
        .stat-label {
          font-size: 12px;
          color: var(--el-text-color-secondary);
        }
      }
    }
    
    .chart-container {
      .chart-title {
        font-size: 14px;
        font-weight: 600;
        margin-bottom: 12px;
      }
      
      .result-chart {
        .chart-bar {
          height: 20px;
          background: var(--el-border-color-lighter);
          border-radius: 10px;
          overflow: hidden;
          display: flex;
          margin-bottom: 12px;
          
          .bar-segment {
            height: 100%;
            
            &.web-search {
              background: var(--el-color-success);
            }
            
            &.knowledge-search {
              background: var(--el-color-primary);
            }
            
            &.filtered {
              background: var(--el-color-warning);
            }
          }
        }
        
        .chart-legend {
          display: flex;
          flex-wrap: wrap;
          gap: 16px;
          
          .legend-item {
            display: flex;
            align-items: center;
            gap: 4px;
            font-size: 12px;
            
            .legend-color {
              width: 12px;
              height: 12px;
              border-radius: 2px;
              
              &.web-search {
                background: var(--el-color-success);
              }
              
              &.knowledge-search {
                background: var(--el-color-primary);
              }
              
              &.filtered {
                background: var(--el-color-warning);
              }
            }
          }
        }
      }
    }
  }
  
  // 性能容器
  .performance-container {
    .performance-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 16px;
      margin-bottom: 20px;
      
      .perf-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 16px;
        background: var(--el-bg-color-page);
        border-radius: 8px;
        
        .perf-icon {
          font-size: 24px;
          color: var(--el-color-primary);
        }
        
        .perf-info {
          .perf-value {
            font-size: 18px;
            font-weight: bold;
            color: var(--el-text-color-primary);
            margin-bottom: 2px;
          }
          
          .perf-label {
            font-size: 12px;
            color: var(--el-text-color-secondary);
          }
        }
      }
    }
    
    .performance-analysis {
      padding: 16px;
      background: var(--el-bg-color-page);
      border-radius: 8px;
      border-left: 4px solid var(--el-color-primary);
      
      .analysis-title {
        font-size: 14px;
        font-weight: 600;
        margin-bottom: 8px;
      }
      
      .analysis-content {
        display: flex;
        align-items: center;
        gap: 8px;
        
        .performance-tag {
          flex-shrink: 0;
        }
        
        .analysis-text {
          font-size: 13px;
          color: var(--el-text-color-regular);
        }
      }
    }
  }
  
  // 历史记录
  .history-container {
    .history-list {
      .history-item {
        padding: 12px;
        border: 1px solid var(--el-border-color-lighter);
        border-radius: 6px;
        margin-bottom: 8px;
        cursor: pointer;
        transition: all 0.3s ease;
        
        &:hover {
          border-color: var(--el-color-primary);
          background: var(--el-color-primary-light-9);
        }
        
        .history-time {
          font-size: 12px;
          color: var(--el-text-color-secondary);
          margin-bottom: 4px;
        }
        
        .history-summary {
          font-size: 13px;
          color: var(--el-text-color-regular);
        }
      }
    }
    
    .history-more {
      text-align: center;
      margin-top: 12px;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .debug-info-panel {
    .config-grid {
      grid-template-columns: 1fr;
    }
    
    .stats-container {
      .stats-grid {
        grid-template-columns: repeat(2, 1fr);
      }
    }
    
    .performance-container {
      .performance-grid {
        grid-template-columns: 1fr;
      }
    }
  }
}
</style>