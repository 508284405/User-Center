<template>
  <div class="intent-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">意图管理</h1>
        <p class="page-description">管理和配置智能分类意图</p>
      </div>
      <div class="header-actions">
        <el-button 
          type="success" 
          @click="goToCatalogManagement"
          :icon="FolderOpened"
        >
          目录管理
        </el-button>
        <el-button 
          type="primary" 
          @click="openCreateModal"
          :icon="Plus"
        >
          新建意图
        </el-button>
        <el-button 
          @click="refreshList"
          :icon="RefreshRight"
          :loading="loading.intentList"
        >
          刷新
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="意图代码">
          <el-input 
            v-model="searchForm.code" 
            placeholder="请输入意图代码"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="意图名称">
          <el-input 
            v-model="searchForm.name" 
            placeholder="请输入意图名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="分类目录">
          <el-select 
            v-model="searchForm.catalogId" 
            placeholder="请选择目录"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="catalog in catalogList"
              :key="catalog.id"
              :label="catalog.name"
              :value="catalog.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select 
            v-model="searchForm.status" 
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="激活" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchIntents">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 意图列表 -->
    <el-card class="table-card" shadow="never">
      <!-- 调试信息 -->
      <div style="margin-bottom: 1rem; padding: 0.5rem; background-color: #f5f5f5; border-radius: 4px; font-size: 12px; color: #666;">
        <div>调试信息: 列表长度={{ intentList.length }}, 总数={{ intentPagination.total }}, 当前页={{ intentPagination.current }}, 页大小={{ intentPagination.pageSize }}</div>
        <div>加载状态: {{ loading.intentList ? '加载中' : '加载完成' }}</div>
      </div>
      
      <el-table 
        :data="intentList" 
        v-loading="loading.intentList"
        stripe
        style="width: 100%"
        :empty-text="loading.intentList ? '加载中...' : '暂无意图数据'"
      >
        <el-table-column prop="code" label="意图代码" width="150" />
        <el-table-column prop="name" label="意图名称" width="200" />
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
        <el-table-column label="标签数量" width="120" align="center">
          <template #default="scope">
            <div class="label-count-cell">
              <el-tag 
                :type="(scope.row.labels || []).length > 0 ? 'primary' : 'info'" 
                size="small"
                class="count-tag"
              >
                {{ (scope.row.labels || []).length }}
              </el-tag>
              <el-button 
                size="small" 
                text 
                type="primary"
                @click="viewLabels(scope.row)"
                :disabled="!(scope.row.labels || []).length"
                class="view-btn"
              >
                查看
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button 
                size="small" 
                type="primary" 
                text
                @click="editIntent(scope.row)"
                :icon="Edit"
              >
                编辑
              </el-button>
              <el-button 
                size="small" 
                type="info" 
                text
                @click="viewVersions(scope.row)"
                :icon="Clock"
              >
                版本
              </el-button>
              <el-button 
                size="small" 
                type="success" 
                text
                @click="manageCorpus(scope.row)"
                :icon="Document"
              >
                语料
              </el-button>
              <el-button 
                size="small" 
                type="danger" 
                text
                @click="deleteIntent(scope.row)"
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
          v-model:current-page="intentPagination.current"
          v-model:page-size="intentPagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="intentPagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 意图编辑模态框 -->
    <IntentModal 
      v-model:visible="modalVisible"
      :intent-data="currentIntent"
      @save="handleSave"
    />

    <!-- 语料管理模态框 -->
    <CorpusManagementModal
      v-model:visible="corpusModalVisible"
      :intent-data="currentCorpusIntent"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { 
  Plus, 
  RefreshRight, 
  Edit, 
  Clock, 
  Delete,
  Document,
  FolderOpened
} from '@element-plus/icons-vue'
import { useSmartCSAdminStore } from '@/stores/admin/admin.js'
import IntentModal from './components/IntentModal.vue'
import CorpusManagementModal from './components/CorpusManagementModal.vue'

const store = useSmartCSAdminStore()
const router = useRouter()
const route = useRoute()

// 响应式数据
const modalVisible = ref(false)
const currentIntent = ref(null)

// 语料管理相关
const corpusModalVisible = ref(false)
const currentCorpusIntent = ref(null)

