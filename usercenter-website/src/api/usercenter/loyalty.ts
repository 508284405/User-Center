import request from '../config'

// 积分管理API
export const pointsApi = {
  // 查询用户积分余额
  getPointBalance(userId: number) {
    return request.get(`/api/loyalty/points/balance?userId=${userId}`)
  },

  // 用户积分获取
  earnPoints(data: {
    userId: number
    amount: number
    bizType: string
    bizId: string
    idempotencyKey: string
    description?: string
  }) {
    return request.post('/api/loyalty/points/earn', data)
  },

  // 用户积分扣减
  deductPoints(data: {
    userId: number
    amount: number
    bizType: string
    bizId: string
    idempotencyKey: string
    description?: string
  }) {
    return request.post('/api/loyalty/points/deduct', data)
  },

  // 查询积分流水
  getPointTransactions(params: {
    userId: number
    page?: number
    size?: number
  }) {
    return request.get('/api/loyalty/points/transactions', { params })
  },

  // 批量过期积分处理
  processExpiredPoints(data: { batchSize: number }) {
    return request.post('/api/loyalty/points/expire-batch', data)
  }
}

// 等级管理API
export const levelsApi = {
  // 查询用户当前等级
  getUserLevel(userId: number) {
    return request.get(`/api/loyalty/levels/current?userId=${userId}`)
  },

  // 查询等级定义列表
  getLevelDefinitions() {
    return request.get('/api/loyalty/levels/definitions')
  },

  // 创建等级定义
  createLevelDefinition(data: {
    levelCode: string
    name: string
    threshold: number
    pkgId?: number
    priority: number
  }) {
    return request.post('/api/loyalty/levels/definitions', data)
  },

  // 更新等级定义
  updateLevelDefinition(id: number, data: any) {
    return request.put(`/api/loyalty/levels/definitions/${id}`, data)
  },

  // 触发等级评估
  evaluateLevel(userId: number) {
    return request.post(`/api/loyalty/levels/evaluate`, { userId })
  }
}

// 优惠券管理API  
export const couponsApi = {
  // 查询优惠券模板列表
  getCouponTemplates(params: {
    page?: number
    size?: number
    status?: string
  }) {
    return request.get('/api/coupons/templates', { params })
  },

  // 创建优惠券模板
  createCouponTemplate(data: {
    tplCode: string
    name: string
    type: string
    faceValue?: number
    discountRate?: number
    thresholdAmount?: number
    validFrom: string
    validTo: string
    issueStart: string
    issueEnd: string
    total: number
    constraintsJson?: string
  }) {
    return request.post('/api/coupons/templates', data)
  },

  // 更新优惠券模板
  updateCouponTemplate(tplCode: string, data: any) {
    return request.put(`/api/coupons/templates/${tplCode}`, data)
  },

  // 发放优惠券
  issueCoupon(data: {
    tplCode: string
    userId?: number
    userIds?: number[]
    idempotencyKey: string
  }) {
    return request.post('/api/coupons/issue', data)
  },

  // 查询用户优惠券
  getUserCoupons(params: {
    userId: number
    state?: string
    page?: number
    size?: number
  }) {
    return request.get('/api/coupons/mine', { params })
  },

  // 校验优惠券
  validateCoupon(data: {
    couponCode: string
    items: any[]
    orderAmount: number
    channel: string
    userId: number
  }) {
    return request.post('/api/coupons/validate', data)
  },

  // 预留优惠券
  reserveCoupon(data: {
    couponCode: string
    orderId: string
    ttlSeconds: number
    idempotencyKey: string
  }) {
    return request.post('/api/coupons/reserve', data)
  },

  // 确认核销优惠券
  redeemCoupon(data: {
    couponCode: string
    orderId: string
    idempotencyKey: string
  }) {
    return request.post('/api/coupons/redeem', data)
  },

  // 释放优惠券
  releaseCoupon(data: {
    couponCode: string
    orderId: string
    reason: string
  }) {
    return request.post('/api/coupons/release', data)
  }
}

// 权益管理API
export const benefitsApi = {
  // 查询权益定义列表
  getBenefitDefinitions() {
    return request.get('/api/benefits/definitions')
  },

  // 创建权益定义
  createBenefitDefinition(data: {
    benefitCode: string
    name: string
    type: string
    ruleJson?: string
  }) {
    return request.post('/api/benefits/definitions', data)
  },

  // 查询权益包列表
  getBenefitPackages(params?: { enabled?: boolean }) {
    return request.get('/api/benefits/packages', { params })
  },

  // 创建权益包
  createBenefitPackage(data: {
    pkgId: number
    name: string
    items: Array<{
      benefitCode: string
      quota: number
      period: string
      priority: number
    }>
  }) {
    return request.post('/api/benefits/packages', data)
  },

  // 查询用户权益
  getUserEntitlements(params: {
    userId: number
    state?: string
    page?: number
    size?: number
  }) {
    return request.get('/api/benefits/entitlements', { params })
  },

  // 使用权益
  useBenefit(data: {
    benefitCode?: string
    entitlementId?: number
    userId: number
    amount: number
    bizType: string
    bizId: string
    idempotencyKey: string
  }) {
    return request.post('/api/benefits/use', data)
  }
}

// 统计分析API
export const loyaltyStatsApi = {
  // 积分统计
  getPointsStats(params: {
    startDate?: string
    endDate?: string
    userId?: number
  }) {
    return request.get('/api/loyalty/stats/points', { params })
  },

  // 优惠券统计
  getCouponStats(params: {
    startDate?: string
    endDate?: string
    tplCode?: string
  }) {
    return request.get('/api/loyalty/stats/coupons', { params })
  },

  // 等级分布统计
  getLevelStats() {
    return request.get('/api/loyalty/stats/levels')
  },

  // 权益使用统计
  getBenefitStats(params: {
    startDate?: string
    endDate?: string
    benefitCode?: string
  }) {
    return request.get('/api/loyalty/stats/benefits', { params })
  }
}