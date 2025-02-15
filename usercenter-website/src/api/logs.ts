import request from './config'

export interface LogEntry {
  id: number
  userId: number
  username: string
  operation: string
  module: string
  description: string
  ip: string
  status: string
  createTime: string
}

export interface LogPageQuery {
  pageNum: number
  pageSize: number
  userId?: number
  operation?: string
  startTime?: string
  endTime?: string
}

export interface LogPageResponse {
  data: LogEntry[]
  total: number
}

export const logsApi = {
  // 分页查询操作日志
  page: (params: LogPageQuery) =>
    request.post<LogPageResponse>('/logs/page', params)
}