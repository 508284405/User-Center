import request from '../config'
import type { LogisticsPackage as Package, LogisticsOrder as Order, LogisticsException as Exception, LogisticsSLA as SLA } from '@/types/logistics'
import type { CreateLogisticsOrderParams } from '@/types/logisticsOrderCreate'

// 物流统计接口
export interface StatisticsParams {
  providerCode?: string;
  startTime: number;
  endTime: number;
}

export interface ExceptionsParams {
  pageSize?: number;
  pageIndex?: number;
  orderBy?: string;
  orderDirection?: string;
  groupBy?: string;
  needTotalCount?: boolean;
  waybillNo?: string;
  exceptionType?: string;
  status?: string;
  providerCode?: string;
  startTime?: number;
  endTime?: number;
}

export interface SlaParams {
  bizType?: string;
}

export interface OrdersQueryParams {
  pageSize?: number;
  pageIndex?: number;
  orderBy?: string;
  orderDirection?: string;
  groupBy?: string;
  needTotalCount?: boolean;
  refNo?: number;
  waybillNo?: string;
  providerCode?: string;
  status?: string;
  bizType?: string;
  startTime?: number;
  endTime?: number;
  senderName?: string;
  senderMobile?: string;
  receiverName?: string;
  receiverMobile?: string;
}

export interface RedeliverOrderParams {
  orderNumber: number;
  originalWaybillNo: string;
  providerCode: string;
  senderName: string;
  senderPhone: string;
  senderAddress: string;
  receiverName: string;
  receiverPhone: string;
  receiverAddress: string;
  packageItems: {
    skuId: number;
    qty: number;
  }[];
}

export interface LogisticsPackage {
  id?: number;
  logisticsOrderId?: number;
  waybillNo?: string;
  productName?: string;
  skuId?: number;
  qty?: number;
  weight?: number;
  length?: number;
  width?: number;
  height?: number;
  createTime?: number;
  updateTime?: number;
}

// 物流接口封装
export const logisticsApi = {
  // 统计分析相关接口
  getStatistics(params: StatisticsParams) {
    return request({
      url: '/client-web/api/logistics/analysis/statistics',
      method: 'get',
      params
    })
  },
  
  getExceptions(params: ExceptionsParams) {
    return request({
      url: '/client-web/api/logistics/analysis/exceptions',
      method: 'get',
      params
    })
  },
  
  getSla(params: SlaParams) {
    return request({
      url: '/client-web/api/logistics/analysis/sla',
      method: 'get',
      params
    })
  },
  
  // 物流订单相关接口
  getOrders(params: OrdersQueryParams) {
    return request({
      url: '/client-web/api/logistics/orders',
      method: 'get',
      params
    })
  },
  
  getOrderDetail(id: number) {
    return request({
      url: `/client-web/api/logistics/orders/${id}`,
      method: 'get'
    })
  },
  
  getOrderDetailByWaybill(waybillNo: string) {
    return request({
      url: `/client-web/api/logistics/orders/${waybillNo}/detail`,
      method: 'get'
    })
  },
  
  getOrderTracking(waybillNo: string) {
    return request({
      url: `/client-web/api/logistics/orders/${waybillNo}/tracking`,
      method: 'get'
    })
  },
  
  getOrdersByBizType(bizType: string) {
    return request({
      url: '/client-web/api/logistics/orders/by-biz-type',
      method: 'get',
      params: { bizType }
    })
  },
  
  createOrder(data: CreateLogisticsOrderParams) {
    return request({
      url: '/client-web/api/logistics/orders',
      method: 'post',
      data
    })
  },
  
  cancelOrder(id: number) {
    return request({
      url: `/client-web/api/logistics/orders/${id}/cancel`,
      method: 'put'
    })
  },
  
  redeliverOrder(data: RedeliverOrderParams) {
    return request({
      url: '/client-web/api/logistics/orders/redeliver',
      method: 'post',
      data
    })
  },
  
  // 包裹相关接口
  getPackages(logisticsOrderId: number) {
    return request({
      url: `/client-web/api/logistics/order/${logisticsOrderId}/packages`,
      method: 'get'
    })
  },
  
  getPackageDetail(packageId: number) {
    return request({
      url: `/client-web/api/logistics/package/${packageId}`,
      method: 'get'
    })
  },
  
  addPackage(logisticsOrderId: number, data: LogisticsPackage) {
    return request({
      url: `/client-web/api/logistics/order/${logisticsOrderId}/package`,
      method: 'post',
      data
    })
  }
}

