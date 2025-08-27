<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑字典项' : '新增字典项'"
    width="600px"
    @closed="resetForm"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      @submit.prevent
    >
      <el-form-item label="字典类型" prop="type">
        <el-select
          v-model="form.type"
          placeholder="选择字典类型"
          style="width: 100%"
          @change="onTypeChange"
        >
          <el-option
            v-for="typeInfo in dictionaryTypes"
            :key="typeInfo.type"
            :label="typeInfo.label"
            :value="typeInfo.type"
          >
            <div>
              <div>{{ typeInfo.label }}</div>
              <div style="font-size: 12px; color: #8c8c8c;">{{ typeInfo.description }}</div>
            </div>
          </el-option>
        </el-select>
        <div v-if="selectedTypeInfo" class="type-info">
          <el-text size="small" type="info">{{ selectedTypeInfo.description }}</el-text>
        </div>
      </el-form-item>

      <el-form-item label="键" prop="key">
        <el-input
          v-model="form.key"
          placeholder="输入键"
          :disabled="isEdit"
        />
        <div v-if="selectedTypeInfo?.keyPattern" class="pattern-info">
          <el-text size="small" type="warning">格式: {{ selectedTypeInfo.keyPattern }}</el-text>
        </div>
      </el-form-item>

      <el-form-item label="值" prop="value">
        <el-input
          v-model="form.value"
          type="textarea"
          :rows="3"
          placeholder="输入值"
        />
        <div v-if="selectedTypeInfo?.valuePattern" class="pattern-info">
          <el-text size="small" type="warning">格式: {{ selectedTypeInfo.valuePattern }}</el-text>
        </div>
      </el-form-item>

      <el-form-item label="描述">
        <el-input
          v-model="form.description"
          placeholder="输入描述信息"
        />
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="权重" prop="weight">
            <el-input-number
              v-model="form.weight"
              :min="0"
              :max="1000"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态">
            <el-switch
              v-model="form.enabled"
              active-text="启用"
              inactive-text="禁用"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider content-position="left">上下文配置</el-divider>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="租户">
            <el-input
              v-model="form.tenant"
              placeholder="租户ID"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="渠道">
            <el-input
              v-model="form.channel"
              placeholder="渠道ID"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="域名">
            <el-input
              v-model="form.domain"
              placeholder="域名"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="语言">
            <el-select
              v-model="form.locale"
              placeholder="选择语言"
              clearable
              style="width: 100%"
            >
              <el-option label="中文" value="zh-CN" />
              <el-option label="英文" value="en-US" />
              <el-option label="日文" value="ja-JP" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 元数据配置 -->
      <el-divider content-position="left">元数据配置</el-divider>
      
      <el-form-item label="元数据">
        <div class="metadata-editor">
          <div
            v-for="(item, index) in metadataItems"
            :key="index"
            class="metadata-item"
          >
            <el-input
              v-model="item.key"
              placeholder="键"
              style="width: 120px"
            />
            <el-input
              v-model="item.value"
              placeholder="值"
              style="width: 200px; margin-left: 8px"
            />
            <el-button
              type="danger"
              size="small"
              @click="removeMetadataItem(index)"
              style="margin-left: 8px"
            >
              删除
            </el-button>
          </div>
          <el-button
            type="primary"
            size="small"
            @click="addMetadataItem"
          >
            添加元数据
          </el-button>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="validateAndSubmit" :loading="submitting">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { dictionaryApi, DictionaryItem, DictionaryType, DictionaryTypeInfo } from '@/api/smartcs/dictionary';

// Props & Emits
const props = defineProps<{
  modelValue: boolean;
  dictionary?: DictionaryItem | null;
  dictionaryTypes: DictionaryTypeInfo[];
}>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  'success': [];
}>();

// Refs
const formRef = ref<FormInstance>();
const submitting = ref(false);

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

const isEdit = computed(() => !!props.dictionary);

const selectedTypeInfo = computed(() => {
  return props.dictionaryTypes.find(type => type.type === form.type);
});

// 表单数据
const form = reactive<DictionaryItem>({
  type: DictionaryType.STOP_WORDS,
  key: '',
  value: '',
  description: '',
  weight: 1,
  enabled: true,
  tenant: '',
  channel: '',
  domain: '',
  locale: 'zh-CN',
  metadata: {}
});

// 元数据编辑
const metadataItems = ref<Array<{ key: string; value: string }>>([]);

// 表单验证规则
const rules: FormRules = {
  type: [
    { required: true, message: '请选择字典类型', trigger: 'change' }
  ],
  key: [
    { required: true, message: '请输入键', trigger: 'blur' },
    { min: 1, max: 200, message: '键长度在1到200个字符', trigger: 'blur' }
  ],
  value: [
    { required: true, message: '请输入值', trigger: 'blur' },
    { min: 1, max: 2000, message: '值长度在1到2000个字符', trigger: 'blur' }
  ],
  weight: [
    { type: 'number', min: 0, max: 1000, message: '权重范围0-1000', trigger: 'blur' }
  ]
};

// 方法
const resetForm = () => {
  if (formRef.value) {
    formRef.value.clearValidate();
  }
  
  Object.assign(form, {
    type: DictionaryType.STOP_WORDS,
    key: '',
    value: '',
    description: '',
    weight: 1,
    enabled: true,
    tenant: '',
    channel: '',
    domain: '',
    locale: 'zh-CN',
    metadata: {}
  });
  
  metadataItems.value = [];
};

const onTypeChange = () => {
  // 类型变更时可以做一些默认值设置
  if (selectedTypeInfo.value) {
    // 根据类型设置默认权重等
    if (selectedTypeInfo.value.type === DictionaryType.STOP_WORDS) {
      form.weight = 1;
    } else if (selectedTypeInfo.value.type === DictionaryType.SENSITIVE_WORDS) {
      form.weight = 10;
    }
  }
};

const addMetadataItem = () => {
  metadataItems.value.push({ key: '', value: '' });
};

const removeMetadataItem = (index: number) => {
  metadataItems.value.splice(index, 1);
};

const validateAndSubmit = async () => {
  if (!formRef.value) return;

  try {
    await formRef.value.validate();
    await submitForm();
  } catch (error) {
    console.error('表单验证失败:', error);
  }
};

const submitForm = async () => {
  submitting.value = true;
  
  try {
    // 处理元数据
    const metadata: Record<string, any> = {};
    metadataItems.value.forEach(item => {
      if (item.key && item.value) {
        metadata[item.key] = item.value;
      }
    });
    
    const submitData: DictionaryItem = {
      ...form,
      metadata
    };
    
    await dictionaryApi.upsertDictionaries({
      items: [submitData]
    });
    
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功');
    emit('success');
  } catch (error) {
    console.error('提交失败:', error);
  } finally {
    submitting.value = false;
  }
};

// 监听器
watch(() => props.dictionary, (newVal) => {
  if (newVal) {
    Object.assign(form, newVal);
    
    // 处理元数据显示
    metadataItems.value = [];
    if (newVal.metadata) {
      Object.entries(newVal.metadata).forEach(([key, value]) => {
        metadataItems.value.push({ key, value: String(value) });
      });
    }
  } else {
    resetForm();
  }
}, { immediate: true });
</script>

<style scoped lang="scss">
.type-info, .pattern-info {
  margin-top: 4px;
}

.metadata-editor {
  .metadata-item {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-select-dropdown__item) {
  height: auto;
  padding: 8px 20px;
}
</style>