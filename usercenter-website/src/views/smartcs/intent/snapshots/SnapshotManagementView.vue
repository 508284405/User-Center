<template>
  <div class="snapshot-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">快照管理</h1>
        <p class="page-description">管理意图配置快照和版本发布</p>
      </div>
      <div class="header-actions">
        <el-button 
          type="primary" 
          @click="createSnapshot"
          :loading="loading.createSnapshot"
          :icon="Camera"
        >
          创建快照
        </el-button>
        <el-button 
          @click="refreshList"
          :icon="RefreshRight"
          :loading="loading.snapshotList"
        >
          刷新
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="快照代码">
          <el-input 
            v-model="searchForm.code" 
            placeholder="请输入快照代码"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="快照名称">
          <el-input 
            v-model="searchForm.name" 
            placeholder="请输入快照名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select 
            v-model="searchForm.status" 
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="激活" value="ACTIVE" />
            <el-option label="草稿" value="DRAFT" />
            <el-option label="停用" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchSnapshots">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 快照列表 -->
    <el-card class="table-card" shadow="never">
      <el-table 
        :data="snapshotList" 
        v-loading="loading.snapshotList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="code" label="快照代码" width="150" />
        <el-table-column prop="name" label="快照名称" width="200" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag 
              :type="getStatusTagType(scope.row.status)"
              size="small"
            >
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="scope" label="作用域" width="100" align="center">
          <template #default="scope">
            <el-tag type="info" size="small">
              {{ scope.row.scope || 'global' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="意图数量" width="100" align="center">
          <template #default="scope">
            <el-badge 
              :value="(scope.row.items || []).length" 
              :max="99"
              type="primary"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" align="center" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button 
                size="small" 
                type="primary" 
                text
                @click="viewSnapshot(scope.row)"
                :icon="View"
              >
                查看
              </el-button>
              <el-button 
                size="small" 
                type="success" 
                text
                @click="publishSnapshot(scope.row)"
                :disabled="scope.row.status === 'ACTIVE'"
                :icon="VideoPlay"
              >
                发布
              </el-button>
              <el-button 
                size="small" 
                type="info" 
                text
                @click="exportSnapshot(scope.row)"
                :icon="Download"
              >
                导出
              </el-button>
              <el-button 
                size="small" 
                type="danger" 
                text
                @click="deleteSnapshot(scope.row)"
                :icon="Delete"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="snapshotPagination.current"
          v-model:page-size="snapshotPagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="snapshotPagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 快照详情模态框 -->
    <SnapshotModal 
      v-model:visible="modalVisible"
      :snapshot-data="currentSnapshot"
      @refresh="refreshList"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Camera,
  RefreshRight, 
  View, 
  VideoPlay,
  Download,
  Delete 
} from '@element-plus/icons-vue'
import { useSmartCSAdminStore } from '@/stores/admin/admin.js'
import { snapshotApi } from '@/api/smartcs/intent'
import SnapshotModal from './components/SnapshotModal.vue'

const store = useSmartCSAdminStore()

// 响应式数据
const modalVisible = ref(false)
const currentSnapshot = ref(null)

const loading = reactive({
  createSnapshot: false
})

const searchForm = reactive({
  code: '',
  name: '',
  status: ''
})

// 计算属性
const snapshotList = computed(() => store.snapshotList)
const snapshotPagination = computed(() => store.snapshotPagination)

// 方法
const createSnapshot = async () => {
  loading.createSnapshot = true
  try {
    const snapshotData = {
      name: `快照_${new Date().toISOString().slice(0, 19).replace(/[:-]/g, '')}`,
      description: '手动创建的快照',
      scope: 'global'
    }
    
    await store.createSnapshot(snapshotData)
    ElMessage.success('快照创建成功')
  } catch (error) {
    ElMessage.error('快照创建失败: ' + error.message)
  } finally {
    loading.createSnapshot = false
  }
}

const viewSnapshot = async (snapshot) => {
  try {
    const detail = await store.loadSnapshotDetail(snapshot.id)
    if (detail) {
      currentSnapshot.value = detail
      modalVisible.value = true
    }
  } catch (error) {
    ElMessage.error('加载快照详情失败')
  }
}

const publishSnapshot = async (snapshot) => {
  try {
    await ElMessageBox.confirm(
      `确定要发布快照 "${snapshot.name}" 吗？发布后将替换当前激活的快照。`,
      '确认发布',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    await store.publishSnapshot(snapshot.id)
    ElMessage.success('快照发布成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('快照发布失败: ' + error.message)
    }
  }
}

const exportSnapshot = async (snapshot) => {
  try {
    const response = await snapshotApi.exportSnapshotConfig(snapshot.id)
    if (response.success) {
      // 创建下载链接
      const blob = new Blob([JSON.stringify(response.data, null, 2)], {
        type: 'application/json'
      })
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `snapshot-${snapshot.code}-config.json`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      window.URL.revokeObjectURL(url)
      
      ElMessage.success('配置导出成功')
    }
  } catch (error) {
    ElMessage.error('配置导出失败: ' + error.message)
  }
}

const deleteSnapshot = async (snapshot) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除快照 "${snapshot.name}" 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    await store.deleteSnapshot(snapshot.id)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + error.message)
    }
  }
}

const searchSnapshots = () => {
  snapshotPagination.value.current = 1
  loadSnapshotList()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    code: '',
    name: '',
    status: ''
  })
  snapshotPagination.value.current = 1
  loadSnapshotList()
}

const refreshList = () => {
  loadSnapshotList()
}

const loadSnapshotList = () => {
  const params = {}
  if (searchForm.code) params.code = searchForm.code
  if (searchForm.name) params.name = searchForm.name
  if (searchForm.status) params.status = searchForm.status
  
  store.loadSnapshotList(params)
}

const handleSizeChange = (val) => {
  snapshotPagination.value.pageSize = val
  snapshotPagination.value.current = 1
  loadSnapshotList()
}

const handleCurrentChange = (val) => {
  snapshotPagination.value.current = val
  loadSnapshotList()
}

const getStatusTagType = (status) => {
  switch (status) {
    case 'ACTIVE':
      return 'success'
    case 'DRAFT':
      return 'warning'
    case 'INACTIVE':
      return 'info'
    default:
      return 'info'
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 'ACTIVE':
      return '激活'
    case 'DRAFT':
      return '草稿'
    case 'INACTIVE':
      return '停用'
    default:
      return '未知'
  }
}

const formatDate = (timestamp) => {
  if (!timestamp) return ''
  return new Date(timestamp).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadSnapshotList()
})
</script>

<style scoped>
.snapshot-management {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1.5rem;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 1.75rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 0.5rem 0;
}

.page-description {
  color: #6c757d;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 0.75rem;
}

.search-card {
  margin-bottom: 1.5rem;
  border-radius: 8px;
}

.table-card {
  border-radius: 8px;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
  justify-content: center;
  flex-wrap: wrap;
}

.pagination-wrapper {
  margin-top: 1rem;
  display: flex;
  justify-content: flex-end;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 1rem;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .action-buttons {
    flex-direction: column;
    gap: 0.25rem;
  }

  .pagination-wrapper {
    justify-content: center;
  }
}
</style>