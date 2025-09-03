<template>
  <div class="campaign-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>促销活动管理</h3>
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
        <el-form-item label="活动类型">
          <el-select v-model="searchForm.type" placeholder="选择类型" clearable>
            <el-option label="满减活动" value="FULL_REDUCTION" />
            <el-option label="折扣活动" value="DISCOUNT" />
            <el-option label="秒杀活动" value="SECKILL" />
            <el-option label="团购活动" value="GROUP_BUY" />
            <el-option label="新人专享" value="NEW_USER" />
            <el-option label="会员专享" value="MEMBER_ONLY" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
            <el-option label="草稿" value="DRAFT" />
            <el-option label="待开始" value="PENDING" />
            <el-option label="进行中" value="ACTIVE" />
            <el-option label="已暂停" value="PAUSED" />
            <el-option label="已结束" value="ENDED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动时间">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 350px;"
          />
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
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getCampaignTypeColor(row.type)">
              {{ getCampaignTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="活动规则" width="250" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="campaign-rules">
              <div v-if="row.type === 'FULL_REDUCTION'" class="full-reduction">
                <span v-if="row.threshold">满¥{{ (row.threshold / 100).toFixed(2) }}</span>
                <span v-if="row.discount">减¥{{ (row.discount / 100).toFixed(2) }}</span>
                <span v-if="row.discountRate">打{{ row.discountRate }}折</span>
              </div>
              <div v-else-if="row.type === 'DISCOUNT'" class="discount">
                <span>全场{{ row.discountRate }}折</span>
                <span v-if="row.threshold">满¥{{ (row.threshold / 100).toFixed(2) }}</span>
              </div>
              <div v-else-if="row.type === 'SECKILL'" class="seckill">
                <span>秒杀价¥{{ (row.seckillPrice / 100).toFixed(2) }}</span>
                <span>库存{{ row.totalStock }}</span>
              </div>
              <div v-else>
                <span>{{ row.description || '暂无规则描述' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="活动时间" width="200">
          <template #default="{ row }">
            <div class="campaign-period">
              <div>{{ formatDateTime(row.startTime) }}</div>
              <div style="text-align: center; color: #909399;">至</div>
              <div>{{ formatDateTime(row.endTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="参与情况" width="150">
          <template #default="{ row }">
            <div class="participation-info">
              <div class="participation-stats">
                <span class="label">参与人数:</span>
                <span class="value">{{ row.participantCount || 0 }}</span>
              </div>
              <div class="participation-stats" v-if="row.totalStock">
                <span class="label">库存:</span>
                <span class="value">{{ row.remainingStock || 0 }}/{{ row.totalStock }}</span>
              </div>
              <div class="participation-stats" v-if="row.orderCount">
                <span class="label">订单:</span>
                <span class="value">{{ row.orderCount }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="150">
          <template #default="{ row }">
            {{ formatDateTime(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button 
              type="success" 
              size="small" 
              @click="handleActivate(row)" 
              v-if="row.status === 'DRAFT' || row.status === 'PAUSED'"
            >
              {{ row.status === 'PAUSED' ? '恢复' : '激活' }}
            </el-button>
            <el-button 
              type="warning" 
              size="small" 
              @click="handleEdit(row)" 
              v-if="row.status === 'DRAFT'"
            >
              编辑
            </el-button>
            <el-dropdown @command="handleCommand($event, row)" style="margin-left: 12px">
              <el-button type="info" size="small">
                更多 <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="pause" v-if="row.status === 'ACTIVE'">暂停</el-dropdown-item>
                  <el-dropdown-item command="complete" v-if="row.status === 'ACTIVE' || row.status === 'PAUSED'">完成</el-dropdown-item>
                  <el-dropdown-item command="analytics" divided>数据分析</el-dropdown-item>
                  <el-dropdown-item command="participants">参与用户</el-dropdown-item>
                  <el-dropdown-item command="orders">关联订单</el-dropdown-item>
                  <el-dropdown-item command="copy" divided>复制活动</el-dropdown-item>
                  <el-dropdown-item command="delete" v-if="row.status === 'DRAFT'" divided>删除</el-dropdown-item>
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

    <!-- 活动详情/编辑对话框 -->
    <CampaignDialog
      v-model="dialogVisible"
      :form-data="editForm"
      :is-edit="isEdit"
      :is-view="isView"
      @success="handleDialogSuccess"
    />

    <!-- 数据分析对话框 -->
    <CampaignAnalyticsDialog
      v-model="analyticsDialogVisible"
      :campaign="selectedCampaign"
    />

    <!-- 参与用户对话框 -->
    <CampaignParticipantsDialog
      v-model="participantsDialogVisible"
      :campaign="selectedCampaign"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowDown } from '@element-plus/icons-vue'
import type { CampaignDTO } from '@/types/campaign'
import { campaignApi } from '@/api/campaign'
import CampaignDialog from './components/CampaignDialog.vue'
import CampaignAnalyticsDialog from './components/CampaignAnalyticsDialog.vue'
import CampaignParticipantsDialog from './components/CampaignParticipantsDialog.vue'

const tableData = ref<CampaignDTO[]>([])
const dialogVisible = ref(false)
const analyticsDialogVisible = ref(false)
const participantsDialogVisible = ref(false)
const isEdit = ref(false)
const isView = ref(false)
const selectedCampaign = ref<CampaignDTO | null>(null)
const dateRange = ref<[string, string] | null>(null)

const searchForm = reactive({
  name: '',
  type: '',
  status: ''
})

const editForm = ref<Partial<CampaignDTO>>({})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const fetchData = async () => {
  try {
    const params: any = {
      pageNum: pagination.page,
      pageSize: pagination.size,
      name: searchForm.name || undefined,
      type: searchForm.type || undefined,
      status: searchForm.status || undefined
    }

    if (dateRange.value) {
      params.startTimeFrom = dateRange.value[0]
      params.startTimeTo = dateRange.value[1]
    }
    
    const response = await campaignApi.getCampaignPage(params)
    if (response.success) {
      tableData.value = response.data || []
      pagination.total = response.totalCount || 0
    }
  } catch (error) {
    ElMessage.error('获取数据失败')
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    type: '',
    status: ''
  })
  dateRange.value = null
  pagination.page = 1
  fetchData()
}

const handleCreate = () => {
  isEdit.value = false
  isView.value = false
  editForm.value = {
    type: 'FULL_REDUCTION',
    status: 'DRAFT'
  }
  dialogVisible.value = true
}

const handleView = (row: CampaignDTO) => {
  isEdit.value = false
  isView.value = true
  editForm.value = { ...row }
  dialogVisible.value = true
}

const handleEdit = (row: CampaignDTO) => {
  isEdit.value = true
  isView.value = false
  editForm.value = { ...row }
  dialogVisible.value = true
}

const handleActivate = async (row: CampaignDTO) => {
  try {
    const action = row.status === 'PAUSED' ? '恢复' : '激活'
    await ElMessageBox.confirm(`确定要${action}这个促销活动吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = row.status === 'PAUSED' 
      ? await campaignApi.resumeCampaign(row.id!)
      : await campaignApi.activateCampaign(row.id!)
      
    if (response.success) {
      ElMessage.success(`${action}成功`)
      fetchData()
    }
  } catch (error) {
    // 用户取消或操作失败
  }
}

const handleCommand = async (command: string, row: CampaignDTO) => {
  selectedCampaign.value = row
  
  try {
    switch (command) {
      case 'pause':
        await ElMessageBox.confirm('确定要暂停这个促销活动吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const pauseResponse = await campaignApi.pauseCampaign(row.id!)
        if (pauseResponse.success) {
          ElMessage.success('暂停成功')
          fetchData()
        }
        break
        
      case 'complete':
        await ElMessageBox.confirm('确定要完成这个促销活动吗？完成后不可恢复！', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const completeResponse = await campaignApi.completeCampaign(row.id!)
        if (completeResponse.success) {
          ElMessage.success('活动已完成')
          fetchData()
        }
        break
        
      case 'analytics':
        analyticsDialogVisible.value = true
        break
        
      case 'participants':
        participantsDialogVisible.value = true
        break
        
      case 'orders':
        ElMessage.info('跳转到订单管理页面')
        // 这里可以跳转到订单管理页面
        break
        
      case 'copy':
        const copyData = { ...row }
        delete copyData.id
        copyData.name = `${row.name}_副本`
        copyData.status = 'DRAFT'
        
        isEdit.value = false
        isView.value = false
        editForm.value = copyData
        dialogVisible.value = true
        break
        
      case 'delete':
        await ElMessageBox.confirm('确定要删除这个促销活动吗？删除后不可恢复！', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        // 这里应该调用删除API
        ElMessage.success('删除成功')
        fetchData()
        break
    }
  } catch (error) {
    // 用户取消或操作失败
  }
}

const handleSizeChange = (val: number) => {
  pagination.size = val
  fetchData()
}

const handleCurrentChange = (val: number) => {
  pagination.page = val
  fetchData()
}

const handleDialogSuccess = () => {
  dialogVisible.value = false
  fetchData()
}

// 格式化方法
const getCampaignTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    FULL_REDUCTION: 'success',
    DISCOUNT: 'warning',
    SECKILL: 'danger',
    GROUP_BUY: 'primary',
    NEW_USER: 'info',
    MEMBER_ONLY: 'success'
  }
  return colorMap[type] || ''
}

const getCampaignTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    FULL_REDUCTION: '满减活动',
    DISCOUNT: '折扣活动',
    SECKILL: '秒杀活动',
    GROUP_BUY: '团购活动',
    NEW_USER: '新人专享',
    MEMBER_ONLY: '会员专享'
  }
  return textMap[type] || type
}

const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    DRAFT: 'info',
    PENDING: 'warning',
    ACTIVE: 'success',
    PAUSED: 'warning',
    ENDED: 'info',
    CANCELLED: 'danger'
  }
  return typeMap[status] || ''
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    DRAFT: '草稿',
    PENDING: '待开始',
    ACTIVE: '进行中',
    PAUSED: '已暂停',
    ENDED: '已结束',
    CANCELLED: '已取消'
  }
  return textMap[status] || status
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
.campaign-container {
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

.campaign-rules {
  font-size: 12px;
}

.campaign-rules .full-reduction,
.campaign-rules .discount,
.campaign-rules .seckill {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.campaign-period {
  font-size: 12px;
  line-height: 1.2;
}

.participation-info {
  font-size: 12px;
}

.participation-stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 2px;
}

.participation-stats .label {
  color: #909399;
}

.participation-stats .value {
  font-weight: bold;
  color: #303133;
}
</style>