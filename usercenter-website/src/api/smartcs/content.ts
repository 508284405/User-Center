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

export interface ContentStatusUpdateRequest {
  contentId: number;
  status: string;
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
   * 更新内容状态
   */
  updateContentStatus: (data: ContentStatusUpdateRequest): Promise<Response> => {
    return request({
      url: '/smartcs/api/admin/content/status',
      method: 'PUT',
      data,
    });
  },
}; 