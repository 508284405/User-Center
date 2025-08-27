// SmartCS 管理控制台 API 接口
import request from '../config'

// API 基础路径
const API_BASE = '/smartcs/api/admin'

// 意图管理接口
export const intentApi = {
  // 分页查询意图列表
  getIntentPage(params: any) {
    return request.get(`${API_BASE}/intent/intents`, { params })
  },

  // 获取意图详情
  getIntentDetail(intentId: string | number) {
    return request.get(`${API_BASE}/intent/intents/${intentId}`)
  },

  // 创建意图
  createIntent(data: any) {
    return request.post(`${API_BASE}/intent/intents`, data)
  },

  // 更新意图
  updateIntent(intentId: string | number, data: any) {
    return request.put(`${API_BASE}/intent/intents/${intentId}`, data)
  },

  // 删除意图
  deleteIntent(intentId: string | number) {
    return request.delete(`${API_BASE}/intent/intents/${intentId}`)
  },

  // 获取意图版本列表
  getIntentVersions(intentId: string | number) {
    return request.get(`${API_BASE}/intent/intents/${intentId}/versions`)
  },

  // 激活意图版本
  activateIntentVersion(intentId: string | number, versionId: string | number) {
    return request.post(`${API_BASE}/intent/intents/${intentId}/version/${versionId}/activate`)
  },

  // 获取分类目录列表
  getCatalogList() {
    return request.get(`${API_BASE}/intent/catalog/list`)
  },

  // 更新意图标签
  updateIntentLabels(intentId: string | number, data: any) {
    return request.put(`${API_BASE}/intent/intents/${intentId}/labels`, data)
  },

  // 更新意图边界
  updateIntentBoundaries(intentId: string | number, data: any) {
    return request.put(`${API_BASE}/intent/intents/${intentId}/boundaries`, data)
  }
}

// 目录管理接口
export const catalogApi = {
  // 分页查询目录列表
  getCatalogPage(params: any) {
    return request.get(`${API_BASE}/intent/catalogs`, { params })
  },

  // 获取目录列表（不分页）
  getCatalogList() {
    return request.get(`${API_BASE}/intent/catalog/list`)
  },

  // 获取目录详情
  getCatalogDetail(catalogId: string | number) {
    return request.get(`${API_BASE}/intent/catalogs/${catalogId}`)
  },

  // 创建目录
  createCatalog(data: any) {
    return request.post(`${API_BASE}/intent/catalogs`, data)
  },

  // 更新目录
  updateCatalog(catalogId: string | number, data: any) {
    return request.put(`${API_BASE}/intent/catalogs/${catalogId}`, data)
  },

  // 删除目录
  deleteCatalog(catalogId: string | number) {
    return request.delete(`${API_BASE}/intent/catalogs/${catalogId}`)
  },

  // 获取目录树结构
  getCatalogTree() {
    return request.get(`${API_BASE}/intent/catalog/tree`)
  }
}

// 版本管理接口
export const versionApi = {
  // 创建意图版本
  createVersion(intentId: string | number, data: any) {
    return request.post(`${API_BASE}/intent/intents/${intentId}/versions`, data)
  },

  // 查询版本列表
  getVersionList(intentId: string | number, params: any) {
    return request.get(`${API_BASE}/intent/intents/${intentId}/versions`, { params })
  },

  // 发布版本
  publishVersion(versionId: string | number) {
    return request.post(`${API_BASE}/intent/versions/${versionId}/publish`)
  },

  // 下线版本
  offlineVersion(versionId: string | number) {
    return request.post(`${API_BASE}/intent/versions/${versionId}/offline`)
  }
}

// 策略管理接口
export const policyApi = {
  // 创建意图策略
  createPolicy(intentId: string | number, data: any) {
    return request.post(`${API_BASE}/intent/intents/${intentId}/policies`, data)
  },

  // 更新意图策略
  updatePolicy(policyId: string | number, data: any) {
    return request.put(`${API_BASE}/intent/policies/${policyId}`, data)
  },

  // 查询策略列表
  getPolicyList(intentId: string | number, params: any) {
    return request.get(`${API_BASE}/intent/intents/${intentId}/policies`, { params })
  },

  // 删除意图策略
  deletePolicy(policyId: string | number) {
    return request.delete(`${API_BASE}/intent/policies/${policyId}`)
  }
}

// 路由管理接口
export const routeApi = {
  // 创建意图路由
  createRoute(intentId: string | number, data: any) {
    return request.post(`${API_BASE}/intent/intents/${intentId}/routes`, data)
  },

  // 更新意图路由
  updateRoute(routeId: string | number, data: any) {
    return request.put(`${API_BASE}/intent/routes/${routeId}`, data)
  },

  // 查询路由列表
  getRouteList(intentId: string | number, params: any) {
    return request.get(`${API_BASE}/intent/intents/${intentId}/routes`, { params })
  },

  // 删除意图路由
  deleteRoute(routeId: string | number) {
    return request.delete(`${API_BASE}/intent/routes/${routeId}`)
  }
}

