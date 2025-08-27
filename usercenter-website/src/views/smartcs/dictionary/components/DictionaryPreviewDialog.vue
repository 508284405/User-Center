<template>
  <el-dialog
    v-model="dialogVisible"
    title="字典效果预览"
    width="900px"
    @closed="resetForm"
  >
    <div class="preview-container">
      <!-- 预览配置 -->
      <div class="preview-config">
        <el-form :model="previewForm" inline label-width="80px">
          <el-form-item label="阶段">
            <el-select v-model="previewForm.stage" placeholder="选择处理阶段" style="width: 200px">
              <el-option label="规范化阶段" value="normalization" />
              <el-option label="拼音纠错阶段" value="phonetic-correction" />
              <el-option label="前缀补全阶段" value="prefix-completion" />
              <el-option label="近义词召回阶段" value="synonym-recall" />
              <el-option label="语义对齐阶段" value="semantic-alignment" />
              <el-option label="意图提取阶段" value="intent-extraction" />
              <el-option label="改写阶段" value="rewrite" />
              <el-option label="扩展策略阶段" value="expansion-strategy" />
            </el-select>
          </el-form-item>
          <el-form-item label="租户">
            <el-input v-model="previewForm.tenant" placeholder="租户ID" style="width: 120px" />
          </el-form-item>
          <el-form-item label="渠道">
            <el-input v-model="previewForm.channel" placeholder="渠道ID" style="width: 120px" />
          </el-form-item>
          <el-form-item label="域名">
            <el-input v-model="previewForm.domain" placeholder="域名" style="width: 120px" />
          </el-form-item>
        </el-form>
      </div>

      <!-- 查询输入区 -->
      <div class="query-input">
        <h3>测试查询</h3>
        <div class="input-group">
          <el-input
            v-model="previewForm.query"
            type="textarea"
            :rows="3"
            placeholder="输入需要测试的查询文本..."
            class="query-textarea"
          />
          <el-button
            type="primary"
            @click="previewStage"
            :loading="previewing"
            class="preview-btn"
          >
            预览效果
          </el-button>
        </div>
      </div>

      <!-- 预览结果 -->
      <div v-if="previewResult" class="preview-result">
        <el-divider content-position="left">处理结果</el-divider>
        
        <div class="result-overview">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-card class="result-card">
                <template #header>
                  <div class="card-header">
                    <span>原始查询</span>
                  </div>
                </template>
                <div class="query-text original">
                  {{ previewResult.originalQuery }}
                </div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card class="result-card">
                <template #header>
                  <div class="card-header">
                    <span>处理后查询</span>
                  </div>
                </template>
                <div class="query-text processed">
                  {{ previewResult.processedQuery }}
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <div class="processing-details">
          <el-row :gutter="20">
            <el-col :span="16">
              <el-card>
                <template #header>
                  <div class="card-header">
                    <span>应用的规则</span>
                    <el-tag type="info" size="small">共 {{ previewResult.appliedRules.length }} 条</el-tag>
                  </div>
                </template>
                <div class="applied-rules">
                  <el-empty v-if="previewResult.appliedRules.length === 0" description="未应用任何规则" />
                  <div v-else class="rules-list">
                    <div
                      v-for="(rule, index) in previewResult.appliedRules"
                      :key="index"
                      class="rule-item"
                    >
                      <el-icon class="rule-icon"><Check /></el-icon>
                      <span class="rule-text">{{ rule }}</span>
                    </div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card>
                <template #header>
                  <div class="card-header">
                    <span>执行信息</span>
                  </div>
                </template>
                <div class="execution-info">
                  <div class="info-item">
                    <span class="label">处理阶段：</span>
                    <el-tag type="primary" size="small">{{ getStageLabel(previewResult.stage) }}</el-tag>
                  </div>
                  <div class="info-item">
                    <span class="label">执行时间：</span>
                    <span class="value">{{ previewResult.executionTime }}ms</span>
                  </div>
                  <div class="info-item">
                    <span class="label">处理状态：</span>
                    <el-tag
                      :type="previewResult.processedQuery !== previewResult.originalQuery ? 'success' : 'info'"
                      size="small"
                    >
                      {{ previewResult.processedQuery !== previewResult.originalQuery ? '已修改' : '无变化' }}
                    </el-tag>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 变更对比 -->
        <div v-if="previewResult.processedQuery !== previewResult.originalQuery" class="change-comparison">
          <el-divider content-position="left">变更对比</el-divider>
          <div class="diff-view">
            <div class="diff-container">
              <div class="diff-side">
                <h4>原始文本</h4>
                <div class="diff-content original-content">
                  {{ previewResult.originalQuery }}
                </div>
              </div>
              <div class="diff-arrow">
                <el-icon size="24"><Right /></el-icon>
              </div>
              <div class="diff-side">
                <h4>处理结果</h4>
                <div class="diff-content processed-content">
                  {{ previewResult.processedQuery }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 批量测试 -->
      <div class="batch-test">
        <el-divider content-position="left">批量测试</el-divider>
        <div class="batch-input">
          <el-input
            v-model="batchQueries"
            type="textarea"
            :rows="5"
            placeholder="输入多个查询，每行一个..."
          />
          <div class="batch-actions">
            <el-button @click="clearBatchResults">清空结果</el-button>
            <el-button type="primary" @click="batchPreview" :loading="batchPreviewing">
              批量测试
            </el-button>
          </div>
        </div>

        <div v-if="batchResults.length > 0" class="batch-results">
          <el-table :data="batchResults" border stripe>
            <el-table-column prop="originalQuery" label="原始查询" width="200" show-overflow-tooltip />
            <el-table-column prop="processedQuery" label="处理结果" width="200" show-overflow-tooltip />
            <el-table-column label="是否变化" width="100" align="center">
              <template #default="{ row }">
                <el-tag
                  :type="row.processedQuery !== row.originalQuery ? 'success' : 'info'"
                  size="small"
                >
                  {{ row.processedQuery !== row.originalQuery ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="executionTime" label="耗时(ms)" width="100" align="center" />
            <el-table-column label="应用规则" min-width="300">
              <template #default="{ row }">
                <div v-if="row.appliedRules.length === 0" class="no-rules">无</div>
                <div v-else class="rules-summary">
                  <el-tag
                    v-for="rule in row.appliedRules"
                    :key="rule"
                    size="small"
                    type="info"
                    class="rule-tag"
                  >
                    {{ rule }}
                  </el-tag>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="exportResults" :disabled="batchResults.length === 0">
          导出结果
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Check, Right } from '@element-plus/icons-vue';
import { dictionaryApi, DictionaryPreviewResult } from '@/api/smartcs/dictionary';

// Props & Emits
const props = defineProps<{
  modelValue: boolean;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
}>();

// 响应式数据
const previewing = ref(false);
const batchPreviewing = ref(false);
const previewResult = ref<DictionaryPreviewResult | null>(null);
const batchResults = ref<DictionaryPreviewResult[]>([]);
const batchQueries = ref('');

const previewForm = reactive({
  stage: 'normalization',
  query: '',
  tenant: '',
  channel: '',
  domain: ''
});

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

// 方法
const resetForm = () => {
  Object.assign(previewForm, {
    stage: 'normalization',
    query: '',
    tenant: '',
    channel: '',
    domain: ''
  });
  previewResult.value = null;
  batchResults.value = [];
  batchQueries.value = '';
};

const previewStage = async () => {
  if (!previewForm.query.trim()) {
    ElMessage.error('请输入查询文本');
    return;
  }

  if (!previewForm.stage) {
    ElMessage.error('请选择处理阶段');
    return;
  }

  previewing.value = true;
  try {
    const response = await dictionaryApi.previewStage({
      stage: previewForm.stage,
      query: previewForm.query,
      tenant: previewForm.tenant || undefined,
      channel: previewForm.channel || undefined,
      domain: previewForm.domain || undefined
    });
    previewResult.value = response.data;
  } catch (error) {
    console.error('预览失败:', error);
  } finally {
    previewing.value = false;
  }
};

const batchPreview = async () => {
  const queries = batchQueries.value
    .split('\n')
    .map(q => q.trim())
    .filter(q => q);

  if (queries.length === 0) {
    ElMessage.error('请输入需要测试的查询');
    return;
  }

  if (!previewForm.stage) {
    ElMessage.error('请选择处理阶段');
    return;
  }

  batchPreviewing.value = true;
  const results: DictionaryPreviewResult[] = [];

  try {
    // 并发处理多个查询
    const promises = queries.map(query =>
      dictionaryApi.previewStage({
        stage: previewForm.stage,
        query,
        tenant: previewForm.tenant || undefined,
        channel: previewForm.channel || undefined,
        domain: previewForm.domain || undefined
      })
    );

    const responses = await Promise.all(promises);
    responses.forEach(response => {
      results.push(response.data);
    });

    batchResults.value = results;
    ElMessage.success(`批量测试完成，处理了 ${results.length} 个查询`);
  } catch (error) {
    console.error('批量预览失败:', error);
  } finally {
    batchPreviewing.value = false;
  }
};

const clearBatchResults = () => {
  batchResults.value = [];
  batchQueries.value = '';
};

const exportResults = () => {
  if (batchResults.value.length === 0) {
    ElMessage.error('没有可导出的结果');
    return;
  }

  const csvContent = [
    '原始查询,处理结果,是否变化,执行时间(ms),应用规则'
  ];

  batchResults.value.forEach(result => {
    const isChanged = result.processedQuery !== result.originalQuery;
    const rulesText = result.appliedRules.join(';');
    csvContent.push(`"${result.originalQuery}","${result.processedQuery}",${isChanged ? '是' : '否'},${result.executionTime},"${rulesText}"`);
  });

  const content = csvContent.join('\n');
  const blob = new Blob([content], { type: 'text/csv;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `dictionary_preview_${new Date().getTime()}.csv`;
  link.click();
  URL.revokeObjectURL(url);

  ElMessage.success('结果导出成功');
};

const getStageLabel = (stage: string): string => {
  const stageLabels: Record<string, string> = {
    'normalization': '规范化',
    'phonetic-correction': '拼音纠错',
    'prefix-completion': '前缀补全',
    'synonym-recall': '近义词召回',
    'semantic-alignment': '语义对齐',
    'intent-extraction': '意图提取',
    'rewrite': '改写',
    'expansion-strategy': '扩展策略'
  };
  return stageLabels[stage] || stage;
};
</script>

<style scoped lang="scss">
.preview-container {
  .preview-config {
    background: #f8f9fa;
    padding: 16px;
    border-radius: 6px;
    margin-bottom: 20px;
  }

  .query-input {
    margin-bottom: 30px;

    h3 {
      margin-bottom: 12px;
      color: #303133;
    }

    .input-group {
      display: flex;
      gap: 12px;
      align-items: flex-end;

      .query-textarea {
        flex: 1;
      }

      .preview-btn {
        height: 86px;
      }
    }
  }

  .preview-result {
    .result-overview {
      margin-bottom: 20px;

      .result-card {
        height: 120px;

        .query-text {
          font-size: 16px;
          line-height: 1.5;
          padding: 8px;
          border-radius: 4px;
          min-height: 60px;

          &.original {
            background: #fff2f0;
            border: 1px solid #ffccc7;
          }

          &.processed {
            background: #f6ffed;
            border: 1px solid #b7eb8f;
          }
        }
      }
    }

    .processing-details {
      margin-bottom: 20px;

      .applied-rules {
        .rules-list {
          .rule-item {
            display: flex;
            align-items: center;
            margin-bottom: 8px;
            padding: 8px;
            background: #f0f9ff;
            border-radius: 4px;

            .rule-icon {
              color: #52c41a;
              margin-right: 8px;
            }

            .rule-text {
              font-size: 14px;
            }
          }
        }
      }

      .execution-info {
        .info-item {
          display: flex;
          align-items: center;
          margin-bottom: 12px;

          .label {
            font-weight: 500;
            margin-right: 8px;
            min-width: 80px;
          }

          .value {
            color: #1890ff;
            font-weight: 500;
          }
        }
      }
    }

    .change-comparison {
      .diff-container {
        display: flex;
        align-items: stretch;
        gap: 20px;

        .diff-side {
          flex: 1;

          h4 {
            margin-bottom: 8px;
            color: #606266;
          }

          .diff-content {
            padding: 16px;
            border-radius: 6px;
            border: 1px solid #e0e0e0;
            min-height: 80px;
            font-family: monospace;
            font-size: 14px;
            line-height: 1.6;

            &.original-content {
              background: #fff2f0;
              border-color: #ffccc7;
            }

            &.processed-content {
              background: #f6ffed;
              border-color: #b7eb8f;
            }
          }
        }

        .diff-arrow {
          display: flex;
          align-items: center;
          color: #1890ff;
        }
      }
    }
  }

  .batch-test {
    margin-top: 30px;

    .batch-input {
      margin-bottom: 20px;

      .batch-actions {
        margin-top: 12px;
        text-align: right;
        display: flex;
        justify-content: flex-end;
        gap: 12px;
      }
    }

    .batch-results {
      .no-rules {
        color: #8c8c8c;
        font-style: italic;
      }

      .rules-summary {
        .rule-tag {
          margin: 2px;
        }
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
}

// Card header styles
:deep(.el-card__header) {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

// Table styles
:deep(.el-table) {
  .el-table__header th {
    background-color: #fafafa;
  }
}
</style>