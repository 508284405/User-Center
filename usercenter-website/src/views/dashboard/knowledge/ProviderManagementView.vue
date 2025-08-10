<template>
  <div class="provider-management">
    <div class="header">
      <div class="title">模型提供商管理</div>
      <el-button type="primary" @click="showCreateDialog" :icon="Plus">
        新增提供商
      </el-button>
    </div>

    <div class="search-bar">
      <el-form :model="searchForm" inline>
        <el-form-item label="提供商名称">
          <el-select
            v-model="searchForm.providerType"
            placeholder="请选择提供商"
            clearable
            @change="handleSearch"
          >
            <el-option
              v-for="option in providerTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">搜索</el-button>
          <el-button @click="handleReset" :icon="Refresh">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="providerType" label="提供商名称">
        <template #default="{ row }">
          {{ getProviderTypeLabel(row.providerType) }}
        </template>
      </el-table-column>
      <el-table-column label="图标" width="120">
        <template #default="{ row }">
          <div class="icon-display">
            <img v-if="row.iconSmall" :src="row.iconSmall" class="provider-icon" alt="图标" />
            <span v-else>-</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="endpoint" label="API端点" show-overflow-tooltip />
      <el-table-column prop="supportedModelTypes" label="支持的模型类型" show-overflow-tooltip />
      <el-table-column label="API Key状态" width="150">
        <template #default="{ row }">
          <el-tag v-if="row.hasApiKey" type="success">已设置</el-tag>
          <el-tag v-else type="warning">未设置</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="API Key" show-overflow-tooltip>
        <template #default="{ row }">
          <span>{{ row.apiKeyMasked || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleEdit(row)" :icon="Edit">
            编辑
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)" :icon="Delete">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pagination.pageIndex"
      v-model:page-size="pagination.pageSize"
      :total="pagination.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />

    <!-- 创建/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="140px"
      >
        <el-form-item label="模型提供商" prop="providerType">
          <el-select v-model="form.providerType" placeholder="请选择模型提供商">
            <el-option
              v-for="option in providerTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="小图标URL" prop="iconSmall">
          <el-input v-model="form.iconSmall" placeholder="请输入小图标URL" />
        </el-form-item>
        <el-form-item label="大图标URL" prop="iconLarge">
          <el-input v-model="form.iconLarge" placeholder="请输入大图标URL" />
        </el-form-item>
        <el-form-item label="API Key" prop="apiKey">
          <el-input
            v-model="form.apiKey"
            type="password"
            :placeholder="isEdit ? '留空表示不修改' : '请输入API Key'"
            show-password
            clearable
          />
          <div v-if="isEdit" class="form-item-tip">
            留空表示保持原有API Key不变
          </div>
        </el-form-item>
        <el-form-item label="API端点" prop="endpoint">
          <el-input v-model="form.endpoint" placeholder="请输入API端点" />
        </el-form-item>
        <el-form-item label="支持的模型类型" prop="supportedModelTypes">
          <el-input
            v-model="form.supportedModelTypes"
            placeholder="请输入支持的模型类型，用逗号分隔"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import { providerApi, type Provider, type CreateProviderRequest, type UpdateProviderRequest, providerTypeOptions } from '@/api/smartcs/provider'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const tableData = ref<Provider[]>([])
const formRef = ref()

const searchForm = reactive({
  providerType: ''
})

const pagination = reactive({
  pageIndex: 1,
  pageSize: 10,
  total: 0
})

const form = reactive<CreateProviderRequest & { id?: number }>({
  providerType: '',
  iconSmall: '',
  iconLarge: '',
  apiKey: '',
  endpoint: '',
  supportedModelTypes: ''
})

const formRules = {
  providerType: [
    { required: true, message: '请选择模型提供商', trigger: 'change' }
  ],
  apiKey: [
    { 
      required: () => !isEdit.value, 
      message: '请输入API Key', 
      trigger: 'blur' 
    }
  ]
}

// 获取数据
const fetchData = async () => {
  try {
    loading.value = true
    const params = {
      pageIndex: pagination.pageIndex,
      pageSize: pagination.pageSize,
      needTotalCount: true,
      providerType: searchForm.providerType || undefined
    }
    const response = await providerApi.getPage(params)
    if (response.success) {
      tableData.value = response.data
      pagination.total = response.totalCount
    }
  } catch (error) {
    console.error('获取数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageIndex = 1
  fetchData()
}

// 重置
const handleReset = () => {
  searchForm.providerType = ''
  pagination.pageIndex = 1
  fetchData()
}

// 分页
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchData()
}

const handleCurrentChange = (page: number) => {
  pagination.pageIndex = page
  fetchData()
}

// 显示创建对话框
const showCreateDialog = () => {
  dialogTitle.value = '新增模型提供商'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: Provider) => {
  dialogTitle.value = '编辑模型提供商'
  isEdit.value = true
  Object.assign(form, {
    ...row,
    apiKey: '' // 编辑时不回填API Key
  })
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: Provider) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除提供商 "${getProviderTypeLabel(row.providerType)}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await providerApi.delete(row.id!)
    if (response.success && response.data) {
      ElMessage.success('删除成功')
      fetchData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitLoading.value = true
    
    let response
    if (isEdit.value) {
      response = await providerApi.update(form as UpdateProviderRequest)
    } else {
      response = await providerApi.create(form)
    }
    
    if (response.success) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      fetchData()
    }
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 关闭对话框
const handleDialogClose = () => {
  formRef.value?.resetFields()
  resetForm()
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    id: undefined,
    providerType: '',
    iconSmall: '',
    iconLarge: '',
    apiKey: '',
    endpoint: '',
    supportedModelTypes: ''
  })
}

// 获取提供商类型标签
const getProviderTypeLabel = (providerType: string) => {
  const option = providerTypeOptions.find(opt => opt.value === providerType)
  return option ? option.label : providerType
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.provider-management {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.title {
  font-size: 24px;
  font-weight: bold;
}

.search-bar {
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 8px;
}

.icon-display {
  display: flex;
  align-items: center;
  justify-content: center;
}

.provider-icon {
  width: 24px;
  height: 24px;
  object-fit: contain;
}

.el-pagination {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  text-align: right;
}

.el-select {
  width: 100%;
}

.form-item-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}
</style>