<template>
  <div class="app-management">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <div class="title">
        <h2>应用管理</h2>
        <p>创建和管理 AI 应用，支持工作流、聊天助手等多种类型</p>
      </div>
      <div class="actions">
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          创建应用
        </el-button>
      </div>
    </div>

    <!-- 搜索和过滤条件 -->
    <div class="search-bar">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索应用名称、描述或编码"
            @keyup.enter="handleSearch"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.type" placeholder="应用类型" clearable>
            <el-option
              v-for="type in appTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.status" placeholder="应用状态" clearable>
            <el-option
              v-for="status in appStatuses"
              :key="status.value"
              :label="status.label"
              :value="status.value"
            />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 应用列表 -->
    <div class="app-list">
      <el-row :gutter="24" v-loading="loading">
        <el-col :span="6" v-for="app in appList" :key="app.id">
          <div class="app-card" @click="handleEdit(app)">
            <div class="app-icon">
              <span>{{ getAppTypeIcon(app.type) }}</span>
            </div>
            <div class="app-info">
              <h3 class="app-name">{{ app.name }}</h3>
              <p class="app-description">{{ app.description || '暂无描述' }}</p>
              
              <!-- 应用类型和状态 -->
              <div class="app-meta">
                <el-tag type="info" size="small">{{ app.typeName }}</el-tag>
                <el-tag 
                  :type="getStatusTagType(app.status || '')" 
                  size="small"
                >
                  {{ app.statusName }}
                </el-tag>
              </div>

              <!-- 应用标签 -->
              <div class="app-tags" v-if="app.tags && app.tags.length">
                <el-tag
                  v-for="tag in app.tags.slice(0, 3)"
                  :key="tag"
                  size="small"
                  class="tag"
                >
                  {{ tag }}
                </el-tag>
                <span v-if="app.tags.length > 3" class="more-tags">
                  +{{ app.tags.length - 3 }}
                </span>
              </div>

              <!-- 操作按钮 -->
              <div class="app-actions" @click.stop>
                <el-button 
                  text 
                  type="primary" 
                  size="small"
                  @click="handleEdit(app)"
                  :disabled="!app.editable"
                >
                  编辑
                </el-button>
                <el-dropdown @command="(command: string) => handleAction(command, app)">
                  <el-button text size="small">
                    更多
                    <el-icon><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="preview">
                        预览（新窗口）
                      </el-dropdown-item>
                      <el-dropdown-item 
                        command="run" 
                        :disabled="app.status !== 'PUBLISHED'"
                      >
                        运行（新窗口）
                      </el-dropdown-item>
                      <el-dropdown-item divided command="publish" v-if="app.status === 'DRAFT'">
                        发布
                      </el-dropdown-item>
                      <el-dropdown-item command="disable" v-if="app.status === 'PUBLISHED'">
                        停用
                      </el-dropdown-item>
                      <el-dropdown-item command="enable" v-if="app.status === 'DISABLED'">
                        启用
                      </el-dropdown-item>
                      <el-dropdown-item command="duplicate">
                        复制
                      </el-dropdown-item>
                      <el-dropdown-item 
                        command="delete" 
                        v-if="app.editable"
                        class="danger-item"
                      >
                        删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
            
            <!-- 应用时间信息 -->
            <div class="app-footer">
              <span class="create-time">
                {{ formatTime(app.createdAt || '') }}
              </span>
            </div>
          </div>
        </el-col>
        
        <!-- 空状态 -->
        <div v-if="!loading && appList.length === 0" class="empty-state">
          <el-empty description="暂无应用">
            <el-button type="primary" @click="showCreateDialog">创建第一个应用</el-button>
          </el-empty>
        </div>
      </el-row>

      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="searchForm.pageIndex"
          v-model:page-size="searchForm.pageSize"
          :total="total"
          :page-sizes="[12, 24, 48, 96]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </div>

    <!-- 创建应用对话框 -->
    <AppCreateDialog 
      v-model:visible="createDialogVisible"
      @success="handleCreateSuccess"
    />


  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, ArrowDown } from '@element-plus/icons-vue'
import { listApps, deleteApp, updateAppStatus, getAppTypeInfo, type AiAppDTO, APP_TYPES, APP_STATUS } from '@/api/smartcs/app'
import AppCreateDialog from './components/AppCreateDialog.vue'
import { formatTime } from '@/utils/format'

// 数据状态
const loading = ref(false)
const appList = ref<AiAppDTO[]>([])
const total = ref(0)
const createDialogVisible = ref(false)
const router = useRouter()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  type: '',
  status: '',
  pageIndex: 1,
  pageSize: 12
})

// 选项数据
const appTypes = APP_TYPES
const appStatuses = APP_STATUS

// 获取应用类型图标
const getAppTypeIcon = (type: string) => {
  const typeInfo = getAppTypeInfo(type)
  return typeInfo?.icon || '📱'
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  switch (status) {
    case 'PUBLISHED':
      return 'success'
    case 'DISABLED':
      return 'danger'
    case 'DRAFT':
    default:
      return 'info'
  }
}

// 搜索应用列表
const handleSearch = async () => {
  loading.value = true
  try {
    const response = await listApps(searchForm)
    if (response.success) {
      appList.value = response.data
      total.value = response.total
    } else {
      ElMessage.error(response.errMessage || '获取应用列表失败')
    }
  } catch (error) {
    console.error('获取应用列表失败:', error)
    ElMessage.error('获取应用列表失败')
  } finally {
    loading.value = false
  }
}

// 重置搜索条件
const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    type: '',
    status: '',
    pageIndex: 1,
    pageSize: 12
  })
  handleSearch()
}

