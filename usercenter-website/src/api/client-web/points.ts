import  request  from '../config';

// 积分管理相关接口
export interface PointsRecord {
  id?: number;
  userId: number;
  points: number;
  type: number; // 1: 充值, 2: 扣减, 3: 兑换, 4: 活动赠送
  reason: string;
  operator?: string;
  beforePoints?: number;
  afterPoints?: number;
  relatedOrderId?: number;
  createdAt?: string;
}

// 用户积分变动（充值/扣减）
export interface PointsChange {
  userId: number;
  points: number;
  type: number;
  reason: string;
}

export const changeUserPoints = (data: PointsChange) => {
  return request.post('/client-web/api/points/change', data);
};

// 获取用户积分余额
export const getUserPoints = (userId: number) => {
  return request.get(`/client-web/api/users/${userId}/points`);
};

// 分页查询用户积分流水
export interface PointsRecordQuery {
  pageNum: number;
  pageSize: number;
  userId?: number;
  type?: number;
  startTime?: string;
  endTime?: string;
}

export const getPointsRecordList = (params: PointsRecordQuery) => {
  return request.post('/client-web/api/points/records/page', params);
};

// 获取用户积分流水详情
export const getPointsRecord = (id: number) => {
  return request.get(`/client-web/api/points/records/${id}`);
};

// 获取用户积分统计信息
export interface PointsStatistics {
  totalPoints: number;
  usedPoints: number;
  frozenPoints: number;
  lastChangeTime?: string;
}

export const getUserPointsStatistics = (userId: number) => {
  return request.get(`/client-web/api/users/${userId}/points/statistics`);
};

// 积分管理后台接口

// 积分充值接口
export interface PointsRecharge {
  userId: number;
  points: number;
  validDays?: number;
  remark?: string;
}

// 用户积分充值
export const rechargeUserPoints = (data: PointsRecharge) => {
  return request.post('/client-web/api/admin/points/users/points/recharge', data);
};

// 积分规则相关接口
export interface PointsRule {
  id?: number;
  ruleName: string;
  ruleDescription?: string;
  earnType: string;
  consumeType: string;
  pointValue: number;
  validDays: number;
  enabled?: boolean;
  createTime?: string;
  updateTime?: string;
}

// 获取所有积分规则
export const getAllPointsRules = () => {
  return request.get('/client-web/api/admin/points/rules');
};

// 创建积分规则
export const createPointsRule = (data: PointsRule) => {
  return request.post('/client-web/api/admin/points/rules', data);
};

// 更新积分规则
export const updatePointsRule = (data: PointsRule) => {
  return request.put('/client-web/api/admin/points/rules', data);
};

// 删除积分规则
export const deletePointsRule = (ruleId: number) => {
  return request.delete(`/client-web/api/admin/points/rules/${ruleId}`);
};

// 积分活动相关接口
export interface PointsActivity {
  id?: number;
  activityName: string;
  activityDescription?: string;
  activityType?: string;
  pointValue?: number;
  ruleId?: number;
  startTime?: number | string;
  endTime?: number | string;
  participationLimit?: number;
  status?: number;
  creatorId?: number;
  updaterId?: number;
  enabled?: boolean;
  createTime?: string;
  updateTime?: string;
}

// 获取所有积分活动
export const getAllPointsActivities = () => {
  return request.get('/client-web/api/admin/points/activities');
};

// 创建积分活动
export const createPointsActivity = (data: PointsActivity) => {
  return request.post('/client-web/api/admin/points/activities', data);
};

// 更新积分活动
export const updatePointsActivity = (data: PointsActivity) => {
  return request.put('/client-web/api/admin/points/activities', data);
};

// 删除积分活动
export const deletePointsActivity = (activityId: number) => {
  return request.delete(`/client-web/api/admin/points/activities/${activityId}`);
};

// 用户积分管理相关接口
export interface UserPointsDetail {
  userId: number;
  currentPoints: number;
  totalEarnedPoints: number;
  totalUsedPoints: number;
  expiringPoints: number;
  expirationTime?: number;
  lastUpdateTime: number;
  status: number;
}

// 积分管理后台接口 - 分页查询用户积分信息
export interface UserPointsInfo {
  userId: number;
  totalPoints: number;
  availablePoints: number;
  frozenPoints: number;
  lastEarnTime?: string;
  lastConsumeTime?: string;
}

// 积分管理后台接口 - 查询用户积分详情
export const getUserPointsDetails = (userId: number) => {
  return request.get(`/client-web/api/admin/points/users/${userId}/details`);
};

// 积分管理后台接口 - 调整用户积分
export interface PointsAdjustment {
  userId: number;
  points: number;
  reason: string;
  operatorId: number;
  remark?: string;
}

export const adjustUserPoints = (data: PointsAdjustment) => {
  return request.post('/client-web/api/admin/points/users/adjust', data);
};

// 积分管理后台接口 - 分页查询用户积分历史记录
export interface PointsHistoryQuery {
  userId: number;
  startTime?: number;
  endTime?: number;
  type?: string;
  pageSize?: number;
  pageNum?: number;
}

export const getUserPointsHistory = (userId: number, params: PointsHistoryQuery) => {
  return request.get(`/client-web/api/admin/points/users/${userId}/history/page`, { params });
};

// 获取所有用户积分列表
export interface UserPointsListQuery {
  pageNum: number;
  pageSize: number;
  userId?: number;
  status?: number;
}

// 积分管理后台接口 - 分页查询用户积分信息
export interface UserPointsQuery {
  pageSize?: number;
  pageIndex?: number;
  orderBy?: string;
  orderDirection?: string;
  groupBy?: string;
  needTotalCount?: boolean;
  userId?: number;
  minPoints?: number;
  maxPoints?: number;
}

export const getUserPointsList = (params: UserPointsQuery) => {
  return request.post('/client-web/api/admin/points/users/points/page', params);
};

// 积分管理后台接口 - 分页查询用户积分历史记录
export interface PointsHistoryQuery {
  userId: number;
  startTime?: number;
  endTime?: number;
  type?: string;
  pageSize?: number;
  pageNum?: number;
}

export const getPointsHistoryList = (userId: number, params: PointsHistoryQuery) => {
  return request.get(`/client-web/api/admin/points/users/${userId}/history/page`, { params });
};