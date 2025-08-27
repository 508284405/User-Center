<template>
  <div class="slot-definition-editor">
    <div class="editor-header">
      <div class="slot-info">
        <el-tag :type="slotData.required ? 'danger' : 'info'" size="small">
          {{ slotData.required ? '必填' : '可选' }}
        </el-tag>
        <span class="slot-title">
          {{ slotData.label || slotData.name || `槽位 #${index + 1}` }}
        </span>
        <el-tag v-if="slotData.type" type="success" size="small">
          {{ getSlotTypeLabel(slotData.type) }}
        </el-tag>
      </div>
      
      <div class="editor-actions">
        <el-button 
          size="small" 
          :icon="ArrowUp"
          @click="$emit('move-up')"
          :disabled="index === 0"
          title="上移"
        />
        <el-button 
          size="small" 
          :icon="ArrowDown"
          @click="$emit('move-down')"
          title="下移"
        />
        <el-button 
          size="small" 
          type="danger"
          :icon="Delete"
          @click="confirmRemove"
          title="删除"
        />
      </div>
    </div>

    <el-form :model="slotData" label-width="100px" size="small">
      <!-- 基本信息 -->
      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="槽位名称" required>
            <el-input 
              v-model="slotData.name" 
              placeholder="英文标识符"
              @blur="validateSlotName"
            />
            <div v-if="nameError" class="error-tip">{{ nameError }}</div>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="显示标签" required>
            <el-input 
              v-model="slotData.label" 
              placeholder="用户可见的标签"
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="槽位类型" required>
            <el-select v-model="slotData.type" @change="onTypeChange">
              <el-option
                v-for="type in slotTypes"
                :key="type.value"
                :label="type.label"
                :value="type.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 配置选项 -->
      <el-row :gutter="16">
        <el-col :span="6">
          <el-form-item label="必填">
            <el-switch v-model="slotData.required" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="多值">
            <el-switch v-model="slotData.multiple" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="单位">
            <el-input 
              v-model="slotData.unit" 
              placeholder="如：元、天、个"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 提示和默认值 -->
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="提示信息">
            <el-input 
              v-model="slotData.hint" 
              type="textarea"
              :rows="2"
              placeholder="给用户的提示信息"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="默认值">
            <el-input 
              v-model="slotData.defaultValue" 
              placeholder="可选的默认值"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 类型特定配置 -->
      <div v-if="isNumberType" class="type-specific-config">
        <el-divider content-position="left">数值配置</el-divider>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="最小值">
              <el-input-number 
                v-model="slotData.minValue" 
                placeholder="最小值"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="最大值">
              <el-input-number 
                v-model="slotData.maxValue" 
                placeholder="最大值"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <div v-if="isStringType" class="type-specific-config">
        <el-divider content-position="left">字符串配置</el-divider>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="最小长度">
              <el-input-number 
                v-model="slotData.minLength" 
                :min="0"
                placeholder="最小长度"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="最大长度">
              <el-input-number 
                v-model="slotData.maxLength" 
                :min="1"
                placeholder="最大长度"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="正则模式">
              <el-input 
                v-model="slotData.pattern" 
                placeholder="验证正则表达式"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <div v-if="isEnumType" class="type-specific-config">
        <el-divider content-position="left">枚举配置</el-divider>
        <el-form-item label="枚举选项">
          <div class="enum-options-editor">
            <el-tag
              v-for="(option, optIndex) in slotData.enumOptions || []"
              :key="optIndex"
              closable
              @close="removeEnumOption(optIndex)"
              class="enum-option-tag"
            >
              {{ option }}
            </el-tag>
            <el-input
              v-if="showEnumInput"
              ref="enumInput"
              v-model="newEnumOption"
              size="small"
              style="width: 100px"
              @blur="addEnumOption"
              @keyup.enter="addEnumOption"
            />
            <el-button 
              v-else
              size="small" 
              @click="showEnumInput = true"
            >
              + 添加选项
            </el-button>
          </div>
        </el-form-item>
      </div>

      <!-- 示例值 -->
      <el-form-item label="示例值">
        <div class="examples-editor">
          <el-tag
            v-for="(example, exIndex) in slotData.examples || []"
            :key="exIndex"
            closable
            @close="removeExample(exIndex)"
            class="example-tag"
          >
            {{ example }}
          </el-tag>
          <el-input
            v-if="showExampleInput"
            ref="exampleInput"
            v-model="newExample"
            size="small"
            style="width: 120px"
            @blur="addExample"
            @keyup.enter="addExample"
          />
          <el-button 
            v-else
            size="small" 
            @click="showExampleInput = true"
          >
            + 添加示例
          </el-button>
        </div>
        <div class="form-tip">示例值用于帮助用户理解槽位含义</div>
      </el-form-item>

      <!-- 依赖关系 -->
      <el-form-item v-if="availableDependencies.length > 0" label="依赖槽位">
        <el-select 
          v-model="slotData.dependencies"
          multiple
          placeholder="选择依赖的槽位"
          style="width: 100%"
        >
          <el-option
            v-for="dep in availableDependencies"
            :key="dep.value"
            :label="dep.label"
            :value="dep.value"
          />
        </el-select>
        <div class="form-tip">当前槽位依赖这些槽位填充后才会被询问</div>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, inject } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowUp, ArrowDown, Delete } from '@element-plus/icons-vue'
