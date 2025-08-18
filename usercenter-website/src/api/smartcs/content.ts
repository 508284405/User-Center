import request from '../config';

export interface ContentListQuery {
  knowledgeBaseId?: number;
  title?: string;
  contentType?: string;
  status?: string;
  segmentMode?: string;
  pageIndex?: number;
  pageSize?: number;
}

export interface ContentDTO {
  id: number;
  knowledgeBaseId: number;
  title: string;
  contentType: string;
  fileUrl: string;
  fileType: string;
  textExtracted?: string;
  status: string;
  segmentMode: string;
  charCount: number;
  recallCount: number;
  createdBy: number;
  createdAt: number;
  updatedAt: number;
  metadata?: string;
  originalFileName?: string;
  fileSize?: number;
  source?: string;
  processingTime?: number;
  embeddingTime?: number;
  embeddingCost?: number;
  averageChunkLength?: number;
  chunkCount?: number;
  processingStatus?: string;
  processingErrorMessage?: string;
  recallRate?: number;
}

export interface ContentCreateRequest {
  knowledgeBaseId: number;
  title: string;
  contentType: string;
  ossUrl: string;
  fileSize: number;
  fileType: string;
}

export interface ContentUpdateRequest {
  id: number;
  title: string;
}

export interface ContentStatusUpdateRequest {
  id: number;
  status: 'enabled' | 'disabled';
}

export interface DocumentSearchRequest {
  query: string;
  contentId?: number;
  topK?: number;
  scoreThreshold?: number;
}

export interface DocumentSearchResultDTO {
  chunkId: number;
  content: string;
  score: number;
  metadata?: any;
}

export interface PageResponse<T> {
  data: T[];
  total: number;
  pageSize: number;
  pageIndex: number;
}

export interface Response {
  success: boolean;
  errCode?: string;
  errMessage?: string;
}

export const contentApi = {
  /**
   * 查询内容列表
   */
  listContents: (params: ContentListQuery): Promise<PageResponse<ContentDTO>> => {
    return request({
      url: '/smartcs/api/admin/content',
      method: 'GET',
      params,
    });
  },

  /**
   * 获取单个内容详情
   */
  getById: (id: number): Promise<Response & { data?: ContentDTO }> => {
    return request({
      url: `/smartcs/api/admin/content/${id}`,
      method: 'GET',
    });
  },

  /**
   * 创建内容
   */
  create: (data: ContentCreateRequest): Promise<Response> => {
    return request({
      url: '/smartcs/api/admin/content',
      method: 'POST',
      data,
    });
  },

  /**
   * 更新内容
   */
  update: (data: ContentUpdateRequest): Promise<Response> => {
    return request({
      url: '/smartcs/api/admin/content',
      method: 'PUT',
      data,
    });
  },

  /**
   * 编辑内容（包含分段设置）
   */
  updateContent: (data: ContentEditRequest): Promise<Response> => {
    return request({
      url: '/smartcs/api/admin/content/edit',
      method: 'PUT',
      data,
    });
  },

  /**
   * 删除内容
   */
  delete: (id: number): Promise<Response> => {
    return request({
      url: `/smartcs/api/admin/content/${id}`,
      method: 'DELETE',
    });
  },

  /**
   * 解析内容
   */
  parse: (id: number): Promise<Response> => {
    return request({
      url: `/smartcs/api/admin/content/${id}/parse`,
      method: 'POST',
    });
  },

  /**
   * 向量检索
   */
  vectorSearch: (data: DocumentSearchRequest): Promise<Response & { data?: DocumentSearchResultDTO[] }> => {
    return request({
      url: '/smartcs/api/admin/content/vector-search',
      method: 'POST',
      data,
    });
  },

  /**
   * 更新内容状态
   */
  updateContentStatus: (data: ContentStatusUpdateRequest): Promise<Response> => {
    return request({
      url: '/smartcs/api/admin/content/status',
      method: 'PUT',
      data,
    });
  },

  /**
   * 文档处理（包含分块和向量化）
   */
  processDocument: (data: DocumentProcessRequest): Promise<Response & { data?: DocumentProcessResult }> => {
    return request({
      url: '/smartcs/api/admin/content/process',
      method: 'POST',
      data,
    });
  },
};

export interface ContentEditRequest {
  id: number;
  knowledgeBaseId: number;
  title: string;
  segmentMode: string;
  segmentSettings: any;
  parentChildSettings?: any;
}

// 新增：文档处理请求接口
export interface DocumentProcessRequest {
  knowledgeBaseId: number;
  title: string;
  fileUrl: string;
  fileType: string;
  fileSize: number;
  originalFileName?: string;
  source?: string;
  metadata?: string;
  segmentMode: 'general' | 'parent_child';
  segmentSettings: any;
  parentChildSettings?: any;
  indexMethod: string;
  retrievalSettings: any;
  editMode: boolean;
  editData?: any;
}

// 新增：文档处理结果接口
export interface DocumentProcessResult {
  contentId: number;
  chunkCount: number;
  processingTime: number;
  tokenCount: number;
  embeddingCost: number;
  charCount: number;
  recallCount: number;
  status: string;
  errorMessage?: string;
} 

// 新增：通过URL导入文档请求
export interface UrlDocumentImportRequest {
  knowledgeBaseId: number;
  modelId: number;
  url: string;
  title?: string;
  fileType?: string;
  fileSize?: number;
  originalFileName?: string;
  segmentMode?: 'general' | 'parent_child';
  segmentSettings?: any;
  parentChildSettings?: any;
  indexMethod?: string;
  retrievalSettings?: any;
  useKbDefaults?: boolean;
}

// 新增：通过URL导入
export const importByUrl = (data: UrlDocumentImportRequest): Promise<Response & { data?: DocumentProcessResult }> => {
  return request({
    url: '/smartcs/api/admin/content/import-url',
    method: 'POST',
    data,
  });
};