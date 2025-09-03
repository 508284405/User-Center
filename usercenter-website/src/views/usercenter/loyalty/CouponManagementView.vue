<template>
  <div class="coupon-management">
    <div class="page-header">
      <h2>优惠券管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          新建券模板
        </el-button>
        <el-button @click="showIssueDialog = true">
          <el-icon><Gift /></el-icon>
          发放优惠券
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-title">今日发放</div>
              <div class="stat-value">{{ stats.todayIssued || 0 }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-title">今日核销</div>
              <div class="stat-value">{{ stats.todayRedeemed || 0 }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-title">活跃模板</div>
              <div class="stat-value">{{ stats.activeTemplates || 0 }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-title">核销率</div>
              <div class="stat-value">{{ (stats.redemptionRate || 0) }}%</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 查询表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" :inline="true" label-width="80px">
        <el-form-item label="模板编码">
          <el-input v-model="searchForm.tplCode" placeholder="请输入模板编码" clearable />
        </el-form-item>
        <el-form-item label="券类型">
          <el-select v-model="searchForm.type" placeholder="选择券类型" clearable>
            <el-option label="现金券" value="CASH" />
            <el-option label="折扣券" value="DISCOUNT" />
            <el-option label="赠品券" value="FREEBIE" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
            <el-option label="激活" value="ACTIVE" />
            <el-option label="暂停" value="PAUSED" />
            <el-option label="完成" value="FINISHED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadTemplates">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 优惠券模板表格 -->
    <el-card class="table-card">
      <el-table :data="templates" v-loading="loading">
        <el-table-column prop="tplCode" label="模板编码" width="120" />
        <el-table-column prop="name" label="券名称" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="getCouponTypeTagType(row.type)">
              {{ getCouponTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="面额/折扣" width="100">
          <template #default="{ row }">
            <span v-if="row.type === 'CASH'">¥{{ row.faceValue }}</span>
            <span v-else-if="row.type === 'DISCOUNT'">{{ (row.discountRate * 10).toFixed(1) }}折</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="thresholdAmount" label="门槛" width="80">
          <template #default="{ row }">
            {{ row.thresholdAmount ? '¥' + row.thresholdAmount : '无门槛' }}
          </template>
        </el-table-column>
        <el-table-column label="发放进度" width="120">
          <template #default="{ row }">
            <el-progress 
              :percentage="(row.issued / row.total * 100)" 
              :stroke-width="6"
              :show-text="false"
            />
            <div class="progress-text">{{ row.issued }}/{{ row.total }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="有效期" width="200">
          <template #default="{ row }">
            <div class="date-range">
              <div>{{ formatDateTime(row.validFrom) }}</div>
              <div>{{ formatDateTime(row.validTo) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link @click="viewTemplate(row)">详情</el-button>
            <el-button link @click="editTemplate(row)" v-if="row.status === 'ACTIVE'">编辑</el-button>
            <el-button link @click="pauseTemplate(row)" v-if="row.status === 'ACTIVE'">暂停</el-button>
            <el-button link @click="resumeTemplate(row)" v-if="row.status === 'PAUSED'">恢复</el-button>
            <el-button link @click="issueFromTemplate(row)" v-if="row.status === 'ACTIVE'">发放</el-button>
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
          @size-change="loadTemplates"
          @current-change="loadTemplates"
        />
      </div>
    </el-card>

    <!-- 创建模板对话框 -->
    <el-dialog v-model="showCreateDialog" title="创建优惠券模板" width="600px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="120px">
        <el-form-item label="模板编码" prop="tplCode">
          <el-input v-model="createForm.tplCode" placeholder="请输入模板编码" />
        </el-form-item>
        <el-form-item label="券名称" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入券名称" />
        </el-form-item>
        <el-form-item label="券类型" prop="type">
          <el-select v-model="createForm.type" placeholder="选择券类型" style="width: 100%" @change="onTypeChange">
            <el-option label="现金券" value="CASH" />
            <el-option label="折扣券" value="DISCOUNT" />
            <el-option label="赠品券" value="FREEBIE" />
          </el-select>
        </el-form-item>
        <el-form-item label="面额" prop="faceValue" v-if="createForm.type === 'CASH'">
          <el-input-number v-model="createForm.faceValue" :min="0" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="折扣率" prop="discountRate" v-if="createForm.type === 'DISCOUNT'">
          <el-input-number 
            v-model="createForm.discountRate" 
            :min="0.1" 
            :max="0.99" 
            :step="0.01" 
            style="width: 100%" 
            placeholder="0.8表示8折"
          />
        </el-form-item>
        <el-form-item label="使用门槛" prop="thresholdAmount">
          <el-input-number v-model="createForm.thresholdAmount" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="有效期" prop="validRange">
          <el-date-picker
            v-model="createForm.validRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="发放期" prop="issueRange">
          <el-date-picker
            v-model="createForm.issueRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="发放总量" prop="total">
          <el-input-number v-model="createForm.total" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="使用约束">
          <el-input 
            v-model="createForm.constraintsJson" 
            type="textarea" 
            :rows="3"
            placeholder="JSON格式的使用约束，可选"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createTemplate" :loading="submitting">创建</el-button>
      </template>
    </el-dialog>

    <!-- 发放优惠券对话框 -->
    <el-dialog v-model="showIssueDialog" title="发放优惠券" width="500px">
      <el-form :model="issueForm" :rules="issueRules" ref="issueFormRef" label-width="100px">
        <el-form-item label="模板编码" prop="tplCode">
          <el-select v-model="issueForm.tplCode" placeholder="选择模板" style="width: 100%">
            <el-option 
              v-for="tpl in activeTemplates" 
              :key="tpl.tplCode" 
              :label="`${tpl.name} (${tpl.tplCode})`" 
              :value="tpl.tplCode" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="发放方式">
          <el-radio-group v-model="issueForm.issueType">
            <el-radio label="single">单个用户</el-radio>
            <el-radio label="batch">批量用户</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="用户ID" prop="userId" v-if="issueForm.issueType === 'single'">
          <el-input v-model="issueForm.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="用户ID列表" prop="userIds" v-if="issueForm.issueType === 'batch'">
          <el-input 
            v-model="issueForm.userIds" 
            type="textarea" 
            :rows="4"
            placeholder="请输入用户ID，用逗号分隔"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showIssueDialog = false">取消</el-button>
        <el-button type="primary" @click="issueCoupon" :loading="submitting">发放</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Gift, Search } from '@element-plus/icons-vue'
import { couponsApi, loyaltyStatsApi } from '@/api/usercenter/loyalty'
import { formatDateTime } from '@/utils/dateFormat'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const showCreateDialog = ref(false)
const showIssueDialog = ref(false)

const stats = reactive({
  todayIssued: 0,
  todayRedeemed: 0,
  activeTemplates: 0,
  redemptionRate: 0
})

const searchForm = reactive({
  tplCode: '',
  type: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const templates = ref([])
const activeTemplates = computed(() => 
  templates.value.filter((t: any) => t.status === 'ACTIVE')
)

const createForm = reactive({
  tplCode: '',
  name: '',
  type: '',
  faceValue: null,
  discountRate: null,
  thresholdAmount: null,
  validRange: [],
  issueRange: [],
  total: null,
  constraintsJson: ''
})

const createRules = {
  tplCode: [{ required: true, message: '请输入模板编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入券名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择券类型', trigger: 'change' }],
  faceValue: [{ required: true, message: '请输入面额', trigger: 'blur' }],
  discountRate: [{ required: true, message: '请输入折扣率', trigger: 'blur' }],
  validRange: [{ required: true, message: '请选择有效期', trigger: 'change' }],
  issueRange: [{ required: true, message: '请选择发放期', trigger: 'change' }],
  total: [{ required: true, message: '请输入发放总量', trigger: 'blur' }]
}

const issueForm = reactive({
  tplCode: '',
  issueType: 'single',
  userId: '',
  userIds: ''
})

const issueRules = {
  tplCode: [{ required: true, message: '请选择模板', trigger: 'change' }],
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  userIds: [{ required: true, message: '请输入用户ID列表', trigger: 'blur' }]
}

const createFormRef = ref()
const issueFormRef = ref()

// 方法
const loadStats = async () => {
  try {
    const res = await loyaltyStatsApi.getCouponStats({
      startDate: new Date().toISOString().split('T')[0]
    })
    Object.assign(stats, res.data)
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadTemplates = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      size: pagination.size
    }
    
    if (searchForm.tplCode) params.tplCode = searchForm.tplCode
    if (searchForm.type) params.type = searchForm.type
    if (searchForm.status) params.status = searchForm.status

    const res = await couponsApi.getCouponTemplates(params)
    templates.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  Object.assign(searchForm, {
    tplCode: '',
    type: '',
    status: ''
  })
  pagination.page = 1
  loadTemplates()
}

const onTypeChange = () => {
  if (createForm.type !== 'CASH') {
    createForm.faceValue = null
  }
  if (createForm.type !== 'DISCOUNT') {
    createForm.discountRate = null
  }
}

const createTemplate = async () => {
  if (!createFormRef.value) return
  
  try {
    await createFormRef.value.validate()
    
    submitting.value = true
    const data: any = {
      ...createForm,
      validFrom: createForm.validRange[0],
      validTo: createForm.validRange[1],
      issueStart: createForm.issueRange[0],
      issueEnd: createForm.issueRange[1]
    }
    
    delete data.validRange
    delete data.issueRange
    
    await couponsApi.createCouponTemplate(data)
    
    ElMessage.success('优惠券模板创建成功')
    showCreateDialog.value = false
    resetCreateForm()
    loadTemplates()
    loadStats()
  } catch (error) {
    ElMessage.error('创建失败')
  } finally {
    submitting.value = false
  }
}

const issueCoupon = async () => {
  if (!issueFormRef.value) return
  
  try {
    await issueFormRef.value.validate()
    
    submitting.value = true
    const data: any = {
      tplCode: issueForm.tplCode,
      idempotencyKey: `issue_${Date.now()}_${Math.random()}`
    }
    
    if (issueForm.issueType === 'single') {
      data.userId = parseInt(issueForm.userId)
    } else {
      data.userIds = issueForm.userIds.split(',').map(id => parseInt(id.trim()))
    }
    
    const res = await couponsApi.issueCoupon(data)
    
    ElMessage.success(`优惠券发放成功，共发放 ${res.data.count} 张`)
    showIssueDialog.value = false
    resetIssueForm()
    loadTemplates()
    loadStats()
  } catch (error) {
    ElMessage.error('发放失败')
  } finally {
    submitting.value = false
  }
}

const resetCreateForm = () => {
  Object.assign(createForm, {
    tplCode: '',
    name: '',
    type: '',
    faceValue: null,
    discountRate: null,
    thresholdAmount: null,
    validRange: [],
    issueRange: [],
    total: null,
    constraintsJson: ''
  })
}

const resetIssueForm = () => {
  Object.assign(issueForm, {
    tplCode: '',
    issueType: 'single',
    userId: '',
    userIds: ''
  })
}

const viewTemplate = (row: any) => {
  // 实现模板详情查看
  ElMessage.info('查看模板详情功能待实现')
}

const editTemplate = (row: any) => {
  // 实现模板编辑
  ElMessage.info('编辑模板功能待实现')
}

const pauseTemplate = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要暂停此模板吗？', '确认操作')
    // 调用暂停接口
    ElMessage.success('模板已暂停')
    loadTemplates()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('暂停失败')
    }
  }
}

const resumeTemplate = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要恢复此模板吗？', '确认操作')
    // 调用恢复接口
    ElMessage.success('模板已恢复')
    loadTemplates()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('恢复失败')
    }
  }
}

const issueFromTemplate = (row: any) => {
  issueForm.tplCode = row.tplCode
  showIssueDialog.value = true
}

const getCouponTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    'CASH': '现金券',
    'DISCOUNT': '折扣券',
    'FREEBIE': '赠品券'
  }
  return typeMap[type] || type
}

const getCouponTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    'CASH': 'success',
    'DISCOUNT': 'warning',
    'FREEBIE': 'info'
  }
  return typeMap[type] || ''
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'ACTIVE': '激活',
    'PAUSED': '暂停',
    'FINISHED': '完成'
  }
  return statusMap[status] || status
}

const getStatusTagType = (status: string) => {
  const statusMap: Record<string, string> = {
    'ACTIVE': 'success',
    'PAUSED': 'warning',
    'FINISHED': 'info'
  }
  return statusMap[status] || ''
}

// 生命周期
onMounted(() => {
  loadStats()
  loadTemplates()
})
</script>

<style scoped>
.coupon-management {
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

.progress-text {
  font-size: 12px;
  color: #909399;
  text-align: center;
  margin-top: 4px;
}

.date-range {
  font-size: 12px;
  line-height: 1.4;
}

.date-range div:first-child {
  color: #67C23A;
}

.date-range div:last-child {
  color: #F56C6C;
}
</style>