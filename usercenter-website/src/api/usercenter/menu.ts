import request from '../config';

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
  type: number; // 菜单类型：1-目录 2-菜单 3-按钮
  menuType: number; // 菜单客户端类型：0-客户端菜单 1-运营端菜单
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
  type: number; // 菜单类型：1-目录 2-菜单 3-按钮
  menuType: number; // 菜单客户端类型：0-客户端菜单 1-运营端菜单
}

// 更新菜单参数类型
interface UpdateMenuParams {
  menuName: string;
  path: string;
  component: string;
  sort: number;
  type: number; // 菜单类型：1-目录 2-菜单 3-按钮
  menuType: number; // 菜单客户端类型：0-客户端菜单 1-运营端菜单
}

// 分页查询参数类型
interface MenuPageParams {
  pageNum: number;
  pageSize: number;
  menuName?: string;
  menuCode?: string;
  systemId?: number;
  type?: number; // 菜单类型：1-目录 2-菜单 3-按钮
  menuType?: number; // 菜单客户端类型：0-客户端菜单 1-运营端菜单
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
    return request.post<any, MenuResponse>('/usercenter/api/menus/create', params);
  },

  // 更新菜单
  update: (id: number, params: UpdateMenuParams) => {
    return request.put<any, MenuResponse>(`/usercenter/api/menus/${id}`, params);
  },

  // 删除菜单
  delete: (id: number) => {
    return request.delete<any, void>(`/usercenter/api/menus/${id}`);
  },

  // 根据ID查询菜单
  getById: (id: number) => {
    return request.get<any, MenuResponse>(`/usercenter/api/menus/${id}`);
  },

  // 查询系统下的所有菜单
  getBySystem: (systemId: number) => {
    return request.get<any, MenuResponse[]>(`/usercenter/api/menus/system/${systemId}`);
  },

  // 查询子菜单
  getByParent: (parentId: number) => {
    return request.get<any, MenuResponse[]>(`/usercenter/api/menus/parent/${parentId}`);
  },

  // 查询所有菜单
  getAll: () => {
    return request.get<any, any>('/usercenter/api/menus');
  },

  // 分页查询菜单（已改为查询全部菜单，不分页）
  page: () => {
    return request.get<any, MenuResponse[]>('/usercenter/api/menus');
  }
};