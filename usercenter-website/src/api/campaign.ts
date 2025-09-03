import { request } from '@/utils/request'
import type { 
  CampaignDTO, 
  CreateCampaignCmd, 
  UpdateCampaignCmd, 
  CampaignPageQuery,
  CampaignAnalytics,
  CampaignParticipant
} from '@/types/campaign'

export const campaignApi = {
  // 创建促销活动
  createCampaign: (data: CreateCampaignCmd) => 
    request.post<CampaignDTO>('/api/campaign', data),

  // 更新促销活动
  updateCampaign: (id: number, data: UpdateCampaignCmd) => 
    request.put<CampaignDTO>(`/api/campaign/${id}`, data),

  // 获取促销活动详情
  getCampaignById: (id: number) => 
    request.get<CampaignDTO>(`/api/campaign/${id}`),

  // 分页查询促销活动
  getCampaignPage: (params: any) => 
    request.get<CampaignDTO[]>('/api/campaign/page', { params }),

  // 激活促销活动
  activateCampaign: (id: number) => 
    request.put<void>(`/api/campaign/${id}/activate`),

  // 暂停促销活动
  pauseCampaign: (id: number) => 
    request.put<void>(`/api/campaign/${id}/pause`),

  // 恢复促销活动
  resumeCampaign: (id: number) => 
    request.put<void>(`/api/campaign/${id}/resume`),

  // 完成促销活动
  completeCampaign: (id: number) => 
    request.put<void>(`/api/campaign/${id}/complete`),

  // 获取活动数据分析
  getCampaignAnalytics: (id: number) => 
    request.get<CampaignAnalytics>(`/api/campaign/${id}/analytics`),

  // 获取活动参与用户
  getCampaignParticipants: (id: number, params?: any) => 
    request.get<CampaignParticipant[]>(`/api/campaign/${id}/participants`, { params }),

  // 获取活动关联订单
  getCampaignOrders: (id: number, params?: any) => 
    request.get<any[]>(`/api/campaign/${id}/orders`, { params })
}