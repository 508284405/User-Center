<template>
  <div class="test-panel">
    <div class="panel-header">
      <h4>测试与预览</h4>
      <div class="header-actions">
        <el-button text @click="clearHistory" :disabled="messages.length === 0">
          <el-icon><Delete /></el-icon>
          清空
        </el-button>
      </div>
    </div>

    <div class="panel-content">
      <!-- 变量输入区域 -->
      <div class="variables-section" v-if="promptConfig.variables.length > 0">
        <div class="section-title">
          <span>输入变量</span>
          <el-button text size="small" @click="resetVariables">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </div>
        
        <div class="variables-form">
          <div 
            v-for="variable in promptConfig.variables" 
            :key="variable.key"
            class="variable-input"
          >
            <div class="variable-label">
              <span class="label-text">{{ variable.label }}</span>
              <el-tag 
                v-if="variable.required" 
                type="danger" 
                size="small"
              >
                必填
              </el-tag>
            </div>
            
            <!-- 根据变量类型渲染不同的输入控件 -->
            <el-input
              v-if="variable.type === 'string'"
              v-model="variableValues[variable.key]"
              :placeholder="variable.defaultValue || `请输入${variable.label}`"
              clearable
            />
            
            <el-input-number
              v-else-if="variable.type === 'number'"
              v-model="variableValues[variable.key]"
              :placeholder="variable.defaultValue || 0"
              style="width: 100%"
            />
            
            <el-input
              v-else-if="variable.type === 'textarea'"
              v-model="variableValues[variable.key]"
              :placeholder="variable.defaultValue || `请输入${variable.label}`"
              type="textarea"
              :rows="3"
              clearable
            />
            
            <el-select
              v-else-if="variable.type === 'select'"
              v-model="variableValues[variable.key]"
              :placeholder="`请选择${variable.label}`"
              style="width: 100%"
            >
              <el-option
                v-for="option in getSelectOptions(variable)"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </div>
        </div>
      </div>

      <!-- 对话区域 -->
      <div class="chat-section">
        <div class="section-title">
          <span>对话测试</span>
          <div class="chat-actions">
            <el-button 
              text 
              size="small" 
              @click="handleStartTest"
              :disabled="!canStartTest"
            >
              <el-icon><VideoPlay /></el-icon>
              开始测试
            </el-button>
          </div>
        </div>

        <div class="chat-container" ref="chatContainer">
          <div class="chat-messages">
            <div 
              v-for="(message, index) in messages" 
              :key="index"
              class="message-item"
              :class="{ 'user': message.role === 'user', 'assistant': message.role === 'assistant' }"
            >
              <div class="message-avatar">
                <span v-if="message.role === 'user'">👤</span>
                <span v-else>{{ appData?.icon || '🤖' }}</span>
              </div>
              <div class="message-content">
                <div class="message-text">{{ message.content }}</div>
                <div class="message-time">{{ formatTime(message.timestamp) }}</div>
              </div>
            </div>

            <div v-if="isLoading" class="message-item assistant">
              <div class="message-avatar">
                <span>{{ appData?.icon || '🤖' }}</span>
              </div>
              <div class="message-content">
                <div class="message-text typing">
                  <span class="typing-dot"></span>
                  <span class="typing-dot"></span>
                  <span class="typing-dot"></span>
                </div>
              </div>
            </div>

            <div v-if="messages.length === 0" class="empty-chat">
              <div class="empty-icon">💬</div>
              <div class="empty-text">开始对话测试</div>
              <div class="empty-description">
                {{ promptConfig.variables.length > 0 ? '填写变量值后点击"开始测试"' : '点击"开始测试"开始对话' }}
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="chat-input-area" v-if="messages.length > 0 || hasStartedTest">
          <div class="input-container">
            <el-input
              ref="messageInput"
              v-model="currentMessage"
              placeholder="输入消息..."
              @keydown.enter="handleSendMessage"
              :disabled="isLoading"
            >
              <template #append>
                <el-button 
                  @click="handleSendMessage" 
                  :disabled="!currentMessage.trim() || isLoading"
                  type="primary"
                  style="border: none"
                >
                  <el-icon><Promotion /></el-icon>
                </el-button>
              </template>
            </el-input>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete, Refresh, VideoPlay, Promotion } from '@element-plus/icons-vue'