const searchForm = reactive({
  code: '',
  name: '',
  catalogId: '',
  status: ''
})

// 计算属性
const intentList = computed(() => store.intentList)
const intentPagination = computed(() => store.intentPagination)
const loading = computed(() => store.loading)
const catalogList = computed(() => store.catalogList)

// 方法
const openCreateModal = () => {
  currentIntent.value = null
  modalVisible.value = true
}

const goToCatalogManagement = () => {
  router.push('/dashboard/intent-management/catalog')
}

const editIntent = (intent) => {
  currentIntent.value = { ...intent }
  modalVisible.value = true
}

const deleteIntent = async (intent) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除意图 "${intent.name}" 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    await store.deleteIntent(intent.id)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + error.message)
    }
  }
}

const viewVersions = (intent) => {
  if (!intent.id) {
    ElMessage.warning('无效的意图数据')
    return
  }
  
  // 暂时显示版本信息，可以后续扩展为详细的版本管理弹窗
  ElMessage({
    message: `意图版本管理功能\n意图ID: ${intent.id}\n意图名称: ${intent.name}\n当前状态: ${intent.status}`,
    type: 'info',
    duration: 3000,
    dangerouslyUseHTMLString: false
  })
}

const manageCorpus = (intent) => {
  currentCorpusIntent.value = intent
  corpusModalVisible.value = true
}

const searchIntents = () => {
  intentPagination.value.current = 1
  loadIntentList()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    code: '',
    name: '',
    catalogId: '',
    status: ''
  })
  intentPagination.value.current = 1
  loadIntentList()
}

const refreshList = () => {
  loadIntentList()
}

const loadIntentList = () => {
  const params = {}
  if (searchForm.code) params.keyword = searchForm.code
  if (searchForm.name) params.keyword = searchForm.name
  if (searchForm.catalogId) params.catalogId = searchForm.catalogId
  if (searchForm.status) params.status = searchForm.status
  
  store.loadIntentList(params)
}

const handleSizeChange = (val) => {
  intentPagination.value.pageSize = val
  intentPagination.value.current = 1
  loadIntentList()
}

const handleCurrentChange = (val) => {
  intentPagination.value.current = val
  loadIntentList()
}

const handleSave = () => {
  modalVisible.value = false
  refreshList()
}

const getStatusTagType = (status) => {
  switch (status) {
    case 'ACTIVE':
      return 'success'
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
    case 'INACTIVE':
      return '停用'
    default:
      return '未知'
  }
}

const formatDate = (timestamp) => {
  if (!timestamp) return ''
  
  try {
    // 检查时间戳是否为毫秒级（13位数字）
    let date
    if (typeof timestamp === 'number') {
      // 如果是毫秒级时间戳，直接使用
      if (timestamp.toString().length === 13) {
        date = new Date(timestamp)
      } else if (timestamp.toString().length === 10) {
        // 如果是秒级时间戳，转换为毫秒
        date = new Date(timestamp * 1000)
      } else {
        date = new Date(timestamp)
      }
    } else {
      date = new Date(timestamp)
    }
    
    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      return '时间格式错误'
    }
    
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } catch (error) {
    console.error('时间格式化错误:', error, '时间戳:', timestamp)
    return '时间格式错误'
  }
}

const viewLabels = (intent) => {
  if (!intent.labels || intent.labels.length === 0) {
    ElMessage.info('该意图暂无标签')
    return
  }
  
  const labelsText = intent.labels.join(', ')
  ElMessage.info(`标签：${labelsText}`)
}

// 生命周期
onMounted(() => {
  // 处理从目录管理页面传来的查询参数
  if (route.query.catalogId) {
    searchForm.catalogId = route.query.catalogId
    // 如果有目录名称，可以显示一个提示
    if (route.query.catalogName) {
      ElMessage.success(`已筛选目录：${route.query.catalogName}`)
    }
  }
  
  loadIntentList()
  store.loadCatalogList()
})
</script>

<style scoped>
.intent-management {
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

.label-count-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.label-count-cell .count-tag {
  min-width: 24px;
  text-align: center;
}

.label-count-cell .view-btn {
  padding: 2px 4px;
  font-size: 12px;
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
