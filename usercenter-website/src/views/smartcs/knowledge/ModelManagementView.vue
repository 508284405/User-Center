<template>
  <div class="model-management">
    <div class="header">
      <div class="title">模型实例管理</div>
      <el-button type="primary" @click="showCreateDialog" :icon="Plus">
        新增模型
      </el-button>
    </div>

    <div class="search-bar">
      <el-form :model="searchForm" inline>
        <el-form-item label="提供商">
          <el-select v-model="searchForm.providerId" placeholder="请选择提供商" clearable>
            <el-option
              v-for="provider in providerOptions"
              :key="provider.id"
              :label="provider.providerType"
              :value="provider.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="模型类型">
          <el-select v-model="searchForm.modelType" placeholder="请选择模型类型" clearable multiple>
            <el-option
              v-for="type in modelTypeOptions"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option
              v-for="status in statusOptions"
              :key="status.value"
              :label="status.label"
              :value="status.value"
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
      <el-table-column prop="label" label="模型名称" />
      <el-table-column prop="providerId" label="提供商" width="120">
        <template #default="{ row }">
          {{ getProviderName(row.providerId) }}
        </template>
      </el-table-column>
      <el-table-column prop="modelType" label="模型类型" width="200">
        <template #default="{ row }">
          <div v-if="row.modelType && row.modelType.length > 0">
            <el-tag
              v-for="type in row.modelType"
              :key="type"
              :type="getModelTypeTagType(type)"
              size="small"
              class="model-type-tag"
            >
              {{ getModelTypeLabel(type) }}
            </el-tag>
          </div>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="features" label="能力标签" show-overflow-tooltip>
        <template #default="{ row }">
          <div v-if="row.features">
            <el-tag
              v-for="feature in row.features.split(',')"
              :key="feature"
              size="small"
              class="feature-tag"
            >
              {{ feature }}
            </el-tag>
          </div>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="废弃" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.deprecated" type="danger">已废弃</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="负载均衡" width="100">
        <template #default="{ row }">
          <el-switch
            v-model="row.loadBalancingEnabled"
            @change="handleLoadBalancingChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button
            :type="row.status === 'active' ? 'warning' : 'success'"
            size="small"
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 'active' ? '禁用' : '启用' }}
          </el-button>
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
      width="700px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="提供商" prop="providerId">
          <el-select v-model="form.providerId" placeholder="请选择提供商">
            <el-option
              v-for="provider in providerOptions"
              :key="provider.id"
              :label="provider.providerType"
              :value="provider.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="模型名称" prop="label">
          <el-input v-model="form.label" placeholder="请输入模型名称" />
        </el-form-item>
        <el-form-item label="模型类型" prop="modelType">
          <el-select v-model="form.modelType" placeholder="请选择模型类型" multiple>
            <el-option
              v-for="type in modelTypeOptions"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="能力标签" prop="features">
          <el-input
            v-model="form.features"
            placeholder="请输入能力标签，用逗号分隔"
          />
        </el-form-item>
        <el-form-item label="来源" prop="fetchFrom">
          <el-select v-model="form.fetchFrom" placeholder="请选择模型来源">
            <el-option
              v-for="source in fetchFromOptions"
              :key="source.value"
              :label="source.label"
              :value="source.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="模型属性" prop="modelProperties">
          <el-input
            v-model="form.modelProperties"
            type="textarea"
            :rows="4"
            placeholder="请输入模型属性（JSON格式）"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option
              v-for="status in statusOptions"
              :key="status.value"
              :label="status.label"
              :value="status.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="form.deprecated">是否废弃</el-checkbox>
          <el-checkbox v-model="form.loadBalancingEnabled">启用负载均衡</el-checkbox>
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
import { modelApi, type Model, type CreateModelRequest, type UpdateModelRequest, ModelType, ModelStatus, FetchFrom } from '@/api/smartcs/model'
import { providerApi, type Provider } from '@/api/smartcs/provider'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const tableData = ref<Model[]>([])
const providerOptions = ref<Provider[]>([])
const formRef = ref()

const searchForm = reactive({
  providerId: undefined as number | undefined,
  modelType: [] as string[],
  status: ''
})

const pagination = reactive({
  pageIndex: 1,
  pageSize: 10,
  total: 0
})

const form = reactive<CreateModelRequest & { id?: number }>({
  providerId: 0,
  label: '',
  modelType: [] as string[],
  features: '',
  fetchFrom: '',
  modelProperties: '',
  deprecated: false,
  status: 'ACTIVE',
  loadBalancingEnabled: false
})

const formRules = {
  providerId: [
    { required: true, message: '请选择提供商', trigger: 'change' }
  ],
  label: [
    { required: true, message: '请输入模型名称', trigger: 'blur' }
  ],
  modelType: [
    { required: true, message: '请选择模型类型', trigger: 'change', type: 'array', min: 1 }
  ]
}

