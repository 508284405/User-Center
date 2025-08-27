<template>
  <div class="template-test">
    <div class="test-form">
      <h4 class="form-title">模板测试</h4>
      <p class="form-desc">输入测试内容和变量值，预览生成的prompt效果</p>

      <el-form label-width="120px">
        <!-- 测试内容 -->
        <el-form-item label="测试内容">
          <el-input
            v-model="testContent"
            type="textarea"
            :rows="4"
            placeholder="请输入要测试的内容"
          />
        </el-form-item>

        <!-- 变量值配置 -->
        <el-form-item v-if="hasVariables" label="变量值">
          <div class="variables-config">
            <div
              v-for="[key, desc] in Object.entries(template.variables || {})"
              :key="key"
              class="variable-input"
            >
              <label class="variable-label">
                <code>&#123;&#123;{{ key }}&#125;&#125;</code>
                <span class="variable-desc">{{ desc }}</span>
              </label>
              <el-input
                v-model="variableValues[key]"
                :placeholder="`请输入${key}的值`"
                size="small"
              />
            </div>
          </div>
        </el-form-item>

        <!-- 测试按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            :loading="testing"
            :disabled="!testContent.trim()"
            @click="handleTest"
          >
            <el-icon><VideoPlay /></el-icon>
            生成预览
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-divider />

    <!-- 测试结果 -->
    <div class="test-result">
      <h4 class="result-title">生成结果</h4>
      
      <div v-if="generatedPrompt" class="result-content">
        <div class="prompt-preview">
          <div class="preview-header">
            <span class="preview-title">生成的Prompt</span>
            <el-button size="small" @click="copyPrompt">
              <el-icon><CopyDocument /></el-icon>
              复制
            </el-button>
          </div>
          <div class="preview-content">
            <pre>{{ generatedPrompt }}</pre>
          </div>
        </div>

        <div class="result-stats">
          <div class="stat-item">
            <span class="stat-label">字符数：</span>
            <span class="stat-value">{{ generatedPrompt.length }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">单词数：</span>
            <span class="stat-value">{{ getWordCount(generatedPrompt) }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">变量替换：</span>
            <span class="stat-value">{{ getVariableCount() }}个</span>
          </div>
        </div>
      </div>

      <div v-else class="empty-result">
        <el-icon><Box /></el-icon>
        <p>点击"生成预览"按钮查看效果</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { VideoPlay, Refresh, CopyDocument, Box } from '@element-plus/icons-vue'
import type { ModerationPolicyTemplate } from '@/api/smartcs/moderation'

interface Props {
  template: ModerationPolicyTemplate
}

const props = defineProps<Props>()

// 测试状态
const testing = ref(false)
const testContent = ref('')
const variableValues = reactive<Record<string, string>>({})
const generatedPrompt = ref('')

// 是否有变量
const hasVariables = computed(() => {
  return props.template.variables && Object.keys(props.template.variables).length > 0
})

// 初始化变量默认值
const initializeVariables = () => {
  if (props.template.variables) {
    Object.keys(props.template.variables).forEach(key => {
      variableValues[key] = props.template.defaultValues?.[key] || ''
    })
  }
}

// 页面初始化时设置默认值
initializeVariables()

// 生成测试预览
const handleTest = () => {
  if (!testContent.value.trim()) {
    ElMessage.warning('请输入测试内容')
    return
  }

  testing.value = true
  
  try {
    // 模拟生成过程
    setTimeout(() => {
      let prompt = props.template.promptTemplate || ''
      
      // 替换内容变量
      prompt = prompt.replace(/\{\{content\}\}/g, testContent.value.trim())
      
      // 替换自定义变量
      if (props.template.variables) {
        Object.keys(props.template.variables).forEach(key => {
          const value = variableValues[key] || props.template.defaultValues?.[key] || `[${key}]`
          const regex = new RegExp(`\\{\\{${key}\\}\\}`, 'g')
          prompt = prompt.replace(regex, String(value))
        })
      }
      
      // 替换一些常见的系统变量
      const now = new Date()
      prompt = prompt.replace(/\{\{timestamp\}\}/g, now.toISOString())
      prompt = prompt.replace(/\{\{date\}\}/g, now.toLocaleDateString('zh-CN'))
      prompt = prompt.replace(/\{\{time\}\}/g, now.toLocaleTimeString('zh-CN'))
      
      generatedPrompt.value = prompt
      testing.value = false
    }, 800)
    
  } catch (error) {
    console.error('生成预览失败:', error)
    ElMessage.error('生成预览失败')
    testing.value = false
  }
}

// 重置表单
const handleReset = () => {
  testContent.value = ''
  initializeVariables()
  generatedPrompt.value = ''
}

// 复制生成的prompt
const copyPrompt = async () => {
  if (!generatedPrompt.value) return
  
  try {
    await navigator.clipboard.writeText(generatedPrompt.value)
    ElMessage.success('Prompt已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  }
}

// 获取单词数
const getWordCount = (text: string) => {
  return text.trim().split(/\s+/).length
}

// 获取变量替换数量
const getVariableCount = () => {
  if (!props.template.variables) return 0
  return Object.keys(props.template.variables).length
}
</script>

<style scoped lang="scss">
.template-test {
  .test-form {
    .form-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 8px 0;
    }

    .form-desc {
      color: #909399;
      font-size: 14px;
      margin: 0 0 24px 0;
    }

    .variables-config {
      width: 100%;
      
      .variable-input {
        margin-bottom: 16px;
        
        &:last-child {
          margin-bottom: 0;
        }

        .variable-label {
          display: block;
          margin-bottom: 6px;

          code {
            background: #f0f8ff;
            color: #409eff;
            padding: 2px 6px;
            border-radius: 4px;
            font-size: 13px;
            margin-right: 8px;
          }

          .variable-desc {
            color: #606266;
            font-size: 13px;
          }
        }
      }
    }
  }

  .test-result {
    .result-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 16px 0;
    }

    .result-content {
      .prompt-preview {
        background: #f8f9fa;
        border: 1px solid #e4e7ed;
        border-radius: 6px;
        margin-bottom: 16px;

        .preview-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 12px 16px;
          border-bottom: 1px solid #e4e7ed;
          background: #fff;
          border-radius: 6px 6px 0 0;

          .preview-title {
            font-weight: 500;
            color: #303133;
          }
        }

        .preview-content {
          padding: 16px;
          max-height: 300px;
          overflow-y: auto;

          pre {
            margin: 0;
            font-family: 'Courier New', monospace;
            font-size: 13px;
            line-height: 1.5;
            color: #303133;
            white-space: pre-wrap;
            word-break: break-word;
          }
        }
      }

      .result-stats {
        display: flex;
        gap: 24px;
        padding: 16px;
        background: #fafafa;
        border-radius: 6px;

        .stat-item {
          .stat-label {
            color: #909399;
            font-size: 14px;
          }

          .stat-value {
            font-weight: 600;
            color: #303133;
          }
        }
      }
    }

    .empty-result {
      text-align: center;
      padding: 60px 20px;
      color: #909399;
      background: #fafafa;
      border-radius: 6px;

      .el-icon {
        font-size: 48px;
        margin-bottom: 16px;
      }

      p {
        margin: 0;
        font-size: 14px;
      }
    }
  }
}

// Element Plus 样式自定义
:deep(.el-textarea__inner) {
  font-family: inherit;
}
</style>
