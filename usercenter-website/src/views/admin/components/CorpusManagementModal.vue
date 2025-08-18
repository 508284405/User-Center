<template>
  <el-dialog
    v-model="visible"
    :title="`语料管理 - ${intentData?.name || ''}`"
    width="80%"
    @close="handleClose"
  >
    <!-- 操作工具栏 -->
    <div class="corpus-toolbar">
      <div class="toolbar-left">
        <el-button
          type="primary"
          :icon="Plus"
          @click="openAddDialog"
        >
          添加语料
        </el-button>
        <el-button
          type="success"
          :icon="Upload"
          @click="openBatchImportDialog"
        >
          批量导入
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索语料内容"
          style="width: 300px"
          clearable
          @input="searchCorpus"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 语料列表 -->
    <el-table
      :data="corpusList"
      v-loading="loading.corpusList"
      stripe
      style="width: 100%; margin-top: 16px"
      :empty-text="loading.corpusList ? '加载中...' : '暂无语料数据'"
      max-height="400px"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="text" label="语料内容" min-width="300" show-overflow-tooltip />
      <el-table-column prop="type" label="类型" width="100" align="center">
        <template #default="scope">
          <el-tag :type="getTypeTagType(scope.row.type)" size="small">
            {{ getTypeText(scope.row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="source" label="来源" width="120" show-overflow-tooltip />
      <el-table-column prop="confidenceScore" label="置信度" width="100" align="center">
        <template #default="scope">
          <span v-if="scope.row.confidenceScore">
            {{ (scope.row.confidenceScore * 100).toFixed(1) }}%
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center" fixed="right">
        <template #default="scope">
          <div class="action-buttons">
            <el-button
              size="small"
              type="primary"
              text
              @click="editCorpus(scope.row)"
              :icon="Edit"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="danger"
              text
              @click="deleteCorpus(scope.row)"
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
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 语料编辑对话框 -->
    <CorpusEditDialog
      v-model:visible="editDialogVisible"
      :corpus-data="currentCorpus"
      :intent-id="intentData?.id"
      @save="handleCorpusSave"
    />

    <!-- 批量导入对话框 -->
    <CorpusBatchImportDialog
      v-model:visible="batchImportDialogVisible"
      :intent-id="intentData?.id"
      @import="handleBatchImport"
    />
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Upload,
  Search,
  Edit,
  Delete
} from '@element-plus/icons-vue'
import { sampleApi } from '@/api/smartcs/intent'
import CorpusEditDialog from './CorpusEditDialog.vue'
import CorpusBatchImportDialog from './CorpusBatchImportDialog.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  intentData: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:visible'])

// 响应式数据
const corpusList = ref([])
const searchKeyword = ref('')
const editDialogVisible = ref(false)
const batchImportDialogVisible = ref(false)
const currentCorpus = ref(null)

const loading = reactive({
  corpusList: false
})

const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0
})

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

// 监听对话框显示状态
watch(() => props.visible, (newVal) => {
  if (newVal && props.intentData?.id) {
    loadCorpusList()
  }
})

// 方法
const loadCorpusList = async () => {
  if (!props.intentData?.id) return
  
  loading.corpusList = true
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    }
    
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    
    const response = await sampleApi.getSampleList(props.intentData.id, params)
    
    if (response.success) {
      corpusList.value = response.data?.list || []
      pagination.total = response.data?.total || 0
    } else {
      ElMessage.error(response.errMessage || '加载语料列表失败')
    }
  } catch (error) {
    console.error('加载语料列表失败:', error)
    ElMessage.error('加载语料列表失败: ' + error.message)
  } finally {
    loading.corpusList = false
  }
}

const searchCorpus = () => {
  pagination.current = 1
  loadCorpusList()
}

const openAddDialog = () => {
  currentCorpus.value = null
  editDialogVisible.value = true
}

const editCorpus = (corpus) => {
  currentCorpus.value = { ...corpus }
  editDialogVisible.value = true
}

const deleteCorpus = async (corpus) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除语料 "${corpus.text}" 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const response = await sampleApi.deleteSample(corpus.id)
    
    if (response.success) {
      ElMessage.success('删除成功')
      loadCorpusList()
    } else {
      ElMessage.error(response.errMessage || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除语料失败:', error)
      ElMessage.error('删除失败: ' + error.message)
    }
  }
}

const openBatchImportDialog = () => {
  batchImportDialogVisible.value = true
}

const handleCorpusSave = () => {
  editDialogVisible.value = false
  loadCorpusList()
}

const handleBatchImport = () => {
  batchImportDialogVisible.value = false
  loadCorpusList()
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.current = 1
  loadCorpusList()
}

const handleCurrentChange = (val) => {
  pagination.current = val
  loadCorpusList()
}

const handleClose = () => {
  visible.value = false
  // 重置搜索条件
  searchKeyword.value = ''
  pagination.current = 1
}

const getTypeTagType = (type) => {
  switch (type) {
    case 'POSITIVE':
      return 'success'
    case 'NEGATIVE':
      return 'danger'
    case 'BOUNDARY':
      return 'warning'
    default:
      return 'info'
  }
}

const getTypeText = (type) => {
  switch (type) {
    case 'POSITIVE':
      return '正样本'
    case 'NEGATIVE':
      return '负样本'
    case 'BOUNDARY':
      return '边界样本'
    default:
      return '未知'
  }
}

const formatDate = (timestamp) => {
  if (!timestamp) return ''
  return new Date(timestamp).toLocaleString('zh-CN')
}
</script>

<style scoped>
.corpus-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.toolbar-left {
  display: flex;
  gap: 12px;
}

.toolbar-right {
  display: flex;
  align-items: center;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .corpus-toolbar {
    flex-direction: column;
    gap: 12px;
  }
  
  .toolbar-right {
    width: 100%;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .pagination-wrapper {
    justify-content: center;
  }
}
</style>