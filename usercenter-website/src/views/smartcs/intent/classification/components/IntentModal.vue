<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="$emit('update:visible', $event)"
    :title="modalTitle"
    width="800px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <!-- 选项卡导航 -->
    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="基本信息" name="basic">
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="120px"
          label-position="left"
        >
      <!-- 基本信息 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="意图代码" prop="code">
            <div class="code-input-group">
              <el-input
                v-model="formData.code"
                placeholder="英文字母、数字和下划线"
                :disabled="isEditMode && !codeEditEnabled"
              />
              <el-button
                v-if="isEditMode"
                :type="codeEditEnabled ? 'warning' : 'primary'"
                size="small"
                @click="toggleCodeEdit"
              >
                {{ codeEditEnabled ? '锁定' : '编辑' }}
              </el-button>
            </div>
            <div class="form-tip">
              {{ isEditMode ? '意图代码创建后默认不可修改，如需修改请点击编辑按钮' : '输入中文名称时会自动生成英文代码' }}
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="意图名称" prop="name">
            <el-input
              v-model="formData.name"
              placeholder="请输入意图名称"
              @input="generateCode"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="2"
          placeholder="请输入意图描述"
        />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="分类目录">
            <el-select
              v-model="formData.catalogId"
              placeholder="选择分类目录"
              clearable
              style="width: 100%"
            >
              <el-option
                v-for="catalog in catalogList"
                :key="catalog.id"
                :label="catalog.name"
                :value="catalog.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态" prop="status">
            <el-select
              v-model="formData.status"
              style="width: 100%"
            >
              <el-option label="激活" value="ACTIVE" />
              <el-option label="停用" value="INACTIVE" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 标签管理 -->
      <el-form-item label="意图标签">
        <div class="label-input-group">
          <el-input
            v-model="newLabel"
            placeholder="输入标签内容"
            @keyup.enter="addLabel"
          />
          <el-button
            type="primary"
            @click="addLabel"
            :icon="Plus"
          >
            添加
          </el-button>
        </div>
        <div class="labels-container">
          <el-tag
            v-for="(label, index) in formData.labels"
            :key="index"
            closable
            @close="removeLabel(index)"
            class="label-tag"
          >
            {{ label }}
          </el-tag>
        </div>
        <div class="form-tip">
          标签用于训练意图分类模型，建议添加3-10个代表性语句
        </div>
      </el-form-item>

      <!-- 边界管理 -->
      <el-form-item label="排除边界">
        <div class="label-input-group">
          <el-input
            v-model="newBoundary"
            placeholder="输入不属于此意图的语句"
            @keyup.enter="addBoundary"
          />
          <el-button
            type="primary"
            @click="addBoundary"
            :icon="Plus"
          >
            添加
          </el-button>
        </div>
        <div class="labels-container">
          <el-tag
            v-for="(boundary, index) in formData.boundaries"
            :key="index"
            type="info"
            closable
            @close="removeBoundary(index)"
            class="label-tag"
          >
            {{ boundary }}
          </el-tag>
        </div>
        <div class="form-tip">
          边界用于明确定义不属于此意图的语句，提高分类准确性
        </div>
      </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="槽位配置" name="slots">
        <SlotTemplateEditor 
          v-model="formData.slotTemplate"
          :intent-code="formData.code || ''"
          :intent-id="formData.id"
        />
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="$emit('update:visible', false)">取消</el-button>
        <el-button
          type="primary"
          @click="handleSave"
          :loading="saving"
        >
          保存
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useSmartCSAdminStore } from '@/stores/admin/admin.js'
import { slotTemplateApi } from '@/api/smartcs/intent'
import SlotTemplateEditor from '@/components/intent/SlotTemplateEditor.vue'

// Props & Emits
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  intentData: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:visible', 'save'])

// Store
const store = useSmartCSAdminStore()

// Refs
const formRef = ref()
const saving = ref(false)
const newLabel = ref('')
const newBoundary = ref('')
const codeEditEnabled = ref(false)
const activeTab = ref('basic')

// 计算属性
const catalogList = computed(() => store.catalogList)
const isEditMode = computed(() => !!props.intentData?.id)
const modalTitle = computed(() => isEditMode.value ? '编辑意图' : '新建意图')

// 表单数据
const formData = reactive({
  id: null,
  code: '',
  name: '',
  description: '',
  catalogId: '',
  status: 'ACTIVE',
  labels: [],
  boundaries: [],
  slotTemplate: {
    templateId: '',
    templateName: '',
    description: '',
    intentCode: '',
    slotDefinitions: [],
    slotFillingEnabled: false,
    maxClarificationAttempts: 3,
    completenessThreshold: 0.8,
    blockRetrievalOnMissing: false,
    promptTemplate: '',
    clarificationTemplates: {},
    language: 'zh-CN',
    version: '1.0',
    extensions: {}
  }
})

