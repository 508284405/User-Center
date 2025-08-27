<template>
  <div class="dictionary-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>字典管理</h1>
      <p>管理查询转换器字典数据，支持多阶段处理配置</p>
    </div>

    <!-- 操作栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          新增字典项
        </el-button>
        <el-button @click="showBatchImportDialog">
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>
        <el-button @click="refreshCache">
          <el-icon><Refresh /></el-icon>
          刷新缓存
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-button @click="showPreviewDialog">
          <el-icon><View /></el-icon>
          效果预览
        </el-button>
      </div>
    </div>

    <!-- 查询表单 -->
    <el-form :model="queryForm" inline class="query-form">
      <el-form-item label="字典类型">
        <el-select v-model="queryForm.type" clearable placeholder="选择类型" style="width: 200px">
          <el-option
            v-for="typeInfo in dictionaryTypes"
            :key="typeInfo.type"
            :label="typeInfo.label"
            :value="typeInfo.type"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="关键字">
        <el-input v-model="queryForm.key" placeholder="输入关键字" style="width: 200px" />
      </el-form-item>
      <el-form-item label="租户">
        <el-input v-model="queryForm.tenant" placeholder="租户ID" style="width: 150px" />
      </el-form-item>
      <el-form-item label="渠道">
        <el-input v-model="queryForm.channel" placeholder="渠道ID" style="width: 150px" />
      </el-form-item>
      <el-form-item label="域名">
        <el-input v-model="queryForm.domain" placeholder="域名" style="width: 150px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryForm.enabled" clearable placeholder="选择状态" style="width: 120px">
          <el-option label="启用" :value="true" />
          <el-option label="禁用" :value="false" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadDictionaries">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据表格 -->
    <el-table
      :data="dictionaries"
      v-loading="loading"
      stripe
      border
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="type" label="类型" width="150">
        <template #default="{ row }">
          <el-tag :type="getTypeTagColor(row.type)">
            {{ getTypeLabelByValue(row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="key" label="键" width="200" show-overflow-tooltip />
      <el-table-column prop="value" label="值" width="250" show-overflow-tooltip />
      <el-table-column prop="description" label="描述" width="200" show-overflow-tooltip />
      <el-table-column prop="weight" label="权重" width="80" align="center" />
      <el-table-column prop="enabled" label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.enabled"
            @change="toggleEnabled(row)"
            :loading="row.switchLoading"
          />
        </template>
      </el-table-column>
      <el-table-column prop="tenant" label="租户" width="100" show-overflow-tooltip />
      <el-table-column prop="channel" label="渠道" width="100" show-overflow-tooltip />
      <el-table-column prop="domain" label="域名" width="120" show-overflow-tooltip />
      <el-table-column prop="locale" label="语言" width="80" />
      <el-table-column prop="version" label="版本" width="80" align="center" />
      <el-table-column prop="updatedAt" label="更新时间" width="180" show-overflow-tooltip />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="editDictionary(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteDictionary(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="pagination.pageNo"
      v-model:page-size="pagination.pageSize"
      :total="pagination.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="loadDictionaries"
      @current-change="loadDictionaries"
      class="pagination"
    />

    <!-- 编辑对话框 -->
    <DictionaryEditDialog
      v-model="editDialog.visible"
      :dictionary="editDialog.data"
      :dictionary-types="dictionaryTypes"
      @success="handleEditSuccess"
    />

    <!-- 批量导入对话框 -->
    <DictionaryBatchImportDialog
      v-model="batchImportDialog.visible"
      :dictionary-types="dictionaryTypes"
      @success="handleBatchImportSuccess"
    />

    <!-- 效果预览对话框 -->
    <DictionaryPreviewDialog
      v-model="previewDialog.visible"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Upload, Refresh, View } from '@element-plus/icons-vue';
import { dictionaryApi, DictionaryItem, DictionaryType, DictionaryTypeInfo, DictionaryPageQry } from '@/api/smartcs/dictionary';
import DictionaryEditDialog from './components/DictionaryEditDialog.vue';
import DictionaryBatchImportDialog from './components/DictionaryBatchImportDialog.vue';
import DictionaryPreviewDialog from './components/DictionaryPreviewDialog.vue';

// 响应式数据
const loading = ref(false);
const dictionaries = ref<DictionaryItem[]>([]);
const dictionaryTypes = ref<DictionaryTypeInfo[]>([]);
const selectedItems = ref<DictionaryItem[]>([]);

// 查询表单
const queryForm = reactive<DictionaryPageQry>({
  type: undefined,
  key: '',
  tenant: '',
  channel: '',
  domain: '',
  enabled: undefined,
  pageNo: 1,
  pageSize: 20
});

// 分页
const pagination = reactive({
  pageNo: 1,
  pageSize: 20,
  total: 0
});

// 对话框状态
const editDialog = reactive({
  visible: false,
  data: null as DictionaryItem | null
});

const batchImportDialog = reactive({
  visible: false
});

const previewDialog = reactive({
  visible: false
});

// 计算属性
const getTypeLabelByValue = computed(() => (type: DictionaryType) => {
  const typeInfo = dictionaryTypes.value.find(t => t.type === type);
  return typeInfo?.label || type;
});

// 方法
const loadDictionaryTypes = async () => {
  try {
    const response = await dictionaryApi.getTypes();
    dictionaryTypes.value = response.data;
  } catch (error) {
    console.error('加载字典类型失败:', error);
  }
};

const loadDictionaries = async () => {
  loading.value = true;
  try {
    const query = {
      ...queryForm,
      pageNo: pagination.pageNo,
      pageSize: pagination.pageSize
    };
    const response = await dictionaryApi.queryDictionaries(query);
    dictionaries.value = response.data.data;
    pagination.total = response.data.totalCount;
  } catch (error) {
    console.error('加载字典数据失败:', error);
  } finally {
    loading.value = false;
  }
};

const resetQuery = () => {
  Object.assign(queryForm, {
    type: undefined,
    key: '',
    tenant: '',
    channel: '',
    domain: '',
    enabled: undefined,
    pageNo: 1,
    pageSize: 20
  });
  pagination.pageNo = 1;
  loadDictionaries();
};

const showCreateDialog = () => {
  editDialog.data = null;
  editDialog.visible = true;
};

const showBatchImportDialog = () => {
  batchImportDialog.visible = true;
};

const showPreviewDialog = () => {
  previewDialog.visible = true;
};

const editDictionary = (dictionary: DictionaryItem) => {
  editDialog.data = { ...dictionary };
  editDialog.visible = true;
};

const deleteDictionary = async (dictionary: DictionaryItem) => {
  try {
    await ElMessageBox.confirm(
      `确定删除字典项 "${dictionary.key}" 吗？`,
      '确认删除',
      { type: 'warning' }
    );
    
    await dictionaryApi.deleteDictionary(dictionary.id!);
    ElMessage.success('删除成功');
    loadDictionaries();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error);
    }
  }
};

