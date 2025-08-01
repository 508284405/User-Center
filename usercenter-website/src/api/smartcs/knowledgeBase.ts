import request from '../config';

// 知识库创建命令对象
interface KnowledgeBaseCreateCmd {
  name: string;
  code: string;
  description?: string;
  visibility: string;
}

// 知识库更新命令对象
interface KnowledgeBaseUpdateCmd {
  id: number;
  name?: string;
  description?: string;
  visibility?: string;
}

// 知识库查询对象
interface KnowledgeBaseQuery {
  pageSize?: number;
  pageIndex?: number;
  orderBy?: string;
  orderDirection?: string;
  groupBy?: string;
  needTotalCount?: boolean;
  name?: string;
  visibility?: string;
  ownerId?: number;
}

// 文本检索查询对象
interface TextSearchQuery {
  pageSize?: number;
  pageIndex?: number;
  orderBy?: string;
  orderDirection?: string;
  groupBy?: string;
  needTotalCount?: boolean;
  kbId?: number;
  contentId?: number;
  keyword?: string;
  k?: number;
  threshold?: number;
}

// 通用分块命令对象
interface KnowledgeGeneralChunkCmd {
  fileUrl: string;
  chunkSize?: number;
  overlapSize?: number;
  chunkSeparator?: string;
  minChunkSize?: number;
  maxChunkSize?: number;
  keepSeparator?: boolean;
  stripWhitespace?: boolean;
  removeAllUrls?: boolean;
  useQASegmentation?: boolean;
  qaLanguage?: string;
  modelRequest?: any; // 模型请求参数
}

// 父子分块命令对象
interface KnowledgeParentChildChunkCmd {
  content: string;
  parentChunkSize?: number;
  childChunkSize?: number;
  contextParagraphs?: number;
  parentOverlapSize?: number;
  childOverlapSize?: number;
  chunkSeparator?: string;
  minChunkSize?: number;
  maxChunkSize?: number;
  keepSeparator?: boolean;
  stripWhitespace?: boolean;
  removeAllUrls?: boolean;
}

// 召回测试请求对象
export interface RecallTestRequest {
  knowledgeBaseId: number;
  query: string;
  retrievalMethod: 'vector' | 'full_text' | 'hybrid';
  topK?: number;
  scoreThreshold?: number;
  rerankEnabled?: boolean;
}

// 召回测试结果对象
export interface RecallTestResult {
  chunkId: number;
  contentId: number;
  content: string;
  score: number;
  metadata?: any;
  docTitle?: string;
  chunkIndex?: number;
}

// 知识库设置对象
export interface KnowledgeBaseSettings {
  id: number;
  name: string;
  description?: string;
  visibility: 'public' | 'private';
  indexingMode: 'high_quality' | 'economy';
  embeddingModel: string;
  retrievalSettings: {
    vectorSearch: {
      enabled: boolean;
      topK: number;
      scoreThreshold: number;
    };
    fullTextSearch: {
      enabled: boolean;
    };
    hybridSearch: {
      enabled: boolean;
      rerankEnabled: boolean;
    };
  };
}

// 分块状态更新请求
export interface ChunkStatusUpdateRequest {
  chunkId: number;
  status: 'enabled' | 'disabled';
}

// 分块更新请求
export interface ChunkUpdateRequest {
  chunkId: number;
  content: string;
  metadata?: any;
}

// API响应接口
interface ApiResponse<T> {
  success: boolean;
  errCode?: string;
  errMessage?: string;
  data?: T;
  totalCount?: number;
  pageSize?: number;
  pageIndex?: number;
}

// 知识库DTO接口
export interface KnowledgeBaseDTO {
  id?: number;
  name: string;
  code: string;
  description?: string;
  visibility: string;
  ownerId?: number;
  createdAt?: number;
  updatedAt?: number;
}

