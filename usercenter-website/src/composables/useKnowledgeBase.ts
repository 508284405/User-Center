import { ref, reactive, readonly, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  knowledgeBaseApi,
  type KnowledgeBaseDTO,
  type KnowledgeBaseSettings,
  type RecallTestRequest,
  type RecallTestResult,
  type ChunkStatusUpdateRequest,
  type ChunkUpdateRequest
} from '@/api/smartcs/knowledgeBase'

// 知识库详情状态
interface KnowledgeBaseState {
  data: KnowledgeBaseDTO | null
  settings: KnowledgeBaseSettings | null
  loading: boolean
  error: string | null
}

// 召回测试状态
interface RecallTestState {
  loading: boolean
  results: RecallTestResult[]
  error: string | null
  lastQuery: string
}

// 分块管理状态
interface ChunkManagementState {
  chunks: any[]
  loading: boolean
  error: string | null
  totalCount: number
  pageIndex: number
  pageSize: number
}

// 组合式函数
export function useKnowledgeBase(knowledgeBaseId?: number) {
  // 知识库基础状态
  const knowledgeBaseState = reactive<KnowledgeBaseState>({
    data: null,
    settings: null,
    loading: false,
    error: null
  })

  // 召回测试状态
  const recallTestState = reactive<RecallTestState>({
    loading: false,
    results: [],
    error: null,
    lastQuery: ''
  })

  // 分块管理状态
  const chunkManagementState = reactive<ChunkManagementState>({
    chunks: [],
    loading: false,
    error: null,
    totalCount: 0,
    pageIndex: 1,
    pageSize: 20
  })

  // 计算属性
  const isLoading = computed(() => 
    knowledgeBaseState.loading || recallTestState.loading || chunkManagementState.loading
  )

  const hasError = computed(() => 
    knowledgeBaseState.error || recallTestState.error || chunkManagementState.error
  )

  const knowledgeBaseData = computed(() => knowledgeBaseState.data)
  const knowledgeBaseSettings = computed(() => knowledgeBaseState.settings)
  const recallTestResults = computed(() => recallTestState.results)
  const documentChunks = computed(() => chunkManagementState.chunks)

  // 获取知识库详情
  const fetchKnowledgeBase = async (id?: number) => {
    const targetId = id || knowledgeBaseId
    if (!targetId) {
      knowledgeBaseState.error = '知识库ID不能为空'
      return
    }

    try {
      knowledgeBaseState.loading = true
      knowledgeBaseState.error = null

      const response = await knowledgeBaseApi.getById(targetId)
      
      if (response.success && response.data) {
        knowledgeBaseState.data = response.data
      } else {
        throw new Error(response.errMessage || '获取知识库详情失败')
      }
    } catch (error: any) {
      console.error('获取知识库详情失败:', error)
      knowledgeBaseState.error = error.message || '获取知识库详情失败'
      ElMessage.error(knowledgeBaseState.error)
    } finally {
      knowledgeBaseState.loading = false
    }
  }

  // 获取知识库设置
  const fetchKnowledgeBaseSettings = async (id?: number) => {
    const targetId = id || knowledgeBaseId
    if (!targetId) {
      knowledgeBaseState.error = '知识库ID不能为空'
      return
    }

    try {
      knowledgeBaseState.loading = true
      knowledgeBaseState.error = null

      const response = await knowledgeBaseApi.getSettings(targetId)
      
      if (response.success && response.data) {
        knowledgeBaseState.settings = response.data
      } else {
        throw new Error(response.errMessage || '获取知识库设置失败')
      }
    } catch (error: any) {
      console.error('获取知识库设置失败:', error)
      knowledgeBaseState.error = error.message || '获取知识库设置失败'
      ElMessage.error(knowledgeBaseState.error)
    } finally {
      knowledgeBaseState.loading = false
    }
  }

  // 更新知识库设置
  const updateKnowledgeBaseSettings = async (settings: KnowledgeBaseSettings) => {
    try {
      knowledgeBaseState.loading = true
      knowledgeBaseState.error = null

      const response = await knowledgeBaseApi.updateSettings(settings)
      
      if (response.success) {
        knowledgeBaseState.settings = settings
        ElMessage.success('知识库设置更新成功')
        return true
      } else {
        throw new Error(response.errMessage || '更新知识库设置失败')
      }
    } catch (error: any) {
      console.error('更新知识库设置失败:', error)
      knowledgeBaseState.error = error.message || '更新知识库设置失败'
      ElMessage.error(knowledgeBaseState.error)
      return false
    } finally {
      knowledgeBaseState.loading = false
    }
  }

  // 执行召回测试
  const performRecallTest = async (request: RecallTestRequest) => {
    try {
      recallTestState.loading = true
      recallTestState.error = null
      recallTestState.results = []

      const response = await knowledgeBaseApi.recallTest(request)
      
      if (response.success && response.data) {
        recallTestState.results = response.data
        recallTestState.lastQuery = request.query
        return response.data
      } else {
        throw new Error(response.errMessage || '召回测试失败')
      }
    } catch (error: any) {
      console.error('召回测试失败:', error)
      recallTestState.error = error.message || '召回测试失败'
      ElMessage.error(recallTestState.error)
      return []
    } finally {
      recallTestState.loading = false
    }
  }

  // 重置召回测试结果
  const resetRecallTest = () => {
    recallTestState.results = []
    recallTestState.error = null
    recallTestState.lastQuery = ''
  }

  // 获取文档分块列表
  const fetchDocumentChunks = async (contentId: number, params?: any) => {
    try {
      chunkManagementState.loading = true
      chunkManagementState.error = null

      const response = await knowledgeBaseApi.getDocumentChunks(contentId, {
        pageIndex: chunkManagementState.pageIndex,
        pageSize: chunkManagementState.pageSize,
        ...params
      })
      
      if (response.success && response.data) {
        chunkManagementState.chunks = response.data
        chunkManagementState.totalCount = response.totalCount || 0
        return response.data
      } else {
        throw new Error(response.errMessage || '获取分块列表失败')
      }
    } catch (error: any) {
      console.error('获取分块列表失败:', error)
      chunkManagementState.error = error.message || '获取分块列表失败'
      ElMessage.error(chunkManagementState.error)
      return []
    } finally {
      chunkManagementState.loading = false
    }
  }

  // 更新分块状态
  const updateChunkStatus = async (request: ChunkStatusUpdateRequest) => {
    try {
      const response = await knowledgeBaseApi.updateChunkStatus(request)
      
      if (response.success) {
        // 更新本地状态
        const chunkIndex = chunkManagementState.chunks.findIndex(
          chunk => chunk.id === request.chunkId
        )
        if (chunkIndex !== -1) {
          chunkManagementState.chunks[chunkIndex].status = request.status
        }
        
        ElMessage.success(`分块${request.status === 'enabled' ? '启用' : '禁用'}成功`)
        return true
      } else {
        throw new Error(response.errMessage || '更新分块状态失败')
      }
    } catch (error: any) {
      console.error('更新分块状态失败:', error)
      ElMessage.error(error.message || '更新分块状态失败')
      return false
    }
  }

  // 更新分块内容
  const updateChunkContent = async (request: ChunkUpdateRequest) => {
    try {
      const response = await knowledgeBaseApi.updateChunk(request)
      
      if (response.success) {
        // 更新本地状态
        const chunkIndex = chunkManagementState.chunks.findIndex(
          chunk => chunk.id === request.chunkId
        )
        if (chunkIndex !== -1) {
          chunkManagementState.chunks[chunkIndex].content = request.content
          if (request.metadata) {
            chunkManagementState.chunks[chunkIndex].metadata = request.metadata
          }
        }
        
        ElMessage.success('分块更新成功')
        return true
      } else {
        throw new Error(response.errMessage || '更新分块失败')
      }
    } catch (error: any) {
      console.error('更新分块失败:', error)
      ElMessage.error(error.message || '更新分块失败')
      return false
    }
  }

  // 删除分块
  const deleteChunk = async (chunkId: number) => {
    try {
      const response = await knowledgeBaseApi.deleteChunk(chunkId)
      
      if (response.success) {
        // 从本地状态中移除
        const chunkIndex = chunkManagementState.chunks.findIndex(
          chunk => chunk.id === chunkId
        )
        if (chunkIndex !== -1) {
          chunkManagementState.chunks.splice(chunkIndex, 1)
          chunkManagementState.totalCount -= 1
        }
        
        ElMessage.success('分块删除成功')
        return true
      } else {
        throw new Error(response.errMessage || '删除分块失败')
      }
    } catch (error: any) {
      console.error('删除分块失败:', error)
      ElMessage.error(error.message || '删除分块失败')
      return false
    }
  }

  // 批量更新分块状态
  const batchUpdateChunkStatus = async (chunkIds: number[], status: 'enabled' | 'disabled') => {
    try {
      const promises = chunkIds.map(chunkId => 
        updateChunkStatus({ chunkId, status })
      )
      
      const results = await Promise.all(promises)
      const successCount = results.filter(result => result).length
      
      if (successCount === chunkIds.length) {
        ElMessage.success(`批量${status === 'enabled' ? '启用' : '禁用'}成功`)
      } else if (successCount > 0) {
        ElMessage.warning(`部分分块${status === 'enabled' ? '启用' : '禁用'}成功 (${successCount}/${chunkIds.length})`)
      }
      
      return successCount
    } catch (error: any) {
      console.error('批量更新分块状态失败:', error)
      ElMessage.error('批量操作失败')
      return 0
    }
  }

  // 批量删除分块
  const batchDeleteChunks = async (chunkIds: number[]) => {
    try {
      const promises = chunkIds.map(chunkId => deleteChunk(chunkId))
      
      const results = await Promise.all(promises)
      const successCount = results.filter(result => result).length
      
      if (successCount === chunkIds.length) {
        ElMessage.success('批量删除成功')
      } else if (successCount > 0) {
        ElMessage.warning(`部分分块删除成功 (${successCount}/${chunkIds.length})`)
      }
      
      return successCount
    } catch (error: any) {
      console.error('批量删除分块失败:', error)
      ElMessage.error('批量删除失败')
      return 0
    }
  }

  // 设置分页参数
  const setChunkPagination = (pageIndex: number, pageSize: number) => {
    chunkManagementState.pageIndex = pageIndex
    chunkManagementState.pageSize = pageSize
  }

  // 重置错误状态
  const clearErrors = () => {
    knowledgeBaseState.error = null
    recallTestState.error = null
    chunkManagementState.error = null
  }

  // 重置所有状态
  const resetState = () => {
    knowledgeBaseState.data = null
    knowledgeBaseState.settings = null
    knowledgeBaseState.error = null
    
    recallTestState.results = []
    recallTestState.error = null
    recallTestState.lastQuery = ''
    
    chunkManagementState.chunks = []
    chunkManagementState.error = null
    chunkManagementState.totalCount = 0
    chunkManagementState.pageIndex = 1
  }

  return {
    // 只读状态
    isLoading: readonly(isLoading),
    hasError: readonly(hasError),
    knowledgeBaseData: readonly(knowledgeBaseData),
    knowledgeBaseSettings: readonly(knowledgeBaseSettings),
    recallTestResults: readonly(recallTestResults),
    documentChunks: readonly(documentChunks),
    recallTestLoading: readonly(ref(() => recallTestState.loading)),
    chunkManagementLoading: readonly(ref(() => chunkManagementState.loading)),
    chunkTotalCount: readonly(ref(() => chunkManagementState.totalCount)),
    chunkPageIndex: readonly(ref(() => chunkManagementState.pageIndex)),
    chunkPageSize: readonly(ref(() => chunkManagementState.pageSize)),
    lastRecallQuery: readonly(ref(() => recallTestState.lastQuery)),
    
    // 方法
    fetchKnowledgeBase,
    fetchKnowledgeBaseSettings,
    updateKnowledgeBaseSettings,
    performRecallTest,
    resetRecallTest,
    fetchDocumentChunks,
    updateChunkStatus,
    updateChunkContent,
    deleteChunk,
    batchUpdateChunkStatus,
    batchDeleteChunks,
    setChunkPagination,
    clearErrors,
    resetState
  }
}