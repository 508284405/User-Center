<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    title="审核记录详情"
    width="800px"
    :close-on-click-modal="false"
  >
    <div class="record-detail" v-if="record">
      <!-- 基本信息 -->
      <el-card class="info-card">
        <template #header>
          <span>基本信息</span>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="记录ID">{{ record.id }}</el-descriptions-item>
          <el-descriptions-item label="内容类型">
            <el-tag :type="getContentTypeTag(record.contentType)" size="small">
              {{ getContentTypeLabel(record.contentType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="来源类型">
            <el-tag :type="getSourceTypeTag(record.sourceType)" size="small">
              {{ getSourceTypeLabel(record.sourceType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审核结果">
            <el-tag :type="getResultTagType(record.moderationResult)" size="small">
              {{ getResultLabel(record.moderationResult) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="风险等级" v-if="record.riskLevel">
            <el-tag :type="getRiskTagType(record.riskLevel)" size="small">
              {{ getRiskLabel(record.riskLevel) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="置信度" v-if="record.confidenceScore">
            {{ (record.confidenceScore * 100).toFixed(1) }}%
          </el-descriptions-item>
          <el-descriptions-item label="是否阻断">
            <el-tag :type="record.isBlocked ? 'danger' : 'success'" size="small">
              {{ record.isBlocked ? '已阻断' : '未阻断' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理时间" v-if="record.processingTimeMs">
            {{ formatProcessingTime(record.processingTimeMs) }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 用户信息 -->
      <el-card class="info-card" v-if="record.userId || record.sessionId || record.clientIp">
        <template #header>
          <span>用户信息</span>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户ID" v-if="record.userId">{{ record.userId }}</el-descriptions-item>
          <el-descriptions-item label="会话ID" v-if="record.sessionId">{{ record.sessionId }}</el-descriptions-item>
          <el-descriptions-item label="客户端IP" v-if="record.clientIp">{{ record.clientIp }}</el-descriptions-item>
          <el-descriptions-item label="请求ID" v-if="record.requestId">{{ record.requestId }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 原始内容 -->
      <el-card class="info-card">
        <template #header>
          <span>原始内容</span>
          <div class="header-actions">
            <el-button type="text" size="small" @click="copyContent">
              <el-icon><DocumentCopy /></el-icon>
              复制
            </el-button>
          </div>
        </template>
        <div class="content-display">
          <pre class="content-text">{{ record.originalContent }}</pre>
        </div>
      </el-card>

      <!-- 违规详情 -->
      <el-card class="info-card" v-if="record.violationCategories?.length">
        <template #header>
          <span>违规详情</span>
        </template>
        <div class="violations-list">
          <div 
            v-for="(violation, index) in record.violationCategories" 
            :key="index"
            class="violation-item"
          >
            <div class="violation-header">
              <el-tag type="danger" size="small">{{ violation.categoryName }}</el-tag>
              <span class="confidence">置信度: {{ (violation.confidence * 100).toFixed(1) }}%</span>
            </div>
            <div class="violation-details">
              <span class="code">编码: {{ violation.categoryCode }}</span>
              <span class="rule" v-if="violation.triggerRule">触发规则: {{ violation.triggerRule }}</span>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 关键词匹配 -->
      <el-card class="info-card" v-if="record.keywordMatches?.length">
        <template #header>
          <span>关键词匹配</span>
        </template>
        <div class="keywords-list">
          <el-tag 
            v-for="keyword in record.keywordMatches" 
            :key="keyword"
            type="warning" 
            size="small"
            class="keyword-tag"
          >
            {{ keyword }}
          </el-tag>
        </div>
      </el-card>

      <!-- 审核技术详情 -->
      <el-card class="info-card" v-if="record.moderationMethods || record.aiModelUsed">
        <template #header>
          <span>技术详情</span>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="审核方法" v-if="record.moderationMethods">
            {{ record.moderationMethods }}
          </el-descriptions-item>
          <el-descriptions-item label="AI模型" v-if="record.aiModelUsed">
            {{ record.aiModelUsed }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 人工审核信息 -->
      <el-card class="info-card" v-if="record.manualReviewStatus">
        <template #header>
          <span>人工审核</span>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="审核状态">{{ record.manualReviewStatus }}</el-descriptions-item>
          <el-descriptions-item label="审核员" v-if="record.manualReviewerId">
            {{ record.manualReviewerId }}
          </el-descriptions-item>
          <el-descriptions-item label="审核时间" v-if="record.manualReviewedAt" :span="2">
            {{ formatDateTime(record.manualReviewedAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="审核备注" v-if="record.manualReviewNotes" :span="2">
            <div class="review-notes">{{ record.manualReviewNotes }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 时间信息 -->
      <el-card class="info-card">
        <template #header>
          <span>时间信息</span>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="创建时间" v-if="record.createdAt">
            {{ formatDateTime(record.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间" v-if="record.updatedAt">
            {{ formatDateTime(record.updatedAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="$emit('update:modelValue', false)">关闭</el-button>
        <el-button 
          v-if="record?.moderationResult === 'NEEDS_REVIEW'" 
          type="primary" 
          @click="$emit('review', record)"
        >
          人工审核
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { DocumentCopy } from '@element-plus/icons-vue'
import { type ModerationRecord } from '@/api/smartcs/moderation'
import { formatDateTime } from '@/utils/dateFormat'

// Props 和 Emits
interface Props {
  modelValue: boolean
  record: ModerationRecord | null
}

defineProps<Props>()
defineEmits<{
  'update:modelValue': [value: boolean]
  'review': [record: ModerationRecord]
}>()

// 方法定义
const copyContent = () => {
  if (!props.record?.originalContent) return
  
  navigator.clipboard.writeText(props.record.originalContent)
    .then(() => {
      ElMessage.success('内容已复制到剪贴板')
    })
    .catch(() => {
      ElMessage.error('复制失败')
    })
}

const formatProcessingTime = (timeMs: number) => {
  if (timeMs < 1000) {
    return `${timeMs}ms`
  } else {
    return `${(timeMs / 1000).toFixed(1)}s`
  }
}

// 标签类型和标签文本辅助函数
const getContentTypeTag = (type: string) => {
  const tags: Record<string, string> = {
    MESSAGE: 'primary',
    KNOWLEDGE: 'success',
    DOCUMENT: 'info',
    FAQ: 'warning',
    RAG_QUERY: 'danger'
  }
  return tags[type] || ''
}

const getContentTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    MESSAGE: '消息',
    KNOWLEDGE: '知识库',
    DOCUMENT: '文档',
    FAQ: 'FAQ',
    RAG_QUERY: 'RAG查询'
  }
  return labels[type] || type
}

const getSourceTypeTag = (type: string) => {
  const tags: Record<string, string> = {
    CHAT: 'primary',
    KNOWLEDGE_BASE: 'success',
    RAG_QUERY: 'info',
    FILE_UPLOAD: 'warning'
  }
  return tags[type] || ''
}

const getSourceTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    CHAT: '聊天',
    KNOWLEDGE_BASE: '知识库',
    RAG_QUERY: 'RAG查询',
    FILE_UPLOAD: '文件上传'
  }
  return labels[type] || type
}

const getResultTagType = (result: string) => {
  const tags: Record<string, string> = {
    APPROVED: 'success',
    REJECTED: 'danger',
    NEEDS_REVIEW: 'warning',
    PENDING: 'info'
  }
  return tags[result] || ''
}

const getResultLabel = (result: string) => {
  const labels: Record<string, string> = {
    APPROVED: '通过',
    REJECTED: '拒绝',
    NEEDS_REVIEW: '需审核',
    PENDING: '处理中'
  }
  return labels[result] || result
}

const getRiskTagType = (risk: string) => {
  const tags: Record<string, string> = {
    LOW: 'success',
    MEDIUM: 'warning',
    HIGH: 'danger',
    CRITICAL: 'danger'
  }
  return tags[risk] || ''
}

const getRiskLabel = (risk: string) => {
  const labels: Record<string, string> = {
    LOW: '低',
    MEDIUM: '中',
    HIGH: '高',
    CRITICAL: '极高'
  }
  return labels[risk] || risk
}
</script>

<style scoped lang="scss">
.record-detail {
  .info-card {
    margin-bottom: 16px;
    
    &:last-child {
      margin-bottom: 0;
    }

    .header-actions {
      display: flex;
      gap: 8px;
    }
  }

  .content-display {
    max-height: 300px;
    overflow-y: auto;
    
    .content-text {
      margin: 0;
      padding: 12px;
      background-color: #f5f7fa;
      border-radius: 4px;
      font-family: 'Courier New', monospace;
      white-space: pre-wrap;
      word-wrap: break-word;
      line-height: 1.5;
    }
  }

  .violations-list {
    .violation-item {
      padding: 12px;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      margin-bottom: 12px;
      
      &:last-child {
        margin-bottom: 0;
      }

      .violation-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .confidence {
          font-size: 12px;
          color: #909399;
        }
      }

      .violation-details {
        display: flex;
        gap: 16px;
        font-size: 12px;
        color: #606266;

        .code, .rule {
          flex: 1;
        }
      }
    }
  }

  .keywords-list {
    .keyword-tag {
      margin: 4px 8px 4px 0;
    }
  }

  .review-notes {
    padding: 8px 12px;
    background-color: #f5f7fa;
    border-radius: 4px;
    line-height: 1.5;
    max-height: 100px;
    overflow-y: auto;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-card__header) {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

// 响应式设计
@media (max-width: 768px) {
  :deep(.el-descriptions) {
    .el-descriptions__body {
      .el-descriptions__table {
        display: block;
        
        .el-descriptions__row {
          display: block;
          
          .el-descriptions__cell {
            display: block;
            padding: 8px 0;
            border: none;
            border-bottom: 1px solid #ebeef5;
            
            &.is-bordered-label {
              font-weight: bold;
              background-color: #fafafa;
            }
          }
        }
      }
    }
  }
}
</style>