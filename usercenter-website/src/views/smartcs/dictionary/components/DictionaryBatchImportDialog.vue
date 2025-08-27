<template>
  <el-dialog
    v-model="dialogVisible"
    title="批量导入字典项"
    width="800px"
    @closed="resetForm"
  >
    <div class="batch-import-container">
      <!-- 步骤指示器 -->
      <el-steps :active="currentStep" finish-status="success" class="steps">
        <el-step title="选择导入方式" />
        <el-step title="数据配置" />
        <el-step title="验证数据" />
        <el-step title="导入结果" />
      </el-steps>

      <!-- 步骤1：选择导入方式 -->
      <div v-if="currentStep === 0" class="step-content">
        <div class="import-options">
          <el-radio-group v-model="importMode" size="large">
            <el-radio-button value="text">文本输入</el-radio-button>
            <el-radio-button value="file">文件上传</el-radio-button>
            <el-radio-button value="template">模板下载</el-radio-button>
          </el-radio-group>
        </div>

        <div v-if="importMode === 'text'" class="text-input-mode">
          <el-form :model="configForm" label-width="120px">
            <el-form-item label="字典类型" required>
              <el-select
                v-model="configForm.type"
                placeholder="选择字典类型"
                style="width: 300px"
              >
                <el-option
                  v-for="typeInfo in dictionaryTypes"
                  :key="typeInfo.type"
                  :label="typeInfo.label"
                  :value="typeInfo.type"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="数据格式">
              <el-radio-group v-model="configForm.format">
                <el-radio value="json">JSON格式</el-radio>
                <el-radio value="csv">CSV格式</el-radio>
                <el-radio value="keyvalue">键值对格式</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>

          <el-divider content-position="left">数据输入</el-divider>
          
          <div class="format-examples">
            <el-collapse>
              <el-collapse-item title="查看数据格式示例" name="examples">
                <div class="examples">
                  <div v-if="configForm.format === 'json'" class="example">
                    <h4>JSON格式示例：</h4>
                    <pre>{{ jsonExample }}</pre>
                  </div>
                  <div v-if="configForm.format === 'csv'" class="example">
                    <h4>CSV格式示例：</h4>
                    <pre>{{ csvExample }}</pre>
                  </div>
                  <div v-if="configForm.format === 'keyvalue'" class="example">
                    <h4>键值对格式示例：</h4>
                    <pre>{{ keyValueExample }}</pre>
                  </div>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>

          <el-input
            v-model="textData"
            type="textarea"
            :rows="10"
            placeholder="请输入数据..."
            class="data-input"
          />
        </div>

        <div v-if="importMode === 'file'" class="file-upload-mode">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :on-change="handleFileChange"
            :before-upload="beforeUpload"
            accept=".json,.csv,.txt"
            drag
            class="upload-demo"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 json/csv/txt 格式文件，文件大小不超过10MB
              </div>
            </template>
          </el-upload>
        </div>

        <div v-if="importMode === 'template'" class="template-mode">
          <el-alert
            title="下载导入模板"
            type="info"
            :closable="false"
            show-icon
          >
            <template #default>
              <p>选择字典类型，下载对应的导入模板文件：</p>
            </template>
          </el-alert>
          
          <el-form :model="configForm" label-width="120px" class="template-form">
            <el-form-item label="字典类型" required>
              <el-select
                v-model="configForm.type"
                placeholder="选择字典类型"
                style="width: 300px"
              >
                <el-option
                  v-for="typeInfo in dictionaryTypes"
                  :key="typeInfo.type"
                  :label="typeInfo.label"
                  :value="typeInfo.type"
                />
              </el-select>
            </el-form-item>
          </el-form>

          <div class="template-actions">
            <el-button type="primary" @click="downloadTemplate('json')">
              下载JSON模板
            </el-button>
            <el-button type="primary" @click="downloadTemplate('csv')">
              下载CSV模板
            </el-button>
          </div>
        </div>
      </div>

      <!-- 步骤2：数据配置 -->
      <div v-if="currentStep === 1" class="step-content">
        <el-form :model="configForm" label-width="120px">
          <el-form-item label="租户" >
            <el-input v-model="configForm.tenant" placeholder="留空则应用到全局" />
          </el-form-item>
          <el-form-item label="渠道">
            <el-input v-model="configForm.channel" placeholder="留空则应用到全部渠道" />
          </el-form-item>
          <el-form-item label="域名">
            <el-input v-model="configForm.domain" placeholder="留空则应用到全部域名" />
          </el-form-item>
          <el-form-item label="语言">
            <el-select v-model="configForm.locale" placeholder="选择语言" clearable>
              <el-option label="中文" value="zh-CN" />
              <el-option label="英文" value="en-US" />
              <el-option label="日文" value="ja-JP" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <!-- 步骤3：验证数据 -->
      <div v-if="currentStep === 2" class="step-content">
        <div class="validation-header">
          <el-button type="primary" @click="validateData" :loading="validating">
            验证数据
          </el-button>
          <div class="validation-status">
            <span v-if="validationResult">
              <el-icon v-if="validationResult.valid" color="green"><check /></el-icon>
              <el-icon v-else color="red"><close /></el-icon>
              {{ validationResult.valid ? '数据验证通过' : '数据验证失败' }}
            </span>
          </div>
        </div>

        <div v-if="validationResult && !validationResult.valid" class="validation-errors">
          <el-alert
            title="验证错误"
            type="error"
            :closable="false"
            show-icon
          >
            <ul>
              <li v-for="error in validationResult.errors" :key="error">{{ error }}</li>
            </ul>
          </el-alert>
        </div>

        <div v-if="parsedData.length > 0" class="data-preview">
          <el-divider content-position="left">数据预览（前10条）</el-divider>
          <el-table :data="parsedData.slice(0, 10)" border>
            <el-table-column prop="key" label="键" width="200" />
            <el-table-column prop="value" label="值" width="250" />
            <el-table-column prop="description" label="描述" width="200" />
            <el-table-column prop="weight" label="权重" width="80" />
            <el-table-column prop="enabled" label="启用" width="80">
              <template #default="{ row }">
                {{ row.enabled ? '是' : '否' }}
              </template>
            </el-table-column>
          </el-table>
          <div class="data-summary">
            共解析到 {{ parsedData.length }} 条数据
          </div>
        </div>
      </div>

      <!-- 步骤4：导入结果 -->
      <div v-if="currentStep === 3" class="step-content">
        <div v-if="importResult" class="import-result">
          <el-result
            :icon="importResult.successCount > 0 ? 'success' : 'error'"
            :title="importResult.successCount > 0 ? '导入完成' : '导入失败'"
          >
            <template #sub-title>
              <p>成功导入：{{ importResult.successCount }} 条</p>
              <p>失败：{{ importResult.failureCount }} 条</p>
            </template>
          </el-result>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button v-if="currentStep > 0" @click="prevStep">上一步</el-button>
        <el-button
          v-if="currentStep < 3"
          type="primary"
          @click="nextStep"
          :disabled="!canProceed"
        >
          下一步
        </el-button>
        <el-button
          v-if="currentStep === 2"
          type="success"
          @click="importData"
          :loading="importing"
          :disabled="!validationResult?.valid"
        >
          开始导入
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { UploadFilled, Check, Close } from '@element-plus/icons-vue';
import { dictionaryApi, DictionaryItem, DictionaryType, DictionaryTypeInfo } from '@/api/smartcs/dictionary';

