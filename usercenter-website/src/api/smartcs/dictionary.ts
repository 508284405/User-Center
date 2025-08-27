import axios from '../config';

// 字典类型枚举
export enum DictionaryType {
  STOP_WORDS = 'STOP_WORDS',
  SENSITIVE_WORDS = 'SENSITIVE_WORDS',
  PHONETIC_MAPPING = 'PHONETIC_MAPPING',
  PHONETIC_CORRECTION = 'PHONETIC_CORRECTION',
  PREFIX_COMPLETION = 'PREFIX_COMPLETION',
  SYNONYM_MAPPING = 'SYNONYM_MAPPING',
  SEMANTIC_KEYWORDS = 'SEMANTIC_KEYWORDS',
  SEMANTIC_CATEGORIES = 'SEMANTIC_CATEGORIES',
  SEMANTIC_ALIGNMENT = 'SEMANTIC_ALIGNMENT',
  INTENT_PATTERNS = 'INTENT_PATTERNS',
  INTENT_KEYWORDS = 'INTENT_KEYWORDS',
  REWRITE_RULES = 'REWRITE_RULES',
  EXPANSION_STRATEGIES = 'EXPANSION_STRATEGIES',
  QUERY_TEMPLATES = 'QUERY_TEMPLATES',
  DOMAIN_TERMS = 'DOMAIN_TERMS'
}

// 字典项数据结构
export interface DictionaryItem {
  id?: number;
  type: DictionaryType;
  key: string;
  value: string;
  description?: string;
  weight?: number;
  enabled: boolean;
  metadata?: Record<string, any>;
  tenant?: string;
  channel?: string;
  domain?: string;
  locale?: string;
  version?: number;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

// 字典类型信息
export interface DictionaryTypeInfo {
  type: DictionaryType;
  label: string;
  description: string;
  stage?: string;
  keyPattern?: string;
  valuePattern?: string;
  required: boolean;
  maxEntries?: number;
}

// 分页查询参数
export interface DictionaryPageQry {
  type?: DictionaryType;
  key?: string;
  tenant?: string;
  channel?: string;
  domain?: string;
  locale?: string;
  enabled?: boolean;
  pageNo: number;
  pageSize: number;
}

// 批量操作命令
export interface DictionaryUpsertCmd {
  items: DictionaryItem[];
  tenant?: string;
  channel?: string;
  domain?: string;
  locale?: string;
}

// 验证命令
export interface DictionaryValidateCmd {
  items: DictionaryItem[];
}

// 预览命令
export interface DictionaryPreviewCmd {
  stage: string;
  query: string;
  tenant?: string;
  channel?: string;
  domain?: string;
  locale?: string;
}

// 预览结果
export interface DictionaryPreviewResult {
  originalQuery: string;
  processedQuery: string;
  appliedRules: string[];
  executionTime: number;
  stage: string;
}

// 分页结果
export interface PageResult<T> {
  data: T[];
  totalCount: number;
  pageNo: number;
  pageSize: number;
}

// API 接口
export const dictionaryApi = {
  // 获取字典类型列表
  getTypes(): Promise<{ data: DictionaryTypeInfo[] }> {
    return axios.get('/smartcs/api/dictionaries/types');
  },

  // 分页查询字典项
  queryDictionaries(query: DictionaryPageQry): Promise<{ data: PageResult<DictionaryItem> }> {
    return axios.get('/smartcs/api/dictionaries', { params: query });
  },

  // 验证字典数据
  validateDictionaries(cmd: DictionaryValidateCmd): Promise<{ data: { valid: boolean; errors: string[] } }> {
    return axios.post('/smartcs/api/dictionaries/validate', cmd);
  },

  // 批量创建/更新字典项
  upsertDictionaries(cmd: DictionaryUpsertCmd): Promise<{ data: { successCount: number; failureCount: number } }> {
    return axios.post('/smartcs/api/dictionaries/upsert', cmd);
  },

  // 删除字典项
  deleteDictionary(id: number): Promise<{ data: boolean }> {
    return axios.delete(`/smartcs/api/dictionaries/${id}`);
  },

  // 刷新缓存
  refreshCache(): Promise<{ data: boolean }> {
    return axios.post('/smartcs/api/dictionaries/refresh');
  },

  // 预览阶段效果
  previewStage(cmd: DictionaryPreviewCmd): Promise<{ data: DictionaryPreviewResult }> {
    return axios.post('/smartcs/api/dictionaries/preview/stage', cmd);
  }
};