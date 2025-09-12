import request from '../config';

// API响应的通用接口
export interface ApiResponse<T> {
  success: boolean;
  errCode?: string;
  errMessage?: string;
  data: T;
}

// 分页结果接口
export interface PageResult<T> {
  pageNo: number;
  pageSize: number;
  total: number;
  data: T[];
}

// 商家相关接口
export interface Merchant {
  id?: number;
  merchantNo?: string;
  merchantName: string;
  legalName: string;
  contactPhone: string;
  contactEmail: string;
  businessLicense: string;
  status?: string;
  rejectReason?: string;
  commissionRate?: number;
  createdAt?: number;
  updatedAt?: number;
}

// 店铺相关接口
export interface Shop {
  id?: number;
  shopNo?: string;
  shopName: string;
  merchantNo?: string;
  description?: string;
  logoUrl?: string;
  address?: string;
  contactPhone?: string;
  status?: string;
  createdAt?: number;
  updatedAt?: number;
}

// 商家入驻命令
export interface MerchantOnboardCmd {
  merchantName: string;
  legalName: string;
  contactPhone: string;
  contactEmail: string;
  businessLicense: string;
}

// 商家审批命令
export interface MerchantApprovalCmd {
  merchantNo: string;
  approved: boolean;
  rejectReason?: string;
  commissionRate?: number;
}

// 店铺创建命令
export interface ShopCreateCmd {
  shopName: string;
  description?: string;
  logoUrl?: string;
  address?: string;
  contactPhone?: string;
}

// 商家分页查询参数
export interface MerchantPageQuery {
  pageNo: number;
  pageSize: number;
  status?: string;
  merchantName?: string;
}

// 商家入驻申请
export const onboardMerchant = (data: MerchantOnboardCmd): Promise<ApiResponse<string>> => {
  return request.post('/client-web/api/merchant/onboard', data);
};

// 商家审批
export const approveMerchant = (data: MerchantApprovalCmd): Promise<ApiResponse<void>> => {
  return request.post('/client-web/api/admin/merchant/approve', data);
};

// 获取商家档案
export const getMerchantProfile = (): Promise<ApiResponse<Merchant>> => {
  return request.get('/client-web/api/merchant/profile');
};

// 根据商家编号获取商家信息
export const getMerchantByNo = (merchantNo: string): Promise<ApiResponse<Merchant>> => {
  return request.get(`/client-web/api/admin/merchant/${merchantNo}`);
};

// 分页查询商家列表
export const pageMerchants = (params: MerchantPageQuery): Promise<ApiResponse<PageResult<Merchant>>> => {
  return request.post('/client-web/api/admin/merchant/page', params);
};

// 创建店铺
export const createShop = (data: ShopCreateCmd): Promise<ApiResponse<string>> => {
  return request.post('/client-web/api/merchant/shops', data);
};

// 获取我的店铺列表
export const getMyShops = (): Promise<ApiResponse<Shop[]>> => {
  return request.get('/client-web/api/merchant/shops');
};

// 根据商家编号获取店铺列表
export const getShopsByMerchant = (merchantNo: string): Promise<ApiResponse<Shop[]>> => {
  return request.get(`/client-web/api/admin/merchant/${merchantNo}/shops`);
};

// 根据店铺编号获取店铺信息
export const getShopByNo = (shopNo: string): Promise<ApiResponse<Shop>> => {
  return request.get(`/client-web/api/merchant/shops/${shopNo}`);
};

// 商家状态枚举
export const MerchantStatus = {
  PENDING: { value: 'PENDING', label: '待审核', color: 'warning' },
  APPROVED: { value: 'APPROVED', label: '已通过', color: 'success' },
  REJECTED: { value: 'REJECTED', label: '已拒绝', color: 'danger' },
  SUSPENDED: { value: 'SUSPENDED', label: '已暂停', color: 'info' },
  FROZEN: { value: 'FROZEN', label: '已冻结', color: 'danger' }
};

// 店铺状态枚举
export const ShopStatus = {
  ACTIVE: { value: 'ACTIVE', label: '激活', color: 'success' },
  INACTIVE: { value: 'INACTIVE', label: '未激活', color: 'info' },
  SUSPENDED: { value: 'SUSPENDED', label: '已暂停', color: 'warning' }
};

// 商家分析数据接口
export interface MerchantAnalytics {
  merchantNo: string;
  merchantName: string;
  totalShops: number;
  totalProducts: number;
  totalOrders: number;
  totalRevenue: number;
  totalCommission: number;
  monthlyRevenue: number;
  monthlyCommission: number;
  monthlyOrders: number;
  lastLoginTime: number;
  activeProductCount: number;
  conversionRate: number;
  revenueTrend: RevenueDataPoint[];
  orderTrend: OrderDataPoint[];
}

export interface RevenueDataPoint {
  date: string;
  revenue: number;
  commission: number;
}

export interface OrderDataPoint {
  date: string;
  orderCount: number;
  completedCount: number;
}

// 收入报表查询参数
export interface MerchantRevenueReportQuery {
  merchantNo?: string;
  startTime: number;
  endTime: number;
  timeRange: 'DAY' | 'WEEK' | 'MONTH';
  reportType: 'REVENUE' | 'COMMISSION' | 'ORDERS';
}

// 批量操作命令
export interface MerchantBatchCmd {
  merchantNos: string[];
  action: 'APPROVE' | 'REJECT' | 'SUSPEND' | 'ACTIVATE' | 'UPDATE_COMMISSION';
  reason?: string;
  commissionRate?: number;
}

// 获取商家分析数据
export const getMerchantAnalytics = (merchantNo: string): Promise<ApiResponse<MerchantAnalytics>> => {
  return request.get(`/client-web/api/admin/merchant/${merchantNo}/analytics`);
};

// 分页查询商家分析列表
export const getMerchantAnalyticsList = (params: MerchantPageQuery): Promise<ApiResponse<PageResult<MerchantAnalytics>>> => {
  return request.post('/client-web/api/admin/merchant/analytics/list', params);
};

// 获取收入报表
export const getMerchantRevenueReport = (params: MerchantRevenueReportQuery): Promise<ApiResponse<RevenueDataPoint[]>> => {
  return request.post('/client-web/api/admin/merchant/revenue/report', params);
};

// 批量处理商家
export const batchProcessMerchants = (data: MerchantBatchCmd): Promise<ApiResponse<void>> => {
  return request.post('/client-web/api/admin/merchant/batch', data);
};