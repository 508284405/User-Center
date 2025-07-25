import request from '../config';
import { AfterSaleStatus } from '@/types/afterSaleStatus'
import { AfterSaleType } from '@/types/afterSaleType'
import { AfterSaleRefundItem } from './afterSaleRefund'

export interface AfterSalesItem {
  id: string;
  orderNo: string;
  userId: string;
  username: string;
  afterSaleNo: string;
  orderNumber?: string;
  applyTime?: number;
  refundAmount?: number;
  phone: string;
  reason: string;
  status: AfterSaleStatus;
  afterSaleType: AfterSaleType;
  createTime: string;
  updateTime: string;
  applyAmount: number;
  attachments: string[];
  description: string;
  // 商品信息
  productId: number;
  skuId: number;
  productName: string;
  skuSpec: any;
  skuImage: string;
  quantity: number;
  isGift: boolean;
  logs?: Array<{
    id: number;
    afterSaleNo: string;
    operatorId: number;
    operatorName: string;
    operatorRole: string;
    action: string;
    comment: string;
    logTime: string;
  }>;
  deliveries?: Array<{
    id: number;
    afterSaleNo: string;
    direction: string;
    expressCompany: string;
    trackingNumber: string;
    deliveryTime: number;
    receiveTime: number;
  }>;
  refund?: AfterSaleRefundItem;
  files?: Array<{
    id: number;
    afterSaleNo: string;
    fileUrl: string;
    fileType: string;
    uploadTime: number;
  }>;
}

export interface AfterSalesQuery {
  keyword?: string;
  status?: AfterSaleStatus | '';
  afterSaleType?: AfterSaleType;
  startTime?: string;
  endTime?: string;
  pageNum: number;
  pageSize: number;
}

export interface ApproveParams {
  afterSaleNo: string;
  remark?: string;
}

export interface RejectParams {
  afterSaleNo: string;
  reason: string;
}

// 售后管理API
export const afterSalesApi = {
  // 获取售后列表（管理端分页查询）
  getAfterSalesList(params: AfterSalesQuery) {
    return request({
      url: '/client-web/api/aftersale/admin/page',
      method: 'get',
      params: {
        pageSize: params.pageSize,
        pageIndex: params.pageNum,
        orderNumber: params.keyword,
        status: params.status,
        afterSaleType: params.afterSaleType,
        needTotalCount: true,
        startTime: params.startTime ? new Date(params.startTime).getTime() : undefined,
        endTime: params.endTime ? new Date(params.endTime).getTime() : undefined
      }
    })
  },
  
  // 获取售后详情
  getAfterSalesDetail(id: string) {
    return request({
      url: '/client-web/api/aftersale/admin/detail',
      method: 'get',
      params: { afterSaleNo: id }
    })
  },
  
  // 审核通过
  approveAfterSales(data: ApproveParams) {
    return request({
      url: '/client-web/api/aftersale/admin/audit',
      method: 'post',
      data: {
        afterSaleNo: data.afterSaleNo,
        auditResult: 'APPROVE',
        comment: data.remark || ''
      }
    })
  },
  
  // 审核拒绝
  rejectAfterSales(data: RejectParams) {
    return request({
      url: '/client-web/api/aftersale/admin/audit',
      method: 'post',
      data: {
        afterSaleNo: data.afterSaleNo,
        auditResult: 'REJECT',
        comment: data.reason
      }
    })
  },
  
  // 取消售后
  cancelAfterSales(id: string) {
    return request({
      url: `/client-web/api/aftersale/admin/cancel/${id}`,
      method: 'post'
    })
  },
  
  // 分配客服
  assignCustomerService(id: string, serviceId: string) {
    return request({
      url: '/client-web/api/aftersale/admin/assign',
      method: 'post',
      data: {
        id,
        serviceId
      }
    })
  },
  
  // 上传售后凭证
  uploadAttachment(formData: FormData) {
    return request({
      url: '/client-web/api/aftersale/admin/upload',
      method: 'post',
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      data: formData
    })
  },
  
  // 导出售后数据
  exportAfterSales(params: AfterSalesQuery) {
    return request({
      url: '/client-web/api/aftersale/admin/export',
      method: 'get',
      params,
      responseType: 'blob'
    })
  },
  
  // 分页查询退款记录（管理端）
  pageRefundRecords(params: {
    pageSize?: number;
    pageIndex?: number;
    refundNo?: string;
    afterSaleNo?: string;
    refundStatus?: string;
    startTime?: number;
    endTime?: number;
    needTotalCount?: boolean;
  }) {
    return request({
      url: '/client-web/api/aftersale/admin/refund/page',
      method: 'get',
      params
    })
  },
  
  // 查询退款详情 by ID（管理端）
  getRefundDetailById(refundNo: string) {
    return request({
      url: `/client-web/api/aftersale/admin/refund/${refundNo}`,
      method: 'get'
    })  
  },
  
  // 根据售后单号查询退款详情（管理端）
  getRefundDetailByAfterSaleNo(afterSaleNo: string) {
    return request({
      url: `/client-web/api/aftersale/admin/refund/afterSale/${afterSaleNo}`,
      method: 'get'
    })
  },
  
  // 处理退款（同意）
  processRefund(refundNo: string) {
    return request({
      url: `/client-web/api/aftersale/admin/refund/${refundNo}/process`,
      method: 'post'
    })
  },
  
  // 拒绝退款
  rejectRefund(refundNo: string, reason: string) {
    return request({
      url: `/client-web/api/aftersale/admin/refund/${refundNo}/reject`,
      method: 'post',
      params: { reason }
    })
  }
} 