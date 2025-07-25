<script setup lang="ts">
import { ref, reactive, defineEmits, defineProps, watch } from 'vue'
import { ElForm, ElFormItem, ElButton, ElInput, ElSelect, ElOption, ElDatePicker } from 'element-plus'

// 定义筛选字段配置类型
interface FilterField {
  name: string;
  label: string;
  type: 'input' | 'select' | 'date' | 'daterange';
  placeholder?: string;
  options?: { label: string; value: string | number }[];
  valueFormat?: string;
}

// 定义组件属性
const props = defineProps<{
  fields: FilterField[];
  initialValues?: Record<string, any>;
}>()

// 定义组件事件
const emit = defineEmits(['search', 'reset'])

// 表单数据和引用
const formRef = ref<InstanceType<typeof ElForm> | null>(null)
const formData = reactive<Record<string, any>>({})

// 监听初始值变化
watch(() => props.initialValues, (newValues) => {
  if (newValues) {
    Object.assign(formData, newValues)
  }
}, { immediate: true, deep: true })

// 搜索处理
const handleSearch = () => {
  emit('search', { ...formData })
}

// 重置处理
const handleReset = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  emit('reset')
}
</script>

<template>
  <el-form ref="formRef" :model="formData" inline class="filter-form">
    <el-form-item 
      v-for="field in fields" 
      :key="field.name" 
      :label="field.label" 
      :prop="field.name"
    >
      <!-- 输入框 -->
      <el-input 
        v-if="field.type === 'input'" 
        v-model="formData[field.name]" 
        :placeholder="field.placeholder || `请输入${field.label}`" 
        clearable
      />
      
      <!-- 下拉选择框 -->
      <el-select 
        v-else-if="field.type === 'select'" 
        v-model="formData[field.name]" 
        :placeholder="field.placeholder || `请选择${field.label}`" 
        clearable
      >
        <el-option 
          v-for="option in field.options" 
          :key="option.value" 
          :label="option.label" 
          :value="option.value" 
        />
      </el-select>
      
      <!-- 日期选择器 -->
      <el-date-picker 
        v-else-if="field.type === 'date'" 
        v-model="formData[field.name]" 
        type="date" 
        :placeholder="field.placeholder || `请选择${field.label}`" 
        :value-format="field.valueFormat || 'YYYY-MM-DD'" 
        clearable
      />
      
      <!-- 日期范围选择器 -->
      <el-date-picker 
        v-else-if="field.type === 'daterange'" 
        v-model="formData[field.name]" 
        type="daterange" 
        range-separator="至" 
        start-placeholder="开始日期" 
        end-placeholder="结束日期" 
        :value-format="field.valueFormat || 'YYYY-MM-DD'" 
        clearable
      />
    </el-form-item>
    
    <el-form-item>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>
.filter-form {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #fff;
  border-radius: 4px;
}
</style> 