// Props & Emits
const props = defineProps<{
  modelValue: boolean;
  dictionaryTypes: DictionaryTypeInfo[];
}>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  'success': [];
}>();

// 响应式数据
const currentStep = ref(0);
const importMode = ref<'text' | 'file' | 'template'>('text');
const textData = ref('');
const uploadedFile = ref<File | null>(null);
const validating = ref(false);
const importing = ref(false);

const configForm = reactive({
  type: undefined as DictionaryType | undefined,
  format: 'json' as 'json' | 'csv' | 'keyvalue',
  tenant: '',
  channel: '',
  domain: '',
  locale: 'zh-CN'
});

const parsedData = ref<DictionaryItem[]>([]);
const validationResult = ref<{ valid: boolean; errors: string[] } | null>(null);
const importResult = ref<{ successCount: number; failureCount: number } | null>(null);

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

const canProceed = computed(() => {
  switch (currentStep.value) {
    case 0:
      if (importMode.value === 'text') {
        return configForm.type && textData.value.trim();
      } else if (importMode.value === 'file') {
        return uploadedFile.value;
      } else if (importMode.value === 'template') {
        return true; // 模板模式直接可以进行
      }
      return false;
    case 1:
      return true; // 配置步骤总是可以进行
    case 2:
      return validationResult.value?.valid;
    default:
      return false;
  }
});

