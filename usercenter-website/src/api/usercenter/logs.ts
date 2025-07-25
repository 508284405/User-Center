import request from '../config'

export interface LogEntry {
  id: number
  userId: number
  username: string
  operation?: string
  operationType: string
  module: string
  targetId: number
  description: string
  operationContent: Record<string, any>
  ip?: string
  ipAddress: string
  status?: string
  operationResult: string
  relatedOperationId: number
  severityLevel: string
  isSensitive: number
  approvalInfo: Record<string, any>
  createTime?: string
  createdAt: string
  createdBy: string
  updatedAt: string
  extendField1: string
  extendField2: string
  extendField3: string
}

export interface LogPageQuery {
  pageNum?: number
  pageIndex?: number
  pageSize?: number
  orderBy?: string
  orderDirection?: string
  groupBy?: string
  needTotalCount?: boolean
  userId?: number
  username?: string
  operationType?: string
  operation?: string
  module?: string
  startTime?: string
  endTime?: string
  ipAddress?: string
  operationResult?: string
  severityLevel?: string
  onlySensitive?: boolean
  relatedOperationId?: number
  description?: string
  targetId?: number
}

export interface LogPageResponse {
  data: LogEntry[]
  total: number
}

export interface StatisticData {
  id: number
  statisticDate: string
  module: string
  operationType: string
  userCount: number
  operationCount: number
  successCount: number
  failCount: number
  createdAt: string
  updatedAt: string
}

export interface ApiResponse<T> {
  success: boolean
  errCode: string
  errMessage: string
  data: T
  totalCount?: number
  pageSize?: number
  pageIndex?: number
}

export const logsApi = {
  // 分页查询操作日志
  page: (params: LogPageQuery) =>
    request.get<ApiResponse<LogEntry[]>>('/client-web/operation-logs', { params }),

  // 获取操作日志详情
  getDetail: (id: number) =>
    request.get<ApiResponse<LogEntry>>(`/usercenter/api/operation-logs/${id}`),

  // 获取操作链
  getChain: (relatedOperationId: number) =>
    request.get<ApiResponse<LogEntry[]>>(`/usercenter/api/operation-logs/chain/${relatedOperationId}`),

  // 获取用户操作统计
  getUserStatistics: (userId: number, params?: { startTime?: string, endTime?: string }) =>
    request.get<ApiResponse<Record<string, any>>>(`/usercenter/api/operation-logs/statistics/user/${userId}`, { params }),

  // 获取模块操作统计
  getModuleStatistics: (params: { modules: string[], startTime?: string, endTime?: string }) =>
    request.get<ApiResponse<StatisticData[]>>('/usercenter/api/operation-logs/statistics/module', { params }),

  // 获取操作类型统计
  getOperationTypeStatistics: (params: { operationTypes: string[], startTime?: string, endTime?: string }) =>
    request.get<ApiResponse<StatisticData[]>>('/usercenter/api/operation-logs/statistics/operation-type', { params }),

  // 导出操作日志
  export: (format: 'CSV' | 'EXCEL' | 'PDF', params: LogPageQuery) =>
    request.post<ApiResponse<string>>(`/usercenter/api/operation-logs/export?format=${format}`, params),

  // 归档操作日志
  archive: (params: { startTime: string, endTime: string }) =>
    request.post<ApiResponse<void>>('/usercenter/api/operation-logs/archive', params),

  // 清理过期操作日志
  clean: (retentionDays: number) =>
    request.post<ApiResponse<void>>('/usercenter/api/operation-logs/clean', { retentionDays }),

  // 获取资源操作历史
  getHistory: (params: { module: string, targetId: number }) =>
    request.get<ApiResponse<LogEntry[]>>('/usercenter/api/operation-logs/history', { params }),

  // 检测异常操作行为
  detectAbnormal: (userId: number, timeRangeMinutes: number) =>
    request.get<ApiResponse<LogEntry[]>>(`/usercenter/api/operation-logs/abnormal/${userId}?timeRangeMinutes=${timeRangeMinutes}`)
}