const modelTypeOptions = [
  { label: 'LLM', value: ModelType.LLM },
  { label: 'TTS', value: ModelType.TTS },
  { label: '文本嵌入', value: ModelType.TEXT_EMBEDDING },
  { label: '重排序', value: ModelType.RERANK },
  { label: '语音转文本', value: ModelType.SPEECH2TEXT }
]

const statusOptions = [
  { label: '激活', value: ModelStatus.ACTIVE },
  { label: '未激活', value: ModelStatus.INACTIVE },
  { label: '禁用', value: ModelStatus.DISABLED }
]

const fetchFromOptions = [
  { label: '预定义模型', value: FetchFrom.PREDEFINED_MODEL },
  { label: '自定义模型', value: FetchFrom.CUSTOM_MODEL }
]

// 获取提供商列表
const fetchProviders = async () => {
  try {
    const response = await providerApi.getPage({ pageSize: 1000, needTotalCount: false })
    if (response.success) {
      providerOptions.value = response.data
      // 如果是新增模式且有提供商数据，默认选中第一个
      if (!isEdit.value && response.data.length > 0) {
        form.providerId = response.data[0].id || 0
      }
    }
  } catch (error) {
    console.error('获取提供商列表失败:', error)
  }
}

// 获取数据
const fetchData = async () => {
  try {
    loading.value = true
    const params = {
      pageIndex: pagination.pageIndex,
      pageSize: pagination.pageSize,
      needTotalCount: true,
      providerId: searchForm.providerId,
      modelType: searchForm.modelType.length > 0 ? searchForm.modelType : undefined,
      status: searchForm.status || undefined
    }
    const response = await modelApi.getPage(params)
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
  searchForm.providerId = undefined
  searchForm.modelType = []
  searchForm.status = ''
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
  dialogTitle.value = '新增模型实例'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: Model) => {
  dialogTitle.value = '编辑模型实例'
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: Model) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除模型 "${row.label}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await modelApi.delete(row.id!)
    if (response.success) {
      ElMessage.success('删除成功')
      fetchData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 切换状态
const handleToggleStatus = async (row: Model) => {
  const newStatus = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  try {
    const response = await modelApi.updateStatus(row.id!, {
      id: row.id!,
      status: newStatus
    })
    if (response.success) {
      ElMessage.success('状态更新成功')
      row.status = newStatus
    }
  } catch (error) {
    console.error('状态更新失败:', error)
  }
}

// 负载均衡切换
const handleLoadBalancingChange = async (row: Model) => {
  try {
    const response = await modelApi.update(row as UpdateModelRequest)
    if (response.success) {
      ElMessage.success('负载均衡设置已更新')
    } else {
      row.loadBalancingEnabled = !row.loadBalancingEnabled
    }
  } catch (error) {
    row.loadBalancingEnabled = !row.loadBalancingEnabled
    console.error('负载均衡设置失败:', error)
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    
    // 验证 JSON 格式
    if (form.modelProperties) {
      try {
        JSON.parse(form.modelProperties)
      } catch {
        ElMessage.error('模型属性必须是有效的JSON格式')
        return
      }
    }
    
    submitLoading.value = true
    
    let response
    if (isEdit.value) {
      response = await modelApi.update(form as UpdateModelRequest)
    } else {
      response = await modelApi.create(form)
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
  const defaultProviderId = providerOptions.value.length > 0 ? providerOptions.value[0].id || 0 : 0
  Object.assign(form, {
    id: undefined,
    providerId: defaultProviderId,
    label: '',
    modelType: [],
    features: '',
    fetchFrom: '',
    modelProperties: '',
    deprecated: false,
    status: 'ACTIVE',
    loadBalancingEnabled: false
  })
}

// 获取提供商名称
const getProviderName = (providerId: number) => {
  const provider = providerOptions.value.find(p => p.id === providerId)
  return provider ? provider.providerType : '-'
}

// 获取模型类型标签样式
const getModelTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    'LLM': 'primary',
    'TTS': 'success',
    'TEXT_EMBEDDING': 'warning',
    'RERANK': 'info',
    'SPEECH2TEXT': 'danger'
  }
  return typeMap[type] || ''
}

// 获取模型类型标签文本
const getModelTypeLabel = (type: string) => {
  const option = modelTypeOptions.find(opt => opt.value === type)
  return option ? option.label : type
}

// 获取状态标签样式
const getStatusTagType = (status: string) => {
  const statusMap: Record<string, string> = {
    'active': 'success',
    'inactive': 'warning',
    'disabled': 'danger'
  }
  return statusMap[status] || ''
}

// 获取状态标签文本
const getStatusLabel = (status: string) => {
  const option = statusOptions.find(opt => opt.value === status)
  return option ? option.label : status
}

onMounted(() => {
  fetchProviders()
  fetchData()
})
</script>

<style scoped>
.model-management {
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

.feature-tag {
  margin-right: 5px;
  margin-bottom: 5px;
}

.model-type-tag {
  margin-right: 5px;
  margin-bottom: 5px;
}

.el-pagination {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  text-align: right;
}

.el-select {
  width: 200px;
}
</style>