// 示例数据
const jsonExample = `[
  {
    "key": "停用词1",
    "value": "的|了|在",
    "description": "常用停用词",
    "weight": 1,
    "enabled": true
  }
]`;

const csvExample = `key,value,description,weight,enabled
停用词1,的|了|在,常用停用词,1,true
停用词2,是|都|也,其他停用词,1,true`;

const keyValueExample = `停用词1=的|了|在
停用词2=是|都|也
停用词3=这|那|哪`;

// 方法
const resetForm = () => {
  currentStep.value = 0;
  importMode.value = 'text';
  textData.value = '';
  uploadedFile.value = null;
  parsedData.value = [];
  validationResult.value = null;
  importResult.value = null;
  
  Object.assign(configForm, {
    type: undefined,
    format: 'json',
    tenant: '',
    channel: '',
    domain: '',
    locale: 'zh-CN'
  });
};

const nextStep = () => {
  if (currentStep.value === 0) {
    if (importMode.value === 'template') {
      // 模板模式直接到结果页面
      currentStep.value = 3;
      return;
    }
    // 解析数据
    parseData();
  }
  
  if (currentStep.value < 3) {
    currentStep.value++;
  }
};

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--;
  }
};

const parseData = () => {
  try {
    let items: DictionaryItem[] = [];
    
    if (importMode.value === 'text') {
      items = parseTextData();
    } else if (importMode.value === 'file' && uploadedFile.value) {
      // 文件解析逻辑
      items = parseFileData();
    }
    
    // 添加配置信息
    items.forEach(item => {
      item.type = configForm.type!;
      item.tenant = configForm.tenant || undefined;
      item.channel = configForm.channel || undefined;
      item.domain = configForm.domain || undefined;
      item.locale = configForm.locale || undefined;
    });
    
    parsedData.value = items;
  } catch (error) {
    ElMessage.error('数据解析失败：' + (error as Error).message);
    console.error(error);
  }
};

const parseTextData = (): DictionaryItem[] => {
  const data = textData.value.trim();
  if (!data) return [];
  
  switch (configForm.format) {
    case 'json':
      return JSON.parse(data);
    case 'csv':
      return parseCsvData(data);
    case 'keyvalue':
      return parseKeyValueData(data);
    default:
      throw new Error('不支持的数据格式');
  }
};

const parseCsvData = (data: string): DictionaryItem[] => {
  const lines = data.split('\n').filter(line => line.trim());
  if (lines.length < 2) throw new Error('CSV数据至少需要标题行和数据行');
  
  const headers = lines[0].split(',').map(h => h.trim());
  const items: DictionaryItem[] = [];
  
  for (let i = 1; i < lines.length; i++) {
    const values = lines[i].split(',').map(v => v.trim());
    const item: any = {};
    
    headers.forEach((header, index) => {
      const value = values[index] || '';
      switch (header) {
        case 'weight':
          item[header] = parseInt(value) || 1;
          break;
        case 'enabled':
          item[header] = value === 'true';
          break;
        default:
          item[header] = value;
      }
    });
    
    items.push(item as DictionaryItem);
  }
  
  return items;
};

const parseKeyValueData = (data: string): DictionaryItem[] => {
  const lines = data.split('\n').filter(line => line.trim() && line.includes('='));
  return lines.map(line => {
    const [key, value] = line.split('=').map(s => s.trim());
    return {
      key,
      value,
      description: '',
      weight: 1,
      enabled: true
    } as DictionaryItem;
  });
};

