import request from '@/utils/request'
import type {
  MemberPricingDTO,
  CreateMemberPricingCmd,
  UpdateMemberPricingCmd,
  MemberPricingQuery,
  MemberPricingPageQuery,
  MemberPriceCalculateParams,
  BatchMemberPricingParams,
  ApiResponse
} from '@/types/member-pricing'

/**
 * 会员价格配置API
 */
export const memberPricingApi = {
  /**
   * 创建会员价格配置
   */
  create(data: CreateMemberPricingCmd): Promise<ApiResponse<MemberPricingDTO>> {
    return request.post('/api/member-pricing', data)
  },

  /**
   * 更新会员价格配置
   */
  update(id: number, data: Partial<UpdateMemberPricingCmd>): Promise<ApiResponse<MemberPricingDTO>> {
    return request.put(`/api/member-pricing/${id}`, data)
  },

  /**
   * 获取会员价格配置详情
   */
  getById(id: number): Promise<ApiResponse<MemberPricingDTO>> {
    return request.get(`/api/member-pricing/${id}`)
  },

  /**
   * 根据商品ID获取会员价格配置
   */
  getByProduct(productId: number): Promise<ApiResponse<MemberPricingDTO[]>> {
    return request.get(`/api/member-pricing/product/${productId}`)
  },

  /**
   * 根据会员等级获取价格配置
   */
  getByLevel(memberLevel: string): Promise<ApiResponse<MemberPricingDTO[]>> {
    return request.get(`/api/member-pricing/level/${memberLevel}`)
  },

  /**
   * 分页查询会员价格配置
   */
  getPage(params: MemberPricingPageQuery): Promise<ApiResponse<MemberPricingDTO[]>> {
    return request.get('/api/member-pricing/page', { params })
  },

  /**
   * 删除会员价格配置
   */
  delete(id: number): Promise<ApiResponse> {
    return request.delete(`/api/member-pricing/${id}`)
  },

  /**
   * 激活会员价格配置
   */
  activate(id: number): Promise<ApiResponse> {
    return request.put(`/api/member-pricing/${id}/activate`)
  },

  /**
   * 停用会员价格配置
   */
  deactivate(id: number): Promise<ApiResponse> {
    return request.put(`/api/member-pricing/${id}/deactivate`)
  },

  /**
   * 计算会员价格
   */
  calculate(params: MemberPriceCalculateParams): Promise<ApiResponse<number>> {
    return request.get('/api/member-pricing/calculate', { params })
  },

  /**
   * 批量获取会员价格配置
   */
  getBatch(params: BatchMemberPricingParams): Promise<ApiResponse<MemberPricingDTO[]>> {
    return request.post('/api/member-pricing/batch', params.productIds, {
      params: { memberLevel: params.memberLevel }
    })
  }
}