// 包裹相关API
/**
 * 分页获取物流包裹列表
 * @method GET
 * @url /logistics/packages
 * @param params 请求参数，支持 { logisticsOrderId: number; pageIndex?: number; pageSize?: number }
 * @returns Promise<{ success: boolean; errCode: string; errMessage: string; data: LogisticsPackage[] }>
 */
export function getLogisticsPackageList(params: any) {
  return request({
    url: '/client-web/api/logistics/packages',
    method: 'get',
    params
  })
}

/**
 * 获取单个包裹详情
 * @method GET
 * @url /logistics/packages/{id}
 * @param id 包裹ID
 * @returns Promise<{ success: boolean; errCode: string; errMessage: string; data: LogisticsPackage }>
 */
export function getLogisticsPackage(id: number) {
  return request({
    url: `/client-web/api/logistics/packages/${id}`,
    method: 'get'
  })
}

/**
 * 新增物流包裹
 * @method POST
 * @url /logistics/packages
 * @param data 包裹信息
 * @returns Promise<{ success: boolean; errCode: string; errMessage: string; data: LogisticsPackage }>
 */
export function createLogisticsPackage(data: Partial<Package>) {
  return request({
    url: '/client-web/api/logistics/packages',
    method: 'post',
    data
  })
}

/**
 * 更新物流包裹
 * @method PUT
 * @url /logistics/packages/{id}
 * @param id 包裹ID
 * @param data 包裹更新信息
 * @returns Promise<{ success: boolean; errCode: string; errMessage: string; data: LogisticsPackage }>
 */
