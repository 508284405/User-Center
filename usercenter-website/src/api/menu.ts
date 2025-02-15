import request from './config';

// 菜单接口返回类型
interface MenuResponse {
  id: number;
  menuName: string;
  menuCode: string;
  systemId: number;
  parentId: number;
  path: string;
  component: string;
  sort: number;
}

// 创建菜单参数类型
interface CreateMenuParams {
  menuName: string;
  menuCode: string;
  systemId: number;
  parentId: number;
  path: string;
  component: string;
  sort: number;
}

// 更新菜单参数类型
interface UpdateMenuParams {
  menuName: string;
  path: string;
  component: string;
  sort: number;
}

// 分页查询参数类型
interface MenuPageParams {
  pageNum: number;
  pageSize: number;
  menuName?: string;
  menuCode?: string;
  systemId?: number;
}

// 分页查询返回类型
interface MenuPageResponse {
  total: number;
  data: MenuResponse[];
}

// 菜单管理相关接口
export const menuApi = {
  // 创建菜单
  create: (params: CreateMenuParams) => {
    return request.post<any, MenuResponse>('/menus', params);
  },

  // 更新菜单
  update: (id: number, params: UpdateMenuParams) => {
    return request.put<any, MenuResponse>(`/menus/${id}`, params);
  },

  // 删除菜单
  delete: (id: number) => {
    return request.delete<any, void>(`/menus/${id}`);
  },

  // 根据ID查询菜单
  getById: (id: number) => {
    return request.get<any, MenuResponse>(`/menus/${id}`);
  },

  // 查询系统下的所有菜单
  getBySystem: (systemId: number) => {
    return request.get<any, MenuResponse[]>(`/menus/system/${systemId}`);
  },

  // 查询子菜单
  getByParent: (parentId: number) => {
    return request.get<any, MenuResponse[]>(`/menus/parent/${parentId}`);
  },

  // 查询所有菜单
  getAll: () => {
    return request.get<any, MenuResponse[]>('/menus');
  },

  // 分页查询菜单
  page: (params: MenuPageParams) => {
    return request.post<any, MenuPageResponse>('/menus/page', params);
  }
};