// 样本管理接口
export const sampleApi = {
  // 创建意图样本
  createSample(intentId: string | number, data: any) {
    return request.post(`${API_BASE}/intent/intents/${intentId}/samples`, data)
  },

  // 批量导入样本
  batchImportSamples(intentId: string | number, data: any) {
    return request.post(`${API_BASE}/intent/intents/${intentId}/samples/batch-import`, data)
  },

  // 查询样本列表
  getSampleList(intentId: string | number, params: any) {
    return request.get(`${API_BASE}/intent/intents/${intentId}/samples`, { params })
  },

  // 删除意图样本
  deleteSample(sampleId: string | number) {
    return request.delete(`${API_BASE}/intent/samples/${sampleId}`)
  },

  // 批量删除样本
  batchDeleteSamples(data: any) {
    return request.post(`${API_BASE}/intent/samples/batch-delete`, data)
  }
}

// 意图分类规则管理接口
export const classificationRuleApi = {
  // 分页查询规则
  getRulePage(params: any) {
    return request.get(`${API_BASE}/intent/classification-rule/page`, { params })
  },
  // 获取规则详情
  getRuleDetail(id: string | number) {
    return request.get(`${API_BASE}/intent/classification-rule/${id}`)
  },
  // 创建规则
  createRule(data: any) {
    return request.post(`${API_BASE}/intent/classification-rule`, data)
  },
  // 更新规则
  updateRule(id: string | number, data: any) {
    return request.put(`${API_BASE}/intent/classification-rule/${id}`, data)
  },
  // 删除规则
  deleteRule(id: string | number) {
    return request.delete(`${API_BASE}/intent/classification-rule/${id}`)
  },
  // 启用/停用
  toggleRuleStatus(id: string | number, enabled: boolean) {
    return request.post(`${API_BASE}/intent/classification-rule/${id}/${enabled ? 'enable' : 'disable'}`)
  }
}

// 快照管理接口
export const snapshotApi = {
  // 分页查询快照列表
  getSnapshotPage(params: any) {
    return request.get(`${API_BASE}/intent/snapshot`, { params })
  },

  // 获取快照详情
  getSnapshotDetail(snapshotId: string | number) {
    return request.get(`${API_BASE}/intent/snapshot/${snapshotId}`)
  },

  // 创建快照
  createSnapshot(data: any) {
    return request.post(`${API_BASE}/intent/snapshot/create`, data)
  },

  // 发布快照
  publishSnapshot(snapshotId: string | number) {
    return request.post(`${API_BASE}/intent/snapshot/${snapshotId}/publish`)
  },

  // 删除快照
  deleteSnapshot(snapshotId: string | number) {
    return request.delete(`${API_BASE}/intent/snapshot/${snapshotId}`)
  },

  // 导出快照配置
  exportSnapshotConfig(snapshotId: string | number) {
    return request.get(`${API_BASE}/intent/snapshot/${snapshotId}/export`)
  },

  // 回滚快照
  rollbackSnapshot(snapshotId: string | number) {
    return request.post(`${API_BASE}/intent/snapshot/${snapshotId}/rollback`)
  }
}

// 分类测试接口
export const classificationApi = {
  // 测试意图分类
  testClassification(data: any) {
    return request.post('/smartcs/api/intent/classify', data)
  },

  // 批量测试分类
  batchTestClassification(data: any) {
    return request.post('/smartcs/api/intent/classify/batch', data)
  },

  // 获取运行时配置
  getRuntimeConfig(params: any) {
    return request.get('/smartcs/api/intent/runtime/config', { params })
  }
}

// 槽位模板管理接口
export const slotTemplateApi = {
  // 获取意图的槽位模板
  getSlotTemplate(intentId: string | number) {
    return request.get(`${API_BASE}/intent/intents/${intentId}/slot-template`)
  },

  // 更新意图的槽位模板
  updateSlotTemplate(intentId: string | number, data: any) {
    return request.put(`${API_BASE}/intent/intents/${intentId}/slot-template`, data)
  },

  // 删除意图的槽位模板
  deleteSlotTemplate(intentId: string | number) {
    return request.delete(`${API_BASE}/intent/intents/${intentId}/slot-template`)
  },

  // 测试槽位填充
  testSlotFilling(intentId: string | number, data: any) {
    return request.post(`${API_BASE}/intent/intents/${intentId}/slot-template/test`, data)
  },

  // 获取所有槽位模板
  getAllSlotTemplates(params?: any) {
    return request.get(`${API_BASE}/intent/slot-templates`, { params })
  },

  // 根据意图编码获取槽位模板
  getSlotTemplateByIntentCode(intentCode: string, params?: any) {
    return request.get(`${API_BASE}/intent/slot-templates/by-intent/${intentCode}`, { params })
  }
}

// 通用工具函数
export const utils = {
  // 格式化日期
  formatDate(timestamp: number | string | null): string {
    if (!timestamp) return ''
    const date = new Date(timestamp)
    return date.toLocaleString('zh-CN')
  },

  // 获取状态徽章样式
  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'success'
      case 'INACTIVE':
        return 'secondary'
      case 'DRAFT':
        return 'warning'
      default:
        return 'secondary'
    }
  },

  // 获取状态文本
  getStatusText(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return '激活'
      case 'INACTIVE':
        return '停用'
      case 'DRAFT':
        return '草稿'
      default:
        return '未知'
    }
  }
}
