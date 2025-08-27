<template>
  <div class="query-transformer-debug">
    <el-card class="page-header">
      <template #header>
        <div class="card-header">
          <h2>查询转换器调试工具</h2>
          <p>用于调试和分析 QueryTransformer 各阶段的查询转换过程</p>
        </div>
      </template>
    </el-card>

    <el-row :gutter="20">
      <!-- 输入区域 -->
      <el-col :span="12">
        <el-card class="input-section">
          <template #header>
            <span class="section-title">输入配置</span>
          </template>
          
          <el-form :model="form" label-width="100px" @submit.prevent="onTrace">
            <el-form-item label="模型ID">
              <el-input-number 
                v-model="form.modelId" 
                :min="1" 
                placeholder="默认为1"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="查询文本" required>
              <el-input
                v-model="form.query"
                type="textarea"
                :rows="4"
                placeholder="请输入要调试的查询文本..."
                maxlength="1000"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item>
              <el-button 
                type="primary" 
                :loading="loading"
                :disabled="!form.query?.trim()"
                @click="onTrace"
              >
                <el-icon><Search /></el-icon>
                开始调试
              </el-button>
              <el-button @click="onReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 结果概览 -->
      <el-col :span="12">
        <el-card class="summary-section" v-if="traceResult">
          <template #header>
            <span class="section-title">转换结果概览</span>
          </template>
          
          <div class="summary-content">
            <div class="summary-item">
              <label>原始查询：</label>
              <div class="query-text original">{{ traceResult.originalQuery }}</div>
            </div>
            
            <div class="summary-item" v-if="traceResult.finalQueries?.length">
              <label>最终查询（{{ traceResult.finalQueries.length }}个）：</label>
              <div class="final-queries">
                <el-tag 
                  v-for="(query, index) in traceResult.finalQueries" 
                  :key="index"
                  class="query-tag"
                  size="large"
                >
                  {{ query }}
                </el-tag>
              </div>
            </div>
            
            <div class="summary-item">
              <label>处理阶段：</label>
              <span class="stage-count">{{ traceResult.stages?.length || 0 }} 个</span>
            </div>

            <div class="summary-item">
              <label>总耗时：</label>
              <span class="total-time">{{ totalElapsedTime }}ms</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 阶段详情 -->
    <el-card class="stages-section" v-if="traceResult?.stages?.length">
      <template #header>
        <span class="section-title">阶段处理详情</span>
      </template>
      
      <el-timeline>
        <el-timeline-item
          v-for="(stage, index) in traceResult.stages"
          :key="index"
          :timestamp="`${stage.elapsedMs}ms`"
          placement="top"
        >
          <el-card class="stage-card">
            <template #header>
              <div class="stage-header">
                <span class="stage-name">{{ stage.stage }}</span>
                <el-tag v-if="stage.note" type="warning" size="small">
                  {{ stage.note }}
                </el-tag>
              </div>
            </template>
            
            <div class="stage-content">
              <el-row :gutter="20">
                <!-- 输入 -->
                <el-col :span="8">
                  <div class="stage-section">
                    <h4 class="section-label">输入 ({{ stage.before?.length || 0 }})</h4>
                    <div class="query-list">
                      <el-tag
                        v-for="(query, qIndex) in stage.before"
                        :key="`before-${qIndex}`"
                        class="query-item"
                        type="info"
                      >
                        {{ query }}
                      </el-tag>
                      <div v-if="!stage.before?.length" class="empty-state">无</div>
                    </div>
                  </div>
                </el-col>
                
                <!-- 输出 -->
                <el-col :span="8">
                  <div class="stage-section">
                    <h4 class="section-label">输出 ({{ stage.after?.length || 0 }})</h4>
                    <div class="query-list">
                      <el-tag
                        v-for="(query, qIndex) in stage.after"
                        :key="`after-${qIndex}`"
                        class="query-item"
                        type="primary"
                      >
                        {{ query }}
                      </el-tag>
                      <div v-if="!stage.after?.length" class="empty-state">无</div>
                    </div>
                  </div>
                </el-col>
                
                <!-- 变更统计 -->
                <el-col :span="8">
                  <div class="stage-section">
                    <h4 class="section-label">变更统计</h4>
                    <div class="changes-stats">
                      <div class="stat-item" v-if="stage.added?.length">
                        <el-icon color="#67C23A"><Plus /></el-icon>
                        <span>新增 {{ stage.added.length }} 个</span>
                        <div class="change-list">
                          <el-tag
                            v-for="(query, qIndex) in stage.added"
                            :key="`added-${qIndex}`"
                            class="change-item"
                            type="success"
                            size="small"
                          >
                            {{ query }}
                          </el-tag>
                        </div>
                      </div>
                      
                      <div class="stat-item" v-if="stage.removed?.length">
                        <el-icon color="#F56C6C"><Minus /></el-icon>
                        <span>移除 {{ stage.removed.length }} 个</span>
                        <div class="change-list">
                          <el-tag
                            v-for="(query, qIndex) in stage.removed"
                            :key="`removed-${qIndex}`"
                            class="change-item"
                            type="danger"
                            size="small"
                          >
                            {{ query }}
                          </el-tag>
                        </div>
                      </div>
                      
                      <div class="stat-item" v-if="stage.unchanged?.length">
                        <el-icon color="#909399"><Check /></el-icon>
                        <span>保持 {{ stage.unchanged.length }} 个</span>
                      </div>
                      
                      <div class="stat-item" v-if="!stage.added?.length && !stage.removed?.length && !stage.unchanged?.length">
                        <span class="no-changes">无变更</span>
                      </div>
                    </div>
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, Minus, Check } from '@element-plus/icons-vue'
import { queryTransformerDebugAPI, type TraceRequest, type QueryTransformationTrace } from '@/api/smartcs/queryTransformerDebug'

