<template>
  <div class="benefit-management">
    <div class="page-header">
      <h2>权益管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateBenefitDialog = true">
          <el-icon><Plus /></el-icon>
          新建权益
        </el-button>
        <el-button type="success" @click="showCreatePackageDialog = true">
          <el-icon><Box /></el-icon>
          新建权益包
        </el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" type="card">
      <!-- 权益定义标签页 -->
      <el-tab-pane label="权益定义" name="benefits">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>权益定义列表</span>
              <el-button text @click="loadBenefits">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </template>
          
          <el-table :data="benefits" v-loading="loadingBenefits" row-key="benefitCode">
            <el-table-column prop="benefitCode" label="权益编码" width="150" />
            <el-table-column prop="name" label="权益名称" width="200" />
            <el-table-column prop="type" label="类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getBenefitTypeTagType(row.type)">
                  {{ getBenefitTypeText(row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="ruleJson" label="规则配置" show-overflow-tooltip>
              <template #default="{ row }">
                <div class="rule-preview">
                  {{ row.ruleJson || '无特殊规则' }}
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="enabled" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.enabled ? 'success' : 'danger'">
                  {{ row.enabled ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button link @click="editBenefit(row)">编辑</el-button>
                <el-button link @click="viewBenefitUsage(row)">使用统计</el-button>
                <el-button 
                  link 
                  :type="row.enabled ? 'danger' : 'success'"
                  @click="toggleBenefitStatus(row)"
                >
                  {{ row.enabled ? '禁用' : '启用' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 权益包标签页 -->
      <el-tab-pane label="权益包" name="packages">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>权益包列表</span>
              <el-button text @click="loadPackages">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </template>
          
          <el-table :data="packages" v-loading="loadingPackages" row-key="pkgId">
            <el-table-column prop="pkgId" label="包ID" width="80" />
            <el-table-column prop="name" label="权益包名称" width="200" />
            <el-table-column label="包含权益" min-width="300">
              <template #default="{ row }">
                <div class="benefit-items">
                  <el-tag 
                    v-for="item in row.items" 
                    :key="item.benefitCode" 
                    class="benefit-tag"
                    size="small"
                  >
                    {{ item.benefitName }} ({{ item.quota }}{{ getPeriodText(item.period) }})
                  </el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="enabled" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.enabled ? 'success' : 'danger'">
                  {{ row.enabled ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button link @click="editPackage(row)">编辑</el-button>
                <el-button link @click="viewPackageUsers(row)">使用用户</el-button>
                <el-button 
                  link 
                  :type="row.enabled ? 'danger' : 'success'"
                  @click="togglePackageStatus(row)"
                >
                  {{ row.enabled ? '禁用' : '启用' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 用户权益标签页 -->
      <el-tab-pane label="用户权益" name="entitlements">
        <el-card>
          <template #header>
            <div class="search-header">
              <span>用户权益管理</span>
              <el-form :model="entitlementSearch" :inline="true">
                <el-form-item label="用户ID">
                  <el-input v-model="entitlementSearch.userId" placeholder="请输入用户ID" />
                </el-form-item>
                <el-form-item label="权益状态">
                  <el-select v-model="entitlementSearch.state" placeholder="选择状态" clearable>
                    <el-option label="生效中" value="ACTIVE" />
                    <el-option label="已用完" value="USED" />
                    <el-option label="已过期" value="EXPIRED" />
                    <el-option label="已撤销" value="REVOKED" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="loadEntitlements">
                    <el-icon><Search /></el-icon>
                    查询
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </template>
          
          <el-table :data="entitlements" v-loading="loadingEntitlements">
            <el-table-column prop="id" label="权益ID" width="100" />
            <el-table-column prop="userId" label="用户ID" width="100" />
            <el-table-column prop="benefitCode" label="权益编码" width="150" />
            <el-table-column prop="benefitName" label="权益名称" width="150" />
            <el-table-column label="配额使用" width="120">
              <template #default="{ row }">
                <el-progress 
                  :percentage="(row.quotaUsed / row.quotaTotal * 100)" 
                  :stroke-width="6"
                  :show-text="false"
                />
                <div class="quota-text">{{ row.quotaUsed }}/{{ row.quotaTotal }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="state" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getEntitlementStateTagType(row.state)">
                  {{ getEntitlementStateText(row.state) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="有效期" width="200">
              <template #default="{ row }">
                <div class="validity-period">
                  <div>{{ formatDateTime(row.validFrom) }}</div>
                  <div>{{ row.validTo ? formatDateTime(row.validTo) : '永久有效' }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link @click="viewEntitlementHistory(row)">使用记录</el-button>
                <el-button link type="danger" @click="revokeEntitlement(row)" v-if="row.state === 'ACTIVE'">
                  撤销
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="entitlementPagination.page"
              v-model:page-size="entitlementPagination.size"
              :total="entitlementPagination.total"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadEntitlements"
              @current-change="loadEntitlements"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 创建权益对话框 -->
    <el-dialog v-model="showCreateBenefitDialog" title="创建权益定义" width="500px">
      <el-form :model="createBenefitForm" :rules="createBenefitRules" ref="createBenefitFormRef" label-width="100px">
        <el-form-item label="权益编码" prop="benefitCode">
          <el-input v-model="createBenefitForm.benefitCode" placeholder="请输入权益编码" />
        </el-form-item>
        <el-form-item label="权益名称" prop="name">
          <el-input v-model="createBenefitForm.name" placeholder="请输入权益名称" />
        </el-form-item>
        <el-form-item label="权益类型" prop="type">
          <el-select v-model="createBenefitForm.type" placeholder="选择权益类型" style="width: 100%">
            <el-option label="包邮特权" value="SHIPPING" />
            <el-option label="专属折扣" value="DISCOUNT" />
            <el-option label="专属服务" value="SERVICE" />
            <el-option label="成长加速" value="ACCELERATION" />
            <el-option label="生日礼品" value="GIFT" />
          </el-select>
        </el-form-item>
        <el-form-item label="规则配置">
          <el-input 
            v-model="createBenefitForm.ruleJson" 
            type="textarea" 
            :rows="3"
            placeholder="JSON格式的规则配置，可选"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateBenefitDialog = false">取消</el-button>
        <el-button type="primary" @click="createBenefit" :loading="submitting">创建</el-button>
      </template>
    </el-dialog>

    <!-- 创建权益包对话框 -->
    <el-dialog v-model="showCreatePackageDialog" title="创建权益包" width="600px">
      <el-form :model="createPackageForm" :rules="createPackageRules" ref="createPackageFormRef" label-width="100px">
        <el-form-item label="权益包ID" prop="pkgId">
          <el-input-number v-model="createPackageForm.pkgId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="权益包名称" prop="name">
          <el-input v-model="createPackageForm.name" placeholder="请输入权益包名称" />
        </el-form-item>
        <el-form-item label="包含权益">
          <div class="benefit-items-form">
            <div v-for="(item, index) in createPackageForm.items" :key="index" class="benefit-item-form">
              <el-row :gutter="10">
                <el-col :span="8">
                  <el-select v-model="item.benefitCode" placeholder="选择权益">
                    <el-option 
                      v-for="benefit in benefits" 
                      :key="benefit.benefitCode" 
                      :label="benefit.name" 
                      :value="benefit.benefitCode" 
                    />
                  </el-select>
                </el-col>
                <el-col :span="6">
                  <el-input-number v-model="item.quota" :min="1" placeholder="配额" />
                </el-col>
                <el-col :span="6">
                  <el-select v-model="item.period" placeholder="周期">
                    <el-option label="永久" value="INFINITE" />
                    <el-option label="每日" value="DAILY" />
                    <el-option label="每月" value="MONTHLY" />
                    <el-option label="每年" value="YEARLY" />
                  </el-select>
                </el-col>
                <el-col :span="4">
                  <el-button type="danger" @click="removePackageItem(index)">删除</el-button>
                </el-col>
              </el-row>
            </div>
            <el-button @click="addPackageItem" style="width: 100%; margin-top: 10px;">
              <el-icon><Plus /></el-icon>
              添加权益
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreatePackageDialog = false">取消</el-button>
        <el-button type="primary" @click="createPackage" :loading="submitting">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Box, Refresh, Search } from '@element-plus/icons-vue'
import { benefitsApi, loyaltyStatsApi } from '@/api/usercenter/loyalty'
import { formatDateTime } from '@/utils/dateFormat'

// 响应式数据
const activeTab = ref('benefits')
const loadingBenefits = ref(false)
const loadingPackages = ref(false)
const loadingEntitlements = ref(false)
const submitting = ref(false)
const showCreateBenefitDialog = ref(false)
const showCreatePackageDialog = ref(false)

const benefits = ref([])
const packages = ref([])
const entitlements = ref([])

const entitlementSearch = reactive({
  userId: '',
  state: ''
})

const entitlementPagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const createBenefitForm = reactive({
  benefitCode: '',
  name: '',
  type: '',
  ruleJson: ''
})

const createBenefitRules = {
  benefitCode: [{ required: true, message: '请输入权益编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入权益名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择权益类型', trigger: 'change' }]
}

const createPackageForm = reactive({
  pkgId: null,
  name: '',
  items: [{ benefitCode: '', quota: 1, period: 'INFINITE', priority: 0 }]
})

const createPackageRules = {
  pkgId: [{ required: true, message: '请输入权益包ID', trigger: 'blur' }],
  name: [{ required: true, message: '请输入权益包名称', trigger: 'blur' }]
}

const createBenefitFormRef = ref()
const createPackageFormRef = ref()

// 方法
const loadBenefits = async () => {
  loadingBenefits.value = true
  try {
    const res = await benefitsApi.getBenefitDefinitions()
    benefits.value = res.data
  } catch (error) {
    ElMessage.error('加载权益定义失败')
  } finally {
    loadingBenefits.value = false
  }
}

const loadPackages = async () => {
  loadingPackages.value = true
  try {
    const res = await benefitsApi.getBenefitPackages()
    packages.value = res.data
  } catch (error) {
    ElMessage.error('加载权益包失败')
  } finally {
    loadingPackages.value = false
  }
}

const loadEntitlements = async () => {
  loadingEntitlements.value = true
  try {
    const params: any = {
      page: entitlementPagination.page,
      size: entitlementPagination.size
    }
    
    if (entitlementSearch.userId) params.userId = entitlementSearch.userId
    if (entitlementSearch.state) params.state = entitlementSearch.state
    
    const res = await benefitsApi.getUserEntitlements(params)
    entitlements.value = res.data.records
    entitlementPagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载用户权益失败')
  } finally {
    loadingEntitlements.value = false
  }
}

const createBenefit = async () => {
  if (!createBenefitFormRef.value) return
  
  try {
    await createBenefitFormRef.value.validate()
    
    submitting.value = true
    await benefitsApi.createBenefitDefinition(createBenefitForm)
    
    ElMessage.success('权益创建成功')
    showCreateBenefitDialog.value = false
    resetCreateBenefitForm()
    loadBenefits()
  } catch (error) {
    ElMessage.error('创建失败')
  } finally {
    submitting.value = false
  }
}

const createPackage = async () => {
  if (!createPackageFormRef.value) return
  
  try {
    await createPackageFormRef.value.validate()
    
    submitting.value = true
    await benefitsApi.createBenefitPackage(createPackageForm)
    
    ElMessage.success('权益包创建成功')
    showCreatePackageDialog.value = false
    resetCreatePackageForm()
    loadPackages()
  } catch (error) {
    ElMessage.error('创建失败')
  } finally {
    submitting.value = false
  }
}

const addPackageItem = () => {
  createPackageForm.items.push({
    benefitCode: '',
    quota: 1,
    period: 'INFINITE',
    priority: createPackageForm.items.length
  })
}

const removePackageItem = (index: number) => {
  createPackageForm.items.splice(index, 1)
}

const toggleBenefitStatus = async (row: any) => {
  try {
    const actionText = row.enabled ? '禁用' : '启用'
    await ElMessageBox.confirm(`确定要${actionText}权益 ${row.name} 吗？`, '确认操作')
    
    // 调用切换状态接口
    ElMessage.success(`权益已${actionText}`)
    loadBenefits()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const togglePackageStatus = async (row: any) => {
  try {
    const actionText = row.enabled ? '禁用' : '启用'
    await ElMessageBox.confirm(`确定要${actionText}权益包 ${row.name} 吗？`, '确认操作')
    
    // 调用切换状态接口
    ElMessage.success(`权益包已${actionText}`)
    loadPackages()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const revokeEntitlement = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要撤销用户 ${row.userId} 的权益吗？`, '确认操作', {
      type: 'warning'
    })
    
    // 调用撤销接口
    ElMessage.success('权益已撤销')
    loadEntitlements()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('撤销失败')
    }
  }
}

const resetCreateBenefitForm = () => {
  Object.assign(createBenefitForm, {
    benefitCode: '',
    name: '',
    type: '',
    ruleJson: ''
  })
}

const resetCreatePackageForm = () => {
  Object.assign(createPackageForm, {
    pkgId: null,
    name: '',
    items: [{ benefitCode: '', quota: 1, period: 'INFINITE', priority: 0 }]
  })
}

const getBenefitTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    'SHIPPING': '包邮',
    'DISCOUNT': '折扣',
    'SERVICE': '服务',
    'ACCELERATION': '加速',
    'GIFT': '礼品'
  }
  return typeMap[type] || type
}

const getBenefitTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    'SHIPPING': 'success',
    'DISCOUNT': 'warning',
    'SERVICE': 'info',
    'ACCELERATION': 'primary',
    'GIFT': 'danger'
  }
  return typeMap[type] || ''
}

const getPeriodText = (period: string) => {
  const periodMap: Record<string, string> = {
    'INFINITE': '',
    'DAILY': '/日',
    'WEEKLY': '/周',
    'MONTHLY': '/月',
    'YEARLY': '/年'
  }
  return periodMap[period] || ''
}

const getEntitlementStateText = (state: string) => {
  const stateMap: Record<string, string> = {
    'ACTIVE': '生效中',
    'USED': '已用完',
    'EXPIRED': '已过期',
    'REVOKED': '已撤销'
  }
  return stateMap[state] || state
}

const getEntitlementStateTagType = (state: string) => {
  const stateMap: Record<string, string> = {
    'ACTIVE': 'success',
    'USED': 'info',
    'EXPIRED': 'warning',
    'REVOKED': 'danger'
  }
  return stateMap[state] || ''
}

// 生命周期
onMounted(() => {
  loadBenefits()
  loadPackages()
  loadEntitlements()
})
</script>

<style scoped>
.benefit-management {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.rule-preview {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.benefit-items {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.benefit-tag {
  margin-bottom: 5px;
}

.quota-text {
  font-size: 12px;
  color: #909399;
  text-align: center;
  margin-top: 2px;
}

.validity-period {
  font-size: 12px;
  line-height: 1.4;
}

.validity-period div:first-child {
  color: #67C23A;
}

.validity-period div:last-child {
  color: #F56C6C;
}

.pagination-wrapper {
  margin-top: 20px;
  text-align: center;
}

.benefit-items-form {
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  padding: 15px;
}

.benefit-item-form {
  margin-bottom: 10px;
}

.benefit-item-form:last-child {
  margin-bottom: 0;
}
</style>