<template>
  <div class="keyword-rules">
    <div class="header-section">
      <h2>关键词规则管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          新增规则
        </el-button>
        <el-button type="success" @click="showImportDialog = true">
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>
        <el-button @click="refreshRules">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form
        :model="queryForm"
        inline
        label-width="80px"
        class="filter-form"
      >
        <el-form-item label="违规分类">
          <el-select v-model="queryForm.categoryId" placeholder="全部分类" clearable style="width: 200px">
            <el-option
              v-for="category in allCategories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="规则类型">
          <el-select v-model="queryForm.ruleType" placeholder="全部类型" clearable style="width: 120px">
            <el-option label="精确匹配" value="EXACT" />
            <el-option label="模糊匹配" value="FUZZY" />
            <el-option label="正则表达式" value="REGEX" />
            <el-option label="子字符串" value="SUBSTRING" />
          </el-select>
        </el-form-item>

        <el-form-item label="语言">
          <el-select v-model="queryForm.language" placeholder="全部语言" clearable style="width: 100px">
            <el-option label="中文" value="zh-CN" />
            <el-option label="英文" value="en-US" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="queryForm.isActive" placeholder="全部状态" clearable style="width: 100px">
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-form-item>

        <el-form-item label="关键词">
          <el-input 
            v-model="searchKeyword" 
            placeholder="搜索关键词" 
            style="width: 200px" 
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 批量操作 -->
    <el-card v-show="selectedRules.length > 0" class="batch-actions-card">
      <div class="batch-actions">
        <span>已选择 {{ selectedRules.length }} 条规则</span>
        <div class="actions">
          <el-button type="success" size="small" @click="batchEnable" :disabled="selectedRules.length === 0">
            批量启用
          </el-button>
          <el-button type="warning" size="small" @click="batchDisable" :disabled="selectedRules.length === 0">
            批量禁用
          </el-button>
          <el-button type="danger" size="small" @click="batchDelete" :disabled="selectedRules.length === 0">
            批量删除
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 规则表格 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="rules"
        @selection-change="handleSelectionChange"
        row-key="id"
        class="rules-table"
      >
        <el-table-column type="selection" width="50" />
        
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column prop="ruleName" label="规则名称" width="150" show-overflow-tooltip />

        <el-table-column prop="keyword" label="关键词" width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="keyword-display">
              <el-tag :type="getRuleTypeTag(row.ruleType)" size="small">
                {{ getRuleTypeLabel(row.ruleType) }}
              </el-tag>
              <span class="keyword-text">{{ row.keyword }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="categoryId" label="违规分类" width="150">
          <template #default="{ row }">
            <span>{{ getCategoryName(row.categoryId) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="priority" label="优先级" width="80" sortable />

        <el-table-column prop="severityWeight" label="严重度权重" width="100" sortable />

        <el-table-column prop="hitCount" label="命中次数" width="100" sortable>
          <template #default="{ row }">
            <el-tag :type="getHitCountTag(row.hitCount)" size="small">
              {{ row.hitCount }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="language" label="语言" width="80">
          <template #default="{ row }">
            <el-tag size="small">{{ getLanguageLabel(row.language) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="isActive" label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.isActive"
              @change="toggleRuleStatus(row)"
              :loading="row.statusLoading"
            />
          </template>
        </el-table-column>

        <el-table-column prop="lastHitAt" label="最后命中" width="160">
          <template #default="{ row }">
            <span v-if="row.lastHitAt">{{ formatDateTime(row.lastHitAt) }}</span>
            <span v-else class="no-hit">从未命中</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="text" size="small" @click="testRule(row)">
                <el-icon><Tools /></el-icon>
                测试
              </el-button>
              <el-button type="text" size="small" @click="editRule(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button type="text" size="small" @click="duplicateRule(row)">
                <el-icon><DocumentCopy /></el-icon>
                复制
              </el-button>
              <el-popconfirm
                title="确认删除此规则吗？"
                @confirm="deleteRule(row)"
              >
                <template #reference>
                  <el-button type="text" size="small" danger>
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 统计信息 -->
    <el-row :gutter="20" class="stats-section">
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-value">{{ stats.totalRules }}</div>
            <div class="stat-label">总规则数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-value">{{ stats.activeRules }}</div>
            <div class="stat-label">启用规则</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-value">{{ stats.totalHits }}</div>
            <div class="stat-label">总命中次数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-value">{{ stats.averageHits }}</div>
            <div class="stat-label">平均命中</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 创建/编辑对话框 -->
    <RuleEditDialog
      v-model="showCreateDialog"
      :rule="editingRule"
      :categories="allCategories"
      @success="handleEditSuccess"
    />

    <!-- 批量导入对话框 -->
    <ImportDialog
      v-model="showImportDialog"
      :categories="allCategories"
      @success="handleImportSuccess"
    />

    <!-- 规则测试对话框 -->
    <RuleTestDialog
      v-model="showTestDialog"
      :rule="testingRule"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Upload, Refresh, Search, RefreshRight, Tools, Edit, 
  DocumentCopy, Delete 
} from '@element-plus/icons-vue'
import moderationApi, { 
  type KeywordRule, 
  type ModerationCategory, 
  type RuleQuery 
} from '@/api/smartcs/moderation'
import RuleEditDialog from './components/RuleEditDialog.vue'
import ImportDialog from './components/ImportDialog.vue'
import RuleTestDialog from './components/RuleTestDialog.vue'
import { formatDateTime } from '@/utils/dateFormat'

// 响应式数据
const loading = ref(false)
const showCreateDialog = ref(false)
const showImportDialog = ref(false)
const showTestDialog = ref(false)
const rules = ref<KeywordRule[]>([])
const allCategories = ref<ModerationCategory[]>([])
const editingRule = ref<KeywordRule | null>(null)
const testingRule = ref<KeywordRule | null>(null)
const selectedRules = ref<KeywordRule[]>([])
const searchKeyword = ref('')

// 查询表单
const queryForm = reactive<RuleQuery>({
  categoryId: undefined,
  ruleType: undefined,
  language: undefined,
  isActive: undefined,
  pageNumber: 1,
  pageSize: 20
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 统计数据
const stats = reactive({
  totalRules: 0,
  activeRules: 0,
  totalHits: 0,
  averageHits: 0
})

// 生命周期
onMounted(() => {
  loadCategories()
  loadRules()
})

// 方法定义
const loadCategories = async () => {
  try {
    const response = await moderationApi.getCategoryTree()
    allCategories.value = flattenCategories(response.data || [])
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
}

const flattenCategories = (categories: ModerationCategory[]): ModerationCategory[] => {
  const result: ModerationCategory[] = []
  const flatten = (cats: ModerationCategory[]) => {
    cats.forEach(cat => {
      result.push(cat)
      if (cat.children) {
        flatten(cat.children)
      }
    })
  }
  flatten(categories)
  return result
}

const loadRules = async () => {
  loading.value = true
  try {
    const query = {
      ...queryForm,
      pageNumber: pagination.page,
      pageSize: pagination.size
    }
    
    const response = await moderationApi.getRules(query)
    rules.value = response.data.data || []
    pagination.total = response.data.total || 0
    
    calculateStats()
  } catch (error) {
    console.error('Failed to load rules:', error)
    ElMessage.error('加载规则失败')
  } finally {
    loading.value = false
  }
}

const calculateStats = () => {
  stats.totalRules = rules.value.length
  stats.activeRules = rules.value.filter(rule => rule.isActive).length
  stats.totalHits = rules.value.reduce((sum, rule) => sum + rule.hitCount, 0)
  stats.averageHits = stats.totalRules > 0 ? Math.round(stats.totalHits / stats.totalRules) : 0
}

const refreshRules = () => {
  pagination.page = 1
  loadRules()
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    // 在实际实现中，这里应该添加关键词搜索参数
    console.log('Search keyword:', searchKeyword.value)
  }
  pagination.page = 1
  loadRules()
}

const handleReset = () => {
  Object.assign(queryForm, {
    categoryId: undefined,
    ruleType: undefined,
    language: undefined,
    isActive: undefined
  })
  searchKeyword.value = ''
  pagination.page = 1
  loadRules()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadRules()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadRules()
}

const handleSelectionChange = (selection: KeywordRule[]) => {
  selectedRules.value = selection
}

const toggleRuleStatus = async (row: KeywordRule & { statusLoading?: boolean }) => {
  row.statusLoading = true
  try {
    await moderationApi.updateRule(row.id!, { isActive: row.isActive })
    ElMessage.success(`${row.isActive ? '启用' : '禁用'}成功`)
    calculateStats()
  } catch (error) {
    console.error('Toggle status failed:', error)
    ElMessage.error('状态更新失败')
    row.isActive = !row.isActive // 回滚状态
  } finally {
    row.statusLoading = false
  }
}

const editRule = (rule: KeywordRule) => {
  editingRule.value = { ...rule }
  showCreateDialog.value = true
}

const duplicateRule = (rule: KeywordRule) => {
  editingRule.value = {
    ...rule,
    id: undefined,
    ruleName: `${rule.ruleName} (副本)`,
    hitCount: 0,
    lastHitAt: undefined
  }
  showCreateDialog.value = true
}

const deleteRule = async (rule: KeywordRule) => {
  try {
    await moderationApi.deleteRule(rule.id!)
    ElMessage.success('删除成功')
    loadRules()
  } catch (error) {
    console.error('Delete failed:', error)
    ElMessage.error('删除失败')
  }
}

const testRule = (rule: KeywordRule) => {
  testingRule.value = rule
  showTestDialog.value = true
}

const batchEnable = async () => {
  await batchUpdateStatus(true)
}

const batchDisable = async () => {
  await batchUpdateStatus(false)
}

const batchUpdateStatus = async (isActive: boolean) => {
  try {
    const updates = selectedRules.value.map(rule => 
      moderationApi.updateRule(rule.id!, { isActive })
    )
    await Promise.all(updates)
    ElMessage.success(`批量${isActive ? '启用' : '禁用'}成功`)
    loadRules()
    selectedRules.value = []
  } catch (error) {
    console.error('Batch update failed:', error)
    ElMessage.error('批量操作失败')
  }
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${selectedRules.value.length} 条规则吗？删除后无法恢复！`,
      '批量删除确认',
      { type: 'warning' }
    )

    const deletions = selectedRules.value.map(rule => 
      moderationApi.deleteRule(rule.id!)
    )
    await Promise.all(deletions)
    
    ElMessage.success('批量删除成功')
    loadRules()
    selectedRules.value = []
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('Batch delete failed:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

const handleEditSuccess = () => {
  showCreateDialog.value = false
  editingRule.value = null
  loadRules()
}

const handleImportSuccess = () => {
  showImportDialog.value = false
  loadRules()
}

// 辅助函数
const getCategoryName = (categoryId: number) => {
  const category = allCategories.value.find(cat => cat.id === categoryId)
  return category?.name || '未知分类'
}

const getRuleTypeTag = (type: string) => {
  const tags: Record<string, string> = {
    EXACT: 'success',
    FUZZY: 'warning',
    REGEX: 'danger',
    SUBSTRING: 'info'
  }
  return tags[type] || ''
}

const getRuleTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    EXACT: '精确',
    FUZZY: '模糊',
    REGEX: '正则',
    SUBSTRING: '子串'
  }
  return labels[type] || type
}

const getHitCountTag = (count: number) => {
  if (count >= 100) return 'danger'
  if (count >= 50) return 'warning'
  if (count >= 10) return 'info'
  return 'success'
}

const getLanguageLabel = (lang: string) => {
  const labels: Record<string, string> = {
    'zh-CN': '中文',
    'en-US': '英文',
    'other': '其他'
  }
  return labels[lang] || lang
}
</script>

<style scoped lang="scss">
.keyword-rules {
  padding: 20px;

  .header-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
      color: #303133;
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .filter-card,
  .batch-actions-card,
  .table-card {
    margin-bottom: 20px;
  }

  .filter-form {
    .el-form-item {
      margin-bottom: 12px;
    }
  }

  .batch-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;

    .actions {
      display: flex;
      gap: 12px;
    }
  }

  .rules-table {
    .keyword-display {
      display: flex;
      align-items: center;
      gap: 8px;

      .keyword-text {
        font-family: 'Courier New', monospace;
        font-size: 12px;
      }
    }

    .no-hit {
      color: #c0c4cc;
      font-style: italic;
    }

    .action-buttons {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }
  }

  .pagination-container {
    display: flex;
    justify-content: center;
    padding: 20px 0;
  }

  .stats-section {
    .stat-item {
      text-align: center;
      padding: 20px;

      .stat-value {
        font-size: 32px;
        font-weight: bold;
        color: #409eff;
        margin-bottom: 8px;
      }

      .stat-label {
        font-size: 14px;
        color: #909399;
      }
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .filter-form {
    :deep(.el-form-item) {
      margin-right: 8px;
    }
  }

  .rules-table {
    font-size: 14px;
  }
}

@media (max-width: 768px) {
  .keyword-rules {
    padding: 12px;
  }

  .filter-form {
    :deep(.el-form-item) {
      width: 100%;
      margin-right: 0;
    }
  }

  .batch-actions {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }
}
</style>