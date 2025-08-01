import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  getApp, 
  updateApp, 
  optimizePrompt, 
  getFunctionConfig, 
  updateFunctionConfig,
  type AiAppDTO, 
  type AiAppUpdateRequest,
  type AiAppPromptOptimizeRequest,
  type AiAppPromptOptimizeResponse,
  type AiAppFunctionConfigRequest,
  type AiAppFunctionConfigResponse
} from '@/api/smartcs/app'
import { modelApi, type Model } from '@/api/smartcs/model'

interface AppConfigState {
  appData: AiAppDTO | null
  loading: boolean
  saving: boolean
  models: Model[]
  modelsLoading: boolean
  functionConfig: AiAppFunctionConfigResponse | null
  functionConfigLoading: boolean
}

export function useAppConfig(appId?: number) {
  // 状态管理
  const state = reactive<AppConfigState>({
    appData: null,
    loading: false,
    saving: false,
    models: [],
    modelsLoading: false,
    functionConfig: null,
    functionConfigLoading: false
  })

  // 计算属性
  const isInitialized = computed(() => Boolean(state.appData))
  const isEditable = computed(() => state.appData?.editable ?? true)
  const canSave = computed(() => !state.saving && isEditable.value)

  // ==================== 应用基础信息管理 ====================

  /**
   * 加载应用详情
   */
  const loadAppDetail = async (id?: number) => {
    const targetId = id || appId
    if (!targetId) {
      throw new Error('应用ID不能为空')
    }

    try {
      state.loading = true
      const response = await getApp(targetId)
      
      if (response.success && response.data) {
        state.appData = response.data
        return response.data
      } else {
        throw new Error(response.errMessage || '获取应用详情失败')
      }
    } catch (error) {
      console.error('加载应用详情失败:', error)
      throw error
    } finally {
      state.loading = false
    }
  }

  /**
   * 更新应用配置
   */
  const updateAppConfig = async (config: Partial<AiAppUpdateRequest>) => {
    if (!state.appData) {
      throw new Error('应用数据未加载')
    }

    try {
      state.saving = true
      
      const updateData: AiAppUpdateRequest = {
        id: state.appData.id!,
        name: state.appData.name,
        description: state.appData.description,
        config: state.appData.config,
        icon: state.appData.icon,
        tags: state.appData.tags,
        ...config
      }

      const response = await updateApp(updateData)
      
      if (response.success) {
        // 更新本地数据
        Object.assign(state.appData, config)
        ElMessage.success('应用配置更新成功')
        return true
      } else {
        throw new Error(response.errMessage || '更新应用配置失败')
      }
    } catch (error) {
      console.error('更新应用配置失败:', error)
      ElMessage.error(error.message || '更新应用配置失败')
      throw error
    } finally {
      state.saving = false
    }
  }

  /**
   * 更新Prompt配置
   */
  const updatePromptConfig = async (promptContent: string, variables?: any[]) => {
    if (!state.appData) {
      throw new Error('应用数据未加载')
    }

    const newConfig = {
      ...state.appData.config,
      prompt_template: promptContent,
      variables: variables || []
    }

    return updateAppConfig({ config: newConfig })
  }

  // ==================== 模型管理 ====================

  /**
   * 加载可用模型列表
   */
  const loadAvailableModels = async () => {
    try {
      state.modelsLoading = true
      const response = await modelApi.getAvailableModels()
      
      if (response.success && response.data) {
        // 过滤出LLM类型的激活模型
        state.models = response.data.filter(model => 
          model.modelType.includes('LLM') && model.status === 'ACTIVE'
        )
        return state.models
      } else {
        throw new Error(response.errMessage || '获取模型列表失败')
      }
    } catch (error) {
      console.error('加载模型列表失败:', error)
      ElMessage.error('加载模型列表失败')
      throw error
    } finally {
      state.modelsLoading = false
    }
  }

  /**
   * 根据ID获取模型信息
   */
  const getModelById = (modelId?: number) => {
    if (!modelId) return null
    return state.models.find(model => model.id === modelId) || null
  }

  // ==================== Prompt优化 ====================

  /**
   * 优化Prompt
   */
  const optimizeAppPrompt = async (
    originalPrompt: string, 
    optimizeGoal?: string, 
    modelId?: number
  ): Promise<AiAppPromptOptimizeResponse> => {
    if (!state.appData) {
      throw new Error('应用数据未加载')
    }

    try {
      const request: AiAppPromptOptimizeRequest = {
        appId: state.appData.id!,
        originalPrompt,
        optimizeGoal,
        modelId
      }

      const response = await optimizePrompt(request)
      
      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.errMessage || 'Prompt优化失败')
      }
    } catch (error) {
      console.error('Prompt优化失败:', error)
      throw error
    }
  }

  // ==================== 功能配置管理 ====================

  /**
   * 加载功能配置
   */
  const loadFunctionConfig = async (id?: number) => {
    const targetId = id || appId || state.appData?.id
    if (!targetId) {
      throw new Error('应用ID不能为空')
    }

    try {
      state.functionConfigLoading = true
      const response = await getFunctionConfig(targetId)
      
      if (response.success && response.data) {
        state.functionConfig = response.data
        return response.data
      } else {
        // 如果获取失败，返回默认配置
        const defaultConfig: AiAppFunctionConfigResponse = {
          appId: targetId,
          functionConfig: {},
          conversationOpenerEnabled: false,
          nextQuestionSuggestionEnabled: false,
          textToSpeechEnabled: false,
          speechToTextEnabled: false,
          citationEnabled: true,
          contentModerationEnabled: false,
          standardReplyEnabled: false
        }
        state.functionConfig = defaultConfig
        return defaultConfig
      }
    } catch (error) {
      console.error('加载功能配置失败:', error)
      // 返回默认配置而不是抛出错误
      const defaultConfig: AiAppFunctionConfigResponse = {
        appId: targetId,
        functionConfig: {},
        conversationOpenerEnabled: false,
        nextQuestionSuggestionEnabled: false,
        textToSpeechEnabled: false,
        speechToTextEnabled: false,
        citationEnabled: true,
        contentModerationEnabled: false,
        standardReplyEnabled: false
      }
      state.functionConfig = defaultConfig
      return defaultConfig
    } finally {
      state.functionConfigLoading = false
    }
  }

  /**
   * 更新功能配置
   */
  const updateAppFunctionConfig = async (config: Partial<AiAppFunctionConfigRequest>) => {
    const targetId = appId || state.appData?.id
    if (!targetId) {
      throw new Error('应用ID不能为空')
    }

    try {
      const request: AiAppFunctionConfigRequest = {
        appId: targetId,
        ...config
      }

      const response = await updateFunctionConfig(targetId, request)
      
      if (response.success) {
        // 重新加载功能配置
        await loadFunctionConfig(targetId)
        ElMessage.success('功能配置更新成功')
        return true
      } else {
        throw new Error(response.errMessage || '更新功能配置失败')
      }
    } catch (error) {
      console.error('更新功能配置失败:', error)
      ElMessage.error(error.message || '更新功能配置失败')
      throw error
    }
  }

  // ==================== 工具方法 ====================

  /**
   * 初始化配置（加载应用详情、模型列表、功能配置）
   */
  const initializeConfig = async (id?: number) => {
    const targetId = id || appId
    if (!targetId) {
      throw new Error('应用ID不能为空')
    }

    try {
      // 并行加载应用详情和模型列表
      const [appData] = await Promise.all([
        loadAppDetail(targetId),
        loadAvailableModels()
      ])

      // 加载功能配置
      await loadFunctionConfig(targetId)

      return appData
    } catch (error) {
      console.error('初始化配置失败:', error)
      throw error
    }
  }

  /**
   * 重置状态
   */
  const reset = () => {
    state.appData = null
    state.loading = false
    state.saving = false
    state.models = []
    state.modelsLoading = false
    state.functionConfig = null
    state.functionConfigLoading = false
  }

  /**
   * 获取当前prompt内容
   */
  const getCurrentPrompt = () => {
    return state.appData?.config?.prompt_template || ''
  }

  /**
   * 获取当前变量配置
   */
  const getCurrentVariables = () => {
    return state.appData?.config?.variables || []
  }

  return {
    // 状态
    state,
    
    // 计算属性
    isInitialized,
    isEditable,
    canSave,
    
    // 应用基础信息
    loadAppDetail,
    updateAppConfig,
    updatePromptConfig,
    getCurrentPrompt,
    getCurrentVariables,
    
    // 模型管理
    loadAvailableModels,
    getModelById,
    
    // Prompt优化
    optimizeAppPrompt,
    
    // 功能配置
    loadFunctionConfig,
    updateAppFunctionConfig,
    
    // 工具方法
    initializeConfig,
    reset
  }
}