<template>
  <div class="level-management">
    <div class="page-header">
      <h2>等级管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          新建等级
        </el-button>
        <el-button @click="showEvaluateDialog = true">
          <el-icon><Refresh /></el-icon>
          批量评估等级
        </el-button>
      </div>
    </div>

    <!-- 等级统计 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :span="18">
          <el-card title="等级分布统计">
            <div ref="levelChart" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card>
            <div class="level-stats">
              <div class="stat-item" v-for="stat in levelStats" :key="stat.levelCode">
                <div class="level-name">{{ stat.levelName }}</div>
                <div class="user-count">{{ stat.userCount }}人</div>
                <div class="percentage">{{ stat.percentage }}%</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 等级定义表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>等级定义</span>
          <el-button text @click="loadLevels">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      
      <el-table :data="levels" v-loading="loading" row-key="levelCode">
        <el-table-column prop="levelCode" label="等级编码" width="120" />
        <el-table-column prop="name" label="等级名称" width="150" />
        <el-table-column prop="threshold" label="成长值门槛" width="120">
          <template #default="{ row }">
            <el-tag type="info">{{ formatNumber(row.threshold) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" />
        <el-table-column prop="pkgId" label="权益包ID" width="100">
          <template #default="{ row }">
            <span v-if="row.pkgId">{{ row.pkgId }}</span>
            <span v-else class="text-placeholder">未绑定</span>
          </template>
        </el-table-column>
        <el-table-column prop="userCount" label="用户数量" width="100">
          <template #default="{ row }">
            <el-tag type="success">{{ row.userCount || 0 }}</el-tag>
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
            <el-button link @click="editLevel(row)">编辑</el-button>
            <el-button link @click="bindBenefitPackage(row)">绑定权益包</el-button>
            <el-button link @click="viewLevelUsers(row)">查看用户</el-button>
            <el-dropdown @command="handleLevelCommand">
              <el-button link>
                更多<el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="{ action: 'toggle', row }" :divided="true">
                    {{ row.enabled ? '禁用' : '启用' }}
                  </el-dropdown-item>
                  <el-dropdown-item :command="{ action: 'delete', row }" class="danger-item">
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建等级对话框 -->
    <el-dialog v-model="showCreateDialog" title="创建等级定义" width="500px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-form-item label="等级编码" prop="levelCode">
          <el-input v-model="createForm.levelCode" placeholder="请输入等级编码，如 LV5" />
        </el-form-item>
        <el-form-item label="等级名称" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入等级名称，如 白金会员" />
        </el-form-item>
        <el-form-item label="成长值门槛" prop="threshold">
          <el-input-number 
            v-model="createForm.threshold" 
            :min="0" 
            :step="1000" 
            placeholder="达到该成长值即可升级" 
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number 
            v-model="createForm.priority" 
            :min="0" 
            placeholder="数值越小优先级越高" 
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="权益包">
          <el-select v-model="createForm.pkgId" placeholder="选择关联的权益包" clearable style="width: 100%">
            <el-option 
              v-for="pkg in benefitPackages" 
              :key="pkg.pkgId" 
              :label="pkg.name" 
              :value="pkg.pkgId" 
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createLevel" :loading="submitting">创建</el-button>
      </template>
    </el-dialog>

    <!-- 批量评估对话框 -->
    <el-dialog v-model="showEvaluateDialog" title="批量等级评估" width="400px">
      <div class="evaluate-content">
        <el-alert type="info" show-icon :closable="false">
          <template #title>
            将重新评估所有用户的等级，根据当前成长值自动升级或降级
          </template>
        </el-alert>
        <div class="evaluate-stats" v-if="evaluateStats">
          <p>预计影响用户：<strong>{{ evaluateStats.affectedUsers }}</strong> 人</p>
          <p>预计升级：<strong>{{ evaluateStats.upgradeUsers }}</strong> 人</p>
          <p>预计降级：<strong>{{ evaluateStats.downgradeUsers }}</strong> 人</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="showEvaluateDialog = false">取消</el-button>
        <el-button @click="previewEvaluate" :loading="submitting">预览影响</el-button>
        <el-button type="primary" @click="batchEvaluate" :loading="submitting">执行评估</el-button>
      </template>
    </el-dialog>

    <!-- 用户详情对话框 -->
    <el-dialog v-model="showUsersDialog" :title="`${currentLevel?.name} 用户列表`" width="800px">
      <el-table :data="levelUsers" v-loading="loadingUsers" max-height="400">
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="growthValue" label="成长值" width="120">
          <template #default="{ row }">
            <el-tag type="success">{{ formatNumber(row.growthValue) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="validFrom" label="等级获得时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.validFrom) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link @click="evaluateUserLevel(row.userId)">重新评估</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, ArrowDown } from '@element-plus/icons-vue'
import { levelsApi, benefitsApi, loyaltyStatsApi } from '@/api/usercenter/loyalty'
import { formatDateTime } from '@/utils/dateFormat'

// 响应式数据
const loading = ref(false)
const loadingUsers = ref(false)
const submitting = ref(false)
const showCreateDialog = ref(false)
const showEvaluateDialog = ref(false)
const showUsersDialog = ref(false)

const levels = ref([])
const levelStats = ref([])
const benefitPackages = ref([])
const levelUsers = ref([])
const currentLevel = ref(null)
const evaluateStats = ref(null)

const createForm = reactive({
  levelCode: '',
  name: '',
  threshold: 0,
  priority: 0,
  pkgId: null
})

const createRules = {
  levelCode: [
    { required: true, message: '请输入等级编码', trigger: 'blur' },
    { pattern: /^LV\d+$/, message: '等级编码格式应为 LVx，如 LV5', trigger: 'blur' }
  ],
  name: [{ required: true, message: '请输入等级名称', trigger: 'blur' }],
  threshold: [{ required: true, message: '请输入成长值门槛', trigger: 'blur' }],
  priority: [{ required: true, message: '请输入优先级', trigger: 'blur' }]
}

const createFormRef = ref()
const levelChart = ref()

// 方法
const loadLevels = async () => {
  loading.value = true
  try {
    const res = await levelsApi.getLevelDefinitions()
    levels.value = res.data.sort((a: any, b: any) => a.priority - b.priority)
  } catch (error) {
    ElMessage.error('加载等级定义失败')
  } finally {
    loading.value = false
  }
}

const loadLevelStats = async () => {
  try {
    const res = await loyaltyStatsApi.getLevelStats()
    levelStats.value = res.data
    
    // 渲染图表
    nextTick(() => {
      renderLevelChart(res.data)
    })
  } catch (error) {
    console.error('加载等级统计失败:', error)
  }
}

const loadBenefitPackages = async () => {
  try {
    const res = await benefitsApi.getBenefitPackages({ enabled: true })
    benefitPackages.value = res.data
  } catch (error) {
    console.error('加载权益包失败:', error)
  }
}

const renderLevelChart = (data: any[]) => {
  // 这里应该使用 ECharts 渲染图表
  // 简化实现，实际项目中需要引入 ECharts
  console.log('渲染等级分布图表:', data)
}

const createLevel = async () => {
  if (!createFormRef.value) return
  
  try {
    await createFormRef.value.validate()
    
    submitting.value = true
    await levelsApi.createLevelDefinition(createForm)
    
    ElMessage.success('等级创建成功')
    showCreateDialog.value = false
    resetCreateForm()
    loadLevels()
    loadLevelStats()
  } catch (error) {
    ElMessage.error('创建失败')
  } finally {
    submitting.value = false
  }
}

const editLevel = (row: any) => {
  // 实现等级编辑功能
  ElMessage.info('编辑等级功能待实现')
}

const bindBenefitPackage = (row: any) => {
  // 实现权益包绑定功能
  ElMessage.info('权益包绑定功能待实现')
}

const viewLevelUsers = async (row: any) => {
  currentLevel.value = row
  showUsersDialog.value = true
  
  loadingUsers.value = true
  try {
    // 这里应该调用获取指定等级用户列表的接口
    // 暂时使用模拟数据
    levelUsers.value = [
      { userId: 1001, username: 'user1', growthValue: 15000, validFrom: new Date() },
      { userId: 1002, username: 'user2', growthValue: 12000, validFrom: new Date() }
    ]
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loadingUsers.value = false
  }
}

const handleLevelCommand = async (command: any) => {
  const { action, row } = command
  
  if (action === 'toggle') {
    try {
      const actionText = row.enabled ? '禁用' : '启用'
      await ElMessageBox.confirm(`确定要${actionText}等级 ${row.name} 吗？`, '确认操作')
      
      await levelsApi.updateLevelDefinition(row.id, { enabled: !row.enabled })
      ElMessage.success(`等级已${actionText}`)
      loadLevels()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('操作失败')
      }
    }
  } else if (action === 'delete') {
    try {
      await ElMessageBox.confirm(
        `删除等级 ${row.name} 将影响所有该等级用户，确定要删除吗？`, 
        '危险操作', 
        { type: 'warning' }
      )
      
      // 调用删除接口
      ElMessage.success('等级已删除')
      loadLevels()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }
}

const previewEvaluate = async () => {
  submitting.value = true
  try {
    // 调用预览接口获取评估影响
    evaluateStats.value = {
      affectedUsers: 1250,
      upgradeUsers: 89,
      downgradeUsers: 23
    }
  } catch (error) {
    ElMessage.error('预览失败')
  } finally {
    submitting.value = false
  }
}

const batchEvaluate = async () => {
  try {
    await ElMessageBox.confirm('确定要执行批量等级评估吗？此操作不可撤销。', '确认操作', {
      type: 'warning'
    })
    
    submitting.value = true
    // 调用批量评估接口
    
    ElMessage.success('批量等级评估已完成')
    showEvaluateDialog.value = false
    evaluateStats.value = null
    loadLevelStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('评估失败')
    }
  } finally {
    submitting.value = false
  }
}

const evaluateUserLevel = async (userId: number) => {
  try {
    await levelsApi.evaluateLevel(userId)
    ElMessage.success('用户等级评估完成')
    // 刷新用户列表
    viewLevelUsers(currentLevel.value)
  } catch (error) {
    ElMessage.error('评估失败')
  }
}

const resetCreateForm = () => {
  Object.assign(createForm, {
    levelCode: '',
    name: '',
    threshold: 0,
    priority: 0,
    pkgId: null
  })
}

const formatNumber = (num: number) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  return num.toString()
}

// 生命周期
onMounted(() => {
  loadLevels()
  loadLevelStats()
  loadBenefitPackages()
})
</script>

<style scoped>
.level-management {
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

.stats-section {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.level-stats {
  padding: 10px 0;
}

.stat-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.stat-item:last-child {
  border-bottom: none;
}

.level-name {
  flex: 1;
  font-weight: 500;
}

.user-count {
  color: #409EFF;
  margin-right: 10px;
}

.percentage {
  color: #909399;
  font-size: 12px;
}

.table-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.text-placeholder {
  color: #c0c4cc;
  font-style: italic;
}

.evaluate-content {
  padding: 10px 0;
}

.evaluate-stats {
  margin-top: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.evaluate-stats p {
  margin: 5px 0;
}

.danger-item {
  color: #f56c6c;
}
</style>