export function updateLogisticsPackage(id: number, data: Partial<Package>) {
  return request({
    url: `/client-web/api/logistics/packages/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除物流包裹
 * @method DELETE
 * @url /logistics/packages/{id}
 * @param id 包裹ID
 * @returns Promise<{ success: boolean; errCode: string; errMessage: string; data: any }>
 */
export function deleteLogisticsPackage(id: number) {
  return request({
    url: `/client-web/api/logistics/packages/${id}`,
    method: 'delete'
  })
}

// 订单相关API
/**
 * 分页获取物流订单列表
 * @method GET
 * @url /logistics/orders
 * @param params 请求参数，支持分页及筛选字段
 * @returns Promise<{ success: boolean; errCode: string; errMessage: string; data: LogisticsOrder[] }>
 */
export function getLogisticsOrderList(params: any) {
  return request({
    url: '/client-web/api/logistics/orders',
    method: 'get',
    params
  })
}

/**
 * 获取物流订单详情
 * @method GET
 * @url /logistics/orders/{id}
 * @param id 物流订单ID
 * @returns Promise<{ success: boolean; errCode: string; errMessage: string; data: LogisticsOrder }>
 */
export function getLogisticsOrder(id: number) {
  return request({
    url: `/client-web/api/logistics/orders/${id}`,
    method: 'get'
  })
}

/**
 * 新增物流订单
 * @method POST
 * @url /logistics/orders
 * @param data 订单信息
 * @returns Promise<{ success: boolean; errCode: string; errMessage: string; data: LogisticsOrder }>
 */
export function createLogisticsOrder(data: CreateLogisticsOrderParams) {
  return request({
    url: '/client-web/api/logistics/orders',
    method: 'post',
    data
  })
}

/**
 * 更新物流订单
 * @method PUT
 * @url /logistics/orders/{id}
 * @param id 物流订单ID
 * @param data 订单更新信息
 * @returns Promise<{ success: boolean; errCode: string; errMessage: string; data: LogisticsOrder }>
 */
export function updateLogisticsOrder(id: number, data: Partial<Order>) {
  return request({
    url: `/client-web/api/logistics/orders/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除物流订单
 * @method DELETE
 * @url /logistics/orders/{id}
 * @param id 物流订单ID
 * @returns Promise<{ success: boolean; errCode: string; errMessage: string; data: any }>
 */
export function deleteLogisticsOrder(id: number) {
  return request({
    url: `/client-web/api/logistics/orders/${id}`,
    method: 'delete'
  })
}

// 异常管理API
/**
 * 分页获取物流异常列表
 * @method GET
 * @url /logistics/exceptions
 * @param params 请求参数，支持分页及筛选
 * @returns Promise<{ success: boolean; errCode: string; errMessage: string; data: LogisticsException[] }>
 */
export function getLogisticsExceptionList(params: any) {
  return request({
    url: '/client-web/api/logistics/exceptions',
    method: 'get',
    params
  })
}

/**
 * 获取单个物流异常详情
 * @method GET
 * @url /logistics/exceptions/{id}
 * @param id 异常ID
 * @returns Promise<{ success: boolean; errCode: string; errMessage: string; data: LogisticsException }>
 */
export function getLogisticsException(id: number) {
  return request({
    url: `/client-web/api/logistics/exceptions/${id}`,
    method: 'get'
  })
}

/**
 * 新增物流异常
 * @method POST
 * @url /logistics/exceptions
 * @param data 异常信息
 * @returns Promise<{ success: boolean; errCode: string; errMessage: string; data: LogisticsException }>
 */
export function createLogisticsException(data: Partial<Exception>) {
  return request({
    url: '/client-web/api/logistics/exceptions',
    method: 'post',
    data
  })
}

/**
 * 更新物流异常
 * @method PUT
 * @url /logistics/exceptions/{id}
 * @param id 异常ID
 * @param data 异常更新信息
 * @returns Promise<{ success: boolean; errCode: string; errMessage: string; data: LogisticsException }>
 */
export function updateLogisticsException(id: number, data: Partial<Exception>) {
  return request({
    url: `/client-web/api/logistics/exceptions/${id}`,
    method: 'put',
    data
  })
}

/**
 * 处理物流异常
 * @method POST
 * @url /logistics/exceptions/{id}/resolve
 * @param id 异常ID
 * @param remark 处理备注
 * @returns Promise<{ success: boolean; errCode: string; errMessage: string; data: any }>
 */
export function resolveLogisticsException(id: number, remark: string) {
  return request({
    url: `/client-web/api/logistics/exceptions/${id}/resolve`,
    method: 'post',
    data: { remark }
  })
}

// SLA相关API
/**
 * 分页获取SLA监控列表
 * @method GET
 * @url /logistics/slas
 * @param params 请求参数，支持分页及筛选
 * @returns Promise<{ success: boolean; errCode: string; errMessage: string; data: LogisticsSLA[] }>
 */
export function getLogisticsSLAList(params: any) {
  return request({
    url: '/client-web/api/logistics/slas',
    method: 'get',
    params
  })
}

/**
 * 获取单个SLA详情
 * @method GET
 * @url /logistics/slas/{id}
 * @param id SLA ID
 * @returns Promise<{ success: boolean; errCode: string; errMessage: string; data: LogisticsSLA }>
 */
export function getLogisticsSLA(id: number) {
  return request({
    url: `/client-web/api/logistics/slas/${id}`,
    method: 'get'
  })
}

/**
 * 更新SLA数据
 * @method PUT
 * @url /logistics/slas/{id}
 * @param id SLA ID
 * @param data 更新字段
 * @returns Promise<{ success: boolean; errCode: string; errMessage: string; data: LogisticsSLA }>
 */
export function updateLogisticsSLA(id: number, data: Partial<SLA>) {
  return request({
    url: `/client-web/api/logistics/slas/${id}`,
    method: 'put',
    data
  })
} 