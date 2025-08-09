import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import {
  RagComponentConfig,
  RagConfigPreset,
  RagConfigPresetDefinition,
  RagDebugInfo,
  RagConfigValidationResult
} from '@/types/chat';
import { RagConfigValidator, getDefaultRagConfig } from '@/utils/ragConfigValidator';

/**
 * RAG配置状态管理Store
 * 负责管理RAG组件的配置状态，包括预设配置、用户自定义配置等
 */
export const useRagConfigStore = defineStore('ragConfig', () => {
  // 状态
  const currentConfig = ref<RagComponentConfig>(getDefaultRagConfig());
  const selectedPreset = ref<RagConfigPreset>(RagConfigPreset.BALANCED);
  const isCustomConfig = ref<boolean>(false);
  const debugInfo = ref<RagDebugInfo | null>(null);
  const validationResult = ref<RagConfigValidationResult | null>(null);
  
  // 预设配置定义
  const presetDefinitions = ref<RagConfigPresetDefinition[]>([
    {
      id: RagConfigPreset.HIGH_PRECISION,
      name: '高精度模式',
      description: '严格的相关性阈值，返回少量高质量结果，适合对精确性要求高的场景',
      config: {
        contentAggregator: {
          maxResults: 5,
          minScore: 0.85,
          scoringModelId: undefined
        },
        queryTransformer: {
          n: 2,
          promptTemplate: undefined,
          modelId: undefined
        },
        queryRouter: {
          webSearchEnabled: false,
          knowledgeSearchEnabled: true,
          promptTemplate: undefined,
          modelId: undefined
        },
        webSearch: {
          maxResults: 3,
          timeout: 15
        },
        knowledgeSearch: {
          topK: 10,
          scoreThreshold: 0.8
        },
        contentInjector: {
          promptTemplate: undefined,
          metadataKeysToInclude: undefined
        },
        embeddingStore: {
          maxResults: 3,
          minScore: 0.9,
          metadataKeysToInclude: undefined
        },
        sqlQuery: {
          maxResults: 50,
          timeout: 15,
          securitySettings: {
            allowedTables: undefined,
            forbiddenKeywords: undefined,
            enableQueryValidation: true,
            selectOnly: true
          }
        },
        memory: {
          maxMessages: 50,
          memoryType: 'sliding_window'
        }
      }
    },
    {
      id: RagConfigPreset.HIGH_RECALL,
      name: '高召回模式',
      description: '宽松的相关性阈值，返回更多结果，适合需要全面信息的场景',
      config: {
        contentAggregator: {
          maxResults: 20,
          minScore: 0.5,
          scoringModelId: undefined
        },
        queryTransformer: {
          n: 5,
          promptTemplate: undefined,
          modelId: undefined
        },
        queryRouter: {
          webSearchEnabled: true,
          knowledgeSearchEnabled: true,
          promptTemplate: undefined,
          modelId: undefined
        },
        webSearch: {
          maxResults: 10,
          timeout: 20
        },
        knowledgeSearch: {
          topK: 50,
          scoreThreshold: 0.4
        },
        contentInjector: {
          promptTemplate: undefined,
          metadataKeysToInclude: undefined
        },
        embeddingStore: {
          maxResults: 25,
          minScore: 0.3,
          metadataKeysToInclude: undefined
        },
        sqlQuery: {
          maxResults: 200,
          timeout: 45,
          securitySettings: {
            allowedTables: undefined,
            forbiddenKeywords: undefined,
            enableQueryValidation: true,
            selectOnly: true
          }
        },
        memory: {
          maxMessages: 200,
          memoryType: 'summary'
        }
      }
    },
    {
      id: RagConfigPreset.BALANCED,
      name: '平衡模式',
      description: '精度和召回率的平衡配置，适合大多数使用场景',
      config: getDefaultRagConfig()
    },
    {
      id: RagConfigPreset.DEBUG,
      name: '调试模式',
      description: '详细的日志和中间结果，适合开发和调试阶段',
      config: {
        contentAggregator: {
          maxResults: 15,
          minScore: 0.6,
          scoringModelId: undefined
        },
        queryTransformer: {
          n: 4,
          promptTemplate: undefined,
          modelId: undefined
        },
        queryRouter: {
          webSearchEnabled: true,
          knowledgeSearchEnabled: true,
          promptTemplate: undefined,
          modelId: undefined
        },
        webSearch: {
          maxResults: 8,
          timeout: 30
        },
        knowledgeSearch: {
          topK: 30,
          scoreThreshold: 0.5
        },
        contentInjector: {
          promptTemplate: undefined,
          metadataKeysToInclude: undefined
        },
        embeddingStore: {
          maxResults: 20,
          minScore: 0.4,
          metadataKeysToInclude: undefined
        },
        sqlQuery: {
          maxResults: 150,
          timeout: 40,
          securitySettings: {
            allowedTables: undefined,
            forbiddenKeywords: undefined,
            enableQueryValidation: true,
            selectOnly: true
          }
        },
        memory: {
          maxMessages: 150,
          memoryType: 'sliding_window'
        }
      }
    }
  ]);
  
  // 计算属性
  const currentPresetDefinition = computed(() => 
    presetDefinitions.value.find(preset => preset.id === selectedPreset.value)
  );
  
  const isValidConfig = computed(() => 
    validationResult.value?.isValid ?? true
  );
  
  const configErrors = computed(() => 
    validationResult.value?.errors ?? []
  );
  
  const hasDebugInfo = computed(() => 
    debugInfo.value !== null
  );
  
  // 本地存储键名
  const STORAGE_KEY_CONFIG = 'rag-config';
  const STORAGE_KEY_PRESET = 'rag-config-preset';
  const STORAGE_KEY_CUSTOM = 'rag-config-custom';
  
  // 动作
  
  /**
   * 应用预设配置
   */
  const applyPreset = (preset: RagConfigPreset) => {
    const presetDef = presetDefinitions.value.find(p => p.id === preset);
    if (presetDef) {
      currentConfig.value = JSON.parse(JSON.stringify(presetDef.config));
      selectedPreset.value = preset;
      isCustomConfig.value = false;
      
      // 验证配置
      validateCurrentConfig();
      
      // 保存到本地存储
      saveConfigToStorage();
    }
  };
  
  /**
   * 更新配置
   */
  const updateConfig = (newConfig: Partial<RagComponentConfig>) => {
    // 深度合并配置
    currentConfig.value = {
      ...currentConfig.value,
      ...newConfig,
      contentAggregator: {
        ...currentConfig.value.contentAggregator,
        ...newConfig.contentAggregator
      },
      queryTransformer: {
        ...currentConfig.value.queryTransformer,
        ...newConfig.queryTransformer
      },
      queryRouter: {
        ...currentConfig.value.queryRouter,
        ...newConfig.queryRouter
      },
      webSearch: {
        ...currentConfig.value.webSearch,
        ...newConfig.webSearch
      },
      knowledgeSearch: {
        ...currentConfig.value.knowledgeSearch,
        ...newConfig.knowledgeSearch
      },
      contentInjector: {
        ...currentConfig.value.contentInjector,
        ...newConfig.contentInjector
      },
      embeddingStore: {
        ...currentConfig.value.embeddingStore,
        ...newConfig.embeddingStore
      },
      sqlQuery: {
        ...currentConfig.value.sqlQuery,
        ...newConfig.sqlQuery,
        securitySettings: newConfig.sqlQuery?.securitySettings ? {
          ...currentConfig.value.sqlQuery?.securitySettings,
          ...newConfig.sqlQuery.securitySettings
        } : currentConfig.value.sqlQuery?.securitySettings
      },
      memory: {
        ...currentConfig.value.memory,
        ...newConfig.memory
      }
    };
    
    // 标记为自定义配置
    isCustomConfig.value = true;
    selectedPreset.value = RagConfigPreset.BALANCED; // 重置预设选择
    
    // 验证配置
    validateCurrentConfig();
    
    // 保存到本地存储
    saveConfigToStorage();
  };
  
  /**
   * 重置为默认配置
   */
  const resetToDefault = () => {
    applyPreset(RagConfigPreset.BALANCED);
  };
  
  /**
   * 验证当前配置
   */
  const validateCurrentConfig = () => {
    validationResult.value = RagConfigValidator.validateConfig(currentConfig.value);
  };
  
  /**
   * 应用修正后的配置
   */
  const applyCorrectedConfig = () => {
    if (validationResult.value?.correctedConfig) {
      currentConfig.value = validationResult.value.correctedConfig;
      isCustomConfig.value = true;
      validateCurrentConfig();
      saveConfigToStorage();
    }
  };
  
  /**
   * 设置调试信息
   */
  const setDebugInfo = (info: RagDebugInfo) => {
    debugInfo.value = info;
  };
  
  /**
   * 清除调试信息
   */
  const clearDebugInfo = () => {
    debugInfo.value = null;
  };
  
  /**
   * 保存配置到本地存储
   */
  const saveConfigToStorage = () => {
    try {
      localStorage.setItem(STORAGE_KEY_CONFIG, JSON.stringify(currentConfig.value));
      localStorage.setItem(STORAGE_KEY_PRESET, selectedPreset.value);
      localStorage.setItem(STORAGE_KEY_CUSTOM, JSON.stringify(isCustomConfig.value));
    } catch (error) {
      console.warn('保存RAG配置到本地存储失败:', error);
    }
  };
  
  /**
   * 从本地存储加载配置
   */
  const loadConfigFromStorage = () => {
    try {
      const savedConfig = localStorage.getItem(STORAGE_KEY_CONFIG);
      const savedPreset = localStorage.getItem(STORAGE_KEY_PRESET);
      const savedCustom = localStorage.getItem(STORAGE_KEY_CUSTOM);
      
      if (savedConfig) {
        const parsedConfig = JSON.parse(savedConfig);
        // 验证配置结构的完整性
        const validation = RagConfigValidator.validateConfig(parsedConfig);
        if (validation.isValid || validation.correctedConfig) {
          currentConfig.value = validation.correctedConfig || parsedConfig;
          validationResult.value = validation;
        }
      }
      
      if (savedPreset && Object.values(RagConfigPreset).includes(savedPreset as RagConfigPreset)) {
        selectedPreset.value = savedPreset as RagConfigPreset;
      }
      
      if (savedCustom) {
        isCustomConfig.value = JSON.parse(savedCustom);
      }
      
      // 如果没有保存的配置，应用默认配置
      if (!savedConfig) {
        resetToDefault();
      }
      
    } catch (error) {
      console.warn('从本地存储加载RAG配置失败，使用默认配置:', error);
      resetToDefault();
    }
  };
  
  /**
   * 获取当前配置的副本
   */
  const getConfigCopy = (): RagComponentConfig => {
    return JSON.parse(JSON.stringify(currentConfig.value));
  };
  
  /**
   * 检查配置是否与指定预设匹配
   */
  const isPresetConfig = (preset: RagConfigPreset): boolean => {
    const presetDef = presetDefinitions.value.find(p => p.id === preset);
    if (!presetDef) return false;
    
    return JSON.stringify(currentConfig.value) === JSON.stringify(presetDef.config);
  };
  
  /**
   * 导出配置为JSON
   */
  const exportConfig = (): string => {
    return JSON.stringify({
      config: currentConfig.value,
      preset: selectedPreset.value,
      isCustom: isCustomConfig.value,
      timestamp: Date.now()
    }, null, 2);
  };
  
  /**
   * 从JSON导入配置
   */
  const importConfig = (configJson: string): boolean => {
    try {
      const imported = JSON.parse(configJson);
      if (imported.config) {
        const validation = RagConfigValidator.validateConfig(imported.config);
        if (validation.isValid || validation.correctedConfig) {
          currentConfig.value = validation.correctedConfig || imported.config;
          selectedPreset.value = imported.preset || RagConfigPreset.BALANCED;
          isCustomConfig.value = imported.isCustom || true;
          validationResult.value = validation;
          saveConfigToStorage();
          return true;
        }
      }
      return false;
    } catch (error) {
      console.error('导入RAG配置失败:', error);
      return false;
    }
  };
  
  // 初始化时加载配置
  loadConfigFromStorage();
  
  return {
    // 状态
    currentConfig,
    selectedPreset,
    isCustomConfig,
    debugInfo,
    validationResult,
    presetDefinitions,
    
    // 计算属性
    currentPresetDefinition,
    isValidConfig,
    configErrors,
    hasDebugInfo,
    
    // 动作
    applyPreset,
    updateConfig,
    resetToDefault,
    validateCurrentConfig,
    applyCorrectedConfig,
    setDebugInfo,
    clearDebugInfo,
    saveConfigToStorage,
    loadConfigFromStorage,
    getConfigCopy,
    isPresetConfig,
    exportConfig,
    importConfig
  };
});