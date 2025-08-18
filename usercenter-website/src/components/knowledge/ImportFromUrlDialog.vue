<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { importByUrl, type UrlDocumentImportRequest, type DocumentProcessResult } from '@/api/smartcs/content';
import { modelApi, type Model, ModelType, type PageResponse } from '@/api/smartcs/model';

interface Props {
  modelValue: boolean
  knowledgeBaseId: number
}

const props = defineProps<Props>();
const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'success', result: DocumentProcessResult): void
}>();

const visible = computed({
  get: () => props.modelValue,
  set: (v: boolean) => emit('update:modelValue', v)
});

const loading = ref(false);
const models = ref<Model[]>([]);

const form = ref<UrlDocumentImportRequest>({
  knowledgeBaseId: 0,
  modelId: 0,
  url: '',
  title: '',
  segmentMode: 'general',
  useKbDefaults: true
});

watch(() => props.knowledgeBaseId, (id) => {
  form.value.knowledgeBaseId = id || 0;
}, { immediate: true });

const fetchModels = async () => {
  try {
    const resp = await modelApi.getAvailableModels();
    const list = (resp as PageResponse<Model>).data || [];
    models.value = list.filter(m => (m.modelType || []).includes(ModelType.TEXT_EMBEDDING));
  } catch (e) {
    // 忽略下拉失败
  }
};

onMounted(() => {
  fetchModels();
});

const handleSubmit = async () => {
  if (!form.value.url || !/^https?:\/\//i.test(form.value.url)) {
    ElMessage.error('请输入有效的文档URL (http/https)');
    return;
  }
  if (!form.value.modelId) {
    ElMessage.error('请选择嵌入模型');
    return;
  }
  loading.value = true;
  try {
    const resp = await importByUrl(form.value);
    if (resp && resp.success && resp.data) {
      ElMessage.success('导入并处理成功');
      emit('success', resp.data);
      visible.value = false;
    } else {
      ElMessage.error(resp?.errMessage || '导入失败');
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '导入失败');
  } finally {
    loading.value = false;
  }
};

const handleClose = () => {
  visible.value = false;
};
</script>

<template>
  <el-dialog
    v-model="visible"
    title="通过URL导入文档"
    width="560px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form label-width="120px">
      <el-form-item label="文档URL" required>
        <el-input v-model="form.url" placeholder="例如：https://example.com/file.pdf" />
      </el-form-item>

      <el-form-item label="标题">
        <el-input v-model="form.title" placeholder="可选，不填将自动推断" />
      </el-form-item>

      <el-form-item label="嵌入模型" required>
        <el-select v-model="form.modelId" placeholder="请选择">
          <el-option v-for="m in models" :key="m.id" :label="m.label" :value="m.id as number" />
        </el-select>
      </el-form-item>

      <el-form-item label="使用知识库默认">
        <el-switch v-model="form.useKbDefaults" />
      </el-form-item>

      <el-form-item label="分段模式">
        <el-select v-model="form.segmentMode">
          <el-option label="通用" value="general" />
          <el-option label="父子分段" value="parent_child" />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">开始导入</el-button>
    </template>
  </el-dialog>
  
</template>

<style scoped>
</style>


