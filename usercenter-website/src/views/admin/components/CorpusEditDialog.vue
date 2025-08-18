<template>
  <el-dialog
    v-model="visible"
    :title="isEditing ? '编辑语料' : '添加语料'"
    width="600px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-form-item label="语料内容" prop="text">
        <el-input
          v-model="formData.text"
          type="textarea"
          :rows="4"
          placeholder="请输入语料内容"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
      
      <el-form-item label="样本类型" prop="type">
        <el-select
          v-model="formData.type"
          placeholder="请选择样本类型"
          style="width: 100%"
        >
          <el-option label="正样本" value="POSITIVE" />
          <el-option label="负样本" value="NEGATIVE" />
          <el-option label="边界样本" value="BOUNDARY" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="数据来源" prop="source">
        <el-input
          v-model="formData.source"
          placeholder="请输入数据来源（可选）"
          maxlength="100"
        />
      </el-form-item>
      
      <el-form-item label="置信度" prop="confidenceScore">
        <el-input-number
          v-model="formData.confidenceScore"
          :min="0"
          :max="1"
          :step="0.1"
          :precision="2"
          placeholder="0.0 - 1.0"
          style="width: 100%"
        />
        <div class="form-tip">置信度范围：0.0 - 1.0，可选</div>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          type="primary"
          :loading="loading.save"
          @click="handleSave"
        >
          {{ isEditing ? '更新' : '保存' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { sampleApi } from '@/api/smartcs/intent'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  corpusData: {
    type: Object,
    default: null
  },
  intentId: {
    type: [String, Number],
    default: null
  }
})

const emit = defineEmits(['update:visible', 'save'])

// 响应式数据
const formRef = ref()
const loading = reactive({
  save: false
})

const formData = reactive({
  text: '',
  type: 'POSITIVE',
  source: '',
  confidenceScore: null
})

const formRules = {
  text: [
    { required: true, message: '请输入语料内容', trigger: 'blur' },
    { min: 1, max: 500, message: '语料内容长度在 1 到 500 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择样本类型', trigger: 'change' }
  ]
}

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const isEditing = computed(() => {
  return props.corpusData && props.corpusData.id
})

// 监听语料数据变化
watch(() => props.corpusData, (newData) => {
  if (newData) {
    // 编辑模式
    Object.assign(formData, {
      text: newData.text || '',
      type: newData.type || 'POSITIVE',
      source: newData.source || '',
      confidenceScore: newData.confidenceScore || null
    })
  } else {
    // 新增模式
    resetForm()
  }
}, { immediate: true })

// 方法
const resetForm = () => {
  Object.assign(formData, {
    text: '',
    type: 'POSITIVE',
    source: '',
    confidenceScore: null
  })
  
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
    
    loading.save = true
    
    const saveData = {
      text: formData.text,
      type: formData.type,
      source: formData.source || null,
      confidenceScore: formData.confidenceScore
    }
    
    let response
    if (isEditing.value) {
      // 编辑模式 - 目前后端没有更新接口，需要先删除再创建
      ElMessage.warning('编辑功能暂未实现，请删除后重新添加')
      return
    } else {
      // 新增模式
      response = await sampleApi.createSample(props.intentId, saveData)
    }
    
    if (response.success) {
      ElMessage.success(isEditing.value ? '更新成功' : '保存成功')
      emit('save')
      handleClose()
    } else {
      ElMessage.error(response.errMessage || '保存失败')
    }
  } catch (error) {
    console.error('保存语料失败:', error)
    ElMessage.error('保存失败: ' + error.message)
  } finally {
    loading.save = false
  }
}

const handleClose = () => {
  visible.value = false
  resetForm()
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>