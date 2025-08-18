<template>
  <div class="intent-catalog-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">意图目录管理</h1>
        <p class="page-description">管理和配置意图分类目录结构</p>
      </div>
      <div class="header-actions">
        <el-button 
          type="primary" 
          @click="openCreateModal"
          :icon="Plus"
        >
          新建目录
        </el-button>
        <el-button 
          @click="refreshList"
          :icon="RefreshRight"
          :loading="loading"
        >
          刷新
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="目录名称">
          <el-input 
            v-model="searchForm.keyword" 
            placeholder="请输入目录名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="父级目录">
          <el-select 
            v-model="searchForm.parentId" 
            placeholder="选择父级目录"
            clearable
            style="width: 200px"
          >
            <el-option label="顶级目录" :value="null" />
            <el-option
              v-for="catalog in catalogOptions"
              :key="catalog.id"
              :label="catalog.name"
              :value="catalog.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchCatalogs">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 目录列表 -->
    <el-card class="table-card" shadow="never">
      <el-table 
        :data="catalogList" 
        v-loading="loading"
        stripe
        style="width: 100%"
        :empty-text="loading ? '加载中...' : '暂无目录数据'"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column prop="name" label="目录名称" width="200">
          <template #default="scope">
            <div class="catalog-name">
              <el-icon v-if="scope.row.children && scope.row.children.length > 0" class="folder-icon">
                <Folder />
              </el-icon>
              <el-icon v-else class="file-icon">
                <Document />
              </el-icon>
              <span>{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="目录代码" width="150" />
        <el-table-column prop="description" label="描述" min-width="250" show-overflow-tooltip />
        <el-table-column label="排序" width="80" align="center">
          <template #default="scope">
            <el-tag size="small" type="info">{{ scope.row.sortOrder || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="意图数量" width="100" align="center">
          <template #default="scope">
            <el-badge 
              :value="scope.row.intentCount || 0" 
              :max="99"
              type="primary"
            >
              <el-button size="small" text>查看</el-button>
            </el-badge>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button 
                size="small" 
                type="primary" 
                text
                @click="editCatalog(scope.row)"
                :icon="Edit"
              >
                编辑
              </el-button>
              <el-button 
                size="small" 
                type="success" 
                text
                @click="addSubCatalog(scope.row)"
                :icon="Plus"
              >
                子目录
              </el-button>
              <el-button 
                size="small" 
                type="danger" 
                text
                @click="deleteCatalog(scope.row)"
                :icon="Delete"
                :disabled="scope.row.intentCount > 0"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="pagination.total > 0">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 目录编辑模态框 -->
    <CatalogModal 
      v-model:visible="modalVisible"
      :catalog-data="currentCatalog"
      :catalog-options="catalogOptions"
      @save="handleSave"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, 
  RefreshRight, 
  Edit, 
  Delete,
  Folder,
  Document 
} from '@element-plus/icons-vue'
import { intentApi, catalogApi } from '@/api/smartcs/intent'
import CatalogModal from './components/CatalogModal.vue'

// 响应式数据
const loading = ref(false)
const modalVisible = ref(false)
const currentCatalog = ref(null)
const catalogList = ref([])
const catalogOptions = ref([])

const searchForm = reactive({
  keyword: '',
  parentId: null
})

const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0
})

// 方法
const openCreateModal = () => {
  currentCatalog.value = null
  modalVisible.value = true
}

const editCatalog = (catalog) => {
  currentCatalog.value = { ...catalog }
  modalVisible.value = true
}

const addSubCatalog = (parentCatalog) => {
  currentCatalog.value = {
    parentId: parentCatalog.id,
    parentName: parentCatalog.name
  }
  modalVisible.value = true
}

const deleteCatalog = async (catalog) => {
  if (catalog.intentCount > 0) {
    ElMessage.warning('该目录下还有意图，无法删除')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除目录 "${catalog.name}" 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    await catalogApi.deleteCatalog(catalog.id)
    ElMessage.success('删除成功')
    await loadCatalogList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.message || error))
    }
  }
}

const searchCatalogs = () => {
  pagination.current = 1
  loadCatalogList()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    parentId: null
  })
  pagination.current = 1
  loadCatalogList()
}

const refreshList = () => {
  loadCatalogList()
}

const loadCatalogList = async () => {
  try {
    loading.value = true
    
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      parentId: searchForm.parentId,
      keyword: searchForm.keyword
    }
    
    const response = await catalogApi.getCatalogPage(params)
    
    if (response.success) {
      catalogList.value = response.data || []
      pagination.total = response.totalCount || 0
    } else {
      ElMessage.error(response.errMessage || '获取目录列表失败')
    }
  } catch (error) {
    ElMessage.error('获取目录列表失败: ' + (error.message || error))
  } finally {
    loading.value = false
  }
}

const loadCatalogOptions = async () => {
  try {
    const response = await catalogApi.getCatalogList()
    if (response.success) {
      catalogOptions.value = response.data || []
    }
  } catch (error) {
    console.error('获取目录选项失败:', error)
  }
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.current = 1
  loadCatalogList()
}

const handleCurrentChange = (val) => {
  pagination.current = val
  loadCatalogList()
}

const handleSave = async () => {
  modalVisible.value = false
  await loadCatalogList()
  await loadCatalogOptions()
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

// 生命周期
onMounted(() => {
  loadCatalogList()
  loadCatalogOptions()
})
</script>

<style scoped>
.intent-catalog-management {
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

.catalog-name {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.folder-icon {
  color: #e6a23c;
}

.file-icon {
  color: #909399;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
  justify-content: center;
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