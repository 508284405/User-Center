import request from '../config';

// 定义后端数据结构对应的 TypeScript 接口

interface ResponseResult<T = any> {
  success: boolean;
  errCode: string;
  errMessage: string;
  data: T;
  totalCount?: number;
  pageSize?: number;
  pageIndex?: number;
}

export interface GetIndexInfoQry {
  indexName: string;
}

export interface DeleteIndexCmd {
  indexName: string;
}

export interface CreateIndexCmd {
  indexName: string;
  prefix: string;
  schema: Record<string, string>; // 键为字段名，值为字段类型字符串
  replaceIfExists: boolean;
}

export interface IndexInfoDTO {
  name: string;
  attributes: Array<Record<string, any>>; // 简化表示，实际可能需要更详细的字段类型
  // 其他可能需要的字段，根据后端 IndexInfoDTO 补充
  options?: Record<string, any>;
  definition?: Record<string, any>;
  docs?: number;
  terms?: number;
  records?: number;
}

// API 调用函数

/**
 * 获取所有Redisearch索引名称列表
 */
export const listIndexes = (): Promise<ResponseResult<string[]>> => {
  return request({
    url: '/smartcs/api/admin/knowledge/index',
    method: 'GET',
  });
};

/**
 * 获取指定索引的详细信息
 */
export const getIndexInfo = (params: GetIndexInfoQry): Promise<ResponseResult<IndexInfoDTO>> => {
  return request({
    url: '/smartcs/api/admin/knowledge/index/info',
    method: 'GET',
    params: params,
  });
};

/**
 * 创建Redisearch索引
 */
export const createIndex = (data: CreateIndexCmd): Promise<ResponseResult> => {
  return request({
    url: '/smartcs/api/admin/knowledge/index',
    method: 'POST',
    data: data,
  });
};

/**
 * 删除Redisearch索引
 */
export const deleteIndex = (data: DeleteIndexCmd): Promise<ResponseResult> => {
  return request({
    url: '/smartcs/api/admin/knowledge/index',
    method: 'DELETE',
    data: data,
  });
}; 