const loading = ref(false)
const form = ref<TraceRequest>({
  modelId: 1,
  query: ''
})
const traceResult = ref<QueryTransformationTrace | null>(null)

// 计算总耗时
const totalElapsedTime = computed(() => {
  if (!traceResult.value?.stages?.length) return 0
  return traceResult.value.stages.reduce((total, stage) => total + (stage.elapsedMs || 0), 0)
})

// 执行调试追踪
const onTrace = async () => {
  if (!form.value.query?.trim()) {
    ElMessage.warning('请输入查询文本')
    return
  }
  
  loading.value = true
  try {
    const response = await queryTransformerDebugAPI.trace(form.value)
    if (response.success) {
      traceResult.value = response.data
      ElMessage.success('调试执行成功')
    } else {
      ElMessage.error(response.errMessage || '调试执行失败')
    }
  } catch (error) {
    console.error('Debug trace error:', error)
    ElMessage.error('调试执行失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 重置表单
const onReset = () => {
  form.value = {
    modelId: 1,
    query: ''
  }
  traceResult.value = null
}
</script>

<style scoped lang="scss">
.query-transformer-debug {
  padding: 20px;
  
  .page-header {
    margin-bottom: 20px;
    
    .card-header {
      h2 {
        margin: 0 0 8px 0;
        color: var(--el-text-color-primary);
      }
      
      p {
        margin: 0;
        color: var(--el-text-color-regular);
        font-size: 14px;
      }
    }
  }
  
  .section-title {
    font-weight: 600;
    font-size: 16px;
  }
  
  .input-section, .summary-section {
    margin-bottom: 20px;
  }
  
  .summary-content {
    .summary-item {
      margin-bottom: 16px;
      
      label {
        display: block;
        font-weight: 500;
        margin-bottom: 8px;
        color: var(--el-text-color-regular);
      }
      
      .query-text {
        padding: 8px 12px;
        border-radius: 4px;
        background: var(--el-fill-color-light);
        
        &.original {
          border-left: 3px solid var(--el-color-primary);
        }
      }
      
      .final-queries {
        .query-tag {
          margin: 0 8px 8px 0;
          padding: 8px 12px;
          max-width: 100%;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }
      
      .stage-count, .total-time {
        font-weight: 600;
        color: var(--el-color-primary);
      }
    }
  }
  
  .stages-section {
    .stage-card {
      .stage-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        
        .stage-name {
          font-weight: 600;
          font-size: 16px;
        }
      }
      
      .stage-content {
        .stage-section {
          .section-label {
            margin: 0 0 12px 0;
            font-size: 14px;
            font-weight: 500;
            color: var(--el-text-color-regular);
          }
          
          .query-list {
            min-height: 60px;
            
            .query-item {
              display: block;
              margin-bottom: 8px;
              padding: 6px 10px;
              word-break: break-all;
              white-space: normal;
              height: auto;
              line-height: 1.4;
            }
            
            .empty-state {
              color: var(--el-text-color-placeholder);
              font-style: italic;
              text-align: center;
              padding: 20px 0;
            }
          }
          
          .changes-stats {
            .stat-item {
              margin-bottom: 12px;
              
              .el-icon {
                margin-right: 6px;
              }
              
              .change-list {
                margin-top: 8px;
                
                .change-item {
                  margin: 0 4px 4px 0;
                  word-break: break-all;
                  white-space: normal;
                  height: auto;
                  line-height: 1.3;
                }
              }
              
              .no-changes {
                color: var(--el-text-color-placeholder);
                font-style: italic;
              }
            }
          }
        }
      }
    }
  }
}

:deep(.el-timeline-item__timestamp) {
  font-weight: 600;
  color: var(--el-color-primary);
}

:deep(.el-card__header) {
  border-bottom: 1px solid var(--el-border-color-light);
}
</style>