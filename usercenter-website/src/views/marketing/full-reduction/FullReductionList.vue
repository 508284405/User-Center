<template>
  <div class="full-reduction-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>满减活动管理</h3>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            创建活动
          </el-button>
        </div>
      </template>

      <!-- 查询条件 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="活动名称">
          <el-input v-model="searchForm.name" placeholder="请输入活动名称" clearable />
        </el-form-item>
        <el-form-item label="活动状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
            <el-option label="草稿" value="DRAFT" />
            <el-option label="进行中" value="ACTIVE" />
            <el-option label="已暂停" value="PAUSED" />
            <el-option label="已过期" value="EXPIRED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建者">
          <el-input v-model="searchForm.createdBy" placeholder="请输入创建者" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="活动名称" width="200" show-overflow-tooltip />
        <el-table-column prop="description" label="活动描述" width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.statusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="150">
          <template #default="{ row }">
            {{ formatDateTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="150">
          <template #default="{ row }">
            {{ formatDateTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="minOrderAmount" label="最小订单金额" width="120">
          <template #default="{ row }">
            ¥{{ (row.minOrderAmount / 100).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="currentUsage" label="使用次数" width="100">
          <template #default="{ row }">
            {{ row.currentUsage }}{{ row.totalUsageLimit ? `/${row.totalUsageLimit}` : '' }}
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" />
        <el-table-column prop="createdBy" label="创建者" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="150">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button type="success" size="small" @click="handleEdit(row)" v-if="row.status === 'DRAFT'">编辑</el-button>
            <el-button type="warning" size="small" @click="handleStatusChange(row)" 
              v-if="['DRAFT', 'PAUSED'].includes(row.status)">
              {{ row.status === 'DRAFT' ? '启动' : '恢复' }}
            </el-button>
            <el-button type="info" size="small" @click="handleStatusChange(row)" v-if="row.status === 'ACTIVE'">暂停</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)" 
              v-if="['DRAFT', 'PAUSED'].includes(row.status)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :small="false"
        :disabled="false"
        :background="true"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 活动详情对话框 -->
    <FullReductionDialog
      v-model="dialogVisible"
      :form-data="editForm"
      :is-edit="isEdit"
      :is-view="isView"
      @success="handleDialogSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FullReductionActivityDTO } from '@/types/full-reduction'
import { fullReductionApi } from '@/api/full-reduction'
import FullReductionDialog from './components/FullReductionDialog.vue'

// 响应式数据
const tableData = ref<FullReductionActivityDTO[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const isView = ref(false)

const searchForm = reactive({
  name: '',
  status: '',
  createdBy: ''
})

const editForm = ref<Partial<FullReductionActivityDTO>>({})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 获取数据
const fetchData = async () => {
  try {
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.size,
      name: searchForm.name || undefined,
      status: searchForm.status || undefined,
      createdBy: searchForm.createdBy || undefined
    }
    
    const response = await fullReductionApi.getPage(params)
    if (response.success) {
      tableData.value = response.data || []
      pagination.total = response.totalCount || 0
    }
  } catch (error) {
    ElMessage.error('获取数据失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    status: '',
    createdBy: ''
  })
  pagination.page = 1
  fetchData()
}

// 新增
const handleCreate = () => {
  isEdit.value = false
  isView.value = false
  editForm.value = {
    priority: 5,
    canCombineWithCoupon: true,
    canCombineWithMemberDiscount: true,
    rules: []
  }
  dialogVisible.value = true
}

// 查看
const handleView = (row: FullReductionActivityDTO) => {
  isEdit.value = false
  isView.value = true
  editForm.value = { ...row }
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: FullReductionActivityDTO) => {
  isEdit.value = true
  isView.value = false
  editForm.value = { ...row }
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: FullReductionActivityDTO) => {
  try {
    await ElMessageBox.confirm('确定要删除这个活动吗？删除后不可恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await fullReductionApi.delete(row.id!)
    if (response.success) {
      ElMessage.success('删除成功')
      fetchData()
    }
  } catch (error) {
    // 用户取消删除或删除失败
  }
}

// 状态变更
const handleStatusChange = async (row: FullReductionActivityDTO) => {
  try {
    let confirmText = ''
    let successText = ''
    let apiCall: Promise<any>

    switch (row.status) {
      case 'DRAFT':
        confirmText = '确定要启动这个活动吗？'
        successText = '启动成功'
        apiCall = fullReductionApi.activate(row.id!)
        break
      case 'ACTIVE':
        confirmText = '确定要暂停这个活动吗？'
        successText = '暂停成功'
        apiCall = fullReductionApi.pause(row.id!)
        break
      case 'PAUSED':
        confirmText = '确定要恢复这个活动吗？'
        successText = '恢复成功'
        apiCall = fullReductionApi.activate(row.id!)
        break
      default:
        return
    }

    await ElMessageBox.confirm(confirmText, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await apiCall
    if (response.success) {
      ElMessage.success(successText)
      fetchData()
    }
  } catch (error) {
    // 用户取消或操作失败
  }
}

// 分页
const handleSizeChange = (val: number) => {
  pagination.size = val
  fetchData()
}

const handleCurrentChange = (val: number) => {
  pagination.page = val
  fetchData()
}

// 对话框成功回调
const handleDialogSuccess = () => {
  dialogVisible.value = false
  fetchData()
}

// 格式化方法
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    DRAFT: 'info',
    ACTIVE: 'success',
    PAUSED: 'warning',
    EXPIRED: 'info',
    CANCELLED: 'danger'
  }
  return typeMap[status] || ''
}

const formatDateTime = (dateTime: string | null) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.full-reduction-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
}

.search-form {
  margin-bottom: 20px;
}
</style>