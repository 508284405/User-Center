import {
  RagComponentConfig,
  RagConfigValidationResult,
  RagConfigValidationError,
  ContentAggregatorConfig,
  QueryTransformerConfig,
  QueryRouterConfig,
  WebSearchConfig,
  KnowledgeSearchConfig
} from '@/types/chat';

/**
 * RAG配置验证器
 * 验证各个组件的参数是否在有效范围内，并提供自动修正建议
 */
export class RagConfigValidator {
  
  /**
   * 验证完整的RAG配置
   */
  static validateConfig(config: RagComponentConfig): RagConfigValidationResult {
    const errors: RagConfigValidationError[] = [];
    let correctedConfig: RagComponentConfig | undefined;
    
    // 验证各个组件配置
    const contentAggregatorErrors = this.validateContentAggregator(config.contentAggregator);
    const queryTransformerErrors = this.validateQueryTransformer(config.queryTransformer);
    const queryRouterErrors = this.validateQueryRouter(config.queryRouter);
    const webSearchErrors = this.validateWebSearch(config.webSearch);
    const knowledgeSearchErrors = this.validateKnowledgeSearch(config.knowledgeSearch);
    
    // 合并所有错误
    errors.push(
      ...contentAggregatorErrors,
      ...queryTransformerErrors,
      ...queryRouterErrors,
      ...webSearchErrors,
      ...knowledgeSearchErrors
    );
    
    // 如果有错误，生成修正后的配置
    if (errors.length > 0) {
      correctedConfig = this.generateCorrectedConfig(config, errors);
    }
    
    return {
      isValid: errors.length === 0,
      errors,
      correctedConfig
    };
  }
  
  /**
   * 验证内容聚合器配置
   */
  static validateContentAggregator(config: ContentAggregatorConfig): RagConfigValidationError[] {
    const errors: RagConfigValidationError[] = [];
    
    // 验证maxResults (1-50)
    if (config.maxResults < 1 || config.maxResults > 50) {
      errors.push({
        field: 'contentAggregator.maxResults',
        message: '最大结果数必须在1-50之间',
        suggestedValue: Math.max(1, Math.min(50, config.maxResults))
      });
    }
    
    // 验证minScore (0.0-1.0)
    if (config.minScore < 0.0 || config.minScore > 1.0) {
      errors.push({
        field: 'contentAggregator.minScore',
        message: '最小分数阈值必须在0.0-1.0之间',
        suggestedValue: Math.max(0.0, Math.min(1.0, config.minScore))
      });
    }
    
    return errors;
  }
  
  /**
   * 验证查询转换器配置
   */
  static validateQueryTransformer(config: QueryTransformerConfig): RagConfigValidationError[] {
    const errors: RagConfigValidationError[] = [];
    
    // 验证n (1-10)
    if (config.n < 1 || config.n > 10) {
      errors.push({
        field: 'queryTransformer.n',
        message: '查询扩展数量必须在1-10之间',
        suggestedValue: Math.max(1, Math.min(10, config.n))
      });
    }
    
    return errors;
  }
  
  /**
   * 验证查询路由器配置
   */
  static validateQueryRouter(config: QueryRouterConfig): RagConfigValidationError[] {
    const errors: RagConfigValidationError[] = [];
    
    // 至少启用一个搜索器
    if (!config.webSearchEnabled && !config.knowledgeSearchEnabled) {
      errors.push({
        field: 'queryRouter',
        message: '至少需要启用一个搜索器（网络搜索或知识库搜索）',
        suggestedValue: true
      });
    }
    
    return errors;
  }
  
  /**
   * 验证网络搜索配置
   */
  static validateWebSearch(config: WebSearchConfig): RagConfigValidationError[] {
    const errors: RagConfigValidationError[] = [];
    
    // 验证maxResults (1-20)
    if (config.maxResults < 1 || config.maxResults > 20) {
      errors.push({
        field: 'webSearch.maxResults',
        message: '网络搜索最大结果数必须在1-20之间',
        suggestedValue: Math.max(1, Math.min(20, config.maxResults))
      });
    }
    
    // 验证timeout (1-60)
    if (config.timeout < 1 || config.timeout > 60) {
      errors.push({
        field: 'webSearch.timeout',
        message: '搜索超时时间必须在1-60秒之间',
        suggestedValue: Math.max(1, Math.min(60, config.timeout))
      });
    }
    
    return errors;
  }
  
  /**
   * 验证知识库搜索配置
   */
  static validateKnowledgeSearch(config: KnowledgeSearchConfig): RagConfigValidationError[] {
    const errors: RagConfigValidationError[] = [];
    
    // 验证topK (1-100)
    if (config.topK < 1 || config.topK > 100) {
      errors.push({
        field: 'knowledgeSearch.topK',
        message: '返回的最相关结果数必须在1-100之间',
        suggestedValue: Math.max(1, Math.min(100, config.topK))
      });
    }
    
    // 验证scoreThreshold (0.0-1.0)
    if (config.scoreThreshold < 0.0 || config.scoreThreshold > 1.0) {
      errors.push({
        field: 'knowledgeSearch.scoreThreshold',
        message: '分数阈值必须在0.0-1.0之间',
        suggestedValue: Math.max(0.0, Math.min(1.0, config.scoreThreshold))
      });
    }
    
    return errors;
  }
  
