<template>
  <div class="dimension-management">
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <el-icon><Grid /></el-icon>
          审核维度管理
        </h2>
        <p class="page-subtitle">管理内容审核的各个维度，配置检查标准和处理动作</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增维度
        </el-button>
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">总维度数</div>
            </div>
            <div class="stat-icon total">
              <el-icon><Grid /></el-icon>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value">{{ stats.active }}</div>
              <div class="stat-label">启用维度</div>
            </div>
            <div class="stat-icon active">
              <el-icon><CircleCheck /></el-icon>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value">{{ stats.highRisk }}</div>
              <div class="stat-label">高风险维度</div>
            </div>
            <div class="stat-icon high-risk">
              <el-icon><Warning /></el-icon>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value">{{ stats.critical }}</div>
              <div class="stat-label">极高风险维度</div>
            </div>
            <div class="stat-icon critical">
              <el-icon><CircleClose /></el-icon>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-section">
      <el-card>
        <el-form inline label-width="80px">
          <el-form-item label="维度名称">
            <el-input
              v-model="filterForm.name"
              placeholder="请输入维度名称"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="风险级别">
            <el-select
              v-model="filterForm.severityLevel"
              placeholder="请选择风险级别"
              clearable
              style="width: 150px"
            >
              <el-option label="低风险" value="LOW" />
              <el-option label="中风险" value="MEDIUM" />
              <el-option label="高风险" value="HIGH" />
              <el-option label="极高风险" value="CRITICAL" />
            </el-select>
          </el-form-item>
          <el-form-item label="处理动作">
            <el-select
              v-model="filterForm.actionType"
              placeholder="请选择处理动作"
              clearable
              style="width: 150px"
            >
              <el-option label="通过" value="APPROVE" />
              <el-option label="拒绝" value="REJECT" />
              <el-option label="人工审核" value="MANUAL_REVIEW" />
              <el-option label="自动修复" value="AUTO_FIX" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="filterForm.isActive"
              placeholder="请选择状态"
              clearable
              style="width: 120px"
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

    <!-- 维度列表 -->
    <div class="table-section">
      <el-card>
        <div class="table-header">
          <span class="table-title">维度列表</span>
          <div class="table-actions">
            <el-button-group>
              <el-button
                :type="viewMode === 'table' ? 'primary' : ''"
                @click="viewMode = 'table'"
              >
                <el-icon><List /></el-icon>
                表格视图
              </el-button>
              <el-button
                :type="viewMode === 'card' ? 'primary' : ''"
                @click="viewMode = 'card'"
              >
                <el-icon><Grid /></el-icon>
                卡片视图
              </el-button>
            </el-button-group>
          </div>
        </div>

        <!-- 表格视图 -->
        <div v-if="viewMode === 'table'">
          <el-table
            v-loading="loading"
            :data="filteredDimensions"
            stripe
            style="width: 100%"
          >
            <el-table-column prop="name" label="维度名称" min-width="120">
              <template #default="scope">
                <div class="dimension-name">
                  <span>{{ scope.row.name }}</span>
                  <el-tag
                    v-if="!scope.row.isActive"
                    size="small"
                    type="info"
                  >
                    已禁用
                  </el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="code" label="维度编码" min-width="140" />
            <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
            <el-table-column prop="severityLevel" label="风险级别" width="100">
              <template #default="scope">
                <el-tag :type="getSeverityTagType(scope.row.severityLevel)">
                  {{ getSeverityText(scope.row.severityLevel) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="actionType" label="处理动作" width="100">
              <template #default="scope">
                <el-tag :type="getActionTagType(scope.row.actionType)">
                  {{ getActionText(scope.row.actionType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="confidenceThreshold" label="置信阈值" width="100">
              <template #default="scope">
                <span class="threshold-value">{{ scope.row.confidenceThreshold }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="sortOrder" label="排序" width="80" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button
                  type="primary"
                  link
                  @click="handleViewDetail(scope.row)"
                >
                  查看详情
                </el-button>
                <el-button
                  type="primary"
                  link
                  @click="handleEdit(scope.row)"
                >
                  编辑
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
        </div>

        <!-- 卡片视图 -->
        <div v-else class="dimensions-grid">
          <div
            v-for="dimension in filteredDimensions"
            :key="dimension.id"
            class="dimension-card"
            :class="{ 'disabled': !dimension.isActive }"
            @click="handleViewDetail(dimension)"
          >
            <div class="card-header">
              <div class="card-title">
                <span class="dimension-name">{{ dimension.name }}</span>
                <div class="card-tags">
                  <el-tag
                    size="small"
                    :type="getSeverityTagType(dimension.severityLevel)"
                  >
                    {{ getSeverityText(dimension.severityLevel) }}
                  </el-tag>
                  <el-tag
                    v-if="!dimension.isActive"
                    size="small"
                    type="info"
                  >
                    已禁用
                  </el-tag>
                </div>
              </div>
              <div class="card-code">{{ dimension.code }}</div>
            </div>
            
            <div class="card-content">
              <p class="dimension-desc">
                {{ dimension.description || '暂无描述' }}
              </p>
              <div class="dimension-meta">
                <div class="meta-row">
                  <span class="meta-label">处理动作：</span>
                  <el-tag size="small" :type="getActionTagType(dimension.actionType)">
                    {{ getActionText(dimension.actionType) }}
                  </el-tag>
                </div>
                <div class="meta-row">
                  <span class="meta-label">置信阈值：</span>
                  <span class="threshold-value">{{ dimension.confidenceThreshold }}</span>
                </div>
                <div class="meta-row">
                  <span class="meta-label">分类：</span>
                  <span>{{ dimension.category || '未分类' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="filteredDimensions.length === 0 && !loading" class="empty-state">
          <el-icon><Box /></el-icon>
          <p>暂无符合条件的维度数据</p>
        </div>
      </el-card>
    </div>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="维度详情"
      width="800px"
    >
      <dimension-detail
        v-if="detailDialogVisible && currentDimension"
        :dimension="currentDimension"
      />
    </el-dialog>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="formDialogVisible"
      :title="isEditing ? '编辑分类' : '新增分类'"
      width="600px"
    >
      <category-form
        v-if="formDialogVisible"
        :category="currentCategory"
        :is-editing="isEditing"
        @submit="handleFormSubmit"
        @cancel="formDialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Grid, Refresh, Search, List, Warning, CircleCheck, CircleClose, Box, Plus 
} from '@element-plus/icons-vue'
import { moderationApi, type ModerationDimension, type ModerationCategory } from '@/api/smartcs/moderation'
import DimensionDetail from './components/DimensionDetail.vue'
import CategoryForm from './components/CategoryForm.vue'

// 数据状态
const loading = ref(false)
const viewMode = ref<'table' | 'card'>('table')
const dimensions = ref<ModerationDimension[]>([])
const detailDialogVisible = ref(false)
const currentDimension = ref<ModerationDimension | null>(null)
const formDialogVisible = ref(false)
const currentCategory = ref<ModerationCategory | null>(null)
const isEditing = ref(false)

// 筛选表单
const filterForm = reactive({
  name: '',
  severityLevel: '',
  actionType: '',
  isActive: undefined as boolean | undefined
})

// 统计数据
const stats = computed(() => {
  const total = dimensions.value.length
  const active = dimensions.value.filter(d => d.isActive).length
  const highRisk = dimensions.value.filter(d => d.severityLevel === 'HIGH').length
  const critical = dimensions.value.filter(d => d.severityLevel === 'CRITICAL').length
  
  return { total, active, highRisk, critical }
})

// 过滤后的维度数据
const filteredDimensions = computed(() => {
  return dimensions.value.filter(dimension => {
    // 按名称筛选
    if (filterForm.name && !dimension.name.includes(filterForm.name)) {
      return false
    }
    
    // 按风险级别筛选
    if (filterForm.severityLevel && dimension.severityLevel !== filterForm.severityLevel) {
      return false
    }
    
    // 按处理动作筛选
    if (filterForm.actionType && dimension.actionType !== filterForm.actionType) {
      return false
    }
    
    // 按状态筛选
    if (filterForm.isActive !== undefined && dimension.isActive !== filterForm.isActive) {
      return false
    }
    
    return true
  })
})

// 页面加载
onMounted(() => {
  loadData()
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const response = await moderationApi.getAllActiveDimensions()
    dimensions.value = response.data
  } catch (error) {
    console.error('加载维度数据失败:', error)
    ElMessage.error('加载维度数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  // 筛选逻辑已在 computed 中处理
}

// 重置
const handleReset = () => {
  Object.assign(filterForm, {
    name: '',
    severityLevel: '',
    actionType: '',
    isActive: undefined
  })
}

// 刷新
const handleRefresh = () => {
  loadData()
}

// 查看详情
const handleViewDetail = (dimension: ModerationDimension) => {
  currentDimension.value = dimension
  detailDialogVisible.value = true
}

// 工具函数
const getSeverityText = (severity?: string) => {
  const map: Record<string, string> = {
    'LOW': '低风险',
    'MEDIUM': '中风险',
    'HIGH': '高风险',
    'CRITICAL': '极高风险'
  }
  return map[severity || ''] || '未知'
}

const getSeverityTagType = (severity?: string) => {
  const map: Record<string, string> = {
    'LOW': 'info',
    'MEDIUM': '',
    'HIGH': 'warning',
    'CRITICAL': 'danger'
  }
  return map[severity || ''] || ''
}

const getActionText = (action?: string) => {
  const map: Record<string, string> = {
    'APPROVE': '通过',
    'REJECT': '拒绝',
    'MANUAL_REVIEW': '人工审核',
    'AUTO_FIX': '自动修复'
  }
  return map[action || ''] || '未知'
}

const getActionTagType = (action?: string) => {
  const map: Record<string, string> = {
    'APPROVE': 'success',
    'REJECT': 'danger',
    'MANUAL_REVIEW': 'warning',
    'AUTO_FIX': 'primary'
  }
  return map[action || ''] || ''
}

// 新增分类
const handleCreate = () => {
  currentCategory.value = null
  isEditing.value = false
  formDialogVisible.value = true
}

// 编辑分类
const handleEdit = (dimension: ModerationDimension) => {
  // 将维度数据转换为分类数据格式
  currentCategory.value = {
    id: dimension.id,
    name: dimension.name,
    code: dimension.code,
    description: dimension.description || '',
    severityLevel: dimension.severityLevel || 'MEDIUM',
    actionType: dimension.actionType || 'MANUAL_REVIEW',
    sortOrder: dimension.sortOrder,
    isActive: dimension.isActive
  }
  isEditing.value = true
  formDialogVisible.value = true
}

// 删除分类
const handleDelete = async (dimension: ModerationDimension) => {
  try {
    await ElMessageBox.confirm(
      `确认删除维度 "${dimension.name}" 吗？删除后无法恢复。`,
      '删除确认',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await moderationApi.deleteCategory(dimension.id!)
    ElMessage.success('删除成功')
    await loadData()
  } catch (error: any) {
    if (error === 'cancel') return
    console.error('删除失败:', error)
    ElMessage.error(error?.message || '删除失败')
  }
}

// 表单提交
const handleFormSubmit = async (formData: any) => {
  try {
    if (isEditing.value) {
      await moderationApi.updateCategory(currentCategory.value!.id!, formData)
      ElMessage.success('更新成功')
    } else {
      await moderationApi.createCategory(formData)
      ElMessage.success('创建成功')
    }
    
    formDialogVisible.value = false
    await loadData()
  } catch (error: any) {
    console.error('操作失败:', error)
    ElMessage.error(error?.message || '操作失败')
  }
}
</script>

<style scoped lang="scss">
.dimension-management {
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
  }

  .stats-section {
    margin-bottom: 24px;

    .stat-card {
      border: none;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

      :deep(.el-card__body) {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20px;
      }

      .stat-content {
        .stat-value {
          font-size: 32px;
          font-weight: 600;
          color: #303133;
          line-height: 1;
        }

        .stat-label {
          color: #909399;
          font-size: 14px;
          margin-top: 4px;
        }
      }

      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;

        &.total {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
        }

        &.active {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          color: white;
        }

        &.high-risk {
          background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
          color: #e6a23c;
        }

        &.critical {
          background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
          color: #f56c6c;
        }
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

      .table-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;

        .table-title {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
        }
      }

      .dimension-name {
        display: flex;
        align-items: center;
        gap: 8px;
      }

      .threshold-value {
        background: #f0f2f5;
        padding: 2px 8px;
        border-radius: 4px;
        font-size: 12px;
        font-weight: 500;
      }

      .dimensions-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
        gap: 16px;

        .dimension-card {
          border: 1px solid #e4e7ed;
          border-radius: 8px;
          padding: 16px;
          cursor: pointer;
          transition: all 0.3s ease;

          &:hover {
            border-color: #409eff;
            box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
          }

          &.disabled {
            opacity: 0.7;
            background: #fafafa;
          }

          .card-header {
            margin-bottom: 12px;

            .card-title {
              display: flex;
              justify-content: space-between;
              align-items: flex-start;
              margin-bottom: 4px;

              .dimension-name {
                font-weight: 500;
                color: #303133;
              }

              .card-tags {
                display: flex;
                gap: 4px;
                flex-shrink: 0;
              }
            }

            .card-code {
              color: #909399;
              font-size: 12px;
              font-family: 'Courier New', monospace;
            }
          }

          .card-content {
            .dimension-desc {
              color: #606266;
              font-size: 13px;
              line-height: 1.4;
              margin: 0 0 12px 0;
            }

            .dimension-meta {
              .meta-row {
                display: flex;
                align-items: center;
                justify-content: space-between;
                margin-bottom: 6px;
                font-size: 12px;

                &:last-child {
                  margin-bottom: 0;
                }

                .meta-label {
                  color: #909399;
                }

                .threshold-value {
                  background: #f0f2f5;
                  padding: 1px 6px;
                  border-radius: 3px;
                  font-weight: 500;
                }
              }
            }
          }
        }
      }

      .empty-state {
        text-align: center;
        padding: 60px 20px;
        color: #909399;

        .el-icon {
          font-size: 64px;
          margin-bottom: 16px;
        }

        p {
          margin: 0;
          font-size: 16px;
        }
      }
    }
  }
}
</style>