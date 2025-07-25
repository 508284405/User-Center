import  request  from '../config';

// 积分兑换相关接口
export interface ExchangeOrder {
  id?: number;
  userId: number;
  productId: number;
  quantity: number;
  pointsCost: number;
  status: number; // 0: 待审核, 1: 已通过, 2: 已驳回, 3: 已完成, 4: 已发货
  approver?: string;
  approveTime?: string;
  approveRemark?: string;
  logisticsInfo?: string;
  createdAt?: string;
  updatedAt?: string;
}

// 创建兑换申请
export const createExchange = (data: {
  productId: number;
  quantity: number;
}) => {
  return request.post('/client-web/api/exchanges', data);
};

// 获取兑换申请详情
export const getExchange = (id: number) => {
  return request.get(`/client-web/api/exchanges/${id}`);
};

// 分页查询兑换申请列表
export interface ExchangeQuery {
  pageNum: number;
  pageSize: number;
  userId?: number;
  productId?: number;
  status?: number;
  startTime?: string;
  endTime?: string;
}

export const getExchangeList = (params: ExchangeQuery) => {
  return request.post('/client-web/api/admin/points/exchanges/page', params);
};

// 审批兑换申请
export interface ExchangeApproval {
  id: number;
  status: number; // 1: 通过, 2: 驳回
  remark?: string;
}

export const approveExchange = (data: ExchangeApproval) => {
  return request.put(`/client-web/api/exchanges/${data.id}/approve`, data);
};

// 更新物流信息
export const updateLogistics = (id: number, logisticsInfo: string) => {
  return request.put(`/client-web/api/exchanges/${id}/logistics`, { logisticsInfo });
};

// 获取用户兑换记录
export const getUserExchanges = (userId: number, params: ExchangeQuery) => {
  return request.post(`/client-web/api/users/${userId}/exchanges`, params);
};