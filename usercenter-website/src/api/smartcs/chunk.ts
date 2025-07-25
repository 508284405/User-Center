import request from '../config';

// 切片数据传输对象
interface ChunkDTO {
  id?: number;
  contentId?: number;
  chunkIndex?: number;
  content?: string;
  tokenSize?: number;
  metadata?: string;
  createTime?: number;
  updateTime?: number;
}

// 切片创建命令对象
interface ChunkCreateCmd {
  contentId: number;
  chunkIndex: number;
  content: string;
  tokenSize?: number;
  metadata?: string;
}

// 切片更新命令对象
interface ChunkUpdateCmd {
  id: number;
  chunkIndex?: number;
  content?: string;
  tokenSize?: number;
  metadata?: string;
}

// 切片查询对象
interface ChunkListQry {
  pageSize?: number;
  pageIndex?: number;
  orderBy?: string;
  orderDirection?: string;
  groupBy?: string;
  needTotalCount?: boolean;
  contentId?: number;
  keyword?: string;
  chunkIndex?: number;
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

export const chunkApi = {
  /**
   * 创建切片
   */
  create(data: ChunkCreateCmd): Promise<ApiResponse<ChunkDTO>> {
    return request({
      url: '/smartcs/api/admin/chunk',
      method: 'post',
      data
    });
  },

  /**
   * 更新切片
   */
  update(data: ChunkUpdateCmd): Promise<ApiResponse<any>> {
    return request({
      url: '/smartcs/api/admin/chunk',
      method: 'put',
      data
    });
  },

  /**
   * 查询切片详情
   */
  getById(id: number): Promise<ApiResponse<ChunkDTO>> {
    return request({
      url: `/smartcs/api/admin/chunk/${id}`,
      method: 'get'
    });
  },

  /**
   * 删除切片
   */
  delete(id: number): Promise<ApiResponse<any>> {
    return request({
      url: `/smartcs/api/admin/chunk/${id}`,
      method: 'delete'
    });
  },

  /**
   * 查询切片列表
   */
  list(params: ChunkListQry): Promise<ApiResponse<ChunkDTO[]>> {
    return request({
      url: '/smartcs/api/admin/chunk',
      method: 'get',
      params
    });
  },

  /**
   * 切片向量化存储
   */
  vectorize(id: number): Promise<ApiResponse<any>> {
    return request({
      url: `/smartcs/api/admin/chunk/${id}/vectorize`,
      method: 'post'
    });
  }
}; 