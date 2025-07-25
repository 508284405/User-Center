import request from '../config';

// API响应接口
interface ApiResponse<T> {
  success: boolean;
  errCode?: string;
  errMessage?: string;
  data?: T;
  totalCount?: number;
  pageSize?: number;
  pageIndex?: number;
}

/**
 * 分页查询Prompt模板
 */
export function getPageTemplates(params: {
  pageIndex: number;
  pageSize: number;
  templateKey?: string;
}) {
  return request({
    url: '/smartcs/api/admin/bot/prompt-template/page',
    method: 'GET',
    params
  });
}

/**
 * 新增Prompt模板
 */
export function createTemplate(data: {
  templateKey: string;
  templateContent: string;
}) {
  return request({
    url: '/smartcs/api/admin/bot/prompt-template',
    method: 'POST',
    data
  });
}

/**
 * 更新Prompt模板
 */
export function updateTemplate(data: {
  id: number;
  templateContent: string;
}) {
  return request({
    url: '/smartcs/api/admin/bot/prompt-template/update',
    method: 'POST',
    data
  });
}

/**
 * 获取Prompt模板列表
 */
export function listTemplates(params?: {
  templateKey?: string;
  context?: string;
}): Promise<ApiResponse<any[]>> {
  return request({
    url: '/smartcs/api/admin/bot/prompt-template/list',
    method: 'GET',
    params
  });
}

/**
 * 删除Prompt模板
 */
export function deleteTemplate(id: number) {
  return request({
    url: `/smartcs/api/admin/bot/prompt-template/${id}`,
    method: 'DELETE'
  });
}

/**
 * 根据ID查询模板详情
 */
export function getTemplateDetail(id: number) {
  return request({
    url: `/smartcs/api/admin/bot/prompt-template/${id}`,
    method: 'GET'
  });
}