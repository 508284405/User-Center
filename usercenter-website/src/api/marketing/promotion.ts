import request from '../config';

// 营销活动接口返回类型
export interface Campaign {
  id?: number;
  name: string;
  description?: string;
  type: string;
  typeDescription?: string;
  status: string;
  statusDescription?: string;
  startTime: string;
  endTime: string;
  targetAudience?: string;
  rules?: string;
  budget?: number;
  usedBudget?: number;
  participantLimit?: number;
  currentParticipants?: number;
  createdBy?: string;
  createdAt?: string;
  updatedBy?: string;
  updatedAt?: string;
  budgetUsageRate?: number;
  participationRate?: number;
  canParticipate?: boolean;
}

// 创建营销活动参数类型
export interface CreateCampaignParams {
  name: string;
  description?: string;
  type: string;
  startTime: string;
  endTime: string;
  targetAudience?: string;
  rules?: string;
  budget?: number;
  participantLimit?: number;
}

// 更新营销活动参数类型
export interface UpdateCampaignParams {
  name?: string;
  description?: string;
  type?: string;
  startTime?: string;
  endTime?: string;
  targetAudience?: string;
  rules?: string;
  budget?: number;
  participantLimit?: number;
}

// 营销活动查询参数
export interface CampaignQuery {
  pageIndex: number;
  pageSize: number;
  name?: string;
  type?: string;
  status?: string;
  createdBy?: string;
}

// 营销活动分页返回类型
export interface CampaignPageResponse {
  total: number;
  data: Campaign[];
}

// 营销活动参与记录
export interface CampaignParticipant {
  id?: number;
  campaignId: number;
  userId: string;
  participatedAt: string;
  rewardType?: string;
  rewardValue?: string;
  costBudget?: number;
  status: string;
}

// 促销规则类型
export interface PromotionRule {
  id?: number;
  name: string;
  description?: string;
  ruleType: string;
  conditions: string;
  actions: string;
  priority: number;
  startTime: string;
  endTime: string;
  isActive: boolean;
  usageLimit?: number;
  usedCount?: number;
  createdBy?: string;
  createdAt?: string;
  updatedBy?: string;
  updatedAt?: string;
}

// 促销规则查询参数
export interface PromotionRuleQuery {
  pageIndex: number;
  pageSize: number;
  name?: string;
  ruleType?: string;
  isActive?: boolean;
}

// 营销活动相关接口
export const campaignApi = {
  // 创建营销活动
  createCampaign: (params: CreateCampaignParams) => {
    return request.post<any, Campaign>('/api/campaigns', params);
  },

  // 更新营销活动
  updateCampaign: (id: number, params: UpdateCampaignParams) => {
    return request.put<any, Campaign>(`/api/campaigns/${id}`, params);
  },

  // 获取营销活动详情
  getCampaign: (id: number) => {
    return request.get<any, Campaign>(`/api/campaigns/${id}`);
  },

  // 分页查询营销活动
  getCampaignPage: (params: CampaignQuery) => {
    return request.get<any, CampaignPageResponse>('/api/campaigns', { params });
  },

  // 激活营销活动
  activateCampaign: (id: number) => {
    return request.post<any, void>(`/api/campaigns/${id}/activate`);
  },

  // 暂停营销活动
  pauseCampaign: (id: number) => {
    return request.post<any, void>(`/api/campaigns/${id}/pause`);
  },

  // 恢复营销活动
  resumeCampaign: (id: number) => {
    return request.post<any, void>(`/api/campaigns/${id}/resume`);
  },

  // 完成营销活动
  completeCampaign: (id: number) => {
    return request.post<any, void>(`/api/campaigns/${id}/complete`);
  },

  // 获取活动参与记录
  getCampaignParticipants: (campaignId: number, params: { pageIndex: number; pageSize: number }) => {
    return request.get<any, { total: number; data: CampaignParticipant[] }>(`/api/campaigns/${campaignId}/participants`, { params });
  }
};

// 促销规则相关接口
export const promotionRuleApi = {
  // 创建促销规则
  createRule: (params: Omit<PromotionRule, 'id' | 'usedCount' | 'createdAt' | 'updatedAt'>) => {
    return request.post<any, PromotionRule>('/api/promotion-rules', params);
  },

  // 更新促销规则
  updateRule: (id: number, params: Partial<PromotionRule>) => {
    return request.put<any, PromotionRule>(`/api/promotion-rules/${id}`, params);
  },

  // 获取促销规则详情
  getRule: (id: number) => {
    return request.get<any, PromotionRule>(`/api/promotion-rules/${id}`);
  },

  // 分页查询促销规则
  getRulePage: (params: PromotionRuleQuery) => {
    return request.get<any, { total: number; data: PromotionRule[] }>('/api/promotion-rules', { params });
  },

  // 激活/停用促销规则
  toggleRule: (id: number, isActive: boolean) => {
    return request.put<any, void>(`/api/promotion-rules/${id}/toggle`, { isActive });
  },

  // 删除促销规则
  deleteRule: (id: number) => {
    return request.delete<any, void>(`/api/promotion-rules/${id}`);
  }
};

// 秒杀活动接口（整合现有的秒杀功能）
export interface SeckillActivity {
  id?: number;
  name: string;
  description: string;
  startTime: string;
  endTime: string;
  status: number;
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

export interface SeckillActivityQuery {
  pageNum: number;
  pageSize: number;
  name?: string;
  status?: number;
  startTime?: string;
  endTime?: string;
}

export const seckillApi = {
  // 创建秒杀活动
  createActivity: (params: Omit<SeckillActivity, 'id' | 'availableStock' | 'createdAt' | 'updatedAt'>) => {
    return request.post<any, SeckillActivity>('/client-web/api/seckill/activities', params);
  },

  // 更新秒杀活动
  updateActivity: (id: number, params: Partial<SeckillActivity>) => {
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
    return request.get<any, { total: number; data: SeckillActivity[] }>('/client-web/api/seckill/activities', { params });
  }
};

// 营销数据统计接口
export interface MarketingStats {
  totalCampaigns: number;
  activeCampaigns: number;
  totalParticipants: number;
  totalBudgetUsed: number;
  conversionRate: number;
  topCampaigns: Campaign[];
}

export const marketingStatsApi = {
  // 获取营销总览统计
  getOverviewStats: () => {
    return request.get<any, MarketingStats>('/api/marketing/stats/overview');
  },

  // 获取活动效果分析
  getCampaignAnalysis: (campaignId: number, dateRange: [string, string]) => {
    return request.get<any, any>(`/api/marketing/stats/campaigns/${campaignId}/analysis`, {
      params: { startDate: dateRange[0], endDate: dateRange[1] }
    });
  },

  // 获取用户参与度统计
  getUserEngagementStats: (dateRange: [string, string]) => {
    return request.get<any, any>('/api/marketing/stats/user-engagement', {
      params: { startDate: dateRange[0], endDate: dateRange[1] }
    });
  }
};