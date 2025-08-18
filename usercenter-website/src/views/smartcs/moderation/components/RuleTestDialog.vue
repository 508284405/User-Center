<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    title="规则测试"
    width="700px"
    :close-on-click-modal="false"
  >
    <div class="rule-test" v-if="rule">
      <!-- 规则信息 -->
      <el-card class="rule-info-card">
        <template #header>
          <span>规则信息</span>
        </template>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="规则名称">{{ rule.ruleName }}</el-descriptions-item>
          <el-descriptions-item label="关键词">
            <code class="keyword-code">{{ rule.keyword }}</code>
          </el-descriptions-item>
          <el-descriptions-item label="规则类型">
            <el-tag :type="getRuleTypeTag(rule.ruleType)" size="small">
              {{ getRuleTypeLabel(rule.ruleType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="匹配模式">
            {{ getMatchModeLabel(rule.matchMode) }}
          </el-descriptions-item>
          <el-descriptions-item label="大小写敏感">
            {{ rule.caseSensitive ? '是' : '否' }}
          </el-descriptions-item>
          <el-descriptions-item label="相似度阈值" v-if="rule.similarityThreshold">
            {{ rule.similarityThreshold }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 测试输入 -->
      <el-card class="test-input-card">
        <template #header>
          <span>测试内容</span>
          <div class="header-actions">
            <el-button type="text" size="small" @click="loadSampleContent">
              <el-icon><MagicStick /></el-icon>
              加载示例
            </el-button>
            <el-button type="text" size="small" @click="clearTestContent">
              <el-icon><Delete /></el-icon>
              清空
            </el-button>
          </div>
        </template>
        
        <div class="test-input">
          <el-input
            v-model="testContent"
            type="textarea"
            :rows="6"
            placeholder="请输入要测试的内容..."
            maxlength="5000"
            show-word-limit
            class="test-textarea"
          />
          
          <div class="test-actions">
            <el-button type="primary" @click="performTest" :loading="testing">
              <el-icon><Tools /></el-icon>
              开始测试
            </el-button>
            <el-button @click="batchTest" :disabled="!testContent.trim()">
              <el-icon><Collection /></el-icon>
              批量测试
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 测试结果 -->
      <el-card v-if="testResults.length > 0" class="test-results-card">
        <template #header>
          <span>测试结果</span>
          <div class="results-summary">
            <el-tag :type="getOverallResultTag()" size="small">
              {{ getOverallResultText() }}
            </el-tag>
            <span class="match-count">匹配: {{ matchCount }}/{{ testResults.length }}</span>
          </div>
        </template>
        
        <div class="test-results">
          <div 
            v-for="(result, index) in testResults" 
            :key="index"
            class="test-result-item"
            :class="{ 'matched': result.matched }"
          >
            <div class="result-header">
              <div class="result-status">
                <el-icon v-if="result.matched" class="match-icon success">
                  <Check />
                </el-icon>
                <el-icon v-else class="match-icon failed">
                  <Close />
                </el-icon>
                <span class="status-text">
                  {{ result.matched ? '匹配' : '不匹配' }}
                </span>
              </div>
              <div class="result-meta">
                <span class="confidence" v-if="result.confidence !== undefined">
                  置信度: {{ (result.confidence * 100).toFixed(1) }}%
                </span>
                <span class="match-type" v-if="result.matchType">
                  类型: {{ getMatchTypeLabel(result.matchType) }}
                </span>
              </div>
            </div>
            
            <div class="result-content">
              <div class="test-text">
                <span class="label">测试内容:</span>
                <div class="content-display">
                  <pre class="content-text">{{ result.testContent }}</pre>
                </div>
              </div>
              
              <div v-if="result.matchedSegments?.length" class="matched-segments">
                <span class="label">匹配片段:</span>
                <div class="segments">
                  <el-tag 
                    v-for="(segment, segIndex) in result.matchedSegments" 
                    :key="segIndex"
                    type="danger" 
                    size="small"
                    class="segment-tag"
                  >
                    {{ segment }}
                  </el-tag>
                </div>
              </div>
              
              <div v-if="result.details" class="match-details">
                <span class="label">详细信息:</span>
                <div class="details">
                  <p v-if="result.details.position">位置: {{ result.details.position }}</p>
                  <p v-if="result.details.method">方法: {{ result.details.method }}</p>
                  <p v-if="result.details.similarity">相似度: {{ result.details.similarity }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 性能统计 -->
      <el-card v-if="performanceStats" class="performance-card">
        <template #header>
          <span>性能统计</span>
        </template>
        
        <el-descriptions :column="4" border>
          <el-descriptions-item label="总测试数">{{ performanceStats.totalTests }}</el-descriptions-item>
          <el-descriptions-item label="总耗时">{{ performanceStats.totalTime }}ms</el-descriptions-item>
          <el-descriptions-item label="平均耗时">{{ performanceStats.averageTime }}ms</el-descriptions-item>
          <el-descriptions-item label="匹配率">{{ performanceStats.matchRate }}%</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="$emit('update:modelValue', false)">关闭</el-button>
        <el-button type="primary" @click="exportResults" :disabled="testResults.length === 0">
          导出结果
        </el-button>
      </div>
    </template>

    <!-- 批量测试对话框 -->
    <el-dialog
      v-model="showBatchDialog"
      title="批量测试"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="batch-test">
        <el-input
          v-model="batchContent"
          type="textarea"
          :rows="8"
          placeholder="请输入多行测试内容，每行一条"
          maxlength="10000"
          show-word-limit
        />
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showBatchDialog = false">取消</el-button>
          <el-button type="primary" @click="performBatchTest" :loading="testing">
            开始批量测试
          </el-button>
        </div>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, Delete, Tools, Collection, Check, Close } from '@element-plus/icons-vue'
import moderationApi, { type KeywordRule } from '@/api/smartcs/moderation'

// Props 和 Emits
interface Props {
  modelValue: boolean
  rule: KeywordRule | null
}

defineProps<Props>()
defineEmits<{
  'update:modelValue': [value: boolean]
}>()

// 测试结果接口
interface TestResult {
  testContent: string
  matched: boolean
  confidence?: number
  matchType?: string
  matchedSegments?: string[]
  details?: {
    position?: string
    method?: string
    similarity?: number
  }
}

// 响应式数据
const testing = ref(false)
const showBatchDialog = ref(false)
const testContent = ref('')
const batchContent = ref('')
const testResults = ref<TestResult[]>([])

const performanceStats = ref<{
  totalTests: number
  totalTime: number
  averageTime: number
  matchRate: number
} | null>(null)

// 计算属性
const matchCount = computed(() => {
  return testResults.value.filter(r => r.matched).length
})

// 方法定义
const loadSampleContent = () => {
  const samples = [
    '这是一段正常的内容',
    '这里包含敏感词汇',
    '政治敏感内容示例',
    '暴力血腥内容',
    '色情低俗内容',
    '广告垃圾信息'
  ]
  
  testContent.value = samples.join('\n')
}

const clearTestContent = () => {
  testContent.value = ''
  testResults.value = []
  performanceStats.value = null
}

const performTest = async () => {
  if (!testContent.value.trim()) {
    ElMessage.warning('请输入测试内容')
    return
  }
  
  if (!props.rule) {
    ElMessage.error('规则信息不完整')
    return
  }
  
  testing.value = true
  const startTime = Date.now()
  
  try {
    // 分行测试
    const lines = testContent.value.split('\n').filter(line => line.trim())
    const results: TestResult[] = []
    
    for (const line of lines) {
      const result = await testSingleLine(line.trim())
      results.push(result)
    }
    
    testResults.value = results
    
    // 计算性能统计
    const endTime = Date.now()
    const totalTime = endTime - startTime
    const matchedCount = results.filter(r => r.matched).length
    
    performanceStats.value = {
      totalTests: results.length,
      totalTime,
      averageTime: Math.round(totalTime / results.length),
      matchRate: Math.round((matchedCount / results.length) * 100)
    }
    
    ElMessage.success(`测试完成，共测试 ${results.length} 条内容`)
  } catch (error: any) {
    console.error('Test failed:', error)
    ElMessage.error('测试失败: ' + (error.message || '未知错误'))
  } finally {
    testing.value = false
  }
}

const testSingleLine = async (content: string): Promise<TestResult> => {
  if (!props.rule) {
    throw new Error('规则不完整')
  }
  
  try {
    const response = await moderationApi.testRule(props.rule, content)
    const result = response.data
    
    return {
      testContent: content,
      matched: result.matched || false,
      confidence: result.confidence,
      matchType: result.matchType,
      matchedSegments: result.matchedSegments || [],
      details: result.details
    }
  } catch (error) {
    // 如果API调用失败，使用本地简单测试
    return performLocalTest(content)
  }
}

const performLocalTest = (content: string): TestResult => {
  if (!props.rule) {
    return { testContent: content, matched: false }
  }
  
  const { keyword, ruleType, caseSensitive, matchMode } = props.rule
  const searchContent = caseSensitive ? content : content.toLowerCase()
  const searchKeyword = caseSensitive ? keyword : keyword.toLowerCase()
  
  let matched = false
  const matchedSegments: string[] = []
  
  switch (ruleType) {
    case 'EXACT':
      if (matchMode === 'FULL') {
        matched = searchContent === searchKeyword
      } else {
        matched = searchContent.includes(searchKeyword)
      }
      if (matched) matchedSegments.push(keyword)
      break
      
    case 'SUBSTRING':
      matched = searchContent.includes(searchKeyword)
      if (matched) matchedSegments.push(keyword)
      break
      
    case 'REGEX':
      try {
        const regex = new RegExp(keyword, caseSensitive ? 'g' : 'gi')
        const matches = content.match(regex)
        matched = matches !== null
        if (matches) matchedSegments.push(...matches)
      } catch {
        matched = false
      }
      break
      
    case 'FUZZY':
      // 简单的模糊匹配实现
      const similarity = calculateSimilarity(searchContent, searchKeyword)
      const threshold = props.rule.similarityThreshold || 0.8
      matched = similarity >= threshold
      if (matched) matchedSegments.push(keyword)
      break
  }
  
  return {
    testContent: content,
    matched,
    confidence: matched ? 0.9 : 0.1,
    matchType: ruleType,
    matchedSegments,
    details: {
      method: 'local',
      position: matched ? '0' : undefined
    }
  }
}

const calculateSimilarity = (str1: string, str2: string): number => {
  // 简单的编辑距离相似度计算
  const longer = str1.length > str2.length ? str1 : str2
  const shorter = str1.length > str2.length ? str2 : str1
  
  if (longer.length === 0) return 1.0
  
  const editDistance = levenshteinDistance(longer, shorter)
  return (longer.length - editDistance) / longer.length
}

const levenshteinDistance = (str1: string, str2: string): number => {
  const matrix = Array(str2.length + 1).fill(null).map(() => Array(str1.length + 1).fill(null))
  
  for (let i = 0; i <= str1.length; i++) matrix[0][i] = i
  for (let j = 0; j <= str2.length; j++) matrix[j][0] = j
  
  for (let j = 1; j <= str2.length; j++) {
    for (let i = 1; i <= str1.length; i++) {
      const indicator = str1[i - 1] === str2[j - 1] ? 0 : 1
      matrix[j][i] = Math.min(
        matrix[j][i - 1] + 1,
        matrix[j - 1][i] + 1,
        matrix[j - 1][i - 1] + indicator
      )
    }
  }
  
  return matrix[str2.length][str1.length]
}

const batchTest = () => {
  batchContent.value = testContent.value
  showBatchDialog.value = true
}

const performBatchTest = async () => {
  if (!batchContent.value.trim()) {
    ElMessage.warning('请输入批量测试内容')
    return
  }
  
  testContent.value = batchContent.value
  showBatchDialog.value = false
  await performTest()
}

const exportResults = () => {
  if (testResults.value.length === 0) {
    ElMessage.warning('暂无测试结果可导出')
    return
  }
  
  try {
    const reportData = generateTestReport()
    downloadReport(reportData)
    ElMessage.success('测试报告导出成功')
  } catch (error) {
    console.error('Export failed:', error)
    ElMessage.error('导出失败')
  }
}

const generateTestReport = () => {
  const rule = props.rule!
  const timestamp = new Date().toLocaleString('zh-CN')
  
  let report = `关键词规则测试报告\n`
  report += `生成时间: ${timestamp}\n\n`
  
  report += `规则信息:\n`
  report += `规则名称: ${rule.ruleName}\n`
  report += `关键词: ${rule.keyword}\n`
  report += `规则类型: ${getRuleTypeLabel(rule.ruleType)}\n`
  report += `匹配模式: ${getMatchModeLabel(rule.matchMode)}\n\n`
  
  if (performanceStats.value) {
    report += `性能统计:\n`
    report += `总测试数: ${performanceStats.value.totalTests}\n`
    report += `总耗时: ${performanceStats.value.totalTime}ms\n`
    report += `平均耗时: ${performanceStats.value.averageTime}ms\n`
    report += `匹配率: ${performanceStats.value.matchRate}%\n\n`
  }
  
  report += `测试结果:\n`
  testResults.value.forEach((result, index) => {
    report += `${index + 1}. ${result.matched ? '✓' : '✗'} ${result.testContent}\n`
    if (result.matchedSegments?.length) {
      report += `   匹配片段: ${result.matchedSegments.join(', ')}\n`
    }
    if (result.confidence !== undefined) {
      report += `   置信度: ${(result.confidence * 100).toFixed(1)}%\n`
    }
  })
  
  return report
}

const downloadReport = (content: string) => {
  const blob = new Blob(['\uFEFF' + content], { type: 'text/plain;charset=utf-8' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `rule_test_report_${Date.now()}.txt`
  link.click()
  URL.revokeObjectURL(link.href)
}

// 辅助函数
const getOverallResultTag = () => {
  const rate = testResults.value.length > 0 ? (matchCount.value / testResults.value.length) : 0
  if (rate >= 0.8) return 'danger'
  if (rate >= 0.5) return 'warning'
  if (rate >= 0.2) return 'info'
  return 'success'
}

const getOverallResultText = () => {
  const rate = testResults.value.length > 0 ? (matchCount.value / testResults.value.length) : 0
  if (rate >= 0.8) return '高匹配率'
  if (rate >= 0.5) return '中匹配率'
  if (rate >= 0.2) return '低匹配率'
  return '极低匹配率'
}

const getRuleTypeTag = (type: string) => {
  const tags: Record<string, string> = {
    EXACT: 'success',
    FUZZY: 'warning',
    REGEX: 'danger',
    SUBSTRING: 'info'
  }
  return tags[type] || ''
}

const getRuleTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    EXACT: '精确匹配',
    FUZZY: '模糊匹配',
    REGEX: '正则表达式',
    SUBSTRING: '子字符串'
  }
  return labels[type] || type
}

const getMatchModeLabel = (mode: string) => {
  const labels: Record<string, string> = {
    FULL: '完整匹配',
    PARTIAL: '部分匹配',
    WORD_BOUNDARY: '词边界匹配'
  }
  return labels[mode] || mode
}

const getMatchTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    EXACT: '精确',
    FUZZY: '模糊',
    REGEX: '正则',
    SUBSTRING: '子串'
  }
  return labels[type] || type
}
</script>

<style scoped lang="scss">
.rule-test {
  .rule-info-card,
  .test-input-card,
  .test-results-card,
  .performance-card {
    margin-bottom: 16px;
    
    &:last-child {
      margin-bottom: 0;
    }
  }

  .keyword-code {
    background-color: #f5f7fa;
    padding: 2px 6px;
    border-radius: 3px;
    font-family: 'Courier New', monospace;
    font-size: 12px;
  }

  .test-input {
    .test-textarea {
      margin-bottom: 16px;
    }

    .test-actions {
      display: flex;
      gap: 12px;
    }
  }

  .results-summary {
    display: flex;
    align-items: center;
    gap: 12px;

    .match-count {
      font-size: 12px;
      color: #909399;
    }
  }

  .test-results {
    .test-result-item {
      border: 1px solid #ebeef5;
      border-radius: 4px;
      margin-bottom: 12px;
      transition: all 0.3s;

      &:last-child {
        margin-bottom: 0;
      }

      &.matched {
        border-color: #f56565;
        background-color: #fef2f2;
      }

      .result-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 16px;
        border-bottom: 1px solid #ebeef5;
        background-color: #fafafa;

        .result-status {
          display: flex;
          align-items: center;
          gap: 8px;

          .match-icon {
            &.success {
              color: #67c23a;
            }
            &.failed {
              color: #f56565;
            }
          }

          .status-text {
            font-weight: 500;
          }
        }

        .result-meta {
          display: flex;
          gap: 16px;
          font-size: 12px;
          color: #909399;
        }
      }

      .result-content {
        padding: 16px;

        .test-text,
        .matched-segments,
        .match-details {
          margin-bottom: 12px;

          &:last-child {
            margin-bottom: 0;
          }

          .label {
            display: block;
            font-weight: 500;
            color: #606266;
            margin-bottom: 8px;
          }

          .content-display {
            .content-text {
              margin: 0;
              padding: 8px 12px;
              background-color: #f5f7fa;
              border-radius: 4px;
              font-family: 'Courier New', monospace;
              font-size: 12px;
              line-height: 1.5;
              white-space: pre-wrap;
              word-wrap: break-word;
            }
          }

          .segments {
            .segment-tag {
              margin: 4px 8px 4px 0;
            }
          }

          .details {
            font-size: 12px;
            color: #606266;

            p {
              margin: 4px 0;
            }
          }
        }
      }
    }
  }
}

.batch-test {
  margin-bottom: 16px;
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
  .result-header {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
  }

  .result-meta {
    justify-content: space-between;
  }

  .test-actions {
    flex-direction: column;
    gap: 8px;
  }
}
</style>