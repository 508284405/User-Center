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
}

export interface ContentCreateRequest {
  knowledgeBaseId: number;
  title: string;
  contentType: string;
  ossUrl: string;
  fileSize: number;
  fileType: string;
}

export interface ContentStatusUpdateRequest {
  contentId: number;
  status: string;
}

export interface ContentUpdateRequest {
  id: number;
  title: string;
}

export interface ContentEditRequest {
  id: number;
  knowledgeBaseId: number;
  title: string;
  segmentMode: string;
  segmentSettings: any;
  parentChildSettings?: any;
}

export interface DocumentSearchRequest {
  query: string;
  contentId?: number;
  topK?: number;
}

export interface DocumentSearchResultDTO {
  id?: number;
  text?: string;
  score?: number;
  metadata?: any;
}

export interface DocumentProcessRequest {
  knowledgeBaseId: number;
  files: string[];
  segmentMode: 'general' | 'parent_child';
  segmentSettings?: {
    identifier: string;
    maxLength: number;
    overlapLength: number;
    replaceConsecutiveSpaces: boolean;
    removeAllUrls: boolean;
    useQASegmentation: boolean;
    qaLanguage: string;
  };
  parentChildSettings?: {
    parentIdentifier: string;
    parentMaxLength: number;
    childIdentifier: string; 
    childMaxLength: number;
    replaceConsecutiveSpaces: boolean;
    removeAllUrls: boolean;
  };
  indexMethod: string;
  retrievalSettings: {
    method: string;
    rerankModel: string;
    topK: number;
    scoreThreshold: number;
    fullTextSearch: boolean;
    hybridSearch: boolean;
  };
  editMode?: boolean;
  editData?: any;
}

export interface DocumentProcessResponse {
  contentCount: number;
  chunkCount: number;
  vectorCount: number;
  contentIds: number[];
}

export interface PageResponse<T> {
  success: boolean;
  data: T[];
  totalCount: number;
  pageSize: number;
  pageIndex: number;
  errCode?: string;
  errMessage?: string;
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
   * 文档处理 - 完整的文档分块和向量化流程
   */
  processDocument: (data: DocumentProcessRequest): Promise<Response & { data?: DocumentProcessResponse }> => {
    return request({
      url: '/smartcs/api/admin/knowledge/content/process',
      method: 'POST',
      data,
    });
  },
}; 