// 表单验证规则
const formRules = {
  code: [
    { required: true, message: '请输入意图代码', trigger: 'blur' },
    { 
      pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, 
      message: '代码必须以字母开头，只能包含字母、数字和下划线', 
      trigger: 'blur' 
    }
  ],
  name: [
    { required: true, message: '请输入意图名称', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 方法
const resetForm = () => {
  Object.assign(formData, {
    id: null,
    code: '',
    name: '',
    description: '',
    catalogId: '',
    status: 'ACTIVE',
    labels: [],
    boundaries: [],
    slotTemplate: {
      templateId: '',
      templateName: '',
      description: '',
      intentCode: '',
      slotDefinitions: [],
      slotFillingEnabled: false,
      maxClarificationAttempts: 3,
      completenessThreshold: 0.8,
      blockRetrievalOnMissing: false,
      promptTemplate: '',
      clarificationTemplates: {},
      language: 'zh-CN',
      version: '1.0',
      extensions: {}
    }
  })
  newLabel.value = ''
  newBoundary.value = ''
  codeEditEnabled.value = false
  activeTab.value = 'basic'
  
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

const populateForm = async (data) => {
  Object.assign(formData, {
    id: data.id || null,
    code: data.code || '',
    name: data.name || '',
    description: data.description || '',
    catalogId: data.catalogId || '',
    status: data.status || 'ACTIVE',
    labels: [...(data.labels || [])],
    boundaries: [...(data.boundaries || [])],
    slotTemplate: {
      templateId: '',
      templateName: data.code ? `${data.code}槽位模板` : '',
      description: '',
      intentCode: data.code || '',
      slotDefinitions: [],
      slotFillingEnabled: false,
      maxClarificationAttempts: 3,
      completenessThreshold: 0.8,
      blockRetrievalOnMissing: false,
      promptTemplate: '',
      clarificationTemplates: {},
      language: 'zh-CN',
      version: '1.0',
      extensions: {}
    }
  })
  codeEditEnabled.value = false
  
  // 如果是编辑模式，尝试加载槽位模板
  if (data.id) {
    try {
      const slotTemplateResponse = await slotTemplateApi.getSlotTemplate(data.id)
      if (slotTemplateResponse.success && slotTemplateResponse.data) {
        Object.assign(formData.slotTemplate, slotTemplateResponse.data)
      }
    } catch (error) {
      console.warn('获取槽位模板失败:', error)
      // 不阻断正常流程，槽位模板是可选的
    }
  }
}

const generateCode = () => {
  // 只有在新建模式或代码编辑模式下才自动生成
  if (isEditMode.value && !codeEditEnabled.value) return
  
  const name = formData.name.trim()
  if (!name) {
    formData.code = ''
    return
  }
  
  // 中文转英文的简单映射
  const chineseToEnglishMap = {
    '用户': 'user',
    '订单': 'order',
    '商品': 'product',
    '支付': 'payment',
    '物流': 'logistics',
    '客服': 'service',
    '售后': 'aftersale',
    '退款': 'refund',
    '评价': 'review',
    '优惠': 'discount',
    '促销': 'promotion',
    '活动': 'activity',
    '会员': 'member',
    '积分': 'points',
    '优惠券': 'coupon',
    '推荐': 'recommend',
    '搜索': 'search',
    '分类': 'category',
    '标签': 'tag',
    '咨询': 'inquiry',
    '投诉': 'complaint',
    '建议': 'suggestion',
    '帮助': 'help',
    '功能': 'function',
    '设置': 'setting',
    '配置': 'config',
    '管理': 'manage',
    '系统': 'system',
    '通用': 'common',
    '其他': 'other',
    '问候': 'greeting',
    '再见': 'goodbye',
    '感谢': 'thanks',
    '抱歉': 'sorry',
    '查询': 'query',
    '查找': 'find',
    '获取': 'get',
    '登录': 'login',
    '注册': 'register',
    '修改': 'modify',
    '删除': 'delete',
    '添加': 'add',
    '创建': 'create',
    '更新': 'update',
    '取消': 'cancel',
    '确认': 'confirm',
    '提交': 'submit',
    '保存': 'save',
    '重置': 'reset',
    '导出': 'export',
    '导入': 'import',
    '下载': 'download',
    '上传': 'upload'
  }
  
  let code = ''
  
  // 尝试匹配中文关键词
  for (const [chinese, english] of Object.entries(chineseToEnglishMap)) {
    if (name.includes(chinese)) {
      code = english
      break
    }
  }
  
  // 如果没有匹配到，使用拼音或保持原样
  if (!code) {
    // 移除非字母数字字符，转换为小写
    code = name.replace(/[^a-zA-Z0-9\u4e00-\u9fa5]/g, '').toLowerCase()
    
    // 如果包含中文，可以考虑使用拼音库，这里简化处理
    if (/[\u4e00-\u9fa5]/.test(code)) {
      // 简单处理：如果是中文，给一个默认的英文前缀
      code = 'intent_' + Date.now().toString().slice(-6)
    }
  }
  
  // 确保符合变量命名规范
  if (!/^[a-zA-Z]/.test(code)) {
    code = 'intent_' + code
  }
  
  formData.code = code
}

const toggleCodeEdit = () => {
  codeEditEnabled.value = !codeEditEnabled.value
  if (!codeEditEnabled.value && formData.name) {
    generateCode()
  }
}

const addLabel = () => {
  const label = newLabel.value.trim()
  if (!label) {
    ElMessage.warning('请输入标签内容')
    return
  }
  
  if (formData.labels.includes(label)) {
    ElMessage.warning('标签已存在')
    return
  }
  
  formData.labels.push(label)
  newLabel.value = ''
}

const removeLabel = (index) => {
  formData.labels.splice(index, 1)
}

const addBoundary = () => {
  const boundary = newBoundary.value.trim()
  if (!boundary) {
    ElMessage.warning('请输入边界内容')
    return
  }
  
  if (formData.boundaries.includes(boundary)) {
    ElMessage.warning('边界已存在')
    return
  }
  
  formData.boundaries.push(boundary)
  newBoundary.value = ''
}

const removeBoundary = (index) => {
  formData.boundaries.splice(index, 1)
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
    
    saving.value = true
    
    const saveData = {
      code: formData.code,
      name: formData.name,
      description: formData.description,
      catalogId: formData.catalogId || null,
      status: formData.status,
      labels: formData.labels,
      boundaries: formData.boundaries
    }
    
    let intentId = null
    
    if (isEditMode.value) {
      await store.updateIntent(props.intentData.id, saveData)
      intentId = props.intentData.id
      ElMessage.success('意图更新成功')
    } else {
      const createResult = await store.createIntent(saveData)
      intentId = createResult?.data?.id || createResult?.id
      ElMessage.success('意图创建成功')
    }
    
    // 保存槽位模板（如果启用了槽位填充）
    if (intentId && formData.slotTemplate.slotFillingEnabled) {
      try {
        // 更新槽位模板的intentCode
        const slotTemplateData = {
          ...formData.slotTemplate,
          intentCode: formData.code,
          templateName: formData.slotTemplate.templateName || `${formData.code}槽位模板`
        }
        
        await slotTemplateApi.updateSlotTemplate(intentId, slotTemplateData)
        ElMessage.success('槽位模板保存成功')
      } catch (slotError) {
        console.error('槽位模板保存失败:', slotError)
        ElMessage.warning('意图保存成功，但槽位模板保存失败')
      }
    }
    
    emit('save')
    emit('update:visible', false)
    
  } catch (error) {
    if (error.message) {
      ElMessage.error(error.message)
    }
  } finally {
    saving.value = false
  }
}

// 监听器
watch(() => props.visible, (newVal) => {
  if (newVal) {
    if (props.intentData) {
      populateForm(props.intentData)
    } else {
      resetForm()
    }
  }
})

// 监听意图代码变化，同步更新槽位模板的intentCode
watch(() => formData.code, (newCode) => {
  if (formData.slotTemplate) {
    formData.slotTemplate.intentCode = newCode
    if (!formData.slotTemplate.templateName || formData.slotTemplate.templateName.endsWith('槽位模板')) {
      formData.slotTemplate.templateName = newCode ? `${newCode}槽位模板` : ''
    }
  }
})
</script>

<style scoped>
.label-input-group {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.label-input-group .el-input {
  flex: 1;
}

.labels-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
  min-height: 32px;
  padding: 8px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  background-color: #fafafa;
}

.labels-container:empty::before {
  content: '暂无标签';
  color: #c0c4cc;
  font-size: 14px;
}

.label-tag {
  margin: 0;
}

.code-input-group {
  display: flex;
  gap: 8px;
  align-items: center;
}

.code-input-group .el-input {
  flex: 1;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .label-input-group {
    flex-direction: column;
  }

  .code-input-group {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>