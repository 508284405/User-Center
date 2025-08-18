<template>
  <div class="recall-test-panel">
    <!-- 页面标题 -->
    <div class="panel-header">
      <h3>召回测试</h3>
      <p class="description">根据输入的查询文本测试知识的召回效果。</p>
    </div>

    <!-- 测试配置 -->
    <div class="test-config">
      <div class="config-section">
        <h4>检索配置</h4>
        <el-form :model="testConfig" label-width="120px" class="config-form">
          <el-form-item label="检索方式">
            <el-select v-model="testConfig.retrievalMethod" placeholder="选择检索方式">
              <el-option label="向量检索" value="vector" />
              <el-option label="全文检索" value="full_text" />
              <el-option label="混合检索" value="hybrid" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="返回数量">
            <el-input-number
              v-model="testConfig.topK"
              :min="1"
              :max="20"
              controls-position="right"
            />
          </el-form-item>
          
          <el-form-item label="相似度阈值">
            <el-slider
              v-model="testConfig.scoreThreshold"
              :min="0"
              :max="1"
              :step="0.01"
              :format-tooltip="formatThreshold"
              show-input
            />
          </el-form-item>
          
          <el-form-item label="启用Rerank" v-if="testConfig.retrievalMethod === 'hybrid'">
            <el-switch v-model="testConfig.rerankEnabled" />
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- 查询输入区域 -->
    <div class="query-section">
      <div class="query-header">
        <h4>源文本</h4>
        <el-button 
          type="primary" 
          @click="handleRecallTest"
          :loading="testing"
          :disabled="!testQuery.trim()"
        >
          <el-icon><MagicStick /></el-icon>
          向量检索
        </el-button>
      </div>
      
      <el-input
        v-model="testQuery"
        type="textarea"
        :rows="6"
        placeholder="请输入人文，建议使用简短的陈述句。"
        maxlength="200"
        show-word-limit
        class="query-textarea"
      />
    </div>

    <!-- 检索结果 -->
    <div class="results-section" v-if="testResults.length > 0 || testing">
      <div class="results-header">
        <h4>记录</h4>
        <div class="results-info" v-if="!testing">
          <span>最近完整结果</span>
        </div>
      </div>

      <div v-if="testing" class="loading-container">
        <el-skeleton :rows="3" animated />
        <div class="loading-text">召回测试结果将显示在这里</div>
      </div>

      <div v-else class="results-list">
        <div 
          v-for="(result, index) in testResults" 
          :key="result.chunkId"
          class="result-item"
        >
          <div class="result-header">
            <div class="result-title">
              <span class="chunk-index">#{{ result.chunkIndex || index + 1 }}</span>
              <span class="doc-title">{{ result.docTitle || '未命名文档' }}</span>
            </div>
            <div class="result-score">
              <el-tag 
                :type="getScoreTagType(result.score)" 
                size="small"
              >
                {{ (result.score * 100).toFixed(1) }}%
              </el-tag>
            </div>
          </div>
          
          <div class="result-content">
            <p class="content-text">{{ result.content }}</p>
          </div>
          
          <div class="result-meta" v-if="result.metadata">
            <el-collapse>
              <el-collapse-item title="元数据" name="metadata">
                <pre class="metadata-content">{{ formatMetadata(result.metadata) }}</pre>
              </el-collapse-item>
            </el-collapse>
          </div>
        </div>

        <div v-if="testResults.length === 0" class="no-results">
          <el-empty 
            description="未找到相关内容" 
            :image-size="80"
          />
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!testResults.length && !testing" class="empty-state">
      <div class="empty-icon">🔍</div>
      <div class="empty-text">召回测试结果将显示在这里</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick } from '@element-plus/icons-vue'
import { 
  recallTest, 
  type RecallTestRequest, 
  type RecallTestResult 
} from '@/api/smartcs/knowledgeBase'

interface Props {
  knowledgeBaseId: number
}

const props = defineProps<Props>()

// 测试配置
const testConfig = reactive<Omit<RecallTestRequest, 'knowledgeBaseId' | 'query'>>({
  retrievalMethod: 'vector',
  topK: 2,
  scoreThreshold: 0.5,
  rerankEnabled: false
})

// 查询文本
const testQuery = ref('')

// 测试状态
const testing = ref(false)

// 测试结果
const testResults = ref<RecallTestResult[]>([])

// 格式化阈值显示
const formatThreshold = (val: number) => {
  return `${(val * 100).toFixed(0)}%`
}

// 获取评分标签类型
const getScoreTagType = (score: number) => {
  if (score >= 0.8) return 'success'
  if (score >= 0.6) return 'warning'
  return 'danger'
}

// 格式化元数据
const formatMetadata = (metadata: any) => {
  if (typeof metadata === 'string') {
    try {
      return JSON.stringify(JSON.parse(metadata), null, 2)
    } catch {
      return metadata
    }
  }
  return JSON.stringify(metadata, null, 2)
}

