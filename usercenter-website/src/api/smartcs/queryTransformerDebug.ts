import request from '../config'

export interface TraceRequest {
  modelId?: number
  query: string
}

export interface StageTrace {
  stage: string
  before: string[]
  after: string[]
  added: string[]
  removed: string[]
  unchanged: string[]
  elapsedMs: number
  note?: string
}

export interface QueryTransformationTrace {
  originalQuery: string
  finalQueries: string[]
  stages: StageTrace[]
}

export interface TraceResponse {
  data: QueryTransformationTrace
  success: boolean
  errCode?: string
  errMessage?: string
}

export const queryTransformerDebugAPI = {
  /**
   * 执行查询转换调试追踪
   */
  trace(params: TraceRequest): Promise<TraceResponse> {
    return request.post('/api/debug/query-transformer/trace', params)
  }
}