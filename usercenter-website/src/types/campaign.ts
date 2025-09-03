export interface CampaignDTO {
  id?: number
  name: string
  type: string
  description?: string
  status: string
  startTime: string
  endTime: string
  threshold?: number
  discount?: number
  discountRate?: number
  seckillPrice?: number
  totalStock?: number
  remainingStock?: number
  participantCount?: number
  orderCount?: number
  totalSales?: number
  rules?: string
  constraints?: string
  priority?: number
  createdAt?: string
  updatedAt?: string
  createdBy?: string
  updatedBy?: string
}

export interface CreateCampaignCmd {
  name: string
  type: string
  description?: string
  startTime: string
  endTime: string
  threshold?: number
  discount?: number
  discountRate?: number
  seckillPrice?: number
  totalStock?: number
  rules?: string
  constraints?: string
  priority?: number
}

export interface UpdateCampaignCmd extends CreateCampaignCmd {
  id: number
}

export interface CampaignPageQuery {
  pageIndex: number
  pageSize: number
  name?: string
  type?: string
  status?: string
  startTimeFrom?: string
  startTimeTo?: string
}

export interface CampaignAnalytics {
  campaignId: number
  participantCount: number
  orderCount: number
  totalSales: number
  conversionRate: number
  avgOrderAmount: number
  dailyStats: DailyStat[]
}

export interface DailyStat {
  date: string
  participants: number
  orders: number
  sales: number
}

export interface CampaignParticipant {
  userId: string
  userName?: string
  joinTime: string
  orderCount: number
  totalAmount: number
  status: string
}