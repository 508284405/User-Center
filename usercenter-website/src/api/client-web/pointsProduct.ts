import request from '../config';
import type { ApiResponse } from './product';

// 积分商品相关接口
export interface SkuExchangeInfo {
  skuId: number;
  skuCode?: string;
  points: number; // 所需积分
  exchangeLimit?: number; // 每人可兑换数量限制，0表示不限制
  enabled?: boolean;
}

export interface PointsProduct {
  id?: number;
  productId: number;
  points: number; // 默认积分价格
  exchangeLimit?: number; // 每人可兑换数量限制，0表示不限制
  enabled: boolean; // true: 上架(启用), false: 下架(禁用)
  product?: any;
  skuExchangeInfoList: SkuExchangeInfo[];
  hasSpec?: boolean; // 是否有规格
  startTime?: string;
  endTime?: string;
}

// 分页查询积分商品列表
export interface PointsProductQuery {
  pageNum: number;
  pageSize: number;
  name?: string;
  enabled?: boolean;
}

export interface PointsProductListResponse extends ApiResponse<PointsProduct[]> {
  totalCount: number;
  pageSize: number;
  pageNum: number;
}

export const getPointsProductList = (params: PointsProductQuery): Promise<PointsProductListResponse> => {
  return request.post('/client-web/api/admin/points/exchangeable-products/page', params);
};

// 获取积分商品详情
export const getPointsProduct = (id: number): Promise<ApiResponse<PointsProduct>> => {
  return request.get(`/client-web/api/admin/points/exchangeable-products/${id}`);
};

// 创建积分商品
export const createPointsProduct = (data: PointsProduct): Promise<ApiResponse<PointsProduct>> => {
  return request.post('/client-web/api/admin/points/exchangeable-products/set', data);
};

// 更新积分商品
export const updatePointsProduct = (id: number, data: PointsProduct): Promise<ApiResponse<PointsProduct>> => {
  return request.put(`/client-web/api/admin/points/exchangeable-products/set`, data);
};

// 删除积分商品
export const deletePointsProduct = (id: number): Promise<ApiResponse<void>> => {
  return request.delete(`/client-web/api/admin/points/exchangeable-products/${id}`);
};

// 更新积分商品状态
export const updatePointsProductStatus = (id: number, enabled: boolean): Promise<ApiResponse<void>> => {
  return request.put(`/client-web/api/admin/points/exchangeable-products/${id}/status?enabled=${enabled}`);
};