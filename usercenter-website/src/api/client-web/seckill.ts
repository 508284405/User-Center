import request from '../config';

// 秒杀活动接口返回类型
export interface SeckillActivity {
  id?: number;
  name: string;
  description: string;
  startTime: string; // ISO 8601格式时间字符串
  endTime: string;   // ISO 8601格式时间字符串
  status: number;    // 0:未开始 1:进行中 2:已结束
  productId: number;
  productName?: string;
  productPrice: number;
  seckillPrice: number;
  totalStock: number;
  availableStock: number;
  limitPerUser: number;
  createdBy?: string;
  updatedBy?: string;
  createdAt?: string;
  updatedAt?: string;
}

// 创建秒杀活动参数类型
export interface CreateSeckillActivityParams {
  name: string;
  description: string;
  startTime: string;
  endTime: string;
  productId: number;
  productPrice: number;
  seckillPrice: number;
  totalStock: number;
  limitPerUser: number;
}

// 更新秒杀活动参数类型
export interface UpdateSeckillActivityParams {
  name?: string;
  description?: string;
  startTime?: string;
  endTime?: string;
  productId?: number;
  productPrice?: number;
  seckillPrice?: number;
  totalStock?: number;
  limitPerUser?: number;
  status?: number;
}

// 分页查询参数类型
export interface SeckillActivityQuery {
  pageNum: number;
  pageSize: number;
  name?: string;
  status?: number;
  startTime?: string;
  endTime?: string;
}

// 分页查询返回类型
export interface SeckillActivityPageResponse {
  total: number;
  data: SeckillActivity[];
}

// 秒杀订单接口返回类型
export interface SeckillOrder {
  id?: number;
  activityId: number;
  userId: number;
  orderId: string;
  productCount: number;
  orderAmount: number;
  status: number; // 0:待支付 1:已支付 2:已取消
  createdAt?: string;
  updatedAt?: string;
}

// 秒杀订单查询参数
export interface SeckillOrderQuery {
  pageNum: number;
  pageSize: number;
  activityId?: number;
  userId?: number;
  status?: number;
}

// 秒杀订单分页返回类型
export interface SeckillOrderPageResponse {
  total: number;
  data: SeckillOrder[];
}

// 秒杀系统相关接口
export const seckillApi = {
  // 创建秒杀活动
  createActivity: (params: CreateSeckillActivityParams) => {
    return request.post<any, SeckillActivity>('/client-web/api/seckill/activities', params);
  },

  // 更新秒杀活动
  updateActivity: (id: number, params: UpdateSeckillActivityParams) => {
    return request.put<any, SeckillActivity>(`/client-web/api/seckill/activities/${id}`, params);
  },

  // 删除秒杀活动
  deleteActivity: (id: number) => {
    return request.delete<any, void>(`/client-web/api/seckill/activities/${id}`);
  },

  // 获取秒杀活动详情
  getActivity: (id: number) => {
    return request.get<any, SeckillActivity>(`/client-web/api/seckill/activities/${id}`);
  },

  // 分页查询秒杀活动
  getActivityList: (params: SeckillActivityQuery) => {
    return request.get<any, SeckillActivityPageResponse>('/client-web/api/seckill/activities', { params });
  },

  // 获取秒杀订单列表
  getOrderList: (params: SeckillOrderQuery) => {
    return request.get<any, SeckillOrderPageResponse>('/client-web/api/seckill/orders', { params });
  },

  // 获取秒杀订单详情
  getOrder: (id: number) => {
    return request.get<any, SeckillOrder>(`/client-web/api/seckill/orders/${id}`);
  },

  // 更新秒杀订单状态
  updateOrderStatus: (id: number, status: number) => {
    return request.put<any, SeckillOrder>(`/client-web/api/seckill/orders/${id}/status`, { status });
  }
};