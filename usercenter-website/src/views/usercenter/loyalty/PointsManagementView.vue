<template>
  <div class="points-management">
    <div class="page-header">
      <h2>积分管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showEarnDialog = true">
          <el-icon><Plus /></el-icon>
          发放积分
        </el-button>
        <el-button @click="showBatchExpireDialog = true">
          <el-icon><Clock /></el-icon>
          批量过期处理
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-title">今日发放积分</div>
              <div class="stat-value">{{ stats.todayEarned || 0 }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-title">今日消耗积分</div>
              <div class="stat-value">{{ stats.todayConsumed || 0 }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-title">总积分余额</div>
              <div class="stat-value">{{ stats.totalBalance || 0 }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-title">即将过期积分</div>
              <div class="stat-value">{{ stats.willExpire || 0 }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 查询表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" :inline="true" label-width="80px">
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable />
        </el-form-item>
        <el-form-item label="交易类型">
          <el-select v-model="searchForm.txnType" placeholder="选择交易类型" clearable>
            <el-option label="获取" value="EARN" />
            <el-option label="扣减" value="DEDUCT" />
            <el-option label="冻结" value="FREEZE" />
            <el-option label="解冻" value="UNFREEZE" />
            <el-option label="过期" value="EXPIRE" />
            <el-option label="冲正" value="REVERSAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadTransactions">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 积分流水表格 -->
    <el-card class="table-card">
      <el-table :data="transactions" v-loading="loading">
        <el-table-column prop="id" label="流水ID" width="100" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="delta" label="积分变化">
          <template #default="{ row }">
            <span :class="{'text-green': row.delta > 0, 'text-red': row.delta < 0}">
              {{ row.delta > 0 ? '+' : '' }}{{ row.delta }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="交易类型">
          <template #default="{ row }">
            <el-tag :type="getTxnTypeTagType(row.type)">
              {{ getTxnTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bizType" label="业务类型" />
        <el-table-column prop="bizId" label="业务ID" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link @click="viewTransaction(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadTransactions"
          @current-change="loadTransactions"
        />
      </div>
    </el-card>

    <!-- 发放积分对话框 -->
    <el-dialog v-model="showEarnDialog" title="发放积分" width="500px">
      <el-form :model="earnForm" :rules="earnRules" ref="earnFormRef" label-width="100px">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="earnForm.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="积分数量" prop="amount">
          <el-input-number 
            v-model="earnForm.amount" 
            :min="1" 
            :max="10000" 
            placeholder="请输入积分数量" 
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="业务类型" prop="bizType">
          <el-select v-model="earnForm.bizType" placeholder="选择业务类型" style="width: 100%">
            <el-option label="订单完成" value="ORDER" />
            <el-option label="促销活动" value="PROMOTION" />
            <el-option label="手动发放" value="MANUAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务ID" prop="bizId">
          <el-input v-model="earnForm.bizId" placeholder="请输入业务ID" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="earnForm.description" type="textarea" placeholder="可选，描述信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEarnDialog = false">取消</el-button>
        <el-button type="primary" @click="earnPoints" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量过期处理对话框 -->
    <el-dialog v-model="showBatchExpireDialog" title="批量过期处理" width="400px">
      <el-form :model="batchExpireForm" label-width="100px">
        <el-form-item label="批量大小">
          <el-input-number 
            v-model="batchExpireForm.batchSize" 
            :min="10" 
            :max="1000" 
            placeholder="批量处理大小" 
            style="width: 100%"
          />
        </el-form-item>
        <el-alert type="warning" show-icon :closable="false">
          <template #title>
            将处理所有已过期但未清理的积分批次，此操作不可撤销
          </template>
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="showBatchExpireDialog = false">取消</el-button>
        <el-button type="danger" @click="processBatchExpire" :loading="submitting">执行</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Clock, Search } from '@element-plus/icons-vue'
import { pointsApi, loyaltyStatsApi } from '@/api/usercenter/loyalty'
import { formatDateTime } from '@/utils/dateFormat'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const showEarnDialog = ref(false)
const showBatchExpireDialog = ref(false)

const stats = reactive({
  todayEarned: 0,
  todayConsumed: 0,
  totalBalance: 0,
  willExpire: 0
})

const searchForm = reactive({
  userId: '',
  txnType: '',
  dateRange: []
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const transactions = ref([])

const earnForm = reactive({
  userId: '',
  amount: null,
  bizType: '',
  bizId: '',
  description: ''
})

const earnRules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入积分数量', trigger: 'blur' }],
  bizType: [{ required: true, message: '请选择业务类型', trigger: 'change' }],
  bizId: [{ required: true, message: '请输入业务ID', trigger: 'blur' }]
}

const batchExpireForm = reactive({
  batchSize: 100
})

const earnFormRef = ref()

// 方法
const loadStats = async () => {
  try {
    const today = new Date().toISOString().split('T')[0]
    const res = await loyaltyStatsApi.getPointsStats({
      startDate: today,
      endDate: today + ' 23:59:59'
    })
    Object.assign(stats, res.data)
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadTransactions = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      size: pagination.size
    }
    
    if (searchForm.userId) params.userId = searchForm.userId
    if (searchForm.txnType) params.type = searchForm.txnType
    if (searchForm.dateRange?.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }

    const res = await pointsApi.getPointTransactions(params)
    transactions.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  Object.assign(searchForm, {
    userId: '',
    txnType: '',
    dateRange: []
  })
  pagination.page = 1
  loadTransactions()
}

const earnPoints = async () => {
  if (!earnFormRef.value) return
  
  try {
    await earnFormRef.value.validate()
    
    submitting.value = true
    await pointsApi.earnPoints({
      ...earnForm,
      idempotencyKey: `manual_${Date.now()}_${Math.random()}`
    })
    
    ElMessage.success('积分发放成功')
    showEarnDialog.value = false
    resetEarnForm()
    loadTransactions()
    loadStats()
  } catch (error) {
    ElMessage.error('积分发放失败')
  } finally {
    submitting.value = false
  }
}

const processBatchExpire = async () => {
  try {
    await ElMessageBox.confirm('确定要执行批量过期处理吗？', '确认操作', {
      type: 'warning'
    })
    
    submitting.value = true
    const res = await pointsApi.processExpiredPoints(batchExpireForm)
    
    ElMessage.success(`批量过期处理完成，共处理 ${res.data.count} 条记录`)
    showBatchExpireDialog.value = false
    loadTransactions()
    loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量过期处理失败')
    }
  } finally {
    submitting.value = false
  }
}

const resetEarnForm = () => {
  Object.assign(earnForm, {
    userId: '',
    amount: null,
    bizType: '',
    bizId: '',
    description: ''
  })
}

const viewTransaction = (row: any) => {
  ElMessageBox.alert(`
    <p><strong>流水ID:</strong> ${row.id}</p>
    <p><strong>用户ID:</strong> ${row.userId}</p>
    <p><strong>积分变化:</strong> ${row.delta}</p>
    <p><strong>交易类型:</strong> ${getTxnTypeText(row.type)}</p>
    <p><strong>业务类型:</strong> ${row.bizType}</p>
    <p><strong>业务ID:</strong> ${row.bizId}</p>
    <p><strong>幂等键:</strong> ${row.idempotencyKey}</p>
    <p><strong>描述:</strong> ${row.description || '无'}</p>
    <p><strong>创建时间:</strong> ${formatDateTime(row.createdAt)}</p>
  `, '积分流水详情', {
    dangerouslyUseHTMLString: true
  })
}

const getTxnTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    'EARN': '获取',
    'DEDUCT': '扣减',
    'FREEZE': '冻结',
    'UNFREEZE': '解冻',
    'EXPIRE': '过期',
    'REVERSAL': '冲正'
  }
  return typeMap[type] || type
}

const getTxnTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    'EARN': 'success',
    'DEDUCT': 'danger',
    'FREEZE': 'warning',
    'UNFREEZE': 'info',
    'EXPIRE': '',
    'REVERSAL': 'danger'
  }
  return typeMap[type] || ''
}

// 生命周期
onMounted(() => {
  loadStats()
  loadTransactions()
})
</script>

<style scoped>
.points-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.header-actions .el-button + .el-button {
  margin-left: 12px;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.stat-content {
  padding: 10px;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.search-card, .table-card {
  margin-bottom: 20px;
}

.pagination-wrapper {
  margin-top: 20px;
  text-align: center;
}

.text-green {
  color: #67C23A;
}

.text-red {
  color: #F56C6C;
}
</style>