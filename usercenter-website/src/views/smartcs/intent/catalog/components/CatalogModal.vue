<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="$emit('update:visible', $event)"
    :title="modalTitle"
    width="600px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      label-position="left"
    >
      <!-- 基本信息 -->
      <el-form-item label="目录名称" prop="name">
        <el-input
          v-model="formData.name"
          placeholder="请输入目录名称"
          @input="generateCode"
        />
        <div class="form-tip">输入中文名称时会自动生成英文代码</div>
      </el-form-item>

      <el-form-item label="目录代码" prop="code">
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
          {{ isEditMode ? '目录代码创建后默认不可修改，如需修改请点击编辑按钮' : '唯一标识，创建后建议不要修改' }}
        </div>
      </el-form-item>

      <el-form-item label="父级目录">
        <el-select
          v-model="formData.parentId"
          placeholder="选择父级目录（留空为顶级目录）"
          clearable
          style="width: 100%"
          :disabled="!!parentIdFromProps"
        >
          <el-option
            v-for="catalog in filteredCatalogOptions"
            :key="catalog.id"
            :label="catalog.name"
            :value="catalog.id"
          />
        </el-select>
        <div class="form-tip" v-if="parentIdFromProps">
          当前正在为"{{ parentName }}"创建子目录
        </div>
      </el-form-item>

      <el-form-item label="描述">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入目录描述"
        />
      </el-form-item>

      <el-form-item label="排序顺序">
        <el-input-number
          v-model="formData.sortOrder"
          :min="0"
          :max="9999"
          placeholder="数字越小排序越靠前"
          style="width: 100%"
        />
        <div class="form-tip">数字越小排序越靠前，默认为0</div>
      </el-form-item>
    </el-form>

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
import { catalogApi } from '@/api/smartcs/intent'

// Props & Emits
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  catalogData: {
    type: Object,
    default: null
  },
  catalogOptions: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:visible', 'save'])

// Refs
const formRef = ref()
const saving = ref(false)
const codeEditEnabled = ref(false)

// 计算属性
const isEditMode = computed(() => !!props.catalogData?.id)
const parentIdFromProps = computed(() => props.catalogData?.parentId)
const parentName = computed(() => props.catalogData?.parentName)

const modalTitle = computed(() => {
  if (parentIdFromProps.value) {
    return `新建子目录 - ${parentName.value}`
  }
  return isEditMode.value ? '编辑目录' : '新建目录'
})

const filteredCatalogOptions = computed(() => {
  if (!isEditMode.value) return props.catalogOptions
  
  // 编辑时过滤掉自己和自己的子级，避免循环引用
  const currentId = props.catalogData?.id
  return props.catalogOptions.filter(option => {
    return option.id !== currentId && !isDescendantOf(option.id, currentId)
  })
})

// 表单数据
const formData = reactive({
  name: '',
  code: '',
  parentId: null,
  description: '',
  sortOrder: 0
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入目录名称', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入目录代码', trigger: 'blur' },
    { 
      pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, 
      message: '代码必须以字母开头，只能包含字母、数字和下划线', 
      trigger: 'blur' 
    }
  ]
}

// 方法
const resetForm = () => {
  Object.assign(formData, {
    name: '',
    code: '',
    parentId: null,
    description: '',
    sortOrder: 0
  })
  codeEditEnabled.value = false
  
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

const populateForm = (data) => {
  Object.assign(formData, {
    name: data.name || '',
    code: data.code || '',
    parentId: data.parentId || null,
    description: data.description || '',
    sortOrder: data.sortOrder || 0
  })
  codeEditEnabled.value = false
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
    '其他': 'other'
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
      code = 'catalog_' + Date.now().toString().slice(-6)
    }
  }
  
  // 确保符合变量命名规范
  if (!/^[a-zA-Z]/.test(code)) {
    code = 'catalog_' + code
  }
  
  formData.code = code
}

const toggleCodeEdit = () => {
  codeEditEnabled.value = !codeEditEnabled.value
  if (!codeEditEnabled.value && formData.name) {
    generateCode()
  }
}

const isDescendantOf = (childId, ancestorId) => {
  // 检查childId是否是ancestorId的后代
  // 这里需要递归检查目录结构，简化实现
  const findParent = (id) => {
    const catalog = props.catalogOptions.find(c => c.id === id)
    return catalog?.parentId
  }
  
  let parentId = findParent(childId)
  while (parentId) {
    if (parentId === ancestorId) return true
    parentId = findParent(parentId)
  }
  return false
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
    
    saving.value = true
    
    const saveData = {
      name: formData.name,
      code: formData.code,
      parentId: formData.parentId || null,
      description: formData.description,
      sortOrder: formData.sortOrder
    }
    
    let response
    if (isEditMode.value) {
      response = await catalogApi.updateCatalog(props.catalogData.id, saveData)
    } else {
      response = await catalogApi.createCatalog(saveData)
    }
    
    if (response.success) {
      ElMessage.success(isEditMode.value ? '目录更新成功' : '目录创建成功')
      emit('save')
      emit('update:visible', false)
    } else {
      ElMessage.error(response.errMessage || '操作失败')
    }
    
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
    if (props.catalogData) {
      populateForm(props.catalogData)
    } else {
      resetForm()
    }
  }
})
</script>

<style scoped>
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
  .code-input-group {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>