import { SlotDefinitionDTO, SlotType } from '@/types/chat'

// Props
interface Props {
  modelValue: SlotDefinitionDTO
  index: number
}

const props = defineProps<Props>()

// Emits
interface Emits {
  (e: 'update:modelValue', value: SlotDefinitionDTO): void
  (e: 'remove'): void
  (e: 'move-up'): void
  (e: 'move-down'): void
}

const emit = defineEmits<Emits>()

// 响应式数据
const slotData = ref<SlotDefinitionDTO>({ ...props.modelValue })
const nameError = ref('')
const showEnumInput = ref(false)
const showExampleInput = ref(false)
const newEnumOption = ref('')
const newExample = ref('')
const enumInput = ref()
const exampleInput = ref()

// 槽位类型选项
const slotTypes = [
  { value: SlotType.STRING, label: '字符串' },
  { value: SlotType.NUMBER, label: '数字' },
  { value: SlotType.DATE, label: '日期' },
  { value: SlotType.TIME, label: '时间' },
  { value: SlotType.EMAIL, label: '邮箱' },
  { value: SlotType.PHONE, label: '手机号' },
  { value: SlotType.URL, label: '网址' },
  { value: SlotType.BOOLEAN, label: '布尔值' },
  { value: SlotType.ENUM, label: '枚举' },
  { value: SlotType.LIST, label: '列表' },
  { value: SlotType.ENTITY, label: '实体' },
  { value: SlotType.CUSTOM, label: '自定义' }
]

// 计算属性
const isNumberType = computed(() => slotData.value.type === SlotType.NUMBER)
const isStringType = computed(() => slotData.value.type === SlotType.STRING)
const isEnumType = computed(() => slotData.value.type === SlotType.ENUM)

// 从父组件注入所有槽位定义，用于依赖关系选择
const allSlotDefinitions = inject<SlotDefinitionDTO[]>('allSlotDefinitions', [])

const availableDependencies = computed(() => {
  return allSlotDefinitions
    .filter((slot, index) => index !== props.index && slot.name && slot.name !== slotData.value.name)
    .map(slot => ({
      value: slot.name,
      label: slot.label || slot.name
    }))
})

// 方法
const getSlotTypeLabel = (type: SlotType) => {
  const typeOption = slotTypes.find(t => t.value === type)
  return typeOption?.label || type
}

