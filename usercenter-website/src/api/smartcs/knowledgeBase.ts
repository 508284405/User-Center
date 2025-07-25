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
  }
}; 