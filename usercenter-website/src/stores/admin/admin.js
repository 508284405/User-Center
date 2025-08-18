// SmartCS 管理控制台状态管理
import { ref, reactive } from 'vue'
import { defineStore } from 'pinia'
import { intentApi, catalogApi, snapshotApi, classificationApi } from '@/api/smartcs/intent'

export const useSmartCSAdminStore = defineStore('smartcsAdmin', () => {
  // 状态数据
  const dashboardStats = ref({
    intentCount: 0,
    activeSnapshot: '无',
    knowledgeBaseCount: 0,
    todayClassifications: 0
  })

  const intentList = ref([])
  const intentPagination = reactive({
    current: 1,
    pageSize: 20,
    total: 0
  })

  const snapshotList = ref([])
  const snapshotPagination = reactive({
    current: 1,
    pageSize: 20,
    total: 0
  })

  const currentIntent = ref(null)
  const currentSnapshot = ref(null)
  const catalogList = ref([])
  
  const loading = reactive({
    dashboard: false,
    intentList: false,
    snapshotList: false,
    intentDetail: false,
    snapshotDetail: false,
    classification: false
  })

  // Actions
  const loadDashboardStats = async () => {
    // 仪表盘已移除：保留空实现以保持兼容
    loading.dashboard = true
    try {
      dashboardStats.value = {
        intentCount: 0,
        activeSnapshot: '无',
        knowledgeBaseCount: 0,
        todayClassifications: 0
      }
    } finally {
      loading.dashboard = false
    }
  }

  const loadIntentList = async (params = {}) => {
    loading.intentList = true
    try {
      console.log('发送意图列表查询请求，参数:', {
        pageNum: intentPagination.current,
        pageSize: intentPagination.pageSize,
        ...params
      })
      
      const response = await intentApi.getIntentPage({
        pageNum: intentPagination.current,
        pageSize: intentPagination.pageSize,
        ...params
      })

      console.log('意图列表API响应:', response)

      if (response && response.success) {
        if (response.data && Array.isArray(response.data)) {
          intentList.value = response.data
          intentPagination.total = response.totalCount || 0
          console.log('设置意图列表数据成功:', {
            list: intentList.value,
            total: intentPagination.total
          })
        } else {
          console.warn('API响应数据格式异常 - data字段不是数组:', response.data)
          intentList.value = []
          intentPagination.total = 0
        }
      } else {
        console.warn('API响应失败:', response)
        intentList.value = []
        intentPagination.total = 0
      }
    } catch (error) {
      console.error('加载意图列表失败:', error)
      intentList.value = []
      intentPagination.total = 0
    } finally {
      loading.intentList = false
    }
  }

  const loadSnapshotList = async (params = {}) => {
    loading.snapshotList = true
    try {
      console.log('发送快照列表查询请求，参数:', {
        pageNum: snapshotPagination.current,
        pageSize: snapshotPagination.pageSize,
        ...params
      })
      
      const response = await snapshotApi.getSnapshotPage({
        pageNum: snapshotPagination.current,
        pageSize: snapshotPagination.pageSize,
        ...params
      })

      console.log('快照列表API响应:', response)

      if (response && response.success) {
        if (response.data && Array.isArray(response.data)) {
          snapshotList.value = response.data
          snapshotPagination.total = response.totalCount || 0
          console.log('设置快照列表数据成功:', {
            list: snapshotList.value,
            total: snapshotPagination.total
          })
        } else {
          console.warn('API响应数据格式异常 - data字段不是数组:', response.data)
          snapshotList.value = []
          snapshotPagination.total = 0
        }
      } else {
        console.warn('API响应失败:', response)
        snapshotList.value = []
        snapshotPagination.total = 0
      }
    } catch (error) {
      console.error('加载快照列表失败:', error)
      snapshotList.value = []
      snapshotPagination.total = 0
    } finally {
      loading.snapshotList = false
    }
  }

  const loadIntentDetail = async (intentId) => {
    loading.intentDetail = true
    try {
      const response = await intentApi.getIntentDetail(intentId)
      if (response.success && response.data) {
        currentIntent.value = response.data
        return response.data
      }
    } catch (error) {
      console.error('加载意图详情失败:', error)
      currentIntent.value = null
    } finally {
      loading.intentDetail = false
    }
    return null
  }

  const loadSnapshotDetail = async (snapshotId) => {
    loading.snapshotDetail = true
    try {
      const response = await snapshotApi.getSnapshotDetail(snapshotId)
      if (response.success && response.data) {
        currentSnapshot.value = response.data
        return response.data
      }
    } catch (error) {
      console.error('加载快照详情失败:', error)
      currentSnapshot.value = null
    } finally {
      loading.snapshotDetail = false
    }
    return null
  }

  const loadCatalogList = async () => {
    try {
      const response = await catalogApi.getCatalogList()
      if (response.success && response.data) {
        catalogList.value = response.data
      }
    } catch (error) {
      console.error('加载分类目录失败:', error)
      catalogList.value = []
    }
  }

  const createIntent = async (intentData) => {
    try {
      const response = await intentApi.createIntent(intentData)
      if (response.success) {
        await loadIntentList()
        await loadDashboardStats()
        return response
      }
      throw new Error(response.errMessage || '创建失败')
    } catch (error) {
      console.error('创建意图失败:', error)
      throw error
    }
  }

  const updateIntent = async (intentId, intentData) => {
    try {
      const response = await intentApi.updateIntent(intentId, intentData)
      if (response.success) {
        await loadIntentList()
        await loadDashboardStats()
        return response
      }
      throw new Error(response.errMessage || '更新失败')
    } catch (error) {
      console.error('更新意图失败:', error)
      throw error
    }
  }

  const deleteIntent = async (intentId) => {
    try {
      const response = await intentApi.deleteIntent(intentId)
      if (response.success) {
        await loadIntentList()
        await loadDashboardStats()
        return response
      }
      throw new Error(response.errMessage || '删除失败')
    } catch (error) {
      console.error('删除意图失败:', error)
      throw error
    }
  }

  const createSnapshot = async (snapshotData) => {
    try {
      const response = await snapshotApi.createSnapshot(snapshotData)
      if (response.success) {
        await loadSnapshotList()
        await loadDashboardStats()
        return response
      }
      throw new Error(response.errMessage || '创建失败')
    } catch (error) {
      console.error('创建快照失败:', error)
      throw error
    }
  }

  const publishSnapshot = async (snapshotId) => {
    try {
      const response = await snapshotApi.publishSnapshot(snapshotId)
      if (response.success) {
        await loadSnapshotList()
        await loadDashboardStats()
        return response
      }
      throw new Error(response.errMessage || '发布失败')
    } catch (error) {
      console.error('发布快照失败:', error)
      throw error
    }
  }

  const testClassification = async (testData) => {
    loading.classification = true
    try {
      const response = await classificationApi.testClassification(testData)
      return response
    } catch (error) {
      console.error('分类测试失败:', error)
      throw error
    } finally {
      loading.classification = false
    }
  }

  // 重置状态
  const resetStore = () => {
    dashboardStats.value = {
      intentCount: 0,
      activeSnapshot: '无',
      knowledgeBaseCount: 0,
      todayClassifications: 0
    }
    intentList.value = []
    snapshotList.value = []
    currentIntent.value = null
    currentSnapshot.value = null
    catalogList.value = []
    intentPagination.current = 1
    intentPagination.total = 0
    snapshotPagination.current = 1
    snapshotPagination.total = 0
  }

  return {
    // State
    dashboardStats,
    intentList,
    intentPagination,
    snapshotList,
    snapshotPagination,
    currentIntent,
    currentSnapshot,
    catalogList,
    loading,

    // Actions
    loadDashboardStats,
    loadIntentList,
    loadSnapshotList,
    loadIntentDetail,
    loadSnapshotDetail,
    loadCatalogList,
    createIntent,
    updateIntent,
    deleteIntent,
    createSnapshot,
    publishSnapshot,
    testClassification,
    resetStore
  }
})