import { formatTime } from '@/utils/format'

interface Variable {
  key: string
  label: string
  type: string
  required: boolean
  defaultValue?: any
  options?: Array<{ label: string; value: any }>
}

interface Message {
  role: 'user' | 'assistant'
  content: string
  timestamp: number
}

interface Props {
  appData: any
  promptConfig: {
    content: string
    variables: Variable[]
  }
  knowledgeConfig: {
    enabled: boolean
    knowledgeBases: number[]
    retrievalSettings: {
      topK: number
      scoreThreshold: number
    }
  }
  filterConfig: {
    enabled: boolean
    strategy: string
  }
}

interface Emits {
  (e: 'test', data: any): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 响应式数据
const variableValues = reactive<Record<string, any>>({})
const messages = ref<Message[]>([])
const currentMessage = ref('')
const isLoading = ref(false)
const hasStartedTest = ref(false)

// 模板引用
const chatContainer = ref<HTMLElement>()
const messageInput = ref()

// 计算属性
const canStartTest = computed(() => {
  // 检查必填变量是否都已填写
  const requiredVariables = props.promptConfig.variables.filter(v => v.required)
  return requiredVariables.every(v => {
    const value = variableValues[v.key]
    return value !== undefined && value !== null && value !== ''
  })
})

// 初始化变量默认值
const initializeVariables = () => {
  props.promptConfig.variables.forEach(variable => {
    if (variable.defaultValue !== undefined) {
      variableValues[variable.key] = variable.defaultValue
    }
  })
}

// 获取选择框选项
const getSelectOptions = (variable: Variable) => {
  return variable.options || [
    { label: '选项1', value: 'option1' },
    { label: '选项2', value: 'option2' }
  ]
}

// 重置变量
const resetVariables = () => {
  Object.keys(variableValues).forEach(key => {
    delete variableValues[key]
  })
  initializeVariables()
}

// 清空聊天历史
const clearHistory = () => {
  messages.value = []
  hasStartedTest.value = false
  currentMessage.value = ''
}

// 开始测试
const handleStartTest = () => {
  if (!canStartTest.value) {
    ElMessage.warning('请填写所有必填变量')
    return
  }

  hasStartedTest.value = true
  
  // 生成初始提示词
  let processedPrompt = props.promptConfig.content
  
  // 替换变量
  props.promptConfig.variables.forEach(variable => {
    const value = variableValues[variable.key] || variable.defaultValue || ''
    const regex = new RegExp(`\\{\\{${variable.key}\\}\\}`, 'g')
    processedPrompt = processedPrompt.replace(regex, value)
  })

  // 添加系统消息
  if (processedPrompt.trim()) {
    messages.value.push({
      role: 'assistant',
      content: `已应用提示词模板。${props.knowledgeConfig.enabled ? '知识库检索已启用。' : ''}现在可以开始对话了。`,
      timestamp: Date.now()
    })
  }

  nextTick(() => {
    scrollToBottom()
    focusInput()
  })

  // 触发测试事件
  emit('test', {
    prompt: processedPrompt,
    variables: variableValues,
    knowledge: props.knowledgeConfig,
    filter: props.filterConfig
  })
}

// 发送消息
const handleSendMessage = async () => {
  const message = currentMessage.value.trim()
  if (!message || isLoading.value) return

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: message,
    timestamp: Date.now()
  })

  currentMessage.value = ''
  isLoading.value = true

  nextTick(() => {
    scrollToBottom()
  })

  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000 + Math.random() * 2000))
    
    // 模拟AI回复
    const responses = [
      '我理解您的问题。基于当前的配置，我会尽力为您提供帮助。',
      '根据您提供的信息，我建议您可以尝试以下方法...',
      '这是一个很好的问题。让我为您详细解答...',
      '感谢您的提问。根据知识库中的信息，我认为...',
      '我正在处理您的请求，请稍等片刻...'
    ]
    
    const response = responses[Math.floor(Math.random() * responses.length)]
    
    messages.value.push({
      role: 'assistant',
      content: response,
      timestamp: Date.now()
    })

    nextTick(() => {
      scrollToBottom()
      focusInput()
    })
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败，请重试')
  } finally {
    isLoading.value = false
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (chatContainer.value) {
    const container = chatContainer.value.querySelector('.chat-messages')
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  }
}