const parseFileData = (): DictionaryItem[] => {
  // 这里应该根据文件类型进行解析
  // 简化实现，实际应该用FileReader
  return [];
};

const handleFileChange = (file: any) => {
  uploadedFile.value = file.raw;
};

const beforeUpload = (file: File) => {
  const isValidType = ['application/json', 'text/csv', 'text/plain'].includes(file.type);
  const isLt10M = file.size / 1024 / 1024 < 10;

  if (!isValidType) {
    ElMessage.error('文件类型不正确！');
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB！');
  }
  
  return false; // 阻止自动上传
};

const validateData = async () => {
  if (parsedData.value.length === 0) {
    ElMessage.error('没有可验证的数据');
    return;
  }
  
  validating.value = true;
  try {
    const response = await dictionaryApi.validateDictionaries({
      items: parsedData.value
    });
    validationResult.value = response.data;
  } catch (error) {
    console.error('验证失败:', error);
    validationResult.value = { valid: false, errors: ['验证请求失败'] };
  } finally {
    validating.value = false;
  }
};

const importData = async () => {
  if (!validationResult.value?.valid) {
    ElMessage.error('数据验证未通过，无法导入');
    return;
  }
  
  importing.value = true;
  try {
    const response = await dictionaryApi.upsertDictionaries({
      items: parsedData.value,
      tenant: configForm.tenant,
      channel: configForm.channel,
      domain: configForm.domain,
      locale: configForm.locale
    });
    
    importResult.value = response.data;
    currentStep.value = 3;
    
    if (response.data.successCount > 0) {
      emit('success');
    }
  } catch (error) {
    console.error('导入失败:', error);
  } finally {
    importing.value = false;
  }
};

const downloadTemplate = (format: 'json' | 'csv') => {
  const typeInfo = props.dictionaryTypes.find(t => t.type === configForm.type);
  if (!typeInfo) {
    ElMessage.error('请先选择字典类型');
    return;
  }
  
  let content = '';
  let filename = '';
  
  if (format === 'json') {
    const template = [{
      key: `示例键_${typeInfo.type}`,
      value: '示例值',
      description: typeInfo.description,
      weight: 1,
      enabled: true
    }];
    content = JSON.stringify(template, null, 2);
    filename = `dictionary_template_${typeInfo.type}.json`;
  } else if (format === 'csv') {
    content = 'key,value,description,weight,enabled\n';
    content += `示例键_${typeInfo.type},示例值,${typeInfo.description},1,true`;
    filename = `dictionary_template_${typeInfo.type}.csv`;
  }
  
  // 下载文件
  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  link.click();
  URL.revokeObjectURL(url);
  
  ElMessage.success('模板下载成功');
};
</script>

<style scoped lang="scss">
.batch-import-container {
  .steps {
    margin-bottom: 30px;
  }

  .step-content {
    min-height: 300px;
    padding: 20px 0;
  }

  .import-options {
    text-align: center;
    margin-bottom: 30px;
  }

  .text-input-mode {
    .format-examples {
      margin: 20px 0;
    }

    .examples {
      .example {
        margin-bottom: 16px;
        
        h4 {
          margin-bottom: 8px;
          color: #606266;
        }
        
        pre {
          background: #f5f7fa;
          padding: 12px;
          border-radius: 4px;
          font-size: 12px;
          line-height: 1.5;
        }
      }
    }

    .data-input {
      margin-top: 16px;
    }
  }

  .file-upload-mode {
    .upload-demo {
      margin-top: 20px;
    }
  }

  .template-mode {
    .template-form {
      margin: 20px 0;
    }

    .template-actions {
      display: flex;
      gap: 12px;
      justify-content: center;
    }
  }

  .validation-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 20px;
  }

  .validation-errors {
    margin-bottom: 20px;
  }

  .data-preview {
    .data-summary {
      margin-top: 12px;
      text-align: right;
      color: #909399;
      font-size: 14px;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  
  .el-button + .el-button {
    margin-left: 0;
  }
}
</style>