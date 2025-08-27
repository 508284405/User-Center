<template>
  <div class="policy-management">
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <el-icon><Setting /></el-icon>
          审核策略管理
        </h2>
        <p class="page-subtitle">配置不同场景下的内容审核策略，管理审核维度和模板</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          创建策略
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选区域 -->
    <div class="filter-section">
      <el-card>
        <el-form
          :model="filterForm"
          inline
          label-width="80px"
          @submit.prevent="handleSearch"
        >
          <el-form-item label="策略名称">
            <el-input
              v-model="filterForm.name"
              placeholder="请输入策略名称"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="策略编码">
            <el-input
              v-model="filterForm.code"
              placeholder="请输入策略编码"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="适用场景">
            <el-select
              v-model="filterForm.scenario"
              placeholder="请选择场景"
              clearable
            >
              <el-option label="用户聊天" value="USER_CHAT" />
              <el-option label="机器人回复" value="BOT_REPLY" />
              <el-option label="内容发布" value="CONTENT_PUBLISH" />
            </el-select>
          </el-form-item>
          <el-form-item label="策略类型">
            <el-select
              v-model="filterForm.policyType"
              placeholder="请选择类型"
              clearable
            >
              <el-option label="标准" value="STANDARD" />
              <el-option label="严格" value="STRICT" />
              <el-option label="宽松" value="LENIENT" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="filterForm.isActive"
              placeholder="请选择状态"
              clearable
            >
              <el-option label="启用" :value="true" />
              <el-option label="禁用" :value="false" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 策略列表 -->
    <div class="table-section">
      <el-card>
        <el-table
          v-loading="loading"
          :data="tableData"
          stripe
          style="width: 100%"
          @sort-change="handleSortChange"
        >
          <!-- 空状态显示 -->
          <template #empty>
            <div class="empty-state">
              <el-empty 
                description="暂无策略数据" 
                :image-size="100"
              >
                <el-button type="primary" @click="showCreateDialog">
                  创建第一个策略
                </el-button>
              </el-empty>
            </div>
          </template>
          <el-table-column prop="name" label="策略名称" min-width="120" />
          <el-table-column prop="code" label="策略编码" min-width="140" />
          <el-table-column prop="scenario" label="适用场景" min-width="100">
            <template #default="scope">
              <el-tag :type="getScenarioTagType(scope.row.scenario)">
                {{ getScenarioText(scope.row.scenario) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="policyType" label="策略类型" min-width="100">
            <template #default="scope">
              <el-tag :type="getPolicyTypeTagType(scope.row.policyType)">
                {{ getPolicyTypeText(scope.row.policyType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="priority" label="优先级" width="80" sortable="custom" />
          <el-table-column prop="isActive" label="状态" width="80">
            <template #default="scope">
              <el-switch
                v-model="scope.row.isActive"
                :disabled="switching === scope.row.id"
                :loading="switching === scope.row.id"
                @change="handleStatusChange(scope.row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="关联维度" width="100">
            <template #default="scope">
              <span class="dimension-count">
                {{ scope.row.dimensions?.length || 0 }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="updatedAt" label="更新时间" width="160">
            <template #default="scope">
              {{ formatDate(scope.row.updatedAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button
                type="primary"
                link
                @click="handleView(scope.row)"
              >
                查看
              </el-button>
              <el-button
                type="primary"
                link
                @click="handleEdit(scope.row)"
              >
                编辑
              </el-button>
              <el-button
                type="primary"
                link
                @click="handleConfigDimensions(scope.row)"
              >
                配置维度
              </el-button>
              <el-button
                type="danger"
                link
                @click="handleDelete(scope.row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :small="false"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      :before-close="handleDialogClose"
    >
      <policy-form
        ref="policyFormRef"
        v-model="currentPolicy"
        :is-edit="isEdit"
        @submit="handleSubmit"
      />
    </el-dialog>

    <!-- 维度配置对话框 -->
    <el-dialog
      v-model="dimensionDialogVisible"
      title="配置策略维度"
      width="1000px"
    >
      <dimension-config
        v-if="dimensionDialogVisible"
        :policy="currentPolicy"
        @save="handleDimensionSave"
        @close="dimensionDialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting, Plus, Search, Refresh } from '@element-plus/icons-vue'
import { moderationApi, type ModerationPolicy, type ModerationPolicyPageQuery } from '@/api/smartcs/moderation'
import PolicyForm from './components/PolicyForm.vue'
import DimensionConfig from './components/DimensionConfig.vue'

// 数据状态
const loading = ref(false)
const switching = ref<number | null>(null)
const tableData = ref<ModerationPolicy[]>([])

// 分页状态
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 筛选表单
const filterForm = reactive<ModerationPolicyPageQuery>({
  name: '',
  code: '',
  scenario: '',
  policyType: '',
  isActive: undefined,
  pageNum: 1,
  pageSize: 10,
  sortBy: 'priority',
  sortOrder: 'ASC'
})

// 对话框状态
const dialogVisible = ref(false)
const dimensionDialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const currentPolicy = ref<ModerationPolicy>({
  name: '',
  code: '',
  scenario: '',
  policyType: '',
  isActive: true
})

// 表单引用
const policyFormRef = ref()

// 页面加载
onMounted(() => {
  loadData()
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      ...filterForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    console.log('请求参数:', params)
    
    const response = await moderationApi.queryPolicies(params)
    console.log('API响应:', response)
    
    // 修复：使用正确的字段名访问数据
    if (response.success && response.data) {
      tableData.value = response.data
      pagination.total = response.totalCount
      console.log('数据加载成功:', {
        dataCount: tableData.value.length,
        total: pagination.total,
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize
      })
    } else {
      tableData.value = []
      pagination.total = 0
      console.warn('API返回数据格式异常:', response)
    }
  } catch (error) {
    console.error('加载策略数据失败:', error)
    ElMessage.error('加载策略数据失败')
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

// 重置
const handleReset = () => {
  Object.assign(filterForm, {
    name: '',
    code: '',
    scenario: '',
    policyType: '',
    isActive: undefined,
    pageNum: 1,
    pageSize: 10,
    sortBy: 'priority',
    sortOrder: 'ASC'
  })
  pagination.pageNum = 1
  loadData()
}

// 排序变化
const handleSortChange = ({ prop, order }: any) => {
  filterForm.sortBy = prop
  filterForm.sortOrder = order === 'ascending' ? 'ASC' : 'DESC'
  loadData()
}

// 分页变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadData()
}

const handleCurrentChange = (page: number) => {
  pagination.pageNum = page
  loadData()
}

// 状态切换
const handleStatusChange = async (policy: ModerationPolicy) => {
  if (!policy.id) return
  
  switching.value = policy.id
  try {
    if (policy.isActive) {
      await moderationApi.enablePolicy(policy.id)
      ElMessage.success('策略已启用')
    } else {
      await moderationApi.disablePolicy(policy.id)
      ElMessage.success('策略已禁用')
    }
  } catch (error) {
    console.error('状态切换失败:', error)
    ElMessage.error('状态切换失败')
    policy.isActive = !policy.isActive // 回滚状态
  } finally {
    switching.value = null
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  currentPolicy.value = {
    name: '',
    code: '',
    scenario: 'USER_CHAT',
    policyType: 'STANDARD',
    isActive: true,
    priority: 100
  }
  isEdit.value = false
  dialogTitle.value = '创建策略'
  dialogVisible.value = true
}

// 查看
const handleView = (policy: ModerationPolicy) => {
  // 跳转到策略详情页或打开详情对话框
  console.log('查看策略:', policy)
}

// 编辑
const handleEdit = (policy: ModerationPolicy) => {
  currentPolicy.value = { ...policy }
  isEdit.value = true
  dialogTitle.value = '编辑策略'
  dialogVisible.value = true
}

// 配置维度
const handleConfigDimensions = (policy: ModerationPolicy) => {
  currentPolicy.value = policy
  dimensionDialogVisible.value = true
}

// 删除
const handleDelete = async (policy: ModerationPolicy) => {
  if (!policy.id) return
  
  try {
    await ElMessageBox.confirm(
      `确定要删除策略"${policy.name}"吗？删除后不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await moderationApi.deletePolicy(policy.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 提交表单
const handleSubmit = async (policyData: ModerationPolicy) => {
  try {
    if (isEdit.value && currentPolicy.value.id) {
      await moderationApi.updatePolicy(currentPolicy.value.id, policyData)
      ElMessage.success('更新成功')
    } else {
      await moderationApi.createPolicy(policyData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败')
  }
}

// 维度配置保存
const handleDimensionSave = () => {
  dimensionDialogVisible.value = false
  ElMessage.success('维度配置保存成功')
  loadData()
}

// 关闭对话框
const handleDialogClose = (done: () => void) => {
  done()
}

// 工具函数
const getScenarioText = (scenario: string) => {
  const map: Record<string, string> = {
    'USER_CHAT': '用户聊天',
    'BOT_REPLY': '机器人回复', 
    'CONTENT_PUBLISH': '内容发布'
  }
  return map[scenario] || scenario
}

const getScenarioTagType = (scenario: string) => {
  const map: Record<string, string> = {
    'USER_CHAT': 'primary',
    'BOT_REPLY': 'success',
    'CONTENT_PUBLISH': 'warning'
  }
  return map[scenario] || ''
}

const getPolicyTypeText = (type: string) => {
  const map: Record<string, string> = {
    'STANDARD': '标准',
    'STRICT': '严格',
    'LENIENT': '宽松'
  }
  return map[type] || type
}

const getPolicyTypeTagType = (type: string) => {
  const map: Record<string, string> = {
    'STANDARD': '',
    'STRICT': 'danger',
    'LENIENT': 'info'
  }
  return map[type] || ''
}

const formatDate = (timestamp: number | undefined) => {
  if (!timestamp) return '-'
  return new Date(timestamp).toLocaleString('zh-CN')
}
</script>

<style scoped lang="scss">
.policy-management {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-bottom: 24px;

    .header-content {
      .page-title {
        font-size: 24px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 8px 0;
        display: flex;
        align-items: center;
        gap: 8px;
      }

      .page-subtitle {
        color: #606266;
        font-size: 14px;
        margin: 0;
      }
    }

    .header-actions {
      .el-button {
        gap: 6px;
      }
    }
  }

  .filter-section {
    margin-bottom: 24px;

    .el-card {
      border-radius: 8px;
      border: none;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    }
  }

  .table-section {
    .el-card {
      border-radius: 8px;
      border: none;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

      .dimension-count {
        background: #f0f2f5;
        padding: 2px 8px;
        border-radius: 4px;
        font-size: 12px;
        color: #606266;
      }

      .pagination-wrapper {
        margin-top: 24px;
        text-align: right;
      }
    }
  }
}

// Element Plus 样式自定义
:deep(.el-table__header) {
  .el-table__cell {
    background: #fafafa;
    font-weight: 600;
  }
}

:deep(.el-table__body) {
  .el-table__row:hover {
    background: #f8f9ff;
  }
}

:deep(.el-switch) {
  &.is-checked .el-switch__core {
    background-color: #67c23a;
  }
}

/* 空状态样式 */
.empty-state {
  padding: 40px 20px;
  text-align: center;
}

.empty-state .el-empty__description {
  margin: 16px 0;
  color: var(--el-text-color-regular);
}
</style>