<template>
  <div class="slot-template-editor">
    <!-- 槽位模板基本配置 -->
    <el-card class="template-config-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>槽位填充配置</span>
          <el-switch
            v-model="templateData.slotFillingEnabled"
            active-text="启用"
            inactive-text="禁用"
            @change="onSlotFillingToggle"
          />
        </div>
      </template>

      <el-form 
        v-if="templateData.slotFillingEnabled"
        :model="templateData" 
        label-width="140px"
        label-position="left"
      >
        <el-form-item label="模板名称">
          <el-input 
            v-model="templateData.templateName" 
            placeholder="请输入模板名称"
            clearable
          />
        </el-form-item>

        <el-form-item label="模板描述">
          <el-input 
            v-model="templateData.description" 
            type="textarea"
            :rows="2"
            placeholder="请输入模板描述"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最大澄清次数">
              <el-input-number 
                v-model="templateData.maxClarificationAttempts"
                :min="1"
                :max="10"
                placeholder="3"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="完整性阈值">
              <el-input-number 
                v-model="templateData.completenessThreshold"
                :min="0"
                :max="1"
                :step="0.1"
                placeholder="0.8"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="阻断后续检索">
          <el-switch
            v-model="templateData.blockRetrievalOnMissing"
            active-text="是"
            inactive-text="否"
          />
          <div class="form-tip">
            启用后，当存在必填槽位缺失时将阻断后续RAG检索
          </div>
        </el-form-item>

        <el-form-item label="提示模板">
          <el-input 
            v-model="templateData.promptTemplate" 
            type="textarea"
            :rows="3"
            placeholder="用于生成澄清问题的提示模板，使用 {slotName} 占位符"
          />
          <div class="form-tip">
            支持占位符：{slotName}, {slotLabel}, {examples}, {hint}
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 槽位定义列表 -->
    <el-card v-if="templateData.slotFillingEnabled" class="slot-definitions-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>槽位定义 ({{ templateData.slotDefinitions.length }})</span>
          <el-button 
            type="primary" 
            size="small"
            :icon="Plus"
            @click="addSlotDefinition"
          >
            添加槽位
          </el-button>
        </div>
      </template>

      <div v-if="templateData.slotDefinitions.length === 0" class="empty-slots">
        <el-empty description="暂无槽位定义">
          <el-button type="primary" @click="addSlotDefinition">添加第一个槽位</el-button>
        </el-empty>
      </div>

      <div v-else class="slot-definitions-list">
        <SlotDefinitionEditor
          v-for="(slot, index) in sortedSlotDefinitions"
          :key="`slot-${index}-${slot.name || 'new'}`"
          v-model="templateData.slotDefinitions[index]"
          :index="index"
          @remove="removeSlotDefinition(index)"
          @move-up="moveSlotUp(index)"
          @move-down="moveSlotDown(index)"
          class="slot-definition-item"
        />
      </div>
    </el-card>

    <!-- 测试区域 -->
    <el-card v-if="templateData.slotFillingEnabled && hasValidSlots" class="test-card" shadow="never">
      <template #header>
        <span>槽位填充测试</span>
      </template>

      <div class="test-section">
        <el-form inline>
          <el-form-item label="测试查询">
            <el-input 
              v-model="testQuery"
              placeholder="输入测试查询语句"
              style="width: 300px"
            />
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary"
              @click="runSlotFillingTest"
              :loading="testLoading"
            >
              测试
            </el-button>
          </el-form-item>
        </el-form>

        <div v-if="testResult" class="test-result">
          <el-alert 
            :type="testResult.success ? 'success' : 'error'"
            :title="testResult.success ? '测试成功' : '测试失败'"
            :closable="false"
            show-icon
          />
          
          <div v-if="testResult.data" class="result-details">
            <h4>槽位识别结果：</h4>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="已填充槽位">
                {{ Object.keys(testResult.data.filledSlots || {}).length }}
              </el-descriptions-item>
              <el-descriptions-item label="缺失槽位">
                {{ (testResult.data.missingSlots || []).length }}
              </el-descriptions-item>
              <el-descriptions-item label="完整性得分">
                {{ (testResult.data.completenessScore || 0).toFixed(2) }}
              </el-descriptions-item>
              <el-descriptions-item label="澄清问题">
                {{ (testResult.data.clarificationQuestions || []).length }}
              </el-descriptions-item>
            </el-descriptions>

            <div v-if="testResult.data.filledSlots" class="filled-slots">
              <h5>已填充槽位：</h5>
              <el-tag 
                v-for="(value, key) in testResult.data.filledSlots" 
                :key="key"
                type="success"
                class="slot-tag"
              >
                {{ key }}: {{ value }}
              </el-tag>
            </div>

            <div v-if="testResult.data.missingSlots && testResult.data.missingSlots.length" class="missing-slots">
              <h5>缺失槽位：</h5>
              <el-tag 
                v-for="slot in testResult.data.missingSlots" 
                :key="slot"
                type="warning"
                class="slot-tag"
              >
                {{ slot }}
              </el-tag>
            </div>

            <div v-if="testResult.data.clarificationQuestions && testResult.data.clarificationQuestions.length" class="clarification-questions">
              <h5>澄清问题：</h5>
              <ol>
                <li v-for="question in testResult.data.clarificationQuestions" :key="question">
                  {{ question }}
                </li>
              </ol>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { SlotTemplateDTO, SlotDefinitionDTO, SlotType } from '@/types/chat'