// 执行召回测试
const handleRecallTest = async () => {
  if (!testQuery.value.trim()) {
    ElMessage.warning('请输入查询文本')
    return
  }

  try {
    testing.value = true
    testResults.value = []

    const request: RecallTestRequest = {
      knowledgeBaseId: props.knowledgeBaseId,
      query: testQuery.value.trim(),
      ...testConfig
    }

    const response = await recallTest(request)
    
    if (response.success && response.data) {
      testResults.value = response.data
      if (testResults.value.length === 0) {
        ElMessage.info('未找到相关内容，请尝试调整检索参数')
      } else {
        ElMessage.success(`找到 ${testResults.value.length} 条相关内容`)
      }
    } else {
      throw new Error(response.errMessage || '召回测试失败')
    }
  } catch (error: any) {
    console.error('召回测试失败:', error)
    ElMessage.error(error.message || '召回测试失败')
    testResults.value = []
  } finally {
    testing.value = false
  }
}

// 重置测试
const resetTest = () => {
  testQuery.value = ''
  testResults.value = []
}

// 暴露方法给父组件
defineExpose({
  resetTest
})
</script>

<style scoped lang="scss">
.recall-test-panel {
  padding: 24px;
  background: #fff;
  min-height: 100%;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);

  .panel-header {
    margin-bottom: 32px;
    padding-bottom: 16px;
    border-bottom: 1px solid #f0f2f5;

    h3 {
      margin: 0 0 8px 0;
      font-size: 20px;
      font-weight: 600;
      color: #1f2937;
      background: linear-gradient(135deg, #409eff 0%, #36cfc9 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .description {
      margin: 0;
      font-size: 14px;
      color: #6b7280;
      line-height: 1.6;
    }
  }

  .test-config {
    margin-bottom: 32px;
    padding: 24px;
    background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
    border-radius: 12px;
    border: 1px solid #e2e8f0;
    box-shadow: 0 1px 6px rgba(0, 0, 0, 0.02);
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
      transform: translateY(-1px);
    }

    .config-section {
      h4 {
        margin: 0 0 20px 0;
        font-size: 16px;
        font-weight: 600;
        color: #374151;
        display: flex;
        align-items: center;
        gap: 8px;
        
        &::before {
          content: '⚙️';
          font-size: 18px;
        }
      }

      .config-form {
        :deep(.el-form-item) {
          margin-bottom: 20px;
          transition: all 0.2s ease;

          &:last-child {
            margin-bottom: 0;
          }

          .el-form-item__label {
            font-weight: 500;
            color: #4b5563;
          }

          .el-select,
          .el-input-number {
            width: 100%;
            border-radius: 8px;
          }

          .el-slider {
            margin: 8px 0;
          }
        }
      }
    }
  }

  .query-section {
    margin-bottom: 40px;

    .query-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      flex-wrap: wrap;
      gap: 12px;

      h4 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #374151;
        display: flex;
        align-items: center;
        gap: 8px;
        
        &::before {
          content: '📝';
          font-size: 18px;
        }
      }

      .el-button {
        border-radius: 8px;
        font-weight: 500;
        box-shadow: 0 2px 6px rgba(64, 158, 255, 0.2);
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
        }
      }
    }

    .query-textarea {
      :deep(.el-textarea__inner) {
        font-size: 14px;
        line-height: 1.6;
        border-radius: 12px;
        border: 2px solid #e5e7eb;
        transition: all 0.3s ease;
        padding: 16px;
        
        &:focus {
          border-color: #409eff;
          box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
        }
      }
    }
  }

  .results-section {
    .results-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      padding-bottom: 12px;
      border-bottom: 1px solid #f0f2f5;

      h4 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #374151;
        display: flex;
        align-items: center;
        gap: 8px;
        
        &::before {
          content: '📊';
          font-size: 18px;
        }
      }

      .results-info {
        font-size: 13px;
        color: #6b7280;
        background: #f3f4f6;
        padding: 4px 12px;
        border-radius: 16px;
        font-weight: 500;
      }
    }

    .loading-container {
      .loading-text {
        text-align: center;
        margin-top: 16px;
        font-size: 14px;
        color: #909399;
      }
    }

    .results-list {
      .result-item {
        margin-bottom: 20px;
        padding: 20px;
        background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
        border-radius: 12px;
        border: 1px solid #e2e8f0;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        position: relative;
        overflow: hidden;

        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          height: 3px;
          background: linear-gradient(90deg, #409eff 0%, #36cfc9 100%);
          opacity: 0;
          transition: opacity 0.3s ease;
        }

        &:hover {
          border-color: #409eff;
          box-shadow: 0 6px 20px rgba(64, 158, 255, 0.15);
          transform: translateY(-2px);
          
          &::before {
            opacity: 1;
          }
        }

        &:last-child {
          margin-bottom: 0;
        }

        .result-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 16px;
          flex-wrap: wrap;
          gap: 12px;

          .result-title {
            display: flex;
            align-items: center;
            gap: 12px;
            flex: 1;

            .chunk-index {
              font-size: 12px;
              color: #6b7280;
              font-weight: 600;
              background: #f3f4f6;
              padding: 2px 8px;
              border-radius: 12px;
              min-width: fit-content;
            }

            .doc-title {
              font-size: 15px;
              font-weight: 600;
              color: #1f2937;
              line-height: 1.4;
              word-break: break-word;
            }
          }

          .result-score {
            .el-tag {
              font-weight: 600;
              border-radius: 16px;
              padding: 4px 12px;
              font-size: 12px;
            }
          }
        }

        .result-content {
          margin-bottom: 16px;

          .content-text {
            margin: 0;
            font-size: 14px;
            line-height: 1.7;
            color: #4b5563;
            word-break: break-word;
            background: #f9fafb;
            padding: 16px;
            border-radius: 8px;
            border-left: 4px solid #e5e7eb;
            transition: all 0.2s ease;
            
            &:hover {
              border-left-color: #409eff;
              background: #f0f9ff;
            }
          }
        }

        .result-meta {
          :deep(.el-collapse) {
            border: none;

            .el-collapse-item__header {
              height: 32px;
              line-height: 32px;
              background: none;
              border: none;
              font-size: 12px;
              color: #909399;
              padding: 0;
            }

            .el-collapse-item__content {
              padding: 8px 0 0 0;
            }
          }

          .metadata-content {
            margin: 0;
            padding: 8px 12px;
            background: #f5f7fa;
            border-radius: 4px;
            font-size: 12px;
            color: #606266;
            white-space: pre-wrap;
            word-break: break-word;
          }
        }
      }

      .no-results {
        text-align: center;
        padding: 40px 0;
      }
    }
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 0;
    text-align: center;

    .empty-icon {
      font-size: 48px;
      margin-bottom: 16px;
      opacity: 0.6;
    }

    .empty-text {
      font-size: 14px;
      color: #909399;
    }
  }
}

