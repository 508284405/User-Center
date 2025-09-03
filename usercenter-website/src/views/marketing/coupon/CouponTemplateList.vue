<template>
  <div class="coupon-template-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>优惠券模板管理</h3>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            创建模板
          </el-button>
        </div>
      </template>

      <!-- 查询条件 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="模板名称">
          <el-input v-model="searchForm.name" placeholder="请输入模板名称" clearable />
        </el-form-item>
        <el-form-item label="优惠券类型">
          <el-select v-model="searchForm.type" placeholder="选择类型" clearable>
            <el-option label="满减券" value="FULL_REDUCTION" />
            <el-option label="折扣券" value="DISCOUNT" />
            <el-option label="代金券" value="CASH_VOUCHER" />
            <el-option label="免邮券" value="FREE_SHIPPING" />
          </el-select>
        </el-form-item>
        <el-form-item label="模板状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
            <el-option label="草稿" value="DRAFT" />
            <el-option label="可用" value="AVAILABLE" />
            <el-option label="已停用" value="DISABLED" />
            <el-option label="已过期" value="EXPIRED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="模板名称" width="200" show-overflow-tooltip />
        <el-table-column prop="tplCode" label="模板编码" width="120" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getCouponTypeColor(row.type)">
              {{ getCouponTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优惠信息" width="180">
          <template #default="{ row }">
            <div class="coupon-value">
              <div v-if="row.type === 'FULL_REDUCTION'" class="full-reduction">
                <span class="threshold">满¥{{ (row.thresholdAmount / 100).toFixed(2) }}</span>
                <span class="value">减¥{{ (row.faceValue / 100).toFixed(2) }}</span>
              </div>
              <div v-else-if="row.type === 'DISCOUNT'" class="discount">
                <span class="rate">{{ row.discountRate }}折</span>
                <span v-if="row.thresholdAmount" class="threshold">
                  满¥{{ (row.thresholdAmount / 100).toFixed(2) }}
                </span>
              </div>
              <div v-else class="cash-voucher">
                <span class="value">¥{{ (row.faceValue / 100).toFixed(2) }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="有效期" width="200">
          <template #default="{ row }">
            <div class="validity-period">
              <div>{{ formatDateTime(row.validFrom) }}</div>
              <div style="text-align: center; color: #909399;">至</div>
              <div>{{ formatDateTime(row.validTo) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="发放信息" width="120">
          <template #default="{ row }">
            <div class="issue-info">
              <el-progress 
                :percentage="getIssueProgress(row)"
                :color="getProgressColor(row)"
                :stroke-width="6"
              />
              <div class="issue-text">{{ row.issued || 0 }}/{{ row.total || '不限' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="发放期间" width="200">
          <template #default="{ row }">
            <div class="issue-period">
              <div>{{ formatDateTime(row.issueStart) }}</div>
              <div style="text-align: center; color: #909399;">至</div>
              <div>{{ formatDateTime(row.issueEnd) }}</div>
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
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button type="success" size="small" @click="handleIssue(row)" 
              v-if="row.status === 'AVAILABLE'">发放</el-button>
            <el-button type="warning" size="small" @click="handleEdit(row)" 
              v-if="row.status === 'DRAFT'">编辑</el-button>
            <el-dropdown @command="handleCommand($event, row)" style="margin-left: 12px">
              <el-button type="info" size="small">
                更多 <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="activate" v-if="row.status === 'DRAFT'">激活</el-dropdown-item>
                  <el-dropdown-item command="deactivate" v-if="row.status === 'AVAILABLE'">停用</el-dropdown-item>
                  <el-dropdown-item command="coupons" divided>查看券码</el-dropdown-item>
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

    <!-- 模板详情对话框 -->
    <CouponTemplateDialog
      v-model="dialogVisible"
      :form-data="editForm"
      :is-edit="isEdit"
      :is-view="isView"
      @success="handleDialogSuccess"
    />

    <!-- 发放优惠券对话框 -->
    <CouponIssueDialog
      v-model="issueDialogVisible"
      :template="selectedTemplate"
      @success="handleIssueSuccess"
    />

    <!-- 券码管理对话框 -->
    <CouponManageDialog
      v-model="couponDialogVisible"
      :template="selectedTemplate"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowDown } from '@element-plus/icons-vue'
import type { CouponTemplateDTO } from '@/types/coupon'
import { couponApi } from '@/api/coupon'
import CouponTemplateDialog from './components/CouponTemplateDialog.vue'
import CouponIssueDialog from './components/CouponIssueDialog.vue'
import CouponManageDialog from './components/CouponManageDialog.vue'

// 响应式数据
const tableData = ref<CouponTemplateDTO[]>([])
const dialogVisible = ref(false)
const issueDialogVisible = ref(false)
const couponDialogVisible = ref(false)
const isEdit = ref(false)
const isView = ref(false)
const selectedTemplate = ref<CouponTemplateDTO | null>(null)

const searchForm = reactive({
  name: '',
  type: '',
  status: ''
})

const editForm = ref<Partial<CouponTemplateDTO>>({})

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
      type: searchForm.type || undefined,
      status: searchForm.status || undefined
    }
    
    const response = await couponApi.getTemplatePage(params)
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
    type: '',
    status: ''
  })
  pagination.page = 1
  fetchData()
}

// 新增
const handleCreate = () => {
  isEdit.value = false
  isView.value = false
  editForm.value = {
    type: 'FULL_REDUCTION',
    total: 1000,
    status: 'DRAFT'
  }
  dialogVisible.value = true
}

// 查看
const handleView = (row: CouponTemplateDTO) => {
  isEdit.value = false
  isView.value = true
  editForm.value = { ...row }
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: CouponTemplateDTO) => {
  isEdit.value = true
  isView.value = false
  editForm.value = { ...row }
  dialogVisible.value = true
}

// 发放优惠券
const handleIssue = (row: CouponTemplateDTO) => {
  selectedTemplate.value = row
  issueDialogVisible.value = true
}

// 下拉菜单命令处理
const handleCommand = async (command: string, row: CouponTemplateDTO) => {
  selectedTemplate.value = row
  
  try {
    switch (command) {
      case 'activate':
        await ElMessageBox.confirm('确定要激活这个优惠券模板吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const activateResponse = await couponApi.activateTemplate(row.id!)
        if (activateResponse.success) {
          ElMessage.success('激活成功')
          fetchData()
        }
        break
        
      case 'deactivate':
        await ElMessageBox.confirm('确定要停用这个优惠券模板吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const deactivateResponse = await couponApi.deactivateTemplate(row.id!)
        if (deactivateResponse.success) {
          ElMessage.success('停用成功')
          fetchData()
        }
        break
        
      case 'coupons':
        couponDialogVisible.value = true
        break
        
      case 'delete':
        await ElMessageBox.confirm('确定要删除这个优惠券模板吗？删除后不可恢复！', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const deleteResponse = await couponApi.deleteTemplate(row.id!)
        if (deleteResponse.success) {
          ElMessage.success('删除成功')
          fetchData()
        }
        break
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

const handleIssueSuccess = () => {
  issueDialogVisible.value = false
  fetchData()
}

// 格式化方法
const getCouponTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    FULL_REDUCTION: 'success',
    DISCOUNT: 'warning',
    CASH_VOUCHER: 'danger',
    FREE_SHIPPING: 'info'
  }
  return colorMap[type] || ''
}

const getCouponTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    FULL_REDUCTION: '满减券',
    DISCOUNT: '折扣券',
    CASH_VOUCHER: '代金券',
    FREE_SHIPPING: '免邮券'
  }
  return textMap[type] || type
}

const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    DRAFT: 'info',
    AVAILABLE: 'success',
    DISABLED: 'warning',
    EXPIRED: 'danger'
  }
  return typeMap[status] || ''
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    DRAFT: '草稿',
    AVAILABLE: '可用',
    DISABLED: '已停用',
    EXPIRED: '已过期'
  }
  return textMap[status] || status
}

const getIssueProgress = (row: CouponTemplateDTO) => {
  if (!row.total) return 0
  return Math.round(((row.issued || 0) / row.total) * 100)
}

const getProgressColor = (row: CouponTemplateDTO) => {
  const progress = getIssueProgress(row)
  if (progress >= 90) return '#f56c6c'
  if (progress >= 70) return '#e6a23c'
  if (progress >= 50) return '#409eff'
  return '#67c23a'
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
.coupon-template-container {
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

.coupon-value {
  font-size: 12px;
}

.full-reduction .threshold {
  color: #909399;
  margin-right: 8px;
}

.full-reduction .value {
  color: #f56c6c;
  font-weight: bold;
}

.discount .rate {
  color: #e6a23c;
  font-weight: bold;
  margin-right: 8px;
}

.discount .threshold {
  color: #909399;
}

.cash-voucher .value {
  color: #67c23a;
  font-weight: bold;
}

.validity-period,
.issue-period {
  font-size: 12px;
  line-height: 1.2;
}

.issue-info {
  text-align: center;
}

.issue-text {
  font-size: 12px;
  margin-top: 4px;
  color: #666;
}
</style>