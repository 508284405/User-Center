import request from '../config';

// API响应的通用接口
export interface ApiResponse<T> {
  success: boolean;
  errCode?: string;
  errMessage?: string;
  data: T;
}

// 商品管理相关接口
export interface Product {
  id?: number;
  name: string;
  description?: string;
  price: number; // 原价
  images: string[];
  category: string;
  categoryId: string | number;
  status: number; // 0: 草稿, 1: 上架, 2: 下架
  warningStock: number;
  tags: string[];
  specifications: string[];
  skus: ProductSku[];
  createdAt?: string;
  updatedAt?: string;
}

// SKU信息
export interface ProductSku {
  skuId: string;
  attributes: Record<string, string>;
  price: number;
  stock: number; // 总库存
  availableStock: number; // 可用库存
  frozenStock: number; // 冻结库存
  image?: string; // SKU图片
  description?: string; // SKU描述，不超过50个字
  weight: number; // 重量
  volume: number; // 体积
}

// 创建商品
export const createProduct = (data: Product): Promise<ApiResponse<Product>> => {
  return request.post('/client-web/api/admin/products', data);
};

// 更新商品
export const updateProduct = (id: number, data: Product): Promise<ApiResponse<Product>> => {
  return request.put(`/client-web/api/admin/products/${id}`, data);
};

// 删除商品
export const deleteProduct = (id: number): Promise<ApiResponse<void>> => {
  return request.delete(`/client-web/api/admin/products/${id}`);
};

// 获取商品详情
export const getProduct = (id: number): Promise<ApiResponse<Product>> => {
  return request.get(`/client-web/api/admin/products/${id}`);
};

// 分页查询商品列表
export interface ProductQuery {
  pageSize: number;
  pageIndex: number;
  orderBy?: string;
  orderDirection?: string;
  groupBy?: string;
  needTotalCount?: boolean;
  name?: string;
  categoryId?: string;
  minPrice?: number;
  maxPrice?: number;
  status?: number;
  createBy?: string;
  updateBy?: string;
}

export interface ProductListResponse extends ApiResponse<Product[]> {
  totalCount: number;
  pageSize: number;
  pageIndex: number;
}

export const getProductList = (params: ProductQuery): Promise<ProductListResponse> => {
  return request.post('/client-web/api/admin/products/page', params);
};

// 商品上架
export const onShelfProduct = (id: number): Promise<ApiResponse<void>> => {
  return request.put(`/client-web/api/admin/products/${id}/on-shelf`);
};

// 商品下架
export const offShelfProduct = (id: number): Promise<ApiResponse<void>> => {
  return request.put(`/client-web/api/admin/products/${id}/off-shelf`);
};

// 更新商品状态（上下架）
export const updateProductStatus = (id: number, status: number): Promise<ApiResponse<void>> => {
  return status === 1 ? onShelfProduct(id) : offShelfProduct(id);
};

// 更新SKU库存
export const updateSkuInventory = (productId: number, skuId: number, stock: number): Promise<ApiResponse<void>> => {
  return request.put(`/client-web/api/admin/products/${productId}/skus/${skuId}/inventory`, { stock });
};