// 响应式设计
@media (max-width: 1024px) {
  .recall-test-panel {
    .test-config {
      .config-form {
        :deep(.el-form-item) {
          .el-form-item__label {
            width: 100px !important;
          }
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .recall-test-panel {
    padding: 16px;
    border-radius: 8px;
    
    .panel-header {
      margin-bottom: 24px;
      
      h3 {
        font-size: 18px;
      }
    }

    .test-config {
      padding: 16px;
      margin-bottom: 24px;

      .config-section {
        h4 {
          font-size: 14px;
        }
      }

      .config-form {
        :deep(.el-form-item) {
          display: block;
          margin-bottom: 16px;

          .el-form-item__label {
            width: auto !important;
            margin-bottom: 8px;
            padding-right: 0;
          }

          .el-form-item__content {
            margin-left: 0 !important;
          }
        }
      }
    }

    .query-section {
      margin-bottom: 32px;
      
      .query-header {
        flex-direction: column;
        align-items: stretch;
        gap: 16px;
        
        h4 {
          font-size: 14px;
        }
        
        .el-button {
          align-self: stretch;
        }
      }
    }

    .results-section {
      .results-header {
        flex-direction: column;
        align-items: stretch;
        gap: 12px;
        
        h4 {
          font-size: 14px;
        }
      }
      
      .results-list {
        .result-item {
          padding: 16px;
          border-radius: 8px;

          .result-header {
            flex-direction: column;
            align-items: stretch;
            gap: 12px;
            
            .result-title {
              gap: 8px;
              
              .doc-title {
                font-size: 14px;
              }
            }
          }
          
          .result-content {
            .content-text {
              padding: 12px;
              font-size: 13px;
              line-height: 1.6;
            }
          }
        }
      }
    }

    .empty-state {
      padding: 60px 0;
      
      .empty-icon {
        font-size: 36px;
      }
      
      .empty-text {
        font-size: 13px;
      }
    }
  }
}

@media (max-width: 480px) {
  .recall-test-panel {
    padding: 12px;
    
    .panel-header {
      margin-bottom: 20px;
      
      h3 {
        font-size: 16px;
      }
      
      .description {
        font-size: 13px;
      }
    }
    
    .test-config {
      padding: 12px;
      margin-bottom: 20px;
    }
    
    .query-section {
      margin-bottom: 24px;
    }
    
    .results-section {
      .results-list {
        .result-item {
          padding: 12px;
          
          .result-content {
            .content-text {
              padding: 10px;
            }
          }
        }
      }
    }
  }
}
</style>