// 聚焦输入框
const focusInput = () => {
  if (messageInput.value) {
    messageInput.value.focus()
  }
}

// 监听变量变化，重新初始化
watch(() => props.promptConfig.variables, () => {
  initializeVariables()
}, { immediate: true, deep: true })
</script>

<style scoped lang="scss">
.test-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    border-bottom: 1px solid #f3f4f6;

    h4 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #1f2937;
    }

    .header-actions {
      display: flex;
      gap: 8px;
    }
  }

  .panel-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;

    .variables-section {
      padding: 20px 24px;
      border-bottom: 1px solid #f3f4f6;

      .section-title {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;
        font-weight: 500;
        color: #374151;
        font-size: 14px;
      }

      .variables-form {
        .variable-input {
          margin-bottom: 16px;

          &:last-child {
            margin-bottom: 0;
          }

          .variable-label {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 8px;

            .label-text {
              font-size: 13px;
              color: #374151;
              font-weight: 500;
            }
          }
        }
      }
    }

    .chat-section {
      flex: 1;
      display: flex;
      flex-direction: column;
      min-height: 0;

      .section-title {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20px 24px 0;
        font-weight: 500;
        color: #374151;
        font-size: 14px;

        .chat-actions {
          display: flex;
          gap: 8px;
        }
      }

      .chat-container {
        flex: 1;
        padding: 16px 24px 0;
        min-height: 0;

        .chat-messages {
          height: 100%;
          overflow-y: auto;
          padding-right: 8px;

          .message-item {
            display: flex;
            gap: 12px;
            margin-bottom: 16px;

            .message-avatar {
              width: 32px;
              height: 32px;
              border-radius: 50%;
              background: #f3f4f6;
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 16px;
              flex-shrink: 0;
            }

            .message-content {
              flex: 1;
              min-width: 0;

              .message-text {
                background: #f8fafc;
                padding: 12px 16px;
                border-radius: 12px;
                font-size: 14px;
                line-height: 1.5;
                color: #374151;
                word-wrap: break-word;

                &.typing {
                  display: flex;
                  align-items: center;
                  gap: 4px;
                  padding: 16px;

                  .typing-dot {
                    width: 6px;
                    height: 6px;
                    border-radius: 50%;
                    background: #9ca3af;
                    animation: typing 1.4s infinite ease-in-out;

                    &:nth-child(1) {
                      animation-delay: -0.32s;
                    }

                    &:nth-child(2) {
                      animation-delay: -0.16s;
                    }
                  }
                }
              }

              .message-time {
                font-size: 11px;
                color: #9ca3af;
                margin-top: 4px;
              }
            }

            &.user {
              flex-direction: row-reverse;

              .message-content .message-text {
                background: #3b82f6;
                color: white;
              }
            }
          }

          .empty-chat {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            height: 100%;
            text-align: center;
            padding: 40px 20px;

            .empty-icon {
              font-size: 48px;
              margin-bottom: 16px;
            }

            .empty-text {
              font-size: 16px;
              font-weight: 500;
              color: #374151;
              margin-bottom: 8px;
            }

            .empty-description {
              font-size: 14px;
              color: #6b7280;
              line-height: 1.4;
            }
          }
        }
      }

      .chat-input-area {
        padding: 16px 24px 24px;
        border-top: 1px solid #f3f4f6;

        .input-container {
          :deep(.el-input-group__append) {
            padding: 0;
            border: none;
            background: transparent;

            .el-button {
              height: 40px;
              width: 40px;
              border-radius: 0 6px 6px 0;
            }
          }
        }
      }
    }
  }
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

// 滚动条样式
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #f8fafc;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;

  &:hover {
    background: #9ca3af;
  }
}
</style>