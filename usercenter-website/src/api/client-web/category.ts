import request from '../config';

// 获取分类树
export const getCategoryTree = () => {
  return request.get('/client-web/api/admin/categories/tree');
};

// 创建分类
export const createCategory = (data: {
  name: string;
  parentId?: string;
  sort?: number;
}) => {
  return request.post('/client-web/api/admin/categories', data);
};

// 更新分类
export const updateCategory = (id: string, data: {
  name?: string;
  sort?: number;
}) => {
  return request.put(`/client-web/api/admin/categories/${id}`, data);
};

// 删除分类
export const deleteCategory = (id: string) => {
  return request.delete(`/client-web/api/admin/categories/${id}`);
};

// 移动分类
export const moveCategory = (id: string, data: {
  categoryId: string;
  targetParentId: string;
}) => {
  return request.post(`/client-web/api/admin/categories/${id}/move`, data);
};

// 分类接口返回数据类型
export interface Category {
  categoryId: string;
  name: string;
  parentId: string;
  level: number;
  sort: number;
  isLeaf: boolean;
  children?: Category[];
}