export const knowledgeBaseApi = {
  /**
   * 创建知识库
   */
  create(data: KnowledgeBaseCreateCmd): Promise<ApiResponse<any>> {
    return request({
      url: '/smartcs/api/admin/knowledge-base',
      method: 'post',
      data
    });
  },

  /**
   * 更新知识库
   */
  update(data: KnowledgeBaseUpdateCmd): Promise<ApiResponse<any>> {
    return request({
      url: `/smartcs/api/admin/knowledge-base`,
      method: 'put',
      data
    });
  },

  /**
   * 查询知识库详情
   */
  getById(id: number): Promise<ApiResponse<any>> {
    return request({
      url: `/smartcs/api/admin/knowledge-base/${id}`,
      method: 'get'
    });
  },

  /**
   * 删除知识库
   */
  delete(id: number): Promise<ApiResponse<any>> {
    return request({
      url: `/smartcs/api/admin/knowledge-base/${id}`,
      method: 'delete'
    });
  },

  /**
   * 查询知识库列表
   */
  list(params: KnowledgeBaseQuery): Promise<ApiResponse<any>> {
    return request({
      url: '/smartcs/api/admin/knowledge-base',
      method: 'get',
      params
    });
  },

  /**
   * 文本检索
   */
  searchText(data: TextSearchQuery): Promise<ApiResponse<any>> {
    return request({
      url: '/smartcs/api/admin/knowledge-base/search/text',
      method: 'post',
      data
    });
  },

  /**
   * 通用文档分块
   */
  generalChunk(data: KnowledgeGeneralChunkCmd): Promise<ApiResponse<any>> {
    return request({
      url: '/smartcs/api/admin/knowledge-base/chunk/general',
      method: 'post',
      data
    });
  },

  /**
   * 父子文档分块
   */
  parentChildChunk(data: FormData): Promise<ApiResponse<any>> {
    return request.post('/admin/knowledge-base/chunk/parent-child', data, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  },

  /**
   * 召回测试
   */
  recallTest(data: RecallTestRequest): Promise<ApiResponse<RecallTestResult[]>> {
    return request({
      url: `/smartcs/api/admin/knowledge-base/${data.knowledgeBaseId}/recall-test`,
      method: 'post',
      data
    });
  },

  /**
   * 获取知识库设置
   */
  getSettings(id: number): Promise<ApiResponse<KnowledgeBaseSettings>> {
    return request({
      url: `/smartcs/api/admin/knowledge-base/${id}/settings`,
      method: 'get'
    });
  },

  /**
   * 更新知识库设置
   */
  updateSettings(data: KnowledgeBaseSettings): Promise<ApiResponse<any>> {
    return request({
      url: `/smartcs/api/admin/knowledge-base/${data.id}/settings`,
      method: 'put',
      data
    });
  },

  /**
   * 获取文档分块列表
   */
  getDocumentChunks(contentId: number, params?: any): Promise<ApiResponse<any[]>> {
    return request({
      url: `/smartcs/api/admin/content/${contentId}/chunks`,
      method: 'get',
      params
    });
  },

  /**
   * 更新分块状态
   */
  updateChunkStatus(data: ChunkStatusUpdateRequest): Promise<ApiResponse<any>> {
    return request({
      url: `/smartcs/api/admin/chunk/${data.chunkId}/status`,
      method: 'put',
      data: { status: data.status }
    });
  },

  /**
   * 更新分块内容
   */
  updateChunk(data: ChunkUpdateRequest): Promise<ApiResponse<any>> {
    return request({
      url: `/smartcs/api/admin/chunk/${data.chunkId}`,
      method: 'put',
      data
    });
  },

  /**
   * 删除分块
   */
  deleteChunk(chunkId: number): Promise<ApiResponse<any>> {
    return request({
      url: `/smartcs/api/admin/chunk/${chunkId}`,
      method: 'delete'
    });
  }
};

// 导出具名函数以便兼容现有代码
export const listKnowledgeBases = (params: KnowledgeBaseQuery): Promise<ApiResponse<KnowledgeBaseDTO[]>> => {
  return knowledgeBaseApi.list(params);
};



export const createKnowledgeBase = (data: KnowledgeBaseCreateCmd): Promise<ApiResponse<any>> => {
  return knowledgeBaseApi.create(data);
};

export const updateKnowledgeBase = (data: KnowledgeBaseUpdateCmd): Promise<ApiResponse<any>> => {
  return knowledgeBaseApi.update(data);
};

export const getKnowledgeBase = (id: number): Promise<ApiResponse<any>> => {
  return knowledgeBaseApi.getById(id);
};

export const deleteKnowledgeBase = (id: number): Promise<ApiResponse<any>> => {
  return knowledgeBaseApi.delete(id);
};

// 新增API导出函数
export const recallTest = (data: RecallTestRequest): Promise<ApiResponse<RecallTestResult[]>> => {
  return knowledgeBaseApi.recallTest(data);
};

export const getKnowledgeBaseSettings = (id: number): Promise<ApiResponse<KnowledgeBaseSettings>> => {
  return knowledgeBaseApi.getSettings(id);
};

export const updateKnowledgeBaseSettings = (data: KnowledgeBaseSettings): Promise<ApiResponse<any>> => {
  return knowledgeBaseApi.updateSettings(data);
};

export const getDocumentChunks = (contentId: number, params?: any): Promise<ApiResponse<any[]>> => {
  return knowledgeBaseApi.getDocumentChunks(contentId, params);
};

export const updateChunkStatus = (data: ChunkStatusUpdateRequest): Promise<ApiResponse<any>> => {
  return knowledgeBaseApi.updateChunkStatus(data);
};

export const updateChunk = (data: ChunkUpdateRequest): Promise<ApiResponse<any>> => {
  return knowledgeBaseApi.updateChunk(data);
};

export const deleteChunk = (chunkId: number): Promise<ApiResponse<any>> => {
  return knowledgeBaseApi.deleteChunk(chunkId);
}; 