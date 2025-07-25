<template>
  <el-dialog
    :model-value="visible"
    title="创建索引"
    width="600px"
    @close="handleClose"
  >
    <el-form :model="formData" :rules="formRules" ref="formRef" label-width="120px">
      <el-form-item label="索引名称" prop="indexName">
        <el-input v-model="formData.indexName" placeholder="请输入索引名称"></el-input>
      </el-form-item>
      <el-form-item label="索引前缀" prop="prefix">
        <el-input v-model="formData.prefix" placeholder="请输入索引前缀"></el-input>
      </el-form-item>
      <el-form-item label="字段定义" prop="schemaFields">
        <div class="schema-fields">
          <div v-for="(field, index) in formData.schemaFields" :key="index" class="schema-field-item">
            <el-input v-model="field.name" placeholder="字段名" style="width: 120px; margin-right: 10px;"></el-input>
            <el-select v-model="field.type" placeholder="字段类型" style="width: 120px; margin-right: 10px;">
              <el-option label="TEXT" value="TEXT"></el-option>
              <el-option label="TAG" value="TAG"></el-option>
              <el-option label="NUMERIC" value="NUMERIC"></el-option>
              <el-option label="GEO" value="GEO"></el-option>
            </el-select>
            <el-button type="danger" :icon="Delete" circle @click="removeSchemaField(index)"></el-button>
          </div>
          <el-button type="primary" :icon="Plus" @click="addSchemaField">添加字段</el-button>
        </div>
      </el-form-item>
      <el-form-item label="替换现有索引">
        <el-switch v-model="formData.replaceIfExists"></el-switch>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="submitForm">创建</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, defineProps, defineEmits, watch } from 'vue';
import { ElMessage, ElForm, FormRules } from 'element-plus';
import { Plus, Delete } from '@element-plus/icons-vue';
import { createIndex, CreateIndexCmd } from '@/api/smartcs/knowledgeIndex';

interface Props {
  visible: boolean;
}

const props = defineProps<Props>();
const emit = defineEmits(['update:visible', 'created']);

const formRef = ref<InstanceType<typeof ElForm>>();

interface CreateIndexFormData extends CreateIndexCmd {
  schemaFields: { name: string, type: string }[];
}

const formData = reactive<CreateIndexFormData>({
  indexName: '',
  prefix: '',
  schema: {},
  replaceIfExists: false,
  schemaFields: [],
});

const formRules = reactive<FormRules>({
  indexName: [
    { required: true, message: '请输入索引名称', trigger: 'blur' }
  ],
  prefix: [
    { required: true, message: '请输入索引前缀', trigger: 'blur' }
  ],
  schemaFields: [
    {
      validator: (rule, value, callback) => {
        if (formData.schemaFields.length === 0) {
          callback(new Error('请至少定义一个字段'));
        } else {
          const isValid = formData.schemaFields.every(field => field.name && field.type);
          if (!isValid) {
            callback(new Error('字段名和类型不能为空'));
          } else {
            callback();
          }
        }
      },
      trigger: 'change'
    }
  ]
});

const addSchemaField = () => {
  formData.schemaFields.push({ name: '', type: 'TEXT' });
};

const removeSchemaField = (index: number) => {
  formData.schemaFields.splice(index, 1);
};

const submitForm = () => {
  formRef.value?.validate(async (valid) => {
    if (valid) {
      formData.schema = formData.schemaFields.reduce((acc, field) => {
        if (field.name && field.type) {
          acc[field.name] = field.type;
        }
        return acc;
      }, {} as Record<string, string>);

      if (Object.keys(formData.schema).length === 0) {
        ElMessage.error('请至少定义一个有效字段');
        return;
      }

      try {
        const res = await createIndex(formData);
        if (res.success) {
          ElMessage.success('索引创建成功');
          emit('created');
          handleClose();
        } else {
          ElMessage.error('创建失败: ' + res.errMessage);
        }
      } catch (error: any) {
        ElMessage.error('创建异常: ' + (error.message || '未知错误'));
      }
    }
  });
};

const handleClose = () => {
  formRef.value?.resetFields();
  formData.schemaFields = [];
  formData.indexName = '';
  formData.prefix = '';
  formData.replaceIfExists = false;

  emit('update:visible', false);
};

watch(() => props.visible, (newValue) => {
  if (newValue) {
    formData.schemaFields = [];
    formData.indexName = '';
    formData.prefix = '';
    formData.replaceIfExists = false;
    if (formRef.value) {
      formRef.value.resetFields();
    }
  }
});
</script>

<style scoped lang="scss">
.schema-fields {
  .schema-field-item {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
  }
}
</style> 