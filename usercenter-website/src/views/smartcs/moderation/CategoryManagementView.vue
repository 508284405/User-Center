<template>
  <div class="category-management">
    <div class="header-section">
      <h2>违规分类管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          新增分类
        </el-button>
        <el-button @click="refreshCategories">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 分类树表格 -->
    <el-card class="category-tree-card">
      <template #header>
        <span>分类结构</span>
        <div class="tree-actions">
          <el-switch
            v-model="expandAll"
            @change="handleExpandChange"
            active-text="全部展开"
            inactive-text="全部折叠"
          />
        </div>
      </template>

      <el-table
        ref="categoryTable"
        :data="categoryTree"
        v-loading="loading"
        row-key="id"
        :tree-props="{ children: 'children' }"
        :default-expand-all="expandAll"
        class="category-table"
      >
        <el-table-column prop="name" label="分类名称" width="200">
          <template #default="{ row }">
            <div class="category-name">
              <el-tag
                v-if="!row.parentId"
                type="warning"
                size="small"
                class="level-tag"
              >
                一级
              </el-tag>
              <el-tag
                v-else
                type="info"
                size="small"
                class="level-tag"
              >
                二级
              </el-tag>
              {{ row.name }}
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="code" label="分类编码" width="150" />

        <el-table-column prop="severityLevel" label="严重程度" width="120">
          <template #default="{ row }">
            <el-tag
              :type="getSeverityTagType(row.severityLevel)"
              size="small"
            >
              {{ getSeverityLabel(row.severityLevel) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="actionType" label="默认动作" width="100">
          <template #default="{ row }">
            <el-tag
              :type="getActionTagType(row.actionType)"
              size="small"
            >
              {{ getActionLabel(row.actionType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="sortOrder" label="排序" width="80" />

        <el-table-column prop="isActive" label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.isActive"
              @change="toggleCategoryStatus(row)"
              :loading="row.statusLoading"
            />
          </template>
        </el-table-column>

        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button
                v-if="!row.parentId"
                type="text"
                size="small"
                @click="addSubCategory(row)"
              >
                <el-icon><Plus /></el-icon>
                添加子类
              </el-button>
              <el-button
                type="text"
                size="small"
                @click="editCategory(row)"
              >
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-popconfirm
                title="确认删除此分类吗？删除后无法恢复！"
                @confirm="deleteCategory(row)"
                :disabled="hasChildren(row)"
              >
                <template #reference>
                  <el-button
                    type="text"
                    size="small"
                    :disabled="hasChildren(row)"
                    danger
                  >
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 统计信息 -->
    <el-row :gutter="20" class="stats-section">
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-value">{{ stats.totalCategories }}</div>
            <div class="stat-label">总分类数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-value">{{ stats.activeCategories }}</div>
            <div class="stat-label">启用分类</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-value">{{ stats.topLevelCategories }}</div>
            <div class="stat-label">一级分类</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-value">{{ stats.subCategories }}</div>
            <div class="stat-label">二级分类</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 创建/编辑对话框 -->
    <CategoryEditDialog
      v-model="showCreateDialog"
      :category="editingCategory"
      :parent-categories="parentCategories"
      @success="handleCreateSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import moderationApi, { type ModerationCategory } from '@/api/smartcs/moderation'
import CategoryEditDialog from './components/CategoryEditDialog.vue'

// 响应式数据
const loading = ref(false)
const expandAll = ref(true)
const showCreateDialog = ref(false)
const categoryTree = ref<ModerationCategory[]>([])
const editingCategory = ref<ModerationCategory | null>(null)

// 统计数据
const stats = reactive({
  totalCategories: 0,
  activeCategories: 0,
  topLevelCategories: 0,
  subCategories: 0
})

// 计算属性
const parentCategories = computed(() => {
  return categoryTree.value.filter(cat => !cat.parentId)
})

// 组件引用
const categoryTable = ref()

// 生命周期
onMounted(() => {
  refreshCategories()
})

// 方法定义
const refreshCategories = async () => {
  loading.value = true
  try {
    const response = await moderationApi.getCategoryTree()
    categoryTree.value = response.data || []
    calculateStats()
  } catch (error) {
    console.error('Failed to load categories:', error)
    ElMessage.error('加载分类数据失败')
  } finally {
    loading.value = false
  }
}

const calculateStats = () => {
  let total = 0
  let active = 0
  let topLevel = 0
  let subLevel = 0

  const countCategories = (categories: ModerationCategory[]) => {
    categories.forEach(cat => {
      total++
      if (cat.isActive) active++
      if (!cat.parentId) {
        topLevel++
      } else {
        subLevel++
      }
      if (cat.children) {
        countCategories(cat.children)
      }
    })
  }

  countCategories(categoryTree.value)

  stats.totalCategories = total
  stats.activeCategories = active
  stats.topLevelCategories = topLevel
  stats.subCategories = subLevel
}

const handleExpandChange = (expand: boolean) => {
  if (expand) {
    categoryTable.value?.expandAll()
  } else {
    categoryTable.value?.collapseAll()
  }
}

const toggleCategoryStatus = async (row: ModerationCategory & { statusLoading?: boolean }) => {
  row.statusLoading = true
  try {
    await moderationApi.toggleCategoryStatus(row.id!, row.isActive)
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

const addSubCategory = (parent: ModerationCategory) => {
  editingCategory.value = {
    parentId: parent.id,
    name: '',
    code: '',
    description: '',
    severityLevel: 'MEDIUM',
    actionType: 'BLOCK',
    isActive: true,
    sortOrder: 0
  } as ModerationCategory
  showCreateDialog.value = true
}

const editCategory = (category: ModerationCategory) => {
  editingCategory.value = { ...category }
  showCreateDialog.value = true
}

const deleteCategory = async (category: ModerationCategory) => {
  try {
    await moderationApi.deleteCategory(category.id!)
    ElMessage.success('删除成功')
    refreshCategories()
  } catch (error) {
    console.error('Delete failed:', error)
    ElMessage.error('删除失败')
  }
}

const hasChildren = (category: ModerationCategory) => {
  return category.children && category.children.length > 0
}

const handleCreateSuccess = () => {
  showCreateDialog.value = false
  editingCategory.value = null
  refreshCategories()
}

// 辅助函数
const getSeverityTagType = (level: string) => {
  const types: Record<string, string> = {
    LOW: '',
    MEDIUM: 'warning',
    HIGH: 'danger',
    CRITICAL: 'danger'
  }
  return types[level] || ''
}

const getSeverityLabel = (level: string) => {
  const labels: Record<string, string> = {
    LOW: '低风险',
    MEDIUM: '中风险',
    HIGH: '高风险',
    CRITICAL: '极高风险'
  }
  return labels[level] || level
}

const getActionTagType = (action: string) => {
  const types: Record<string, string> = {
    WARN: 'info',
    REVIEW: 'warning',
    BLOCK: 'danger',
    ESCALATE: 'danger'
  }
  return types[action] || ''
}

const getActionLabel = (action: string) => {
  const labels: Record<string, string> = {
    WARN: '警告',
    REVIEW: '审核',
    BLOCK: '阻断',
    ESCALATE: '升级'
  }
  return labels[action] || action
}
</script>

<style scoped lang="scss">
.category-management {
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

  .category-tree-card {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .tree-actions {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .category-table {
      .category-name {
        display: flex;
        align-items: center;
        gap: 8px;

        .level-tag {
          font-size: 10px;
        }
      }

      .action-buttons {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
      }
    }
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

:deep(.el-table) {
  .el-table__row {
    &:hover {
      background-color: #f5f7fa;
    }
  }
}

:deep(.el-card__header) {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>