// 显示创建对话框
const showCreateDialog = () => {
  createDialogVisible.value = true
}

// 创建成功回调
const handleCreateSuccess = () => {
  createDialogVisible.value = false
  handleSearch()
}

// 编辑应用
const handleEdit = (app: AiAppDTO) => {
  router.push(`/platform/smartcs/knowledge/app/${app.id}`)
}

// 预览应用（新窗口打开，管理员模式）
const handlePreview = (app: AiAppDTO) => {
  if (!app.id) {
    ElMessage.warning('应用ID无效')
    return
  }
  
  const previewUrl = `/app/preview/${app.id}`
  window.open(previewUrl, '_blank', 'width=1200,height=800')
}

// 运行应用（新窗口打开，公开模式）
const handleRun = (app: AiAppDTO) => {
  if (!app.id) {
    ElMessage.warning('应用ID无效')
    return
  }
  
  if (app.status !== 'PUBLISHED') {
    ElMessage.warning('仅已发布的应用可以运行')
    return
  }
  
  const runUrl = `/app/run/${app.id}`
  window.open(runUrl, '_blank', 'width=1200,height=800')
}

// 处理应用操作
const handleAction = async (command: string, app: AiAppDTO) => {
  switch (command) {
    case 'preview':
      handlePreview(app)
      break
    case 'run':
      handleRun(app)
      break
    case 'publish':
      await handleStatusChange(app, 'PUBLISHED')
      break
    case 'disable':
      await handleStatusChange(app, 'DISABLED')
      break
    case 'enable':
      await handleStatusChange(app, 'PUBLISHED')
      break
    case 'duplicate':
      handleDuplicate(app)
      break
    case 'delete':
      await handleDelete(app)
      break
  }
}

// 更新应用状态
const handleStatusChange = async (app: AiAppDTO, status: string) => {
  try {
    const response = await updateAppStatus({ id: app.id!, status })
    if (response.success) {
      ElMessage.success('状态更新成功')
      handleSearch()
    } else {
      ElMessage.error(response.errMessage || '状态更新失败')
    }
  } catch (error) {
    console.error('状态更新失败:', error)
    ElMessage.error('状态更新失败')
  }
}

// 复制应用
const handleDuplicate = (app: AiAppDTO) => {
  // TODO: 实现应用复制功能
  ElMessage.info('复制功能开发中...')
}

// 删除应用
const handleDelete = async (app: AiAppDTO) => {
  try {
    await ElMessageBox.confirm(
      `确认删除应用"${app.name}"吗？此操作不可恢复。`,
      '删除应用',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )

    const response = await deleteApp(app.id!)
    if (response.success) {
      ElMessage.success('删除成功')
      handleSearch()
    } else {
      ElMessage.error(response.errMessage || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 组件挂载时获取数据
onMounted(() => {
  handleSearch()
})
</script>

<style scoped lang="scss">
.app-management {
  padding: 20px;
  background: #fff;
  min-height: calc(100vh - 100px);

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;

    .title {
      h2 {
        margin: 0 0 8px 0;
        font-size: 24px;
        font-weight: 600;
        color: #1f2937;
      }

      p {
        margin: 0;
        color: #6b7280;
        font-size: 14px;
      }
    }
  }

  .search-bar {
    margin-bottom: 24px;
    padding: 20px;
    background: #f9fafb;
    border-radius: 8px;
  }

  .app-list {
    .app-card {
      background: #fff;
      border: 1px solid #e5e7eb;
      border-radius: 12px;
      padding: 20px;
      margin-bottom: 24px;
      transition: all 0.3s ease;
      height: 280px;
      display: flex;
      flex-direction: column;
      cursor: pointer;

      &:hover {
        border-color: #3b82f6;
        box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
        transform: translateY(-2px);
      }

      .app-icon {
        text-align: center;
        margin-bottom: 16px;

        span {
          display: inline-block;
          width: 48px;
          height: 48px;
          line-height: 48px;
          font-size: 24px;
          background: #f3f4f6;
          border-radius: 12px;
        }
      }

      .app-info {
        flex: 1;
        display: flex;
        flex-direction: column;

        .app-name {
          margin: 0 0 8px 0;
          font-size: 16px;
          font-weight: 600;
          color: #1f2937;
          text-align: center;
        }

        .app-description {
          margin: 0 0 12px 0;
          color: #6b7280;
          font-size: 12px;
          line-height: 1.4;
          text-align: center;
          display: -webkit-box;
          -webkit-box-orient: vertical;
          -webkit-line-clamp: 2;
          overflow: hidden;
        }

        .app-meta {
          display: flex;
          justify-content: center;
          gap: 8px;
          margin-bottom: 12px;

          .el-tag {
            border-radius: 4px;
          }
        }

        .app-tags {
          display: flex;
          flex-wrap: wrap;
          justify-content: center;
          gap: 4px;
          margin-bottom: 16px;

          .tag {
            font-size: 11px;
            height: 20px;
            line-height: 18px;
            border-radius: 3px;
          }

          .more-tags {
            color: #6b7280;
            font-size: 11px;
          }
        }

        .app-actions {
          display: flex;
          justify-content: center;
          gap: 8px;
          margin-top: auto;
        }
      }

      .app-footer {
        margin-top: 12px;
        text-align: center;
        border-top: 1px solid #f3f4f6;
        padding-top: 12px;

        .create-time {
          color: #9ca3af;
          font-size: 11px;
        }
      }
    }

    .empty-state {
      grid-column: 1 / -1;
      text-align: center;
      padding: 60px 20px;
    }
  }

  .pagination {
    display: flex;
    justify-content: center;
    margin-top: 32px;
  }

  :deep(.danger-item) {
    color: #ef4444;
  }
}
</style>