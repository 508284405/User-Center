/**
 * 会员价格配置相关类型定义
 */

// 折扣类型
export type DiscountType = 'PERCENTAGE' | 'FIXED_AMOUNT' | 'FIXED_PRICE'

// 会员等级
export type MemberLevel = 'SILVER' | 'GOLD' | 'PLATINUM' | 'DIAMOND'

// 会员价格配置DTO
export interface MemberPricingDTO {
  id?: number
  productId: number
  memberLevel: string
  discountType: DiscountType
  discountTypeDesc?: string
  discountValue: number
  minPurchaseQuantity?: number
  maxPurchaseQuantity?: number
  startTime?: string | Date
  endTime?: string | Date
  isActive: boolean
  createdBy?: string
  createdAt?: string
  updatedBy?: string
  updatedAt?: string
}

// 创建会员价格配置命令
export interface CreateMemberPricingCmd {
  productId: number
  memberLevel: string
  discountType: DiscountType
  discountValue: number
  minPurchaseQuantity?: number
  maxPurchaseQuantity?: number
  startTime?: string | Date
  endTime?: string | Date
  isActive?: boolean
  createdBy: string
}

// 更新会员价格配置命令
export interface UpdateMemberPricingCmd {
  id: number
  discountType?: DiscountType
  discountValue?: number
  minPurchaseQuantity?: number
  maxPurchaseQuantity?: number
  startTime?: string | Date
  endTime?: string | Date
  isActive?: boolean
  updatedBy?: string
}

// 会员价格查询参数
export interface MemberPricingQuery {
  id?: number
  productId?: number
  memberLevel?: string
}

// 会员价格分页查询参数
export interface MemberPricingPageQuery {
  pageNum: number
  pageSize: number
  productId?: number | string
  memberLevel?: string
  isActive?: boolean
}

// 会员价格计算参数
export interface MemberPriceCalculateParams {
  productId: number
  memberLevel: string
  originalPrice: number // 单位：分
  quantity: number
}

// 会员价格批量查询参数
export interface BatchMemberPricingParams {
  productIds: number[]
  memberLevel: string
}

// API响应基础类型
export interface ApiResponse<T = any> {
  success: boolean
  data?: T
  errCode?: string
  errMessage?: string
  totalCount?: number
}