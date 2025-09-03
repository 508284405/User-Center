<template>
  <div class="group-buy-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>团购活动管理</h3>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            创建团购
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
            <el-option label="未开始" value="NOT_STARTED" />
            <el-option label="进行中" value="ONGOING" />
            <el-option label="已暂停" value="PAUSED" />
            <el-option label="已结束" value="ENDED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品ID">
          <el-input v-model="searchForm.productId" placeholder="请输入商品ID" clearable />
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
        <el-table-column prop="productName" label="商品名称" width="150" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.statusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="价格信息" width="180">
          <template #default="{ row }">
            <div class="price-info">
              <div class="original-price">原价: ¥{{ (row.originalPrice / 100).toFixed(2) }}</div>
              <div class="group-price">团价: ¥{{ (row.groupPrice / 100).toFixed(2) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="成团要求" width="120" align="center">
          <template #default="{ row }">
            <div class="group-requirement">
              {{ row.requiredParticipants }}人成团
              <div class="timeout">{{ row.groupTimeoutHours }}h超时</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="库存信息" width="120">
          <template #default="{ row }">
            <div class="stock-info">
              <div>总库存: {{ row.totalStock }}</div>
              <div>可用: {{ row.availableStock }}</div>
              <div>已售: {{ row.soldCount }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="团组统计" width="120">
          <template #default="{ row }">
            <div class="group-stats">
              <div>总团数: {{ row.totalGroups }}</div>
              <div>成功: {{ row.successfulGroups }}</div>
              <div class="success-rate">成功率: {{ getSuccessRate(row) }}%</div>
            </div>
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
        <el-table-column prop="createdBy" label="创建者" width="100" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button type="success" size="small" @click="handleGroups(row)">团组</el-button>
            <el-button type="warning" size="small" @click="handleEdit(row)" v-if="row.status === 'NOT_STARTED'">编辑</el-button>
            <el-dropdown @command="handleCommand($event, row)" style="margin-left: 12px">
              <el-button type="info" size="small">
                更多 <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="start" v-if="row.status === 'NOT_STARTED'">启动</el-dropdown-item>
                  <el-dropdown-item command="pause" v-if="row.status === 'ONGOING'">暂停</el-dropdown-item>
                  <el-dropdown-item command="resume" v-if="row.status === 'PAUSED'">恢复</el-dropdown-item>
                  <el-dropdown-item command="end" v-if="['ONGOING', 'PAUSED'].includes(row.status)">结束</el-dropdown-item>
                  <el-dropdown-item command="delete" v-if="row.status === 'NOT_STARTED'" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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
    <GroupBuyActivityDialog
      v-model="dialogVisible"
      :form-data="editForm"
      :is-edit="isEdit"
      :is-view="isView"
      @success="handleDialogSuccess"
    />

    <!-- 团组管理对话框 -->
    <GroupBuyGroupDialog
      v-model="groupDialogVisible"
      :activity="selectedActivity"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowDown } from '@element-plus/icons-vue'
import type { GroupBuyActivityDTO } from '@/types/group-buy'
import { groupBuyApi } from '@/api/group-buy'
import GroupBuyActivityDialog from './components/GroupBuyActivityDialog.vue'
import GroupBuyGroupDialog from './components/GroupBuyGroupDialog.vue'

// 响应式数据
const tableData = ref<GroupBuyActivityDTO[]>([])
const dialogVisible = ref(false)
const groupDialogVisible = ref(false)
const isEdit = ref(false)
const isView = ref(false)
const selectedActivity = ref<GroupBuyActivityDTO | null>(null)

const searchForm = reactive({
  name: '',
  status: '',
  productId: ''
})

const editForm = ref<Partial<GroupBuyActivityDTO>>({})

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
      productId: searchForm.productId ? parseInt(searchForm.productId) : undefined
    }
    
    const response = await groupBuyApi.getActivityPage(params)
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
    productId: ''
  })
  pagination.page = 1
  fetchData()
}

// 新增
const handleCreate = () => {
  isEdit.value = false
  isView.value = false
  editForm.value = {
    requiredParticipants: 2,
    maxParticipants: 10,
    limitPerUser: 1,
    groupTimeoutHours: 24
  }
  dialogVisible.value = true
}

// 查看
const handleView = (row: GroupBuyActivityDTO) => {
  isEdit.value = false
  isView.value = true
  editForm.value = { ...row }
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: GroupBuyActivityDTO) => {
  isEdit.value = true
  isView.value = false
  editForm.value = { ...row }
  dialogVisible.value = true
}

// 查看团组
const handleGroups = (row: GroupBuyActivityDTO) => {
  selectedActivity.value = row
  groupDialogVisible.value = true
}

// 下拉菜单命令处理
const handleCommand = async (command: string, row: GroupBuyActivityDTO) => {
  try {
    let confirmText = ''
    let successText = ''
    let apiCall: Promise<any>

    switch (command) {
      case 'start':
        confirmText = '确定要启动这个团购活动吗？'
        successText = '启动成功'
        apiCall = groupBuyApi.startActivity(row.id!)
        break
      case 'pause':
        confirmText = '确定要暂停这个团购活动吗？'
        successText = '暂停成功'
        apiCall = groupBuyApi.pauseActivity(row.id!)
        break
      case 'resume':
        confirmText = '确定要恢复这个团购活动吗？'
        successText = '恢复成功'
        apiCall = groupBuyApi.startActivity(row.id!)
        break
      case 'end':
        confirmText = '确定要结束这个团购活动吗？'
        successText = '结束成功'
        apiCall = groupBuyApi.endActivity(row.id!)
        break
      case 'delete':
        confirmText = '确定要删除这个团购活动吗？删除后不可恢复！'
        successText = '删除成功'
        apiCall = groupBuyApi.deleteActivity(row.id!)
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
    NOT_STARTED: 'info',
    ONGOING: 'success',
    PAUSED: 'warning',
    ENDED: 'info',
    CANCELLED: 'danger'
  }
  return typeMap[status] || ''
}

const getSuccessRate = (row: GroupBuyActivityDTO) => {
  if (row.totalGroups === 0) return 0
  return Math.round((row.successfulGroups / row.totalGroups) * 100)
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
.group-buy-container {
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

.price-info {
  font-size: 12px;
}

.original-price {
  color: #999;
  text-decoration: line-through;
}

.group-price {
  color: #e6a23c;
  font-weight: bold;
}

.group-requirement {
  font-size: 12px;
  text-align: center;
}

.timeout {
  color: #909399;
  margin-top: 2px;
}

.stock-info {
  font-size: 12px;
}

.group-stats {
  font-size: 12px;
}

.success-rate {
  color: #67c23a;
  font-weight: bold;
}
</style>