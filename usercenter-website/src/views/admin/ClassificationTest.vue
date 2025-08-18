<template>
  <div class="classification-test">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">意图分类测试</h1>
        <p class="page-description">测试意图分类模型的准确性和响应速度</p>
      </div>
    </div>

    <!-- 测试卡片 -->
    <el-card class="test-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>分类测试工具</span>
          <el-tag type="info" size="small">实时测试</el-tag>
        </div>
      </template>

      <el-form 
        :model="testForm" 
        :rules="testRules"
        ref="testFormRef"
        label-width="120px"
      >
        <!-- 测试文本 -->
        <el-form-item label="测试文本" prop="text">
          <el-input
            v-model="testForm.text"
            type="textarea"
            :rows="4"
            placeholder="请输入要测试的文本内容..."
            maxlength="1000"
            show-word-limit
            resize="none"
          />
        </el-form-item>

        <!-- 测试参数 -->
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="渠道" prop="channel">
              <el-input
                v-model="testForm.channel"
                placeholder="请输入渠道"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="租户" prop="tenant">
              <el-input
                v-model="testForm.tenant"
                placeholder="请输入租户"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="置信度阈值">
              <el-slider
                v-model="testForm.threshold"
                :min="0"
                :max="1"
                :step="0.1"
                show-input
                :format-tooltip="formatThreshold"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 操作按钮 -->
        <el-form-item>
          <div class="action-buttons">
            <el-button 
              type="primary" 
              @click="testClassification"
              :loading="loading.classification"
              :icon="PlayIcon"
              size="large"
            >
              开始测试
            </el-button>
            <el-button 
              @click="clearTest"
              :icon="RefreshLeft"
              size="large"
            >
              清空
            </el-button>
            <el-button 
              @click="loadQuickSamples"
              :icon="DocumentAdd"
              size="large"
            >
              快速样例
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 测试结果 -->
    <el-card v-if="testResult" class="result-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>测试结果</span>
          <el-tag 
            :type="testResult.success ? 'success' : 'danger'"
            size="small"
          >
            {{ testResult.success ? '测试成功' : '测试失败' }}
          </el-tag>
        </div>
      </template>

      <div v-if="testResult.success && testResult.data" class="test-success">
        <!-- 主要结果 -->
        <div class="main-result">
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="result-item">
                <div class="result-label">识别意图</div>
                <div class="result-value">
                  <el-tag 
                    type="primary" 
                    size="large"
                    effect="dark"
                  >
                    {{ testResult.data.intent || '未识别' }}
                  </el-tag>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="result-item">
                <div class="result-label">置信度</div>
                <div class="result-value">
                  <el-progress
                    :percentage="Math.round((testResult.data.confidence || 0) * 100)"
                    :color="getConfidenceColor(testResult.data.confidence)"
                    :stroke-width="12"
                    text-inside
                  />
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="result-item">
                <div class="result-label">响应时间</div>
                <div class="result-value">
                  <el-statistic 
                    :value="testResult.data.responseTime || 0"
                    suffix="ms"
                    :precision="0"
                  />
                </div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 备选意图 -->
        <div v-if="testResult.data.alternatives && testResult.data.alternatives.length > 0" class="alternatives">
          <h4 class="section-title">备选意图</h4>
          <div class="alternatives-list">
            <div 
              v-for="(alt, index) in testResult.data.alternatives"
              :key="index"
              class="alternative-item"
            >
              <div class="alt-intent">
                <el-tag size="small">{{ alt.intent }}</el-tag>
              </div>
              <div class="alt-confidence">
                <el-progress
                  :percentage="Math.round((alt.confidence || 0) * 100)"
                  :stroke-width="6"
                  :show-text="true"
                  format="%"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- 详细信息 -->
        <div class="details">
          <h4 class="section-title">详细信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="测试文本">
              {{ testForm.text }}
            </el-descriptions-item>
            <el-descriptions-item label="渠道">
              {{ testForm.channel }}
            </el-descriptions-item>
            <el-descriptions-item label="租户">
              {{ testForm.tenant }}
            </el-descriptions-item>
            <el-descriptions-item label="测试时间">
              {{ formatDate(new Date()) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <!-- 错误结果 -->
      <div v-else class="test-error">
        <el-alert
          :title="testResult.errMessage || '测试失败'"
          type="error"
          :closable="false"
          show-icon
        />
      </div>
    </el-card>

    <!-- 历史记录 -->
    <el-card class="history-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>测试历史 ({{ testHistory.length }})</span>
          <el-button 
            size="small" 
            @click="clearHistory"
            :icon="Delete"
          >
            清空历史
          </el-button>
        </div>
      </template>

      <div v-if="testHistory.length > 0" class="history-list">
        <div 
          v-for="(record, index) in testHistory"
          :key="index"
          class="history-item"
          @click="loadHistoryRecord(record)"
        >
          <div class="history-content">
            <div class="history-text">{{ record.text }}</div>
            <div class="history-result">
              <el-tag 
                :type="record.success ? 'success' : 'danger'"
                size="small"
              >
                {{ record.success ? record.data?.intent || '未识别' : '失败' }}
              </el-tag>
              <span v-if="record.success" class="confidence">
                {{ Math.round((record.data?.confidence || 0) * 100) }}%
              </span>
            </div>
          </div>
          <div class="history-time">
            {{ formatTime(record.timestamp) }}
          </div>
        </div>
      </div>

      <el-empty 
        v-else 
        description="暂无测试历史"
        :image-size="80"
      />
    </el-card>

    <!-- 快速样例选择器 -->
    <el-dialog
      v-model="samplesVisible"
      title="选择快速样例"
      width="600px"
    >
      <div class="samples-list">
        <div 
          v-for="(sample, index) in quickSamples"
          :key="index"
          class="sample-item"
          @click="selectSample(sample)"
        >
          <div class="sample-text">{{ sample.text }}</div>
          <div class="sample-intent">
            <el-tag size="small" type="info">{{ sample.expectedIntent }}</el-tag>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  VideoPlay as PlayIcon,
  RefreshLeft,
  DocumentAdd,
  Delete
} from '@element-plus/icons-vue'
import { useSmartCSAdminStore } from '@/stores/admin/admin.js'

const store = useSmartCSAdminStore()

// 响应式数据
const testFormRef = ref()
const testResult = ref(null)
const testHistory = ref([])
const samplesVisible = ref(false)

const testForm = reactive({
  text: '',
  channel: 'web',
  tenant: 'default',
  threshold: 0.5
})

// 表单验证规则
const testRules = {
  text: [
    { required: true, message: '请输入测试文本', trigger: 'blur' },
    { min: 1, max: 1000, message: '文本长度在 1 到 1000 个字符', trigger: 'blur' }
  ],
  channel: [
    { required: true, message: '请输入渠道', trigger: 'blur' }
  ],
  tenant: [
    { required: true, message: '请输入租户', trigger: 'blur' }
  ]
}

// 快速样例数据
const quickSamples = ref([
  { text: '我想要查看我的订单', expectedIntent: 'order_inquiry' },
  { text: '如何申请退款', expectedIntent: 'refund_request' },
  { text: '产品价格是多少', expectedIntent: 'price_inquiry' },
  { text: '客服电话是什么', expectedIntent: 'contact_info' },
  { text: '这个商品什么时候发货', expectedIntent: 'shipping_inquiry' },
  { text: '我要投诉', expectedIntent: 'complaint' },
  { text: '账户余额不足怎么办', expectedIntent: 'balance_inquiry' },
  { text: '忘记密码了', expectedIntent: 'password_reset' }
])

// 计算属性
const loading = computed(() => store.loading)

// 方法
const testClassification = async () => {
  try {
    await testFormRef.value.validate()
    
    const testData = {
      text: testForm.text,
      channel: testForm.channel,
      tenant: testForm.tenant
    }
    
    const result = await store.testClassification(testData)
    testResult.value = result
    
    // 添加到历史记录
    addToHistory({
      ...testData,
      ...result,
      timestamp: Date.now()
    })
    
    if (result.success) {
      ElMessage.success('分类测试完成')
    } else {
      ElMessage.error('分类测试失败')
    }
    
  } catch (error) {
    if (error.message) {
      ElMessage.error('表单验证失败')
    }
  }
}

const clearTest = () => {
  testForm.text = ''
  testResult.value = null
  testFormRef.value?.clearValidate()
}

const loadQuickSamples = () => {
  samplesVisible.value = true
}

const selectSample = (sample) => {
  testForm.text = sample.text
  samplesVisible.value = false
}

const addToHistory = (record) => {
  testHistory.value.unshift(record)
  if (testHistory.value.length > 50) {
    testHistory.value = testHistory.value.slice(0, 50)
  }
  // 保存到本地存储
  localStorage.setItem('classification_test_history', JSON.stringify(testHistory.value))
}

const loadHistoryRecord = (record) => {
  testForm.text = record.text
  testForm.channel = record.channel
  testForm.tenant = record.tenant
  testResult.value = {
    success: record.success,
    data: record.data,
    errMessage: record.errMessage
  }
}

const clearHistory = () => {
  testHistory.value = []
  localStorage.removeItem('classification_test_history')
  ElMessage.success('历史记录已清空')
}

const getConfidenceColor = (confidence) => {
  if (confidence >= 0.8) return '#67c23a'
  if (confidence >= 0.6) return '#e6a23c'
  return '#f56c6c'
}

const formatThreshold = (value) => {
  return `${Math.round(value * 100)}%`
}

const formatDate = (date) => {
  return date.toLocaleString('zh-CN')
}

const formatTime = (timestamp) => {
  const now = Date.now()
  const diff = now - timestamp
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return new Date(timestamp).toLocaleDateString('zh-CN')
}

// 生命周期
onMounted(() => {
  // 加载历史记录
  const history = localStorage.getItem('classification_test_history')
  if (history) {
    try {
      testHistory.value = JSON.parse(history)
    } catch (error) {
      console.error('Failed to load test history:', error)
    }
  }
})
</script>

<style scoped>
.classification-test {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 1.5rem;
}

.page-title {
  font-size: 1.75rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 0.5rem 0;
}

.page-description {
  color: #6c757d;
  margin: 0;
}

.test-card,
.result-card,
.history-card {
  margin-bottom: 1.5rem;
  border-radius: 8px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  color: #2c3e50;
}

.action-buttons {
  display: flex;
  gap: 1rem;
}

.test-success {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.main-result {
  padding: 1rem;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 8px;
  border: 1px solid #dee2e6;
}

.result-item {
  text-align: center;
  padding: 0.5rem;
}

.result-label {
  font-size: 0.875rem;
  color: #6c757d;
  margin-bottom: 0.75rem;
  font-weight: 500;
}

.result-value {
  display: flex;
  justify-content: center;
  align-items: center;
}

.section-title {
  font-size: 1rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 1rem 0;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #e9ecef;
}

.alternatives-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.alternative-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem;
  background: #f8f9fa;
  border-radius: 6px;
}

.alt-intent {
  min-width: 120px;
}

.alt-confidence {
  flex: 1;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-height: 400px;
  overflow-y: auto;
}

.history-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.history-item:hover {
  background: #f8f9fa;
  border-color: #0d6efd;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.history-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.history-text {
  font-weight: 500;
  color: #2c3e50;
  line-height: 1.4;
}

.history-result {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.confidence {
  font-size: 0.875rem;
  color: #6c757d;
  font-weight: 500;
}

.history-time {
  font-size: 0.875rem;
  color: #6c757d;
  white-space: nowrap;
}

.samples-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-height: 400px;
  overflow-y: auto;
}

.sample-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.sample-item:hover {
  background: #f8f9fa;
  border-color: #0d6efd;
}

.sample-text {
  flex: 1;
  font-weight: 500;
  color: #2c3e50;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .action-buttons {
    flex-direction: column;
    width: 100%;
  }

  .main-result {
    padding: 0.75rem;
  }

  .history-item,
  .sample-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }

  .history-content {
    width: 100%;
  }

  .sample-text {
    margin-bottom: 0.5rem;
  }
}
</style>