const toggleEnabled = async (dictionary: DictionaryItem) => {
  dictionary.switchLoading = true;
  try {
    await dictionaryApi.upsertDictionaries({
      items: [dictionary]
    });
    ElMessage.success('状态更新成功');
  } catch (error) {
    dictionary.enabled = !dictionary.enabled; // 回滚状态
    console.error('状态更新失败:', error);
  } finally {
    dictionary.switchLoading = false;
  }
};

const refreshCache = async () => {
  try {
    await dictionaryApi.refreshCache();
    ElMessage.success('缓存刷新成功');
  } catch (error) {
    console.error('缓存刷新失败:', error);
  }
};

const handleSelectionChange = (selection: DictionaryItem[]) => {
  selectedItems.value = selection;
};

const handleEditSuccess = () => {
  editDialog.visible = false;
  loadDictionaries();
};

const handleBatchImportSuccess = () => {
  batchImportDialog.visible = false;
  loadDictionaries();
};

const getTypeTagColor = (type: DictionaryType): string => {
  const colorMap: Record<string, string> = {
    STOP_WORDS: 'info',
    SENSITIVE_WORDS: 'danger',
    PHONETIC_MAPPING: 'warning',
    PREFIX_COMPLETION: 'success',
    SYNONYM_MAPPING: 'primary',
    SEMANTIC_KEYWORDS: 'info',
    INTENT_PATTERNS: 'warning',
    REWRITE_RULES: 'success'
  };
  return colorMap[type] || '';
};

// 生命周期
onMounted(() => {
  loadDictionaryTypes();
  loadDictionaries();
});
</script>

<style scoped lang="scss">
.dictionary-management {
  padding: 20px;

  .page-header {
    margin-bottom: 20px;
    
    h1 {
      margin: 0 0 8px 0;
      color: #303133;
    }
    
    p {
      margin: 0;
      color: #909399;
      font-size: 14px;
    }
  }

  .toolbar {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;
    
    .toolbar-left, .toolbar-right {
      display: flex;
      gap: 12px;
    }
  }

  .query-form {
    background: #f8f9fa;
    padding: 16px;
    border-radius: 6px;
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}

// 表格样式增强
:deep(.el-table) {
  .el-table__header th {
    background-color: #fafafa;
    color: #606266;
    font-weight: 500;
  }
}

:deep(.el-tag) {
  font-size: 12px;
}
</style>