  /**
   * 根据验证错误生成修正后的配置
   */
  static generateCorrectedConfig(
    originalConfig: RagComponentConfig,
    errors: RagConfigValidationError[]
  ): RagComponentConfig {
    const corrected = JSON.parse(JSON.stringify(originalConfig)); // 深拷贝
    
    errors.forEach(error => {
      if (error.suggestedValue !== undefined) {
        const fieldPath = error.field.split('.');
        let current: any = corrected;
        
        // 导航到目标字段的父对象
        for (let i = 0; i < fieldPath.length - 1; i++) {
          current = current[fieldPath[i]];
        }
        
        // 设置修正值
        const finalField = fieldPath[fieldPath.length - 1];
        if (finalField === 'queryRouter' && error.suggestedValue === true) {
          // 特殊处理queryRouter错误，启用知识库搜索
          current.knowledgeSearchEnabled = true;
        } else {
          current[finalField] = error.suggestedValue;
        }
      }
    });
    
    return corrected;
  }
  
  /**
   * 获取默认RAG配置
   */
  static getDefaultConfig(): RagComponentConfig {
    return {
      contentAggregator: {
        maxResults: 10,
        minScore: 0.7
      },
      queryTransformer: {
        n: 3
      },
      queryRouter: {
        webSearchEnabled: true,
        knowledgeSearchEnabled: true
      },
      webSearch: {
        maxResults: 5,
        timeout: 10
      },
      knowledgeSearch: {
        topK: 20,
        scoreThreshold: 0.6
      }
    };
  }
  
  /**
   * 检查配置是否为默认配置
   */
  static isDefaultConfig(config: RagComponentConfig): boolean {
    const defaultConfig = this.getDefaultConfig();
    return JSON.stringify(config) === JSON.stringify(defaultConfig);
  }
  
  /**
   * 验证单个字段值
   */
  static validateField(fieldPath: string, value: any): RagConfigValidationError | null {
    switch (fieldPath) {
      case 'contentAggregator.maxResults':
        if (value < 1 || value > 50) {
          return {
            field: fieldPath,
            message: '最大结果数必须在1-50之间',
            suggestedValue: Math.max(1, Math.min(50, value))
          };
        }
        break;
        
      case 'contentAggregator.minScore':
        if (value < 0.0 || value > 1.0) {
          return {
            field: fieldPath,
            message: '最小分数阈值必须在0.0-1.0之间',
            suggestedValue: Math.max(0.0, Math.min(1.0, value))
          };
        }
        break;
        
      case 'queryTransformer.n':
        if (value < 1 || value > 10) {
          return {
            field: fieldPath,
            message: '查询扩展数量必须在1-10之间',
            suggestedValue: Math.max(1, Math.min(10, value))
          };
        }
        break;
        
      case 'webSearch.maxResults':
        if (value < 1 || value > 20) {
          return {
            field: fieldPath,
            message: '网络搜索最大结果数必须在1-20之间',
            suggestedValue: Math.max(1, Math.min(20, value))
          };
        }
        break;
        
      case 'webSearch.timeout':
        if (value < 1 || value > 60) {
          return {
            field: fieldPath,
            message: '搜索超时时间必须在1-60秒之间',
            suggestedValue: Math.max(1, Math.min(60, value))
          };
        }
        break;
        
      case 'knowledgeSearch.topK':
        if (value < 1 || value > 100) {
          return {
            field: fieldPath,
            message: '返回的最相关结果数必须在1-100之间',
            suggestedValue: Math.max(1, Math.min(100, value))
          };
        }
        break;
        
      case 'knowledgeSearch.scoreThreshold':
        if (value < 0.0 || value > 1.0) {
          return {
            field: fieldPath,
            message: '分数阈值必须在0.0-1.0之间',
            suggestedValue: Math.max(0.0, Math.min(1.0, value))
          };
        }
        break;
    }
    
    return null;
  }
  
  /**
   * 获取字段的有效范围信息
   */
  static getFieldRange(fieldPath: string): { min: number; max: number; step?: number } | null {
    switch (fieldPath) {
      case 'contentAggregator.maxResults':
        return { min: 1, max: 50, step: 1 };
      case 'contentAggregator.minScore':
        return { min: 0.0, max: 1.0, step: 0.1 };
      case 'queryTransformer.n':
        return { min: 1, max: 10, step: 1 };
      case 'webSearch.maxResults':
        return { min: 1, max: 20, step: 1 };
      case 'webSearch.timeout':
        return { min: 1, max: 60, step: 1 };
      case 'knowledgeSearch.topK':
        return { min: 1, max: 100, step: 1 };
      case 'knowledgeSearch.scoreThreshold':
        return { min: 0.0, max: 1.0, step: 0.1 };
      default:
        return null;
    }
  }
}

/**
 * 导出便捷函数
 */
export const validateRagConfig = (config: RagComponentConfig) => 
  RagConfigValidator.validateConfig(config);

export const getDefaultRagConfig = () => 
  RagConfigValidator.getDefaultConfig();

export const validateRagField = (fieldPath: string, value: any) => 
  RagConfigValidator.validateField(fieldPath, value);

export const getRagFieldRange = (fieldPath: string) => 
  RagConfigValidator.getFieldRange(fieldPath);