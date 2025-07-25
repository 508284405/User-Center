import request from '../config';

// 售后退款记录项类型
export interface AfterSaleRefundItem {
  id: number;
  afterSaleNo: string;
  orderNumber: string;
  orderItemId?: number;
  skuId?: number;
  quantity?: number;
  refundNo: string;
  paymentChannel: string;
  refundAmount: number;
  refundStatus: string;
  refundTime: number;
  reason: string;
  operatorId?: number;
  createdAt?: number;
  updatedAt?: number;
}

// 分页查询响应类型
interface PageResponse {
  success: boolean;
  errCode: string;
  errMessage: string;
  data: AfterSaleRefundItem[];
  totalCount: number;
}

// 详情响应类型
interface DetailResponse {
  success: boolean;
  errCode: string;
  errMessage: string;
  data: AfterSaleRefundItem;
}

// 审核通过/处理响应类型
interface ProcessResponse {
  success: boolean;
  errCode: string;
  errMessage: string;
}

// 分页获取售后退款列表
export function getAfterSaleRefundPage(params: {
  pageIndex: number;
  pageSize: number;
  afterSaleNo?: string;
  refundNo?: string;
  refundStatus?: string;
  startTime?: number;
  endTime?: number;
}) {
  return request.get<any, PageResponse>('/client-web/api/aftersale/admin/refund/page', { params });
}

// 获取退款详情
export function getAfterSaleRefundDetail(id: number) {
  return request.get<any, DetailResponse>(`/client-web/api/aftersale/admin/refund/${id}`);
}

// 审核通过（处理退款）
export function approveAfterSaleRefund(id: number) {
  return request.post<any, ProcessResponse>(`/client-web/api/aftersale/admin/refund/${id}/process`);
} 