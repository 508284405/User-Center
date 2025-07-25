import request from '../config';

export interface ResponseResult<T = any> {
  success: boolean;
  errCode?: string;
  errMessage?: string;
  data?: T;
}

export interface ClearIndexCacheCmd {
  indexName: string;
}

export interface ListIndexCacheKeysQry {
  indexName: string;
}

export interface GetCacheValueQry {
  cacheKey: string;
}

export interface CacheValueDTO {
  cacheKey: string;
  value: Record<string, any>;
  ttl: number;
}

/**
 * 清空指定索引下的缓存
 */
export const clearIndexCache = (data: ClearIndexCacheCmd): Promise<ResponseResult> => {
  return request({
    url: '/smartcs/api/admin/knowledge/index/cache',
    method: 'DELETE',
    data: data,
  });
};

/**
 * 列出指定索引前缀下的所有缓存键
 */
export const listIndexCacheKeys = (data: ListIndexCacheKeysQry): Promise<ResponseResult<string[]>> => {
  return request({
    url: '/smartcs/api/admin/knowledge/index/cache/keys',
    method: 'GET',
    params: data,
  });
};

/**
 * 根据键名获取缓存的详细值
 */
export const getCacheValue = (params: GetCacheValueQry): Promise<ResponseResult<CacheValueDTO>> => {
  return request({
    url: '/smartcs/api/admin/knowledge/index/cache',
    method: 'GET',
    params: params,
  });
}; 