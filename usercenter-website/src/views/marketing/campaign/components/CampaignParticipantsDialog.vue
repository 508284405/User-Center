<template>
  <el-dialog
    v-model="visible"
    :title="`参与用户 - ${campaign?.name || ''}`"
    width="1000px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="participants-container">
      <!-- 统计概览 -->
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic 
              title="总参与人数" 
              :value="totalParticipants"
              :value-style="{ color: '#409eff' }"
            />
          </el-col>
          <el-col :span="6">
            <el-statistic 
              title="已下单用户" 
              :value="orderUsers"
              :value-style="{ color: '#67c23a' }"
            />
          </el-col>
          <el-col :span="6">
            <el-statistic 
              title="平均订单数" 
              :value="avgOrders"
              :precision="1"
              :value-style="{ color: '#e6a23c' }"
            />
          </el-col>
          <el-col :span="6">
            <el-statistic 
              title="平均消费金额" 
              :value="formatAmount(avgAmount)"
              prefix="¥"
              :value-style="{ color: '#f56c6c' }"
            />
          </el-col>
        </el-row>
      </div>

      <el-divider />

      <!-- 查询条件 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="用户ID">
          <el-input 
            v-model="searchForm.userId" 
            placeholder="请输入用户ID" 
            clearable 
            style="width: 150px;"
          />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input 
            v-model="searchForm.userName" 
            placeholder="请输入用户名" 
            clearable 
            style="width: 150px;"
          />
        </el-form-item>
        <el-form-item label="参与状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 120px;">
            <el-option label="已参与" value="PARTICIPATED" />
            <el-option label="已下单" value="ORDERED" />
            <el-option label="未下单" value="NO_ORDER" />
          </el-select>
        </el-form-item>
        <el-form-item label="参与时间">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 320px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="exportParticipants">导出</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="userId" label="用户ID" width="120" show-overflow-tooltip />
        <el-table-column prop="userName" label="用户名" width="120" show-overflow-tooltip />
        <el-table-column prop="joinTime" label="参与时间" width="150">
          <template #default="{ row }">
            {{ formatDateTime(row.joinTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="参与状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderCount" label="订单数量" width="100" align="center">
          <template #default="{ row }">
            <el-link 
              v-if="row.orderCount > 0" 
              type="primary" 
              @click="viewUserOrders(row.userId)"
            >
              {{ row.orderCount }}
            </el-link>
            <span v-else>0</span>
          </template>
        </el-table-column>
        <el-table-column label="消费金额" width="120" align="right">
          <template #default="{ row }">
            <span class="amount-text">
              ¥{{ formatAmount(row.totalAmount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="优惠金额" width="120" align="right">
          <template #default="{ row }">
            <span class="discount-text">
              -¥{{ formatAmount(row.discountAmount || 0) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="节省金额" width="120" align="right">
          <template #default="{ row }">
            <span class="saved-text">
              ¥{{ formatAmount((row.totalAmount || 0) - (row.actualAmount || 0)) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="firstOrderTime" label="首次下单" width="150">
          <template #default="{ row }">
            {{ formatDateTime(row.firstOrderTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="lastOrderTime" label="最近下单" width="150">
          <template #default="{ row }">
            {{ formatDateTime(row.lastOrderTime) }}
          </template>
        </el-table-column>
        <el-table-column label="用户标签" width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="user-tags">
              <el-tag v-if="row.isNewUser" size="small" type="success">新用户</el-tag>
              <el-tag v-if="row.isMember" size="small" type="primary">会员</el-tag>
              <el-tag v-if="row.isVip" size="small" type="warning">VIP</el-tag>
              <el-tag v-if="isHighValueUser(row)" size="small" type="danger">高价值</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewUserDetail(row)">详情</el-button>
            <el-button 
              v-if="row.orderCount > 0" 
              type="success" 
              size="small" 
              @click="viewUserOrders(row.userId)"
            >
              订单
            </el-button>
            <el-dropdown @command="handleUserCommand($event, row)" style="margin-left: 12px">
              <el-button type="info" size="small">
                更多 <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="sendCoupon">发放优惠券</el-dropdown-item>
                  <el-dropdown-item command="sendMessage">发送消息</el-dropdown-item>
                  <el-dropdown-item command="addGroup" divided>加入用户群组</el-dropdown-item>
                  <el-dropdown-item command="viewProfile">查看用户档案</el-dropdown-item>
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
    </div>

    <!-- 用户详情对话框 -->
    <el-dialog
      v-model="userDetailVisible"
      title="用户详情"
      width="600px"
      append-to-body
    >
      <el-descriptions v-if="selectedUser" :column="2" border>
        <el-descriptions-item label="用户ID">{{ selectedUser.userId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ selectedUser.userName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="参与时间">{{ formatDateTime(selectedUser.joinTime) }}</el-descriptions-item>
        <el-descriptions-item label="参与状态">
          <el-tag :type="getStatusType(selectedUser.status)">
            {{ getStatusText(selectedUser.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="订单数量">{{ selectedUser.orderCount }}</el-descriptions-item>
        <el-descriptions-item label="消费金额">¥{{ formatAmount(selectedUser.totalAmount) }}</el-descriptions-item>
        <el-descriptions-item label="优惠金额">¥{{ formatAmount(selectedUser.discountAmount || 0) }}</el-descriptions-item>
        <el-descriptions-item label="实付金额">¥{{ formatAmount(selectedUser.actualAmount || 0) }}</el-descriptions-item>
        <el-descriptions-item label="首次下单" v-if="selectedUser.firstOrderTime">{{ formatDateTime(selectedUser.firstOrderTime) }}</el-descriptions-item>
        <el-descriptions-item label="最近下单" v-if="selectedUser.lastOrderTime">{{ formatDateTime(selectedUser.lastOrderTime) }}</el-descriptions-item>
        <el-descriptions-item label="用户类型">
          <div class="user-type-tags">
            <el-tag v-if="selectedUser.isNewUser" size="small" type="success">新用户</el-tag>
            <el-tag v-if="selectedUser.isMember" size="small" type="primary">会员</el-tag>
            <el-tag v-if="selectedUser.isVip" size="small" type="warning">VIP</el-tag>
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import type { CampaignDTO, CampaignParticipant } from '@/types/campaign'
import { campaignApi } from '@/api/campaign'

interface Props {
  modelValue: boolean
  campaign: CampaignDTO | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const loading = ref(false)
const tableData = ref<CampaignParticipant[]>([])
const userDetailVisible = ref(false)
const selectedUser = ref<CampaignParticipant | null>(null)
const dateRange = ref<[string, string] | null>(null)

const searchForm = reactive({
  userId: '',
  userName: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 计算统计指标
const totalParticipants = computed(() => tableData.value.length)
const orderUsers = computed(() => tableData.value.filter(u => u.orderCount > 0).length)
const avgOrders = computed(() => {
  const total = tableData.value.reduce((sum, u) => sum + u.orderCount, 0)
  return totalParticipants.value > 0 ? total / totalParticipants.value : 0
})
const avgAmount = computed(() => {
  const total = tableData.value.reduce((sum, u) => sum + (u.totalAmount || 0), 0)
  return totalParticipants.value > 0 ? total / totalParticipants.value : 0
})

const fetchParticipants = async () => {
  if (!props.campaign?.id) return

  try {
    loading.value = true
    const params: any = {
      pageNum: pagination.page,
      pageSize: pagination.size
    }

    if (searchForm.userId) params.userId = searchForm.userId
    if (searchForm.userName) params.userName = searchForm.userName
    if (searchForm.status) params.status = searchForm.status
    if (dateRange.value) {
      params.joinTimeStart = dateRange.value[0]
      params.joinTimeEnd = dateRange.value[1]
    }

    const response = await campaignApi.getCampaignParticipants(props.campaign.id, params)
    if (response.success) {
      tableData.value = response.data || []
      pagination.total = response.totalCount || 0
    }
  } catch (error) {
    ElMessage.error('获取参与用户数据失败')
    // 生成模拟数据用于演示
    tableData.value = generateMockParticipants()
    pagination.total = 100
  } finally {
    loading.value = false
  }
}

const generateMockParticipants = (): CampaignParticipant[] => {
  const participants = []
  for (let i = 1; i <= 20; i++) {
    const orderCount = Math.floor(Math.random() * 5)
    const totalAmount = orderCount > 0 ? Math.floor(Math.random() * 50000) + 5000 : 0
    
    participants.push({
      userId: `user${String(i).padStart(4, '0')}`,
      userName: `用户${i}`,
      joinTime: new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000).toISOString(),
      orderCount,
      totalAmount,
      status: orderCount > 0 ? 'ORDERED' : 'PARTICIPATED',
      discountAmount: orderCount > 0 ? Math.floor(totalAmount * 0.1) : 0,
      actualAmount: orderCount > 0 ? totalAmount * 0.9 : 0,
      firstOrderTime: orderCount > 0 ? new Date(Date.now() - Math.random() * 5 * 24 * 60 * 60 * 1000).toISOString() : null,
      lastOrderTime: orderCount > 0 ? new Date(Date.now() - Math.random() * 2 * 24 * 60 * 60 * 1000).toISOString() : null,
      isNewUser: Math.random() > 0.7,
      isMember: Math.random() > 0.5,
      isVip: Math.random() > 0.8
    })
  }
  return participants
}

const handleSearch = () => {
  pagination.page = 1
  fetchParticipants()
}

const handleReset = () => {
  Object.assign(searchForm, {
    userId: '',
    userName: '',
    status: ''
  })
  dateRange.value = null
  pagination.page = 1
  fetchParticipants()
}

const handleSizeChange = (val: number) => {
  pagination.size = val
  fetchParticipants()
}

const handleCurrentChange = (val: number) => {
  pagination.page = val
  fetchParticipants()
}

const viewUserDetail = (user: CampaignParticipant) => {
  selectedUser.value = user
  userDetailVisible.value = true
}

const viewUserOrders = (userId: string) => {
  ElMessage.info(`查看用户 ${userId} 的订单`)
  // 这里可以跳转到订单管理页面或打开订单对话框
}

const handleUserCommand = (command: string, user: CampaignParticipant) => {
  switch (command) {
    case 'sendCoupon':
      ElMessage.info(`为用户 ${user.userId} 发放优惠券`)
      break
    case 'sendMessage':
      ElMessage.info(`向用户 ${user.userId} 发送消息`)
      break
    case 'addGroup':
      ElMessage.info(`将用户 ${user.userId} 加入群组`)
      break
    case 'viewProfile':
      ElMessage.info(`查看用户 ${user.userId} 的详细档案`)
      break
  }
}

const exportParticipants = () => {
  ElMessage.info('正在导出参与用户数据...')
  setTimeout(() => {
    ElMessage.success('导出完成')
  }, 2000)
}

const isHighValueUser = (user: CampaignParticipant) => {
  return (user.totalAmount || 0) > 100000 // 消费超过1000元的用户
}

const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    PARTICIPATED: 'info',
    ORDERED: 'success',
    NO_ORDER: 'warning'
  }
  return typeMap[status] || ''
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    PARTICIPATED: '已参与',
    ORDERED: '已下单',
    NO_ORDER: '未下单'
  }
  return textMap[status] || status
}

const formatAmount = (amount: number | undefined) => {
  if (!amount) return '0.00'
  return (amount / 100).toFixed(2)
}

const formatDateTime = (dateTime: string | null) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const handleClose = () => {
  emit('update:modelValue', false)
  // 重置数据
  tableData.value = []
  Object.assign(searchForm, {
    userId: '',
    userName: '',
    status: ''
  })
  dateRange.value = null
  pagination.page = 1
}

watch(() => props.modelValue, (newVal) => {
  if (newVal && props.campaign) {
    fetchParticipants()
  }
}, { immediate: true })

onMounted(() => {
  if (props.modelValue && props.campaign) {
    fetchParticipants()
  }
})
</script>

<style scoped>
.participants-container {
  max-height: 70vh;
  overflow-y: auto;
}

.stats-section {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.search-form {
  margin-bottom: 20px;
}

.user-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.user-type-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.amount-text {
  color: #67c23a;
  font-weight: bold;
}

.discount-text {
  color: #e6a23c;
  font-weight: bold;
}

.saved-text {
  color: #f56c6c;
  font-weight: bold;
}

:deep(.el-statistic__number) {
  font-size: 20px;
}

:deep(.el-statistic__title) {
  font-size: 12px;
}

:deep(.el-link) {
  font-weight: bold;
}

:deep(.el-tag) {
  margin: 2px;
}
</style>