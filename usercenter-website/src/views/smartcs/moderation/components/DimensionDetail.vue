<template>
  <div class="dimension-detail">
    <div class="detail-header">
      <div class="dimension-info">
        <h3 class="dimension-name">{{ dimension.name }}</h3>
        <p class="dimension-code">{{ dimension.code }}</p>
        <div class="dimension-tags">
          <el-tag :type="getSeverityTagType(dimension.severityLevel)">
            {{ getSeverityText(dimension.severityLevel) }}
          </el-tag>
          <el-tag :type="getActionTagType(dimension.actionType)">
            {{ getActionText(dimension.actionType) }}
          </el-tag>
          <el-tag v-if="!dimension.isActive" type="info">已禁用</el-tag>
        </div>
      </div>
      <div class="dimension-status">
        <div class="status-item">
          <span class="status-label">状态</span>
          <el-tag :type="dimension.isActive ? 'success' : 'info'">
            {{ dimension.isActive ? '启用' : '禁用' }}
          </el-tag>
        </div>
        <div class="status-item">
          <span class="status-label">排序权重</span>
          <span class="status-value">{{ dimension.sortOrder }}</span>
        </div>
      </div>
    </div>

    <el-divider />

    <div class="detail-content">
      <el-row :gutter="24">
        <el-col :span="16">
          <div class="content-section">
            <h4 class="section-title">基本信息</h4>
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">维度名称：</span>
                <span class="info-value">{{ dimension.name }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">维度编码：</span>
                <span class="info-value code">{{ dimension.code }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">维度描述：</span>
                <span class="info-value">{{ dimension.description || '暂无描述' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">分类：</span>
                <span class="info-value">{{ dimension.category || '未分类' }}</span>
              </div>
            </div>
          </div>

          <div class="content-section">
            <h4 class="section-title">检查指南</h4>
            <div class="guideline-content">
              <div v-if="dimension.checkGuideline" class="guideline-text">
                {{ dimension.checkGuideline }}
              </div>
              <div v-else class="empty-guideline">
                <el-icon><Document /></el-icon>
                <span>暂无检查指南</span>
              </div>
            </div>
          </div>

          <div class="content-section">
            <h4 class="section-title">配置参数</h4>
            <div class="config-content">
              <div v-if="dimension.configParams && Object.keys(dimension.configParams).length > 0">
                <el-descriptions :column="2" border>
                  <el-descriptions-item
                    v-for="[key, value] in Object.entries(dimension.configParams)"
                    :key="key"
                    :label="key"
                  >
                    <span class="config-value">{{ formatConfigValue(value) }}</span>
                  </el-descriptions-item>
                </el-descriptions>
              </div>
              <div v-else class="empty-config">
                <el-icon><Setting /></el-icon>
                <span>暂无配置参数</span>
              </div>
            </div>
          </div>
        </el-col>

        <el-col :span="8">
          <div class="sidebar-content">
            <div class="content-section">
              <h4 class="section-title">处理配置</h4>
              <div class="config-items">
                <div class="config-item">
                  <div class="config-header">
                    <span class="config-label">风险级别</span>
                  </div>
                  <div class="config-value-display">
                    <el-tag :type="getSeverityTagType(dimension.severityLevel)" size="large">
                      {{ getSeverityText(dimension.severityLevel) }}
                    </el-tag>
                  </div>
                </div>

                <div class="config-item">
                  <div class="config-header">
                    <span class="config-label">处理动作</span>
                  </div>
                  <div class="config-value-display">
                    <el-tag :type="getActionTagType(dimension.actionType)" size="large">
                      {{ getActionText(dimension.actionType) }}
                    </el-tag>
                  </div>
                </div>

                <div class="config-item">
                  <div class="config-header">
                    <span class="config-label">置信阈值</span>
                    <el-tooltip content="当AI模型的置信度超过此阈值时，将触发对应的处理动作">
                      <el-icon><QuestionFilled /></el-icon>
                    </el-tooltip>
                  </div>
                  <div class="config-value-display">
                    <div class="threshold-display">
                      <div class="threshold-bar">
                        <div 
                          class="threshold-fill" 
                          :style="{ width: `${(dimension.confidenceThreshold || 0) * 100}%` }"
                        />
                      </div>
                      <span class="threshold-value">{{ dimension.confidenceThreshold }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="content-section">
              <h4 class="section-title">使用统计</h4>
              <div class="usage-stats">
                <div class="stat-item">
                  <div class="stat-icon">
                    <el-icon><DataLine /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-label">关联策略数</div>
                    <div class="stat-value">{{ usageStats.policyCount }}</div>
                  </div>
                </div>
                <div class="stat-item">
                  <div class="stat-icon">
                    <el-icon><Histogram /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-label">今日检查次数</div>
                    <div class="stat-value">{{ usageStats.todayChecks }}</div>
                  </div>
                </div>
                <div class="stat-item">
                  <div class="stat-icon">
                    <el-icon><TrendCharts /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-label">检出率</div>
                    <div class="stat-value">{{ usageStats.detectionRate }}%</div>
                  </div>
                </div>
              </div>
            </div>

            <div class="content-section">
              <h4 class="section-title">操作历史</h4>
              <div class="history-timeline">
                <el-timeline>
                  <el-timeline-item
                    v-for="record in operationHistory"
                    :key="record.id"
                    :timestamp="formatDate(record.timestamp)"
                    size="small"
                  >
                    <div class="timeline-content">
                      <div class="operation-type">{{ record.operation }}</div>
                      <div class="operation-user">by {{ record.user }}</div>
                    </div>
                  </el-timeline-item>
                </el-timeline>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-divider />

    <div class="detail-footer">
      <div class="footer-info">
        <div class="info-item">
          <span class="info-label">创建时间：</span>
          <span class="info-value">{{ formatDate(dimension.createdAt) }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">更新时间：</span>
          <span class="info-value">{{ formatDate(dimension.updatedAt) }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">创建者：</span>
          <span class="info-value">{{ dimension.createdBy || '系统' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">更新者：</span>
          <span class="info-value">{{ dimension.updatedBy || '系统' }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { 
  Document, Setting, QuestionFilled, DataLine, Histogram, TrendCharts
} from '@element-plus/icons-vue'
import type { ModerationDimension } from '@/api/smartcs/moderation'

interface Props {
  dimension: ModerationDimension
}

const props = defineProps<Props>()

// 模拟使用统计数据
const usageStats = ref({
  policyCount: 3,
  todayChecks: 1247,
  detectionRate: 12.5
})

// 模拟操作历史数据
const operationHistory = ref([
  {
    id: 1,
    operation: '更新配置参数',
    user: 'admin',
    timestamp: Date.now() - 2 * 60 * 60 * 1000
  },
  {
    id: 2,
    operation: '修改检查指南',
    user: 'admin',
    timestamp: Date.now() - 24 * 60 * 60 * 1000
  },
  {
    id: 3,
    operation: '创建维度',
    user: 'system',
    timestamp: Date.now() - 7 * 24 * 60 * 60 * 1000
  }
])

onMounted(() => {
  // 这里可以加载实际的使用统计和操作历史数据
})

// 工具函数
const getSeverityText = (severity?: string) => {
  const map: Record<string, string> = {
    'LOW': '低风险',
    'MEDIUM': '中风险',
    'HIGH': '高风险',
    'CRITICAL': '极高风险'
  }
  return map[severity || ''] || '未知'
}

const getSeverityTagType = (severity?: string) => {
  const map: Record<string, string> = {
    'LOW': 'info',
    'MEDIUM': '',
    'HIGH': 'warning',
    'CRITICAL': 'danger'
  }
  return map[severity || ''] || ''
}

const getActionText = (action?: string) => {
  const map: Record<string, string> = {
    'APPROVE': '通过',
    'REJECT': '拒绝',
    'MANUAL_REVIEW': '人工审核',
    'AUTO_FIX': '自动修复'
  }
  return map[action || ''] || '未知'
}

const getActionTagType = (action?: string) => {
  const map: Record<string, string> = {
    'APPROVE': 'success',
    'REJECT': 'danger',
    'MANUAL_REVIEW': 'warning',
    'AUTO_FIX': 'primary'
  }
  return map[action || ''] || ''
}

const formatConfigValue = (value: any) => {
  if (typeof value === 'object') {
    return JSON.stringify(value)
  }
  return String(value)
}

const formatDate = (timestamp: number | undefined) => {
  if (!timestamp) return '-'
  return new Date(timestamp).toLocaleString('zh-CN')
}
</script>

<style scoped lang="scss">
.dimension-detail {
  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;

    .dimension-info {
      flex: 1;

      .dimension-name {
        font-size: 20px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 8px 0;
      }

      .dimension-code {
        font-family: 'Courier New', monospace;
        color: #909399;
        font-size: 14px;
        margin: 0 0 12px 0;
      }

      .dimension-tags {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
      }
    }

    .dimension-status {
      display: flex;
      gap: 24px;

      .status-item {
        text-align: right;

        .status-label {
          display: block;
          color: #909399;
          font-size: 13px;
          margin-bottom: 4px;
        }

        .status-value {
          font-weight: 500;
          color: #303133;
        }
      }
    }
  }

  .detail-content {
    .content-section {
      margin-bottom: 24px;

      .section-title {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 16px 0;
        padding-bottom: 8px;
        border-bottom: 2px solid #e4e7ed;
      }

      .info-grid {
        display: grid;
        grid-template-columns: 1fr;
        gap: 12px;

        .info-item {
          display: flex;
          align-items: flex-start;

          .info-label {
            width: 100px;
            color: #606266;
            font-size: 14px;
            flex-shrink: 0;
          }

          .info-value {
            flex: 1;
            color: #303133;
            word-break: break-all;

            &.code {
              font-family: 'Courier New', monospace;
              background: #f5f7fa;
              padding: 2px 6px;
              border-radius: 4px;
            }
          }
        }
      }

      .guideline-content {
        .guideline-text {
          padding: 16px;
          background: #f8f9fa;
          border-radius: 6px;
          line-height: 1.6;
          color: #303133;
        }

        .empty-guideline {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
          padding: 40px;
          color: #909399;
          background: #fafafa;
          border-radius: 6px;
        }
      }

      .config-content {
        .config-value {
          font-family: 'Courier New', monospace;
          font-size: 13px;
        }

        .empty-config {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
          padding: 40px;
          color: #909399;
          background: #fafafa;
          border-radius: 6px;
        }
      }
    }

    .sidebar-content {
      .config-items {
        .config-item {
          padding: 16px;
          border: 1px solid #e4e7ed;
          border-radius: 6px;
          margin-bottom: 16px;

          .config-header {
            display: flex;
            align-items: center;
            gap: 4px;
            margin-bottom: 8px;

            .config-label {
              font-weight: 500;
              color: #303133;
            }

            .el-icon {
              color: #909399;
              cursor: help;
            }
          }

          .config-value-display {
            .threshold-display {
              display: flex;
              align-items: center;
              gap: 12px;

              .threshold-bar {
                flex: 1;
                height: 8px;
                background: #e4e7ed;
                border-radius: 4px;
                position: relative;

                .threshold-fill {
                  height: 100%;
                  background: linear-gradient(90deg, #409eff 0%, #67c23a 100%);
                  border-radius: 4px;
                  transition: width 0.3s ease;
                }
              }

              .threshold-value {
                font-weight: 600;
                color: #303133;
                min-width: 40px;
              }
            }
          }
        }
      }

      .usage-stats {
        .stat-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 12px 0;
          border-bottom: 1px solid #f0f0f0;

          &:last-child {
            border-bottom: none;
          }

          .stat-icon {
            width: 40px;
            height: 40px;
            background: #f0f8ff;
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #409eff;
          }

          .stat-info {
            flex: 1;

            .stat-label {
              color: #606266;
              font-size: 13px;
              margin-bottom: 2px;
            }

            .stat-value {
              font-size: 18px;
              font-weight: 600;
              color: #303133;
            }
          }
        }
      }

      .history-timeline {
        .timeline-content {
          .operation-type {
            font-weight: 500;
            color: #303133;
            margin-bottom: 2px;
          }

          .operation-user {
            color: #909399;
            font-size: 12px;
          }
        }
      }
    }
  }

  .detail-footer {
    .footer-info {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 16px;

      .info-item {
        display: flex;
        align-items: center;

        .info-label {
          color: #909399;
          font-size: 13px;
          margin-right: 8px;
        }

        .info-value {
          color: #606266;
          font-size: 13px;
        }
      }
    }
  }
}

// Element Plus 样式自定义
:deep(.el-descriptions__label) {
  font-weight: 500;
}

:deep(.el-timeline-item__timestamp) {
  color: #909399;
  font-size: 12px;
}
</style>