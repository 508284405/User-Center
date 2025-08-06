<template>
  <div class="prompt-editor">
    <div class="editor-header">
      <div class="title-row">
        <span class="title">提示词</span>
        <div class="title-actions">
          <el-button text type="primary" @click="showOptimizeDialog">
            <el-icon><MagicStick /></el-icon>
            优化
          </el-button>
        </div>
      </div>
      <div class="editor-info">
        <span class="char-count">{{ content.length }}</span>
        <span class="variable-count">{{ variables.length }} 个变量</span>
      </div>
    </div>

    <div class="editor-container" ref="editorContainer">
      <textarea
        ref="editorTextarea"
        v-model="content"
        class="editor-textarea"
        placeholder="根据用户输入，将其翻译为{{target}}语言，无需其他回复。"
        @input="handleInput"
        @blur="handleBlur"
        @focus="handleFocus"
        @scroll="handleScroll"
      />
      <div 
        ref="highlightLayer"
        class="highlight-layer"
        v-html="highlightedContent"
      ></div>
    </div>

    <div class="editor-footer" v-if="variables.length > 0">
      <div class="detected-variables">
        <span class="label">检测到的变量:</span>
        <el-tag
          v-for="variable in variables"
          :key="variable.key"
          size="small"
          class="variable-tag"
        >
          {{ variable.key }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { MagicStick } from '@element-plus/icons-vue'

interface Variable {
  key: string
  label: string
  type: string
  required: boolean
  defaultValue?: any
}

interface Props {
  content: string
  variables: Variable[]
}

interface Emits {
  (e: 'update:content', value: string): void
  (e: 'update:variables', value: Variable[]): void
  (e: 'change'): void
  (e: 'optimize-prompt'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 模板引用
const editorContainer = ref<HTMLElement>()
const editorTextarea = ref<HTMLTextAreaElement>()
const highlightLayer = ref<HTMLElement>()

// 响应式数据
const content = computed({
  get: () => props.content,
  set: (value) => emit('update:content', value)
})

// 提取变量的正则表达式
const variableRegex = /\{\{([^}]+)\}\}/g

// 高亮显示的内容
const highlightedContent = computed(() => {
  // 确保 content.value 是字符串类型
  const contentStr = String(content.value || '')
  if (!contentStr) return ''
  
  let highlighted = contentStr
    // 转义HTML字符
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    // 高亮变量
    .replace(variableRegex, '<span class="variable">{{$1}}</span>')
    // 保持换行
    .replace(/\n/g, '<br>')
  
  return highlighted
})

// 从内容中提取变量
const extractVariables = (text: string): Variable[] => {
  const matches = text.matchAll(variableRegex)
  const variableMap = new Map<string, Variable>()
  
  for (const match of matches) {
    const key = match[1].trim()
    if (key && !variableMap.has(key)) {
      // 检查是否已存在该变量
      const existingVar = props.variables.find(v => v.key === key)
      
      variableMap.set(key, existingVar || {
        key,
        label: key,
        type: 'string',
        required: true
      })
    }
  }
  
  return Array.from(variableMap.values())
}

// 处理输入
const handleInput = () => {
  const newVariables = extractVariables(content.value)
  emit('update:variables', newVariables)
  emit('change')
  
  // 同步滚动
  nextTick(() => {
    syncScroll()
  })
}

// 处理焦点
const handleFocus = () => {
  // 可以添加焦点时的逻辑
}

const handleBlur = () => {
  // 可以添加失焦时的逻辑
}

// 同步滚动
const handleScroll = () => {
  syncScroll()
}

const syncScroll = () => {
  if (editorTextarea.value && highlightLayer.value) {
    const textarea = editorTextarea.value
    const highlight = highlightLayer.value
    
    highlight.scrollTop = textarea.scrollTop
    highlight.scrollLeft = textarea.scrollLeft
  }
}


// 显示优化对话框
const showOptimizeDialog = () => {
  emit('optimize-prompt')
}

// 调整文本域高度
const adjustTextareaHeight = () => {
  if (editorTextarea.value) {
    const textarea = editorTextarea.value
    textarea.style.height = 'auto'
    textarea.style.height = Math.max(120, textarea.scrollHeight) + 'px'
    
    // 同步高亮层高度
    if (highlightLayer.value) {
      highlightLayer.value.style.height = textarea.style.height
    }
  }
}

// 监听内容变化调整高度
watch(() => content.value, () => {
  nextTick(() => {
    adjustTextareaHeight()
  })
})

// 组件挂载后调整高度
onMounted(() => {
  adjustTextareaHeight()
  
  // 监听窗口大小变化
  window.addEventListener('resize', adjustTextareaHeight)
})

onUnmounted(() => {
  window.removeEventListener('resize', adjustTextareaHeight)
})
</script>

<style scoped lang="scss">
.prompt-editor {
  .editor-header {
    margin-bottom: 12px;

    .title-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .title {
        font-weight: 500;
        color: #374151;
        font-size: 14px;
      }
      
      .title-actions {
        display: flex;
        gap: 8px;
      }
    }

    .editor-info {
      display: flex;
      gap: 16px;
      font-size: 12px;
      color: #6b7280;

      .char-count {
        &::after {
          content: ' 字符';
        }
      }

      .variable-count {
        color: #3b82f6;
      }
    }
  }

  .editor-container {
    position: relative;
    border: 1px solid #d1d5db;
    border-radius: 8px;
    background: #fff;
    overflow: hidden;

    &:focus-within {
      border-color: #3b82f6;
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
    }

    .editor-textarea {
      width: 100%;
      min-height: 120px;
      padding: 12px;
      border: none;
      outline: none;
      background: transparent;
      resize: none;
      font-family: 'SF Mono', Monaco, 'Cascadia Code', 'Roboto Mono', Consolas, 'Courier New', monospace;
      font-size: 14px;
      line-height: 1.5;
      color: #374151;
      z-index: 2;
      position: relative;
      font-feature-settings: normal;
      -webkit-font-feature-settings: normal;
      text-rendering: optimizeLegibility;
      -webkit-font-smoothing: antialiased;
      -moz-osx-font-smoothing: grayscale;

      &::placeholder {
        color: #9ca3af;
      }
    }

    .highlight-layer {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      padding: 12px;
      font-family: 'SF Mono', Monaco, 'Cascadia Code', 'Roboto Mono', Consolas, 'Courier New', monospace;
      font-size: 14px;
      line-height: 1.5;
      color: transparent;
      white-space: pre-wrap;
      word-wrap: break-word;
      overflow: hidden;
      pointer-events: none;
      z-index: 1;
      font-feature-settings: normal;
      -webkit-font-feature-settings: normal;
      text-rendering: optimizeLegibility;
      -webkit-font-smoothing: antialiased;
      -moz-osx-font-smoothing: grayscale;

      :deep(.variable) {
        background-color: rgba(59, 130, 246, 0.1);
        color: #3b82f6;
        padding: 2px 4px;
        border-radius: 4px;
        font-weight: 500;
        font-family: 'SF Mono', Monaco, 'Cascadia Code', 'Roboto Mono', Consolas, 'Courier New', monospace;
        font-feature-settings: normal;
        -webkit-font-feature-settings: normal;
        text-rendering: optimizeLegibility;
        -webkit-font-smoothing: antialiased;
        -moz-osx-font-smoothing: grayscale;
      }
    }
  }

  .editor-footer {
    margin-top: 12px;

    .detected-variables {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;

      .label {
        font-size: 12px;
        color: #6b7280;
        font-weight: 500;
      }

      .variable-tag {
        font-family: 'SF Mono', Monaco, 'Cascadia Code', 'Roboto Mono', Consolas, 'Courier New', monospace;
        background-color: rgba(59, 130, 246, 0.1);
        color: #3b82f6;
        border: 1px solid rgba(59, 130, 246, 0.2);
      }
    }
  }
}
</style>