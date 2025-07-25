import { reactive } from 'vue'
import { Router } from 'vue-router'
import { userApi } from '@/api/usercenter/user'

// 菜单类型接口
interface Menu {
  id: number;
  menuId?: string;
  menuName: string;
  menuCode?: string;
  path?: string;
  component?: string;
  type?: number;
  parentId: number;
  sort?: number;
  icon?: string;
  children?: Menu[];
  permissions?: string[];
}

// 当前用户权限状态
export const userPermissionState = reactive({
  // 用户信息
  userInfo: null as any,
  // 用户拥有的菜单权限列表
  menuPermissions: [] as string[],
  // 用户拥有的菜单树
  visibleMenus: [] as Menu[],
  // 扁平化的可见菜单ID列表
  visibleMenuIds: [] as number[],
  // 是否为管理员
  isAdmin: false,
  // 权限是否已加载
  initialized: false
})

// 递归扁平化菜单树，提取所有菜单ID
function flattenMenuIds(menus: Menu[]): number[] {
  const ids: number[] = [];
  menus.forEach(menu => {
    ids.push(menu.id);
    if (menu.children && menu.children.length > 0) {
      ids.push(...flattenMenuIds(menu.children));
    }
  });
  return ids;
}

// 初始化权限数据
export async function initPermissions() {
  try {
    // 获取当前用户信息和权限（包含角色菜单权限）
    const response = await userApi.getCurrentUserInfo()
    
    if (!response.success || !response.data) {
      console.error('获取用户信息失败:', response.errMessage)
      return false
    }
    
    const { data } = response
    
    // 保存用户信息
    userPermissionState.userInfo = data
    
    // 提取所有角色下的菜单权限代码
    const menuPermissions = new Set<string>()
    
    // 收集所有角色的菜单树
    const allRoleMenus: Menu[] = []
    
    data.roles.forEach(role => {
      // 添加该角色的菜单到总菜单列表
      if (role.menus && role.menus.length > 0) {
        allRoleMenus.push(...role.menus);
      }
      
      role.menus.forEach(menu => {
        if (menu.menuCode) {
          menuPermissions.add(menu.menuCode)
        }
        // 添加菜单的权限列表
        menu.permissions?.forEach(permission => {
          if (permission) {
            menuPermissions.add(permission)
          }
        })
      })
    })
    
    userPermissionState.menuPermissions = Array.from(menuPermissions)
    userPermissionState.visibleMenus = allRoleMenus
    
    // 扁平化菜单ID
    userPermissionState.visibleMenuIds = flattenMenuIds(allRoleMenus)
    
    // 检查是否为管理员角色
    userPermissionState.isAdmin = data.roles.some(role => 
      role.roleCode === 'ROLE_ADMIN'
    )
    
    // 标记初始化完成
    userPermissionState.initialized = true
    return true
  } catch (error) {
    console.error('初始化权限数据失败:', error)
    return false
  }
}

/**
 * 检查用户是否拥有指定的菜单权限
 * @param menuCode 菜单权限代码
 * @returns 是否拥有权限
 */
export function hasPermission(menuCode: string): boolean {
  // 未初始化则返回 false
  if (!userPermissionState.initialized) {
    console.warn('权限系统尚未初始化')
    return false
  }
  
  // 管理员拥有所有权限
  if (userPermissionState.isAdmin) {
    return true
  }
  
  // 检查是否包含指定权限
  const hasPermCode = userPermissionState.menuPermissions.includes(menuCode);
  
  // 返回是否有菜单权限
  return hasPermCode
}

/**
 * 路由导航守卫中使用的权限检查
 * @param router Vue Router 实例
 */
export function setupPermissionGuard(router: Router) {
  router.beforeEach(async (to, from, next) => {
    // 登录和注册页面不需要权限检查
    if (to.path === '/login' || to.path === '/register') {
      // 设置页面标题
      const pageTitle = typeof to.meta?.title === 'string' ? to.meta.title : '用户中心';
      document.title = pageTitle;
      next()
      return
    }
    
    // 如果权限尚未初始化，则先初始化权限
    if (!userPermissionState.initialized) {
      await initPermissions()
    }
    
    // 如果初始化失败且不在登录页，则跳转到登录页
    if (!userPermissionState.initialized && to.path !== '/login') {
      next({ path: '/login' })
      return
    }
    
    // ====== 临时关闭所有权限校验，所有页面都放行 ======
    next()
    return
    
    /*
    // 判断路由是否需要权限检查
    if (to.meta?.requiresAuth) {
      const requiredPermission = to.meta?.permission as string
      // 设置页面标题
      const pageTitle = typeof to.meta?.title === 'string' ? to.meta.title : '用户中心';
      document.title = pageTitle;
      if (requiredPermission) {
        // 检查是否拥有对应权限
        if (hasPermission(requiredPermission)) {
          next() // 有权限，放行
        } else {
          next({ path: '/403' }) // 无权限，跳转到 403 页面
        }
      } else {
        // 路由只需要登录不需要特定权限
        next()
      }
    } else {
      // 不需要权限检查的路由
      const pageTitle = typeof to.meta?.title === 'string' ? to.meta.title : '用户中心';
      document.title = pageTitle;
      next()
    }
    */
  })
}

/**
 * 页面级权限指令 v-permission
 */
export const permissionDirective = {
  mounted(el: HTMLElement, binding: {value: string | string[]}) {
    const { value } = binding
    
    if (!value) {
      return
    }
    
    // 值可以是单个权限字符串或权限数组
    const permissions = Array.isArray(value) ? value : [value]
    
    // 检查是否有权限
    const hasAuth = permissions.some(permission => hasPermission(permission))
    
    // 无权限则将元素从DOM中移除
    if (!hasAuth) {
      // 获取父元素后移除自身
      const parent = el.parentNode
      if (parent) {
        parent.removeChild(el)
      }
    }
  }
}