const validateSlotName = () => {
  nameError.value = ''
  const name = slotData.value.name?.trim()
  
  if (!name) {
    nameError.value = '槽位名称不能为空'
    return
  }

  if (!/^[a-zA-Z][a-zA-Z0-9_]*$/.test(name)) {
    nameError.value = '槽位名称必须以字母开头，只能包含字母、数字和下划线'
    return
  }

  // 检查是否与其他槽位重名
  const isDuplicate = allSlotDefinitions.some(
    (slot, index) => index !== props.index && slot.name === name
  )
  
  if (isDuplicate) {
    nameError.value = '槽位名称不能重复'
  }
}

const onTypeChange = (type: SlotType) => {
  // 清理类型特定的配置
  if (type !== SlotType.NUMBER) {
    slotData.value.minValue = undefined
    slotData.value.maxValue = undefined
  }
  
  if (type !== SlotType.STRING) {
    slotData.value.minLength = undefined
    slotData.value.maxLength = undefined
    slotData.value.pattern = undefined
  }
  
  if (type !== SlotType.ENUM) {
    slotData.value.enumOptions = undefined
  }

  // 初始化默认配置
  if (!slotData.value.validation) {
    slotData.value.validation = {}
  }
}

const confirmRemove = async () => {
  try {
    await ElMessageBox.confirm(
      '确认删除此槽位定义吗？',
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    emit('remove')
  } catch {
    // 用户取消删除
  }
}

const addEnumOption = () => {
  const option = newEnumOption.value.trim()
  if (option) {
    if (!slotData.value.enumOptions) {
      slotData.value.enumOptions = []
    }
    if (!slotData.value.enumOptions.includes(option)) {
      slotData.value.enumOptions.push(option)
    }
    newEnumOption.value = ''
  }
  showEnumInput.value = false
}

const removeEnumOption = (index: number) => {
  if (slotData.value.enumOptions) {
    slotData.value.enumOptions.splice(index, 1)
  }
}

const addExample = () => {
  const example = newExample.value.trim()
  if (example) {
    if (!slotData.value.examples) {
      slotData.value.examples = []
    }
    if (!slotData.value.examples.includes(example)) {
      slotData.value.examples.push(example)
    }
    newExample.value = ''
  }
  showExampleInput.value = false
}

const removeExample = (index: number) => {
  if (slotData.value.examples) {
    slotData.value.examples.splice(index, 1)
  }
}

// 监听数据变化
import { watch } from 'vue'

watch(
  () => slotData.value,
  (newVal) => {
    emit('update:modelValue', newVal)
  },
  { deep: true }
)

watch(
  () => props.modelValue,
  (newVal) => {
    slotData.value = { ...newVal }
  },
  { deep: true }
)

// 处理输入框聚焦
watch(showEnumInput, (show) => {
  if (show) {
    nextTick(() => {
      enumInput.value?.focus()
    })
  }
})

watch(showExampleInput, (show) => {
  if (show) {
    nextTick(() => {
      exampleInput.value?.focus()
    })
  }
})

// 初始化
if (!slotData.value.examples) {
  slotData.value.examples = []
}
if (!slotData.value.dependencies) {
  slotData.value.dependencies = []
}
if (!slotData.value.validation) {
  slotData.value.validation = {}
}
</script>

<style scoped>
.slot-definition-editor {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: white;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  border-radius: 6px 6px 0 0;
}

.slot-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.slot-title {
  font-weight: 500;
  color: #303133;
}

.editor-actions {
  display: flex;
  gap: 4px;
}

.el-form {
  padding: 16px;
}

.error-tip {
  font-size: 12px;
  color: #f56c6c;
  margin-top: 4px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.type-specific-config {
  margin: 16px 0;
}

.enum-options-editor,
.examples-editor {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.enum-option-tag,
.example-tag {
  margin: 0;
}

.el-divider {
  margin: 16px 0 8px 0;
}
</style>