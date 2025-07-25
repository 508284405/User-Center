import request from '../config';
import { OrderStatusEnum } from '@/enums/orderStatus';

// API响应的通用接口
export interface ApiResponse<T> {
  success: boolean;
  errCode?: string;
  errMessage?: string;
  data: T;
}

// 订单管理相关接口
export interface Order {
  id?: number;
  orderNumber: string;
  userId: number;
  status: OrderStatusEnum;
  totalAmount: number;
  payAmount: number;
  payTime?: string;
  trackingNumber?: string;
  carrier?: string;
  refundStatus?: OrderStatusEnum;
  refundReason?: string;
  refundComment?: string;
  createdAt?: string;
  updatedAt?: string;
}

// 订单地址信息
export interface OrderAddress {
  recipient: string;
  phone: string;
  province: string;
  city: string;
  district: string;
  detailAddress: string;
}

// 订单项信息
export interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  productImage: string;
  skuId: number;
  skuProperties: Record<string, string>;
  skuImage: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  skuDescription: string;
  afterSaleNo?: string;
  afterSaleStatus?: string;
}

// 订单详情接口 - 按文档定义
export interface OrderDetail {
  id: number;
  orderNumber: string;
  userId: number;
  userName: string;
  totalAmount: number;
  orderStatus: OrderStatusEnum;
  createTime: number;
  payTime?: number;
  shipTime?: number;
  completeTime?: number;
  expireTime?: number;
  paymentMethod?: string;
  trackingNumber?: string;
  carrier?: string;
  address?: OrderAddress;
  items: OrderItem[];
  refundReason?: string;
  refundStatus?: OrderStatusEnum;
  paymentForm?: string;
  transactionId?: string;
}

// 订单查询参数
export interface OrderQuery {
  pageSize: number;
  pageIndex: number;
  orderBy?: string;
  orderDirection?: string;
  groupBy?: string;
  needTotalCount?: boolean;
  orderNo?: string;
  userId?: number;
  status?: number;
  startTime?: string;
  endTime?: string;
}

// 订单列表响应
export interface OrderListResponse extends ApiResponse<Order[]> {
  totalCount: number;
  pageSize: number;
  pageIndex: number;
}

// 获取订单列表（分页查询）
export const getOrderList = (params: OrderQuery): Promise<OrderListResponse> => {
  return request.post('/client-web/api/admin/orders/page', params);
};

// 获取订单详情 - 根据订单号
export const getOrderDetail = (orderNumber: string): Promise<ApiResponse<OrderDetail>> => {
  return request.get(`/client-web/api/admin/orders/${orderNumber}`);
};

// 发货信息
export interface ShipOrderParams {
  orderNumber: string;
  orderItemId: string[];
  waybillNo?: string;
  carrier?: string;
  noLogistics?: boolean;
}

// 发货
export const shipOrder = (orderNumber: string, params: ShipOrderParams): Promise<ApiResponse<void>> => {
  return request.post(`/client-web/api/admin/orders/${orderNumber}/ship`, params);
};

// 退款处理参数
export interface RefundProcessParams {
  approved: boolean;
  comment: string;
}

// 处理退款申请
export const processRefund = (orderNumber: string, params: RefundProcessParams): Promise<ApiResponse<void>> => {
  return request.post(`/client-web/api/admin/orders/${orderNumber}/process-refund`, params);
};

// 订单统计查询参数
export interface OrderStatisticsQuery {
  userId?: number;
  startDate?: string;
  endDate?: string;
  orderStatus?: string;
}

// 订单统计信息
export interface OrderStatistics {
  totalOrders: number;
  totalAmount: number;
  completedOrders: number;
  pendingOrders: number;
  cancelledOrders: number;
}

// 获取订单统计信息
export const getOrderStatistics = (params: OrderStatisticsQuery): Promise<ApiResponse<OrderStatistics>> => {
  return request.get('/client-web/api/admin/orders/statistics', { params });
};