import { slotTemplateApi } from '@/api/smartcs/intent'
import SlotDefinitionEditor from './SlotDefinitionEditor.vue'

// Props
interface Props {
  modelValue: SlotTemplateDTO
  intentCode: string
  intentId?: string | number
}

const props = defineProps<Props>()

// Emits  
interface Emits {
  (e: 'update:modelValue', value: SlotTemplateDTO): void
}

const emit = defineEmits<Emits>()

// 响应式数据
const templateData = ref<SlotTemplateDTO>({ ...props.modelValue })
const testQuery = ref('')
const testResult = ref<any>(null)
const testLoading = ref(false)

// 计算属性
const sortedSlotDefinitions = computed(() => {
  return [...templateData.value.slotDefinitions].sort((a, b) => (a.order || 0) - (b.order || 0))
})

const hasValidSlots = computed(() => {
  return templateData.value.slotDefinitions.some(slot => slot.name && slot.label)
})

// 监听数据变化
watch(
  () => templateData.value,
  (newVal) => {
    emit('update:modelValue', newVal)
  },
  { deep: true }
)

watch(
  () => props.modelValue,
  (newVal) => {
    templateData.value = { ...newVal }
  },
  { deep: true }
)

// 方法
const onSlotFillingToggle = (enabled: boolean) => {
  if (!enabled) {
    templateData.value.slotDefinitions = []
  } else {
    // 初始化默认配置
    if (!templateData.value.maxClarificationAttempts) {
      templateData.value.maxClarificationAttempts = 3
    }
    if (!templateData.value.completenessThreshold) {
      templateData.value.completenessThreshold = 0.8
    }
    if (!templateData.value.templateName) {
      templateData.value.templateName = `${props.intentCode}槽位模板`
    }
  }
}

const addSlotDefinition = () => {
  const newSlot: SlotDefinitionDTO = {
    name: '',
    label: '',
    type: SlotType.STRING,
    required: false,
    order: templateData.value.slotDefinitions.length,
    examples: [],
    validation: {}
  }
  templateData.value.slotDefinitions.push(newSlot)
}

const removeSlotDefinition = (index: number) => {
  templateData.value.slotDefinitions.splice(index, 1)
  // 重新排序
  templateData.value.slotDefinitions.forEach((slot, i) => {
    slot.order = i
  })
}

const moveSlotUp = (index: number) => {
  if (index > 0) {
    const temp = templateData.value.slotDefinitions[index]
    templateData.value.slotDefinitions[index] = templateData.value.slotDefinitions[index - 1]
    templateData.value.slotDefinitions[index - 1] = temp
    
    // 更新order
    templateData.value.slotDefinitions[index].order = index
    templateData.value.slotDefinitions[index - 1].order = index - 1
  }
}

const moveSlotDown = (index: number) => {
  if (index < templateData.value.slotDefinitions.length - 1) {
    const temp = templateData.value.slotDefinitions[index]
    templateData.value.slotDefinitions[index] = templateData.value.slotDefinitions[index + 1]
    templateData.value.slotDefinitions[index + 1] = temp
    
    // 更新order
    templateData.value.slotDefinitions[index].order = index
    templateData.value.slotDefinitions[index + 1].order = index + 1
  }
}

const runSlotFillingTest = async () => {
  if (!testQuery.value.trim()) {
    ElMessage.warning('请输入测试查询语句')
    return
  }

  if (!props.intentId) {
    ElMessage.warning('需要保存意图后才能进行测试')
    return
  }

  testLoading.value = true
  testResult.value = null

  try {
    const response = await slotTemplateApi.testSlotFilling(props.intentId, {
      query: testQuery.value.trim(),
      slotTemplate: templateData.value
    })

    testResult.value = response
    ElMessage.success('测试完成')
  } catch (error) {
    console.error('槽位填充测试失败:', error)
    testResult.value = {
      success: false,
      message: '测试失败，请检查配置'
    }
    ElMessage.error('测试失败')
  } finally {
    testLoading.value = false
  }
}

// 初始化
if (!templateData.value.slotDefinitions) {
  templateData.value.slotDefinitions = []
}
if (!templateData.value.clarificationTemplates) {
  templateData.value.clarificationTemplates = {}
}
</script>

<style scoped>
.slot-template-editor {
  max-height: 80vh;
  overflow-y: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.template-config-card,
.slot-definitions-card,
.test-card {
  margin-bottom: 20px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.empty-slots {
  padding: 40px 0;
  text-align: center;
}

.slot-definitions-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.slot-definition-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  background: #fafafa;
}

.test-section {
  padding: 0;
}

.test-result {
  margin-top: 20px;
}

.result-details {
  margin-top: 16px;
}

.result-details h4,
.result-details h5 {
  margin: 16px 0 8px 0;
  color: #303133;
}

.slot-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.filled-slots,
.missing-slots,
.clarification-questions {
  margin-top: 16px;
}

.clarification-questions ol {
  margin-left: 20px;
}

.clarification-questions li {
  margin-bottom: 4px;
}
</style>