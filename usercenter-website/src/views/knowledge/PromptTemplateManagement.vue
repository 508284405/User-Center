<template>
  <div class="prompt-template-management">
    <div class="header">
      <h2>模板管理</h2>
      <el-button type="primary" @click="showCreateDialog">新增模板</el-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-area">
      <el-form :model="searchForm" inline>
        <el-form-item label="模板标识">
          <el-input
            v-model="searchForm.templateKey"
            placeholder="请输入模板标识"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格区域 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      style="width: 100%"
      border
    >
      <el-table-column prop="templateKey" label="模板标识" width="200" />
      <el-table-column prop="templateContent" label="模板内容" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="更新时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.updatedAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.pageIndex"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="模板标识" prop="templateKey">
          <el-input
            v-model="form.templateKey"
            placeholder="请输入模板标识"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="模板内容" prop="templateContent">
          <el-input
            v-model="form.templateContent"
            type="textarea"
            :rows="10"
            placeholder="请输入模板内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { getPageTemplates, createTemplate, updateTemplate, deleteTemplate } from '@/api/smartcs/template';

// 响应式数据
const loading = ref(false);
const tableData = ref([]);
const dialogVisible = ref(false);
const isEdit = ref(false);
const formRef = ref<FormInstance>();

// 搜索表单
const searchForm = reactive({
  templateKey: ''
});

// 分页信息
const pagination = reactive({
  pageIndex: 1,
  pageSize: 10,
  total: 0
});

// 表单数据
const form = reactive({
  id: null as number | null,
  templateKey: '',
  templateContent: ''
});

// 表单验证规则
const formRules: FormRules = {
  templateKey: [
    { required: true, message: '请输入模板标识', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  templateContent: [
    { required: true, message: '请输入模板内容', trigger: 'blur' }
  ]
};

// 计算属性
const dialogTitle = computed(() => isEdit.value ? '编辑模板' : '新增模板');

// 方法
const loadData = async () => {
  loading.value = true;
  try {
    const params = {
      pageIndex: pagination.pageIndex,
      pageSize: pagination.pageSize,
      templateKey: searchForm.templateKey || undefined
    };
    
    const response = await getPageTemplates(params);
    if (response.success) {
      tableData.value = response.data.data || [];
      pagination.total = response.data.totalCount || 0;
    } else {
      ElMessage.error(response.errMessage || '查询失败');
    }
  } catch (error) {
    console.error('查询失败:', error);
    ElMessage.error('网络异常，请重试');
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.pageIndex = 1;
  loadData();
};

const handleReset = () => {
  searchForm.templateKey = '';
  pagination.pageIndex = 1;
  loadData();
};

const handleSizeChange = (size: number) => {
  pagination.pageSize = size;
  pagination.pageIndex = 1;
  loadData();
};

const handleCurrentChange = (page: number) => {
  pagination.pageIndex = page;
  loadData();
};

const showCreateDialog = () => {
  isEdit.value = false;
  dialogVisible.value = true;
  resetForm();
};

const handleEdit = (row: any) => {
  isEdit.value = true;
  dialogVisible.value = true;
  form.id = row.id;
  form.templateKey = row.templateKey;
  form.templateContent = row.templateContent;
};

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除模板 "${row.templateKey}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );

    const response = await deleteTemplate(row.id);
    if (response.success) {
      ElMessage.success('删除成功');
      loadData();
    } else {
      ElMessage.error(response.errMessage || '删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error);
      ElMessage.error('网络异常，请重试');
    }
  }
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    
    const data = {
      templateKey: form.templateKey,
      templateContent: form.templateContent
    };

    let response;
    if (isEdit.value) {
      response = await updateTemplate({
        id: form.id,
        templateContent: form.templateContent
      });
    } else {
      response = await createTemplate(data);
    }

    if (response.success) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功');
      dialogVisible.value = false;
      loadData();
    } else {
      ElMessage.error(response.errMessage || '操作失败');
    }
  } catch (error) {
    console.error('提交失败:', error);
    ElMessage.error('网络异常，请重试');
  }
};

const handleDialogClose = () => {
  formRef.value?.resetFields();
  resetForm();
};

const resetForm = () => {
  form.id = null;
  form.templateKey = '';
  form.templateContent = '';
};

const formatTime = (timestamp: number | string) => {
  if (!timestamp) return '';
  // 强制转换为数值类型
  const timestampNum = typeof timestamp === 'string' ? Number(timestamp) : timestamp;
  return new Date(timestampNum).toLocaleString();
};

// 生命周期
onMounted(() => {
  loadData();
});
</script>

<style scoped>
.prompt-template-management {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-area {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 